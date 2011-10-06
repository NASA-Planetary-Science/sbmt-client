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
                getTimeIndex(),
                getNumberHeaderLines(),
                isInMeters(),
                getNoiseIndex());

        lidarData.addPropertyChangeListener(this);

        fileToLidarPerUnitMap.put(path, lidarData);

        actorToFileMap.put(lidarData.getProps().get(0), path);

        lidarPerUnitActors.add(lidarData.getProps().get(0));

        this.setRadialOffset(radialOffset);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeLidarData(String path)
    {
        vtkProp actor = fileToLidarPerUnitMap.get(path).getProps().get(0);

        lidarPerUnitActors.remove(actor);

        actorToFileMap.remove(actor);

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

    abstract protected int[] getXYZIndices();

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
}
