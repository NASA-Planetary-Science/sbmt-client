package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.controllers.StateHistoryController.RunInfo;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.TimeIntervalTableModel;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.columns;

public class StateHistoryIntervalSelectionController
{

	public StateHistoryIntervalSelectionController()
	{
		// TODO Auto-generated constructor stub
	}

	private void initializeIntervalSelectionPanel()
    {
        view.setTable(new TimeIntervalTable(runs, bodyModel, renderer));
        view.getTable().getModel().addTableModelListener(this);
        view.getTable().addMouseListener(new TableMouseHandler());
        view.getLoadButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();

                JFileChooser fc = new JFileChooser();
                fc.addChoosableFileFilter(new FileNameExtensionFilter("timefiles", "csv"));

                int returnVal = fc.showOpenDialog(view);

                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    File file = fc.getSelectedFile();
                    SmallBodyModel bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
                    model.loadIntervalFromFile(file, bodyModel);
                    setViewControlPanelEnabled(true);

                }
            }
        });
        view.getSaveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (view.getTable().getSelectedRowCount() == 1)
                {
                    TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();
                    // TODO set default filename to "name" in name column
                    JFileChooser fc = new JFileChooser();

                    int returnVal = fc.showSaveDialog(view);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {
                            // filename is OK as-is
                        } else {
                            // remove the extension (if any) and replace it with ".csv"
                            file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".csv");
                        }
                        model.saveRowToFile(view.getTable().getSelectedRow(), file);
                    }

                }
                else {
                    JOptionPane.showMessageDialog(null, "You must have exactly one row of the table selected to save an interval", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }


            }

        });

        view.getRemoveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (view.getTable().getSelectedRowCount() != 0)
                {
                    TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();
                    model.removeRows(view.getTable().getSelectedRows());
                }
            }
        });
    }


	class TableMouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            TimeIntervalTable timeTable = view.getTable();
            int row = timeTable.rowAtPoint(e.getPoint());
            int col = timeTable.columnAtPoint(e.getPoint());

            if (e.getClickCount() == 1 && row >=0)
            { // clicked any row, set currentRun to that row (all view options and such)
                runs.setCurrentRun(runs.getKeyFromRow(row));
            }

            if (e.getClickCount() == 2 && row >= 0 && col == columns.COLOR.ordinal())
            {
                StateHistoryKey key = runs.getKeyFromRow(row);
                StateHistoryModel currentRun = runs.getRun(key);
                double[] currColor = currentRun.getTrajectoryColor();
                Color color = ColorChooser.showColorChooser(
                        JOptionPane.getFrameForComponent(timeTable),
                        new int[]{(int) currColor[0], (int)currColor[1], (int)currColor[2], (int)currColor[3]});

                if (color == null)
                    return;

                int[] c = new int[4];
                c[0] = color.getRed();
                c[1] = color.getGreen();
                c[2] = color.getBlue();
                c[3] = color.getAlpha();

                timeTable.setValueAt(new Color(c[0], c[1], c[2], c[3]), row, columns.COLOR.ordinal());
            }
        }
    }

	public void initializeRunList() throws IOException
    {
        if (initialized)
            return;

        MapUtil configMap = new MapUtil(getConfigFilename());

        boolean needToUpgradeConfigFile = false;
        String[] runNames = configMap.getAsArray(StateHistoryModel.RUN_NAMES);
        String[] runFilenames = configMap.getAsArray(StateHistoryModel.RUN_FILENAMES);
        if (runFilenames == null)
        {
            // Mark that we need to upgrade config file to latest version
            // which we'll do at end of function.
            needToUpgradeConfigFile = true;
            initialized = true;
            return;
        }

        int numRuns = runFilenames.length;
        for (int i=0; i<numRuns; ++i)
        {
            RunInfo runInfo = new RunInfo();
            runInfo.name = runNames[i];
            runInfo.runfilename = runFilenames[i];

        }

        if (needToUpgradeConfigFile)
            updateConfigFile();

        initialized = true;
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int col = e.getColumn();

        if (runs == null || row + 1 > runs.size()) { // adding a new row
            // do nothing, we just added a row.
        }
        else {
            StateHistoryKey key = runs.getKeyFromRow(row);
            StateHistoryModel currentRun = runs.getRunFromRow(row);

            if (col == columns.MAP.ordinal()) { // map trajectory
                if ((Boolean)view.getTable().getValueAt(row, col)) {
                    currentRun.setActorVisibility("Trajectory", true);
                    currentRun.showTrajectory(((Boolean)view.getTable().getValueAt(row, columns.SHOW.ordinal())));
                    view.getViewControlPanel().setEnabled(true);
                }
                else {
                    currentRun.setActorVisibility("Trajectory", false);
                    view.getTable().setValueAt(false, row, columns.SHOW.ordinal());
                    view.getViewControlPanel().setEnabled(false);
                }
            }
            else if (col == columns.SHOW.ordinal()) // show trajectory
            {
                if ((Boolean)view.getTable().getValueAt(row, columns.MAP.ordinal()))
                {
                    currentRun.setActorVisibility("Trajectory", true);

                }
                if ((Boolean)view.getTable().getValueAt(row, col))
                {
                    runs.setCurrentRun(key);
                    currentRun = runs.getCurrentRun();
                    currentRun.showTrajectory(true);
//                    viewOptionsPanel.setEnabled(true);
                    // uncheck show for all other rows
                    int numRows = view.getTable().getRowCount();
                    for (int iRow = 0; iRow < numRows; iRow++)
                    {
                        if (iRow != row)
                            view.getTable().setValueAt(false, iRow, columns.SHOW.ordinal());
                    }
                    setSpacecraftView(runs.getCurrentRun());
                    return;
                }
                else // hide trajectory
                {
                    currentRun.showTrajectory(false);
                    // TODO uncheck all of the viewOptions
                    // (show spacecraft, show lighting, etc) ??
//                    viewOptionsPanel.setEnabled(false);
                }
            } else if (col == columns.COLOR.ordinal()){
                Color newColor = (Color)view.getTable().getValueAt(row, col);
                currentRun.setTrajectoryColor(new double[]{newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha()});
            } else if (col == columns.NAME.ordinal()) {
                currentRun.setTrajectoryName((String)view.getTable().getValueAt(row, col));
            } else if (col == columns.DESC.ordinal()) {
                currentRun.setDescription((String)view.getTable().getValueAt(row, col));
            } else if (col == columns.LINE.ordinal()) {
                currentRun.setTrajectoryLineThickness(Double.parseDouble((String)view.getTable().getValueAt(row, col)));
            }
        }
    }
}
