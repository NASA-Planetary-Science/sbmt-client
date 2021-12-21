package edu.jhuapl.sbmt.stateHistory.ui.lidars.schedule;

import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackVtkCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackScheduleCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class PlannedLidarTrackScheduleItemHandler extends BasicItemHandler<PlannedLidarTrackVtkCollection, PlannedLidarTrackScheduleColumnLookup>
{
	PlannedLidarTrackScheduleCollection plannedLidarTrackCollection;

	public PlannedLidarTrackScheduleItemHandler(PlannedLidarTrackScheduleCollection aManager, QueryComposer<PlannedLidarTrackScheduleColumnLookup> aComposer)
	{
		super(aComposer);
		plannedLidarTrackCollection = aManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(PlannedLidarTrackVtkCollection lidarTrackCollection, PlannedLidarTrackScheduleColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case Show:
				return lidarTrackCollection.isShowing();
			case Details:
				return lidarTrackCollection.isDisplayingDetails();
//			case Color:
//				return new ConstColorProvider(lidarTrackCollection.getColor());
			case Filename:
				return lidarTrackCollection.getFilename();
			case StateHistory:
				return lidarTrackCollection.getStateHistoryMetadata().getStateHistoryName();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(PlannedLidarTrackVtkCollection lidarTrackCollection, PlannedLidarTrackScheduleColumnLookup aEnum, Object aValue)
	{
		switch (aEnum)
		{
			case Show:
				lidarTrackCollection.setShowing((Boolean)aValue);
				break;
			case Details:
				lidarTrackCollection.setDisplayingDetails((Boolean)aValue);
				plannedLidarTrackCollection.showDetailedScheduleFor(lidarTrackCollection);
				break;
//			case Color:
//				lidarTrackCollection.setColor(((ConstColorProvider)aValue).getBaseColor());
//				lidarTrackCollection.getAllItems().forEach(item -> item.setColor((Color)aValue));
//				break;
			default:
				throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
		}
	}
}
