package edu.jhuapl.near.popupmenus.eros;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkActor;
import vtk.vtkProp;

import nom.tam.fits.FitsException;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.popupmenus.PopupMenu;


public class NISPopupMenu extends PopupMenu 
{
    private ModelManager modelManager;
    private String currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumInfoMenuItem;
    private JMenuItem centerSpectrumMenuItem;
    private ModelInfoWindowManager infoPanelManager;
    private SmallBodyModel erosModel;
    
    /**
     * 
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
	public NISPopupMenu(
			ModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager)
	{
    	this.modelManager = modelManager;
    	this.infoPanelManager = infoPanelManager;
    	this.erosModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
    	
		showRemoveSpectrumIn3DMenuItem = new JMenuItem(new ShowRemoveIn3DAction());
		this.add(showRemoveSpectrumIn3DMenuItem);
		
		if (this.infoPanelManager != null)
		{
			showSpectrumInfoMenuItem = new JMenuItem(new ShowInfoAction());
			showSpectrumInfoMenuItem.setText("Spectrum...");
			this.add(showSpectrumInfoMenuItem);
		}
		
		centerSpectrumMenuItem = new JMenuItem(new CenterImageAction());
		centerSpectrumMenuItem.setText("Center in Window");
		//this.add(centerImageMenuItem);

	}

	
	public void setCurrentSpectrum(String name)
	{
		currentSpectrum = name;

		NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);		
		if (model.containsSpectrum(name))
			showRemoveSpectrumIn3DMenuItem.setText("Remove Footprint");
		else
			showRemoveSpectrumIn3DMenuItem.setText("Show Footprint");
	}
	

	private class ShowRemoveIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);		
			try 
			{
				if (showRemoveSpectrumIn3DMenuItem.getText().startsWith("Show"))
					model.addSpectrum(currentSpectrum);
				else
					model.removeImage(currentSpectrum);
				
				// Force an update on the first 2 menu items
				setCurrentSpectrum(currentSpectrum);
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
			String name = currentSpectrum;
			
			try 
			{
				infoPanelManager.addData(NISSpectrum.NISSpectrumFactory.createSpectrum(name, erosModel));
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
	
	
	private class CenterImageAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
		}
	}


	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
			double[] pickedPosition)
	{
		if (pickedProp instanceof vtkActor)
		{
			NISSpectraCollection msiImages = (NISSpectraCollection)modelManager.getModel(ModelNames.NIS_SPECTRA);
			String name = msiImages.getSpectrumName((vtkActor)pickedProp);
			setCurrentSpectrum(name);
			show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
}
