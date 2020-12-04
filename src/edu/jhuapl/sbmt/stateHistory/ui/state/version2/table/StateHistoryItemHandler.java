package edu.jhuapl.sbmt.stateHistory.ui.state.version2.table;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * @author steelrj1
 *
 */
public class StateHistoryItemHandler extends BasicItemHandler<StateHistory, StateHistoryColumnLookup>
{
	/**
	 *
	 */
	private final StateHistoryCollection stateHistoryCollection;
	private StateHistoryRendererManager rendererManager;

	/**
	 * @param aManager
	 * @param aComposer
	 */
	public StateHistoryItemHandler(StateHistoryRendererManager rendererManager, QueryComposer<StateHistoryColumnLookup> aComposer)
	{
		super(aComposer);
		this.rendererManager = rendererManager;
		stateHistoryCollection = rendererManager.getRuns();
	}

	/**
	 *
	 */
	@Override
	public Object getColumnValue(StateHistory stateHistory, StateHistoryColumnLookup aEnum)
	{
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		String timeString;
		switch (aEnum)
		{
			case Map:
				return stateHistory.isMapped();
			case Show:
				return stateHistory.isVisible();
			case Color:
				return new ConstColorProvider(stateHistory.getTrajectory().getColor());
			case Name:
				if (stateHistory.getStateHistoryName().equals("")) return "Segment " + stateHistory.getKey().getValue();
				return stateHistory.getStateHistoryName();
			case Description:
				return stateHistory.getStateHistoryDescription();
			case Source:
				return "SOURCE-UPDATE";
			case StartTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(stateHistory.getStartTime());
				return timeString.substring(0, 23);
			case EndTime:
				fmt.withZone(DateTimeZone.UTC);
				timeString = TimeUtil.et2str(stateHistory.getEndTime());
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
	public void setColumnValue(StateHistory history, StateHistoryColumnLookup aEnum, Object aValue)
	{
		if (aEnum == StateHistoryColumnLookup.Map)
		{
			if (!history.isMapped())
			{
				rendererManager.addRun(history);
				rendererManager.setVisibility(history, true);
			}
			else
			{
				rendererManager.removeRun(history);
			}
		}
		else if (aEnum == StateHistoryColumnLookup.Show)
		{
			if (history.isMapped()) rendererManager.setVisibility(history, (boolean) aValue);

		}
		else if (aEnum == StateHistoryColumnLookup.Name)
		{
			history.setStateHistoryName((String)aValue);
			stateHistoryCollection.fireHistorySegmentUpdatedListeners(history);
		}
		else if (aEnum == StateHistoryColumnLookup.Description)
		{
			history.setStateHistoryDescription((String)aValue);
			stateHistoryCollection.fireHistorySegmentUpdatedListeners(history);
		}
		else if (aEnum == StateHistoryColumnLookup.Color)
		{
			history.setTrajectoryColor(((ConstColorProvider)aValue).getBaseColor());
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}