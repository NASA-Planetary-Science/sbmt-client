package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.TimeIntervalTableModel;
import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.columns;
import edu.jhuapl.sbmt.stateHistory.ui.version2.table.StateHistoryTableView;

public class StateHistoryIntervalSelectionController implements TableModelListener
{

//    private JTable optionsTable;
    private SmallBodyModel bodyModel;
    private Renderer renderer;
    private StateHistoryCollection runs;
//    private StateHistoryIntervalSelectionPanel view;
    private StateHistoryTableView view;
    private StateHistoryModel historyModel;

	public StateHistoryIntervalSelectionController(StateHistoryModel historyModel, SmallBodyModel bodyModel, Renderer renderer)
	{
		this.renderer = renderer;
		this.bodyModel = bodyModel;
		this.historyModel = historyModel;
		this.runs = historyModel.getRuns();
		this.view = new StateHistoryTableView(runs);
		System.out.println("StateHistoryIntervalSelectionController: StateHistoryIntervalSelectionController: runs " + runs);
		initializeIntervalSelectionPanel();
	}

	private void initializeIntervalSelectionPanel()
    {
		view = new StateHistoryTableView(runs);
//        view.setTable(new TimeIntervalTable(runs, bodyModel, renderer));
//        view.getTable().getModel().addTableModelListener(this);
//        view.getTable().addMouseListener(new TableMouseHandler());
        view.getLoadStateHistoryButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                fc.addChoosableFileFilter(new FileNameExtensionFilter("timefiles", "csv"));
                if (fc.showOpenDialog(view) != JFileChooser.APPROVE_OPTION) return;

            	historyModel.loadIntervalFromFile(fc.getSelectedFile(), bodyModel);
            }
        });
        view.getSaveStateHistoryButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (view.getTable().getSelectedRowCount() == 1)
                {
                    TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();
                    // TODO set default filename to "name" in name column
                    JFileChooser fc = new JFileChooser();

                    if (fc.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;

                    File file = fc.getSelectedFile();
                    if (!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {
                        // remove the extension (if any) and replace it with ".csv"
                        file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".csv");
                    }
                    StateHistory history = model.getStateHistoryAtRow(view.getTable().getSelectedRow());
                    historyModel.saveRowToFile(history, file);
                }
                else {
                    JOptionPane.showMessageDialog(null, "You must have exactly one row of the table selected to save an interval", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

        });

        view.getRemoveStateHistoryButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (view.getTable().getSelectedRowCount() != 0)
                {
                    TimeIntervalTableModel model = (TimeIntervalTableModel) view.getTable().getModel();
                    model.removeRows(view.getTable().getSelectedRows());
                }
            }
        });

        historyModel.addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener()
		{
			@Override
			public void historySegmentCreated(StateHistory historySegment)
			{
				System.out.println(
						"StateHistoryIntervalSelectionController.initializeIntervalSelectionPanel().new DefaultStateHistoryModelChangedListener() {...}: historySegmentCreated: number runs " + runs.size());

				view.getTable().repaint();
				//				view.getTable().addInterval(historySegment, renderer);
			}
		});
    }


	class TableMouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            JTable timeTable = view.getTable();
            int row = timeTable.rowAtPoint(e.getPoint());
            int col = timeTable.columnAtPoint(e.getPoint());

            if (e.getClickCount() == 1 && row >=0)
            { // clicked any row, set currentRun to that row (all view options and such)
                runs.setCurrentRun(runs.getKeyFromRow(row));
            }

            if (e.getClickCount() == 2 && row >= 0 && col == columns.COLOR.ordinal())
            {
                StateHistoryKey key = runs.getKeyFromRow(row);
                StateHistory currentRun = runs.getRun(key);
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
            StateHistory currentRun = runs.getRunFromRow(row);

//            if (col == columns.MAP.ordinal()) { // map trajectory
//                if ((Boolean)view.getTable().getValueAt(row, col)) {
//                    currentRun.setActorVisibility("Trajectory", true);
//                    currentRun.showTrajectory(((Boolean)view.getTable().getValueAt(row, columns.SHOW.ordinal())));
//                    view.getViewControlPanel().setEnabled(true);
//                }
//                else {
//                    currentRun.setActorVisibility("Trajectory", false);
//                    view.getTable().setValueAt(false, row, columns.SHOW.ordinal());
//                    view.getViewControlPanel().setEnabled(false);
//                }
//            }
//            else if (col == columns.SHOW.ordinal()) // show trajectory
//            {
//                if ((Boolean)view.getTable().getValueAt(row, columns.MAP.ordinal()))
//                {
//                    currentRun.setActorVisibility("Trajectory", true);
//
//                }
//                if ((Boolean)view.getTable().getValueAt(row, col))
//                {
//                    runs.setCurrentRun(key);
//                    currentRun = runs.getCurrentRun();
//                    currentRun.showTrajectory(true);
////                    viewOptionsPanel.setEnabled(true);
//                    // uncheck show for all other rows
//                    int numRows = view.getTable().getRowCount();
//                    for (int iRow = 0; iRow < numRows; iRow++)
//                    {
//                        if (iRow != row)
//                            view.getTable().setValueAt(false, iRow, columns.SHOW.ordinal());
//                    }
//                    setSpacecraftView(runs.getCurrentRun());
//                    return;
//                }
//                else // hide trajectory
//                {
//                    currentRun.showTrajectory(false);
//                    // TODO uncheck all of the viewOptions
//                    // (show spacecraft, show lighting, etc) ??
////                    viewOptionsPanel.setEnabled(false);
//                }
//            } else if (col == columns.COLOR.ordinal()){
//                Color newColor = (Color)view.getTable().getValueAt(row, col);
//                currentRun.getTrajectory().setTrajectoryColor(new double[]{newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha()});
//            } else if (col == columns.NAME.ordinal()) {
//                currentRun.getTrajectory().setTrajectoryName((String)view.getTable().getValueAt(row, col));
//            } else if (col == columns.DESC.ordinal()) {
//                currentRun.getTrajectory().setTrajectoryDescription((String)view.getTable().getValueAt(row, col));
//            } else if (col == columns.LINE.ordinal()) {
//                currentRun.getTrajectory().setTrajectoryLineThickness(Double.parseDouble((String)view.getTable().getValueAt(row, col)));
//            }
        }
    }

	public StateHistoryTableView getView()
	{
		return view;
	}

    //
    // a custom table model that was tried to display map and show options for multiple trajectories. Not used because multiple trajectory showing would mess up the animation.
    //

//    class OptionsTableModel extends DefaultTableModel
//    {
//        public OptionsTableModel(Object[][] data, String[] columnNames)
//        {
//            super(data, columnNames);
//        }
//
//        public boolean isCellEditable(int row, int column)
//        {
//            if (column == 2 && !(boolean)optionsTable.getValueAt(0, 1)) //|| column ==6 || column ==7)
//                return false;
//            else
//                return true;
//        }
//    }
}
