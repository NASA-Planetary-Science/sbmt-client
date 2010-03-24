package edu.jhuapl.near.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.Spice;

import vtk.*;

/**
 * Model of line structures drawn on Eros.
 * 
 * @author 
 *
 */
public class PointModel extends StructureModel 
{
	private ArrayList<Point> points = new ArrayList<Point>();
	private vtkPolyData pointsPolyData;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper pointsMapper;
    private vtkActor pointActor;
//	private int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
//	private int[] redColor = {255, 0, 0, 255}; // RGBA red
//	private int[] blueColor = {0, 0, 255, 255}; // RGBA blue
    private ErosModel erosModel;
    private double currentRadius = 0.25;
    private vtkAppendPolyData appendFilter;
    private vtkPolyData emptyPolyData;
    
	static public String POINTS = "points";

	
	public static class Point extends StructureModel.Structure
	{
		static protected int maxId = 0;
		
		public String name = "default";
		public int id;

		public double[] center;
		public double radius;
		
		public vtkPolyData polyData;
		
		static public String POINT = "point";
		static public String ID = "id";
		
		public Point()
		{
			id = ++maxId;
			polyData = new vtkPolyData();
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
			return POINT;
		}
		
		public String getInfo()
		{
			return "";
		}

	    public void updateCircle(ErosModel erosModel, double[] center, double radius)
	    {
	    	this.center = center;
	    	this.radius = radius;
	    	
	    	erosModel.drawPolygon(center, radius, 4, polyData, null);
	    }
	    
	    public Element toXmlDomElement(Document dom)
	    {
	    	Element linEle = dom.createElement(POINT);
	    	return linEle;
	    }

	    public void fromXmlDomElement(Element element, ErosModel erosModel)
	    {
	    }

	    public String getClickStatusBarText()
	    {
	    	//return "Point, Id = " + id + ", Diameter = " + 2.0*radius + " km";
	    	return "Point, Id = " + id;
	    }

	}

	public PointModel(ErosModel erosModel)
	{
		this.erosModel = erosModel;

		pointActor = new vtkActor();
		pointActor.GetProperty().LightingOff();
		pointActor.GetProperty().SetColor(1.0, 0.0, 1.0);
		pointActor.GetProperty().SetLineWidth(2.0);
		
		emptyPolyData = new vtkPolyData();
	}

    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(POINTS);

		for (Point lin : this.points)
		{
			rootEle.appendChild(lin.toXmlDomElement(dom));
		}

		return rootEle;
    }
    
    public void fromXmlDomElement(Element element)
    {
    	this.points.clear();
    	
		NodeList nl = element.getElementsByTagName(Point.POINT);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Point lin = new Point();
				
				lin.fromXmlDomElement(el, erosModel);

				this.points.add(lin);
			}
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
	private void updatePolyData()
	{
		if (pointsPolyData == null)
			pointsPolyData = new vtkPolyData();
		
		if (points.size() > 0)
		{
			//vtkAppendPolyData appendFilter = new vtkAppendPolyData();
			if (appendFilter == null)
			{
				appendFilter = new vtkAppendPolyData();
				appendFilter.UserManagedInputsOn();
			}
			
			appendFilter.SetNumberOfInputs(points.size());
			
			//for (Circle cir : this.circles)
			for (int i=0; i<points.size(); ++i)
			{
				vtkPolyData poly = points.get(i).polyData;
				if (poly != null)
					appendFilter.SetInputByNumber(i, poly);
			}

			appendFilter.Update();

			pointsPolyData.DeepCopy(appendFilter.GetOutput());

			erosModel.shiftPolyLineInNormalDirection(pointsPolyData, 0.002);
		}
		else
		{
			pointsPolyData.DeepCopy(emptyPolyData);
		}
//		if (pointsPolyData == null)
//			pointsPolyData = new vtkPolyData();
//		
//		if (points.size() > 0)
//		{
//			vtkAppendPolyData append = new vtkAppendPolyData();
//
//			for (Point pt : this.points)
//			{
//				append.AddInput(pt.polyData);
//			}
//
//			append.Update();
//
//			pointsPolyData.DeepCopy(append.GetOutput());
//
//			erosModel.shiftPolyLineInNormalDirection(pointsPolyData, 0.002);
//		}
//		else
//		{
//			pointsPolyData = new vtkPolyData();
//		}


		if (pointsMapper == null)
			pointsMapper = new vtkPolyDataMapper();
		pointsMapper.SetInput(pointsPolyData);
		pointsMapper.ScalarVisibilityOff();
		pointsMapper.SetScalarModeToDefault();
		pointsMapper.Update();

		
        if (!actors.contains(pointActor))
        	actors.add(pointActor);

        pointActor.SetMapper(pointsMapper);
        pointActor.Modified();
	}
	
	public ArrayList<vtkProp> getProps() 
	{
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	if (prop == pointActor)
    	{
        	int pointId = this.getPointIdFromCellId(cellId);
        	Point pt = points.get(pointId);
        	return pt.getClickStatusBarText();
    	}
    	else
    	{
    		return "";
    	}
    }

    public int getNumberOfStructures()
    {
    	return points.size();
    }
    
    public Structure getStructure(int cellId)
    {
    	return points.get(cellId);
    }
    
    
    public vtkActor getPointActor()
    {
    	return pointActor;
    }
    
    public void addNewStructure()
    {
    	// do nothing
    }
    
    public void addNewStructure(double[] pos)
    {
        Point pt = new Point();
        points.add(pt);

        pt.updateCircle(erosModel, pos, currentRadius);
        updatePolyData();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public void removeStructure(int cellId)
    {
    	System.out.println(cellId);
    	points.remove(cellId);

        updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
	
    public void movePoint(int vertexId, double[] newPoint)
    {
    	Point pt = points.get(vertexId);
        pt.updateCircle(erosModel, newPoint, currentRadius);
        updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void changeAllPointSizes(double newSize)
    {
        updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
	public void selectStructure(int idx)
	{
		// Do nothing. PointModel does not support selection.
	}
	
	public void setCurrentRadius(double radius)
	{
		if (radius == this.currentRadius)
			return;
		
		this.currentRadius = radius;
		for (Point pt : points)
		{
			pt.radius = radius;
			pt.updateCircle(erosModel, pt.center, radius);
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public double getCurrentRadius()
	{
		return currentRadius;
	}
	
	/** 
	 * A picker picking the actor of this model will return a 
	 * cellId. But since there are many cells per Point, we need to be
	 * able to figure out which Point was picked
	 */
	public int getPointIdFromCellId(int cellId)
	{
		int numberCellsSoFar = 0;
		for (int i=0; i<points.size(); ++i)
		{
			numberCellsSoFar += points.get(i).polyData.GetNumberOfCells();
			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	public void loadModel(File file) throws IOException
	{
		ArrayList<String> words = FileUtil.getFileWordsAsStringList(file.getAbsolutePath(), "\t");
		
		ArrayList<Point> newPoints = new ArrayList<Point>();
		for (int i=0; i<words.size();)
		{
			Point pt = new Point();
			pt.center = new double[3];
			
			pt.id = Integer.parseInt(words.get(i++));
			pt.name = words.get(i++);
			pt.center[0] = Double.parseDouble(words.get(i++));
			pt.center[1] = Double.parseDouble(words.get(i++));
			pt.center[2] = Double.parseDouble(words.get(i++));
			
			// Note the next 3 words in the line (the point in spherical coordinates) are not used
			++i;
			++i;
			++i;

	    	if (pt.id > Point.maxId)
	    		Point.maxId = pt.id;
	    	
	        pt.updateCircle(erosModel, pt.center, currentRadius);
			newPoints.add(pt);
		}

		// Only if we reach here and no exception is thrown do we modify this class
		points = newPoints;

		updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void saveModel(File file) throws IOException
	{
		FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

		for (Point pt : points)
		{
			String name = pt.name;
			if (name.length() == 0)
				name = "default";
	
			LatLon llr = Spice.reclat(pt.center);
			double lat = llr.lat*180.0/Math.PI;
			double lon = llr.lon*180.0/Math.PI;
			if (lon < 0.0)
				lon += 360.0;

			String str = 
				pt.id + "\t" + 
				name + "\t" + 
				pt.center[0] + "\t" + 
				pt.center[1] + "\t" + 
				pt.center[2] + "\t" +
				lat + "\t" +
				lon + "\t" +
				llr.rad + "\n";

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
