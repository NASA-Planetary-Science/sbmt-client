package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.near.util.Properties;

abstract public class LidarBrowseDataCollection extends Model implements PropertyChangeListener
{
    private ArrayList<vtkProp> lidarPerUnitActors = new ArrayList<vtkProp>();

    private HashMap<String, LidarDataPerUnit> fileToLidarPerUnitMap = new HashMap<String, LidarDataPerUnit>();
    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private double radialOffset = 0.0;
    private boolean showSpacecraftPosition = true;

    public LidarBrowseDataCollection()
    {
    }

    public void addLidarData(String path) throws IOException
    {
        if (fileToLidarPerUnitMap.containsKey(path))
            return;

        LidarDataPerUnit lidarData = new LidarDataPerUnit(
                path,
                getXYZIndices(),
                getSpacecraftXYZIndices(),
                getTimeIndex(),
                getNumberHeaderLines(),
                isInMeters(),
                getNoiseIndex());
        lidarData.setShowSpacecraftPosition(showSpacecraftPosition);

        lidarData.addPropertyChangeListener(this);

        fileToLidarPerUnitMap.put(path, lidarData);

        for (vtkProp prop : lidarData.getProps())
        {
            actorToFileMap.put(prop, path);
            lidarPerUnitActors.add(prop);
        }

        this.setRadialOffset(radialOffset);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeLidarData(String path)
    {
        ArrayList<vtkProp> props = fileToLidarPerUnitMap.get(path).getProps();
        for (vtkProp prop : props)
        {
            lidarPerUnitActors.remove(prop);
            actorToFileMap.remove(prop);
        }

        fileToLidarPerUnitMap.remove(path);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllLidarData()
    {
        lidarPerUnitActors.clear();
        actorToFileMap.clear();
        fileToLidarPerUnitMap.clear();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public LidarDataPerUnit getLidarData(String path)
    {
        return fileToLidarPerUnitMap.get(path);
    }

    public ArrayList<vtkProp> getProps()
    {
        return lidarPerUnitActors;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        LidarDataPerUnit data = fileToLidarPerUnitMap.get(actorToFileMap.get(prop));
        return data.getClickStatusBarText(prop, cellId, pickPosition);
    }

    public String getLidarName(vtkActor actor)
    {
        return actorToFileMap.get(actor);
    }

    public boolean containsLidarData(String file)
    {
        return fileToLidarPerUnitMap.containsKey(file);
    }

    public ArrayList<String> getAllLidarPaths()
    {
        ArrayList<String> paths = new ArrayList<String>();

        InputStream is = getClass().getResourceAsStream(getFileListResourcePath());
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);

        String line;
        try
        {
            while ((line = in.readLine()) != null)
            {
                paths.add(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return paths;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setRadialOffset(double offset)
    {
        radialOffset = offset;

        if (fileToLidarPerUnitMap.isEmpty())
            return;

        for (String key : fileToLidarPerUnitMap.keySet())
        {
            LidarDataPerUnit data = fileToLidarPerUnitMap.get(key);
            data.setRadialOffset(offset);
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setShowSpacecraftPosition(boolean show)
    {
        showSpacecraftPosition = show;

        if (fileToLidarPerUnitMap.isEmpty())
            return;

        for (String key : fileToLidarPerUnitMap.keySet())
        {
            LidarDataPerUnit data = fileToLidarPerUnitMap.get(key);
            data.setShowSpacecraftPosition(show);
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    abstract protected int[] getXYZIndices();

    abstract protected int[] getSpacecraftXYZIndices();

    abstract protected int getTimeIndex();

    abstract protected int getNoiseIndex();

    abstract protected String getFileListResourcePath();

    abstract protected int getNumberHeaderLines();

    /**
     * Return whether or not the units of the lidar points are in meters. If false
     * they are assumed to be in kilometers.
     * @return
     */
    abstract protected boolean isInMeters();

    abstract public double getOffsetScale();
}
