package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import edu.jhuapl.near.util.MathUtil;

import vtk.*;

/**
 * Model of line structures drawn on a body.
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
    private SmallBodyModel smallBodyModel;
    private int selectedLine = -1;
    private int currentLineVertex = -1000;
    private int highlightedStructure = -1;
    private int[] highlightColor = {0, 0, 255, 255};
    private int maximumVerticesPerLine = Integer.MAX_VALUE;
    
    private vtkPolyData emptyPolyData;

	private static final String LINES = "lines";
	private static final String SHAPE_MODEL_NAME = "shapemodel";
	private static final int[] redColor = {255, 0, 0, 255}; // RGBA red
	private static final int[] blueColor = {0, 0, 255, 255}; // RGBA blue
	

	public LineModel(SmallBodyModel smallBodyModel)
	{
		super(ModelNames.LINE_STRUCTURES);
		
		this.smallBodyModel = smallBodyModel;

		this.smallBodyModel.addPropertyChangeListener(this);
		
		lineActor = new vtkActor();
		lineActor.GetProperty().SetLineWidth(2.0);

		lineSelectionActor = new vtkActor();
    	lineSelectionActor.GetProperty().SetColor(1.0, 0.0, 0.0);
    	lineSelectionActor.GetProperty().SetPointSize(7.0);

    	// Initialize an empty polydata for resetting
		emptyPolyData = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetNumberOfComponents(4);
		emptyPolyData.SetPoints(points);
		emptyPolyData.SetLines(cells);
		emptyPolyData.SetVerts(cells);
		emptyPolyData.GetCellData().SetScalars(colors);

		linesPolyData = new vtkPolyData();
		linesPolyData.DeepCopy(emptyPolyData);

		selectionPolyData = new vtkPolyData();
		selectionPolyData.DeepCopy(emptyPolyData);

		lineSelectionMapper = new vtkPolyDataMapper();
        lineSelectionMapper.SetInput(selectionPolyData);
        lineSelectionMapper.Update();
        
        lineSelectionActor.SetMapper(lineSelectionMapper);
        lineSelectionActor.Modified();
        
    	actors.add(lineSelectionActor);
	}

    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(LINES);
    	rootEle.setAttribute(SHAPE_MODEL_NAME, smallBodyModel.getModelName());

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

		NodeList nl = element.getElementsByTagName(Line.PATH);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Line lin = new Line(smallBodyModel);
				
				lin.fromXmlDomElement(el, shapeModelName);

				this.lines.add(lin);
			}
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
	private void updatePolyData()
	{
		linesPolyData.DeepCopy(emptyPolyData);
		vtkPoints points = linesPolyData.GetPoints();
		vtkCellArray lineCells = linesPolyData.GetLines();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray)linesPolyData.GetCellData().GetScalars();

		vtkIdList idList = new vtkIdList();

		int c=0;
		for (int j=0; j<this.lines.size(); ++j)
		{
			Line lin = this.lines.get(j);
			
			int[] color = lin.color;

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

		smallBodyModel.shiftPolyLineInNormalDirection(linesPolyData, 0.002);

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
        lin.updateSegment(smallBodyModel, 0);
        lines.add(lin);
        
        updatePolyData();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    */
    
    public void addNewStructure()
    {
        Line lin = new Line(smallBodyModel);
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

    	LatLon ll = MathUtil.reclat(newPoint);
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
        
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, Properties.VERTEX_POSITION_CHANGED);
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

        if (lin.controlPointIds.size() == maximumVerticesPerLine)
        	return;
        
    	if (currentLineVertex < -1 || currentLineVertex >= lin.controlPointIds.size())
    		System.out.println("Error: currentLineVertex is invalid");
    	
        LatLon ll = MathUtil.reclat(newPoint);
        
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

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, Properties.VERTEX_INSERTED_INTO_LINE);
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
    
    protected void updateLineSelection()
    {
    	if (selectedLine == -1)
    	{
            if (actors.contains(lineSelectionActor))
            	actors.remove(lineSelectionActor);
            
            return;
    	}

        Line lin = lines.get(selectedLine);
        
        selectionPolyData.DeepCopy(emptyPolyData);
		vtkPoints points = selectionPolyData.GetPoints();
		vtkCellArray vert = selectionPolyData.GetVerts();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray)selectionPolyData.GetCellData().GetScalars();

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

		smallBodyModel.shiftPolyLineInNormalDirection(selectionPolyData, 0.001);
		
        if (!actors.contains(lineSelectionActor))
        	actors.add(lineSelectionActor);
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

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, Properties.LINE_SELECTED);
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
	
	public void setMaximumVerticesPerLine(int max)
	{
		maximumVerticesPerLine = max;
	}

	public void setStructureColor(int idx, int[] color)
	{
		lines.get(idx).setColor(color);
		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	protected vtkPolyData getSelectionPolyData()
	{
		return selectionPolyData;
	}
	
	protected vtkPolyData getEmptyPolyData()
	{
		return emptyPolyData;
	}
	
}
