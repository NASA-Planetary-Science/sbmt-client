package edu.jhuapl.sbmt.stateHistory.model.viewOptions;

import java.util.HashMap;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.ui.color.GroupColorProvider;

public class StateHistoryColorOptionsAttributeModel
{
	private HashMap<StateHistory, StateHistoryColoringFunctions> stateHistoryToColoringFunction =
			new HashMap<StateHistory, StateHistoryColoringFunctions>();

	private HashMap<StateHistory, GroupColorProvider> stateHistoryToColoringProvider =
			new HashMap<StateHistory, GroupColorProvider>();

	private HashMap<StateHistory, Colormap> stateHistoryToColormap =
			new HashMap<StateHistory, Colormap>();


	public StateHistoryColorOptionsAttributeModel()
	{
		// TODO Auto-generated constructor stub
	}

	public void setColoringFunctionForStateHistory(StateHistoryColoringFunctions func,
													StateHistory stateHistory)
	{
		stateHistoryToColoringFunction.put(stateHistory, func);
	}

	public void setColoringProviderForStateHistory(GroupColorProvider gcp, StateHistory stateHistory)
	{
		stateHistoryToColoringProvider.put(stateHistory, gcp);
	}

	public StateHistoryColoringFunctions getColoringFunctionForStateHistory(StateHistory stateHistory)
	{
		return stateHistoryToColoringFunction.get(stateHistory);
	}

	public GroupColorProvider getColoringProviderForStateHistory(StateHistory stateHistory)
	{
		return stateHistoryToColoringProvider.get(stateHistory);
	}

	public void setColormapForStateHistory(Colormap colormap, StateHistory stateHistory)
	{
		stateHistoryToColormap.put(stateHistory, colormap);
	}

	public Colormap getColormapForStateHistory(StateHistory stateHistory)
	{
		return stateHistoryToColormap.get(stateHistory);
	}

}
