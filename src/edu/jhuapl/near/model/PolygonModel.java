package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

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
import vtk.vtkAppendPolyData;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

/**
 * Model of polygon structures drawn on a body.
 */
public class PolygonModel extends ControlPointsStructureModel implements PropertyChangeListener
{
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    private vtkPolyData activationPolyData;

    private vtkPolyData boundaryPolyData;
    private vtkAppendPolyData boundaryAppendFilter;
    private vtkPolyDataMapper boundaryMapper;
    private vtkActor boundaryActor;

    private vtkPolyData interiorPolyData;
    private vtkAppendPolyData interiorAppendFilter;
    private vtkPolyDataMapper interiorMapper;
    private vtkActor interiorActor;

    private vtkUnsignedCharArray boundaryColors;
    private vtkUnsignedCharArray interiorColors;

    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper polygonActivationMapper;
    private vtkActor polygonActivationActor;

    private SmallBodyModel smallBodyModel;
    private int activatedPolygon = -1;
    private int currentPolygonVertex = -1000;
    private int[] selectedStructures = {};
    private int maximumVerticesPerPolygon = Integer.MAX_VALUE;
    private vtkIdList idList;

    private double interiorOpacity = 0.3;

    private vtkPolyData emptyPolyData;

    private double offset;

    private static final String POLYGONS = "polygons";
    private static final String SHAPE_MODEL_NAME = "shapemodel";
    private static final int[] redColor = {255, 0, 0, 255}; // RGBA red
    private static final int[] blueColor = {0, 0, 255, 255}; // RGBA blue

    public PolygonModel(SmallBodyModel smallBodyModel)
    {
        super(ModelNames.POLYGON_STRUCTURES);

        this.smallBodyModel = smallBodyModel;

        this.offset = getDefaultOffset();

        this.smallBodyModel.addPropertyChangeListener(this);

        idList = new vtkIdList();

        boundaryColors = new vtkUnsignedCharArray();
        boundaryColors.SetNumberOfComponents(3);

        interiorColors = new vtkUnsignedCharArray();
        interiorColors.SetNumberOfComponents(3);

        boundaryPolyData = new vtkPolyData();
        boundaryAppendFilter = new vtkAppendPolyData();
        boundaryAppendFilter.UserManagedInputsOn();
        boundaryMapper = new vtkPolyDataMapper();
        boundaryActor = new vtkActor();
        vtkProperty boundaryProperty = boundaryActor.GetProperty();
        boundaryProperty.LightingOff();
        boundaryProperty.SetLineWidth(2.0);

        actors.add(boundaryActor);

        interiorPolyData = new vtkPolyData();
        interiorAppendFilter = new vtkAppendPolyData();
        interiorAppendFilter.UserManagedInputsOn();
        interiorMapper = new vtkPolyDataMapper();
        interiorActor = new vtkActor();
        vtkProperty interiorProperty = interiorActor.GetProperty();
        interiorProperty.LightingOff();
        interiorProperty.SetOpacity(interiorOpacity);

        actors.add(interiorActor);

        polygonActivationActor = new vtkActor();
        vtkProperty polygonActivationProperty = polygonActivationActor.GetProperty();
        polygonActivationProperty.SetColor(1.0, 0.0, 0.0);
        polygonActivationProperty.SetPointSize(7.0);

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

        activationPolyData = new vtkPolyData();
        activationPolyData.DeepCopy(emptyPolyData);

        polygonActivationMapper = new vtkPolyDataMapper();
        polygonActivationMapper.SetInput(activationPolyData);
        polygonActivationMapper.Update();

        polygonActivationActor.SetMapper(polygonActivationMapper);
        polygonActivationActor.Modified();

        actors.add(polygonActivationActor);
    }

    public Element toXmlDomElement(Document dom)
    {
        Element rootEle = dom.createElement(POLYGONS);
        rootEle.setAttribute(SHAPE_MODEL_NAME, smallBodyModel.getModelName());

        for (Polygon pol : this.polygons)
        {
            rootEle.appendChild(pol.toXmlDomElement(dom));
        }

        return rootEle;
    }

    public void fromXmlDomElement(Element element, boolean append)
    {
        if (!append)
            this.polygons.clear();

        String shapeModelName = null;
        if (element.hasAttribute(SHAPE_MODEL_NAME))
            shapeModelName= element.getAttribute(SHAPE_MODEL_NAME);

        NodeList nl = element.getElementsByTagName(Polygon.POLYGON);
        if(nl != null && nl.getLength() > 0)
        {
            for(int i = 0 ; i < nl.getLength();i++)
            {
                Element el = (Element)nl.item(i);

                Polygon pol = new Polygon(smallBodyModel);

                pol.fromXmlDomElement(el, shapeModelName, append);

                this.polygons.add(pol);
            }
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private void updatePolyData()
    {
        if (polygons.size() > 0)
        {
            boundaryAppendFilter.SetNumberOfInputs(polygons.size());
            interiorAppendFilter.SetNumberOfInputs(polygons.size());

            for (int i=0; i<polygons.size(); ++i)
            {
                vtkPolyData poly = polygons.get(i).boundaryPolyData;
                if (poly != null)
                    boundaryAppendFilter.SetInputByNumber(i, poly);
                poly = polygons.get(i).interiorPolyData;
                if (poly != null)
                    interiorAppendFilter.SetInputByNumber(i, poly);
            }

            boundaryAppendFilter.Update();
            interiorAppendFilter.Update();

            vtkPolyData boundaryAppendFilterOutput = boundaryAppendFilter.GetOutput();
            vtkPolyData interiorAppendFilterOutput = interiorAppendFilter.GetOutput();
            boundaryPolyData.DeepCopy(boundaryAppendFilterOutput);
            interiorPolyData.DeepCopy(interiorAppendFilterOutput);

            smallBodyModel.shiftPolyLineInNormalDirection(boundaryPolyData, offset);
            PolyDataUtil.shiftPolyDataInNormalDirection(interiorPolyData, offset);

            boundaryColors.SetNumberOfTuples(boundaryPolyData.GetNumberOfCells());
            interiorColors.SetNumberOfTuples(interiorPolyData.GetNumberOfCells());
            for (int i=0; i<polygons.size(); ++i)
            {
                int[] color = polygons.get(i).color;

                if (Arrays.binarySearch(this.selectedStructures, i) >= 0)
                    color = getCommonData().getSelectionColor();

                IdPair range = this.getCellIdRangeOfPolygon(i, false);
                for (int j=range.id1; j<range.id2; ++j)
                    boundaryColors.SetTuple3(j, color[0], color[1], color[2]);

                range = this.getCellIdRangeOfPolygon(i, true);
                for (int j=range.id1; j<range.id2; ++j)
                    interiorColors.SetTuple3(j, color[0], color[1], color[2]);
            }
            vtkCellData boundaryCellData = boundaryPolyData.GetCellData();
            vtkCellData interiorCellData = interiorPolyData.GetCellData();

            boundaryCellData.SetScalars(boundaryColors);
            interiorCellData.SetScalars(interiorColors);

            boundaryAppendFilterOutput.Delete();
            interiorAppendFilterOutput.Delete();
            boundaryCellData.Delete();
            interiorCellData.Delete();
        }
        else
        {
            boundaryPolyData.DeepCopy(emptyPolyData);
            interiorPolyData.DeepCopy(emptyPolyData);
        }


        boundaryMapper.SetInput(boundaryPolyData);
        boundaryMapper.Update();
        interiorMapper.SetInput(interiorPolyData);
        interiorMapper.Update();

        boundaryActor.SetMapper(boundaryMapper);
        boundaryActor.Modified();
        interiorActor.SetMapper(interiorMapper);
        interiorActor.Modified();

    }

    public ArrayList<vtkProp> getProps()
    {
        return actors;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        if (prop == boundaryActor || prop == interiorActor)
        {
            int polygonId = this.getPolygonIdFromCellId(cellId, prop == interiorActor);
            Polygon pol = this.polygons.get(polygonId);
            return pol.getClickStatusBarText();
        }
        else
        {
            return "";
        }
    }

    public int getNumberOfStructures()
    {
        return polygons.size();
    }

    public Structure getStructure(int cellId)
    {
        return polygons.get(cellId);
    }

    public Polygon getActivatedPolygon()
    {
        if (activatedPolygon >= 0 && activatedPolygon < polygons.size())
            return polygons.get(activatedPolygon);
        else
            return null;
    }

    public int getActivatedStructureIndex()
    {
        return activatedPolygon;
    }

    public vtkActor getBoundaryActor()
    {
        return boundaryActor;
    }

    public vtkActor getInteriorActor()
    {
        return interiorActor;
    }

    public vtkActor getActivationActor()
    {
        return polygonActivationActor;
    }

    /**
     * Return the total number of control points in all the polygons combined.
     * @return
     */
    public int getTotalNumberOfPoints()
    {
        int numberOfPoints = 0;
        for (Polygon pol : this.polygons)
        {
            numberOfPoints += pol.controlPoints.size();
        }
        return numberOfPoints;
    }

    public void addNewStructure()
    {
        Polygon pol = new Polygon(smallBodyModel);
        polygons.add(pol);
        activateStructure(polygons.size()-1);
        this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
    }

    public void updateActivatedStructureVertex(int vertexId, double[] newPoint)
    {
        Polygon pol = polygons.get(activatedPolygon);

        LatLon ll = MathUtil.reclat(newPoint);
        pol.controlPoints.set(vertexId, ll);

        pol.updatePolygon(pol.controlPoints);

        updatePolyData();

        updatePolygonActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.VERTEX_POSITION_CHANGED, null, activatedPolygon);
    }


    public void insertVertexIntoActivatedStructure(double[] newPoint)
    {
        if (activatedPolygon < 0)
            return;

        Polygon pol = polygons.get(activatedPolygon);

        if (pol.controlPoints.size() == maximumVerticesPerPolygon)
            return;

        if (currentPolygonVertex < -1 || currentPolygonVertex >= pol.controlPoints.size())
            System.out.println("Error: currentPolygonVertex is invalid");

        LatLon ll = MathUtil.reclat(newPoint);

        pol.controlPoints.add(currentPolygonVertex+1, ll);

        pol.updatePolygon(pol.controlPoints);

        ++currentPolygonVertex;

        updatePolyData();

        updatePolygonActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.VERTEX_INSERTED_INTO_LINE, null, activatedPolygon);
    }


    public void removeCurrentStructureVertex()
    {
        Polygon pol = polygons.get(activatedPolygon);

        if (currentPolygonVertex < 0 || currentPolygonVertex >= pol.controlPoints.size())
            return;

        int vertexId = currentPolygonVertex;

        pol.controlPoints.remove(vertexId);

        pol.updatePolygon(pol.controlPoints);

        --currentPolygonVertex;
        if (currentPolygonVertex < 0 && pol.controlPoints.size() > 0)
            currentPolygonVertex = 0;

        updatePolyData();

        updatePolygonActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }


    public void removeStructure(int cellId)
    {
        polygons.remove(cellId);

        updatePolyData();

        if (cellId == activatedPolygon)
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
            polygons.remove(indices[i]);
            this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, indices[i]);
        }

        updatePolyData();

        if (Arrays.binarySearch(indices, activatedPolygon) < 0)
            activateStructure(-1);
        else
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllStructures()
    {
        polygons.clear();

        updatePolyData();

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

    protected void updatePolygonActivation()
    {
        if (activatedPolygon == -1)
        {
            if (actors.contains(polygonActivationActor))
                actors.remove(polygonActivationActor);

            return;
        }

        Polygon pol = polygons.get(activatedPolygon);

        activationPolyData.DeepCopy(emptyPolyData);
        vtkPoints points = activationPolyData.GetPoints();
        vtkCellArray vert = activationPolyData.GetVerts();
        vtkCellData cellData = activationPolyData.GetCellData();
        vtkUnsignedCharArray colors = (vtkUnsignedCharArray)cellData.GetScalars();

        int numPoints = pol.controlPoints.size();

        points.SetNumberOfPoints(numPoints);

        idList.SetNumberOfIds(1);

        for (int i=0; i<numPoints; ++i)
        {
            LatLon ll = pol.controlPoints.get(i);
            points.SetPoint(i, MathUtil.latrec(ll));
            idList.SetId(0, i);
            vert.InsertNextCell(idList);
            if (i == this.currentPolygonVertex)
                colors.InsertNextTuple4(blueColor[0],blueColor[1],blueColor[2],blueColor[3]);
            else
                colors.InsertNextTuple4(redColor[0],redColor[1],redColor[2],redColor[3]);
        }

        smallBodyModel.shiftPolyLineInNormalDirection(activationPolyData, offset);

        if (!actors.contains(polygonActivationActor))
            actors.add(polygonActivationActor);
    }

    public void activateStructure(int cellId)
    {
        if (activatedPolygon == cellId)
            return;

        activatedPolygon = cellId;

        if (cellId >= 0)
        {
            Polygon pol = polygons.get(activatedPolygon);
            currentPolygonVertex = pol.controlPoints.size()-1;
        }
        else
        {
            currentPolygonVertex = -1000;
        }

        updatePolygonActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void selectCurrentStructureVertex(int idx)
    {
        currentPolygonVertex = idx;

        updatePolygonActivation();

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

        if (PolygonModel.POLYGONS.equals(docEle.getTagName()))
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
        if (prop == boundaryActor)
        {
            return getPolygonIdFromBoundaryCellId(cellId);
        }
        else if (prop == interiorActor)
        {
            return getPolygonIdFromInteriorCellId(cellId);
        }
        else if (prop == polygonActivationActor)
        {
            return activatedPolygon;
        }

        return -1;
    }

    public void selectStructures(int[] indices)
    {
        this.selectedStructures = indices;
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public int[] getSelectedStructures()
    {
        return selectedStructures;
    }

    public void redrawAllStructures()
    {
        for (Polygon pol : this.polygons)
        {
            for (int i=0; i<pol.controlPoints.size(); ++i)
                pol.shiftPointOnPathToClosestPointOnAsteroid(i);

            for (int i=0; i<pol.controlPoints.size()-1; ++i)
                pol.updatePolygon(pol.controlPoints);
        }

        updatePolyData();

        updatePolygonActivation();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            redrawAllStructures();
        }
    }

    public void setMaximumVerticesPerPolygon(int max)
    {
        maximumVerticesPerPolygon = max;
    }

    public void setStructureColor(int idx, int[] color)
    {
        polygons.get(idx).setColor(color);
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

    /**
     * A picker picking the actor of this model will return a
     * cellId. But since there are many cells per RegularPolygon, we need to be
     * able to figure out which RegularPolygon was picked
     */
    private int getPolygonIdFromCellId(int cellId, boolean interior)
    {
        int numberCellsSoFar = 0;
        for (int i=0; i<polygons.size(); ++i)
        {
            if (interior)
                numberCellsSoFar += polygons.get(i).interiorPolyData.GetNumberOfCells();
            else
                numberCellsSoFar += polygons.get(i).boundaryPolyData.GetNumberOfCells();
            if (cellId < numberCellsSoFar)
                return i;
        }
        return -1;
    }

    public int getPolygonIdFromBoundaryCellId(int cellId)
    {
        return this.getPolygonIdFromCellId(cellId, false);
    }

    public int getPolygonIdFromInteriorCellId(int cellId)
    {
        return this.getPolygonIdFromCellId(cellId, true);
    }

    private IdPair getCellIdRangeOfPolygon(int polygonId, boolean interior)
    {
        int startCell = 0;
        for (int i=0; i<polygonId; ++i)
        {
            if (interior)
                startCell += polygons.get(i).interiorPolyData.GetNumberOfCells();
            else
                startCell += polygons.get(i).boundaryPolyData.GetNumberOfCells();
        }

        int endCell = startCell;
        if (interior)
            endCell += polygons.get(polygonId).interiorPolyData.GetNumberOfCells();
        else
            endCell += polygons.get(polygonId).boundaryPolyData.GetNumberOfCells();

        return new IdPair(startCell, endCell);
    }

    @Override
    public double getLineWidth()
    {
        vtkProperty boundaryProperty = boundaryActor.GetProperty();
        return boundaryProperty.GetLineWidth();
    }

    @Override
    public void setLineWidth(double width)
    {
        if (width >= 1.0)
        {
            vtkProperty boundaryProperty = boundaryActor.GetProperty();
            boundaryProperty.SetLineWidth(width);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void setVisible(boolean b)
    {
        boolean needToUpdate = false;
        for (Polygon pol : polygons)
        {
            if (pol.hidden == b)
            {
                pol.hidden = !b;
                pol.updatePolygon(pol.controlPoints);
                needToUpdate = true;
            }
        }
        if (needToUpdate)
        {
            updatePolyData();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }

        boundaryActor.SetVisibility(b ? 1 : 0);
        interiorActor.SetVisibility(b ? 1 : 0);
        polygonActivationActor.SetVisibility(b ? 1 : 0);
        super.setVisible(b);
    }

    public void savePlateDataInsideStructure(int idx, File file) throws IOException
    {
        vtkPolyData polydata = polygons.get(idx).interiorPolyData;
        smallBodyModel.savePlateDataInsidePolydata(polydata, file);
    }

    @Override
    public void setStructuresHidden(int[] polygonIds, boolean hidden)
    {
        for (int i=0; i<polygonIds.length; ++i)
        {
            Polygon pol = polygons.get(polygonIds[i]);
            if (pol.hidden != hidden)
            {
                pol.hidden = hidden;
                pol.updatePolygon(pol.controlPoints);
            }
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public boolean isStructureHidden(int id)
    {
        return polygons.get(id).hidden;
    }

    @Override
    public double[] getStructureCenter(int id)
    {
        return polygons.get(id).getCentroid();
    }
}
