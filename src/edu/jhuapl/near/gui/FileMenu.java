package edu.jhuapl.near.gui;

import java.io.*;
import javax.swing.*;


import vtk.*;

import java.awt.event.*;

public class FileMenu extends JMenu 
{
    public FileMenu(vtkRenderWindowPanel viewer, boolean showExit)
    {
        super("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(viewer));
        this.add(mi);
    	this.addSeparator();
    	mi = new JMenuItem(new AboutAction(viewer));
        this.add(mi);
        
        if (showExit)
        {
        	this.addSeparator();
        	mi = new JMenuItem(new ExitAction());
        	this.add(mi);
        }
    }

    private static class SaveImageAction extends AbstractAction
    {
    	private vtkRenderWindowPanel viewer;

    	public SaveImageAction(vtkRenderWindowPanel viewer)
        {
            super("Export to Image...");
            this.viewer = viewer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	File file = ImageFileChooser.showSaveDialog(viewer, "Export to Image");
        	if (file != null)
        	{
        		viewer.lock();
        		vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
        		windowToImage.SetInput(viewer.GetRenderWindow());
        		
        		String filename = file.getAbsolutePath();
        		if (filename.toLowerCase().endsWith("bmp"))
        		{
        			vtkBMPWriter writer = new vtkBMPWriter();
        			writer.SetFileName(filename);
        			writer.SetInputConnection(windowToImage.GetOutputPort());
        			writer.Write();
        		}
        		else if (filename.toLowerCase().endsWith("jpg") ||
        				filename.toLowerCase().endsWith("jpeg"))
        		{
        			vtkJPEGWriter writer = new vtkJPEGWriter();
        			writer.SetFileName(filename);
        			writer.SetInputConnection(windowToImage.GetOutputPort());
        			writer.Write();
        		}
        		else if (filename.toLowerCase().endsWith("png"))
        		{
        			vtkPNGWriter writer = new vtkPNGWriter();
        			writer.SetFileName(filename);
        			writer.SetInputConnection(windowToImage.GetOutputPort());
        			writer.Write();
        		}
        		else if (filename.toLowerCase().endsWith("pnm"))
        		{
        			vtkPNMWriter writer = new vtkPNMWriter();
        			writer.SetFileName(filename);
        			writer.SetInputConnection(windowToImage.GetOutputPort());
        			writer.Write();
        		}
        		else if (filename.toLowerCase().endsWith("ps"))
        		{
        			vtkPostScriptWriter writer = new vtkPostScriptWriter();
        			writer.SetFileName(filename);
        			writer.SetInputConnection(windowToImage.GetOutputPort());
        			writer.Write();
        		}
        		else if (filename.toLowerCase().endsWith("tif") ||
        				filename.toLowerCase().endsWith("tiff"))
        		{
        			vtkTIFFWriter writer = new vtkTIFFWriter();
        			writer.SetFileName(filename);
        			writer.SetInputConnection(windowToImage.GetOutputPort());
        			writer.SetCompressionToNoCompression();
        			writer.Write();
        		}
        		viewer.unlock();
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

    private static class AboutAction extends AbstractAction
    {
    	private vtkRenderWindowPanel viewer;
    	private static final String COPYRIGHT  = "\u00a9";

    	public AboutAction(vtkRenderWindowPanel viewer)
        {
            super("About Small Body Mapping Tool");
            this.viewer = viewer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	String versionString = "\n";
        	try
        	{
        		InputStream is = this.getClass().getResourceAsStream("/svn.version");
        		byte[] data = new byte[128];
        		is.read(data, 0, data.length);
        		versionString = (new String(data)).trim() + "\n\n";
        	}
        	catch (Exception e)
        	{
        	}
        	
			JOptionPane.showMessageDialog(viewer,
					"Small Body Mapping Tool\n" + versionString +
				    COPYRIGHT + " 2010 The Johns Hopkins University Applied Physics Laboratory\n",
				    "About Small Body Mapping Tool",
				    JOptionPane.PLAIN_MESSAGE);
        }
    }
}
