package edu.jhuapl.near.model;

import java.io.File;
import java.util.ArrayList;

import edu.jhuapl.near.util.FileCache;

import vtk.vtkActor;

public class NLRData extends Model 
{
	//private static void NLRPoint
	
	public void addNLRData(String path)
	{
		// Download this file from the internet and display it
		File file = FileCache.getFileFromServer(path);

	}
	
	public ArrayList<vtkActor> getActors() 
	{
		return null;
	}

	//private ArrayList
}
