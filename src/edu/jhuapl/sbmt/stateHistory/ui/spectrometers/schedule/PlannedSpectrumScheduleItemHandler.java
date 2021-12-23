package edu.jhuapl.sbmt.stateHistory.ui.spectrometers.schedule;

import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumScheduleCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class PlannedSpectrumScheduleItemHandler extends BasicItemHandler<PlannedSpectrumCollection, PlannedSpectrumScheduleColumnLookup>
{
	PlannedSpectrumScheduleCollection plannedSpectrumCollection;

	public PlannedSpectrumScheduleItemHandler(PlannedSpectrumScheduleCollection aManager, QueryComposer<PlannedSpectrumScheduleColumnLookup> aComposer)
	{
		super(aComposer);
		plannedSpectrumCollection = aManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(PlannedSpectrumCollection spectrumCollection, PlannedSpectrumScheduleColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case Show:
				return spectrumCollection.isShowing();
			case Details:
				return spectrumCollection.isDisplayingDetails();
//			case Color:
//				return new ConstColorProvider(spectrumCollection.getColor());
			case Filename:
				return spectrumCollection.getFilename();
			case StateHistory:
				return spectrumCollection.getStateHistoryMetadata().getStateHistoryName();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(PlannedSpectrumCollection spectrumCollection, PlannedSpectrumScheduleColumnLookup aEnum, Object aValue)
	{
		switch (aEnum)
		{
			case Show:
				spectrumCollection.setShowing((Boolean)aValue);
				break;
			case Details:
				spectrumCollection.setDisplayingDetails((Boolean)aValue);
				plannedSpectrumCollection.showDetailedScheduleFor(spectrumCollection);
				break;
//			case Color:
//				spectrumCollection.setColor(((ConstColorProvider)aValue).getBaseColor());
//				spectrumCollection.getAllItems().forEach(item -> item.setColor((Color)aValue));
//				break;
			default:
				throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
		}
	}
}
