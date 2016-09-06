package edu.jhuapl.near.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;

public class PerspectiveImageBoundaryCollection extends AbstractModel implements PropertyChangeListener
{
    private HashMap<PerspectiveImageBoundary, ArrayList<vtkProp>> boundaryToActorsMap = new HashMap<PerspectiveImageBoundary, ArrayList<vtkProp>>();
    private HashMap<vtkProp, PerspectiveImageBoundary> actorToBoundaryMap = new HashMap<vtkProp, PerspectiveImageBoundary>();
    private SmallBodyModel smallBodyModel;
    // Create a buffer of initial boundary colors to use. We cycle through these colors when creating new boundaries
    private Color[] initialColors = {Color.RED, Color.PINK.darker(), Color.ORANGE.darker(),
            Color.GREEN.darker(), Color.MAGENTA, Color.CYAN.darker(), Color.BLUE,
            Color.GRAY, Color.DARK_GRAY, Color.BLACK};
    private int initialColorIndex = 0;

    public PerspectiveImageBoundaryCollection(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
    }

    protected PerspectiveImageBoundary createBoundary(
            ImageKey key,
            SmallBodyModel smallBodyModel) throws IOException, FitsException
    {
        PerspectiveImageBoundary boundary = new PerspectiveImageBoundary((PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, true), smallBodyModel);
        boundary.setBoundaryColor(initialColors[initialColorIndex++]);
        if (initialColorIndex >= initialColors.length)
            initialColorIndex = 0;
        return boundary;
    }

    private boolean containsKey(ImageKey key)
    {
        for (PerspectiveImageBoundary boundary : boundaryToActorsMap.keySet())
        {
            if (boundary.getKey().equals(key))
                return true;
        }

        return false;
    }

    private PerspectiveImageBoundary getBoundaryFromKey(ImageKey key)
    {
        for (PerspectiveImageBoundary boundary : boundaryToActorsMap.keySet())
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

        PerspectiveImageBoundary boundary = createBoundary(key, smallBodyModel);

        smallBodyModel.addPropertyChangeListener(boundary);
        boundary.addPropertyChangeListener(this);

        boundaryToActorsMap.put(boundary, new ArrayList<vtkProp>());

        List<vtkProp> boundaryPieces = boundary.getProps();

        boundaryToActorsMap.get(boundary).addAll(boundaryPieces);

        for (vtkProp act : boundaryPieces)
            actorToBoundaryMap.put(act, boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeBoundary(ImageKey key)
    {
        PerspectiveImageBoundary boundary = getBoundaryFromKey(key);

        if(boundary != null)
        {
            ArrayList<vtkProp> actors = boundaryToActorsMap.get(boundary);

            if(actors != null)
            {
                for (vtkProp act : actors)
                    actorToBoundaryMap.remove(act);
            }

            boundaryToActorsMap.remove(boundary);

            boundary.removePropertyChangeListener(this);
            smallBodyModel.removePropertyChangeListener(boundary);

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void removeAllBoundaries()
    {
        HashMap<PerspectiveImageBoundary, ArrayList<vtkProp>> map = (HashMap<PerspectiveImageBoundary, ArrayList<vtkProp>>)boundaryToActorsMap.clone();
        for (PerspectiveImageBoundary boundary : map.keySet())
            removeBoundary(boundary.getKey());
    }

    public List<vtkProp> getProps()
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

    public PerspectiveImageBoundary getBoundary(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor);
    }

    public PerspectiveImageBoundary getBoundary(ImageKey key)
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
        {
//            System.out.println("BoundaryCollection MODEL_CHANGED event: " + evt.getSource().getClass().getSimpleName());
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }
}
