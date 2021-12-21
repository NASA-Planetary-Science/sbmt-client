//package edu.jhuapl.sbmt.stateHistory.deprecated;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.io.File;
//
//import javax.swing.JButton;
//import javax.swing.JFileChooser;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
//import javax.swing.filechooser.FileNameExtensionFilter;
//
//import org.apache.commons.io.FilenameUtils;
//
//import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.sbmt.client.SmallBodyModel;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryCollection;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
//import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel.StateHistoryKey;
//import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable;
//import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.TimeIntervalTableModel;
//import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable.columns;
//
//public class TimeIntervalTablePanel extends JPanel implements TableModelListener
//{
//
//    private TimeIntervalTable timeTable;
//    private JButton btnLoadIntervalFrom;
//    private JButton btnRemoveInterval;
//    private JPanel panel;
//    private JButton button;
//    StateHistoryCollection intervals;
//
//    private ModelManager modelManager;
//    private Renderer renderer;
//    private ViewOptionsPanel viewOptionsPanel;
//
//    /**
//     * Create the panel.
//     * @param simulationMarkerPanel
//     */
//    public TimeIntervalTablePanel(StateHistoryCollection intervals, final ModelManager modelManager, Renderer renderer, ViewOptionsPanel simulationMarkerPanel)
//    {
//        this.modelManager = modelManager;
//        this.intervals = intervals;
//        this.renderer = renderer;
//        this.viewOptionsPanel = simulationMarkerPanel;
//
//        GridBagLayout gbl_timeTablePanel = new GridBagLayout();
//        gbl_timeTablePanel.columnWidths = new int[]{148};
//        gbl_timeTablePanel.rowHeights = new int[]{0, 0};
//        gbl_timeTablePanel.columnWeights = new double[]{1.0};
//        gbl_timeTablePanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
//        setLayout(gbl_timeTablePanel);
//
//        SmallBodyModel bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
//        timeTable = new TimeIntervalTable(intervals, bodyModel, renderer);
//        timeTable.getModel().addTableModelListener(this);
//        timeTable.addMouseListener(new TableMouseHandler());
//        GridBagConstraints gbc_table = new GridBagConstraints();
//        gbc_table.insets = new Insets(0, 0, 5, 0);
//        gbc_table.gridx = 0;
//        gbc_table.gridy = 0;
//        gbc_table.fill = GridBagConstraints.BOTH;
//        // scrollpane for the table
//        JScrollPane tableScrollPane = new JScrollPane(timeTable);
//        tableScrollPane.setPreferredSize(new Dimension(10000, 10000));
//        add(tableScrollPane, gbc_table);
//
//        panel = new JPanel();
//        GridBagConstraints gbc_panel = new GridBagConstraints();
//        gbc_panel.fill = GridBagConstraints.BOTH;
//        gbc_panel.gridx = 0;
//        gbc_panel.gridy = 1;
//        add(panel, gbc_panel);
//        GridBagLayout gbl_panel = new GridBagLayout();
//        gbl_panel.columnWidths = new int[]{112, 115, 131, 0};
//        gbl_panel.rowHeights = new int[]{0, 0};
//        gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
//        gbl_panel.rowWeights = new double[]{4.9E-324, Double.MIN_VALUE};
//        panel.setLayout(gbl_panel);
//
//        btnLoadIntervalFrom = new JButton("Load Interval From File");
//        btnLoadIntervalFrom.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                TimeIntervalTableModel model = (TimeIntervalTableModel) timeTable.getModel();
//
//                JFileChooser fc = new JFileChooser();
//                fc.addChoosableFileFilter(new FileNameExtensionFilter("timefiles", "csv"));
//
//                int returnVal = fc.showOpenDialog(TimeIntervalTablePanel.this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION)
//                {
//                    File file = fc.getSelectedFile();
//                    SmallBodyModel bodyModel = (SmallBodyModel) modelManager.getPolyhedralModel();
//                    model.loadIntervalFromFile(file, bodyModel);
//                }
//            }
//        });
//        GridBagConstraints gbc_btnLoadIntervalFrom = new GridBagConstraints();
//        gbc_btnLoadIntervalFrom.insets = new Insets(0, 0, 0, 5);
//        gbc_btnLoadIntervalFrom.gridx = 0;
//        gbc_btnLoadIntervalFrom.gridy = 0;
//        panel.add(btnLoadIntervalFrom, gbc_btnLoadIntervalFrom);
//
//        button = new JButton("Save Selected Interval");
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//
//                if (timeTable.getSelectedRowCount() == 1)
//                {
//                    TimeIntervalTableModel model = (TimeIntervalTableModel) timeTable.getModel();
//                    // TODO set default filename to "name" in name column
//                    JFileChooser fc = new JFileChooser();
//
//                    int returnVal = fc.showSaveDialog(TimeIntervalTablePanel.this);
//                    if (returnVal == JFileChooser.APPROVE_OPTION)
//                    {
//                        File file = fc.getSelectedFile();
//                        if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("csv")) {
//                            // filename is OK as-is
//                        } else {
//                            // remove the extension (if any) and replace it with ".csv"
//                            file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".csv");
//                        }
//                        model.saveRowToFile(timeTable.getSelectedRow(), file);
//                    }
//
//                }
//                else {
//                    JOptionPane.showMessageDialog(null, "You must have exactly one row of the table selected to save an interval", "Error",
//                            JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//
//            }
//
//        });
//
//        GridBagConstraints gbc_button = new GridBagConstraints();
//        gbc_button.insets = new Insets(0, 0, 0, 5);
//        gbc_button.gridx = 1;
//        gbc_button.gridy = 0;
//        panel.add(button, gbc_button);
//
//        btnRemoveInterval = new JButton("Remove Selected Intervals");
//        btnRemoveInterval.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e)
//            {
//                if (timeTable.getSelectedRowCount() != 0)
//                {
//                    TimeIntervalTableModel model = (TimeIntervalTableModel) timeTable.getModel();
//                    model.removeRows(timeTable.getSelectedRows());
//                }
//            }
//        });
//        GridBagConstraints gbc_btnRemoveInterval = new GridBagConstraints();
//        gbc_btnRemoveInterval.gridx = 2;
//        gbc_btnRemoveInterval.gridy = 0;
//        panel.add(btnRemoveInterval, gbc_btnRemoveInterval);
//    }
//
//    public void addIntervalToTable(StateHistoryModel interval, Renderer renderer) {
//        timeTable.addInterval(interval, renderer);
//    }
//
//    @Override
//    public void tableChanged(TableModelEvent e)
//    {
//        int row = e.getFirstRow();
//        int col = e.getColumn();
//
//        if (intervals == null || row + 1 > intervals.size()) { // adding a new row
//            // do nothing, we just added a row.
//        }
//        else {
//            StateHistoryKey key = intervals.getKeyFromRow(row);
//            StateHistoryModel currentRun = intervals.getRunFromRow(row);
//
//            if (col == columns.MAP.ordinal()) { // map trajectory
//                if ((Boolean)timeTable.getValueAt(row, col)) {
//                    currentRun.setActorVisibility("Trajectory", true);
//                    currentRun.showTrajectory(((Boolean)timeTable.getValueAt(row, columns.SHOW.ordinal())));
//                    viewOptionsPanel.setEnabled(true);
//                }
//                else {
//                    currentRun.setActorVisibility("Trajectory", false);
//                    timeTable.setValueAt(false, row, columns.SHOW.ordinal());
//                    viewOptionsPanel.setEnabled(false);
//                }
//            }
//            else if (col == columns.SHOW.ordinal()) // show trajectory
//            {
//                if ((Boolean)timeTable.getValueAt(row, columns.MAP.ordinal()))
//                {
//                    currentRun.setActorVisibility("Trajectory", true);
//
//                }
//                if ((Boolean)timeTable.getValueAt(row, col))
//                {
//                    intervals.setCurrentRun(key);
//                    currentRun = intervals.getCurrentRun();
//                    currentRun.showTrajectory(true);
////                    viewOptionsPanel.setEnabled(true);
//                    // uncheck show for all other rows
//                    int numRows = timeTable.getRowCount();
//                    for (int iRow = 0; iRow < numRows; iRow++)
//                    {
//                        if (iRow != row)
//                            timeTable.setValueAt(false, iRow, columns.SHOW.ordinal());
//                    }
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
//                Color newColor = (Color)timeTable.getValueAt(row, col);
//                currentRun.setTrajectoryColor(new double[]{newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha()});
//            } else if (col == columns.NAME.ordinal()) {
//                currentRun.setTrajectoryName((String)timeTable.getValueAt(row, col));
//            } else if (col == columns.DESC.ordinal()) {
//                currentRun.setDescription((String)timeTable.getValueAt(row, col));
//            } else if (col == columns.LINE.ordinal()) {
//                currentRun.setTrajectoryLineThickness(Double.parseDouble((String)timeTable.getValueAt(row, col)));
//            }
//        }
//
//
//    }
//
//    class TableMouseHandler extends MouseAdapter
//    {
//        @Override
//        public void mouseClicked(MouseEvent e)
//        {
//            int row = timeTable.rowAtPoint(e.getPoint());
//            int col = timeTable.columnAtPoint(e.getPoint());
//
//            if (e.getClickCount() == 1 && row >=0)
//            { // clicked any row, set currentRun to that row (all view options and such)
//                intervals.setCurrentRun(intervals.getKeyFromRow(row));
//            }
//
//            if (e.getClickCount() == 2 && row >= 0 && col == columns.COLOR.ordinal())
//            {
//                StateHistoryKey key = intervals.getKeyFromRow(row);
//                StateHistoryModel currentRun = intervals.getRun(key);
//                double[] currColor = currentRun.getTrajectoryColor();
//                Color color = ColorChooser.showColorChooser(
//                        JOptionPane.getFrameForComponent(timeTable),
//                        new int[]{(int) currColor[0], (int)currColor[1], (int)currColor[2], (int)currColor[3]});
//
//                if (color == null)
//                    return;
//
//                int[] c = new int[4];
//                c[0] = color.getRed();
//                c[1] = color.getGreen();
//                c[2] = color.getBlue();
//                c[3] = color.getAlpha();
//
//                timeTable.setValueAt(new Color(c[0], c[1], c[2], c[3]), row, columns.COLOR.ordinal());
//            }
//        }
//    }
//
//}
//
