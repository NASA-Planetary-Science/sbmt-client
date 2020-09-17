package edu.jhuapl.sbmt.stateHistory.ui.spectrometers;

import java.awt.Color;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.sbmt.lidar.gui.color.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrum;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class PlannedSpectrumItemHandler extends BasicItemHandler<PlannedSpectrum, PlannedSpectrumColumnLookup>
{
	PlannedSpectrumCollection plannedSpectrumCollection;

	public PlannedSpectrumItemHandler(PlannedSpectrumCollection aManager, QueryComposer<PlannedSpectrumColumnLookup> aComposer)
	{
		super(aComposer);
		plannedSpectrumCollection = aManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(PlannedSpectrum spectrum, PlannedSpectrumColumnLookup aEnum)
	{
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		String timeString;
		switch (aEnum)
		{
			case Show:
				return spectrum.isShowing();
			case Frus:
				return spectrum.isFrustumShowing();
			case Color:
				return new ConstColorProvider(spectrum.getColor());
			case Instrument:
				return spectrum.getInstrumentName();
			case SpectrumTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(spectrum.getTime());
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
	public void setColumnValue(PlannedSpectrum spectrum, PlannedSpectrumColumnLookup aEnum, Object aValue)
	{
		switch (aEnum)
		{
			case Show:
				spectrum.setShowing((Boolean)aValue);
				break;
			case Frus:
				spectrum.setFrustumShowing((Boolean)aValue);
				break;
			case Color:
				spectrum.setColor((Color)aValue);
				break;
			default:
				throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
		}
	}
}
