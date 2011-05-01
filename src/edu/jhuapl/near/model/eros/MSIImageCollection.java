package edu.jhuapl.near.model.eros;

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
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Properties;

public class MSIImageCollection extends Model implements PropertyChangeListener
{
    private SmallBodyModel erosModel;

    private HashMap<MSIImage, ArrayList<vtkProp>> imageToActorsMap = new HashMap<MSIImage, ArrayList<vtkProp>>();

    private HashMap<vtkProp, MSIImage> actorToImageMap = new HashMap<vtkProp, MSIImage>();

    public MSIImageCollection(SmallBodyModel eros)
    {
        super(ModelNames.MSI_IMAGES);

        this.erosModel = eros;
    }

    private boolean containsKey(ImageKey key)
    {
        for (MSIImage image : imageToActorsMap.keySet())
        {
            if (image.getKey().equals(key))
                return true;
        }

        return false;
    }

    private MSIImage getImageFromKey(ImageKey key)
    {
        for (MSIImage image : imageToActorsMap.keySet())
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

//        MSIImage image = MSIImage.MSIImageFactory.createImage(key, erosModel);
        MSIImage image = new MSIImage(key, erosModel);

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
        MSIImage image = getImageFromKey(key);

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
        HashMap<MSIImage, ArrayList<vtkProp>> map = (HashMap<MSIImage, ArrayList<vtkProp>>)imageToActorsMap.clone();
        for (MSIImage image : map.keySet())
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
        return "MSI image " + file.getName().substring(2, 11);
    }

    public String getImageName(vtkActor actor)
    {
        return actorToImageMap.get(actor).getKey().name;
    }

    public MSIImage getImage(vtkActor actor)
    {
        return actorToImageMap.get(actor);
    }

    public MSIImage getImage(ImageKey key)
    {
        return getImageFromKey(key);
    }

    public boolean containsImage(ImageKey key)
    {
        return containsKey(key);
    }

    public void setShowFrustums(boolean b)
    {
        for (MSIImage image : imageToActorsMap.keySet())
            image.setShowFrustum(b);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
