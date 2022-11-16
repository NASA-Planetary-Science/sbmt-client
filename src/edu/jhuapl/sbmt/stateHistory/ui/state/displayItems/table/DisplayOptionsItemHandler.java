package edu.jhuapl.sbmt.stateHistory.ui.state.displayItems.table;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class DisplayOptionsItemHandler extends BasicItemHandler<DisplayableItem, DisplayOptionsColumnLookup>
{

	/**
	 *
	 */
	private final StateHistoryRendererManager stateHistoryRendererManager;

	/**
	 * @param aManager
	 * @param aComposer
	 */
	public DisplayOptionsItemHandler(StateHistoryRendererManager rendererManager, QueryComposer<DisplayOptionsColumnLookup> aComposer)
	{
		super(aComposer);
		this.stateHistoryRendererManager = rendererManager;
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(DisplayableItem item, DisplayOptionsColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case Show:
				return item.isVisible();
			case Label:
				return item.isLabelVisible();
			case Size:
				return item.getPointerRadius();
			case LabelString:
				return item.getLabel();
			case Color:
				Color fovColor = item.getColor();
				return new ConstColorProvider(fovColor);
			case Name:
				return item.getLabel();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 *
	 */
	@Override
	public void setColumnValue(DisplayableItem item, DisplayOptionsColumnLookup aEnum, Object aValue)
	{
		if (aEnum == DisplayOptionsColumnLookup.Show)
		{
			item.setVisible((boolean) aValue);
			stateHistoryRendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));
		}
		else if (aEnum == DisplayOptionsColumnLookup.Label)
		{
			item.setLabelVisible((boolean) aValue);
			stateHistoryRendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));
		}
		else if (aEnum == DisplayOptionsColumnLookup.Size)
		{
			item.setPointerRadius((double)aValue);
			stateHistoryRendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));
		}
		else if (aEnum == DisplayOptionsColumnLookup.LabelString)
		{
			item.setLabel((String)aValue);
			stateHistoryRendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));

		}
		else if (aEnum == DisplayOptionsColumnLookup.Color)
		{
			item.setColor(((ConstColorProvider)aValue).getBaseColor());
			stateHistoryRendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));

		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}