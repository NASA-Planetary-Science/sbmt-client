package edu.jhuapl.sbmt.image2.ui.table.popup.boundaryColor;

import java.awt.Color;
import java.util.List;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Fixed Color".
 *
 * @author lopeznr1
 */
class FixedImageColorAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final PerspectiveImageCollection refManager;
	private final ColorProvider refCP;

	/**
	 * Standard Constructor
	 */
	public FixedImageColorAction(PerspectiveImageCollection aManager, Color aColor)
	{
		refManager = aManager;
		refCP = new ConstColorProvider(aColor);
	}

	/**
	 * Returns the color associated with this Action
	 */
	public Color getColor()
	{
		return refCP.getBaseColor();
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		//TODO fix this
//		refManager.installCustomColorProviders(aItemL, refCP, refCP);
	}

}