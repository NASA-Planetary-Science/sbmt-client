package edu.jhuapl.near.gui;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.Properties;

abstract public class ModelInfoWindowManager implements PropertyChangeListener
{
	HashMap<Model, ModelInfoWindow> infoPanels = 
		new HashMap<Model, ModelInfoWindow>();
	
	ModelManager modelManager;
	
	public ModelInfoWindowManager(ModelManager modelManager) 
	{
		this.modelManager = modelManager;
	}
	public void addData(final Model model) throws Exception
	{
		if (infoPanels.containsKey(model))
		{
			infoPanels.get(model).toFront();
		}
		else
		{
			final ModelInfoWindow infoPanel = createModelInfoWindow(model, modelManager);
		
			final Model collectionModel = infoPanel.getCollectionModel();
			
			model.addPropertyChangeListener(infoPanel);
			collectionModel.addPropertyChangeListener(this);
			
			if (infoPanel == null)
			{
				throw new Exception("The Info Panel Manager cannot handle the model you specified.");
			}
			
			infoPanel.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					Model mod = infoPanel.getModel();
					infoPanels.remove(mod);
					model.removePropertyChangeListener(infoPanel);
					collectionModel.removePropertyChangeListener(ModelInfoWindowManager.this);
				}
			});
			
			infoPanels.put(model, infoPanel);
		}
	}

	public void propertyChange(PropertyChangeEvent e) 
	{
    	if (e.getPropertyName().equals(Properties.MODEL_REMOVED))
    	{
    		Object model = e.getNewValue();
    		if (infoPanels.containsKey(model))
    		{
    			ModelInfoWindow frame = infoPanels.get(model);
    			frame.setVisible(false);
    			frame.dispose();
    		}
    	}
	}

	abstract public ModelInfoWindow createModelInfoWindow(Model model, ModelManager modelManager);
}
