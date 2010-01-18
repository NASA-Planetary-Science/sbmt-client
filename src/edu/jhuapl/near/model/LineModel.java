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
public class LineModel extends Model 
{
	//private HashMap<Integer, Lineament> idToLineamentMap = new HashMap<Integer, Lineament>();
	//private HashMap<Integer, Lineament> cellIdToLineamentMap = new HashMap<Integer, Lineament>();
	private ArrayList<Lineament> lineaments = new ArrayList<Lineament>();
	private vtkPolyData lineamentsPolyData;
    private ArrayList<vtkActor> lineamentsActors = new ArrayList<vtkActor>();
    //private vtkActor structuresActor;
	private int[] defaultColor = {255, 0, 255, 255}; // RGBA, default to purple
	static public String LINEAMENTS = "lineaments";

	public static class Lineament extends StructureModel.Structure
	{
		public int cellId;
		public ArrayList<Integer> pointIds = new ArrayList<Integer>();
		
		public String name = "";
		public int id;
		public ArrayList<Double> lat = new ArrayList<Double>();
		public ArrayList<Double> lon = new ArrayList<Double>();
		public ArrayList<Double> rad = new ArrayList<Double>();
		//public ArrayList<Double> x = new ArrayList<Double>();
		//public ArrayList<Double> y = new ArrayList<Double>();
		//public ArrayList<Double> z = new ArrayList<Double>();
		//public BoundingBox bb = new BoundingBox();
		
		static public String LINEAMENT = "lineament";
		static public String ID = "id";
		static public String MSI_IMAGE = "msi-image";
		static public String VERTICES = "vertices";
		
	    public Element toXmlDomElement(Document dom)
	    {
	    	Element linEle = dom.createElement(LINEAMENT);
	    	linEle.setAttribute(ID, String.valueOf(id));
	    	linEle.setAttribute(MSI_IMAGE, String.valueOf(name));

	    	String vertices = "";
            int size = lat.size();
            
            for (int i=0;i<size;++i)
            {
            	vertices += lat.get(i) + " " + lon.get(i) + " " + rad.get(i);
            	
            	if (i < size-1)
            		vertices += " ";
            }

	    	linEle.setAttribute(VERTICES, vertices);

	    	return linEle;
	    }

	    public void fromXmlDomElement(Element element)
	    {
	    	id = Integer.parseInt(element.getAttribute(ID));
	    	name = element.getAttribute(MSI_IMAGE);
	    	String tmp = element.getAttribute(VERTICES);

	    	String[] tokens = tmp.split(" ");

	    	lat.clear();
	    	lon.clear();
	    	rad.clear();
	    	for (int i=0; i<tokens.length;)
	    	{
	    		lat.add(Double.parseDouble(tokens[i++]));
	    		lon.add(Double.parseDouble(tokens[i++]));
	    		rad.add(Double.parseDouble(tokens[i++]));
	    	}
	    	
	    }

	    public String getClickStatusBarText()
	    {
	    	return "Lineament " + id + " mapped on MSI image " + name + " contains " + lat.size() + " vertices";
	    }

	}


    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(LINEAMENTS);

    	dom.appendChild(rootEle);

		for (Lineament lin : this.lineaments)
		{
			rootEle.appendChild(lin.toXmlDomElement(dom));
		}

		return rootEle;
    }
    
    public void fromXmlDomElement(Element element)
    {
    	this.lineaments.clear();
    	
		NodeList nl = element.getElementsByTagName(Lineament.LINEAMENT);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Lineament lin = new Lineament();
				
				lin.fromXmlDomElement(el);

				this.lineaments.add(lin);
			}
		}

		createPolyData();

		vtkPolyDataMapper lineamentMapper = new vtkPolyDataMapper();
		lineamentMapper.SetInput(lineamentsPolyData);
		//lineamentMapper.SetResolveCoincidentTopologyToPolygonOffset();
		//lineamentMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);

		vtkActor lineamentActor = new vtkActor();
		lineamentActor.SetMapper(lineamentMapper);

		lineamentsActors.clear();
		lineamentsActors.add(lineamentActor);

		System.out.println("Number of lineaments: " + this.lineaments.size());
    }
    
	

	private void createPolyData()
	{
		lineamentsPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        
        colors.SetNumberOfComponents(4);
        
        vtkIdList idList = new vtkIdList();

        int c=0;
        int cellId = 0;
		for (Lineament lin : this.lineaments)
		{
			lin.cellId = cellId;
			lin.pointIds.clear();
			
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
            	lin.pointIds.add(c);
            	++c;
            }

            lines.InsertNextCell(idList);
        	colors.InsertNextTuple4(defaultColor[0],defaultColor[1],defaultColor[2],defaultColor[3]);
            
            ++cellId;
		}
		
        lineamentsPolyData.SetPoints(points);
        lineamentsPolyData.SetLines(lines);
        lineamentsPolyData.GetCellData().SetScalars(colors);
	}
	
	public StructureModel.Structure getStructure(int idx)
	{
		return this.lineaments.get(idx);
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
        vtkPoints points = lineamentsPolyData.GetPoints();
        
		for (Lineament lin : this.lineaments)
		{
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

		lineamentsPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}

	/*
	public void setShowStructures(boolean show)
	{
		if (show)
		{
			if (lineamentsActors.isEmpty())
			{
				lineamentsActors.add(structuresActor);
				this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (!lineamentsActors.isEmpty())
			{
				lineamentsActors.clear();
				this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
			}
		}
		
	}
	*/
	
	public ArrayList<vtkActor> getActors() 
	{
		return lineamentsActors;
	}
	
    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
    	StructureModel.Structure struc = getStructure(cellId);
		if (struc != null)
			return struc.getClickStatusBarText();
		else
			return "";
    }

    public void addNewLineament(double[] pt1, double[] pt2)
    {
		lineamentsPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
    }
    
    public void updateLineamentVertex(int cellId, int vertexId, double[] newPoint)
    {
        vtkPoints points = lineamentsPolyData.GetPoints();
        
        Lineament lin = lineaments.get(cellId);
        int ptId = lin.pointIds.get(vertexId);
        points.SetPoint(ptId, newPoint);
        
		lineamentsPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
    }
    
    public void addLineamentVertex(int cellId, double[] newPoint)
    {
		lineamentsPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
    }

    public void removeLineamentVertex(int cellId, int vertexId)
    {
		lineamentsPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
    }

}
