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

import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.util.Properties;

public class LidarBrowseDataCollection extends Model implements PropertyChangeListener
{
    private ModelConfig modelConfig;
    private ArrayList<vtkProp> lidarPerUnitActors = new ArrayList<vtkProp>();

    private HashMap<String, LidarDataPerUnit> fileToLidarPerUnitMap = new HashMap<String, LidarDataPerUnit>();
    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private double radialOffset = 0.0;
    private double startPercent = 0.0;
    private double stopPercent = 1.0;
    private boolean showSpacecraftPosition = true;

    public LidarBrowseDataCollection(SmallBodyModel smallBodyModel)
    {
        this.modelConfig = smallBodyModel.getModelConfig();
    }

    public void addLidarData(String path) throws IOException
    {
        if (fileToLidarPerUnitMap.containsKey(path))
            return;

        LidarDataPerUnit lidarData = new LidarDataPerUnit(
                path, modelConfig);
        lidarData.setShowSpacecraftPosition(showSpacecraftPosition);

        lidarData.addPropertyChangeListener(this);

        fileToLidarPerUnitMap.put(path, lidarData);

        for (vtkProp prop : lidarData.getProps())
        {
            actorToFileMap.put(prop, path);
            lidarPerUnitActors.add(prop);
        }

        this.setOffset(radialOffset);
        this.setPercentageShown(startPercent, stopPercent);

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

        InputStream is = getClass().getResourceAsStream(modelConfig.lidarBrowseFileListResourcePath);
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

    public void setOffset(double offset)
    {
        radialOffset = offset;

        if (fileToLidarPerUnitMap.isEmpty())
            return;

        for (String key : fileToLidarPerUnitMap.keySet())
        {
            LidarDataPerUnit data = fileToLidarPerUnitMap.get(key);
            data.setOffset(offset);
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setPercentageShown(double startPercent, double stopPercent)
    {
        this.startPercent = startPercent;
        this.stopPercent = stopPercent;

        if (fileToLidarPerUnitMap.isEmpty())
            return;

        for (String key : fileToLidarPerUnitMap.keySet())
        {
            LidarDataPerUnit data = fileToLidarPerUnitMap.get(key);
            data.setPercentageShown(startPercent, stopPercent);
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

    public double getOffsetScale()
    {
        return modelConfig.lidarOffsetScale;
    }
}
