package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
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

import vtk.vtkActor;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Point3D;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.gravity.Gravity;

/**
 * Model of line structures drawn on a body.
 */
public class LineModel extends ControlPointsStructureModel implements PropertyChangeListener
{
    public enum Mode
    {
        DEFAULT,
        PROFILE,
        CLOSED
    }

    private ArrayList<Line> lines = new ArrayList<Line>();
    private vtkPolyData linesPolyData;
    private vtkPolyData activationPolyData;

    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper lineMapper;
    private vtkPolyDataMapper lineActivationMapper;
    private vtkActor lineActor;
    private vtkActor lineActivationActor;
    private SmallBodyModel smallBodyModel;
    private int activatedLine = -1;
    private int currentLineVertex = -1000;
    private int[] selectedStructures = {};
    private int maximumVerticesPerLine = Integer.MAX_VALUE;
    private vtkIdList idList;
    private int maxPolygonId = 0;

    private vtkPolyData emptyPolyData;

    private Mode mode = Mode.DEFAULT;

    private double offset;

    private static final String LINES = "lines";
    private static final String SHAPE_MODEL_NAME = "shapemodel";
    private static final int[] redColor = {255, 0, 0, 255}; // RGBA red
    private static final int[] greenColor = {0, 255, 0, 255}; // RGBA green
    private static final int[] blueColor = {0, 0, 255, 255}; // RGBA blue

    public LineModel(SmallBodyModel smallBodyModel)
    {
        this(smallBodyModel, false);
    }

    public LineModel(SmallBodyModel smallBodyModel, boolean profileMode)
    {
        this(smallBodyModel, profileMode ? Mode.PROFILE : Mode.DEFAULT);
    }

    public LineModel(SmallBodyModel smallBodyModel, Mode mode)
    {
        this.smallBodyModel = smallBodyModel;
        this.mode = mode;

        this.offset = getDefaultOffset();

        if (hasProfileMode())
            setMaximumVerticesPerLine(2);

        this.smallBodyModel.addPropertyChangeListener(this);

        idList = new vtkIdList();

        lineActor = new vtkActor();
        vtkProperty lineProperty = lineActor.GetProperty();

        lineProperty.SetLineWidth(2.0);

        if (hasProfileMode())
            lineProperty.SetLineWidth(3.0);

        lineActivationActor = new vtkActor();
        vtkProperty lineActivationProperty = lineActivationActor.GetProperty();
        lineActivationProperty.SetColor(1.0, 0.0, 0.0);
        lineActivationProperty.SetPointSize(7.0);

        // Initialize an empty polydata for resetting
        emptyPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray cells = new vtkCellArray();
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        colors.SetNumberOfComponents(4);
        emptyPolyData.SetPoints(points);
        emptyPolyData.SetLines(cells);
        emptyPolyData.SetVerts(cells);
        vtkCellData cellData = emptyPolyData.GetCellData();
        cellData.SetScalars(colors);

        linesPolyData = new vtkPolyData();
        linesPolyData.DeepCopy(emptyPolyData);

        activationPolyData = new vtkPolyData();
        activationPolyData.DeepCopy(emptyPolyData);

        lineActivationMapper = new vtkPolyDataMapper();
        lineActivationMapper.SetInputData(activationPolyData);
        lineActivationMapper.Update();

        lineActivationActor.SetMapper(lineActivationMapper);
        lineActivationActor.Modified();

        actors.add(lineActivationActor);
    }

    protected String getType()
    {
        return LINES;
    }

    public Element toXmlDomElement(Document dom)
    {
        Element rootEle = dom.createElement(getType());
        rootEle.setAttribute(SHAPE_MODEL_NAME, smallBodyModel.getModelName());

        for (Line lin : this.lines)
        {
            rootEle.appendChild(lin.toXmlDomElement(dom));
        }

        return rootEle;
    }

    public void fromXmlDomElement(Element element, boolean append)
    {
        if (!append)
            this.lines.clear();

        String shapeModelName = null;
        if (element.hasAttribute(SHAPE_MODEL_NAME))
            shapeModelName= element.getAttribute(SHAPE_MODEL_NAME);

        Line dummyLine = (Line) createStructure(smallBodyModel);
        NodeList nl = element.getElementsByTagName(dummyLine.getType());
        if(nl != null && nl.getLength() > 0)
        {
            for(int i = 0 ; i < nl.getLength();i++)
            {
                Element el = (Element)nl.item(i);

                Line lin = (Line)createStructure(smallBodyModel);

                lin.fromXmlDomElement(el, shapeModelName, append);

                this.lines.add(lin);
            }
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    protected void updatePolyData()
    {
        linesPolyData.DeepCopy(emptyPolyData);
        vtkPoints points = linesPolyData.GetPoints();
        vtkCellArray lineCells = linesPolyData.GetLines();
        vtkCellData cellData = linesPolyData.GetCellData();
        vtkUnsignedCharArray colors = (vtkUnsignedCharArray)cellData.GetScalars();

        int c=0;
        for (int j=0; j<this.lines.size(); ++j)
        {
            Line lin = this.lines.get(j);

            int[] color = lin.color;

            if (Arrays.binarySearch(this.selectedStructures, j) >= 0)
                color = getCommonData().getSelectionColor();

            int size = lin.xyzPointList.size();
            if (mode == Mode.CLOSED && size > 2)
                idList.SetNumberOfIds(size + 1);
            else
                idList.SetNumberOfIds(size);

            int startId = 0;
            for (int i=0;i<size;++i)
            {
                if (i == 0)
                    startId = c;

                points.InsertNextPoint(lin.xyzPointList.get(i).xyz);
                if (lin.hidden)
                    idList.SetId(i, 0); // set to degenerate line if hidden
                else
                    idList.SetId(i, c);
                ++c;
            }

            if (mode == Mode.CLOSED && size > 2)
            {
                if (lin.hidden)
                    idList.SetId(size, 0);
                else
                    idList.SetId(size, startId);
            }

            lineCells.InsertNextCell(idList);
            colors.InsertNextTuple4(color[0],color[1],color[2],255);
        }

        smallBodyModel.shiftPolyLineInNormalDirection(linesPolyData, offset);

        if (lineMapper == null)
            lineMapper = new vtkPolyDataMapper();
        lineMapper.SetInputData(linesPolyData);
        lineMapper.Update();

        if (!actors.contains(lineActor))
            actors.add(lineActor);

        lineActor.SetMapper(lineMapper);
        lineActor.Modified();
    }

    public List<vtkProp> getProps()
    {
        return actors;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
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

    public Line getActivatedLine()
    {
        if (activatedLine >= 0 && activatedLine < lines.size())
            return lines.get(activatedLine);
        else
            return null;
    }

    public int getActivatedStructureIndex()
    {
        return activatedLine;
    }

    public vtkActor getStructureActor()
    {
        return lineActor;
    }

    public vtkActor getActivationActor()
    {
        return lineActivationActor;
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
            numberOfPoints += lin.controlPoints.size();
        }
        return numberOfPoints;
    }

    public void addNewStructure()
    {
        Line lin = (Line)createStructure(smallBodyModel);
        lines.add(lin);
        activateStructure(lines.size()-1);
        this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
    }

    public void updateActivatedStructureVertex(int vertexId, double[] newPoint)
    {
        Line lin = lines.get(activatedLine);

        int numVertices = lin.controlPoints.size();

        LatLon ll = MathUtil.reclat(newPoint);
        lin.controlPoints.set(vertexId, ll);

        // If we're modifying the last vertex
        if (vertexId == numVertices - 1)
        {
            lin.updateSegment(vertexId-1);
            if (mode == Mode.CLOSED)
                lin.updateSegment(vertexId);
        }
        // If we're modifying the first vertex
        else if (vertexId == 0)
        {
            if (mode == Mode.CLOSED)
                lin.updateSegment(numVertices-1);
            lin.updateSegment(vertexId);
        }
        // If we're modifying a middle vertex
        else
        {
            lin.updateSegment(vertexId-1);
            lin.updateSegment(vertexId);
        }

        updatePolyData();

        updateLineActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.VERTEX_POSITION_CHANGED, null, activatedLine);
    }

    public void insertVertexIntoActivatedStructure(double[] newPoint)
    {
        if (activatedLine < 0)
            return;

        Line lin = lines.get(activatedLine);

        if (lin.controlPointIds.size() == maximumVerticesPerLine)
            return;

        if (currentLineVertex < -1 || currentLineVertex >= lin.controlPointIds.size())
            System.out.println("Error: currentLineVertex is invalid");

        LatLon ll = MathUtil.reclat(newPoint);

        lin.controlPoints.add(currentLineVertex+1, ll);

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
                if (mode == Mode.CLOSED)
                    lin.updateSegment(currentLineVertex+1);
            }
        }

        ++currentLineVertex;

        updatePolyData();

        updateLineActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.VERTEX_INSERTED_INTO_LINE, null, activatedLine);
    }


    public void removeCurrentStructureVertex()
    {
        Line lin = lines.get(activatedLine);

        if (currentLineVertex < 0 || currentLineVertex >= lin.controlPointIds.size())
            return;

        int vertexId = currentLineVertex;

        lin.controlPoints.remove(vertexId);

        // If not in CLOSED mode:
        // If one of the end points is being removed, then we only need to remove the line connecting the
        // end point to the adjacent point. If we're removing a non-end point, we need to remove the line
        // segments connecting the 2 adjacent control points and in addition, we need to draw a new line
        // connecting the 2 adjacent control points.
        //
        // But if in CLOSED mode:
        // We always need to remove 2 adjacent segments to the control point that was removed and draw a
        // new line connecting the 2 adjacent control point.
        if (lin.controlPointIds.size() > 1)
        {
            // Remove initial point
            if (vertexId == 0)
            {
                int id2 = lin.controlPointIds.get(vertexId+1);
                int numberPointsRemoved = id2;
                for (int i=0; i<numberPointsRemoved; ++i)
                {
                    lin.xyzPointList.remove(0);
                }
                lin.controlPointIds.remove(vertexId);

                for (int i=0; i<lin.controlPointIds.size(); ++i)
                    lin.controlPointIds.set(i, lin.controlPointIds.get(i) - numberPointsRemoved);

                if (mode == Mode.CLOSED)
                {
                    int id = lin.controlPointIds.get(lin.controlPointIds.size()-1);
                    numberPointsRemoved = lin.xyzPointList.size()-id-1;;
                    for (int i=0; i<numberPointsRemoved; ++i)
                    {
                        lin.xyzPointList.remove(id+1);
                    }

                    // redraw segment connecting last point to first
                    lin.updateSegment(lin.controlPointIds.size()-1);
                }
            }
            // Remove final point
            else if (vertexId == lin.controlPointIds.size()-1)
            {
                if (mode == Mode.CLOSED)
                {
                    int id = lin.controlPointIds.get(lin.controlPointIds.size()-1);
                    int numberPointsRemoved = lin.xyzPointList.size()-id-1;;
                    for (int i=0; i<numberPointsRemoved; ++i)
                    {
                        lin.xyzPointList.remove(id+1);
                    }
                }

                int id1 = lin.controlPointIds.get(vertexId-1);
                int id2 = lin.controlPointIds.get(vertexId);
                int numberPointsRemoved = id2-id1;
                for (int i=0; i<numberPointsRemoved; ++i)
                {
                    lin.xyzPointList.remove(id1+1);
                }
                lin.controlPointIds.remove(vertexId);

                if (mode == Mode.CLOSED)
                {
                    // redraw segment connecting last point to first
                    lin.updateSegment(lin.controlPointIds.size()-1);
                }
            }
            // Remove a middle point
            else
            {
                // Remove points BETWEEN the 2 adjacent control points
                int id1 = lin.controlPointIds.get(vertexId-1);
                int id2 = lin.controlPointIds.get(vertexId+1);
                int numberPointsRemoved = id2-id1-1;
                for (int i=0; i<numberPointsRemoved; ++i)
                {
                    lin.xyzPointList.remove(id1+1);
                }
                lin.controlPointIds.remove(vertexId);

                for (int i=vertexId; i<lin.controlPointIds.size(); ++i)
                    lin.controlPointIds.set(i, lin.controlPointIds.get(i) - numberPointsRemoved);

                lin.updateSegment(vertexId-1);
            }
        }
        else if (lin.controlPointIds.size() == 1)
        {
            lin.controlPointIds.remove(vertexId);
            lin.xyzPointList.clear();
        }

        --currentLineVertex;
        if (currentLineVertex < 0 && lin.controlPointIds.size() > 0)
            currentLineVertex = 0;

        updatePolyData();

        updateLineActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.VERTEX_REMOVED_FROM_LINE, null, activatedLine);
    }


    public void removeStructure(int cellId)
    {
        int id =lines.get(cellId).labelId;
        if(id!=-1)
            actors.remove(id);

        lines.remove(cellId);

        updatePolyData();

        if (hasProfileMode())
            updateLineActivation();

        if (cellId == activatedLine)
            activateStructure(-1);
        else
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

        this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, cellId);
    }

    public void removeStructures(int[] indices)
    {
        if (indices == null || indices.length == 0)
            return;

        Arrays.sort(indices);
        for (int i=indices.length-1; i>=0; --i)
        {
            int id =lines.get(indices[i]).labelId;
            if(id!=-1)
                actors.remove(id);
            lines.remove(indices[i]);
            this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, indices[i]);
        }

        updatePolyData();

        if (hasProfileMode())
            updateLineActivation();

        if (Arrays.binarySearch(indices, activatedLine) < 0)
            activateStructure(-1);
        else
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllStructures()
    {
        lines.clear();

        for(int i =0;i<actors.size();i++)
        {
            if(actors.get(i) instanceof vtkCaptionActor2D)
            {
                actors.remove(i);
                i--;
            }

        }

        updatePolyData();

        if (hasProfileMode())
            updateLineActivation();

        activateStructure(-1);

        this.pcs.firePropertyChange(Properties.ALL_STRUCTURES_REMOVED, null, null);
    }

    public void moveActivationVertex(int vertexId, double[] newPoint)
    {
        vtkPoints points = activationPolyData.GetPoints();
        points.SetPoint(vertexId, newPoint);
        activationPolyData.Modified();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    protected void updateLineActivation()
    {
        if (hasProfileMode())
        {
            activationPolyData.DeepCopy(emptyPolyData);
            vtkPoints points = activationPolyData.GetPoints();
            vtkCellArray vert = activationPolyData.GetVerts();
            vtkCellData cellData = activationPolyData.GetCellData();
            vtkUnsignedCharArray colors = (vtkUnsignedCharArray)cellData.GetScalars();

            idList.SetNumberOfIds(1);

            int count = 0;
            int numLines = getNumberOfStructures();
            for (int j=0; j<numLines; ++j)
            {
                Line lin = lines.get(j);

                for (int i=0; i<lin.controlPointIds.size(); ++i)
                {
                    int idx = lin.controlPointIds.get(i);

                    points.InsertNextPoint(lin.xyzPointList.get(idx).xyz);
                    idList.SetId(0, count++);
                    vert.InsertNextCell(idList);
                    if (i == 0)
                        colors.InsertNextTuple4(greenColor[0],greenColor[1],greenColor[2],greenColor[3]);
                    else
                        colors.InsertNextTuple4(redColor[0],redColor[1],redColor[2],redColor[3]);
                }
            }

            smallBodyModel.shiftPolyLineInNormalDirection(activationPolyData,
                    smallBodyModel.getMinShiftAmount());

        }
        else
        {
            if (activatedLine == -1)
            {
                if (actors.contains(lineActivationActor))
                    actors.remove(lineActivationActor);

                return;
            }

            Line lin = lines.get(activatedLine);

            activationPolyData.DeepCopy(emptyPolyData);
            vtkPoints points = activationPolyData.GetPoints();
            vtkCellArray vert = activationPolyData.GetVerts();
            vtkCellData cellData = activationPolyData.GetCellData();
            vtkUnsignedCharArray colors = (vtkUnsignedCharArray)cellData.GetScalars();

            int numPoints = lin.controlPointIds.size();

            points.SetNumberOfPoints(numPoints);

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

            smallBodyModel.shiftPolyLineInNormalDirection(activationPolyData, offset);

            if (!actors.contains(lineActivationActor))
                actors.add(lineActivationActor);
        }
    }

    public void activateStructure(int cellId)
    {
        if (activatedLine == cellId)
            return;

        activatedLine = cellId;

        if (cellId >= 0)
        {
            Line lin = lines.get(activatedLine);
            currentLineVertex = lin.controlPointIds.size()-1;
        }
        else
        {
            currentLineVertex = -1000;
        }

        updateLineActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void selectCurrentStructureVertex(int idx)
    {
        currentLineVertex = idx;

        updateLineActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void loadModel(File file, boolean append) throws Exception
    {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();

        //parse using builder to get DOM representation of the XML file
        Document dom = db.parse(file);

        //get the root element
        Element docEle = dom.getDocumentElement();

        if (getType().equals(docEle.getTagName()))
            fromXmlDomElement(docEle, append);
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

    public boolean supportsActivation()
    {
        return true;
    }

    public int getStructureIndexFromCellId(int cellId, vtkProp prop)
    {
        if (prop == lineActor)
            return cellId;
        else if (prop == lineActivationActor)
            return activatedLine;
        else
            return -1;
    }

    public void selectStructures(int[] indices)
    {
        this.selectedStructures = indices.clone();
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public int[] getSelectedStructures()
    {
        return selectedStructures;
    }

    public void redrawAllStructures()
    {
        for (Line lin : this.lines)
        {
            for (int i=0; i<lin.controlPointIds.size(); ++i)
                lin.shiftPointOnPathToClosestPointOnAsteroid(i);

            for (int i=0; i<lin.controlPointIds.size()-1; ++i)
                lin.updateSegment(i);

            if (mode == Mode.CLOSED)
                lin.updateSegment(lin.controlPoints.size()-1);
        }

        updatePolyData();

        updateLineActivation();

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
        this.pcs.firePropertyChange(Properties.COLOR_CHANGED, null, idx);
    }

    protected vtkPolyData getActivationPolyData()
    {
        return activationPolyData;
    }

    protected vtkPolyData getEmptyPolyData()
    {
        return emptyPolyData;
    }

    public boolean hasProfileMode()
    {
        return mode == Mode.PROFILE;
    }

    /**
     * PROFILE MODE ONLY!!
     * Get the vertex id of the line the specified vertex belongs.
     * Only 0 or 1 can be returned.
     * @param idx
     * @return
     */
    public int getVertexIdFromActivationCellId(int idx)
    {
        int numLines = getNumberOfStructures();
        for (int j=0; j<numLines; ++j)
        {
            Line lin = lines.get(j);
            int size = lin.controlPointIds.size();

            if (idx == 0)
            {
                return 0;
            }
            else if (idx == 1 && size == 2)
            {
                return 1;
            }
            else
            {
                idx -= size;
            }
        }

        return -1;
    }

    /**
     * PROFILE MODE ONLY!!
     * Get which line the specified vertex belongs to
     * @param idx
     * @return
     */
    public int getStructureIdFromActivationCellId(int idx)
    {
        int count = 0;
        int numLines = getNumberOfStructures();
        for (int j=0; j<numLines; ++j)
        {
            Line lin = lines.get(j);
            int size = lin.controlPointIds.size();
            count += size;
            if (idx < count)
                return j;
        }

        return -1;
    }

    public double getDefaultOffset()
    {
        return 5.0*smallBodyModel.getMinShiftAmount();
    }

    public void setOffset(double offset)
    {
        this.offset = offset;

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getOffset()
    {
        return offset;
    }

    public void generateProfile(ArrayList<Point3D> xyzPointList, ArrayList<Double> profileValues,
            ArrayList<Double> profileDistances, int coloringIndex) throws Exception
    {
        profileValues.clear();
        profileDistances.clear();

        // For each point in xyzPointList, find the cell containing that
        // point and then, using barycentric coordinates find the value
        // of the height at that point
        //
        // To compute the distance, assume we have a straight line connecting the first
        // and last points of xyzPointList. For each point, p, in xyzPointList, find the point
        // on the line closest to p. The distance from p to the start of the line is what
        // is placed in heights. Use SPICE's nplnpt function for this.

        double[] first = xyzPointList.get(0).xyz;
        double[] last = xyzPointList.get(xyzPointList.size()-1).xyz;
        double[] lindir = new double[3];
        lindir[0] = last[0] - first[0];
        lindir[1] = last[1] - first[1];
        lindir[2] = last[2] - first[2];

        // The following can be true if the user clicks on the same point twice
        boolean zeroLineDir = MathUtil.vzero(lindir);

        double[] pnear = new double[3];
        double[] notused = new double[1];

        double distance = 0.0;
        double val = 0.0;

        for (Point3D p : xyzPointList)
        {
            distance = 0.0;
            if (!zeroLineDir)
            {
                MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
                distance = 1000.0 * MathUtil.distanceBetween(first, pnear);
            }

            // Save out the distance
            profileDistances.add(distance);

            // Save out the plate coloring value
            val = 1000.0 * smallBodyModel.getColoringValue(coloringIndex, p.xyz);
            profileValues.add(val);
        }
    }

    /**
     * Save out a file which contains the value of the various coloring data
     * as a function of distance along the profile. A profile is path with
     * only 2 control points.
     */
    @Override
    public void saveProfile(int cellId, File file) throws Exception
    {
        Line lin = this.lines.get(cellId);

        if (lin.controlPointIds.size() != 2)
            throw new Exception("Line must contain exactly 2 control points.");

        final String lineSeparator = System.getProperty("line.separator");

        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        // write header
        out.write("Distance (m)");
        out.write(",X (m)");
        out.write(",Y (m)");
        out.write(",Z (m)");
        out.write(",Latitude (deg)");
        out.write(",Longitude (deg)");
        out.write(",Radius (m)");

        int numColors = smallBodyModel.getNumberOfColors();
        for (int i=0; i<numColors; ++i)
        {
            out.write("," + smallBodyModel.getColoringName(i));
            String units = smallBodyModel.getColoringUnits(i);
            if (units != null && !units.isEmpty())
                out.write(" (" + units + ")");
        }
        out.write(lineSeparator);


        ArrayList<Point3D> xyzPointList = lin.xyzPointList;

        // For each point in xyzPointList, find the cell containing that
        // point and then, using barycentric coordinates find the value
        // of the height at that point
        //
        // To compute the distance, assume we have a straight line connecting the first
        // and last points of xyzPointList. For each point, p, in xyzPointList, find the point
        // on the line closest to p. The distance from p to the start of the line is what
        // is placed in heights. Use SPICE's nplnpt function for this.

        double[] first = xyzPointList.get(0).xyz;
        double[] last = xyzPointList.get(xyzPointList.size()-1).xyz;
        double[] lindir = new double[3];
        lindir[0] = last[0] - first[0];
        lindir[1] = last[1] - first[1];
        lindir[2] = last[2] - first[2];

        // The following can be true if the user clicks on the same point twice
        boolean zeroLineDir = MathUtil.vzero(lindir);

        double[] pnear = new double[3];
        double[] notused = new double[1];

        for (Point3D p : xyzPointList)
        {
            double distance = 0.0;
            if (!zeroLineDir)
            {
                MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
                distance = 1000.0 * MathUtil.distanceBetween(first, pnear);
            }

            out.write(String.valueOf(distance));

            double[] vals = smallBodyModel.getAllColoringValues(p.xyz);

            out.write("," + 1000.0 * p.xyz[0]);
            out.write("," + 1000.0 * p.xyz[1]);
            out.write("," + 1000.0 * p.xyz[2]);

            LatLon llr = MathUtil.reclat(p.xyz).toDegrees();
            out.write("," + llr.lat);
            out.write("," + llr.lon);
            out.write("," + 1000.0 * llr.rad);

            for (double val : vals)
                out.write("," + val);

            out.write(lineSeparator);
        }

        out.close();
    }

    /**
     * Similar to the saveProfile function but this one uses the gravity program
     * directly to compute the slope, elevation, acceleration, and potential columns
     * rather than simply getting the value from the plate data. It also has
     * a third argument for passing in a different shape model to use which is useful
     * for saving profiles on mapmaker maplets since we can't run the gravity program
     * on a maplet. It also does not save out any of the plate data.
     *
     * @param cellId
     * @param file
     * @param otherSmallBodyModel - use this small body for running gravity program. If null
     *                              use small body model passed into constructor.
     * @throws Exception
     */
    public void saveProfileUsingGravityProgram(int cellId, File file, SmallBodyModel otherSmallBodyModel) throws Exception
    {
        if (otherSmallBodyModel == null)
            otherSmallBodyModel = this.smallBodyModel;

        Line lin = this.lines.get(cellId);

        if (lin.controlPointIds.size() != 2)
            throw new Exception("Line must contain exactly 2 control points.");

        final String lineSeparator = System.getProperty("line.separator");

        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        // write header
        out.write("Distance (m)");
        out.write(",X (m)");
        out.write(",Y (m)");
        out.write(",Z (m)");
        out.write(",Latitude (deg)");
        out.write(",Longitude (deg)");
        out.write(",Radius (m)");

        out.write(",Slope (deg)");
        out.write(",Elevation (m)");
        out.write(",Gravitational Acceleration (m/s^2)");
        out.write(",Gravitational Potential (J/kg)");

        out.write(lineSeparator);


        ArrayList<Point3D> xyzPointList = lin.xyzPointList;

        // Run the gravity program
        ArrayList<Double> elevation = new ArrayList<Double>();
        ArrayList<Double> accelerationMagnitude = new ArrayList<Double>();
        ArrayList<Point3D> accelerationVector = new ArrayList<Point3D>();
        ArrayList<Double> potential = new ArrayList<Double>();
        ArrayList<double[]> pointList = new ArrayList<double[]>();
        for (Point3D p : lin.xyzPointList)
            pointList.add(p.xyz);
        Gravity.getGravityAtPoints(
                pointList,
                smallBodyModel.getDensity(),
                smallBodyModel.getRotationRate(),
                smallBodyModel.getReferencePotential(),
                smallBodyModel.getSmallBodyPolyData(),
                elevation,
                accelerationMagnitude,
                accelerationVector,
                potential);

        // To compute the distance, assume we have a straight line connecting the first
        // and last points of xyzPointList. For each point, p, in xyzPointList, find the point
        // on the line closest to p. The distance from p to the start of the line is what
        // is placed in heights. Use SPICE's nplnpt function for this.

        double[] first = xyzPointList.get(0).xyz;
        double[] last = xyzPointList.get(xyzPointList.size()-1).xyz;
        double[] lindir = new double[3];
        lindir[0] = last[0] - first[0];
        lindir[1] = last[1] - first[1];
        lindir[2] = last[2] - first[2];

        // The following can be true if the user clicks on the same point twice
        boolean zeroLineDir = MathUtil.vzero(lindir);

        double[] pnear = new double[3];
        double[] notused = new double[1];

        int i = 0;
        for (Point3D p : xyzPointList)
        {
            double distance = 0.0;
            if (!zeroLineDir)
            {
                MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
                distance = 1000.0 * MathUtil.distanceBetween(first, pnear);
            }

            out.write(String.valueOf(distance));

            out.write("," + 1000.0 * p.xyz[0]);
            out.write("," + 1000.0 * p.xyz[1]);
            out.write("," + 1000.0 * p.xyz[2]);

            LatLon llr = MathUtil.reclat(p.xyz).toDegrees();
            out.write("," + llr.lat);
            out.write("," + llr.lon);
            out.write("," + 1000.0 * llr.rad);

            // compute slope. Note to get the normal, use the smallBodyModel passed into constructor of
            // this class, not the smallBodyModel passed into this function.
            // The slope is the angular separation between the (negative) acceleration vector and
            // the normal vector.
            double[] normal = this.smallBodyModel.getClosestNormal(p.xyz);
            double[] accVector = accelerationVector.get(i).xyz;
            accVector[0] = -accVector[0];
            accVector[1] = -accVector[1];
            accVector[2] = -accVector[2];
            double slope = MathUtil.vsep(normal, accVector) * 180.0 / Math.PI;

            out.write("," + slope);
            out.write("," + elevation.get(i));
            out.write("," + accelerationMagnitude.get(i));
            out.write("," + potential.get(i));

            out.write(lineSeparator);
            ++i;
        }

        out.close();
    }

    @Override
    public double getLineWidth()
    {
        vtkProperty lineProperty = lineActor.GetProperty();
        return lineProperty.GetLineWidth();
    }

    @Override
    public void setLineWidth(double width)
    {
        if (width >= 1.0)
        {
            vtkProperty lineProperty = lineActor.GetProperty();
            lineProperty.SetLineWidth(width);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void setVisible(boolean b)
    {
        boolean needToUpdate = false;
        for (Line line : lines)
        {
            if (line.hidden == b)
            {
                actors.get(line.labelId).VisibilityOn();
                line.hidden = !b;
                needToUpdate = true;
            }
        }
        if (needToUpdate)
        {
            updatePolyData();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }

        lineActor.SetVisibility(b ? 1 : 0);
        lineActivationActor.SetVisibility(b ? 1 : 0);
        super.setVisible(b);
    }

    @Override
    public void setStructuresHidden(int[] lineIds, boolean hidden)
    {
        for (int i=0; i<lineIds.length; ++i)
        {
            vtkProp a = actors.get(lines.get(i).labelId);
            a.SetVisibility(1-a.GetVisibility());
            Line line = lines.get(lineIds[i]);
            line.hidden = hidden;
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public boolean isStructureHidden(int id)
    {
        return lines.get(id).hidden;
    }

    @Override
    public double[] getStructureCenter(int id)
    {
        return lines.get(id).getCentroid();
    }

    @Override
    public double[] getStructureNormal(int id)
    {
        double[] center = getStructureCenter(id);
        return smallBodyModel.getNormalAtPoint(center);
    }

    @Override
    public double getStructureSize(int id)
    {
        return lines.get(id).getSize();
    }

    protected StructureModel.Structure createStructure(SmallBodyModel smallBodyModel)
    {
        return new Line(smallBodyModel, false, ++maxPolygonId);
    }

    @Override
    public boolean setStructureLabel(int idx, String label)
    {
        if(lines.get(idx).xyzPointList.isEmpty())
            return false;
        lines.get(idx).setLabel(label);
        int numLetters = label.length();
        if(lines.get(idx).editingLabel)
        {
            if(label==null||label.equals(""))
            {
                actors.get(lines.get(idx).labelId).VisibilityOff();
            }
            else
            {
                int l=lines.get(idx).labelId;
                vtkProp prop = actors.get(l);
                ((vtkCaptionActor2D)prop).SetCaption(label);
                ((vtkCaptionActor2D)prop).SetPosition2(0.04, numLetters*0.02);
            }
        }
        else
        {
            if(label==null||label.equals(""))
            {
                return true;
            }
            vtkCaptionActor2D v = new vtkCaptionActor2D();
            v.GetCaptionTextProperty().SetColor(1.0, 0.1, 0.2);
            v.GetCaptionTextProperty().SetJustificationToCentered();
            v.GetCaptionTextProperty().BoldOn();
            v.VisibilityOn();
            v.BorderOff();
            v.GetCaptionTextProperty().SetFontSize(-100);
            v.ThreeDimensionalLeaderOn();
            for (int index : selectedStructures)
            {
                v.SetAttachmentPoint(lines.get(index).getCentroid());
                v.SetPosition(0, 0);
                v.SetPosition2(0.04, numLetters*0.02);
                v.SetCaption(lines.get(index).getLabel());
                actors.add(v);
                lines.get(index).labelId=(actors.size()-1);
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, index);
            }
            lines.get(idx).editingLabel=true;
        }
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, idx);
        return true;
    }

    public void showLabel(int index)
    {
        if(lines.get(index).getLabel().equals(""))
        {
            JOptionPane.showMessageDialog(null, "Please name the structure!");
            return;
        }
        int l=lines.get(index).labelId;
        vtkProp prop = actors.get(l);
        prop.SetVisibility(1-prop.GetVisibility());

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, index);


    }
}
