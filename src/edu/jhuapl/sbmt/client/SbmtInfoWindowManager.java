package edu.jhuapl.sbmt.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.WindowManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.gui.eros.NISSpectrumInfoPanel;
import edu.jhuapl.sbmt.gui.image.ColorImageInfoPanel;
import edu.jhuapl.sbmt.gui.image.ImageInfoPanel;
import edu.jhuapl.sbmt.gui.spectrum.SpectrumStatisticsInfoPanel;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSpectrumInfoPanel;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSpectrumInfoPanel;
import edu.jhuapl.sbmt.model.eros.NISSpectrum;
import edu.jhuapl.sbmt.model.image.ColorImage;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.rosetta.OsirisImage;
import edu.jhuapl.sbmt.model.rosetta.OsirisImageInfoPanel;
import edu.jhuapl.sbmt.model.spectrum.statistics.SpectrumStatistics;

public class SbmtInfoWindowManager implements WindowManager, PropertyChangeListener
{
    HashMap<Model, ModelInfoWindow> infoPanels =
        new HashMap<Model, ModelInfoWindow>();

    ModelManager modelManager;
    StatusBar statusBar;

    public SbmtInfoWindowManager(ModelManager modelManager, StatusBar statusBar)
    {
        this.modelManager = modelManager;
        this.statusBar = statusBar;
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
                    collectionModel.removePropertyChangeListener(SbmtInfoWindowManager.this);
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
        System.out
                .println("SbmtInfoWindowManager: createModelInfoWindow: model is of type " + model.getClass());
        if (model instanceof ColorImage)
        {
            ColorImageCollection images = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
            return new ColorImageInfoPanel((ColorImage)model, images, statusBar);
        }
        else if (model instanceof Image)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
            if (model instanceof OsirisImage)
                return new OsirisImageInfoPanel((Image)model, images, boundaries, statusBar);
            return new ImageInfoPanel((Image)model, images, boundaries, statusBar);
        }
        else if (model instanceof NISSpectrum)
        {
            return new NISSpectrumInfoPanel((NISSpectrum)model, modelManager);
        }
        else if (model instanceof OTESSpectrum)
        {
            return new OTESSpectrumInfoPanel((OTESSpectrum)model, modelManager);
        }
        else if (model instanceof OVIRSSpectrum)
        {
            return new OVIRSSpectrumInfoPanel((OVIRSSpectrum)model, modelManager);
        }
        else if (model instanceof SpectrumStatistics)
        {
            return new SpectrumStatisticsInfoPanel((SpectrumStatistics)model,modelManager);
        }
        else
        {
            return null;
        }
    }
}
