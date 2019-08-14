package edu.jhuapl.sbmt.gui.lidar.popup;

import java.awt.Component;
import java.util.List;

import edu.jhuapl.sbmt.gui.lidar.LidarTrackTranslateDialog;
import edu.jhuapl.sbmt.model.lidar.LidarTrack;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;

/**
 * Object that defines the translate track action.
 *
 * @author lopeznr1
 */
class TranslateTrackAction extends LidarPopAction<LidarTrack>
{
	// Ref vars
	private final LidarTrackManager refManager;
	private final Component refParent;

	// State vars
	private LidarTrackTranslateDialog translateDialog;

	/**
	 * Standard Constructor
	 */
	public TranslateTrackAction(LidarTrackManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<LidarTrack> aItemL)
	{
		if (translateDialog == null)
			translateDialog = new LidarTrackTranslateDialog(refParent, refManager);

		translateDialog.setVisible(true);
	}

}