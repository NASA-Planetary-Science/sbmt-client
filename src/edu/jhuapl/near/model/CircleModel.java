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
 * Model of circle structures drawn on Eros.
 * 
 * @author 
 *
 */

public class CircleModel extends StructureModel 
{
	private ArrayList<Circle> circles = new ArrayList<Circle>();
	private vtkPolyData circlesPolyData;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper circlesMapper;
    private vtkActor circlesActor;
//	private int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
//	private int[] redColor = {255, 0, 0, 255}; // RGBA red
//	private int[] blueColor = {0, 0, 255, 255}; // RGBA blue
    private ErosModel erosModel;
    private double defaultRadius = 0.25; // radius for new circles drawn
    private vtkAppendPolyData appendFilter;
    private vtkPolyData emptyPolyData;

	static public String CIRCLES = "circles";

	
	public static class Circle extends StructureModel.Structure
	{
		static protected int maxId = 0;
		
		public String name = "default";
		public int id;

		public double[] center;
		public double radius;
		
		public vtkPolyData polyData;
		
		static public String CIRCLE = "circle";
		static public String ID = "id";
		
		public Circle()
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
			return CIRCLE;
		}
		
		public String getInfo()
		{
			return "Diameter = " + 2.0*radius + " km";
		}

	    public void updateCircle(ErosModel erosModel, double[] center, double radius)
	    {
	    	this.center = center;
	    	this.radius = radius;
	    	
	    	this.polyData.DeepCopy(erosModel.drawPolygon(center, radius, 20, false));
	    }
	    
	    public Element toXmlDomElement(Document dom)
	    {
	    	Element linEle = dom.createElement(CIRCLE);
	    	return linEle;
	    }

	    public void fromXmlDomElement(Element element, ErosModel erosModel)
	    {
	    }

	    public String getClickStatusBarText()
	    {
	    	return "Circle, Id = " + id + ", Diameter = " + 2.0*radius + " km";
	    }

	}

	public CircleModel(ErosModel erosModel)
	{
		this.erosModel = erosModel;

		circlesActor = new vtkActor();
		circlesActor.GetProperty().LightingOff();
		circlesActor.GetProperty().SetColor(1.0, 0.0, 1.0);
		circlesActor.GetProperty().SetLineWidth(2.0);

		emptyPolyData = new vtkPolyData();
	}

    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(CIRCLES);

		for (Circle lin : this.circles)
		{
			rootEle.appendChild(lin.toXmlDomElement(dom));
		}

		return rootEle;
    }
    
    public void fromXmlDomElement(Element element)
    {
    	this.circles.clear();
    	
		NodeList nl = element.getElementsByTagName(Circle.CIRCLE);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Circle lin = new Circle();
				
				lin.fromXmlDomElement(el, erosModel);

				this.circles.add(lin);
			}
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
	private void updatePolyData()
	{
		if (circlesPolyData == null)
			circlesPolyData = new vtkPolyData();
		
		if (circles.size() > 0)
		{
			//vtkAppendPolyData appendFilter = new vtkAppendPolyData();
			if (appendFilter == null)
			{
				appendFilter = new vtkAppendPolyData();
				appendFilter.UserManagedInputsOn();
			}
			
			appendFilter.SetNumberOfInputs(circles.size());
			
			//for (Circle cir : this.circles)
			for (int i=0; i<circles.size(); ++i)
			{
				vtkPolyData poly = circles.get(i).polyData;
				if (poly != null)
					appendFilter.SetInputByNumber(i, poly);
			}

			appendFilter.Update();

			circlesPolyData.DeepCopy(appendFilter.GetOutput());

			erosModel.shiftPolyLineInNormalDirection(circlesPolyData, 0.002);
		}
		else
		{
			circlesPolyData.DeepCopy(emptyPolyData);
		}


		if (circlesMapper == null)
			circlesMapper = new vtkPolyDataMapper();
		circlesMapper.SetInput(circlesPolyData);
		circlesMapper.ScalarVisibilityOff();
		circlesMapper.SetScalarModeToDefault();
		circlesMapper.Update();

		
        if (!actors.contains(circlesActor))
        	actors.add(circlesActor);

        circlesActor.SetMapper(circlesMapper);
        circlesActor.Modified();
	}
	
	public ArrayList<vtkProp> getProps() 
	{
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	if (prop == circlesActor)
    	{
        	int circleId = this.getCircleIdFromCellId(cellId);
        	Circle cir = circles.get(circleId);
        	return cir.getClickStatusBarText();
    	}
    	else
    	{
    		return "";
    	}
    }

    public int getNumberOfStructures()
    {
    	return circles.size();
    }
    
    public Structure getStructure(int circleId)
    {
    	return circles.get(circleId);
    }    
    
    public vtkActor getCircleActor()
    {
    	return circlesActor;
    }
    
    public void addNewStructure()
    {
    	// do nothing
    }
    
    public void addNewStructure(double[] pos)
    {
        Circle cir = new Circle();
        circles.add(cir);

        cir.updateCircle(erosModel, pos, defaultRadius);
        updatePolyData();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public void removeStructure(int circleId)
    {
    	circles.remove(circleId);

        updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
	
    public void moveCircle(int circleId, double[] newCenter)
    {
    	Circle cir = circles.get(circleId);
        cir.updateCircle(erosModel, newCenter, cir.radius);
        updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void changeRadiusOfCircle(int circleId, double[] newPointOnPerimeter)
    {
    	Circle cir = circles.get(circleId);
    	double newRadius = Math.sqrt(
    			(cir.center[0]-newPointOnPerimeter[0])*(cir.center[0]-newPointOnPerimeter[0]) +
    			(cir.center[1]-newPointOnPerimeter[1])*(cir.center[1]-newPointOnPerimeter[1]) +
    			(cir.center[2]-newPointOnPerimeter[2])*(cir.center[2]-newPointOnPerimeter[2]));
    	if (newRadius > 5.0)
    		newRadius = 5.0;
    	
        cir.updateCircle(erosModel, cir.center, newRadius);
        updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

	public void selectStructure(int idx)
	{
		// Do nothing. CircleModel does not support selection.
	}
	
	/** 
	 * A picker picking the actor of this model will return a 
	 * cellId. But since there are many cells per Circle, we need to be
	 * able to figure out which Circle was picked
	 */
	public int getCircleIdFromCellId(int cellId)
	{
		int numberCellsSoFar = 0;
		for (int i=0; i<circles.size(); ++i)
		{
			numberCellsSoFar += circles.get(i).polyData.GetNumberOfCells();
			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	public void loadModel(File file) throws IOException
	{
		ArrayList<String> words = FileUtil.getFileWordsAsStringList(file.getAbsolutePath(), "\t");
		
		ArrayList<Circle> newCircles = new ArrayList<Circle>();
		for (int i=0; i<words.size();)
		{
			Circle cir = new Circle();
			cir.center = new double[3];
			
			cir.id = Integer.parseInt(words.get(i++));
			cir.name = words.get(i++);
			cir.center[0] = Double.parseDouble(words.get(i++));
			cir.center[1] = Double.parseDouble(words.get(i++));
			cir.center[2] = Double.parseDouble(words.get(i++));
			
			// Note the next 3 words in the line (the point in spherical coordinates) are not used
			++i;
			++i;
			++i;

			cir.radius = Double.parseDouble(words.get(i++)) / 2.0; // read in diameter not radius
			
	    	if (cir.id > Circle.maxId)
	    		Circle.maxId = cir.id;
	    	
	    	cir.updateCircle(erosModel, cir.center, cir.radius);
	        newCircles.add(cir);
		}
		
		// Only if we reach here and no exception is thrown do we modify this class
		circles = newCircles;

		updatePolyData();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void saveModel(File file) throws IOException
	{
		FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

		for (Circle cir : circles)
		{
			String name = cir.name;
			if (name.length() == 0)
				name = "default";

			LatLon llr = Spice.reclat(cir.center);
			double lat = llr.lat*180.0/Math.PI;
			double lon = llr.lon*180.0/Math.PI;
			if (lon < 0.0)
				lon += 360.0;
			
			String str = 
				cir.id + "\t" + 
				name + "\t" + 
				cir.center[0] + "\t" + 
				cir.center[1] + "\t" + 
				cir.center[2] + "\t" +
				lat + "\t" +
				lon + "\t" +
				llr.rad + "\t" +
				2.0*cir.radius + "\n"; // save out as diameter, not radius

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


/*
public class CircleModel extends StructureModel 
{
	private ArrayList<Circle> circles = new ArrayList<Circle>();
	private vtkPolyData circlesPolyData;
    private ArrayList<vtkProp> circlesActors = new ArrayList<vtkProp>();
    //private vtkActor structuresActor;
	private int[] defaultColor = {255, 0, 255, 255}; // RGBA, default to purple
	static public String CIRCLES = "cicles";


	public static class Circle extends StructureModel.Structure
	{
		public int cellId;
		public ArrayList<Integer> pointIds = new ArrayList<Integer>();

		public String name = "";
		public int id;
		public double centerLat;
		public double centerLon;
		public double centerRad;
		public double radius;
		public double[] normal = new double[3];
		//public ArrayList<Double> x = new ArrayList<Double>();
		//public ArrayList<Double> y = new ArrayList<Double>();
		//public ArrayList<Double> z = new ArrayList<Double>();
		//public BoundingBox bb = new BoundingBox();

		static public String CIRCLE = "circle";
		static public String ID = "id";
		static public String MSI_IMAGE = "msi-image";
		static public String CENTER = "center";
		static public String NORMAL = "normal";
		static public String RADIUS = "radius";

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
			return "Circle";
		}
		
		public String getInfo()
		{
			return CIRCLE;
		}
		
	    public Element toXmlDomElement(Document dom)
	    {
	    	Element circleEle = dom.createElement(CIRCLE);
	    	circleEle.setAttribute(ID, String.valueOf(id));
	    	circleEle.setAttribute(MSI_IMAGE, String.valueOf(name));
	    	circleEle.setAttribute(RADIUS, String.valueOf(radius));
	    	circleEle.setAttribute(CENTER, centerLat + " " + centerLon + " " + centerRad);
	    	circleEle.setAttribute(NORMAL, normal[0] + " " + normal[1] + " " + normal[2]);

	    	return circleEle;
	    }
	    
	    public void fromXmlDomElement(Element element, ErosModel erosModel)
	    {
	    	id = Integer.parseInt(element.getAttribute(ID));
	    	name = element.getAttribute(MSI_IMAGE);
	    	radius = Double.parseDouble(element.getAttribute(RADIUS));

	    	String tmp = element.getAttribute(CENTER);
	    	String[] tokens = tmp.split(" ");

	    	centerLat = Double.parseDouble(tokens[0]);
	    	centerLon = Double.parseDouble(tokens[1]);
	    	centerRad = Double.parseDouble(tokens[2]);

	    	tmp = element.getAttribute(NORMAL);
	    	tokens = tmp.split(" ");

	    	normal[0] = Double.parseDouble(tokens[0]);
	    	normal[1] = Double.parseDouble(tokens[1]);
	    	normal[2] = Double.parseDouble(tokens[2]);
	    }

	    public String getClickStatusBarText()
	    {
	    	return "Circle " + id + ", " + name;
	    }
	}

	
    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(CIRCLES);

		for (Circle cir : this.circles)
		{
			rootEle.appendChild(cir.toXmlDomElement(dom));
		}

		return rootEle;
    }
    
    public void fromXmlDomElement(Element element)
    {
    	this.circles.clear();
    	
		NodeList nl = element.getElementsByTagName(Circle.CIRCLE);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Circle cir = new Circle();
				
				cir.fromXmlDomElement(el, null);

				this.circles.add(cir);
			}
		}

		createPolyData();

		vtkPolyDataMapper circleMapper = new vtkPolyDataMapper();
		circleMapper.SetInput(circlesPolyData);
		//circleMapper.SetResolveCoincidentTopologyToPolygonOffset();
		//circleMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);

		vtkActor circleActor = new vtkActor();
		circleActor.SetMapper(circleMapper);

		circlesActors.clear();
		circlesActors.add(circleActor);

		System.out.println("Number of circles: " + this.circles.size());
    }

	private void createPolyData()
	{
		circlesPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        
        colors.SetNumberOfComponents(4);
        
        vtkIdList idList = new vtkIdList();

//        int c=0;
        int cellId = 0;
		for (Circle lin : circles)
		{
			lin.cellId = cellId;

//            int size = lin.lat.size();
//            idList.SetNumberOfIds(size);
//            
//            for (int i=0;i<size;++i)
//            {
//            	double lat = lin.lat.get(i);
//            	double lon = lin.lon.get(i);
//            	double rad = lin.rad.get(i);
//                double x = rad * Math.cos( lon ) * Math.cos( lat );
//                double y = rad * Math.sin( lon ) * Math.cos( lat );
//                double z = rad * Math.sin( lat );
//
//                points.InsertNextPoint(x, y, z);
//            	idList.SetId(i, c);
//            	++c;
//            }

            lines.InsertNextCell(idList);
        	colors.InsertNextTuple4(defaultColor[0],defaultColor[1],defaultColor[2],defaultColor[3]);
            
            ++cellId;
		}
		
        circlesPolyData.SetPoints(points);
        circlesPolyData.SetLines(lines);
        circlesPolyData.GetCellData().SetScalars(colors);
	}
	
	public StructureModel.Structure getStructure(int idx)
	{
		return this.circles.get(idx);
	}
		
		
	public ArrayList<vtkProp> getProps() 
	{
		return circlesActors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	StructureModel.Structure struc = getStructure(cellId);
		if (struc != null)
			return struc.getClickStatusBarText();
		else
			return "";
    }

//    public void addNewCircle(double[] center, double radius)
//    {
//    	circlesPolyData.Modified();
//		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
//    }
//    
//    public void updateCircleCenter(int cellId, double[] newCenter)
//    {
//    	circlesPolyData.Modified();
//		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
//    }
//    
//    public void updateCircelRadius(int cellId, double newRadius)
//    {
//    	circlesPolyData.Modified();
//		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
//    }

    public void removeStructure(int cellId)
    {
    	circlesPolyData.Modified();
		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
    }

	public void addNewStructure()
	{
		
	}

	public int getNumberOfStructures()
	{
		return 0;
	}

	public void selectStructure(int idx)
	{
		
	}

}
*/