package edu.jhuapl.near.model;

import java.util.*;
import java.io.*;

import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

public class MSIBoundaryCollection extends Model
{
	private static class Boundary
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

	        vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
	        boundaryMapper.SetInput(boundaryReader.GetOutput());
	        boundaryMapper.SetResolveCoincidentTopologyToPolygonOffset();
	        boundaryMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);

	        actor = new vtkActor();
	        actor.SetMapper(boundaryMapper);
	        actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	        actor.GetProperty().SetLineWidth(2.0);
		}
	}

	/**
	 * return this when boundaries are hidden
	 */
	private ArrayList<vtkActor> dummyActors = new ArrayList<vtkActor>();
	private boolean hidden = false;
	
	private ArrayList<vtkActor> boundaryActors = new ArrayList<vtkActor>();

	//private HashMap<Boundary, vtkActor> boundaryActors = new HashMap<Boundary, vtkActor>();

	private HashMap<String, Boundary> fileToBoundaryMap = new HashMap<String, Boundary>();
	
	//private ArrayList<Boundary> boundaries = new ArrayList<Boundary>();

	/*
	public void setBoundaries(ArrayList<String> images)
	{
		boundaries.clear();
		boundaryActors.clear();
		
		// For each file, download the file from the internet and display it
		for (String s : images)
		{
			// Download this file to a temporary location.
			Boundary bound = new Boundary(s);
			boundaries.add(bound);
			boundaryActors.add(bound.actor);
		}

		this.pcs.firePropertyChange(Properties.BOUNDARIES_CHANGED, null, null);
	}
*/

	public void addBoundary(String path) throws FitsException, IOException
	{
		Boundary image = new Boundary(path);

		fileToBoundaryMap.put(path, image);
		
		boundaryActors.add(image.actor);
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeBoundary(String path)
	{
		vtkActor actor = fileToBoundaryMap.get(path).actor;
		
		boundaryActors.remove(actor);
		
		fileToBoundaryMap.remove(path);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllBoundaries()
	{
		boundaryActors.clear();
		fileToBoundaryMap.clear();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		if (!hidden)
			return boundaryActors;
		else
			return dummyActors;
	}
}
