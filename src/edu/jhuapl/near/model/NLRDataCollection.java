package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import vtk.*;

public class NLRDataCollection extends Model 
{
	private ArrayList<vtkActor> nlrPerDayActors = new ArrayList<vtkActor>();

	private HashMap<String, NLRDataPerDay> fileToNlrPerDayMap = new HashMap<String, NLRDataPerDay>();
	private HashMap<vtkActor, String> actorToFileMap = new HashMap<vtkActor, String>();
	
	public void addNlrData(String path) throws IOException
	{
		if (fileToNlrPerDayMap.containsKey(path))
			return;

		NLRDataPerDay image = new NLRDataPerDay(path);

		fileToNlrPerDayMap.put(path, image);
		
		actorToFileMap.put(image.getActors().get(0), path);
		
		nlrPerDayActors.add(image.getActors().get(0));
		
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

	public ArrayList<vtkActor> getActors() 
	{
		return nlrPerDayActors;
	}
	
    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
    	File file = new File(actorToFileMap.get(actor));
    	return "NLR Data " + file.getName().substring(2, 11);
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
    
	public void setNlrRadialOffset(double offset)
	{
//        int ptId=0;
//        vtkPoints points = lineaments.GetPoints();
//        
//		for (Integer id : this.idToLineamentMap.keySet())
//		{
//			Lineament lin =	this.idToLineamentMap.get(id);
//
//            int size = lin.x.size();
//
//            for (int i=0;i<size;++i)
//            {
//                double x = (lin.rad.get(i)+offset) * Math.cos( lin.lon.get(i) ) * Math.cos( lin.lat.get(i) );
//                double y = (lin.rad.get(i)+offset) * Math.sin( lin.lon.get(i) ) * Math.cos( lin.lat.get(i) );
//                double z = (lin.rad.get(i)+offset) * Math.sin( lin.lat.get(i) );
//            	points.SetPoint(ptId, x, y, z);
//            	++ptId;
//            }
//		}		
//
//		lineaments.Modified();
//		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}


}
