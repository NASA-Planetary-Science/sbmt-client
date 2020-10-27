package edu.jhuapl.sbmt.stateHistory.model.viewOptions;

import java.util.HashMap;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StateHistoryViewOptionsModel
{
	private HashMap<StateHistory, Double> stateHistoryToViewInputAngle =
			new HashMap<StateHistory, Double>();

	private HashMap<StateHistory, RendererLookDirection> stateHistoryToLookDirection =
			new HashMap<StateHistory, RendererLookDirection>();

	public Double getViewInputAngleForStateHistory(StateHistory stateHistory)
	{
		return stateHistoryToViewInputAngle.get(stateHistory);
	}

	/**
	 * Sets the view input angle (FOV).  Caps between 1 and 120 if needed
	 * @param viewInputAngle
	 */
	public void setViewInputAngleForStateHistory(double viewInputAngle, StateHistory stateHistory)
	{
		Double inputAngle;
		if (!(viewInputAngle > 120.0 || viewInputAngle < 1.0))
		{
			inputAngle = viewInputAngle;
		}
		else if (viewInputAngle > 120)
		{
			inputAngle = 120.0;
		}
		else
		{
			inputAngle = 1.0;
		}
		stateHistoryToViewInputAngle.put(stateHistory, inputAngle);
	}

	public RendererLookDirection getRendererLookDirectionForStateHistory(StateHistory stateHistory)
	{
		return stateHistoryToLookDirection.get(stateHistory);
	}

	public void setRendererLookDirectionForStateHistory(RendererLookDirection lookDir,
														StateHistory stateHistory)
	{
		stateHistoryToLookDirection.put(stateHistory, lookDir);
	}
}
