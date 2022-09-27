package edu.jhuapl.sbmt.stateHistory.ui.lidars;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackVtkCollection;

public class PlannedLidarTrackView extends JPanel
{
	PlannedLidarTrackTableView table;

	public PlannedLidarTrackView(PlannedLidarTrackVtkCollection collection)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedLidarTrackTableView(collection);
		table.setup();
		add(table);
	}

	/**
	 * @return the table
	 */
	public PlannedLidarTrackTableView getTable()
	{
		return table;
	}
}
