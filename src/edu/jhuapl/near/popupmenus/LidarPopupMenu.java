package edu.jhuapl.near.popupmenus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.near.gui.lidar.LidarPlot;
import edu.jhuapl.near.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.Configuration;

public class LidarPopupMenu extends PopupMenu
{
    private Component invoker;
    private JMenu colorMenu;
    private JMenu saveMenu;
    private JMenu saveAllMenu;
    private ArrayList<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customTrackColorMenuItem;
    private JMenuItem saveTrackOriginalMenuItem;
    private JMenuItem saveTrackModifiedMenuItem;
    private JMenuItem saveAllTracksOriginalToFolderMenuItem;
    private JMenuItem saveAllTracksModifiedToFolderMenuItem;
    private JMenuItem saveAllTracksOriginalToSingleFileMenuItem;
    private JMenuItem saveAllTracksModifiedToSingleFileMenuItem;
    private JMenuItem hideTrackMenuItem;
    private JMenuItem hideOtherTracksMenuItem;
    private JMenuItem plotTrackMenuItem;
    private LidarSearchDataCollection lidarModel;
    private int currentTrack;

    public LidarPopupMenu(LidarSearchDataCollection lidarModel,
            Component invoker)
    {
        this.lidarModel = lidarModel;
        this.invoker = invoker;

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
        customTrackColorMenuItem = new JMenuItem(new CustomTrackColorAction());
        customTrackColorMenuItem.setText("Custom...");
        colorMenu.add(customTrackColorMenuItem);

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


        hideTrackMenuItem = new JCheckBoxMenuItem(new HideTrackAction());
        hideTrackMenuItem.setText("Hide Track");
        this.add(hideTrackMenuItem);

        hideOtherTracksMenuItem = new JMenuItem(new HideOtherTracksAction());
        hideOtherTracksMenuItem.setText("Hide Other Tracks");
        this.add(hideOtherTracksMenuItem);


        if (Configuration.isAPLVersion())
        {
            plotTrackMenuItem = new JMenuItem(new PlotTrackAction());
            plotTrackMenuItem.setText("Plot Track...");
            this.add(plotTrackMenuItem);
        }
    }

    public void setCurrentTrack(int trackId)
    {
        currentTrack = trackId;

        hideTrackMenuItem.setSelected(lidarModel.isTrackHidden(trackId));

        // If the track color equals one of the predefined colors, then check
        // the corresponding menu item.
        int[] currentTrackColor = lidarModel.getTrackColor(trackId);
        for (JCheckBoxMenuItem item : colorMenuItems)
        {
            TrackColorAction action = (TrackColorAction)item.getAction();
            Color color = action.color;
            if (currentTrackColor[0] == color.getRed() &&
                    currentTrackColor[1] == color.getGreen() &&
                    currentTrackColor[2] == color.getBlue() &&
                    currentTrackColor[3] == color.getAlpha())
            {
                item.setSelected(true);
            }
            else
            {
                item.setSelected(false);
            }
        }
    }

    private class TrackColorAction extends AbstractAction
    {
        private Color color;

        public TrackColorAction(Color color)
        {
            this.color = color;
        }

        public void actionPerformed(ActionEvent e)
        {
            lidarModel.setTrackColor(currentTrack, color);
        }
    }

    private class CustomTrackColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int[] currentColor = lidarModel.getTrackColor(currentTrack);
            Color newColor = ColorChooser.showColorChooser(invoker, currentColor);
            if (newColor != null)
                lidarModel.setTrackColor(currentTrack, newColor);
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

            File file = CustomFileChooser.showSaveDialog(invoker, "Save Lidar Track", "track" + currentTrack + ".tab");

            try
            {
                if (file != null)
                    lidarModel.saveTrack(currentTrack, file, transformTrack);
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
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

            if (lidarModel.getNumberOfVisibleTracks() == 0)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "There are no visible tracks to save.",
                        "Error Saving Tracks",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            File dir = DirectoryChooser.showOpenDialog(invoker);

            try
            {
                if (dir != null)
                    lidarModel.saveAllVisibleTracksToFolder(dir, transformTrack);
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + dir.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
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

            if (lidarModel.getNumberOfVisibleTracks() == 0)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "There are no visible tracks to save.",
                        "Error Saving Tracks",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            File file = CustomFileChooser.showSaveDialog(invoker, "Save All Visible Tracks to Single File", "tracks.tab");

            try
            {
                if (file != null)
                    lidarModel.saveAllVisibleTracksToSingleFile(file, transformTrack);
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "Unable to save file to " + file.getAbsolutePath(),
                        "Error Saving File",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }

    private class HideTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            lidarModel.hideTrack(currentTrack, hideTrackMenuItem.isSelected());
        }
    }

    private class HideOtherTracksAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            lidarModel.hideOtherTracksExcept(currentTrack);
        }
    }

    private class PlotTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                ArrayList<Double> potential = new ArrayList<Double>();
                ArrayList<Double> acceleration = new ArrayList<Double>();
                ArrayList<Double> elevation = new ArrayList<Double>();
                ArrayList<Double> distance = new ArrayList<Double>();
                ArrayList<Double> time = new ArrayList<Double>();

                lidarModel.getGravityDataForTrack(currentTrack, potential, acceleration, elevation, distance, time);

                LidarPlot lidarPlot = new LidarPlot(lidarModel, potential, distance, time, "Potential", "J/kg");
                lidarPlot.setVisible(true);
                lidarPlot = new LidarPlot(lidarModel, acceleration, distance, time, "Acceleration", "m/s^2");
                lidarPlot.setVisible(true);
                lidarPlot = new LidarPlot(lidarModel, elevation, distance, time, "Elevation", "m");
                lidarPlot.setVisible(true);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        setCurrentTrack(lidarModel.getTrackIdFromPointId(pickedCellId));
        show(e.getComponent(), e.getX(), e.getY());
    }

}
