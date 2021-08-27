package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.action.PopAction;

/**
 * Object that defines the action: "Custom Color". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
class CustomStateHistoryColorAction extends PopAction<StateHistory>
{
	// Ref vars
	/**
	 *
	 */
	private final StateHistoryRendererManager refManager;
	/**
	 *
	 */
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	/**
	 * @param aManager
	 * @param aParent
	 */
	public CustomStateHistoryColorAction(StateHistoryRendererManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	/**
	 *
	 */
	@Override
	public void executeAction(List<StateHistory> aItemL)
	{
		Color tmpColor = refManager.getColorProviderForStateHistory(aItemL.get(0)).getBaseColor();
		Color newColor = ColorChooser.showColorChooser(refParent, tmpColor);
		if (newColor == null)
			return;

		ColorProvider tmpCP = new ConstColorProvider(newColor);
		refManager.installCustomColorProvider(aItemL, tmpCP);
	}
}