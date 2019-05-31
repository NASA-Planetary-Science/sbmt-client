package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.util.List;

import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.model.lidar.LidarTrack;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * ItemHandler used to process lidar Tracks.
 *
 * @author lopeznr1
 */
class TrackItemHandler extends BasicItemHandler<LidarTrack>
{
	// Ref vars
	private final LidarTrackManager refManager;

	/**
	 * Standard Constructor
	 */
	public TrackItemHandler(LidarTrackManager aManager, QueryComposer<?> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(LidarTrack aTrack, int aColIdx)
	{
		switch (aColIdx)
		{
			case 0:
				return refManager.getIsVisible(aTrack);
			case 1:
				return refManager.getColor(aTrack);
			case 2:
				return aTrack.getId();
			case 3:
				return aTrack.getNumberOfPoints();
			case 4:
				return aTrack.getTimeBeg();
			case 5:
				return aTrack.getTimeEnd();
			case 6:
				return getSourceFileString(aTrack);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Index: " + aColIdx);
	}

	@Override
	public void setColumnValue(LidarTrack aTrack, int aColIdx, Object aValue)
	{
		if (aColIdx == 0)
			refManager.setIsVisible(aTrack, (boolean) aValue);
		else if (aColIdx == 1)
			refManager.setColor(aTrack, (Color) aValue);
		else
			throw new UnsupportedOperationException("Column is not supported. Index: " + aColIdx);
	}

	/**
	 * Utility method that returns the appropriate "Source Files" string for the
	 * specified track.
	 */
	public static String getSourceFileString(LidarTrack aTrack)
	{
		List<String> sourceL = aTrack.getSourceList();
		if (sourceL.size() == 0)
			return "";

		StringBuffer tmpSB = new StringBuffer();
		for (String aSource : sourceL)
		{
			tmpSB.append(" | " + aSource);
			if (tmpSB.length() > 1000)
			{
				tmpSB.append("...");
				break;
			}
		}
		tmpSB.delete(0, 3);

		return tmpSB.toString();
	}

}
