package edu.jhuapl.near.popupmenus.eros;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;

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
    private int lastPickedNlrPoint;

    public NLRPopupMenu(ModelManager modelManager)
    {
        this.nlrModel = (NLRSearchDataCollection2)modelManager.getModel(ModelNames.NLR_DATA_SEARCH);

        highlightTrackMenuItem = new JMenuItem(new HighlightTrackAction());
        highlightTrackMenuItem.setText("Highlight track");
        this.add(highlightTrackMenuItem);

        saveTrackMenuItem = new JMenuItem(new SaveTrackAction());
        saveTrackMenuItem.setText("Save track");
        this.add(saveTrackMenuItem);

        hideTrackMenuItem = new JMenuItem(new HideTrackAction());
        hideTrackMenuItem.setText("Hide track");
        this.add(hideTrackMenuItem);

        hideOtherTracksMenuItem = new JMenuItem(new HideOtherTracksAction());
        hideOtherTracksMenuItem.setText("Hide other tracks");
        this.add(hideOtherTracksMenuItem);

        plotTrackMenuItem = new JMenuItem(new PlotTrackAction());
        plotTrackMenuItem.setText("Plot track");
        this.add(plotTrackMenuItem);
    }

    private class HighlightTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int trackId = nlrModel.getTrackIdFromPointId(lastPickedNlrPoint);
            nlrModel.highlightTrack(trackId);
        }
    }

    private class SaveTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        }
    }

    private class HideTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int trackId = nlrModel.getTrackIdFromPointId(lastPickedNlrPoint);
            nlrModel.hideTrack(trackId);
        }
    }

    private class HideOtherTracksAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int trackId = nlrModel.getTrackIdFromPointId(lastPickedNlrPoint);
            nlrModel.hideOtherTracksExcept(trackId);
        }
    }

    private class PlotTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        }
    }

    private class ShowInfoAboutTrackAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        }
    }

    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        lastPickedNlrPoint = pickedCellId;
        show(e.getComponent(), e.getX(), e.getY());
    }

}
