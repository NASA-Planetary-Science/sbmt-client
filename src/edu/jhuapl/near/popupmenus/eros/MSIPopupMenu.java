package edu.jhuapl.near.popupmenus.eros;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkProp;

import nom.tam.fits.FitsException;
import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.gui.eros.ModelInfoWindowManager;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImageCollection;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection.Boundary;
import edu.jhuapl.near.model.eros.MSIImage.MSIKey;
import edu.jhuapl.near.popupmenus.PopupMenu;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.GeometryUtil;


public class MSIPopupMenu extends PopupMenu 
{
	private Component invoker;
    private ErosModelManager modelManager;
    private MSIKey msiKey;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem showRemoveBoundaryIn3DMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem saveToDiskMenuItem;
    private JMenuItem saveBackplanesMenuItem;
    private JMenuItem centerImageMenuItem;
    private ModelInfoWindowManager infoPanelManager;
    private Renderer renderer;
    
    /**
     * 
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
	public MSIPopupMenu(
			ErosModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager,
			Renderer renderer,
			Component invoker)
	{
    	this.modelManager = modelManager;
    	this.infoPanelManager = infoPanelManager;
    	this.renderer = renderer;
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
		centerImageMenuItem.setText("Center Image in Window");
		this.add(centerImageMenuItem);

	}

	public void setCurrentImage(MSIKey key)
	{
		msiKey = key;
		
		MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ErosModelManager.MSI_BOUNDARY);
		if (msiBoundaries.containsBoundary(msiKey))
			showRemoveBoundaryIn3DMenuItem.setText("Remove Image Boundary");
		else
			showRemoveBoundaryIn3DMenuItem.setText("Show Image Boundary");
		
		MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ErosModelManager.MSI_IMAGES);
		if (msiImages.containsImage(msiKey))
			showRemoveImageIn3DMenuItem.setText("Remove Image");
		else
			showRemoveImageIn3DMenuItem.setText("Show Image");
		
		if (msiBoundaries.containsBoundary(msiKey) || msiImages.containsImage(msiKey))
		    centerImageMenuItem.setEnabled(true);
		else
		    centerImageMenuItem.setEnabled(false);
	}
	

	private class ShowRemoveIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			MSIImageCollection model = (MSIImageCollection)modelManager.getModel(ErosModelManager.MSI_IMAGES);
			try 
			{
				if (showRemoveImageIn3DMenuItem.getText().startsWith("Show"))
					model.addImage(msiKey);
				else
					model.removeImage(msiKey);
				
				// Force an update on the first 2 menu items
				setCurrentImage(msiKey);
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
			MSIBoundaryCollection model = (MSIBoundaryCollection)modelManager.getModel(ErosModelManager.MSI_BOUNDARY);
			try 
			{
				if (showRemoveBoundaryIn3DMenuItem.getText().startsWith("Show"))
					model.addBoundary(msiKey);
				else
					model.removeBoundary(msiKey);

				// Force an update on the first 2 menu items
				setCurrentImage(msiKey);
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
			try 
			{
				SmallBodyModel eros = (SmallBodyModel)modelManager.getModel(ErosModelManager.EROS);
				infoPanelManager.addData(MSIImage.MSIImageFactory.createImage(msiKey, eros));
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
			File file = FITFileChooser.showSaveDialog(invoker, "Save FIT file", msiKey.name + ".FIT");
			try
			{
				if (file != null)
				{
					File fitFile = FileCache.getFileFromServer(msiKey.name + ".FIT");

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
            double[] spacecraftPosition = new double[3];
            double[] boresightDirection = new double[3];
            double[] upVector = new double[3];

            MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ErosModelManager.MSI_BOUNDARY);
	        MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ErosModelManager.MSI_IMAGES);
	        if (msiBoundaries.containsBoundary(msiKey))
	        {
	            Boundary boundary = msiBoundaries.getBoundary(msiKey);
                boundary.getCameraOrientation(spacecraftPosition, boresightDirection, upVector);
	        }
	        else if (msiImages.containsImage(msiKey))
	        {
                MSIImage image = msiImages.getImage(msiKey);
                image.getCameraOrientation(spacecraftPosition, boresightDirection, upVector);
	        }
	        else
	        {
	            return;
	        }
	        
	        final double norm = GeometryUtil.vnorm(spacecraftPosition);
	        double[] position = {
	                spacecraftPosition[0] + 0.6*norm*boresightDirection[0],
	                spacecraftPosition[1] + 0.6*norm*boresightDirection[1],
	                spacecraftPosition[2] + 0.6*norm*boresightDirection[2]
	        };
	        double[] focalPoint = {
	                position[0] + 0.25*norm*boresightDirection[0],
	                position[1] + 0.25*norm*boresightDirection[1],
	                position[2] + 0.25*norm*boresightDirection[2]
	        };

            renderer.setCameraOrientation(position, focalPoint, upVector);
		}
	}

	private class SaveBackplanesAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			// First generate the DDR
			
			File file = AnyFileChooser.showSaveDialog(invoker, "Save Backplanes DDR", msiKey.name + "_DDR.IMG");

			try 
			{
				if (file != null)
				{
					OutputStream out = new FileOutputStream(file);

					SmallBodyModel eros = (SmallBodyModel)modelManager.getModel(ErosModelManager.EROS);
					MSIImage image = MSIImage.MSIImageFactory.createImage(msiKey, eros);

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

			// Then generate the LBL file
			
			file = AnyFileChooser.showSaveDialog(invoker, "Save Backplanes Label", msiKey.name + "_DDR.LBL");

			try 
			{
				if (file != null)
				{
					OutputStream out = new FileOutputStream(file);

					SmallBodyModel eros = (SmallBodyModel)modelManager.getModel(ErosModelManager.EROS);
					MSIImage image = MSIImage.MSIImageFactory.createImage(msiKey, eros);

					String lblstr = image.generateBackplanesLabel();

					byte[] bytes = lblstr.getBytes();
					out.write(bytes, 0, bytes.length);
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
	}
	
	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
			double[] pickedPosition)
	{
		if (pickedProp instanceof vtkActor)
		{
			if (modelManager.getModel(pickedProp) instanceof MSIBoundaryCollection)
			{
				MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ErosModelManager.MSI_BOUNDARY);
				Boundary boundary = msiBoundaries.getBoundary((vtkActor)pickedProp);
				setCurrentImage(boundary.getKey());
				show(e.getComponent(), e.getX(), e.getY());
			}
			else if (modelManager.getModel(pickedProp) instanceof MSIImageCollection)
			{
				MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ErosModelManager.MSI_IMAGES);
				MSIImage image = msiImages.getImage((vtkActor)pickedProp);
				setCurrentImage(image.getKey());
				show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

}
