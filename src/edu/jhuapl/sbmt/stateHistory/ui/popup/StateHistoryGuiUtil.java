package edu.jhuapl.sbmt.stateHistory.ui.popup;

import java.awt.Component;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

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
	public static StateHistoryPopupMenu formStateHistoryFileSpecPopupMenu(StateHistoryCollection aManager,
			Component aParent)
	{
		StateHistoryPopupMenu retLPM = new StateHistoryPopupMenu(aManager);

//		JMenu colorMenu = new JMenu("Trajectory Color");
//		retLPM.installPopAction(new MultiColorStateHistoryAction<StateHistory>(aManager, aParent, colorMenu), colorMenu);

		retLPM.installPopAction(new SaveFileAction(aManager, aParent), "Save Trajectory");
		retLPM.installPopAction(new HideShowStateHistoryAction(aManager, "Trajectory"), "Show Trajectory");
		if (aManager.getNumItems() > 1)
			retLPM.installPopAction(new HideOtherStateHistoryAction(aManager), "Hide Other Trajectories");

		return retLPM;
	}
}
