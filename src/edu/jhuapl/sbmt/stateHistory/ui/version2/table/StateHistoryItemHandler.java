package edu.jhuapl.sbmt.stateHistory.ui.version2.table;

import java.awt.Color;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.sbmt.gui.lidar.color.ColorProvider;
import edu.jhuapl.sbmt.gui.lidar.color.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

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
			case Map:
				return stateHistoryCollection.isStateHistoryMapped(stateHistory);
			case Show:
				return stateHistoryCollection.getVisibility(stateHistory);
			case Color:
				ColorProvider blueCP = new ConstColorProvider(Color.BLUE);
				return blueCP;
			case Line:
				return 1;
			case Name:
				return stateHistory.getTrajectoryName();
			case Description:
				return stateHistory.getTrajectoryDescription();
			case StartTime:
				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
				fmt.withZone(DateTimeZone.UTC);
				String timeString = TimeUtil.et2str(stateHistory.getMinTime());
				return timeString.substring(0, 23);
			case EndTime:
				DateTimeFormatter fmt2 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
				fmt2.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(stateHistory.getMaxTime());
//				DateTime endTime = new DateTime(stateHistory.getMaxTime().longValue());
				return timeString.substring(0, 23);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(StateHistory history, StateHistoryColumnLookup aEnum, Object aValue)
	{
		if (aEnum == StateHistoryColumnLookup.Map)
		{
			if (!stateHistoryCollection.isStateHistoryMapped(history))
				stateHistoryCollection.addRun(history);
			else
			{
				stateHistoryCollection.removeRun(history.getKey());
			}
		}
		else if (aEnum == StateHistoryColumnLookup.Show)
		{
			if (stateHistoryCollection.isStateHistoryMapped(history))
			{
				stateHistoryCollection.setVisibility(history, (boolean) aValue);
			}
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

}
