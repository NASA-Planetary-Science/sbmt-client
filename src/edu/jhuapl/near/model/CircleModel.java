package edu.jhuapl.near.model;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhuapl.near.util.Properties;

import vtk.*;

/**
 * Model of structures drawn on Eros such as lineaments and circles.
 * 
 * @author 
 *
 */
public class CircleModel extends Model 
{
	//private HashMap<Integer, Circle> idToLineamentMap = new HashMap<Integer, Circle>();
	//private HashMap<Integer, Circle> cellIdToLineamentMap = new HashMap<Integer, Circle>();
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
	    
	    public void fromXmlDomElement(Element element)
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
	    	return "Circle " + id + " mapped on MSI image " + name;
	    }
	}

	
    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(CIRCLES);

    	dom.appendChild(rootEle);

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
				
				cir.fromXmlDomElement(el);

				this.circles.add(cir);
			}
		}

		createPolyData();

		vtkPolyDataMapper lineamentMapper = new vtkPolyDataMapper();
		lineamentMapper.SetInput(circlesPolyData);
		//lineamentMapper.SetResolveCoincidentTopologyToPolygonOffset();
		//lineamentMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);

		vtkActor lineamentActor = new vtkActor();
		lineamentActor.SetMapper(lineamentMapper);

		circlesActors.clear();
		circlesActors.add(lineamentActor);

		System.out.println("Number of lineaments: " + this.circles.size());
    }

	private void createPolyData()
	{
		circlesPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        
        colors.SetNumberOfComponents(4);
        
        vtkIdList idList = new vtkIdList();

        int c=0;
        int cellId = 0;
		for (Circle lin : circles)
		{
			lin.cellId = cellId;
/*			
            int size = lin.lat.size();
            idList.SetNumberOfIds(size);
            
            for (int i=0;i<size;++i)
            {
            	double lat = lin.lat.get(i);
            	double lon = lin.lon.get(i);
            	double rad = lin.rad.get(i);
                double x = rad * Math.cos( lon ) * Math.cos( lat );
                double y = rad * Math.sin( lon ) * Math.cos( lat );
                double z = rad * Math.sin( lat );

                points.InsertNextPoint(x, y, z);
            	idList.SetId(i, c);
            	++c;
            }
*/
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
		
	/*
	public void setLineamentColor(int cellId, int[] color)
	{
		structuresPolyData.GetCellData().GetScalars().SetTuple4(cellId, color[0], color[1], color[2], color[3]);
		structuresPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}

	public void setsAllLineamentsColor(int[] color)
	{
		int numLineaments = this.cellIdToLineamentMap.size();
		vtkDataArray colors = structuresPolyData.GetCellData().GetScalars();
		
		for (int i=0; i<numLineaments; ++i)
			colors.SetTuple4(i, color[0], color[1], color[2], color[3]);
		
		structuresPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}
	
	public void setMSIImageLineamentsColor(int cellId, int[] color)
	{
		int numLineaments = this.cellIdToLineamentMap.size();
		String name = cellIdToLineamentMap.get(cellId).name;
		vtkDataArray colors = structuresPolyData.GetCellData().GetScalars();
		
		for (int i=0; i<numLineaments; ++i)
			if (cellIdToLineamentMap.get(i).name.equals(name))
					colors.SetTuple4(i, color[0], color[1], color[2], color[3]);

		structuresPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}
	 */
	
	public void setRadialOffset(double offset)
	{
        int ptId=0;
        vtkPoints points = circlesPolyData.GetPoints();
        /*
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Circle lin =	this.idToLineamentMap.get(id);

            int size = lin.lat.size();

            for (int i=0;i<size;++i)
            {
                double x = (lin.rad.get(i)+offset) * Math.cos( lin.lon.get(i) ) * Math.cos( lin.lat.get(i) );
                double y = (lin.rad.get(i)+offset) * Math.sin( lin.lon.get(i) ) * Math.cos( lin.lat.get(i) );
                double z = (lin.rad.get(i)+offset) * Math.sin( lin.lat.get(i) );
            	points.SetPoint(ptId, x, y, z);
            	++ptId;
            }
		}		
*/
		circlesPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
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

    public void addNewCircle(double[] center, double radius)
    {
    	circlesPolyData.Modified();
		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
    }
    
    public void updateCircleCenter(int cellId, double[] newCenter)
    {
    	circlesPolyData.Modified();
		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
    }
    
    public void updateCircelRadius(int cellId, double newRadius)
    {
    	circlesPolyData.Modified();
		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
    }

    public void removeCircle(int cellId)
    {
    	circlesPolyData.Modified();
		this.pcs.firePropertyChange(Properties.CIRCLE_MODEL_CHANGED, null, null);
    }

}
