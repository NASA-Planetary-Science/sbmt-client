package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.io.*;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import vtk.vtkRenderWindowPanel;

import nom.tam.fits.FitsException;
import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;


public class NISPopupMenu extends JPopupMenu 
{
    private ModelManager modelManager;
    private String currentSpectrum;
    private JMenuItem showRemoveSpectrumIn3DMenuItem;
    private JMenuItem showSpectrumInfoMenuItem;
    private JMenuItem centerSpectrumMenuItem;
    private ModelInfoWindowManager infoPanelManager;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    
    /**
     * 
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on Eros, 2 for right clicks on images
     * mapped to Eros.
     */
	public NISPopupMenu(
			ModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager,
			vtkRenderWindowPanel renWin)
	{
    	this.modelManager = modelManager;
    	this.infoPanelManager = infoPanelManager;
    	this.renWin = renWin;
    	this.erosModel = (ErosModel)modelManager.getModel(ModelManager.EROS);
    	
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

		NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);		
		if (model.containsSpectrum(name))
			showRemoveSpectrumIn3DMenuItem.setText("Remove Footprint");
		else
			showRemoveSpectrumIn3DMenuItem.setText("Show Footprint");
	}
	

	private class ShowRemoveIn3DAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			NISSpectraCollection model = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);		
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
	

}
