package edu.jhuapl.near.gui;

import java.awt.event.*;
import java.util.*;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.NearImage;

public class MSIImageInfoPanelManager 
{
	HashMap<NearImage, MSIImageInfoPanel> infoPanels = 
		new HashMap<NearImage, MSIImageInfoPanel>();
	
	ModelManager modelManager;
	
	public MSIImageInfoPanelManager(ModelManager modelManager) 
	{
		this.modelManager = modelManager;
	}
	public void addImage(NearImage image)
	{
		if (infoPanels.containsKey(image))
		{
			infoPanels.get(image).toFront();
		}
		else
		{
			MSIImageInfoPanel infoPanel = new MSIImageInfoPanel(image, modelManager);
			infoPanel.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					NearImage image = ((MSIImageInfoPanel)e.getComponent()).getNearImage();
					infoPanels.remove(image);
				}
			});
			
			infoPanels.put(image, infoPanel);
		}
	}
}
