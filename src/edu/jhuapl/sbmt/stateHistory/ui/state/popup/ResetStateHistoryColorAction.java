package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Reset Colors". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
class ResetStateHistoryColorAction extends PopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryRendererManager refManager;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 */
	public ResetStateHistoryColorAction(StateHistoryRendererManager aManager)
	{
		refManager = aManager;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		refManager.clearCustomColor(aItemL);
	}

	/**
	 *
	 */
	@Override
	public void setChosenItems(Collection<StateHistory> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

//		// Determine if any of the state history colors can be reset
		boolean isResetAvail = false;
		for (StateHistory aItem : aItemC)
			isResetAvail |= refManager.hasCustomColor(aItem) == true;

		aAssocMI.setEnabled(isResetAvail);
	}
}