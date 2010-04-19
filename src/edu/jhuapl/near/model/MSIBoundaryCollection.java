package edu.jhuapl.near.model;

import java.util.*;
import java.io.*;

import nom.tam.fits.FitsException;

import vtk.*;
import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

public class MSIBoundaryCollection extends Model
{
	private class Boundary
	{
		public vtkActor actor;
		
		public Boundary(String path) throws IOException
		{
			File file = FileCache.getFileFromServer(path);

			if (file == null)
				throw new IOException(path + " could not be loaded");
			
			vtkPolyDataReader boundaryReader = new vtkPolyDataReader();
	        boundaryReader.SetFileName(file.getAbsolutePath());
	        boundaryReader.Update();
	        
	        vtkPolyData boundary = new vtkPolyData();
	        boundary.DeepCopy(boundaryReader.GetOutput());
			PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(boundary, erosModel.getErosPolyData(), 0.003);

	        vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
	        boundaryMapper.SetInput(boundary);
	        //boundaryMapper.SetResolveCoincidentTopologyToPolygonOffset();
	        //boundaryMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);

	        actor = new vtkActor();
	        actor.SetMapper(boundaryMapper);
	        actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	        actor.GetProperty().SetLineWidth(2.0);
		}
	}

	/**
	 * return this when boundaries are hidden
	 */
	private ArrayList<vtkProp> dummyActors = new ArrayList<vtkProp>();
	private boolean hidden = false;
	
	private ArrayList<vtkProp> boundaryActors = new ArrayList<vtkProp>();

	private HashMap<String, Boundary> fileToBoundaryMap = new HashMap<String, Boundary>();
	private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
	private ErosModel erosModel;
	
	public MSIBoundaryCollection(ErosModel erosModel)
	{
		this.erosModel = erosModel;
	}
	
	public void addBoundary(String path) throws FitsException, IOException
	{
		if (fileToBoundaryMap.containsKey(path))
			return;

		Boundary image = new Boundary(path);

		fileToBoundaryMap.put(path, image);
		
		actorToFileMap.put(image.actor, path);
		
		boundaryActors.add(image.actor);
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeBoundary(String path)
	{
		vtkActor actor = fileToBoundaryMap.get(path).actor;
		
		boundaryActors.remove(actor);

		actorToFileMap.remove(actor);
		
		fileToBoundaryMap.remove(path);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllBoundaries()
	{
		boundaryActors.clear();
		actorToFileMap.clear();
		fileToBoundaryMap.clear();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public ArrayList<vtkProp> getProps() 
	{
		if (!hidden)
			return boundaryActors;
		else
			return dummyActors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	File file = new File(actorToFileMap.get(prop));
    	return "Boundary of MSI image " + file.getName().substring(2, 11);
    }

    public String getBoundaryName(vtkActor actor)
    {
    	return actorToFileMap.get(actor);
    }
    
    public boolean containsBoundary(String file)
    {
    	return fileToBoundaryMap.containsKey(file);
    }
}
