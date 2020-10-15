package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.viewOptions.table;

import java.awt.Color;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class ViewOptionsFOVItemHandler extends BasicItemHandler<String, ViewOptionsFOVColumnLookup>
{

	/**
	 *
	 */
	private final StateHistoryCollection stateHistoryCollection;

	/**
	 * @param aManager
	 * @param aComposer
	 */
	public ViewOptionsFOVItemHandler(StateHistoryCollection stateHistoryCollection, QueryComposer<ViewOptionsFOVColumnLookup> aComposer)
	{
		super(aComposer);

		this.stateHistoryCollection = stateHistoryCollection;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(String fov, ViewOptionsFOVColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case Frustum:
				return stateHistoryCollection.getInstrumentFrustumVisibility(fov);
			case Border:
				return stateHistoryCollection.getInstrumentFootprintBorderVisibility(fov);
			case Footprint:
				return stateHistoryCollection.getInstrumentFootprintVisibility(fov);
			case Color:
				Color fovColor = stateHistoryCollection.getInstrumentFrustumColor(fov);
				return new ConstColorProvider(fovColor);
			case Name:
				return fov;
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(String fov, ViewOptionsFOVColumnLookup aEnum, Object aValue)
	{
		if (aEnum == ViewOptionsFOVColumnLookup.Frustum)
		{
			stateHistoryCollection.setInstrumentFrustumVisibility(fov, (boolean) aValue);

		}
		else if (aEnum == ViewOptionsFOVColumnLookup.Border)
		{
			stateHistoryCollection.setInstrumentFootprintBorderVisibility(fov, (boolean) aValue);
		}
		else if (aEnum == ViewOptionsFOVColumnLookup.Footprint)
		{
			stateHistoryCollection.setInstrumentFootprintVisibility(fov, (boolean) aValue);
		}
		else if (aEnum == ViewOptionsFOVColumnLookup.Color)
		{
			stateHistoryCollection.setInstrumentFootprintColor(fov, ((ConstColorProvider)aValue).getBaseColor());
			stateHistoryCollection.setInstrumentFrustumColor(fov, ((ConstColorProvider)aValue).getBaseColor());
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}