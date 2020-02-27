package edu.jhuapl.sbmt.stateHistory.ui.popup;

import java.awt.Component;
import java.util.List;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

/**
 * Object that defines the action: "Custom Color". (based on code from lopeznr1)
 *
 * @author steelrj1
 */
class CustomStateHistoryColorAction<G1> extends StateHistoryPopAction<G1>
{
	// Ref vars
	private final StateHistoryCollection refManager;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public CustomStateHistoryColorAction(StateHistoryCollection aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
//		Color tmpColor = refManager.getColorProviderTarget(aItemL.get(0)).getBaseColor();
//		Color newColor = ColorChooser.showColorChooser(refParent, tmpColor);
//		if (newColor == null)
//			return;
//
//		ColorProvider tmpCP = new ConstColorProvider(newColor);
//		refManager.installCustomColorProviders(aItemL, tmpCP, tmpCP);
	}
}