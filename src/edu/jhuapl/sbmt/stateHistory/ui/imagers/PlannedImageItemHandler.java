package edu.jhuapl.sbmt.stateHistory.ui.imagers;

import java.awt.Color;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImage;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class PlannedImageItemHandler extends BasicItemHandler<PlannedImage, PlannedImageColumnLookup>
{
	PlannedImageCollection plannedImageCollection;

	public PlannedImageItemHandler(PlannedImageCollection aManager, QueryComposer<PlannedImageColumnLookup> aComposer)
	{
		super(aComposer);
		plannedImageCollection = aManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(PlannedImage image, PlannedImageColumnLookup aEnum)
	{
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		String timeString;
		switch (aEnum)
		{
			case Show:
				return image.isShowing();
			case Frus:
				return image.isFrustumShowing();
			case Color:
				return new ConstColorProvider(image.getColor());
			case Instrument:
				return image.getInstrumentName();
			case ImageTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(image.getTime());
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
	public void setColumnValue(PlannedImage image, PlannedImageColumnLookup aEnum, Object aValue)
	{
		switch (aEnum)
		{
			case Show:
				image.setShowing((Boolean)aValue);
				break;
			case Frus:
				image.setFrustumShowing((Boolean)aValue);
				break;
			case Color:
				image.setColor((Color)aValue);
				break;
			default:
				throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
		}
	}
}
