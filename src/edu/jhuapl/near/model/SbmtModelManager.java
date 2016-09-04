package edu.jhuapl.near.model;

import java.beans.PropertyChangeListener;

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
