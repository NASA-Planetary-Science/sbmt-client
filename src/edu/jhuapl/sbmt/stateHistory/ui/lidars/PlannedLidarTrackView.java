package edu.jhuapl.sbmt.stateHistory.ui.lidars;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;

public class PlannedLidarTrackView extends JPanel
{
	PlannedLidarTrackTableView table;

	public PlannedLidarTrackView(PlannedLidarTrackCollection collection, SmallBodyViewConfig config)
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
