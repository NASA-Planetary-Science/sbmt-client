package edu.jhuapl.sbmt.client;

import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.model.AbstractModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;

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

    // See Redmine #1135. This method was added in an attempt to address rendering problems that were caused
    // by clipping range limitations, but it interacted badly with other features, specifically center-in-window,
    // but who knows what else would have been affected. Leaving the code here,
    // but commented out, in case we need to revisit this capability.
//    @Override
//    public void propertyChange(PropertyChangeEvent evt)
//    {
//        // This is a hack. Ideally, the Model interface and implementors would
//        // use a generic update mechanism, but currently there is no general framework
//        // for specific types of models (in this case Images) to get specific
//        // information from other abstractions (in this case, Cameras).
//        if (CameraProperties.CAMERA_DISTANCE.equals(evt.getPropertyName()))
//        {
//            Model model = getModel(ModelNames.IMAGES);
//            if (model instanceof ImageCollection)
//            {
//                ImageCollection collection = (ImageCollection) model;
//                for (Image image : collection.getImages())
//                {
//                    double offset = image.getOffset();
//                    offset = Math.max(0.001 * (double) evt.getNewValue(), image.getOffset());
//                    Image.applyOffset(image, offset);
//                }
//            }
//        }
//        else
//        {
//            super.propertyChange(evt);
//        }
//
//    }

}
