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

}
