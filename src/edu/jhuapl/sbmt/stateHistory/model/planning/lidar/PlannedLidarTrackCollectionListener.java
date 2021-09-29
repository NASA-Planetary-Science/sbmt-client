package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import edu.jhuapl.sbmt.lidar.LidarTrack;

public interface PlannedLidarTrackCollectionListener
{
	public void trackAdded(LidarTrack track, PlannedLidarTrack plannedTrack);
}
