package edu.jhuapl.near.gui.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import nom.tam.fits.FitsException;
import edu.jhuapl.near.gui.FITFileChooser;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.FileCache;


public class MSIPopupMenu extends JPopupMenu 
{
	private Component invoker;
    private ModelManager modelManager;
    private String currentImage;
    private JMenuItem showImageIn3DMenuItem;
    private JMenuItem showBoundaryIn3DMenuItem;
    private JMenuItem removeImageIn3DMenuItem;
    private JMenuItem removeBoundaryIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem centerImageMenuItem;
    private int type;
    
    /**
     * 
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
	public MSIPopupMenu(ModelManager modelManager, int type, Component invoker)
	{
    	this.modelManager = modelManager;
    	this.type = type;
    	this.invoker = invoker;
    	
		showImageIn3DMenuItem = new JMenuItem(new ShowIn3DAction());
		showImageIn3DMenuItem.setText("Map Image to 3D model of Eros");
		this.add(showImageIn3DMenuItem);

		if (this.type != 1)
		{
			showBoundaryIn3DMenuItem = new JMenuItem(new ShowOutlineIn3DAction());
			showBoundaryIn3DMenuItem.setText("Map Image Boundary to 3D Model of Eros");
			this.add(showBoundaryIn3DMenuItem);
		}
		
		if (this.type != 2)
		{
			showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
			showImageInfoMenuItem.setText("Show Image Information...");
			this.add(showImageInfoMenuItem);
		}
		
		saveToDiskMenuItem = new JMenuItem(new SaveImageAction());
		saveToDiskMenuItem.setText("Save Image to Disk...");
		this.add(saveToDiskMenuItem);

		centerImageMenuItem = new JMenuItem(new CenterImageAction());
		centerImageMenuItem.setText("Center Image in Window");
		this.add(centerImageMenuItem);

	}

	public void show(Component invoker, int x, int y, String imageName)
	{
		System.out.println(imageName);
		currentImage = imageName;
		super.show(invoker, x, y);
	}
	

	private class ShowIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			NearImageCollection model = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
			try 
			{
				String name = currentImage;
				if (name.endsWith("_BOUNDARY.VTK"))
					name = name.substring(0, name.length()-13) + ".FIT";
				model.addImage(name);
			} 
			catch (FitsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private class ShowOutlineIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
			try 
			{
				String name = currentImage;
				if (name.endsWith(".FIT"))
					name = name.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
				model.addBoundary(name);
			} 
			catch (FitsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private class ShowInfoAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
		}
	}
	
	private class SaveImageAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			File file = FITFileChooser.showSaveDialog(invoker, "Save FIT file");
			try
			{
				if (file != null)
				{
					File fitFile = FileCache.getFileFromServer(currentImage);

					InputStream in = new FileInputStream(fitFile);

					OutputStream out = new FileOutputStream(file);

					byte[] buf = new byte[2048];
					int len;
					while ((len = in.read(buf)) > 0)
					{
						out.write(buf, 0, len);
					}
					in.close();
					out.close();
				}
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(invoker,
					    "Unable to save file to " + file.getAbsolutePath(),
					    "Error Saving File",
					    JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}
	
	private class CenterImageAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
		}
	}
	

}
