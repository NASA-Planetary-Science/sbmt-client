package edu.jhuapl.sbmt.stateHistory.ui.lidars;

import java.awt.Color;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.sbmt.lidar.gui.color.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class PlannedLidarTrackItemHandler extends BasicItemHandler<PlannedLidarTrack, PlannedLidarTrackColumnLookup>
{
	PlannedLidarTrackCollection plannedLidarTrackCollection;

	public PlannedLidarTrackItemHandler(PlannedLidarTrackCollection aManager, QueryComposer<PlannedLidarTrackColumnLookup> aComposer)
	{
		super(aComposer);
		plannedLidarTrackCollection = aManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(PlannedLidarTrack track, PlannedLidarTrackColumnLookup aEnum)
	{
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		String timeString;
		switch (aEnum)
		{
			case Show:
				return track.isShowing();

			case Color:
				return new ConstColorProvider(track.getColor());
			case Instrument:
				return track.getInstrumentName();
			case TrackStartTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(track.getStartTime());
				return timeString.substring(0, 23);
			case TrackStopTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(track.getStopTime());
				return timeString.substring(0, 23);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(PlannedLidarTrack track, PlannedLidarTrackColumnLookup aEnum, Object aValue)
	{
		switch (aEnum)
		{
			case Show:
				track.setShowing((Boolean)aValue);
				plannedLidarTrackCollection.setVisibility(track, (Boolean)aValue);
				break;
			case Color:
				track.setColor((Color)aValue);
				break;
			default:
				throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
		}
	}
}
