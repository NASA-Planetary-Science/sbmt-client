package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.ui.TimeIntervalTable;

/**
 * @author steelrj1
 *
 */
public class StateHistoryIntervalSelectionPanel extends JPanel
{
    /**
     *
     */
    private JButton loadButton;
    /**
     *
     */
    private JButton saveButton;
    /**
     *
     */
    private JButton removeButton;
    /**
     *
     */
    private TimeIntervalTable table;
    /**
     *
     */
    private JScrollPane tableScrollPane;
    /**
     *
     */
    private JPanel panel_9;

	/**
	 *
	 */
	public StateHistoryIntervalSelectionPanel()
	{
		initUI();
	}

	/**
	 *
	 */
	private void initUI()
	{
        setBorder(new TitledBorder(null, "Interval Selection",
                        TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(
                new BoxLayout(this, BoxLayout.Y_AXIS));

        panel_9 = new JPanel();
        add(panel_9);
        panel_9.setLayout(new BorderLayout(0, 0));

        JPanel panel_8 = new JPanel();
        add(panel_8);
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        loadButton = new JButton("Load...");
        panel_8.add(loadButton);

        saveButton = new JButton("Save...");
        panel_8.add(saveButton);

        removeButton = new JButton("Remove Selected");
        panel_8.add(removeButton);
	}


    /**
     * @return
     */
    public JButton getLoadButton()
    {
        return loadButton;
    }

    /**
     * @return
     */
    public JButton getSaveButton()
    {
        return saveButton;
    }

    /**
     * @return
     */
    public JButton getRemoveButton()
    {
        return removeButton;
    }

    /**
     * @return
     */
    public TimeIntervalTable getTable()
    {
        return table;
    }

    /**
     * @param table
     */
    public void setTable(TimeIntervalTable table)
    {
        this.table = table;
//        this.table.setPreferredSize(new Dimension(intervalSelectionPanel.getWidth(), table.getHeight()));
//        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        this.table.setPreferredScrollableViewportSize(table.getPreferredSize());
        this.table.setFillsViewportHeight(true);
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(150, 150));
        tableScrollPane.setMaximumSize(new Dimension(150, 150));
//        tableScrollPane.setPreferredSize(new Dimension(intervalSelectionPanel.getWidth(), tableScrollPane.getHeight()));
        panel_9.add(tableScrollPane, BorderLayout.CENTER);

//        tableScrollPane.setViewportView(table);
    }

    /**
     * @return
     */
    public JScrollPane getTableScrollPane()
    {
        return tableScrollPane;
    }

}
