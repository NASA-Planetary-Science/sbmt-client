package edu.jhuapl.near.gui;

import java.awt.event.*;
import java.util.*;

import edu.jhuapl.near.model.*;

public class NISSpectrumInfoPanelManager 
{
	HashMap<NISSpectrum, NISSpectrumInfoPanel> infoPanels = 
		new HashMap<NISSpectrum, NISSpectrumInfoPanel>();
	
	ModelManager modelManager;
	
	public NISSpectrumInfoPanelManager(ModelManager modelManager) 
	{
		this.modelManager = modelManager;
	}
	public void addSpectrum(NISSpectrum spectrum)
	{
		if (infoPanels.containsKey(spectrum))
		{
			infoPanels.get(spectrum).toFront();
		}
		else
		{
			NISSpectrumInfoPanel infoPanel = new NISSpectrumInfoPanel(spectrum, modelManager);
			infoPanel.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					//NISSpectrum spectrum = ((NISSpectrumInfoPanel)e.getComponent()).getNearImage();
					//infoPanels.remove(spectrum);
				}
			});
			
			infoPanels.put(spectrum, infoPanel);
		}
	}
}
