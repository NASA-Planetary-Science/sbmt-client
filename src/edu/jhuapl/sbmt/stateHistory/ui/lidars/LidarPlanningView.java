package edu.jhuapl.sbmt.stateHistory.ui.lidars;

import javax.swing.JPanel;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;

public class LidarPlanningView extends JPanel
{
	PlannedLidarTrackCollection collection;

	public LidarPlanningView(PlannedLidarTrackCollection collection, SmallBodyViewConfig config)
	{
		this.collection = collection;
	}


}
