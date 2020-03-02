package edu.jhuapl.sbmt.gui.lidar.popup;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;

import vtk.vtkProp;

import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.model.lidar.LidarManager;

/**
 * UI component that allows a custom lidar popup menu to be built.
 *
 * @author lopeznr1
 */
public class LidarPopupMenu<G1> extends PopupMenu
{
	// Reference vars
	private LidarManager<G1> refManager;

	// State vars
	private Map<JMenuItem, LidarPopAction<G1>> actionM;

	/**
	 * Standard Constructor
	 *
	 * @param aManager
	 */
	public LidarPopupMenu(LidarManager<G1> aManager)
	{
		refManager = aManager;

		actionM = new HashMap<>();
	}

	/**
	 * Registers the specified LidarPopAction into this LidarPopupMenu.
	 * <P>
	 * A simple menu item will be created and associated with the specified
	 * action.
	 */
	public void installPopAction(LidarPopAction<G1> aAction, String aTitle)
	{
		JMenuItem tmpMI = new JMenuItem(aAction);
		tmpMI.setText(aTitle);

		// Delegate
		installPopAction(aAction, tmpMI);
	}

	/**
	 * Registers the specified LidarPopAction into this LidarPopupMenu.
	 * <P>
	 * The action will be associated with the specified menu item.
	 */
	public void installPopAction(LidarPopAction<G1> aAction, JMenuItem aTargMI)
	{
		add(aTargMI);
		actionM.put(aTargMI, aAction);
	}

	@Override
	public void showPopup(MouseEvent aEvent, vtkProp aPickedProp, int aPickedCellId, double[] aPickedPosition)
	{
		// Bail if we do not have selected items
		Set<G1> tmpS = refManager.getSelectedItems();
		if (tmpS.size() == 0)
			return;

		// Update our LidarPopActions
		for (JMenuItem aMI : actionM.keySet())
		{
			LidarPopAction<G1> tmpLPA = actionM.get(aMI);
			tmpLPA.setChosenItems(tmpS, aMI);
		}

		show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
	}

}
