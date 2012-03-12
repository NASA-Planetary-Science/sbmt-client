package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.util.Properties;

public class ImageBoundaryCollection extends Model implements PropertyChangeListener
{
    private HashMap<ImageBoundary, ArrayList<vtkProp>> boundaryToActorsMap = new HashMap<ImageBoundary, ArrayList<vtkProp>>();
    private HashMap<vtkProp, ImageBoundary> actorToBoundaryMap = new HashMap<vtkProp, ImageBoundary>();
    private SmallBodyModel smallBodyModel;

    public ImageBoundaryCollection(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
    }

    protected ImageBoundary createBoundary(
            ImageKey key,
            SmallBodyModel smallBodyModel) throws IOException, FitsException
    {
        return new ImageBoundary(ImageFactory.createImage(key, smallBodyModel, true, null), smallBodyModel);
    }

    private boolean containsKey(ImageKey key)
    {
        for (ImageBoundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.getKey().equals(key))
                return true;
        }

        return false;
    }

    private ImageBoundary getBoundaryFromKey(ImageKey key)
    {
        for (ImageBoundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.getKey().equals(key))
                return boundary;
        }

        return null;
    }


    public void addBoundary(ImageKey key) throws FitsException, IOException
    {
        if (containsKey(key))
            return;

        ImageBoundary boundary = createBoundary(key, smallBodyModel);

        smallBodyModel.addPropertyChangeListener(boundary);
        boundary.addPropertyChangeListener(this);

        boundaryToActorsMap.put(boundary, new ArrayList<vtkProp>());

        ArrayList<vtkProp> boundaryPieces = boundary.getProps();

        boundaryToActorsMap.get(boundary).addAll(boundaryPieces);

        for (vtkProp act : boundaryPieces)
            actorToBoundaryMap.put(act, boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeBoundary(ImageKey key)
    {
        ImageBoundary boundary = getBoundaryFromKey(key);

        ArrayList<vtkProp> actors = boundaryToActorsMap.get(boundary);

        for (vtkProp act : actors)
            actorToBoundaryMap.remove(act);

        boundaryToActorsMap.remove(boundary);

        boundary.removePropertyChangeListener(this);
        smallBodyModel.removePropertyChangeListener(boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllBoundaries()
    {
        HashMap<ImageBoundary, ArrayList<vtkProp>> map = (HashMap<ImageBoundary, ArrayList<vtkProp>>)boundaryToActorsMap.clone();
        for (ImageBoundary boundary : map.keySet())
            removeBoundary(boundary.getKey());
    }

    public ArrayList<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToBoundaryMap.keySet());
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        File file = new File(actorToBoundaryMap.get(prop).getKey().name);
        return "Boundary of image " + file.getName();
    }

    public String getBoundaryName(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor).getKey().name;
    }

    public ImageBoundary getBoundary(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor);
    }

    public ImageBoundary getBoundary(ImageKey key)
    {
        return getBoundaryFromKey(key);
    }

    public boolean containsBoundary(ImageKey key)
    {
        return containsKey(key);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
