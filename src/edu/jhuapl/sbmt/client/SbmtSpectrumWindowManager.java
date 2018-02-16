package edu.jhuapl.sbmt.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.WindowManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class SbmtSpectrumWindowManager implements WindowManager, PropertyChangeListener
{
    HashMap<Model, ModelInfoWindow> spectrumPanels = new HashMap<Model, ModelInfoWindow>();

    ModelManager modelManager;

    public SbmtSpectrumWindowManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
    }

    public int getNumberSpectrumModels()
    {
        return spectrumPanels.size();
    }

    public void addData(final Model model) throws Exception
    {
        if (spectrumPanels.containsKey(model))
        {
            spectrumPanels.get(model).toFront();
        }
        else
        {
            final ModelInfoWindow spectrumPanel = createModelSpectrumWindow(model, modelManager);

            if (spectrumPanel == null)
            {
                throw new Exception("The Spectrum Panel Manager cannot handle the model you specified.");
            }

            final Model collectionModel = spectrumPanel.getCollectionModel();

            model.addPropertyChangeListener(spectrumPanel);
            collectionModel.addPropertyChangeListener(this);

            spectrumPanel.addWindowListener(new WindowAdapter()
            {
                public void windowClosed(WindowEvent e)
                {
                    Model mod = spectrumPanel.getModel();
                    spectrumPanels.remove(mod);
                    model.removePropertyChangeListener(spectrumPanel);
                    collectionModel.removePropertyChangeListener(SbmtSpectrumWindowManager.this);
                }
            });

            spectrumPanels.put(model, spectrumPanel);
        }
    }

    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(Properties.MODEL_REMOVED))
        {
            Object model = e.getNewValue();
            if (spectrumPanels.containsKey(model))
            {
                ModelInfoWindow frame = spectrumPanels.get(model);
                frame.setVisible(false);
                frame.dispose();
            }
        }
    }

    public ModelInfoWindow createModelSpectrumWindow(Model model, ModelManager modelManager)
    {
        if (model instanceof PerspectiveImage && ((PerspectiveImage)model).getImageDepth() > 1)
        {
            return new MultispectralSpectrumInfoPanel((PerspectiveImage)model, modelManager);
        }
//        else if (model instanceof ColorImage)
//        {
//            return new MultispectralSpectrumInfoPanel((ColorImage)model, modelManager);
//        }
        else
        {
            return null;
        }
    }
}
