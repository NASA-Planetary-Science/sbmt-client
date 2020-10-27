package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.table;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class DisplayOptionsItemHandler extends BasicItemHandler<DisplayableItem, DisplayOptionsColumnLookup>
{

	/**
	 *
	 */
	private final StateHistoryCollection stateHistoryCollection;

	/**
	 * @param aManager
	 * @param aComposer
	 */
	public DisplayOptionsItemHandler(StateHistoryCollection stateHistoryCollection, QueryComposer<DisplayOptionsColumnLookup> aComposer)
	{
		super(aComposer);

		this.stateHistoryCollection = stateHistoryCollection;
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
			case Size:
				return item.getPointerRadius();
			case Label:
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
			stateHistoryCollection.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));
		}
		else if (aEnum == DisplayOptionsColumnLookup.Size)
		{
			item.setPointerRadius((double)aValue);
			stateHistoryCollection.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));
		}
		else if (aEnum == DisplayOptionsColumnLookup.Label)
		{
			item.setLabel((String)aValue);
			stateHistoryCollection.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));

		}
		else if (aEnum == DisplayOptionsColumnLookup.Color)
		{
			item.setColor(((ConstColorProvider)aValue).getBaseColor());
			stateHistoryCollection.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, item));

		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}