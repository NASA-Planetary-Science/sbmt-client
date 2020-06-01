package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

/**
 * Object that defines the action: "Reset Colors". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 * @param <G1>
 */
class ResetStateHistoryColorAction<G1> extends StateHistoryPopAction<G1>
{
	// Ref vars
//	/**
//	 *
//	 */
//	private final StateHistoryCollection refManager;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 */
	public ResetStateHistoryColorAction(StateHistoryCollection aManager)
	{
//		refManager = aManager;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<G1> aItemL)
	{
//		refManager.clearCustomColorProvider(aItemL);
	}

	/**
	 *
	 */
	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

//		// Determine if any of the lidar colors can be reset
//		boolean isResetAvail = false;
//		for (G1 aItem : aItemC)
//			isResetAvail |= refManager.hasCustomColorProvider(aItem) == true;
//
//		aAssocMI.setEnabled(isResetAvail);
	}
}