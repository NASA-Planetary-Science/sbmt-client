package edu.jhuapl.near.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import edu.jhuapl.near.gui.eros.NISSpectrumInfoPanel;
import edu.jhuapl.near.model.ColorImage;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.util.Properties;

public class ModelInfoWindowManager implements PropertyChangeListener
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

            if (infoPanel == null)
            {
                throw new Exception("The Info Panel Manager cannot handle the model you specified.");
            }

            final Model collectionModel = infoPanel.getCollectionModel();

            model.addPropertyChangeListener(infoPanel);
            collectionModel.addPropertyChangeListener(this);

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

    public ModelInfoWindow createModelInfoWindow(Model model, ModelManager modelManager)
    {
        if (model instanceof ColorImage)
        {
            ColorImageCollection images = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
            return new ColorImageInfoPanel((ColorImage)model, images);
        }
        else if (model instanceof Image)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
            return new ImageInfoPanel((Image)model, images, boundaries);
        }
        else if (model instanceof NISSpectrum)
        {
            return new NISSpectrumInfoPanel((NISSpectrum)model, modelManager);
        }
        else
        {
            return null;
        }
    }
}
