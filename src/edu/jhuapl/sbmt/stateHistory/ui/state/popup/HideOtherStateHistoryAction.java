package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.util.List;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

/**
 * Object that defines the action: "Hide Other Items". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 */
class HideOtherStateHistoryAction extends StateHistoryPopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryCollection refManager;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 */
	public HideOtherStateHistoryAction(StateHistoryCollection aManager)
	{
		refManager = aManager;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		refManager.setOthersHiddenExcept(aItemL);
	}
}