package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.viewOptions.table;

import java.awt.Color;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.panel.JComboBoxWithItemState;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemEventType;

public class ViewOptionsFOVItemHandler extends BasicItemHandler<String, ViewOptionsFOVColumnLookup>
{

	/**
	 *
	 */
	private final StateHistoryRendererManager rendererManager;

	private JComboBoxWithItemState<String> plateColorings;

	/**
	 * @param aManager
	 * @param aComposer
	 */
	public ViewOptionsFOVItemHandler(StateHistoryRendererManager rendererManager, QueryComposer<ViewOptionsFOVColumnLookup> aComposer, JComboBoxWithItemState<String> plateColorings)
	{
		super(aComposer);
		this.rendererManager = rendererManager;
		this.plateColorings = plateColorings;
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
				return rendererManager.getInstrumentFrustumVisibility(fov);
			case Border:
				return rendererManager.getInstrumentFootprintBorderVisibility(fov);
			case Footprint:
				return rendererManager.getInstrumentFootprintVisibility(fov);
			case Color:
				Color fovColor = rendererManager.getInstrumentFrustumColor(fov);
				return new ConstColorProvider(fovColor);
			case Name:
				return fov;
			case SetAsCurrent:
				if (rendererManager.getRuns().getCurrentRun().getLocationProvider().getPointingProvider().getCurrentInstFrameName() == null) return false;
 				return rendererManager.getRuns().getCurrentRun().getLocationProvider().getPointingProvider().getCurrentInstFrameName().equals(fov);
			case FPPlateColoring:
				return rendererManager.getPlateColoringForInstrument(fov);
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
			if ((boolean)aValue == true)
				rendererManager.makeFrustum(rendererManager.getRuns().getCurrentRun(), fov);
			rendererManager.setInstrumentFrustumVisibility(fov, (boolean) aValue);

		}
		else if (aEnum == ViewOptionsFOVColumnLookup.Border)
		{
			if ((boolean)aValue == true)
				rendererManager.makeFootprint(rendererManager.getRuns().getCurrentRun(), fov);
			rendererManager.setInstrumentFootprintBorderVisibility(fov, (boolean) aValue);
		}
		else if (aEnum == ViewOptionsFOVColumnLookup.Footprint)
		{
			if (rendererManager.getInstrumentFrustumVisibility(fov) == false)
			{
				rendererManager.makeFrustum(rendererManager.getRuns().getCurrentRun(), fov);
				rendererManager.setInstrumentFrustumVisibility(fov, (boolean) aValue);
			}
			rendererManager.makeFootprint(rendererManager.getRuns().getCurrentRun(), fov);
			rendererManager.setInstrumentFootprintVisibility(fov, (boolean) aValue);
		}
		else if (aEnum == ViewOptionsFOVColumnLookup.Color)
		{
			rendererManager.setInstrumentFootprintColor(fov, ((ConstColorProvider)aValue).getBaseColor());
			rendererManager.setInstrumentFrustumColor(fov, ((ConstColorProvider)aValue).getBaseColor());
		}
		else if (aEnum == ViewOptionsFOVColumnLookup.SetAsCurrent)
		{
			if ((boolean)aValue == true)
				rendererManager.getRuns().getCurrentRun().getLocationProvider().getPointingProvider().setCurrentInstFrameName(fov);
			else
				rendererManager.getRuns().getCurrentRun().getLocationProvider().getPointingProvider().setCurrentInstFrameName("");
			rendererManager.notify(this, ItemEventType.ItemsChanged);
			rendererManager.refreshColoring();
		}
		else if (aEnum == ViewOptionsFOVColumnLookup.FPPlateColoring)
		{
			rendererManager.setPlateColoringForInstrument(fov, (String)(plateColorings.getSelectedItem()));
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}