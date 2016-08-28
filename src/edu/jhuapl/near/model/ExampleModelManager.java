package edu.jhuapl.near.model;

import java.beans.PropertyChangeListener;

public class ExampleModelManager extends AbstractModelManager implements PropertyChangeListener
{
    public ExampleModelManager()
    {
        super();
    }

    public SmallBodyModel getPolyhedralModel()
    {
        return (SmallBodyModel)super.getPolyhedralModel();
    }


}
