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

import edu.jhuapl.near.model.PerspectiveImage.ImageKey;
import edu.jhuapl.near.util.Properties;

public class PerspectiveImageCollection extends Model implements PropertyChangeListener
{
    private SmallBodyModel erosModel;

    private HashMap<PerspectiveImage, ArrayList<vtkProp>> imageToActorsMap = new HashMap<PerspectiveImage, ArrayList<vtkProp>>();

    private HashMap<vtkProp, PerspectiveImage> actorToImageMap = new HashMap<vtkProp, PerspectiveImage>();

    public PerspectiveImageCollection(SmallBodyModel eros)
    {
        this.erosModel = eros;
    }

    protected PerspectiveImage createImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        return ImageFactory.createImage(key, smallBodyModel, false, null);
    }

    private boolean containsKey(ImageKey key)
    {
        for (PerspectiveImage image : imageToActorsMap.keySet())
        {
            if (image.getKey().equals(key))
                return true;
        }

        return false;
    }

    private PerspectiveImage getImageFromKey(ImageKey key)
    {
        for (PerspectiveImage image : imageToActorsMap.keySet())
        {
            if (image.getKey().equals(key))
                return image;
        }

        return null;
    }

    public void addImage(ImageKey key) throws FitsException, IOException
    {
        if (containsKey(key))
            return;

        PerspectiveImage image = createImage(key, erosModel);

        erosModel.addPropertyChangeListener(image);
        image.addPropertyChangeListener(this);

        imageToActorsMap.put(image, new ArrayList<vtkProp>());

        ArrayList<vtkProp> imagePieces = image.getProps();

        imageToActorsMap.get(image).addAll(imagePieces);

        for (vtkProp act : imagePieces)
            actorToImageMap.put(act, image);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeImage(ImageKey key)
    {
        PerspectiveImage image = getImageFromKey(key);

        ArrayList<vtkProp> actors = imageToActorsMap.get(image);

        for (vtkProp act : actors)
            actorToImageMap.remove(act);

        imageToActorsMap.remove(image);

        image.removePropertyChangeListener(this);
        erosModel.removePropertyChangeListener(image);
        image.setShowFrustum(false);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, image);
    }

    public void removeAllImages()
    {
        HashMap<PerspectiveImage, ArrayList<vtkProp>> map = (HashMap<PerspectiveImage, ArrayList<vtkProp>>)imageToActorsMap.clone();
        for (PerspectiveImage image : map.keySet())
            removeImage(image.getKey());
    }

    public ArrayList<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToImageMap.keySet());
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        File file = new File(actorToImageMap.get(prop).getKey().name);
        return "Image " + file.getName();
    }

    public String getImageName(vtkActor actor)
    {
        return actorToImageMap.get(actor).getKey().name;
    }

    public PerspectiveImage getImage(vtkActor actor)
    {
        return actorToImageMap.get(actor);
    }

    public PerspectiveImage getImage(ImageKey key)
    {
        return getImageFromKey(key);
    }

    public boolean containsImage(ImageKey key)
    {
        return containsKey(key);
    }

    public void setShowFrustums(boolean b)
    {
        for (PerspectiveImage image : imageToActorsMap.keySet())
            image.setShowFrustum(b);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
