package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import vtk.*;

public class NLRDataCollection extends Model implements PropertyChangeListener 
{
	private ArrayList<vtkActor> nlrPerDayActors = new ArrayList<vtkActor>();

	private HashMap<String, NLRDataPerDay> fileToNlrPerDayMap = new HashMap<String, NLRDataPerDay>();
	private HashMap<vtkActor, String> actorToFileMap = new HashMap<vtkActor, String>();
	private double radialOffset = 0.0;
	
	public void addNlrData(String path) throws IOException
	{
		if (fileToNlrPerDayMap.containsKey(path))
			return;

		NLRDataPerDay nlrData = new NLRDataPerDay(path);

		nlrData.addPropertyChangeListener(this);
		
		fileToNlrPerDayMap.put(path, nlrData);
		
		actorToFileMap.put(nlrData.getActors().get(0), path);
		
		nlrPerDayActors.add(nlrData.getActors().get(0));
	
		this.setRadialOffset(radialOffset);
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeNlrData(String path)
	{
		vtkActor actor = fileToNlrPerDayMap.get(path).getActors().get(0);
		
		nlrPerDayActors.remove(actor);

		actorToFileMap.remove(actor);
		
		fileToNlrPerDayMap.remove(path);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllNlrData()
	{
		nlrPerDayActors.clear();
		actorToFileMap.clear();
		fileToNlrPerDayMap.clear();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public NLRDataPerDay getNlrData(String path)
	{
		return fileToNlrPerDayMap.get(path);
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		return nlrPerDayActors;
	}
	
    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
    	NLRDataPerDay data = fileToNlrPerDayMap.get(actorToFileMap.get(actor));
    	return data.getClickStatusBarText(actor, cellId);
    }

    public String getNlrName(vtkActor actor)
    {
    	return actorToFileMap.get(actor);
    }
    
    public boolean containsNlrData(String file)
    {
    	return fileToNlrPerDayMap.containsKey(file);
    }
    
    public ArrayList<String> getAllNlrPaths()
    {
    	ArrayList<String> paths = new ArrayList<String>();
    	
		InputStream is = getClass().getResourceAsStream("/edu/jhuapl/near/data/NlrFiles.txt");
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
		
    	if (fileToNlrPerDayMap.isEmpty())
    		return;

    	for (String key : fileToNlrPerDayMap.keySet())
    	{
    		NLRDataPerDay data = fileToNlrPerDayMap.get(key);
    		data.setRadialOffset(offset);
    	}
    	
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}


}
