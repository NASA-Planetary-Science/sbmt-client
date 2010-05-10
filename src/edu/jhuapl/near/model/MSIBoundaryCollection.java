package edu.jhuapl.near.model;

import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;

import nom.tam.fits.FitsException;

import vtk.*;
import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

public class MSIBoundaryCollection extends Model implements PropertyChangeListener
{
	private class Boundary extends Model implements PropertyChangeListener
	{
		private vtkActor actor;
		private vtkPolyDataReader boundaryReader;
        private vtkPolyData boundary;
        private vtkPolyDataMapper boundaryMapper;
        
        // The path on the server to the boundary excluding the _RES?.VTK ending.
        private String basePath;
        
		public Boundary(String path) throws IOException
		{
			this.basePath = path;
			
			boundaryReader = new vtkPolyDataReader();
	        boundary = new vtkPolyData();
	        boundaryMapper = new vtkPolyDataMapper();
	        actor = new vtkActor();
	    
	        erosModel.addPropertyChangeListener(this);
	        
//	        initialize(basePath + "_RES" + erosModel.getModelResolution() + ".VTK");
	        initialize(basePath + ".VTK");
		}
		
		private void initialize(String path)
		{
			File file = FileCache.getFileFromServer(path);

			if (file == null)
			{
				System.err.println(path + " could not be loaded");
				(new Exception()).printStackTrace();
			}
				
	        boundaryReader.SetFileName(file.getAbsolutePath());
	        boundaryReader.Update();
	        
	        boundary.DeepCopy(boundaryReader.GetOutput());
			PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(boundary, erosModel.getErosPolyData(), 0.003);

	        boundaryMapper.SetInput(boundary);
	        //boundaryMapper.SetResolveCoincidentTopologyToPolygonOffset();
	        //boundaryMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, -1.0);

	        actor.SetMapper(boundaryMapper);
	        actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	        actor.GetProperty().SetLineWidth(2.0);
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
			{
				//initialize(basePath + "_RES" + erosModel.getModelResolution() + ".VTK");
				initialize(basePath + ".VTK");
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			}
		}

		@Override
		public ArrayList<vtkProp> getProps()
		{
			ArrayList<vtkProp> props = new ArrayList<vtkProp>();
			props.add(actor);
			return props;
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

		image.addPropertyChangeListener(this);
		
		fileToBoundaryMap.put(path, image);
		
		actorToFileMap.put(image.getProps().get(0), path);
		
		boundaryActors.add(image.getProps().get(0));
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeBoundary(String path)
	{
		vtkActor actor = (vtkActor)fileToBoundaryMap.get(path).getProps().get(0);
		
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

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
}
