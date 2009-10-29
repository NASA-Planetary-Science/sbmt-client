package edu.jhuapl.near.model;

import java.util.*;
import java.io.*;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

public class MSIBoundaryCollection extends Model
{
    private ArrayList<vtkActor> boundaryActors = new ArrayList<vtkActor>();

	private static class Boundary
	{
		public vtkActor actor;
		
		public Boundary(String path)
		{
			File file = FileCache.getFileFromServer(path);
			
	    	vtkPolyDataReader boundaryReader = new vtkPolyDataReader();
	        boundaryReader.SetFileName(file.getAbsolutePath());
	        boundaryReader.Update();

	        vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
	        boundaryMapper.SetInput(boundaryReader.GetOutput());

	        actor = new vtkActor();
	        actor.SetMapper(boundaryMapper);
		}
	}
	
	private ArrayList<Boundary> boundaries = new ArrayList<Boundary>();
	
	public void setBoundaries(ArrayList<String> images)
	{
		// For each file, download the file from the internet and display it
		for (String s : images)
		{
			// Download this file to a temporary location.
			Boundary bound = new Boundary(s);
		}

		this.pcs.firePropertyChange(Properties.BOUNDARIES_CHANGED, null, null);
	}

	public ArrayList<vtkActor> getActors() 
	{
		return boundaryActors;
	}
}
