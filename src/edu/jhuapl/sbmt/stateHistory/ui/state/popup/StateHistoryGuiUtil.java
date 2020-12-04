package edu.jhuapl.sbmt.stateHistory.ui.state.popup;

import java.awt.Component;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
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
		StateHistoryCollection aManager = rendererManager.getRuns();
		PopupMenu<StateHistory> retLPM = new PopupMenu<>(rendererManager);

		retLPM.installPopAction(new SaveFileAction(rendererManager, aParent), "Save Trajectory");
		retLPM.installPopAction(new HideShowStateHistoryAction(rendererManager, "Trajectory"), "Show Trajectory");
		if (rendererManager.getNumItems() > 1)
			retLPM.installPopAction(new HideOtherStateHistoryAction(rendererManager), "Hide Other Trajectories");

		return retLPM;
	}
}
