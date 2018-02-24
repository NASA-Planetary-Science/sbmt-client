package edu.jhuapl.sbmt.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.gui.render.camera.CameraProperties;
import edu.jhuapl.saavtk.model.AbstractModelManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;

public class SbmtModelManager extends AbstractModelManager implements PropertyChangeListener
{
    public SbmtModelManager(PolyhedralModel mainModel)
    {
        super(mainModel);
    }

    public SmallBodyModel getPolyhedralModel()
    {
        return (SmallBodyModel)super.getPolyhedralModel();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        // This is a hack. Ideally, the Model interface and implementors would
        // use a generic update mechanism, but currently there is no general framework
        // for specific types of models (in this case Images) to get specific
        // information from other abstractions (in this case, Cameras).
        if (CameraProperties.CAMERA_DISTANCE.equals(evt.getPropertyName()))
        {
            Model model = getModel(ModelNames.IMAGES);
            if (model instanceof ImageCollection)
            {
                ImageCollection collection = (ImageCollection) model;
                for (Image image : collection.getImages())
                {
                    double offset = image.getOffset();
                    offset = Math.max(0.001 * (double) evt.getNewValue(), image.getOffset());
                    Image.applyOffset(image, offset);
                }
            }
        }
        else
        {
            super.propertyChange(evt);
        }

    }

}
