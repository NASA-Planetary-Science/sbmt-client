package edu.jhuapl.sbmt.spectrum.ui;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;

import glum.gui.dock.LookUp;
import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class SpectrumItemHandler extends BasicItemHandler<BasicSpectrum, LookUp>
{
	private final SpectraCollection refManager; 	//was LidarTrackManager for OLA

	public SpectrumItemHandler(SpectraCollection aManager, QueryComposer<LookUp> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(BasicSpectrum aTrack, LookUp aEnum)
	{
		switch (aEnum)
		{
//			case IsVisible:
//				return refManager.getIsVisible(aTrack);
//			case Color:
//				return refManager.getColorProviderTarget(aTrack);
//			case Name:
//				return aTrack.getId();
//			case NumPoints:
//				return aTrack.getNumberOfPoints();
//			case BegTime:
//				return aTrack.getTimeBeg();
//			case EndTime:
//				return aTrack.getTimeEnd();
//			case Source:
//				return getSourceFileString(aTrack);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(BasicSpectrum spec, LookUp aEnum, Object aValue)
	{
//		if (aEnum == LookUp.IsVisible)
//			refManager.setIsVisible(trackL, (boolean) aValue);
//		else
//			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

//	/**
//	 * Utility method that returns the appropriate "Source Files" string for the
//	 * specified track.
//	 */
//	public static String getSourceFileString(LidarTrack aTrack)
//	{
//		List<String> sourceL = aTrack.getSourceList();
//		if (sourceL.size() == 0)
//			return "";
//
//		StringBuffer tmpSB = new StringBuffer();
//		for (String aSource : sourceL)
//		{
//			tmpSB.append(" | " + aSource);
//			if (tmpSB.length() > 1000)
//			{
//				tmpSB.append("...");
//				break;
//			}
//		}
//		tmpSB.delete(0, 3);
//
//		return tmpSB.toString();
//	}

}