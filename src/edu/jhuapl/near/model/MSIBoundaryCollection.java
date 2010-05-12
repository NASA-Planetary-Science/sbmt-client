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
        private vtkPolyData boundary;
        private vtkPolyDataMapper boundaryMapper;
        private double[] spacecraftPosition = new double[3];
        private double[] frustum1 = new double[3];
        private double[] frustum2 = new double[3];
        private double[] frustum3 = new double[3];
        private double[] frustum4 = new double[3];
        private double[] sunPosition = new double[3];

        // The path on the server to the boundary excluding the _RES?.VTK ending.
        private String basePath;
        
		public Boundary(String path) throws IOException
		{
			this.basePath = path;
			
			File lblFile = FileCache.getFileFromServer(path);

			if (lblFile == null)
				throw new IOException("Could not download " + path);
			
	        boundary = new vtkPolyData();
	        boundary.SetPoints(new vtkPoints());
			boundary.SetVerts(new vtkCellArray());

	        boundaryMapper = new vtkPolyDataMapper();
	        actor = new vtkActor();
	    
	        erosModel.addPropertyChangeListener(this);

			String[] startTime = new String[1];
			String[] stopTime = new String[1];

			MSIImage.loadImageInfo(
					lblFile.getAbsolutePath(),
					startTime,
					stopTime,
					spacecraftPosition,
					sunPosition,
					frustum1,
					frustum2,
					frustum3,
					frustum4);

//	        initialize(basePath + "_RES" + erosModel.getModelResolution() + ".VTK");
	        initialize();
		}
		
		private void initialize()
		{
			// Using the frustum, go around the boundary of the frustum and intersect with
			// the asteroid.

			vtkPoints points = boundary.GetPoints();
			vtkCellArray verts = boundary.GetVerts();
			verts.SetNumberOfCells(0);
			points.SetNumberOfPoints(0);
			
			vtkIdList idList = new vtkIdList();
	        idList.SetNumberOfIds(1);
			
			vtksbCellLocator cellLocator = erosModel.getLocator();

			vtkGenericCell cell = new vtkGenericCell();
			
			final int IMAGE_WIDTH = 537;
			final int IMAGE_HEIGHT = 412;

			int count = 0;
			
			double[] corner1 = {
					spacecraftPosition[0] + frustum1[0],
					spacecraftPosition[1] + frustum1[1],
					spacecraftPosition[2] + frustum1[2]
			};
			double[] corner2 = {
					spacecraftPosition[0] + frustum2[0],
					spacecraftPosition[1] + frustum2[1],
					spacecraftPosition[2] + frustum2[2]
			};
			double[] corner3 = {
					spacecraftPosition[0] + frustum3[0],
					spacecraftPosition[1] + frustum3[1],
					spacecraftPosition[2] + frustum3[2]
			};
			double[] vec12 = {
					corner2[0] - corner1[0],
					corner2[1] - corner1[1],
					corner2[2] - corner1[2]
			};
			double[] vec13 = {
					corner3[0] - corner1[0],
					corner3[1] - corner1[1],
					corner3[2] - corner1[2]
			};
			
			//double horizScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
			//double vertScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

			double scdist = GeometryUtil.vnorm(spacecraftPosition);

			for (int i=0; i<IMAGE_HEIGHT; ++i)
			{
				// Compute the vector on the left of the row.
				double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
				double[] left = {
						corner1[0] + fracHeight*vec13[0],
						corner1[1] + fracHeight*vec13[1],
						corner1[2] + fracHeight*vec13[2]
				};

				for (int j=0; j<IMAGE_WIDTH; ++j)
				{
					if (j == 1 && i > 0 && i < IMAGE_HEIGHT-1)
					{
						j = IMAGE_WIDTH-2;
						continue;
					}
					
					double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
					double[] vec = {
							left[0] + fracWidth*vec12[0],
							left[1] + fracWidth*vec12[1],
							left[2] + fracWidth*vec12[2]
					};
					vec[0] -= spacecraftPosition[0];
					vec[1] -= spacecraftPosition[1];
					vec[2] -= spacecraftPosition[2];
					GeometryUtil.unorm(vec, vec);

					double[] lookPt = {
							spacecraftPosition[0] + 2.0*scdist*vec[0],	
							spacecraftPosition[1] + 2.0*scdist*vec[1],	
							spacecraftPosition[2] + 2.0*scdist*vec[2]
					};

					double tol = 1e-6;
					double[] t = new double[1];
					double[] x = new double[3];
					double[] pcoords = new double[3];
					int[] subId = new int[1];
					int[] cellId = new int[1];
					int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);
							
					if (result > 0)
					{
						double[] closestPoint = x;
						//int closestCell = cellId[0];
						//double closestDist = GeometryUtil.distanceBetween(closestPoint, spacecraftPosition);
						
						//double horizPixelScale = closestDist * horizScaleFactor;
						//double vertPixelScale = closestDist * vertScaleFactor;

						points.InsertNextPoint(closestPoint);
			        	idList.SetId(0, count);
			        	verts.InsertNextCell(idList);
			        	
			        	++count;
					}
				}
			}

			
	        
			PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(boundary, erosModel.getErosPolyData(), 0.003);

	        boundaryMapper.SetInput(boundary);

	        actor.SetMapper(boundaryMapper);
	        actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	        actor.GetProperty().SetLineWidth(2.0);
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
			{
				//initialize(basePath + "_RES" + erosModel.getModelResolution() + ".VTK");
				initialize();
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

	private class Boundary_old extends Model implements PropertyChangeListener
	{
		private vtkActor actor;
		private vtkPolyDataReader boundaryReader;
        private vtkPolyData boundary;
        private vtkPolyDataMapper boundaryMapper;
        
        // The path on the server to the boundary excluding the _RES?.VTK ending.
        private String basePath;
        
		public Boundary_old(String path) throws IOException
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
