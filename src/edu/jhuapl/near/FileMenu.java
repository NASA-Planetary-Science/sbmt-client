package edu.jhuapl.near;

import java.io.*;
import javax.swing.*;

import vtk.vtkPNGWriter;
import vtk.vtkWindowToImageFilter;

import java.awt.event.*;

public class FileMenu extends JMenu 
{
    public FileMenu(ImageGLWidget viewer)
    {
        super("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(viewer));
        this.add(mi);
        this.addSeparator();
        mi = new JMenuItem(new ExitAction());
        this.add(mi);
    }

    private static class SaveImageAction extends AbstractAction
    {
    	ImageGLWidget viewer;

    	public SaveImageAction(ImageGLWidget viewer)
        {
            super("Export to PNG...");
            this.viewer = viewer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	File file = PNGFileChooser.showSaveDialog(viewer, "Export to PNG");
        	if (file != null)
        	{
        		vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
        		windowToImage.SetInput(viewer.getRenderWindowPanel().GetRenderWindow());

        		vtkPNGWriter writer = new vtkPNGWriter();
        		writer.SetFileName(file.getAbsolutePath());
        		writer.SetInputConnection(windowToImage.GetOutputPort());
        		writer.Write();
        	}
        }
    }

    private static class ExitAction extends AbstractAction
    {
        public ExitAction()
        {
            super("Exit");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            System.exit(0);
        }
    }

}
