package edu.jhuapl.near.gui.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import vtk.vtkRenderWindowPanel;

import nom.tam.fits.FitsException;
import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;


public class MSIPopupMenu extends JPopupMenu 
{
	private Component invoker;
    private ModelManager modelManager;
    private String currentImageOrBoundary;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showRemoveBoundaryIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem centerImageMenuItem;
    private MSIImageInfoPanelManager infoPanelManager;
    private vtkRenderWindowPanel renWin;
    
    /**
     * 
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
	public MSIPopupMenu(
			ModelManager modelManager, 
			MSIImageInfoPanelManager infoPanelManager,
			vtkRenderWindowPanel renWin,
			Component invoker)
	{
    	this.modelManager = modelManager;
    	this.infoPanelManager = infoPanelManager;
    	this.renWin = renWin;
    	this.invoker = invoker;
    	
		showRemoveImageIn3DMenuItem = new JMenuItem(new ShowRemoveIn3DAction());
		this.add(showRemoveImageIn3DMenuItem);
		
		showRemoveBoundaryIn3DMenuItem = new JMenuItem(new ShowRemoveOutlineIn3DAction());
		this.add(showRemoveBoundaryIn3DMenuItem);
		
		if (this.infoPanelManager != null)
		{
			showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
			showImageInfoMenuItem.setText("Properties...");
			this.add(showImageInfoMenuItem);
		}
		
		saveToDiskMenuItem = new JMenuItem(new SaveImageAction());
		saveToDiskMenuItem.setText("Save Raw FIT Image to Disk...");
		this.add(saveToDiskMenuItem);

		centerImageMenuItem = new JMenuItem(new CenterImageAction());
		centerImageMenuItem.setText("Center in Window");
		//this.add(centerImageMenuItem);

	}

	/**
	 * The reason for this function is that other classes may desire to use
	 * the menu items of this menu in a non-popup type menu, i.e. in an actual
	 * menubar type menu. Therefore we provide this function to allow such a
	 * class to grab the menu items.
	 * @return
	 */
	/*
	public JMenuItem[] getMenuItems()
	{
		if (infoPanelManager != null)
		{
			JMenuItem[] items = {
					this.showRemoveImageIn3DMenuItem,
					this.showRemoveBoundaryIn3DMenuItem,
					this.showImageInfoMenuItem,
					this.saveToDiskMenuItem,
					this.centerImageMenuItem 
			};

			return items;
		}
		else
		{
			JMenuItem[] items = {
					this.showRemoveImageIn3DMenuItem,
					this.showRemoveBoundaryIn3DMenuItem,
					this.saveToDiskMenuItem,
					this.centerImageMenuItem 
			};

			return items;
		}
	}
	*/
	
	public void setCurrentImage(String name)
	{
		currentImageOrBoundary = name;

		String imageName = name;
		if (imageName.endsWith("_BOUNDARY.VTK"))
			imageName = imageName.substring(0, imageName.length()-13) + ".FIT";
		
		String boundaryName = name;
		if (boundaryName.endsWith(".FIT"))
			boundaryName = boundaryName.substring(0,boundaryName.length()-4) + "_BOUNDARY.VTK";
		
		MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
		if (msiBoundaries.containsBoundary(boundaryName))
			showRemoveBoundaryIn3DMenuItem.setText("Remove Image Boundary");
		else
			showRemoveBoundaryIn3DMenuItem.setText("Show Image Boundary in 3D");
		
		NearImageCollection msiImages = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
		if (msiImages.containsImage(imageName))
			showRemoveImageIn3DMenuItem.setText("Remove Image");
		else
			showRemoveImageIn3DMenuItem.setText("Show Image in 3D");
	}
	

	private class ShowRemoveIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			NearImageCollection model = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
			try 
			{
				String name = currentImageOrBoundary;
				if (name.endsWith("_BOUNDARY.VTK"))
					name = name.substring(0, name.length()-13) + ".FIT";
				
				if (showRemoveImageIn3DMenuItem.getText().startsWith("Show"))
					model.addImage(name);
				else
					model.removeImage(name);
				
				// Force an update on the first 2 menu items
				setCurrentImage(currentImageOrBoundary);
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
	
	private class ShowRemoveOutlineIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
			try 
			{
				String name = currentImageOrBoundary;
				if (name.endsWith(".FIT"))
					name = name.substring(0,name.length()-4) + "_BOUNDARY.VTK";
				
				if (showRemoveBoundaryIn3DMenuItem.getText().startsWith("Show"))
					model.addBoundary(name);
				else
					model.removeBoundary(name);

				// Force an update on the first 2 menu items
				setCurrentImage(currentImageOrBoundary);
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
			String name = currentImageOrBoundary;
			if (name.endsWith("_BOUNDARY.VTK"))
				name = name.substring(0, name.length()-13) + ".FIT";
			
			try 
			{
				infoPanelManager.addImage(NearImage.NearImageFactory.createImage(name));
			} 
			catch (FitsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
					File fitFile = FileCache.getFileFromServer(currentImageOrBoundary);

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
			String name = currentImageOrBoundary;
			if (name.endsWith("_BOUNDARY.VTK"))
				name = name.substring(0, name.length()-13) + ".FIT";

			try 
			{
				NearImage image = NearImage.NearImageFactory.createImage(name);
				BoundingBox bb = image.getBoundingBox();
				renWin.GetRenderer().ResetCamera(
						bb.xmin,
						bb.xmax,
						bb.ymin,
						bb.ymax,
						bb.zmin,
						bb.zmax);
				renWin.Render();
			}
			catch (FitsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	

}
