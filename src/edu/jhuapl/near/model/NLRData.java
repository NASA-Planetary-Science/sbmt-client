package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.Properties;

import vtk.*;

public class NLRData extends Model 
{
	private static class NLRDataPerDay
	{
		public vtkActor actor;

		public NLRDataPerDay(String path) throws IOException
		{
			File file = FileCache.getFileFromServer(path);

			if (file == null)
				throw new IOException(path + " could not be loaded");

			ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
			
			vtkPoints points = new vtkPoints();
			vtkCellArray vert = new vtkCellArray();
			vtkPolyData pd = new vtkPolyData();
			pd.SetPoints( points );
			pd.SetVerts( vert );
			 
			for (int i=2; i<lines.size(); ++i)
			{
	            String [] vals = lines.get(i).split("\\s");

				int id = points.InsertNextPoint(
						Double.parseDouble(vals[14]),
						Double.parseDouble(vals[15]),
						Double.parseDouble(vals[16]));
			    vert.InsertNextCell(id);
			}

	        vtkPolyDataMapper pointsMapper = new vtkPolyDataMapper();
	        pointsMapper.SetInput(pd);
	        pointsMapper.SetResolveCoincidentTopologyToPolygonOffset();
	        pointsMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);

	        actor = new vtkActor();
	        actor.SetMapper(pointsMapper);
	        actor.GetProperty().SetColor(0.0, 0.0, 1.0);
	        actor.GetProperty().SetPointSize(2.0);
		}

	}
	
	private ArrayList<vtkActor> nlrPerDayActors = new ArrayList<vtkActor>();

	private HashMap<String, NLRDataPerDay> fileToNlrPerDayMap = new HashMap<String, NLRDataPerDay>();
	private HashMap<vtkActor, String> actorToFileMap = new HashMap<vtkActor, String>();
	
	public void addNlrData(String path) throws IOException
	{
		if (fileToNlrPerDayMap.containsKey(path))
			return;

		NLRDataPerDay image = new NLRDataPerDay(path);

		fileToNlrPerDayMap.put(path, image);
		
		actorToFileMap.put(image.actor, path);
		
		nlrPerDayActors.add(image.actor);
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeNlrData(String path)
	{
		vtkActor actor = fileToNlrPerDayMap.get(path).actor;
		
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
