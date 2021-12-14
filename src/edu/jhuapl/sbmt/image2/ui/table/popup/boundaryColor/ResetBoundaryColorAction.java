package edu.jhuapl.sbmt.image2.ui.table.popup.boundaryColor;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Reset Colors".
 *
 * @author lopeznr1
 */
class ResetBoundaryColorAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final PerspectiveImageCollection refManager;

	/**
	 * Standard Constructor
	 */
	public ResetBoundaryColorAction(PerspectiveImageCollection aManager)
	{
		refManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		//TODO FIX THIS
//		refManager.clearCustomColorProvider(aItemL);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Determine if any of the lidar colors can be reset
		boolean isResetAvail = false;
		//TODO FIX THIS
//		for (G1 aItem : aItemC)
//			isResetAvail |= refManager.hasCustomColorProvider(aItem) == true;

		aAssocMI.setEnabled(isResetAvail);
	}
}