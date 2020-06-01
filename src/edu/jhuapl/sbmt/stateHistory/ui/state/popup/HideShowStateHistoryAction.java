package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

/**
 * Object that defines the action: "Hide/Show Items". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 */
class HideShowStateHistoryAction extends StateHistoryPopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryCollection refManager;

	// Attributes
	/**
	 *
	 */
	private final String itemLabelStr;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aItemLabelStr
	 */
	public HideShowStateHistoryAction(StateHistoryCollection aManager, String aItemLabelStr)
	{
		refManager = aManager;

		itemLabelStr = aItemLabelStr;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		// Determine if all tracks are shown
		boolean isAllShown = true;
		for (StateHistory aItem : aItemL)
			isAllShown &= refManager.getVisibility(aItem) == true;

		// Update the tracks visibility based on whether they are all shown
		boolean tmpBool = isAllShown == false;
		for (StateHistory history : aItemL)
			refManager.setVisibility(history, tmpBool);
	}

	/**
	 *
	 */
	@Override
	public void setChosenItems(Collection<StateHistory> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Determine if all items are shown
		boolean isAllShown = true;
		for (StateHistory aItem : aItemC)
			isAllShown &= refManager.getVisibility(aItem) == true;

		// Determine the display string
		String displayStr = "Hide " + itemLabelStr;
		if (isAllShown == false)
			displayStr = "Show " + itemLabelStr;
		displayStr = MessageUtil.toPluralForm(displayStr, aItemC);

		// Update the text of the associated MenuItem
		aAssocMI.setText(displayStr);

	}
}