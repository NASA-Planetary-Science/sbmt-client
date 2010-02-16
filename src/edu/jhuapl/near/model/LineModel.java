package edu.jhuapl.near.model;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhuapl.near.util.Point3D;
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
public class LineModel extends Model 
{
	private ArrayList<Line> lines = new ArrayList<Line>();
	private vtkPolyData linesPolyData;
	private vtkPolyData selectionPolyData;
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkActor lineActor;
    private vtkActor lineSelectionActor;
	private int[] defaultColor = {255, 0, 255, 255}; // RGBA, default to purple
    private ErosModel erosModel;
    private int selectedLine = -1;
    
	static public String LINES = "lines";

	
	public static class Line extends StructureModel.Structure
	{
		static private int maxId = 0;
		
		public String name = "";
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
		
		static public String LINE = "line";
		static public String ID = "id";
		static public String MSI_IMAGE = "msi-image";
		static public String VERTICES = "vertices";
		
		public Line()
		{
			id = ++maxId;
		}
		
	    public Element toXmlDomElement(Document dom)
	    {
	    	Element linEle = dom.createElement(LINE);
	    	linEle.setAttribute(ID, String.valueOf(id));
	    	linEle.setAttribute(MSI_IMAGE, String.valueOf(name));

	    	String vertices = "";
            int size = lat.size();
            
            for (int i=0;i<size;++i)
            {
            	vertices += lat.get(i)*180.0/Math.PI + " " + lon.get(i)*180.0/Math.PI + " " + rad.get(i);
            	
            	if (i < size-1)
            		vertices += " ";
            }

	    	linEle.setAttribute(VERTICES, vertices);

	    	return linEle;
	    }

	    public void fromXmlDomElement(Element element, ErosModel erosModel)
	    {
	    	id = Integer.parseInt(element.getAttribute(ID));
	    	
	    	if (id > maxId)
	    		maxId = id;
	    	
	    	name = element.getAttribute(MSI_IMAGE);
	    	String tmp = element.getAttribute(VERTICES);

	    	String[] tokens = tmp.split(" ");

	    	lat.clear();
	    	lon.clear();
	    	rad.clear();
	    	int count = 0;
	    	for (int i=0; i<tokens.length;)
	    	{
	    		lat.add(Double.parseDouble(tokens[i++])*Math.PI/180.0);
	    		lon.add(Double.parseDouble(tokens[i++])*Math.PI/180.0);
	    		rad.add(Double.parseDouble(tokens[i++]));
	    		
	    		controlPointIds.add(count);
	    		
	    		// Note, this point will be replaced with the correct value
	    		// when we call updateSegment
	    		double[] dummy = {0.0, 0.0, 0.0};
	    		xyzPointList.add(new Point3D(dummy));
	    		
	    		if (count > 0)
	    			this.updateSegment(erosModel, count-1);
	    		
	    		++count;
	    	}
	    	
	    }

	    public String getClickStatusBarText()
	    {
	    	return "Line " + id + " contains " + lat.size() + " vertices";
	    }

	    public void updateSegment(ErosModel erosModel, int segment)
	    {
    		LatLon ll1 = new LatLon(lat.get(segment), lon.get(segment), rad.get(segment));
    		LatLon ll2 = new LatLon(lat.get(segment+1), lon.get(segment+1), rad.get(segment+1));
    		double pt1[] = Spice.latrec(ll1);
    		double pt2[] = Spice.latrec(ll2);
    		
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
	}

	public LineModel(ErosModel erosModel)
	{
		this.erosModel = erosModel;

		lineActor = new vtkActor();
		lineActor.GetProperty().SetLineWidth(2.0);

		lineSelectionActor = new vtkActor();
    	lineSelectionActor.GetProperty().SetColor(1.0, 0.0, 0.0);
    	lineSelectionActor.GetProperty().SetPointSize(7.0);
	}

    public Element toXmlDomElement(Document dom)
    {
    	Element rootEle = dom.createElement(LINES);

		for (Line lin : this.lines)
		{
			rootEle.appendChild(lin.toXmlDomElement(dom));
		}

		return rootEle;
    }
    
    public void fromXmlDomElement(Element element)
    {
    	this.lines.clear();
    	
		NodeList nl = element.getElementsByTagName(Line.LINE);
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++) 
			{
				Element el = (Element)nl.item(i);

				Line lin = new Line();
				
				lin.fromXmlDomElement(el, erosModel);

				this.lines.add(lin);
			}
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

		System.out.println("Number of lines: " + this.lines.size());
    }
    
	private void updatePolyData()
	{
		linesPolyData = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray lines = new vtkCellArray();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();

		linesPolyData.SetPoints(points);
		linesPolyData.SetLines(lines);
		linesPolyData.GetCellData().SetScalars(colors);

		colors.SetNumberOfComponents(4);

		vtkIdList idList = new vtkIdList();

		int c=0;
		for (Line lin : this.lines)
		{
			int size = lin.xyzPointList.size();
			idList.SetNumberOfIds(size);

			for (int i=0;i<size;++i)
			{
				points.InsertNextPoint(lin.xyzPointList.get(i).xyz);
				idList.SetId(i, c);
				++c;
			}

			lines.InsertNextCell(idList);
			colors.InsertNextTuple4(defaultColor[0],defaultColor[1],defaultColor[2],defaultColor[3]);

		}

		erosModel.shiftPolyLineInNormalDirection(linesPolyData, 0.002);

		vtkPolyDataMapper lineMapper = new vtkPolyDataMapper();
		lineMapper.SetInput(linesPolyData);

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

    public int getNumberOfLines()
    {
    	return lines.size();
    }
    
    public Line getLine(int cellId)
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

    public int getSelectedLineIndex()
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
    
    public void addNewLine()
    {
        Line lin = new Line();
        lines.add(lin);
        selectLine(lines.size()-1);
    }
    
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
    
    
    public void updateSelectedLineVertex(int vertexId, double[] newPoint)
    {
        Line lin = lines.get(selectedLine);
        
        int numVertices = lin.lat.size();

    	LatLon ll = Spice.reclat(newPoint);
    	lin.lat.set(vertexId, ll.lat);
    	lin.lon.set(vertexId, ll.lon);
    	lin.rad.set(vertexId, ll.rad);

        // If we're modifying the last vertex
        if (vertexId == numVertices - 1)
        {
        	lin.updateSegment(erosModel, vertexId-1);
        }
        // If we're modifying the first vertex
        else if (vertexId == 0)
        {
        	lin.updateSegment(erosModel, vertexId);
        }
        // If we're modifying a middle vertex
        else
        {
        	lin.updateSegment(erosModel, vertexId-1);
        	lin.updateSegment(erosModel, vertexId);
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
        	lin.updateSegment(erosModel, lin.lat.size()-2);

        updatePolyData();
        
        updateLineSelection();
        
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    
    public void removeLine(int cellId)
    {
    	lines.remove(cellId);

        updatePolyData();
        
        if (cellId == selectedLine)
        	selectLine(-1);
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
		selectionPolyData.SetPoints( points );
		selectionPolyData.SetVerts( vert );

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
		}

		erosModel.shiftPolyLineInNormalDirection(selectionPolyData, 0.001);
		
        vtkPolyDataMapper pointsMapper = new vtkPolyDataMapper();
        pointsMapper.SetInput(selectionPolyData);

        if (!actors.contains(lineSelectionActor))
        	actors.add(lineSelectionActor);

        lineSelectionActor.SetMapper(pointsMapper);
        lineSelectionActor.Modified();
    }
    
    public void selectLine(int cellId)
    {
    	if (selectedLine == cellId)
    		return;
    	
    	selectedLine = cellId;

    	updateLineSelection();

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
