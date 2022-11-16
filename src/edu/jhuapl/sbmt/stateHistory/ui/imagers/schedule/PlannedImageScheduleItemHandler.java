package edu.jhuapl.sbmt.stateHistory.ui.imagers.schedule;

import java.awt.Color;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageScheduleCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class PlannedImageScheduleItemHandler extends BasicItemHandler<PlannedImageCollection, PlannedImageScheduleColumnLookup>
{
	PlannedImageScheduleCollection plannedImageCollection;

	public PlannedImageScheduleItemHandler(PlannedImageScheduleCollection aManager, QueryComposer<PlannedImageScheduleColumnLookup> aComposer)
	{
		super(aComposer);
		plannedImageCollection = aManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(PlannedImageCollection imageCollection, PlannedImageScheduleColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case Show:
				return imageCollection.isShowing();
			case Details:
				return imageCollection.isDisplayingDetails();
			case Color:
				return new ConstColorProvider(imageCollection.getColor());
			case Filename:
				return imageCollection.getFilename();
			case StateHistory:
				return imageCollection.getStateHistoryMetadata().getStateHistoryName();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(PlannedImageCollection imageCollection, PlannedImageScheduleColumnLookup aEnum, Object aValue)
	{
		switch (aEnum)
		{
			case Show:
				imageCollection.setShowing((Boolean)aValue);
				break;
			case Details:
				imageCollection.setDisplayingDetails((Boolean)aValue);
				plannedImageCollection.showDetailedScheduleFor(imageCollection);
				break;
			case Color:
				imageCollection.setColor(((ConstColorProvider)aValue).getBaseColor());
				imageCollection.getAllItems().forEach(item -> item.setColor((Color)aValue));
				break;
			default:
				throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
		}
	}
}
