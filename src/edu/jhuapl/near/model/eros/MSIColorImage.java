package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import vtk.vtkProp;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.SmallBodyModel;

public class MSIColorImage extends Model implements PropertyChangeListener
{
    private SmallBodyModel erosModel;
    private MSIImage redImage;
    private MSIImage greenImage;
    private MSIImage blueImage;

    public MSIColorImage(MSIImage redImage, MSIImage greenImage, MSIImage blueImage, SmallBodyModel eros)
    {
        this.redImage = redImage;
        this.greenImage = greenImage;
        this.blueImage = blueImage;
        this.erosModel = eros;

    }

    @Override
    public ArrayList<vtkProp> getProps()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        // TODO Auto-generated method stub

    }

}
