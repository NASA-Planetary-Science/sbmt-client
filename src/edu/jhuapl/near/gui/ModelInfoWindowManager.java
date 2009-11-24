package edu.jhuapl.near.gui;

import java.awt.event.*;
import java.util.*;

import edu.jhuapl.near.model.*;

public class ModelInfoWindowManager 
{
	HashMap<Model, ModelInfoWindow> infoPanels = 
		new HashMap<Model, ModelInfoWindow>();
	
	ModelManager modelManager;
	
	public ModelInfoWindowManager(ModelManager modelManager) 
	{
		this.modelManager = modelManager;
	}
	public void addData(Model model) throws Exception
	{
		if (infoPanels.containsKey(model))
		{
			infoPanels.get(model).toFront();
		}
		else
		{
			ModelInfoWindow infoPanel = null;
			
			if (model instanceof NearImage)
				infoPanel = new MSIImageInfoPanel((NearImage)model, modelManager);
			else if (model instanceof NISSpectrum)
				infoPanel = new NISSpectrumInfoPanel((NISSpectrum)model, modelManager);
			else
			{
				throw new Exception("The Info Panel Manager cannot handle the model you specified.");
			}
			
			infoPanel.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					Model mod = ((ModelInfoWindow)e.getComponent()).getModel();
					infoPanels.remove(mod);
				}
			});
			
			infoPanels.put(model, infoPanel);
		}
	}
}
