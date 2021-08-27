package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.util.MessageUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Hide/Show Items". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 */
class HideShowStateHistoryAction extends PopAction<StateHistory>
{
	// Attributes
	/**
	 *
	 */
	private final String itemLabelStr;

	private StateHistoryRendererManager rendererManager;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aItemLabelStr
	 */
	public HideShowStateHistoryAction(StateHistoryRendererManager rendererManager, String aItemLabelStr)
	{
		itemLabelStr = aItemLabelStr;
		this.rendererManager = rendererManager;
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
			isAllShown &= aItem.getMetadata().isVisible() == true;

		// Update the tracks visibility based on whether they are all shown
		boolean tmpBool = isAllShown == false;
		for (StateHistory history : aItemL)
			rendererManager.setVisibility(history, tmpBool);
//			history.setVisible(tmpBool);
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
			isAllShown &= aItem.getMetadata().isVisible() == true;

		// Determine the display string
		String displayStr = "Hide " + itemLabelStr;
		if (isAllShown == false)
			displayStr = "Show " + itemLabelStr;
		displayStr = MessageUtil.toPluralForm(displayStr, aItemC);

		// Update the text of the associated MenuItem
		aAssocMI.setText(displayStr);
	}
}