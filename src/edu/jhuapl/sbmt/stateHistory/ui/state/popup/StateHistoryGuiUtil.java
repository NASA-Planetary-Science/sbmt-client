package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.awt.Component;

import javax.swing.JMenu;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.action.PopupMenu;

/**
 * Collection of lidar UI utility methods. (based on code from lopeznr1)
 *
 * @author steelrj1
 */
/**
 * @author steelrj1
 *
 */
public class StateHistoryGuiUtil
{
	/**
	 * Forms the popup menu associated with lidar files.
	 */
	/**
	 * @param aManager
	 * @param aParent
	 * @return
	 */
	public static PopupMenu<StateHistory> formStateHistoryFileSpecPopupMenu(StateHistoryRendererManager rendererManager,
			Component aParent)
	{
		PopupMenu<StateHistory> menu = new PopupMenu<>(rendererManager);

		JMenu colorMenu = new JMenu("Trajectory Color");
		menu.installPopAction(new MultiColorStateHistoryAction(rendererManager, aParent, colorMenu), colorMenu);

		menu.installPopAction(new SaveFileAction(rendererManager, aParent), "Save Trajectory");
		menu.installPopAction(new HideShowStateHistoryAction(rendererManager, "Trajectory"), "Show Trajectory");
		if (rendererManager.getNumItems() > 1)
			menu.installPopAction(new HideOtherStateHistoryAction(rendererManager), "Hide Other Trajectories");

		return menu;
	}
}
