package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdTypeArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;

/**
 * Model of regular polygon structures drawn on a body.
 *
 * @author
 *
 */

public class RegularPolygonModel extends StructureModel implements PropertyChangeListener
{
    private ArrayList<RegularPolygon> polygons = new ArrayList<RegularPolygon>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();

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

    private vtkPolyData emptyPolyData;
    private SmallBodyModel smallBodyModel;
    private double defaultRadius = 0.25; // radius for new polygons drawn
    private double maxRadius = 5.0;
    private int numberOfSides = 4;
    private int[] defaultColor = {0, 191, 255};
//    private int[] defaultBoundaryColor = {0, 191, 255};
//    private int[] defaultInteriorColor = {0, 191, 255};
    private double interiorOpacity = 0.3;
    private String type;
    private boolean isCircleModel = true;
    private int highlightedStructure = -1;
    private int[] highlightColor = {0, 0, 255};
    private int maxPolygonId = 0;

    public class RegularPolygon extends StructureModel.Structure
    {
        public String name = "default";
        public int id;

        public double[] center;
        public double radius;

        public vtkPolyData boundaryPolyData;
        public vtkPolyData interiorPolyData;
        public int numberOfSides;
        public String type;
        public int[] color;

        public RegularPolygon(int numberOfSides, String type, int[] color)
        {
            id = ++maxPolygonId;
            boundaryPolyData = new vtkPolyData();
            interiorPolyData = new vtkPolyData();
            this.numberOfSides = numberOfSides;
            this.type = type;
            this.color = (int[])color.clone();
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
            return type;
        }

        public String getInfo()
        {
            return "Diameter = " + 2.0*radius + " km";
        }

        public int[] getColor()
        {
            return color;
        }

        public void setColor(int[] color)
        {
            this.color = (int[])color.clone();
        }

        public vtkPolyData getBoundaryPolyData()
        {
            return boundaryPolyData;
        }

        public vtkPolyData getInteriorPolyData()
        {
            return interiorPolyData;
        }

        public void updatePolygon(SmallBodyModel sbModel, double[] center, double radius)
        {
            this.center = center;
            this.radius = radius;

            sbModel.drawPolygon(center, radius, numberOfSides, interiorPolyData, boundaryPolyData);
        }

        public String getClickStatusBarText()
        {
            return type + ", Id = " + id + ", Diameter = " + 2.0*radius + " km";
        }

    }

    public RegularPolygonModel(
            SmallBodyModel smallBodyModel,
            int numberOfSides,
            boolean isCircle,
            String type,
            String name)
    {
        super(name);

        this.smallBodyModel = smallBodyModel;

        defaultRadius = smallBodyModel.getBoundingBoxDiagonalLength() / 155.0;
        maxRadius = smallBodyModel.getBoundingBoxDiagonalLength() / 8.0;

        this.smallBodyModel.addPropertyChangeListener(this);

        emptyPolyData = new vtkPolyData();

        this.numberOfSides = numberOfSides;
        this.isCircleModel = isCircle;
        this.type = type;

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
        //interiorProperty.SetLineWidth(2.0);

        actors.add(interiorActor);
    }

    public void setDefaultColor(int[] color)
    {
        this.defaultColor = (int[])color.clone();
    }

    public int[] getDefaultColor()
    {
        return defaultColor;
    }

    public void setPolygonColor(int i, int[] color)
    {
        this.polygons.get(i).setColor(color);
    }

    public int[] getPolygonColor(int i)
    {
        return this.polygons.get(i).color;
    }

    /*
    public int[] getDefaultBoundaryColor()
    {
        return defaultBoundaryColor;
    }


    public void setDefaultBoundaryColor(int[] color)
    {
        this.defaultBoundaryColor = (int[])color.clone();
    }

    public int[] getDefaultInteriorColor()
    {
        return defaultInteriorColor;
    }

    public void setDefaultInteriorColor(int[] color)
    {
        this.defaultInteriorColor = (int[])color.clone();
    }
    */

    public double getInteriorOpacity()
    {
        return interiorOpacity;
    }

    public void setInteriorOpacity(double opacity)
    {
        this.interiorOpacity = opacity;
        interiorActor.GetProperty().SetOpacity(opacity);
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

            smallBodyModel.shiftPolyLineInNormalDirection(boundaryPolyData, 3.0);
            smallBodyModel.shiftPolyLineInNormalDirection(interiorPolyData, 2.0);

            boundaryColors.SetNumberOfTuples(boundaryPolyData.GetNumberOfCells());
            interiorColors.SetNumberOfTuples(interiorPolyData.GetNumberOfCells());
            for (int i=0; i<polygons.size(); ++i)
            {
                int[] color = polygons.get(i).color;

                if (i == this.highlightedStructure)
                    color = highlightColor;

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
            RegularPolygon pol = polygons.get(polygonId);
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

    public Structure getStructure(int polygonId)
    {
        return polygons.get(polygonId);
    }

    public vtkActor getBoundaryActor()
    {
        return boundaryActor;
    }

    public vtkActor getInteriorActor()
    {
        return interiorActor;
    }

    public void addNewStructure()
    {
        // do nothing
    }

    public void addNewStructure(double[] pos)
    {
        RegularPolygon pol = this.new RegularPolygon(numberOfSides, type, defaultColor);
        polygons.add(pol);

        pol.updatePolygon(smallBodyModel, pos, defaultRadius);
        highlightedStructure = polygons.size()-1;
        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
    }

    public void removeStructure(int polygonId)
    {
        polygons.remove(polygonId);

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void movePolygon(int polygonId, double[] newCenter)
    {
        RegularPolygon pol = polygons.get(polygonId);
        pol.updatePolygon(smallBodyModel, newCenter, pol.radius);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    /**
     * Move the polygon to the specified latitude and longitude.
     *
     * @param polygonId
     * @param latitude - in radians
     * @param longitude - in radians
     */
    public void movePolygon(int polygonId, double latitude, double longitude)
    {
        double[] newCenter = new double[3];
        smallBodyModel.getPointAndCellIdFromLatLon(latitude, longitude, newCenter);
        movePolygon(polygonId, newCenter);
    }

    public void changeRadiusOfPolygon(int polygonId, double[] newPointOnPerimeter)
    {
        RegularPolygon pol = polygons.get(polygonId);
        double newRadius = Math.sqrt(
                (pol.center[0]-newPointOnPerimeter[0])*(pol.center[0]-newPointOnPerimeter[0]) +
                (pol.center[1]-newPointOnPerimeter[1])*(pol.center[1]-newPointOnPerimeter[1]) +
                (pol.center[2]-newPointOnPerimeter[2])*(pol.center[2]-newPointOnPerimeter[2]));
        if (newRadius > maxRadius)
            newRadius = maxRadius;

        pol.updatePolygon(smallBodyModel, pol.center, newRadius);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void changeRadiusOfAllPolygons(double newRadius)
    {
        for (RegularPolygon pol : this.polygons)
        {
            pol.updatePolygon(smallBodyModel, pol.center, newRadius);
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void selectStructure(int idx)
    {
        // Do nothing. RegularPolygonModel does not support selection.
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

    public void loadModel(File file) throws IOException
    {
        ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());

        ArrayList<RegularPolygon> newPolygons = new ArrayList<RegularPolygon>();
        for (int i=0; i<lines.size(); ++i)
        {
            RegularPolygon pol = this.new RegularPolygon(numberOfSides, type, defaultColor);
            pol.center = new double[3];

            String[] words = lines.get(i).trim().split("\\s+");

            // The latest version of this file format has 14 columns. The previous version had
            // 10 columns for circles and 13 columns for points. We still want to support loading
            // both versions, so look at how many columns are in the line.

            // The first 8 columns are the same in both the old and new formats.
            pol.id = Integer.parseInt(words[0]);
            pol.name = words[1];
            pol.center[0] = Double.parseDouble(words[2]);
            pol.center[1] = Double.parseDouble(words[3]);
            pol.center[2] = Double.parseDouble(words[4]);

            if (pol.id > maxPolygonId)
                maxPolygonId = pol.id;

            // Note the next 3 words in the line (the point in spherical coordinates) are not used

            // For the new format and the points file in the old format, the next 4 columns (slope,
            // elevation, acceleration, and potential) are not used.

            if (words.length < 14)
            {
                // OLD VERSION of file
                if (isCircleModel)
                    pol.radius = Double.parseDouble(words[8]) / 2.0; // read in diameter not radius
                else
                    pol.radius = defaultRadius;
            }
            else
            {
                // NEW VERSION of file
                pol.radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
            }

            // If there are 9 or more columns in the file, the last column is the color in both
            // the new and old formats.
            if (words.length > 9)
            {
                String[] colorStr = words[words.length-1].split(",");
                if (colorStr.length == 3)
                {
                    pol.color[0] = Integer.parseInt(colorStr[0]);
                    pol.color[1] = Integer.parseInt(colorStr[1]);
                    pol.color[2] = Integer.parseInt(colorStr[2]);
                }
            }

            pol.updatePolygon(smallBodyModel, pol.center, pol.radius);
            newPolygons.add(pol);
        }

        // Only if we reach here and no exception is thrown do we modify this class
        polygons = newPolygons;

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void saveModel(File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        for (RegularPolygon pol : polygons)
        {
            String name = pol.name;
            if (name.length() == 0)
                name = "default";

            // Since tab is used as the delimiter, replace any tabs in the name with spaces.
            name = name.replace('\t', ' ');

            LatLon llr = MathUtil.reclat(pol.center);
            double lat = llr.lat*180.0/Math.PI;
            double lon = llr.lon*180.0/Math.PI;
            if (lon < 0.0)
                lon += 360.0;

            String str =
                pol.id + "\t" +
                name + "\t" +
                pol.center[0] + "\t" +
                pol.center[1] + "\t" +
                pol.center[2] + "\t" +
                lat + "\t" +
                lon + "\t" +
                llr.rad;


            str += "\t";
            double[] values = getColoringValuesAtPolygon(pol);
            for (int i=0; i<values.length; ++i)
            {
                str += values[i];
                if (i < values.length-1)
                    str += "\t";
            }

            str += "\t" + 2.0*pol.radius; // save out as diameter, not radius

            str += "\t" + pol.color[0] + "," + pol.color[1] + "," + pol.color[2];

            str += "\n";

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

    public double getDefaultRadius()
    {
        return defaultRadius;
    }

    public void setDefaultRadius(double radius)
    {
        this.defaultRadius = radius;
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

        return -1;
    }

    public void redrawAllStructures()
    {
        for (RegularPolygon pol : this.polygons)
        {
            pol.updatePolygon(smallBodyModel, pol.center, pol.radius);
        }

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            redrawAllStructures();
        }
    }

    public void setStructureColor(int idx, int[] color)
    {
        polygons.get(idx).setColor(color);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private double[] getColoringValuesAtPolygon(RegularPolygon pol)
    {
        double[] values = {0.0, 0.0, 0.0, 0.0};

        if (!isCircleModel)
        {
            for (int i=0; i<4; ++i)
                values[i] = smallBodyModel.getColoringValue(i, pol.center);
        }
        else
        {
            // For circles compute the slope and elevation averaged over the rim of the circle.
            // For acceleration and potential simply compute at the center.

            vtkCellArray lines = pol.boundaryPolyData.GetLines();
            vtkPoints points = pol.boundaryPolyData.GetPoints();

            vtkIdTypeArray idArray = lines.GetData();
            int size = idArray.GetNumberOfTuples();

            double totalLength = 0.0;
            double[] midpoint = new double[3];
            for (int i=0; i<size; i+=3)
            {
                if (idArray.GetValue(i) != 2)
                {
                    System.out.println("Big problem: polydata corrupted");
                    return null;
                }

                double[] pt1 = points.GetPoint(idArray.GetValue(i+1));
                double[] pt2 = points.GetPoint(idArray.GetValue(i+2));

                MathUtil.midpointBetween(pt1, pt2, midpoint);
                double dist = MathUtil.distanceBetween(pt1, pt2);
                totalLength += dist;

                double[] valuesAtMidpoint = smallBodyModel.getAllColoringValues(midpoint);

                values[0] += valuesAtMidpoint[0]*dist;
                values[1] += valuesAtMidpoint[1]*dist;
            }

            values[0] /= totalLength;
            values[1] /= totalLength;
            values[2] = smallBodyModel.getColoringValue(2, pol.center);
            values[3] = smallBodyModel.getColoringValue(3, pol.center);
        }

        return values;
    }
}
