package edu.jhuapl.near.model;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhuapl.near.util.Properties;

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
	private int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
	private int[] redColor = {255, 0, 0, 255}; // RGBA red
	private int[] blueColor = {0, 0, 255, 255}; // RGBA blue
    private ErosModel erosModel;
    private double currentRadius = 0.1;
    
	static public String POINTS = "points";

	
	public static class Point extends StructureModel.Structure
	{
		public String name = "";
		public int id;

		public double[] center;
		public double radius;
		
		public vtkPolyData polyData;
		
		static public String POINT = "point";
		static public String ID = "id";
		
		public Point()
		{
			id = ++maxId;
		}

		public int getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
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
	    	
	    	this.polyData = erosModel.drawDisk(center, radius);
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
	    	return "Point " + id;
	    }

	}

	public PointModel(ErosModel erosModel)
	{
		this.erosModel = erosModel;

		pointActor = new vtkActor();
		pointActor.GetProperty().LightingOff();
		pointActor.GetProperty().SetColor(1.0, 0.0, 1.0);
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
		vtkAppendPolyData append = new vtkAppendPolyData();

		for (Point pt : this.points)
		{
			append.AddInput(pt.polyData);
		}

		append.Update();
		
		if (pointsPolyData == null)
			pointsPolyData = new vtkPolyData();
		
		pointsPolyData.DeepCopy(append.GetOutput());
		

		erosModel.shiftPolyLineInNormalDirection(pointsPolyData, 0.002);

		if (pointsMapper == null)
			pointsMapper = new vtkPolyDataMapper();
		pointsMapper.SetInput(pointsPolyData);
		pointsMapper.Update();

		pointsMapper.ScalarVisibilityOff();
		pointsMapper.SetScalarModeToDefault();
		
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
//    		Point lin = this.points.get(cellId);
//    		if (lin != null)
//    			return lin.getClickStatusBarText();
//    		else
    			return "";
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
		this.currentRadius = radius;
	}
}
