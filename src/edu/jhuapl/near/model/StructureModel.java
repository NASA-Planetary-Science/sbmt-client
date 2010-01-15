package edu.jhuapl.near.model;

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

import vtk.*;

public class StructureModel extends Model 
{
	private HashMap<Integer, Lineament> idToLineamentMap = new HashMap<Integer, Lineament>();
	private HashMap<Integer, Lineament> cellIdToLineamentMap = new HashMap<Integer, Lineament>();
	private ArrayList<Structure> structures = new ArrayList<Structure>();
	private vtkPolyData structuresPolyData;
    private ArrayList<vtkActor> structuresActors = new ArrayList<vtkActor>();
    private vtkActor structuresActor;
	private int[] defaultColor = {255, 0, 255, 255}; // RGBA, default to purple
	static public String STRUCTURES = "structures";

	public static abstract class Structure
	{
		public abstract Element toXmlDomElement(Document dom);
	    public abstract void fromXmlDomElement(Element element);
	    public abstract String getClickStatusBarText();
	}
	
	public static class Lineament extends Structure
	{
		public int cellId;
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

	public static class Circle extends Structure
	{
		public int cellId;
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

	public void loadStructures(File file)
	{
		if (structuresActor == null)
		{
			try 
			{
				loadModel(file);

				createPolyData();

				vtkPolyDataMapper lineamentMapper = new vtkPolyDataMapper();
				lineamentMapper.SetInput(structuresPolyData);
				//lineamentMapper.SetResolveCoincidentTopologyToPolygonOffset();
				//lineamentMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);

				structuresActor = new vtkActor();
				structuresActor.SetMapper(lineamentMapper);

				// By default do not show the lineaments
				//lineamentActors.add(lineamentActor);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Number of lineaments: " + this.idToLineamentMap.size());
		}
	}
	
	public void loadModel(File file) throws NumberFormatException, IOException
	{
		try 
		{
			//get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(file);

			//get the root element
			Element docEle = dom.getDocumentElement();

			this.idToLineamentMap.clear();
			
			//get a nodelist of  elements
			NodeList nl = docEle.getElementsByTagName(Lineament.LINEAMENT);
			if(nl != null && nl.getLength() > 0)
			{
				for(int i = 0 ; i < nl.getLength();i++) 
				{
					Element el = (Element)nl.item(i);

					Lineament lin = new Lineament();
					
					lin.fromXmlDomElement(el);

	            	this.idToLineamentMap.put(lin.id, lin);
				}
			}
		}
		catch(Exception e) 
		{
			JOptionPane.showMessageDialog(null,
					"There was an error reading the file.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}
	}

	public void saveModel(File file) throws NumberFormatException, IOException
	{
		try 
		{
			//get an instance of factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//create an instance of DOM
			Document dom = db.newDocument();

	    	Element rootEle = dom.createElement(STRUCTURES);

	    	dom.appendChild(rootEle);

			for (Integer id : this.idToLineamentMap.keySet())
			{
				Lineament lin =	this.idToLineamentMap.get(id);

				rootEle.appendChild(lin.toXmlDomElement(dom));
			}

			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);

			XMLSerializer serializer = new XMLSerializer(
					new FileOutputStream(file), format);

			serializer.serialize(dom);

		} 
		catch(Exception e) 
		{
			JOptionPane.showMessageDialog(null,
					"There was an error saving the file.",
					"Error",
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}
	}
	

	private void createPolyData()
	{
		structuresPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        
        colors.SetNumberOfComponents(4);
        
        vtkIdList idList = new vtkIdList();

        int c=0;
        int cellId = 0;
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Lineament lin =	this.idToLineamentMap.get(id);
			lin.cellId = cellId;
			
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

            lines.InsertNextCell(idList);
        	colors.InsertNextTuple4(defaultColor[0],defaultColor[1],defaultColor[2],defaultColor[3]);
            
            cellIdToLineamentMap.put(cellId, lin);
            ++cellId;
		}
		
        structuresPolyData.SetPoints(points);
        structuresPolyData.SetLines(lines);
        structuresPolyData.GetCellData().SetScalars(colors);
	}
	
	public Structure getStructure(int idx)
	{
		return this.structures.get(idx);
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
        vtkPoints points = structuresPolyData.GetPoints();
        
		for (Integer id : this.idToLineamentMap.keySet())
		{
			Lineament lin =	this.idToLineamentMap.get(id);

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

		structuresPolyData.Modified();
		this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
	}

	public void setShowStructures(boolean show)
	{
		if (show)
		{
			if (structuresActors.isEmpty())
			{
				structuresActors.add(structuresActor);
				this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (!structuresActors.isEmpty())
			{
				structuresActors.clear();
				this.pcs.firePropertyChange(Properties.LINEAMENT_MODEL_CHANGED, null, null);
			}
		}
		
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		return structuresActors;
	}
	
    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
		Structure struc = getStructure(cellId);
		if (struc != null)
			return struc.getClickStatusBarText();
		else
			return "";
    }

}
