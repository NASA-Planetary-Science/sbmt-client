package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import vtk.vtkRenderWindowPanel;

public class ViewMenu extends JMenu 
{
    public ViewMenu(vtkRenderWindowPanel viewer, boolean showExit)
    {
        super("File");

        JMenuItem mi = new JMenuItem(new ShowErosAction());
        this.add(mi);
    }
    
    private static class ShowErosAction extends AbstractAction
    {
        public ShowErosAction()
        {
            super("Eros");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        }
    }

    private static class ShowDeimosAction extends AbstractAction
    {
        public ShowDeimosAction()
        {
            super("Deimos");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        }
    }

    private static class ShowItokawaAction extends AbstractAction
    {
        public ShowItokawaAction()
        {
            super("Itokawa");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        }
    }
}
