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
	private vtkPolyData polygonsPolyData;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper polygonsMapper;
    private vtkActor polygonsActor;
    private ErosModel erosModel;
    private double defaultRadius = 0.25; // radius for new polygons drawn
    private vtkAppendPolyData appendFilter;
    private vtkPolyData emptyPolyData;
    private int numberOfSides = 4;
    private double[] color = {1.0, 0.0, 1.0};
    private boolean filled = false;
    //private int[] fillColor = 
    private String type;
	
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
		public boolean filled;
		public String type;
		
		
		public RegularPolygon(int numberOfSides, boolean filled, String type)
		{
			id = ++maxId;
			boundaryPolyData = new vtkPolyData();
			interiorPolyData = new vtkPolyData();
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
			boolean filled,
			String type)
	{
		this.erosModel = erosModel;

		polygonsActor = new vtkActor();
		polygonsActor.GetProperty().LightingOff();
		polygonsActor.GetProperty().SetColor(color);
		polygonsActor.GetProperty().SetLineWidth(2.0);

		emptyPolyData = new vtkPolyData();
		
		this.numberOfSides = numberOfSides;
		this.filled = filled;
		this.type = type;
	}
	

	public double[] getColor()
	{
		return color;
	}



	public void setColor(double[] color)
	{
		this.color = color;
		polygonsActor.GetProperty().SetColor(color);
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
		if (polygonsPolyData == null)
			polygonsPolyData = new vtkPolyData();
		
		if (polygons.size() > 0)
		{
			//vtkAppendPolyData appendFilter = new vtkAppendPolyData();
			if (appendFilter == null)
			{
				appendFilter = new vtkAppendPolyData();
				appendFilter.UserManagedInputsOn();
			}
			
			appendFilter.SetNumberOfInputs(polygons.size());
			
			for (int i=0; i<polygons.size(); ++i)
			{
				vtkPolyData poly = polygons.get(i).boundaryPolyData;
				if (poly != null)
					appendFilter.SetInputByNumber(i, poly);
			}

			appendFilter.Update();

			polygonsPolyData.DeepCopy(appendFilter.GetOutput());

			erosModel.shiftPolyLineInNormalDirection(polygonsPolyData, 0.002);
		}
		else
		{
			polygonsPolyData.DeepCopy(emptyPolyData);
		}


		if (polygonsMapper == null)
			polygonsMapper = new vtkPolyDataMapper();
		polygonsMapper.SetInput(polygonsPolyData);
		polygonsMapper.ScalarVisibilityOff();
		polygonsMapper.SetScalarModeToDefault();
		polygonsMapper.Update();

		
        if (!actors.contains(polygonsActor))
        	actors.add(polygonsActor);

        polygonsActor.SetMapper(polygonsMapper);
        polygonsActor.Modified();
	}
	
	public ArrayList<vtkProp> getProps() 
	{
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	if (prop == polygonsActor)
    	{
        	int polygonId = this.getPolygonIdFromCellId(cellId);
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
    
    public vtkActor getPolygonActor()
    {
    	return polygonsActor;
    }
    
    public void addNewStructure()
    {
    	// do nothing
    }
    
    public void addNewStructure(double[] pos)
    {
        RegularPolygon pol = new RegularPolygon(numberOfSides, filled, type);
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

	public void selectStructure(int idx)
	{
		// Do nothing. RegularPolygonModel does not support selection.
	}
	
	/** 
	 * A picker picking the actor of this model will return a 
	 * cellId. But since there are many cells per RegularPolygon, we need to be
	 * able to figure out which RegularPolygon was picked
	 */
	public int getPolygonIdFromCellId(int cellId)
	{
		int numberCellsSoFar = 0;
		for (int i=0; i<polygons.size(); ++i)
		{
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
			RegularPolygon pol = new RegularPolygon(numberOfSides, filled, type);
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

			pol.radius = Double.parseDouble(words.get(i++)) / 2.0; // read in diameter not radius
			
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
				llr.rad + "\t" +
				2.0*pol.radius + "\n"; // save out as diameter, not radius

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
}
