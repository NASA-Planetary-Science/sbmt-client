package edu.jhuapl.near.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.Spice;

import vtk.*;

/**
 * Model of regular polygon structures drawn on Eros.
 * 
 * @author 
 *
 */

public class RegularPolygonModel extends StructureModel 
{
	private ArrayList<RegularPolygon> polygons = new ArrayList<RegularPolygon>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();

	private vtkPolyData boundaryPolyData;
    private vtkAppendPolyData boundaryAppendFilter;
    private vtkPolyDataMapper boundaryMapper;
    private vtkActor boundaryActor;

	private vtkPolyData interiorPolyData;
    private vtkAppendPolyData interiorAppendFilter;
    private vtkPolyDataMapper interiorMapper;
    private vtkActor interiorActor;

    private vtkPolyData emptyPolyData;
    private ErosModel erosModel;
    private double defaultRadius = 0.25; // radius for new polygons drawn
    private int numberOfSides = 4;
    private double[] boundaryColor = {1.0, 0.0, 1.0};
    private double[] interiorColor = {1.0, 0.0, 1.0};
    private double interiorOpacity = 0.3;
    private String type;
    private boolean saveRadiusToOutput = true;
    
	public static class RegularPolygon extends StructureModel.Structure
	{
		static protected int maxId = 0;
		
		public String name = "default";
		public int id;

		public double[] center;
		public double radius;
		
		public vtkPolyData boundaryPolyData;
		public vtkPolyData interiorPolyData;
		public int numberOfSides;
		public String type;
		
		
		public RegularPolygon(int numberOfSides, String type)
		{
			id = ++maxId;
			boundaryPolyData = new vtkPolyData();
			interiorPolyData = new vtkPolyData();
			this.numberOfSides = numberOfSides;
			this.type = type;
		}

		public int getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getType()
		{
			return type;
		}
		
		public String getInfo()
		{
			return "Diameter = " + 2.0*radius + " km";
		}
		
		public vtkPolyData getBoundaryPolyData()
		{
			return boundaryPolyData;
		}

		public vtkPolyData getInteriorPolyData()
		{
			return interiorPolyData;
		}

		public void updatePolygon(ErosModel erosModel, double[] center, double radius)
	    {
	    	this.center = center;
	    	this.radius = radius;
	    	
	    	erosModel.drawPolygon(center, radius, numberOfSides, interiorPolyData, boundaryPolyData);
	    }
	    
	    public Element toXmlDomElement(Document dom)
	    {
	    	return null;
	    }

	    public void fromXmlDomElement(Element element, ErosModel erosModel)
	    {
	    }

	    public String getClickStatusBarText()
	    {
	    	return type + ", Id = " + id + ", Diameter = " + 2.0*radius + " km";
	    }

	}

	public RegularPolygonModel(
			ErosModel erosModel,
			int numberOfSides,
			boolean saveRadiusToOutput,
			String type)
	{
		this.erosModel = erosModel;

		emptyPolyData = new vtkPolyData();
		
		this.numberOfSides = numberOfSides;
		this.saveRadiusToOutput = saveRadiusToOutput;
		this.type = type;

		boundaryPolyData = new vtkPolyData();
		boundaryAppendFilter = new vtkAppendPolyData();
		boundaryAppendFilter.UserManagedInputsOn();
		boundaryMapper = new vtkPolyDataMapper();
		boundaryMapper.ScalarVisibilityOff();
		boundaryMapper.SetScalarModeToDefault();
		boundaryActor = new vtkActor();
		boundaryActor.GetProperty().LightingOff();
		boundaryActor.GetProperty().SetColor(boundaryColor);
		boundaryActor.GetProperty().SetLineWidth(2.0);

		actors.add(boundaryActor);

		interiorPolyData = new vtkPolyData();
		interiorAppendFilter = new vtkAppendPolyData();
		interiorAppendFilter.UserManagedInputsOn();
		interiorMapper = new vtkPolyDataMapper();
		interiorMapper.ScalarVisibilityOff();
		interiorMapper.SetScalarModeToDefault();
		interiorActor = new vtkActor();
		interiorActor.GetProperty().LightingOff();
		interiorActor.GetProperty().SetColor(interiorColor);
		interiorActor.GetProperty().SetOpacity(interiorOpacity);
		//interiorActor.GetProperty().SetLineWidth(2.0);

		actors.add(interiorActor);
	}
	
	
	public double[] getBoundaryColor()
	{
		return boundaryColor;
	}


	public void setBoundaryColor(double[] color)
	{
		this.boundaryColor = color;
		boundaryActor.GetProperty().SetColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double[] getInteriorColor()
	{
		return interiorColor;
	}

	public void setInteriorColor(double[] color)
	{
		this.interiorColor = color;
		interiorActor.GetProperty().SetColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getInteriorOpacity()
	{
		return interiorOpacity;
	}
	
	public void setInteriorOpacity(double opacity)
	{
		this.interiorOpacity = opacity;
		boundaryActor.GetProperty().SetOpacity(opacity);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}




	public Element toXmlDomElement(Document dom)
    {
    	return null;
    }
    
    public void fromXmlDomElement(Element element)
    {
    }
    
	private void updatePolyData()
	{
		if (polygons.size() > 0)
		{
			boundaryAppendFilter.SetNumberOfInputs(polygons.size());
			interiorAppendFilter.SetNumberOfInputs(polygons.size());
			
			for (int i=0; i<polygons.size(); ++i)
			{
				vtkPolyData poly = polygons.get(i).boundaryPolyData;
				if (poly != null)
					boundaryAppendFilter.SetInputByNumber(i, poly);
				poly = polygons.get(i).interiorPolyData;
				if (poly != null)
					interiorAppendFilter.SetInputByNumber(i, poly);
			}

			boundaryAppendFilter.Update();
			interiorAppendFilter.Update();

			boundaryPolyData.DeepCopy(boundaryAppendFilter.GetOutput());
			interiorPolyData.DeepCopy(interiorAppendFilter.GetOutput());

			erosModel.shiftPolyLineInNormalDirection(boundaryPolyData, 0.003);
			erosModel.shiftPolyLineInNormalDirection(interiorPolyData, 0.002);
		}
		else
		{
			boundaryPolyData.DeepCopy(emptyPolyData);
			interiorPolyData.DeepCopy(emptyPolyData);
		}


		boundaryMapper.SetInput(boundaryPolyData);
		boundaryMapper.Update();
		interiorMapper.SetInput(interiorPolyData);
		interiorMapper.Update();
		
		boundaryActor.SetMapper(boundaryMapper);
        boundaryActor.Modified();
		interiorActor.SetMapper(interiorMapper);
        interiorActor.Modified();
	}
	
	public ArrayList<vtkProp> getProps() 
	{
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	if (prop == boundaryActor || prop == interiorActor)
    	{
        	int polygonId = this.getPolygonIdFromCellId(cellId, prop == interiorActor);
        	RegularPolygon pol = polygons.get(polygonId);
        	return pol.getClickStatusBarText();
    	}
    	else
    	{
    		return "";
    	}
    }

    public int getNumberOfStructures()
    {
    	return polygons.size();
    }
    
    public Structure getStructure(int polygonId)
    {
    	return polygons.get(polygonId);
    }    
    
    public vtkActor getBoundaryActor()
    {
    	return boundaryActor;
    }
    
    public vtkActor getInteriorActor()
    {
    	return interiorActor;
    }
    
    public void addNewStructure()
    {
    	// do nothing
    }
    
    public void addNewStructure(double[] pos)
    {
        RegularPolygon pol = new RegularPolygon(numberOfSides, type);
        polygons.add(pol);

        pol.updatePolygon(erosModel, pos, defaultRadius);
        updatePolyData();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public void removeStructure(int polygonId)
    {
    	polygons.remove(polygonId);

        updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
	
    public void movePolygon(int polygonId, double[] newCenter)
    {
    	RegularPolygon pol = polygons.get(polygonId);
        pol.updatePolygon(erosModel, newCenter, pol.radius);
        updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void changeRadiusOfPolygon(int polygonId, double[] newPointOnPerimeter)
    {
    	RegularPolygon pol = polygons.get(polygonId);
    	double newRadius = Math.sqrt(
    			(pol.center[0]-newPointOnPerimeter[0])*(pol.center[0]-newPointOnPerimeter[0]) +
    			(pol.center[1]-newPointOnPerimeter[1])*(pol.center[1]-newPointOnPerimeter[1]) +
    			(pol.center[2]-newPointOnPerimeter[2])*(pol.center[2]-newPointOnPerimeter[2]));
    	if (newRadius > 5.0)
    		newRadius = 5.0;
    	
        pol.updatePolygon(erosModel, pol.center, newRadius);
        updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

	public void changeRadiusOfAllPolygons(double newRadius)
	{
		for (RegularPolygon pol : this.polygons)
		{
	        pol.updatePolygon(erosModel, pol.center, newRadius);
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void selectStructure(int idx)
	{
		// Do nothing. RegularPolygonModel does not support selection.
	}
	
	/** 
	 * A picker picking the actor of this model will return a 
	 * cellId. But since there are many cells per RegularPolygon, we need to be
	 * able to figure out which RegularPolygon was picked
	 */
	public int getPolygonIdFromCellId(int cellId, boolean interior)
	{
		int numberCellsSoFar = 0;
		for (int i=0; i<polygons.size(); ++i)
		{
			if (interior)
				numberCellsSoFar += polygons.get(i).interiorPolyData.GetNumberOfCells();
			else
				numberCellsSoFar += polygons.get(i).boundaryPolyData.GetNumberOfCells();
			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	public void loadModel(File file) throws IOException
	{
		ArrayList<String> words = FileUtil.getFileWordsAsStringList(file.getAbsolutePath(), "\t");
		
		ArrayList<RegularPolygon> newPolygons = new ArrayList<RegularPolygon>();
		for (int i=0; i<words.size();)
		{
			RegularPolygon pol = new RegularPolygon(numberOfSides, type);
			pol.center = new double[3];
			
			pol.id = Integer.parseInt(words.get(i++));
			pol.name = words.get(i++);
			pol.center[0] = Double.parseDouble(words.get(i++));
			pol.center[1] = Double.parseDouble(words.get(i++));
			pol.center[2] = Double.parseDouble(words.get(i++));
			
			// Note the next 3 words in the line (the point in spherical coordinates) are not used
			++i;
			++i;
			++i;

			if (saveRadiusToOutput)
				pol.radius = Double.parseDouble(words.get(i++)) / 2.0; // read in diameter not radius
			else
				pol.radius = defaultRadius;
			
	    	if (pol.id > RegularPolygon.maxId)
	    		RegularPolygon.maxId = pol.id;
	    	
	    	pol.updatePolygon(erosModel, pol.center, pol.radius);
	        newPolygons.add(pol);
		}
		
		// Only if we reach here and no exception is thrown do we modify this class
		polygons = newPolygons;

		updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void saveModel(File file) throws IOException
	{
		FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

		for (RegularPolygon pol : polygons)
		{
			String name = pol.name;
			if (name.length() == 0)
				name = "default";

			LatLon llr = Spice.reclat(pol.center);
			double lat = llr.lat*180.0/Math.PI;
			double lon = llr.lon*180.0/Math.PI;
			if (lon < 0.0)
				lon += 360.0;
			
			String str = 
				pol.id + "\t" + 
				name + "\t" + 
				pol.center[0] + "\t" + 
				pol.center[1] + "\t" + 
				pol.center[2] + "\t" +
				lat + "\t" +
				lon + "\t" +
				llr.rad;
			
			if (saveRadiusToOutput)
				str += "\t" + 2.0*pol.radius; // save out as diameter, not radius

			str += "\n";

			out.write(str);
		}
		
		out.close();
	}

	public int getSelectedStructureIndex()
	{
		return -1;
	}

	public boolean supportsSelection()
	{
		return false;
	}
	
	public double getDefaultRadius()
	{
		return defaultRadius;
	}
	
	public void setDefaultRadius(double radius)
	{
		this.defaultRadius = radius;
	}

}
