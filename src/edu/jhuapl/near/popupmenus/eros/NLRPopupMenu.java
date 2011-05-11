package edu.jhuapl.near.popupmenus.eros;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.eros.NLRPlot2;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection2;
import edu.jhuapl.near.popupmenus.PopupMenu;

public class NLRPopupMenu extends PopupMenu
{
    private JMenuItem highlightTrackMenuItem;
    private JMenuItem saveTrackMenuItem;
    private JMenuItem hideTrackMenuItem;
    private JMenuItem hideOtherTracksMenuItem;
    private JMenuItem plotTrackMenuItem;
    private NLRSearchDataCollection2 nlrModel;
    private int currentTrack;

    public NLRPopupMenu(ModelManager modelManager)
    {
        this.nlrModel = (NLRSearchDataCollection2)modelManager.getModel(ModelNames.NLR_DATA_SEARCH);

        highlightTrackMenuItem = new JCheckBoxMenuItem(new HighlightTrackAction());
        highlightTrackMenuItem.setText("Highlight track");
        this.add(highlightTrackMenuItem);

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

        highlightTrackMenuItem.setSelected(nlrModel.isTrackHighlighted(trackId));
        hideTrackMenuItem.setSelected(nlrModel.isTrackHidden(trackId));
    }

    private class HighlightTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            nlrModel.highlightTrack(currentTrack, highlightTrackMenuItem.isSelected());
        }
    }

    private class SaveTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            Component invoker = getInvoker();

            File file = CustomFileChooser.showSaveDialog(invoker, "Save NLR Track", "track" + currentTrack + ".tab");

            try
            {
                if (file != null)
                    nlrModel.saveTrack(currentTrack, file);
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
            nlrModel.hideTrack(currentTrack, hideTrackMenuItem.isSelected());
        }
    }

    private class HideOtherTracksAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            nlrModel.hideOtherTracksExcept(currentTrack);
        }
    }

    private class PlotTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            NLRPlot2 nlrPlot = new NLRPlot2(nlrModel, currentTrack);
            nlrPlot.setVisible(true);
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
        setCurrentTrack(nlrModel.getTrackIdFromPointId(pickedCellId));
        show(e.getComponent(), e.getX(), e.getY());
    }

}
