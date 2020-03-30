package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.ui.version2;
//
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.border.TitledBorder;
//
//import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable;
//
///**
// * @author steelrj1
// *
// */
//public class StateHistoryIntervalSelectionPanel extends JPanel
//{
//    /**
//     * JButton used to load a state history interval
//     */
//    private JButton loadButton;
//
//    /**
//     * JButton used to save a state history interval
//     */
//    private JButton saveButton;
//
//    /**
//     * JButton used to remove a state history interval from the table
//     */
//    private JButton removeButton;
//
//    /**
//     * The
//     */
//    private TimeIntervalTable table;
//
//    /**
//     *
//     */
//    private JScrollPane tableScrollPane;
//
//    /**
//     *
//     */
//    private JPanel tablePanel;
//
//	/**
//	 * Constructor.
//	 */
//	public StateHistoryIntervalSelectionPanel()
//	{
//		initUI();
//	}
//
//	/**
//	 * Initializes the user interfaces
//	 */
//	private void initUI()
//	{
//        setBorder(new TitledBorder(null, "Interval Selection",
//                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//
//        tablePanel = new JPanel();
//        add(tablePanel);
//        tablePanel.setLayout(new BorderLayout(0, 0));
//
//        JPanel buttonPanel = new JPanel();
//        add(buttonPanel);
//        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
//
//        loadButton = new JButton("Load...");
//        buttonPanel.add(loadButton);
//
//        saveButton = new JButton("Save...");
//        buttonPanel.add(saveButton);
//
//        removeButton = new JButton("Remove Selected");
//        buttonPanel.add(removeButton);
//	}
//
//
//    /**
//     * @return
//     */
//    public JButton getLoadButton()
//    {
//        return loadButton;
//    }
//
//    /**
//     * @return
//     */
//    public JButton getSaveButton()
//    {
//        return saveButton;
//    }
//
//    /**
//     * @return
//     */
//    public JButton getRemoveButton()
//    {
//        return removeButton;
//    }
//
//    /**
//     * @return
//     */
//    public TimeIntervalTable getTable()
//    {
//        return table;
//    }
//
//    /**
//     * @param table
//     */
//    public void setTable(TimeIntervalTable table)
//    {
//        this.table = table;
//        this.table.setFillsViewportHeight(true);
//        tableScrollPane = new JScrollPane(table);
//        tableScrollPane.setPreferredSize(new Dimension(150, 150));
//        tableScrollPane.setMaximumSize(new Dimension(150, 150));
//        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
//    }
//
//    /**
//     * @return
//     */
//    public JScrollPane getTableScrollPane()
//    {
//        return tableScrollPane;
//    }
//}