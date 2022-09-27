package edu.jhuapl.sbmt.stateHistory.ui.lidars.schedule;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackScheduleCollection;


public class PlannedLidarTrackScheduleView extends JPanel
{
	PlannedLidarTrackScheduleTableView table;

	public PlannedLidarTrackScheduleView(PlannedLidarTrackScheduleCollection collection)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedLidarTrackScheduleTableView(collection);
		table.setup();
		add(table);
	}

	/**
	 * @return the table
	 */
	public PlannedLidarTrackScheduleTableView getTable()
	{
		return table;
	}
}
