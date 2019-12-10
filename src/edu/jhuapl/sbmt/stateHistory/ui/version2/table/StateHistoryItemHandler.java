package edu.jhuapl.sbmt.stateHistory.ui.version2.table;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class StateHistoryItemHandler extends BasicItemHandler<StateHistory, StateHistoryColumnLookup>
{
	private final StateHistoryCollection stateHistoryCollection;

	public StateHistoryItemHandler(StateHistoryCollection aManager, QueryComposer<StateHistoryColumnLookup> aComposer)
	{
		super(aComposer);

		stateHistoryCollection = aManager;
	}

	@Override
	public Object getColumnValue(StateHistory stateHistory, StateHistoryColumnLookup aEnum)
	{
		switch (aEnum)
		{
//			case Map:
//				return stateHistoryCollection.isStateHistoryMapped(stateHistory);
//			case Show:
//				return stateHistoryCollection.getVisibility(stateHistory);
//			case Id:
//				return spec.getId();
//			case Filename:
//				return spec.getSpectrumName();
//			case Date:
//				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
//				fmt.withZone(DateTimeZone.UTC);
//				return fmt.print(spec.getDateTime());
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(StateHistory history, StateHistoryColumnLookup aEnum, Object aValue)
	{
//		if (aEnum == StateHistoryColumnLookup.Map)
//		{
//			if (!stateHistoryCollection.isStateHistoryMapped(history))
//				stateHistoryCollection.addRun(history);
//			else
//			{
//				stateHistoryCollection.removeSpectrum(spec);
//			}
//		}
//		else if (aEnum == StateHistoryColumnLookup.Show)
//		{
//			if (stateHistoryCollection.isStateHistoryMapped(history))
//			{
//				stateHistoryCollection.setVisibility(history, (boolean) aValue);
//			}
//		}
//		else
//			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

}
