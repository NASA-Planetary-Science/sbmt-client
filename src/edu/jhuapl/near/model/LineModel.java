package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhuapl.near.util.Point3D;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.GeometryUtil;

import vtk.*;

/**
 * Model of line structures drawn on Eros.
 * 
 * @author 
 *
 */
public class LineModel extends StructureModel implements PropertyChangeListener 
{
	private ArrayList<Line> lines = new ArrayList<Line>();
	private vtkPolyData linesPolyData;
	private vtkPolyData selectionPolyData;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper lineMapper;
    private vtkPolyDataMapper lineSelectionMapper;
    private vtkActor lineActor;
    private vtkActor lineSelectionActor;
	private int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
	private int[] redColor = {255, 0, 0, 255}; // RGBA red
	private int[] blueColor = {0, 0, 255, 255}; // RGBA blue
    private ErosModel erosModel;
    private int selectedLine = -1;
    private int currentLineVertex = -1000;
    private int highlightedStructure = -1;
    private int[] highlightColor = {0, 0, 255, 255};

	private static String LINES = "lines";

	private static int maxId = 0;

	public static String PATH = "path";
	public static String ID = "id";
	public static String NAME = "name";
	public static String VERTICES = "vertices";
	public static String LENGTH = "length";
	public static String SHAPE_MODEL_NAME = "shapemodel";
	
    private static DecimalFormat decimalFormatter = new DecimalFormat("#.###");
	
	public class Line extends StructureModel.Structure
	{
		public String name = "default";
		public int id;
		
		// Note the lat, lon, and alt is what gets stored in the saved file.
		// These are the control points.
		public ArrayList<Double> lat = new ArrayList<Double>();
		public ArrayList<Double> lon = new ArrayList<Double>();
		public ArrayList<Double> rad = new ArrayList<Double>();
		
		// Note xyzPointList is what's displayed. There will usually be more of these points than
		// lat, lon, alt in order to ensure the line is right above the surface of eros.
		public ArrayList<Point3D> xyzPointList = new ArrayList<Point3D>();
		public ArrayList<Integer> controlPointIds = new ArrayList<Integer>();
		

		public Line()
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

		public void setName(String name)
		{
			this.name = name;
		}

		public String getType()
		{
			return PATH;
		}
		
		public String getInfo()
		{
			return decimalFormatter.format(getPathLength()) + " km, " + controlPointIds.size() + " vertices";
		}
		
	    public Element toXmlDomElement(Document dom)
	    {
	    	Element linEle = dom.createElement(PATH);
	    	linEle.setAttribute(ID, String.valueOf(id));
	    	linEle.setAttribute(NAME, name);
	    	linEle.setAttribute(LENGTH, String.valueOf(getPathLength()));
	    	
	    	String vertices = "";
            int size = lat.size();
            
            for (int i=0;i<size;++i)
            {
            	double latitude = lat.get(i)*180.0/Math.PI;
            	double longitude = lon.get(i)*180.0/Math.PI;
            	if (longitude < 0.0)
            		longitude += 360.0;
            	
            	vertices += latitude + " " + longitude + " " + rad.get(i);
            	
            	if (i < size-1)
            		vertices += " ";
            }

	    	linEle.setAttribute(VERTICES, vertices);

	    	return linEle;
	    }

	    public void fromXmlDomElement(Element element, String shapeModelName)
	    {
	    	lat.clear();
	    	lon.clear();
	    	rad.clear();
	    	controlPointIds.clear();
	    	xyzPointList.clear();

	    	id = Integer.parseInt(element.getAttribute(ID));
	    	
	    	if (id > maxId)
	    		maxId = id;
	    	
	    	name = element.getAttribute(NAME);
	    	String tmp = element.getAttribute(VERTICES);

	    	if (tmp.length() == 0)
	    		return;
	    	
	    	String[] tokens = tmp.split(" ");
	    	
	    	int count = 0;
	    	for (int i=0; i<tokens.length;)
	    	{
	    		lat.add(Double.parseDouble(tokens[i++])*Math.PI/180.0);
	    		lon.add(Double.parseDouble(tokens[i++])*Math.PI/180.0);
	    		rad.add(Double.parseDouble(tokens[i++]));
	    		
	    		if (shapeModelName == null || !shapeModelName.equals(erosModel.getModelName()))
	    			shiftPointOnPathToClosestPointOnAsteroid(count);

	    		controlPointIds.add(xyzPointList.size());
	    		
	    		// Note, this point will be replaced with the correct value
	    		// when we call updateSegment
	    		double[] dummy = {0.0, 0.0, 0.0};
	    		xyzPointList.add(new Point3D(dummy));
	    		
	    		if (count > 0)
	    			this.updateSegment(count-1);
	    		
	    		++count;
	    	}
	    	
	    }

	    public String getClickStatusBarText()
	    {
	    	return "Path, Id = " + id
	    	+ ", Length = " + decimalFormatter.format(getPathLength()) + " km"
	    	+ ", Number of Vertices = " + lat.size();
	    }

	    public double getPathLength()
	    {
			int size = xyzPointList.size();
			double length = 0.0;
			
			for (int i=1;i<size;++i)
			{
				double dist = xyzPointList.get(i-1).distanceTo(xyzPointList.get(i));
				length += dist;
			}
	    	
			return length;
	    }
	    
	    public void updateSegment(int segment)
	    {
    		LatLon ll1 = new LatLon(lat.get(segment), lon.get(segment), rad.get(segment));
    		LatLon ll2 = new LatLon(lat.get(segment+1), lon.get(segment+1), rad.get(segment+1));
    		double pt1[] = GeometryUtil.latrec(ll1);
    		double pt2[] = GeometryUtil.latrec(ll2);
    		
    		int id1 = controlPointIds.get(segment);
    		int id2 = controlPointIds.get(segment+1);
    		
    		// Set the 2 control points
    		xyzPointList.set(id1, new Point3D(pt1));
    		xyzPointList.set(id2, new Point3D(pt2));
    		
    		vtkPoints points = null;
            if (Math.abs(lat.get(segment) - lat.get(segment+1)) < 1e-8 &&
                	Math.abs(lon.get(segment) - lon.get(segment+1)) < 1e-8 &&
                	Math.abs(rad.get(segment) - rad.get(segment+1)) < 1e-8)
            {
            	points = new vtkPoints();
            	points.InsertNextPoint(pt1);
            	points.InsertNextPoint(pt2);
            }
            else
            {
        		vtkPolyData poly = erosModel.drawPath(pt1, pt2);
        		if (poly == null)
        			return;
        			
        		points = poly.GetPoints();
            }
    		
    		// Remove points BETWEEN the 2 control points
    		for (int i=0; i<id2-id1-1; ++i)
    		{
    			xyzPointList.remove(id1+1);
    		}
    		
    		// Set the new points
    		int numNewPoints = points.GetNumberOfPoints();
    		for (int i=1; i<numNewPoints-1; ++i)
    		{
    			xyzPointList.add(id1+i, new Point3D(points.GetPoint(i)));
    		}
    		
    		// Shift the control points ids from segment+1 till the end by the right amount.
    		int shiftAmount = id1+numNewPoints-1 - id2;
    		for (int i=segment+1; i<controlPointIds.size(); ++i)
    		{
    			controlPointIds.set(i, controlPointIds.get(i) + shiftAmount);
    		}
    		
	    }

		private void shiftPointOnPathToClosestPointOnAsteroid(int idx)
		{
			System.out.println("shifted point " + idx);
			// When the resolution changes, the control points, might no longer
			// be touching the asteroid. Therefore shift each control to the closest
			// point on the asteroid.
			LatLon llr = new LatLon(lat.get(idx), lon.get(idx), rad.get(idx));
			double pt[] = GeometryUtil.latrec(llr);
			double[] closestPoint = erosModel.findClosestPoint(pt);
			LatLon ll = GeometryUtil.reclat(closestPoint);
			lat.set(idx, ll.lat);
			lon.set(idx, ll.lon);
			rad.set(idx, ll.rad);
		}
	}

	public LineModel(ErosModel erosModel)
	{
		this.erosModel = erosModel;

		this.erosModel.addPropertyChangeListener(this);
		
		lineActor = new vtkActor();
		lineActor.GetProperty().SetLineWidth(2.0);

		lineSelectionActor = new vtkActor();
    	lineSelectionActor.GetProperty().SetColor(1.0, 0.0, 0.0);
    	lineSelectionActor.GetProperty().SetPointSize(7.0);
	}

    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(LINES);
    	rootEle.setAttribute(SHAPE_MODEL_NAME, erosModel.getModelName());

		for (Line lin : this.lines)
		{
			rootEle.appendChild(lin.toXmlDomElement(dom));
		}

		return rootEle;
    }
    
    public void fromXmlDomElement(Element element)
    {
    	this.lines.clear();

    	String shapeModelName = null;
    	if (element.hasAttribute(SHAPE_MODEL_NAME))
    		shapeModelName= element.getAttribute(SHAPE_MODEL_NAME);

		NodeList nl = element.getElementsByTagName(PATH);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Line lin = new Line();
				
				lin.fromXmlDomElement(el, shapeModelName);

				this.lines.add(lin);
			}
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
	private void updatePolyData()
	{
		linesPolyData = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray lineCells = new vtkCellArray();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();

		linesPolyData.SetPoints(points);
		linesPolyData.SetLines(lineCells);
		linesPolyData.GetCellData().SetScalars(colors);

		colors.SetNumberOfComponents(4);

		vtkIdList idList = new vtkIdList();

		int c=0;
		for (int j=0; j<this.lines.size(); ++j)
		{
			Line lin = this.lines.get(j);
			
			int[] color = purpleColor;

			if (j == this.highlightedStructure)
				color = highlightColor;

			int size = lin.xyzPointList.size();
			idList.SetNumberOfIds(size);

			for (int i=0;i<size;++i)
			{
				points.InsertNextPoint(lin.xyzPointList.get(i).xyz);
				idList.SetId(i, c);
				++c;
			}

			lineCells.InsertNextCell(idList);
			colors.InsertNextTuple4(color[0],color[1],color[2],color[3]);

		}

		erosModel.shiftPolyLineInNormalDirection(linesPolyData, 0.002);

		if (lineMapper == null)
			lineMapper = new vtkPolyDataMapper();
		lineMapper.SetInput(linesPolyData);
		lineMapper.Update();
		
        if (!actors.contains(lineActor))
        	actors.add(lineActor);

        lineActor.SetMapper(lineMapper);
        lineActor.Modified();
	}
	
	public ArrayList<vtkProp> getProps() 
	{
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	if (prop == lineActor)
    	{
    		Line lin = this.lines.get(cellId);
    		if (lin != null)
    			return lin.getClickStatusBarText();
    		else
    			return "";
    	}
    	else
    	{
    		return "";
    	}
    }

    public int getNumberOfStructures()
    {
    	return lines.size();
    }
    
    public Structure getStructure(int cellId)
    {
    	return lines.get(cellId);
    }
    
    public Line getSelectedLine()
    {
    	if (selectedLine >= 0 && selectedLine < lines.size())
    		return lines.get(selectedLine);
    	else
    		return null;
    }

    public int getSelectedStructureIndex()
    {
    	return selectedLine;
    }
    
    public vtkActor getLineActor()
    {
    	return lineActor;
    }
    
    public vtkActor getLineSelectionActor()
    {
    	return lineSelectionActor;
    }
    
    /**
     * Return the total number of points in all the lines combined.
     * @return
     */
    public int getTotalNumberOfPoints()
    {
    	int numberOfPoints = 0;
		for (Line lin : this.lines)
		{
			numberOfPoints += lin.lat.size();
		}    	
    	return numberOfPoints;
    }

    /*
    public void addNewLine(double[] pt1, double[] pt2)
    {
        LatLon ll1 = Spice.reclat(pt1);
        LatLon ll2 = Spice.reclat(pt2);
        
        Line lin = new Line();
        lin.lat.add(ll1.lat);
        lin.lon.add(ll1.lon);
        lin.rad.add(ll1.rad);
        lin.lat.add(ll2.lat);
        lin.lon.add(ll2.lon);
        lin.rad.add(ll2.rad);

        lin.xyzPointList.add(new Point3D(pt1));
        lin.xyzPointList.add(new Point3D(pt2));
        lin.controlPointIds.add(0);
        lin.controlPointIds.add(1);
        lin.updateSegment(erosModel, 0);
        lines.add(lin);
        
        updatePolyData();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    */
    
    public void addNewStructure()
    {
        Line lin = new Line();
        lines.add(lin);
        selectStructure(lines.size()-1);
		this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
    }
    
    /*
    public void addNewLine(double[] pt1)
    {
        LatLon ll1 = Spice.reclat(pt1);
        
        Line lin = new Line();
        lin.lat.add(ll1.lat);
        lin.lon.add(ll1.lon);
        lin.rad.add(ll1.rad);

        lin.xyzPointList.add(new Point3D(pt1));
        lin.controlPointIds.add(0);
        lines.add(lin);
        
        updatePolyData();
        
        selectLine(lines.size()-1);
    }
    */
    
    public void updateSelectedLineVertex(int vertexId, double[] newPoint)
    {
        Line lin = lines.get(selectedLine);
        
        int numVertices = lin.lat.size();

    	LatLon ll = GeometryUtil.reclat(newPoint);
    	lin.lat.set(vertexId, ll.lat);
    	lin.lon.set(vertexId, ll.lon);
    	lin.rad.set(vertexId, ll.rad);

        // If we're modifying the last vertex
        if (vertexId == numVertices - 1)
        {
        	lin.updateSegment(vertexId-1);
        }
        // If we're modifying the first vertex
        else if (vertexId == 0)
        {
        	lin.updateSegment(vertexId);
        }
        // If we're modifying a middle vertex
        else
        {
        	lin.updateSegment(vertexId-1);
        	lin.updateSegment(vertexId);
        }
        
        updatePolyData();

        updateLineSelection();
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    /*
    public void addVertexToLine(int cellId, double[] newPoint)
    {
        Line lin = lines.get(cellId);
        LatLon ll = Spice.reclat(newPoint);
        
        // If the added point is the same as the current last point, return doing nothing.
        System.out.println(lin.lat.size());
        int lastIdx = lin.lat.size() - 1;
        int prevIdx = lin.lat.size() - 2;

        System.out.println(new LatLon(lin.lat.get(lastIdx), lin.lon.get(lastIdx), lin.rad.get(lastIdx)));
        System.out.println(new LatLon(lin.lat.get(prevIdx), lin.lon.get(prevIdx), lin.rad.get(prevIdx)));

        if (prevIdx >= 0 &&
        	Math.abs(lin.lat.get(prevIdx) - lin.lat.get(lastIdx)) < 1e-8 &&
        	Math.abs(lin.lon.get(prevIdx) - lin.lon.get(lastIdx)) < 1e-8 &&
        	Math.abs(lin.rad.get(prevIdx) - lin.rad.get(lastIdx)) < 1e-8)
        {
        	System.out.println("Warning: new vertex same as last vertex");
//        	return;
        }
        
        lin.lat.add(ll.lat);
        lin.lon.add(ll.lon);
        lin.rad.add(ll.rad);

        lin.xyzPointList.add(new Point3D(newPoint));
        lin.controlPointIds.add(lin.xyzPointList.size()-1);

        updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    */

    /*
    public void addVertexToSelectedLine(double[] newPoint)
    {
        Line lin = lines.get(selectedLine);
        LatLon ll = Spice.reclat(newPoint);
        
        lin.lat.add(ll.lat);
        lin.lon.add(ll.lon);
        lin.rad.add(ll.rad);

        lin.xyzPointList.add(new Point3D(newPoint));
        lin.controlPointIds.add(lin.xyzPointList.size()-1);

        if (lin.controlPointIds.size() >= 2)
        	lin.updateSegment(lin.lat.size()-2);

        updatePolyData();
        
        updateLineSelection();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
     */
    
    public void insertVertexIntoSelectedLine(double[] newPoint)
    {
    	if (selectedLine < 0)
    		return;
    	
        Line lin = lines.get(selectedLine);

    	if (currentLineVertex < -1 || currentLineVertex >= lin.controlPointIds.size())
    		System.out.println("Error: currentLineVertex is invalid");
    	
        LatLon ll = GeometryUtil.reclat(newPoint);
        
        lin.lat.add(currentLineVertex+1, ll.lat);
        lin.lon.add(currentLineVertex+1, ll.lon);
        lin.rad.add(currentLineVertex+1, ll.rad);

		// Remove points BETWEEN the 2 control points (If we're adding a point in the middle)
        if (currentLineVertex < lin.controlPointIds.size()-1)
        {
        	int id1 = lin.controlPointIds.get(currentLineVertex);
        	int id2 = lin.controlPointIds.get(currentLineVertex+1);
        	int numberPointsRemoved = id2-id1-1;
        	for (int i=0; i<id2-id1-1; ++i)
        	{
        		lin.xyzPointList.remove(id1+1);
        	}

        	lin.xyzPointList.add(id1+1, new Point3D(newPoint));
        	lin.controlPointIds.add(currentLineVertex+1, id1+1);

        	// Shift the control points ids from currentLineVertex+2 till the end by the right amount.
        	for (int i=currentLineVertex+2; i<lin.controlPointIds.size(); ++i)
        	{
        		lin.controlPointIds.set(i, lin.controlPointIds.get(i) - (numberPointsRemoved-1));
        	}
        }
        else
        {
        	lin.xyzPointList.add(new Point3D(newPoint));
        	lin.controlPointIds.add(lin.xyzPointList.size()-1);
        }

        if (lin.controlPointIds.size() >= 2)
        {
        	if (currentLineVertex < 0)
        	{
        		// Do nothing
        	}
        	else if (currentLineVertex < lin.controlPointIds.size()-2)
        	{
        		lin.updateSegment(currentLineVertex);
        		lin.updateSegment(currentLineVertex+1);
        	}
        	else
        	{
        		lin.updateSegment(currentLineVertex);
        	}
        }

        ++currentLineVertex;
        
        updatePolyData();
        
        updateLineSelection();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    
    public void removeStructure(int cellId)
    {
    	lines.remove(cellId);

        updatePolyData();
        
        if (cellId == selectedLine)
        	selectStructure(-1);
        else
        	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
	
    public void moveSelectionVertex(int vertexId, double[] newPoint)
    {
    	vtkPoints points = selectionPolyData.GetPoints();
    	points.SetPoint(vertexId, newPoint);
    	selectionPolyData.Modified();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    private void updateLineSelection()
    {
    	if (selectedLine == -1)
    	{
            if (actors.contains(lineSelectionActor))
            	actors.remove(lineSelectionActor);
            
            return;
    	}

        Line lin = lines.get(selectedLine);
        
		selectionPolyData = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray vert = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();

		selectionPolyData.SetPoints( points );
		selectionPolyData.SetVerts( vert );
		selectionPolyData.GetCellData().SetScalars(colors);

        colors.SetNumberOfComponents(4);

		int numPoints = lin.controlPointIds.size();

        points.SetNumberOfPoints(numPoints);

		vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);
        
		for (int i=0; i<numPoints; ++i)
		{
			int idx = lin.controlPointIds.get(i);
            points.SetPoint(i, lin.xyzPointList.get(idx).xyz);
        	idList.SetId(0, i);
		    vert.InsertNextCell(idList);
		    if (i == this.currentLineVertex)
		    	colors.InsertNextTuple4(blueColor[0],blueColor[1],blueColor[2],blueColor[3]);
		    else
		    	colors.InsertNextTuple4(redColor[0],redColor[1],redColor[2],redColor[3]);
		}

		erosModel.shiftPolyLineInNormalDirection(selectionPolyData, 0.001);
		
		if (lineSelectionMapper == null)
			lineSelectionMapper = new vtkPolyDataMapper();
        lineSelectionMapper.SetInput(selectionPolyData);
        lineSelectionMapper.Update();
        
        if (!actors.contains(lineSelectionActor))
        	actors.add(lineSelectionActor);

        lineSelectionActor.SetMapper(lineSelectionMapper);
        lineSelectionActor.Modified();
    }
    
    public void selectStructure(int cellId)
    {
    	if (selectedLine == cellId)
    		return;
    	
    	selectedLine = cellId;

    	if (cellId >= 0)
    	{
    		Line lin = lines.get(selectedLine);
    		currentLineVertex = lin.controlPointIds.size()-1;
    	}
    	else
    	{
    		currentLineVertex = -1000;
    	}
    	
    	updateLineSelection();

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public void selectCurrentLineVertex(int idx)
    {
    	currentLineVertex = idx;
    	
    	updateLineSelection();

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

	public void loadModel(File file) throws Exception
	{
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		//Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		//parse using builder to get DOM representation of the XML file
		Document dom = db.parse(file);

		//get the root element
		Element docEle = dom.getDocumentElement();

		if (LineModel.LINES.equals(docEle.getTagName()))
			fromXmlDomElement(docEle);
	}

	public void saveModel(File file) throws Exception
	{
		//get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		//get an instance of builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		//create an instance of DOM
		Document dom = db.newDocument();

		dom.appendChild(toXmlDomElement(dom));
		
		try
		{
			Source source = new DOMSource(dom);

			OutputStream fout= new FileOutputStream(file);
			Result result = new StreamResult(new OutputStreamWriter(fout, "utf-8"));
			
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", new Integer(4));
			
			Transformer xformer = tf.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");

			xformer.transform(source, result);
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		} 
	}

	public boolean supportsSelection()
	{
		return true;
	}

	public int getStructureIndexFromCellId(int cellId, vtkProp prop)
	{
		if (prop == lineActor)
			return cellId;
		else if (prop == lineSelectionActor)
			return selectedLine;
		else
			return -1;
	}
    
	public void highlightStructure(int idx)
	{
		if (highlightedStructure != idx)
		{
			this.highlightedStructure = idx;
			updatePolyData();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}
	
	public int getHighlightedStructure()
	{
		return highlightedStructure;
	}

	
    /*
    public void removeVertexFromLine(int vertexId)
    {
        Line lin = lines.get(selectedLine);

        lin.lat.remove(vertexId);
        lin.lon.remove(vertexId);
        lin.rad.remove(vertexId);

        // If one of the end points is being removed, then we only need to remove the line connecting the
        // end point to the adjacent point. If we're removing a non-end point, we need to remove the line
        // segments connecting the 2 adjacent control points and in addition, we need to draw a new line
        // connecting the 2 adjacent control points.
		// Remove points BETWEEN the 2 adjacent control points (If we're removing a point in the middle)
        if (lin.controlPointIds.size() > 1)
        {
        	if (vertexId == 0)
        	{
        		int id2 = lin.controlPointIds.get(vertexId+1);
                int numberPointsRemoved = id2;
        		for (int i=0; i<numberPointsRemoved; ++i)
        		{
        			lin.xyzPointList.remove(0);
        		}
                lin.controlPointIds.remove(vertexId);

                for (int i=vertexId+1; i<lin.controlPointIds.size(); ++i)
            		lin.controlPointIds.set(i, lin.controlPointIds.get(i) - (numberPointsRemoved-1));
        	}
        	if (vertexId == lin.controlPointIds.size()-1)
        	{
        		int id1 = lin.controlPointIds.get(vertexId-1);
        		int id2 = lin.controlPointIds.get(vertexId);
                int numberPointRemoved = id2-id1;
        		for (int i=0; i<numberPointRemoved; ++i)
        		{
        			lin.xyzPointList.remove(id1+1);
        		}
                lin.controlPointIds.remove(vertexId);
        	}
        	else
        	{
        		int id1 = lin.controlPointIds.get(vertexId-1);
        		int id2 = lin.controlPointIds.get(vertexId+1);
        		for (int i=id1+1; i<id2; ++i)
        		{
        			lin.xyzPointList.remove(id1+1);
        		}
                lin.controlPointIds.remove(vertexId);
        		lin.updateSegment(vertexId-1);
        	}
        }

        if (currentLineVertex < lin.controlPointIds.size()-1)
        {
        	int id1 = lin.controlPointIds.get(currentLineVertex);
        	int id2 = lin.controlPointIds.get(currentLineVertex+1);
        	int numberPointsRemoved = id2-id1-1;
        	for (int i=0; i<id2-id1-1; ++i)
        	{
        		lin.xyzPointList.remove(id1+1);
        	}

        	lin.xyzPointList.add(id1+1, new Point3D(newPoint));
        	lin.controlPointIds.add(currentLineVertex+1, id1+1);

        	// Shift the control points ids from currentLineVertex+2 till the end by the right amount.
        	for (int i=currentLineVertex+2; i<lin.controlPointIds.size(); ++i)
        	{
        		lin.controlPointIds.set(i, lin.controlPointIds.get(i) - (numberPointsRemoved-1));
        	}
        }
        else
        {
        	lin.xyzPointList.add(new Point3D(newPoint));
        	lin.controlPointIds.add(lin.xyzPointList.size()-1);
        }

        if (lin.controlPointIds.size() >= 2)
        {
        	if (vertexId == 0)
        	{
        		lin.updateSegment(vertexId);
        	}
        	else if (currentLineVertex < lin.controlPointIds.size()-2)
        	{
        		lin.updateSegment(currentLineVertex);
        		lin.updateSegment(currentLineVertex+1);
        	}
        	else
        	{
        		lin.updateSegment(currentLineVertex);
        	}
        }

        if (vertexId == currentLineVertex)
        {
        	--currentLineVertex;
        	if (currentLineVertex < 0 && lin.controlPointIds.size() > 0)
        		currentLineVertex = 0;
        }
        
        updatePolyData();
        
        updateLineSelection();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    	
    }
    */
	
	public void redrawAllStructures()
	{
        for (Line lin : this.lines)
		{
        	for (int i=0; i<lin.controlPointIds.size(); ++i)
        		lin.shiftPointOnPathToClosestPointOnAsteroid(i);
        	
        	for (int i=0; i<lin.controlPointIds.size()-1; ++i)
        		lin.updateSegment(i);
		}

		updatePolyData();

        updateLineSelection();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
		{
			redrawAllStructures();
		}	
	}
}
