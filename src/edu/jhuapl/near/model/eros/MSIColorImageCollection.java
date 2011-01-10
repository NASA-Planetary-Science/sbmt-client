package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIColorImage.MSIColorKey;
import edu.jhuapl.near.model.eros.MSIColorImage.NoOverlapException;
import edu.jhuapl.near.util.Properties;

public class MSIColorImageCollection extends Model implements PropertyChangeListener
{
    private SmallBodyModel erosModel;
    //private MSIImageCollection imageCollection;

    private HashMap<MSIColorImage, ArrayList<vtkProp>> imageToActorsMap = new HashMap<MSIColorImage, ArrayList<vtkProp>>();

    private HashMap<vtkProp, MSIColorImage> actorToImageMap = new HashMap<vtkProp, MSIColorImage>();

    public MSIColorImageCollection(SmallBodyModel eros)
    {
        super(ModelNames.MSI_COLOR_IMAGES);

        this.erosModel = eros;
        //this.imageCollection = imageCollection;
    }

    private boolean containsKey(MSIColorKey key)
    {
        for (MSIColorImage image : imageToActorsMap.keySet())
        {
            if (image.getKey().equals(key))
                return true;
        }

        return false;
    }

    private MSIColorImage getImageFromKey(MSIColorKey key)
    {
        for (MSIColorImage image : imageToActorsMap.keySet())
        {
            if (image.getKey().equals(key))
                return image;
        }

        return null;
    }

    public void addImage(MSIColorKey key) throws IOException, FitsException, NoOverlapException
    {
        if (containsKey(key))
            return;

        MSIColorImage image = new MSIColorImage(key, erosModel);

        erosModel.addPropertyChangeListener(image);
        image.addPropertyChangeListener(this);

        imageToActorsMap.put(image, new ArrayList<vtkProp>());

        ArrayList<vtkProp> imagePieces = image.getProps();

        imageToActorsMap.get(image).addAll(imagePieces);

        for (vtkProp act : imagePieces)
            actorToImageMap.put(act, image);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeImage(MSIColorKey key)
    {
        MSIColorImage image = getImageFromKey(key);

        ArrayList<vtkProp> actors = imageToActorsMap.get(image);

        for (vtkProp act : actors)
            actorToImageMap.remove(act);

        imageToActorsMap.remove(image);

        image.removePropertyChangeListener(this);
        erosModel.removePropertyChangeListener(image);
        //image.setShowFrustum(false);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, image);
    }

    public void removeAllImages()
    {
        HashMap<MSIColorImage, ArrayList<vtkProp>> map = (HashMap<MSIColorImage, ArrayList<vtkProp>>)imageToActorsMap.clone();
        for (MSIColorImage image : map.keySet())
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

//    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
//    {
//        File file = new File(actorToImageMap.get(prop).getKey().name);
//        return "MSI image " + file.getName().substring(2, 11);
//    }

//    public String getImageName(vtkActor actor)
//    {
//        return actorToImageMap.get(actor).getKey().name;
//    }

    public MSIColorImage getImage(vtkActor actor)
    {
        return actorToImageMap.get(actor);
    }

    public MSIColorImage getImage(MSIColorKey key)
    {
        return getImageFromKey(key);
    }

    public boolean containsImage(MSIColorKey key)
    {
        return containsKey(key);
    }

//    public void setShowFrustums(boolean b)
//    {
//        for (MSIColorImage image : imageToActorsMap.keySet())
//            image.setShowFrustum(b);
//
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//    }
}
