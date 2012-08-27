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

import edu.jhuapl.near.gui.ColorChooser;
import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.LidarPlot;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.util.ColorUtil;
import edu.jhuapl.near.util.Configuration;

public class LidarPopupMenu extends PopupMenu
{
    private Component invoker;
    private JMenu colorMenu;
    private ArrayList<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customTrackColorMenuItem;
    private JMenuItem saveTrackMenuItem;
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

        saveTrackMenuItem = new JMenuItem(new SaveTrackAction());
        saveTrackMenuItem.setText("Save track...");
        this.add(saveTrackMenuItem);

        hideTrackMenuItem = new JCheckBoxMenuItem(new HideTrackAction());
        hideTrackMenuItem.setText("Hide track");
        this.add(hideTrackMenuItem);

        hideOtherTracksMenuItem = new JMenuItem(new HideOtherTracksAction());
        hideOtherTracksMenuItem.setText("Hide other tracks");
        this.add(hideOtherTracksMenuItem);

        plotTrackMenuItem = new JMenuItem(new PlotTrackAction());
        plotTrackMenuItem.setText("Plot track...");
        this.add(plotTrackMenuItem);
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
        public void actionPerformed(ActionEvent e)
        {
            Component invoker = getInvoker();

            File file = CustomFileChooser.showSaveDialog(invoker, "Save Lidar Track", "track" + currentTrack + ".tab");

            try
            {
                if (file != null)
                    lidarModel.saveTrack(currentTrack, file, false);
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
            if (Configuration.isWindows())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                        "This feature is currently not supported in Windows platforms. Please try using Linux\n" +
                        "or Mac OS X instead. We apologize for any inconvenience.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                return;
            }

            try
            {
                ArrayList<Double> potential = new ArrayList<Double>();
                ArrayList<Double> acceleration = new ArrayList<Double>();
                ArrayList<Double> elevation = new ArrayList<Double>();
                ArrayList<Double> distance = new ArrayList<Double>();
                ArrayList<Long> time = new ArrayList<Long>();

                lidarModel.getGravityDataForTrack(currentTrack, potential, acceleration, elevation, distance, time);

                LidarPlot lidarPlot = new LidarPlot(lidarModel, potential, distance, time, "Potential", "J/kg");
                lidarPlot.setVisible(true);
                lidarPlot = new LidarPlot(lidarModel, acceleration, distance, time, "Acceleration", "m/s^2");
                lidarPlot.setVisible(true);
                lidarPlot = new LidarPlot(lidarModel, elevation, distance, time, "Elevation", "m");
                lidarPlot.setVisible(true);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

//    private class ShowInfoAboutTrackAction extends AbstractAction
//    {
//        public void actionPerformed(ActionEvent e)
//        {
//        }
//    }

    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        setCurrentTrack(lidarModel.getTrackIdFromPointId(pickedCellId));
        show(e.getComponent(), e.getX(), e.getY());
    }

}
