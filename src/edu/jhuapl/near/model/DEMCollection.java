package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.model.DEM.DEMKey;
import edu.jhuapl.near.util.Properties;

public class DEMCollection extends Model implements PropertyChangeListener
{
    private SmallBodyModel smallBodyModel;

    private HashMap<DEM, ArrayList<vtkProp>> demToActorsMap = new HashMap<DEM, ArrayList<vtkProp>>();

    private HashMap<vtkProp, DEM> actorToDemMap = new HashMap<vtkProp, DEM>();

    public DEMCollection(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
    }

    // Creates a DEM based on a key
    protected DEM createDEM(DEMKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        return new DEM(key);
    }

    // Checks if key exists in map
    private boolean containsKey(DEMKey key)
    {
        for (DEM dem : demToActorsMap.keySet())
        {
            if (dem.getKey().equals(key))
                return true;
        }

        return false;
    }

    // Gets the DEM associated with the key, otherwise returns
    // null if key is not in map
    private DEM getDEMFromKey(DEMKey key)
    {
        for (DEM dem : demToActorsMap.keySet())
        {
            if (dem.getKey().equals(key))
                return dem;
        }

        return null;
    }

    public DEM getDEM(vtkActor actor)
    {
        return actorToDemMap.get(actor);
    }

    public DEM getDEM(DEMKey key)
    {
        return getDEMFromKey(key);
    }

    // Gets set of all DEMs stored in the map
    public Set<DEM> getImages()
    {
        return demToActorsMap.keySet();
    }

    public void addDEM(DEMKey key) throws FitsException, IOException
    {
        // Nothing to do if collection already contains this key
        if(containsKey(key))
        {
            return;
        }

        // Create the DEM
        DEM dem = createDEM(key, smallBodyModel);
        smallBodyModel.addPropertyChangeListener(dem);
        dem.addPropertyChangeListener(this);

        demToActorsMap.put(dem, new ArrayList<vtkProp>());

        List<vtkProp> demPieces = dem.getProps();

        demToActorsMap.get(dem).addAll(demPieces);

        for (vtkProp act : demPieces)
        {
            actorToDemMap.put(act, dem);
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeDEM(DEMKey key)
    {
        // Nothing to remove if key does not exist in map
        if (!containsKey(key))
        {
            return;
        }

        DEM dem = getDEMFromKey(key);

        ArrayList<vtkProp> actors = demToActorsMap.get(dem);

        for (vtkProp act : actors)
        {
            actorToDemMap.remove(act);
        }

        demToActorsMap.remove(dem);

        dem.removePropertyChangeListener(this);
        smallBodyModel.removePropertyChangeListener(dem);
        dem.demAboutToBeRemoved();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, dem);
    }

    public boolean containsDEM(DEMKey key)
    {
        return containsKey(key);
    }

    public List<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToDemMap.keySet());
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
