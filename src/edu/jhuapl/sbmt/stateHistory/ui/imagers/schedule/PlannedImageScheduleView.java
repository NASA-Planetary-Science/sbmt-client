package edu.jhuapl.sbmt.stateHistory.ui.imagers.schedule;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageScheduleCollection;

public class PlannedImageScheduleView extends JPanel
{
	PlannedImageScheduleTableView table;

	public PlannedImageScheduleView(PlannedImageScheduleCollection collection)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedImageScheduleTableView(collection);
		table.setup();
		add(table);
	}

	/**
	 * @return the table
	 */
	public PlannedImageScheduleTableView getTable()
	{
		return table;
	}
}
