package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

public class LidarPopupMenu extends PopupMenu
{
	// Reference vars
	private LidarSearchDataCollection refModel;

	// State vars
	private ImmutableList<Integer> trackL;

	// Gui vars
	private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
	private LidarTrackTranslateDialog translateDialog;
	private Component invoker;
	private JMenu colorMenu;
	private JMenu saveMenu;
	private JMenu saveAllMenu;
	private JMenuItem customTrackColorMI;
	private JMenuItem saveTrackOriginalMenuItem;
	private JMenuItem saveTrackModifiedMenuItem;
	private JMenuItem saveAllTracksOriginalToFolderMenuItem;
	private JMenuItem saveAllTracksModifiedToFolderMenuItem;
	private JMenuItem saveAllTracksOriginalToSingleFileMenuItem;
	private JMenuItem saveAllTracksModifiedToSingleFileMenuItem;
	private JMenuItem showTrackMI;
	private JMenuItem hideOtherTracksMI;
	private JMenuItem translateTrackMI;
	private JMenuItem plotTrackMI;

	/**
	 * Standard Constructor
	 *
	 * @param aModel
	 * @param aInvoker
	 */
	public LidarPopupMenu(LidarSearchDataCollection aModel, Component aInvoker)
	{
		trackL = ImmutableList.of();

		refModel = aModel;
		invoker = aInvoker;

		colorMenu = new JMenu("Track Color");
		this.add(colorMenu);
		for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
		{
			JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new TrackColorAction(color.color()));
			colorMenuItems.add(colorMenuItem);
			colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
			colorMenu.add(colorMenuItem);
		}
		colorMenu.addSeparator();
		customTrackColorMI = new JMenuItem(new CustomTrackColorAction());
		customTrackColorMI.setText("Custom...");
		colorMenu.add(customTrackColorMI);

		saveMenu = new JMenu("Save Track");
		this.add(saveMenu);

		saveTrackOriginalMenuItem = new JMenuItem(new SaveTrackAction(false));
		saveTrackOriginalMenuItem.setText("Unmodified...");
		saveMenu.add(saveTrackOriginalMenuItem);

		saveTrackModifiedMenuItem = new JMenuItem(new SaveTrackAction(true));
		saveTrackModifiedMenuItem.setText("Radial Offset and Translation Applied...");
		saveMenu.add(saveTrackModifiedMenuItem);

		saveAllMenu = new JMenu("Save All Visible Tracks");
		this.add(saveAllMenu);

		saveAllTracksOriginalToFolderMenuItem = new JMenuItem(new SaveAllTracksToFolderAction(false));
		saveAllTracksOriginalToFolderMenuItem.setText("To Folder Unmodified...");
		saveAllMenu.add(saveAllTracksOriginalToFolderMenuItem);

		saveAllTracksModifiedToFolderMenuItem = new JMenuItem(new SaveAllTracksToFolderAction(true));
		saveAllTracksModifiedToFolderMenuItem.setText("To Folder Radial Offset and Translation Applied...");
		saveAllMenu.add(saveAllTracksModifiedToFolderMenuItem);

		saveAllTracksOriginalToSingleFileMenuItem = new JMenuItem(new SaveAllTracksToSingleFileAction(false));
		saveAllTracksOriginalToSingleFileMenuItem.setText("To Single File Unmodified...");
		saveAllMenu.add(saveAllTracksOriginalToSingleFileMenuItem);

		saveAllTracksModifiedToSingleFileMenuItem = new JMenuItem(new SaveAllTracksToSingleFileAction(true));
		saveAllTracksModifiedToSingleFileMenuItem.setText("To Single File Radial Offset and Translation Applied...");
		saveAllMenu.add(saveAllTracksModifiedToSingleFileMenuItem);

		showTrackMI = new JMenuItem(new ShowTrackAction());
		showTrackMI.setText("Show Track");
		this.add(showTrackMI);

		hideOtherTracksMI = new JMenuItem(new HideOtherTracksAction());
		hideOtherTracksMI.setText("Hide Other Tracks");
		this.add(hideOtherTracksMI);

		translateTrackMI = new JMenuItem(new TranslateTrackAction());
		translateTrackMI.setText("Translate Track");
		this.add(translateTrackMI);

		if (Configuration.isAPLVersion())
		{
			plotTrackMI = new JMenuItem(new PlotTrackAction());
			plotTrackMI.setText("Plot Track...");
			this.add(plotTrackMI);
		}
	}

	/**
	 * Sets in the selected tracks (indexes).
	 */
	public void setSelectedTracks(List<Integer> aTrackL)
	{
		trackL = ImmutableList.copyOf(aTrackL);
		if (trackL.size() == 0)
			return;

		// Determine if all tracks are shown
		boolean isAllShown = true;
		for (int aId : trackL)
			isAllShown &= refModel.getTrack(aId).getIsVisible() == true;

		// Determine the display string
		String displayStr = "Hide Track";
		if (isAllShown == false)
			displayStr = "Show Track";

		if (trackL.size() > 1)
			displayStr += "s";

		// Update the text of the showTrackMI
		showTrackMI.setText(displayStr);

		// Determine if all selected tracks have the same color
		Color tmpColor = refModel.getTrack(trackL.get(0)).getColor();
		boolean isSameColor = true;
		for (int aId : trackL)
			isSameColor &= tmpColor.equals(refModel.getTrack(aId).getColor()) == true;

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

		// TODO: Allow the entire set of selected tracks to be saved, not just one
		// TODO: Simplify the menu so that it reads 'Save Selected Tracks...'
		saveMenu.setEnabled(isEnabled);
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
			refModel.setTrackColor(trackL, color);
		}
	}

	private class CustomTrackColorAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			Color tmpColor = refModel.getTrack(trackL.get(0)).getColor();
			Color newColor = ColorChooser.showColorChooser(invoker, tmpColor);
			if (newColor == null)
				return;

			refModel.setTrackColor(trackL, newColor);
		}
	}

	private class SaveTrackAction extends AbstractAction
	{
		private boolean transformTrack;

		public SaveTrackAction(boolean transformTrack)
		{
			this.transformTrack = transformTrack;
		}

		public void actionPerformed(ActionEvent e)
		{
			Component invoker = getInvoker();

			// TODO: The entire set of selected tracks should be saved
			int trackId = trackL.get(0);
			File file = CustomFileChooser.showSaveDialog(invoker, "Save Lidar Track", "track" + trackId + ".tab");

			try
			{
				if (file != null)
					refModel.saveTrack(trackId, file, transformTrack);
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

	private class SaveAllTracksToFolderAction extends AbstractAction
	{
		private boolean transformTrack;

		public SaveAllTracksToFolderAction(boolean transformTrack)
		{
			this.transformTrack = transformTrack;
		}

		public void actionPerformed(ActionEvent e)
		{
			Component invoker = getInvoker();

			if (refModel.getNumberOfVisibleTracks() == 0)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
						"There are no visible tracks to save.", "Error Saving Tracks", JOptionPane.ERROR_MESSAGE);
				return;
			}

			File dir = DirectoryChooser.showOpenDialog(invoker);

			try
			{
				if (dir != null)
					refModel.saveAllVisibleTracksToFolder(dir, transformTrack);
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
						"Unable to save file to " + dir.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

	private class SaveAllTracksToSingleFileAction extends AbstractAction
	{
		private boolean transformTrack;

		public SaveAllTracksToSingleFileAction(boolean transformTrack)
		{
			this.transformTrack = transformTrack;
		}

		public void actionPerformed(ActionEvent e)
		{
			Component invoker = getInvoker();

			if (refModel.getNumberOfVisibleTracks() == 0)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
						"There are no visible tracks to save.", "Error Saving Tracks", JOptionPane.ERROR_MESSAGE);
				return;
			}

			File file = CustomFileChooser.showSaveDialog(invoker, "Save All Visible Tracks to Single File", "tracks.tab");

			try
			{
				if (file != null)
					refModel.saveAllVisibleTracksToSingleFile(file, transformTrack);
			}
			catch (IOException e1)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

	private class ShowTrackAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			// Determine if all tracks are shown
			boolean isAllShown = true;
			for (int aId : trackL)
				isAllShown &= refModel.getTrack(aId).getIsVisible() == true;

			// Update the tracks visibility based on whether they are all shown
			boolean tmpBool = isAllShown == false;
			refModel.setTrackVisible(trackL, tmpBool);
		}
	}

	private class HideOtherTracksAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			refModel.hideOtherTracksExcept(trackL);
		}
	}

	private class TranslateTrackAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (translateDialog == null)
				translateDialog = new LidarTrackTranslateDialog(invoker, refModel);

			translateDialog.setTracks(trackL);
			translateDialog.setVisible(true);
		}
	}

	private class PlotTrackAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e)
		{
			int tmpId = trackL.get(0);

			try
			{
				List<Double> potential = new ArrayList<>();
				List<Double> acceleration = new ArrayList<>();
				List<Double> elevation = new ArrayList<>();
				List<Double> distance = new ArrayList<>();
				List<Double> time = new ArrayList<>();

				refModel.getGravityDataForTrack(tmpId, potential, acceleration, elevation, distance, time);

				LidarPlot lidarPlot = new LidarPlot(refModel, potential, distance, time, "Potential", "J/kg");
				lidarPlot.setVisible(true);
				lidarPlot = new LidarPlot(refModel, acceleration, distance, time, "Acceleration", "m/s^2");
				lidarPlot.setVisible(true);
				lidarPlot = new LidarPlot(refModel, elevation, distance, time, "Elevation", "m");
				lidarPlot.setVisible(true);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
	{
		// Bail if we do not have selected tracks
		List<Integer> tmpL = refModel.getSelectedTracks();
		if (tmpL.size() == 0)
			return;

		setSelectedTracks(tmpL);
		show(e.getComponent(), e.getX(), e.getY());
	}

}
