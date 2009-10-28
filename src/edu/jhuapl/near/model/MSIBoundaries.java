package edu.jhuapl.near.model;

import java.util.*;

import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import edu.jhuapl.near.util.ConvertToRealFile;
import edu.jhuapl.near.util.Properties;

public class MSIBoundaries extends Model
{
	private static class Boundary
	{
		Boundary(String path)
		{
	    	vtkPolyDataReader boundaryReader = new vtkPolyDataReader();
	        boundaryReader.SetFileName(path);
	        boundaryReader.Update();

	        vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
	        boundaryMapper.SetInput(boundaryReader.GetOutput());

	        vtkActor boundaryActor = new vtkActor();
	        boundaryActor.SetMapper(boundaryMapper);

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
}
