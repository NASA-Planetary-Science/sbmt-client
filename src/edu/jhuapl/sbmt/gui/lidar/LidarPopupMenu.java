package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.model.lidar.LidarGeoUtil;
import edu.jhuapl.sbmt.model.lidar.LidarTrack;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;

public class LidarPopupMenu extends PopupMenu
{
	// Reference vars
	private LidarTrackManager refManager;

	// State vars
	private ImmutableList<LidarTrack> trackL;

	// Gui vars
	private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
	private LidarSaveDialog saveDialog;
	private LidarTrackTranslateDialog translateDialog;
	private Component invoker;
	private JMenu colorMenu;
	private JMenuItem customTrackColorMI;
	private JMenuItem showTrackMI;
	private JMenuItem hideOtherTracksMI;
	private JMenuItem translateTrackMI;
	private JMenuItem plotTrackMI;
	private JMenuItem saveTrackMI;

	/**
	 * Standard Constructor
	 *
	 * @param aManager
	 * @param aInvoker
	 */
	public LidarPopupMenu(LidarTrackManager aManager, Component aInvoker)
	{
		trackL = ImmutableList.of();

		refManager = aManager;
		invoker = aInvoker;

		colorMenu = new JMenu("Track Color");
		this.add(colorMenu);
		for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
		{
			JCheckBoxMenuItem tmpColorMI = new JCheckBoxMenuItem(new TrackColorAction(color.color()));
			colorMenuItems.add(tmpColorMI);
			tmpColorMI.setText(color.toString().toLowerCase().replace('_', ' '));
			colorMenu.add(tmpColorMI);
		}
		colorMenu.addSeparator();
		customTrackColorMI = new JMenuItem(new CustomTrackColorAction());
		customTrackColorMI.setText("Custom...");
		colorMenu.add(customTrackColorMI);

		saveTrackMI = new JMenuItem(new SaveTrackAction());
		saveTrackMI.setText("Save Track");
		add(saveTrackMI);

		showTrackMI = new JMenuItem(new ShowTrackAction());
		showTrackMI.setText("Show Track");
		add(showTrackMI);

		hideOtherTracksMI = new JMenuItem(new HideOtherTracksAction());
		hideOtherTracksMI.setText("Hide Other Tracks");
		add(hideOtherTracksMI);

		translateTrackMI = new JMenuItem(new TranslateTrackAction());
		translateTrackMI.setText("Translate Track");
		add(translateTrackMI);

		if (Configuration.isAPLVersion())
		{
			plotTrackMI = new JMenuItem(new PlotTrackAction());
			plotTrackMI.setText("Plot Track...");
			add(plotTrackMI);
		}
	}

	@Override
	public void showPopup(MouseEvent aEvent, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
	{
		// Bail if we do not have selected tracks
		List<LidarTrack> tmpL = refManager.getSelectedItems();
		if (tmpL.size() == 0)
			return;

		setSelectedTracks(tmpL);
		show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
	}

	/**
	 * Helper method which updates various internal GUI components to reflect the
	 * selected Tracks.
	 */
	private void setSelectedTracks(List<LidarTrack> aTrackL)
	{
		// Bail if there are no tracks selected.
		// we will not be display if there are no selected tracks
		trackL = ImmutableList.copyOf(aTrackL);
		if (trackL.size() == 0)
			return;

		// Determine if all tracks are shown
		boolean isAllShown = true;
		for (LidarTrack aTrack : trackL)
			isAllShown &= refManager.getIsVisible(aTrack) == true;

		// Determine the display string
		String displayStr = "Hide Track";
		if (isAllShown == false)
			displayStr = "Show Track";

		if (trackL.size() > 1)
			displayStr += "s";

		// Update the text of the showTrackMI
		showTrackMI.setText(displayStr);

		// Determine if all selected tracks have the same color
		Color tmpColor = refManager.getColor(trackL.get(0));
		boolean isSameColor = true;
		for (LidarTrack aTrack : trackL)
			isSameColor &= tmpColor.equals(refManager.getColor(aTrack)) == true;

		// If the track color equals one of the predefined colors, then check
		// the corresponding menu item.
		for (JCheckBoxMenuItem aItem : colorMenuItems)
		{
			TrackColorAction action = (TrackColorAction) aItem.getAction();
			boolean isSelected = action.color.equals(tmpColor) && isSameColor == true;
			aItem.setSelected(isSelected);
		}

		// Enable the plot menuitem if the number of selected tracks == 1
		boolean isEnabled = trackL.size() == 1;
		plotTrackMI.setEnabled(isEnabled);

		// Update saveTrackMI
		displayStr = "Save Track";
		if (trackL.size() > 1)
			displayStr += "s";

		// Update the text of the showTrackMI
		saveTrackMI.setText(displayStr);
	}

	private class TrackColorAction extends AbstractAction
	{
		private Color color;

		public TrackColorAction(Color aColor)
		{
			color = aColor;
		}

		public void actionPerformed(ActionEvent e)
		{
			refManager.setColor(trackL, color);
		}
	}

	private class CustomTrackColorAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			Color tmpColor = refManager.getColor(trackL.get(0));
			Color newColor = ColorChooser.showColorChooser(invoker, tmpColor);
			if (newColor == null)
				return;

			refManager.setColor(trackL, newColor);
		}
	}

	private class SaveTrackAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			if (saveDialog == null)
				saveDialog = new LidarSaveDialog(invoker, refManager);

			saveDialog.setVisible(true);
		}
	}

	private class ShowTrackAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			// Determine if all tracks are shown
			boolean isAllShown = true;
			for (LidarTrack aTrack : trackL)
				isAllShown &= refManager.getIsVisible(aTrack) == true;

			// Update the tracks visibility based on whether they are all shown
			boolean tmpBool = isAllShown == false;
			refManager.setIsVisible(trackL, tmpBool);
		}
	}

	private class HideOtherTracksAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			refManager.hideOtherTracksExcept(trackL);
		}
	}

	private class TranslateTrackAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (translateDialog == null)
				translateDialog = new LidarTrackTranslateDialog(invoker, refManager);

			translateDialog.setVisible(true);
		}
	}

	private class PlotTrackAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			LidarTrack tmpTrack = trackL.get(0);

			try
			{
				List<Double> potentialL = new ArrayList<>();
				List<Double> accelerationL = new ArrayList<>();
				List<Double> elevationL = new ArrayList<>();
				List<Double> distanceL = new ArrayList<>();
				List<Double> timeL = new ArrayList<>();

				LidarGeoUtil.getGravityDataForTrack(refManager, tmpTrack, potentialL, accelerationL, elevationL, distanceL,
						timeL);

				LidarPlot lidarPlot = new LidarPlot(refManager, tmpTrack, potentialL, distanceL, timeL, "Potential",
						"J/kg");
				lidarPlot.setVisible(true);
				lidarPlot = new LidarPlot(refManager, tmpTrack, accelerationL, distanceL, timeL, "Acceleration", "m/s^2");
				lidarPlot.setVisible(true);
				lidarPlot = new LidarPlot(refManager, tmpTrack, elevationL, distanceL, timeL, "Elevation", "m");
				lidarPlot.setVisible(true);
			}
			catch (Exception aExp)
			{
				aExp.printStackTrace();
			}
		}
	}

}
