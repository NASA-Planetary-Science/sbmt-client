package edu.jhuapl.sbmt.stateHistory.ui.lidars;

import javax.swing.JPanel;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackVtkCollection;

public class LidarPlanningView extends JPanel
{
	PlannedLidarTrackVtkCollection collection;

	public LidarPlanningView(PlannedLidarTrackVtkCollection collection, SmallBodyViewConfig config)
	{
		this.collection = collection;
	}


}
