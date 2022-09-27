package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.awt.Color;
import java.util.List;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Fixed Color" (based on code from lopeznr1)
 *
 * @author steelrj1
 */
class FixedStateHistoryColorAction extends PopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryRendererManager refManager;
	/**
	 *
	 */
	private final ColorProvider refCP;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aColor
	 */
	public FixedStateHistoryColorAction(StateHistoryRendererManager aManager, Color aColor)
	{
		refManager = aManager;
		refCP = new ConstColorProvider(aColor);
	}

	/**
	 * Returns the color associated with this Action
	 */
	/**
	 * @return
	 */
	public Color getColor()
	{
		return refCP.getBaseColor();
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		System.out.println("FixedStateHistoryColorAction: executeAction: fixed color " + aItemL.size());
		refManager.installCustomColorProvider(aItemL, refCP);
	}

}