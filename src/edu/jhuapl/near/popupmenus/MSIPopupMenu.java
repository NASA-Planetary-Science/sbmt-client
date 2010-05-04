package edu.jhuapl.near.popupmenus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import vtk.vtkActor;
import vtk.vtkProp;
import vtk.vtkRenderWindowPanel;

import nom.tam.fits.FitsException;
import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.Properties;


public class MSIPopupMenu extends PopupMenu 
{
	private Component invoker;
    private ModelManager modelManager;
    private String currentImageOrBoundary;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showRemoveBoundaryIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem saveBackplanesMenuItem;
    private JMenuItem centerImageMenuItem;
    private ModelInfoWindowManager infoPanelManager;
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
			ModelInfoWindowManager infoPanelManager,
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

		saveBackplanesMenuItem = new JMenuItem(new SaveBackplanesAction());
		saveBackplanesMenuItem.setText("Generate Backplanes...");
		this.add(saveBackplanesMenuItem);

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
			showRemoveBoundaryIn3DMenuItem.setText("Show Image Boundary");
		
		MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
		if (msiImages.containsImage(imageName))
			showRemoveImageIn3DMenuItem.setText("Remove Image");
		else
			showRemoveImageIn3DMenuItem.setText("Show Image");
	}
	

	private class ShowRemoveIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
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
				ErosModel eros = (ErosModel)modelManager.getModel(ModelManager.EROS);
				infoPanelManager.addData(MSIImage.MSIImageFactory.createImage(name, eros));
			} 
			catch (FitsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
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
				ErosModel eros = (ErosModel)modelManager.getModel(ModelManager.EROS);
				MSIImage image = MSIImage.MSIImageFactory.createImage(name, eros);
				BoundingBox bb = image.getBoundingBox();
				renWin.lock();
				renWin.GetRenderer().ResetCamera(
						bb.xmin,
						bb.xmax,
						bb.ymin,
						bb.ymax,
						bb.zmin,
						bb.zmax);
				renWin.unlock();
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

	private class SaveBackplanesAction extends AbstractAction implements PropertyChangeListener
	{
		private JProgressBar progressBar;
		private JDialog dialog;

		public SaveBackplanesAction()
		{
			progressBar = new JProgressBar(0, MSIImage.IMAGE_HEIGHT);
			progressBar.setPreferredSize(new Dimension(175,20));
			//progressBar.setStringPainted(true);
			//progressBar.setString("");


			JLabel label = new JLabel("Progress: ");

			JPanel center_panel = new JPanel();
			center_panel.add(label);
			center_panel.add(progressBar);

			dialog = new JDialog((JFrame)null, "Working ...");
			dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
			//dialog.setModal(true);
			dialog.pack();
		}

		public void actionPerformed(ActionEvent e) 
		{
			File file = AnyFileChooser.showSaveDialog(invoker, "Save Backplanes");

			try 
			{
				if (file != null)
				{
					OutputStream out = new FileOutputStream(file);

					String name = currentImageOrBoundary;
					if (name.endsWith("_BOUNDARY.VTK"))
						name = name.substring(0, name.length()-13) + ".FIT";

					progressBar.setValue(0);
					dialog.setVisible(true);
					dialog.setLocationRelativeTo(invoker);
					dialog.toFront();

					ErosModel eros = (ErosModel)modelManager.getModel(ModelManager.EROS);
					MSIImage image = MSIImage.MSIImageFactory.createImage(name, eros);

					image.addPropertyChangeListener(this);
					
					float[] backplanes = image.generateBackplanes();

					byte[] buf = new byte[4 * backplanes.length];
					for (int i=0; i<backplanes.length; ++i)
					{
						int v = Float.floatToIntBits(backplanes[i]);
						buf[4*i + 0] = (byte)(v >>> 24);
						buf[4*i + 1] = (byte)(v >>> 16);
						buf[4*i + 2] = (byte)(v >>>  8);
						buf[4*i + 3] = (byte)(v >>>  0);
					}
					out.write(buf, 0, buf.length);
					out.close();
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(invoker,
						"Unable to save file to " + file.getAbsolutePath(),
						"Error Saving File",
						JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			if (evt.getPropertyName().equals(Properties.MSI_IMAGE_BACKPLANE_GENERATION_UPDATE))
			{
				System.out.println(evt.getNewValue());
				progressBar.setValue((Integer)evt.getNewValue());
			}
		}
	}
	
	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
			double[] pickedPosition)
	{
		if (pickedProp instanceof vtkActor)
		{
			if (modelManager.getModel(pickedProp) instanceof MSIBoundaryCollection)
			{
				MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
				String name = msiBoundaries.getBoundaryName((vtkActor)pickedProp);
				setCurrentImage(name);
				show(e.getComponent(), e.getX(), e.getY());
			}
			else if (modelManager.getModel(pickedProp) instanceof MSIImageCollection)
			{
				MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
				String name = msiImages.getImageName((vtkActor)pickedProp);
				setCurrentImage(name);
				show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	

}
