package edu.jhuapl.sbmt.gui.lidar.popup;

import java.awt.Component;

import javax.swing.JMenu;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpec;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.model.lidar.LidarTrack;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;

/**
 * Collection of lidar UI utility methods.
 *
 * @author lopeznr1
 */
public class LidarGuiUtil
{
	/**
	 * Forms the popup menu associated with lidar files.
	 */
	public static LidarPopupMenu<LidarFileSpec> formLidarFileSpecPopupMenu(LidarFileSpecManager aManager,
			Component aParent)
	{
		LidarPopupMenu<LidarFileSpec> retLPM = new LidarPopupMenu<>(aManager);

		JMenu colorMenu = new JMenu("File Color");
		retLPM.installPopAction(new MultiColorLidarAction<>(aManager, aParent, colorMenu), colorMenu);

		retLPM.installPopAction(new SaveFileAction(aManager, aParent), "Save File");
		retLPM.installPopAction(new HideShowLidarAction<>(aManager, "File"), "Show File");
		retLPM.installPopAction(new HideOtherLidarAction<>(aManager), "Hide Other Files");

		return retLPM;
	}

	/**
	 * Forms the popup menu associated with lidar tracks.
	 */
	public static LidarPopupMenu<LidarTrack> formLidarTrackPopupMenu(LidarTrackManager aManager, Component aParent)
	{
		LidarPopupMenu<LidarTrack> retLPM = new LidarPopupMenu<>(aManager);

		JMenu colorMenu = new JMenu("Track Color");
		retLPM.installPopAction(new MultiColorLidarAction<>(aManager, aParent, colorMenu), colorMenu);

		retLPM.installPopAction(new SaveTrackAction(aManager, aParent), "Save Track");
		retLPM.installPopAction(new HideShowLidarAction<>(aManager, "Track"), "Show Track");
		retLPM.installPopAction(new HideOtherLidarAction<>(aManager), "Hide Other Tracks");
		retLPM.installPopAction(new TranslateTrackAction(aManager, aParent), "Translate Track");
		if (Configuration.isAPLVersion() == true)
			retLPM.installPopAction(new PlotTrackAction(aManager), "Plot Track...");

		return retLPM;
	}

}
