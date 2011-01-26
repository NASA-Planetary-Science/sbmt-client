package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import vtk.vtkAbstractPointLocator;
import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkCell;
import vtk.vtkCoordinate;
import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkImageAppendComponents;
import vtk.vtkImageBlend;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkImageShiftScale;
import vtk.vtkLookupTable;
import vtk.vtkMassProperties;
import vtk.vtkPNGReader;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkScalarBarActor;
import vtk.vtkTextProperty;
import vtk.vtkTexture;
import vtk.vtkUnsignedCharArray;
import vtk.vtkXMLImageDataReader;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.ConvertResourceToFile;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.Frustum;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.SmallBodyCubes;

public class SmallBodyModel extends Model
{

    public enum ColoringValueType {
        POINT_DATA,
        CELLDATA
    }

    public enum ShadingType {
        FLAT,
        SMOOTH,
    }

    static public final String FlatShadingStr = "Flat";
    static public final String SmoothShadingStr = "Smooth";
    static public final String LowResModelStr = "Low (49152 plates)";
    static public final String MedResModelStr = "Medium (196608 plates)";
    static public final String HighResModelStr = "High (786432 plates)";
    static public final String VeryHighResModelStr = "Very High (3145728 plates)";

    private String[] coloringNames;
    private String[] coloringUnits;
    // If true the coloring can contain a null value. Meaning the lowest value
    // of the data is should be interperted as no data available at that location,
    // not as a real value. This affects the color table.
    private boolean[] coloringHasNulls;
    private vtkPolyData smallBodyPolyData;
    private vtkPolyData lowResSmallBodyPolyData;
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private vtksbCellLocator cellLocator;
    private vtkPointLocator pointLocator;
    private vtkPointLocator lowResPointLocator;
    private vtkFloatArray[] coloringValues;
    private vtkImageData[] coloringImages;
    private ColoringValueType coloringValueType;
    private vtkImageData originalImageMap;
    private vtkImageData displayedImageMap;
    private vtkScalarBarActor scalarBarActor;
    private SmallBodyCubes smallBodyCubes;
    private int coloringIndex = -1;
    private File defaultModelFile;
    private int resolutionLevel = 0;
    private vtkGenericCell genericCell;
    private String[] modelNames;
    private String[] modelFiles;
    private String[] coloringFiles;
    private String imageMapName = null;
    private boolean showImageMap = false;
    private vtkTexture imageMapTexture = null;
    private BoundingBox boundingBox = null;
    private double imageMapOpacity = 0.50;
    private vtkImageBlend blendFilter;
    private vtkIdList idList; // to avoid repeated allocations

    // Does this class support false coloring
    private boolean supportsFalseColoring = false;
    // If true, a false color will be used by using 3 of the existing
    // colors for the red, green, and blue channels
    private boolean useFalseColoring = false;
    private int redFalseColor = -1; // red channel for false coloring
    private int greenFalseColor = -1; // green channel for false coloring
    private int blueFalseColor = -1; // blue channel for false coloring
    private vtkUnsignedCharArray falseColorArray;


    /**
     * Default constructor. Must be followed by a call to setSmallBodyPolyData.
     */
    public SmallBodyModel()
    {
        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();
    }

    public SmallBodyModel(
            String[] modelNames,
            String[] modelFiles,
            String[] coloringFiles,
            String[] coloringNames,
            String[] coloringUnits,
            boolean[] coloringHasNulls,
            boolean supportsFalseColoring,
            String imageMapName,
            ColoringValueType coloringValueType,
            boolean lowestResolutionModelStoredInResource)
    {
        super(ModelNames.SMALL_BODY);

        this.modelNames = modelNames;
        this.modelFiles = modelFiles;
        this.coloringFiles = coloringFiles;
        this.coloringNames = coloringNames;
        this.coloringUnits = coloringUnits;
        this.coloringHasNulls = coloringHasNulls;
        this.supportsFalseColoring = supportsFalseColoring;
        this.imageMapName = imageMapName;
        this.coloringValueType = coloringValueType;
        if (coloringNames != null)
        {
            this.coloringValues = new vtkFloatArray[coloringNames.length];
            this.coloringImages = new vtkImageData[coloringNames.length];

            // If coloringHasNulls is null, assume false for all coloring (i.e. all data
            // is real data, with no missing data)
            if (coloringHasNulls == null)
            {
                coloringHasNulls = new boolean[coloringNames.length];
                Arrays.fill(coloringHasNulls, false);
            }
        }

        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();

        if (lowestResolutionModelStoredInResource)
            defaultModelFile = ConvertResourceToFile.convertResourceToTempFile(this, modelFiles[0]);
        else
            defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);

        initialize(defaultModelFile);
    }

    public void setSmallBodyPolyData(vtkPolyData polydata,
            vtkFloatArray[] coloringValues,
            String[] coloringNames,
            String[] coloringUnits,
            ColoringValueType coloringValueType)
    {
        smallBodyPolyData.DeepCopy(polydata);
        this.coloringValues = coloringValues;
        this.coloringNames = coloringNames;
        this.coloringUnits = coloringUnits;
        this.coloringValueType = coloringValueType;

        initializeLocators();
    }

    private void initialize(File modelFile)
    {
        vtkPolyDataReader smallBodyReader = new vtkPolyDataReader();
        smallBodyReader.SetFileName(modelFile.getAbsolutePath());
        smallBodyReader.Update();

        vtkPolyData output = smallBodyReader.GetOutput();
        smallBodyPolyData.DeepCopy(output);

        smallBodyReader.Delete();

        initializeLocators();

        //this.computeLargestSmallestEdgeLength();
        //this.computeSurfaceArea();
    }

    private void initializeLocators()
    {
        if (cellLocator == null)
        {
            cellLocator = new vtksbCellLocator();
            pointLocator = new vtkPointLocator();
        }

        // Initialize the cell locator
        cellLocator.FreeSearchStructure();
        cellLocator.SetDataSet(smallBodyPolyData);
        cellLocator.CacheCellBoundsOn();
        cellLocator.AutomaticOn();
        //cellLocator.SetMaxLevel(10);
        //cellLocator.SetNumberOfCellsPerNode(5);
        cellLocator.BuildLocator();

        pointLocator.FreeSearchStructure();
        pointLocator.SetDataSet(smallBodyPolyData);
        pointLocator.BuildLocator();
    }

    private void initializeLowResData()
    {
        if (lowResPointLocator == null)
        {
            lowResSmallBodyPolyData = new vtkPolyData();

            vtkPolyDataReader smallBodyReader = new vtkPolyDataReader();
            smallBodyReader.SetFileName(defaultModelFile.getAbsolutePath());
            smallBodyReader.Update();

            vtkPolyData output = smallBodyReader.GetOutput();
            lowResSmallBodyPolyData.DeepCopy(output);

            smallBodyReader.Delete();

            lowResPointLocator = new vtkPointLocator();
            lowResPointLocator.SetDataSet(lowResSmallBodyPolyData);
            lowResPointLocator.BuildLocator();
        }
    }

    public vtkPolyData getSmallBodyPolyData()
    {
        return smallBodyPolyData;
    }

    public vtkPolyData getLowResSmallBodyPolyData()
    {
        initializeLowResData();

        return lowResSmallBodyPolyData;
    }

    public vtksbCellLocator getCellLocator()
    {
        return cellLocator;
    }

    public vtkAbstractPointLocator getPointLocator()
    {
        return pointLocator;
    }

    public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
    {
        if (smallBodyCubes == null)
        {
            smallBodyCubes = new SmallBodyCubes(
                    getLowResSmallBodyPolyData(),
                    null);
        }

        return smallBodyCubes.getIntersectingCubes(polydata);
    }

    public void setShowSmallBody(boolean show)
    {
        if (show)
        {
            if (!smallBodyActors.contains(smallBodyActor))
            {
                smallBodyActors.add(smallBodyActor);
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
        }
        else
        {
            if (smallBodyActors.contains(smallBodyActor))
            {
                smallBodyActors.remove(smallBodyActor);
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
        }

    }

    public vtkPolyData computeFrustumIntersection(
            double[] origin,
            double[] ul,
            double[] ur,
            double[] lr,
            double[] ll)
    {
        return PolyDataUtil.computeFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, origin, ul, ur, lr, ll);
    }

    public vtkPolyData computeMultipleFrustumIntersection(ArrayList<Frustum> frustums)
    {
        return PolyDataUtil.computeMultipleFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, frustums);
    }

    /**
     * Given 2 points on the surface of the body, draw a nice looking path between the 2
     * that is not obscured anywhere or too distant from the surface. Return this
     * path as a vtkPolyData
     * @param pt1
     * @param pt2
     * @return
     */
    public vtkPolyData drawPath(
            double[] pt1,
            double[] pt2)
    {
        return PolyDataUtil.drawPathOnPolyData(smallBodyPolyData, pointLocator, pt1, pt2);
    }

    public void drawPolygon(
            double[] center,
            double radius,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        PolyDataUtil.drawPolygonOnPolyData(
                smallBodyPolyData,
                pointLocator,
                center,
                radius,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void drawPolygonLowRes(
            double[] center,
            double radius,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        if (resolutionLevel == 0)
        {
            drawPolygon(center, radius, numberOfSides, outputInterior, outputBoundary);
            return;
        }

        initializeLowResData();

        PolyDataUtil.drawPolygonOnPolyData(
                lowResSmallBodyPolyData,
                lowResPointLocator,
                center,
                radius,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void drawCone(
            double[] vertex,
            double[] axis,
            double angle,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        PolyDataUtil.drawConeOnPolyData(
                smallBodyPolyData,
                pointLocator,
                vertex,
                axis,
                angle,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void shiftPolyLineInNormalDirection(
            vtkPolyData polyLine,
            double shiftFactor)
    {
        PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(
                polyLine,
                smallBodyPolyData,
                pointLocator,
                shiftFactor * getMinShiftAmount());
    }

    /**
     * This returns the closest point to the model to pt. Note the returned point need
     * not be a vertex of the model and can lie anywhere on a plate.
     * @param pt
     * @return
     */
    public double[] findClosestPoint(double[] pt)
    {
        double[] closestPoint = new double[3];
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);

        return closestPoint;
    }

    /**
     * This returns the index of the closest cell in the model to pt.
     * The closest point within the cell is returned in closestPoint
     * @param pt
     * @param closestPoint the closest point within the cell is returned here
     * @return
     */
    public int findClosestCell(double[] pt, double[] closestPoint)
    {
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        // Use FindClosestPoint rather the FindCell since not sure what tolerance to use in the latter.
        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);

        return cellId[0];
    }

    /**
     * This returns the index of the closest cell in the model to pt.
     * @param pt
     * @return
     */
    public int findClosestCell(double[] pt)
    {
        double[] closestPoint = new double[3];
        return findClosestCell(pt, closestPoint);
    }

    /**
     * Compute the point on the asteroid that has the specified latitude and longitude. Returns the
     * cell id of the cell containing that point. This is done by shooting a ray from the origin in the
     * specified direction.
     * @param lat
     * @param lon
     * @param intersectPoint
     * @return the cellId of the cell containing the intersect point
     */
    public int getPointAndCellIdFromLatLon(double lat, double lon, double[] intersectPoint)
    {
        LatLon lla = new LatLon(lat, lon);
        double[] lookPt = MathUtil.latrec(lla);

        // Move in the direction of lookPt until we are definitely outside the asteroid
        BoundingBox bb = getBoundingBox();
        double largestSide = bb.getLargestSide() * 1.1;
        lookPt[0] *= largestSide;
        lookPt[1] *= largestSide;
        lookPt[2] *= largestSide;

        double[] origin = {0.0, 0.0, 0.0};
        double tol = 1e-6;
        double[] t = new double[1];
        double[] x = new double[3];
        double[] pcoords = new double[3];
        int[] subId = new int[1];
        int[] cellId = new int[1];

        int result = cellLocator.IntersectWithLine(origin, lookPt, tol, t, x, pcoords, subId, cellId, genericCell);

        intersectPoint[0] = x[0];
        intersectPoint[1] = x[1];
        intersectPoint[2] = x[2];

        if (result > 0)
            return cellId[0];
        else
            return -1;
    }

    protected void initializeActorsAndMappers()
    {
        if (smallBodyActor == null)
        {
            smallBodyMapper = new vtkPolyDataMapper();
            smallBodyMapper.SetInput(smallBodyPolyData);
            vtkLookupTable lookupTable = new vtkLookupTable();
            smallBodyMapper.SetLookupTable(lookupTable);
            smallBodyMapper.UseLookupTableScalarRangeOn();

            smallBodyActor = new vtkActor();
            smallBodyActor.SetMapper(smallBodyMapper);
            vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
            smallBodyProperty.SetInterpolationToGouraud();

            smallBodyActors.add(smallBodyActor);

            scalarBarActor = new vtkScalarBarActor();
            vtkCoordinate coordinate = scalarBarActor.GetPositionCoordinate();
            coordinate.SetCoordinateSystemToNormalizedViewport();
            coordinate.SetValue(0.2, 0.01);
            scalarBarActor.SetOrientationToHorizontal();
            scalarBarActor.SetWidth(0.6);
            scalarBarActor.SetHeight(0.1275);
            vtkTextProperty tp = new vtkTextProperty();
            tp.SetFontSize(10);
            scalarBarActor.SetTitleTextProperty(tp);
        }
    }

    public ArrayList<vtkProp> getProps()
    {
        initializeActorsAndMappers();

        return smallBodyActors;
    }

    public void setShadingToFlat()
    {
        initializeActorsAndMappers();

        vtkProperty property = smallBodyActor.GetProperty();

        if (property.GetInterpolation() != 0) // The value 0 corresponds to flat (see vtkProperty.h)
        {
            property.SetInterpolationToFlat();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void setShadingToSmooth()
    {
        initializeActorsAndMappers();

        vtkProperty property = smallBodyActor.GetProperty();

        if (property.GetInterpolation() != 1) // The value 1 corresponds to gouraud (see vtkProperty.h)
        {
            property.SetInterpolationToGouraud();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public BoundingBox getBoundingBox()
    {
        if (boundingBox == null)
        {
            smallBodyPolyData.ComputeBounds();
            boundingBox = new BoundingBox(smallBodyPolyData.GetBounds());
        }

        return boundingBox;

        /*
        BoundingBox bb = new BoundingBox();
        vtkPoints points = smallBodyPolyData.GetPoints();
        int numberPoints = points.GetNumberOfPoints();
        for (int i=0; i<numberPoints; ++i)
        {
            double[] pt = points.GetPoint(i);
            bb.update(pt[0], pt[1], pt[2]);
        }

        return bb;
        */
    }

    public double getBoundingBoxDiagonalLength()
    {
        return getBoundingBox().getDiagonalLength();
    }

    /**
     * Get the minimum shift amount needed so shift an object away from
     * the model so it is not obscured by the model and looks like it's
     * laying on the model
     * @return
     */
    public double getMinShiftAmount()
    {
        return getBoundingBoxDiagonalLength() / 38660.0;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        // Coloring is currently only supported in the lowest resolution level
        if (resolutionLevel == 0)
        {
            if (coloringIndex >= 0)
            {
                float value = (float)getColoringValue(coloringIndex, pickPosition);
                return coloringNames[coloringIndex] + ": " + value + " " + coloringUnits[coloringIndex];
            }
            else if (useFalseColoring)
            {
                float red = (float)getColoringValue(redFalseColor, pickPosition);
                float green = (float)getColoringValue(greenFalseColor, pickPosition);
                float blue = (float)getColoringValue(blueFalseColor, pickPosition);
                return coloringNames[redFalseColor] + ": " + red + " " + coloringUnits[redFalseColor] + ", " +
                coloringNames[greenFalseColor] + ": " + green + " " + coloringUnits[greenFalseColor] +  ", " +
                coloringNames[blueFalseColor] + ": " + blue + " " + coloringUnits[blueFalseColor];
            }
        }
        return "";
    }

    public double[] computeLargestSmallestMeanEdgeLength()
    {
        double[] largestSmallestMean = new double[3];

        double minLength = Double.MAX_VALUE;
        double maxLength = 0.0;
        double meanLength = 0.0;

        int numberOfCells = smallBodyPolyData.GetNumberOfCells();

        System.out.println(numberOfCells);

        for (int i=0; i<numberOfCells; ++i)
        {
            vtkCell cell = smallBodyPolyData.GetCell(i);
            vtkPoints points = cell.GetPoints();
            double[] pt0 = points.GetPoint(0);
            double[] pt1 = points.GetPoint(1);
            double[] pt2 = points.GetPoint(2);
            double dist0 = MathUtil.distanceBetween(pt0, pt1);
            double dist1 = MathUtil.distanceBetween(pt1, pt2);
            double dist2 = MathUtil.distanceBetween(pt2, pt0);
            if (dist0 < minLength)
                minLength = dist0;
            if (dist0 > maxLength)
                maxLength = dist0;
            if (dist1 < minLength)
                minLength = dist1;
            if (dist1 > maxLength)
                maxLength = dist1;
            if (dist2 < minLength)
                minLength = dist2;
            if (dist2 > maxLength)
                maxLength = dist2;

            meanLength += (dist0 + dist1 + dist2);
        }

        meanLength /= ((double)(numberOfCells * 3));

        System.out.println("minLength  " + minLength);
        System.out.println("maxLength  " + maxLength);
        System.out.println("meanLength  " + meanLength);

        largestSmallestMean[0] = minLength;
        largestSmallestMean[1] = maxLength;
        largestSmallestMean[2] = meanLength;

        return largestSmallestMean;
    }

    public void computeSurfaceArea()
    {
        vtkMassProperties massProp = new vtkMassProperties();
        massProp.SetInput(smallBodyPolyData);
        massProp.Update();

        System.out.println("Surface area " + massProp.GetSurfaceArea());
        System.out.println("Volume " + massProp.GetVolume());
    }

    public void setModelResolution(int level) throws IOException
    {
        if (level == resolutionLevel)
            return;

        resolutionLevel = level;
        if (level < 0)
            resolutionLevel = 0;
        else if (level > 3)
            resolutionLevel = 3;

        smallBodyCubes = null;
        if (coloringValues != null)
        {
            for (int i=0; i<coloringValues.length; ++i)
                coloringValues[i] = null;
        }

        File smallBodyFile = defaultModelFile;
        switch(level)
        {
        case 1:
            smallBodyFile = FileCache.getFileFromServer(modelFiles[1]);
            break;
        case 2:
            smallBodyFile = FileCache.getFileFromServer(modelFiles[2]);
            break;
        case 3:
            smallBodyFile = FileCache.getFileFromServer(modelFiles[3]);
            break;
        }

        if (resolutionLevel != 0)
            setColoringIndex(-1);

        this.initialize(smallBodyFile);

        this.pcs.firePropertyChange(Properties.MODEL_RESOLUTION_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public int getModelResolution()
    {
        return resolutionLevel;
    }

    public int getNumberResolutionLevels()
    {
        return modelFiles.length;
    }

    public String getModelName()
    {
        if (resolutionLevel >= 0 && resolutionLevel < 4)
            return modelNames[resolutionLevel];
        else
            return null;
    }

    /**
     * This file loads the coloring used when the coloring method is point data
     * @throws IOException
     */
    private void loadColoringData() throws IOException
    {
        if (coloringValues != null && coloringValues.length > 0 && coloringValues[0] == null)
        {
            for (int i=0; i<coloringValues.length; ++i)
                coloringValues[i] = new vtkFloatArray();
        }
        else
        {
            return;
        }

        for (int i=0; i<coloringValues.length; ++i)
        {
            File file = FileCache.getFileFromServer(coloringFiles[i]);
            vtkFloatArray array = coloringValues[i];

            array.SetNumberOfComponents(1);
            if (coloringValueType == ColoringValueType.POINT_DATA)
                array.SetNumberOfTuples(smallBodyPolyData.GetNumberOfPoints());
            else
                array.SetNumberOfTuples(smallBodyPolyData.GetNumberOfCells());

            FileInputStream fs =  new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fs);
            BufferedReader in = new BufferedReader(isr);

            String line;
            int j = 0;
            while ((line = in.readLine()) != null)
            {
                array.SetTuple1(j, Float.parseFloat(line));
                ++j;
            }

            in.close();
        }
    }

    /**
     * This file loads the coloring used when the coloring method is texture
     * @throws IOException
     */
    private void loadColoringTexture() throws IOException
    {
        if (coloringImages != null && coloringImages.length > 0 && coloringImages[0] == null)
        {
            loadColoringData();

            for (int i=0; i<coloringImages.length; ++i)
                coloringImages[i] = new vtkImageData();
        }
        else
        {
            return;
        }

        for (int i=0; i<coloringImages.length; ++i)
        {
            int length = coloringFiles[i].length();
            File file = FileCache.getFileFromServer(coloringFiles[i].substring(0, length-6) + "vti");

            vtkImageData image = coloringImages[i];

            vtkXMLImageDataReader reader = new vtkXMLImageDataReader();
            //vtkStructuredPointsReader reader = new vtkStructuredPointsReader();
            reader.SetFileName(file.getAbsolutePath());
            reader.Update();

            vtkImageData readerOutput = reader.GetOutput();
            image.DeepCopy(readerOutput);

//            vtkLookupTable lookupTable = new vtkLookupTable();
//            lookupTable.SetRange(coloringValues[i].GetRange());
//            lookupTable.Build();
//            invertLookupTableCharArray(lookupTable.GetTable());
//
//            vtkImageMapToColors mapToColors = new vtkImageMapToColors();
//            mapToColors.SetInputConnection(reader.GetOutputPort());
//            mapToColors.SetLookupTable(lookupTable);
//            mapToColors.SetOutputFormatToRGB();
//            mapToColors.Update();
//
//            image.DeepCopy(mapToColors.GetOutput());
        }
    }

    private void invertLookupTableCharArray(vtkUnsignedCharArray table)
    {
        int numberOfValues = table.GetNumberOfTuples();
        for (int i=0; i<numberOfValues/2; ++i)
        {
            double[] v1 = table.GetTuple4(i);
            double[] v2 = table.GetTuple4(numberOfValues-i-1);
            table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
            table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
        }
    }

    /**
     * Invert the lookup table so that red is high values
     * and blue is low values (rather than the reverse).
     */
    private void invertLookupTable()
    {
        vtkLookupTable lookupTable = (vtkLookupTable)smallBodyMapper.GetLookupTable();
        vtkUnsignedCharArray table = lookupTable.GetTable();

        invertLookupTableCharArray(table);
//        int numberOfValues = table.GetNumberOfTuples();
//        for (int i=0; i<numberOfValues/2; ++i)
//        {
//            double[] v1 = table.GetTuple4(i);
//            double[] v2 = table.GetTuple4(numberOfValues-i-1);
//            table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
//            table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
//        }

        lookupTable.SetTable(table);
        smallBodyMapper.Modified();
    }

    public void setColoringIndex(int index) throws IOException
    {
//        if (coloringIndex == index)
//            return;
//
        // Coloring is currently only supported in the lowest resolution level
        if (resolutionLevel == 0)
        {
            coloringIndex = index;
            useFalseColoring = false;

            paintBody();
        }
    }

    public void setFalseColoring(int redChannel, int greenChannel, int blueChannel) throws IOException
    {
//        if (redFalseColor == redChannel &&
//                greenFalseColor == greenChannel &&
//                blueFalseColor == blueChannel &&
//                useFalseColoring == true)
//        {
//            return;
//        }
//
        // Coloring is currently only supported in the lowest resolution level
        if (resolutionLevel == 0)
        {
            coloringIndex = -1;
            useFalseColoring = true;
            redFalseColor = redChannel;
            greenFalseColor = greenChannel;
            blueFalseColor = blueChannel;

            paintBody();
        }
    }

    public boolean isColoringDataAvailable()
    {
        return coloringFiles != null;
    }

    private void blendImageMapWithColoring()
    {
        vtkImageData image = null;

        if (coloringIndex >= 0)
        {
            vtkLookupTable lookupTable = new vtkLookupTable();
            //lookupTable.SetRange(coloringValues[coloringIndex].GetRange());
            lookupTable.SetRange(computeRange(coloringIndex, true));
            lookupTable.Build();
            invertLookupTableCharArray(lookupTable.GetTable());
            // If there's missing data, map them to white
            if (coloringHasNulls != null && coloringHasNulls[coloringIndex])
                ((vtkLookupTable)smallBodyMapper.GetLookupTable()).SetTableValue(0, 1.0, 1.0, 1.0, 1.0);

            vtkImageMapToColors mapToColors = new vtkImageMapToColors();
            mapToColors.SetInput(coloringImages[coloringIndex]);
            mapToColors.SetLookupTable(lookupTable);
            mapToColors.SetOutputFormatToRGB();
            mapToColors.Update();

            //image = this.coloringImages[coloringIndex];
            image = mapToColors.GetOutput();
            scalarBarActor.SetTitle(coloringNames[coloringIndex] + " (" + coloringUnits[coloringIndex] + ")");
        }
        else if (useFalseColoring)
        {
            vtkImageAppendComponents appendComponents = new vtkImageAppendComponents();

            int[] components = {redFalseColor, greenFalseColor, blueFalseColor};
            for (int c : components)
            {
                //double[] range = coloringValues[c].GetRange();
                double[] range = computeRange(c, false);
                double extent = range[1] - range[0];

                vtkImageShiftScale shiftScale = new vtkImageShiftScale();
                shiftScale.SetShift(-range[0]);
                shiftScale.SetScale(255.0 / extent);
                shiftScale.SetInput(coloringImages[c]);
                shiftScale.SetOutputScalarTypeToUnsignedChar();
                shiftScale.Update();

                // TODO in this situation, invalid data get mapped to black, not white, as
                // is the convention throughout the rest of this class. Fix this if desired.

                vtkAlgorithmOutput outputPort = shiftScale.GetOutputPort();
                appendComponents.AddInputConnection(outputPort);
            }

            appendComponents.Update();
            image = appendComponents.GetOutput();
        }

        if (blendFilter == null)
        {
            blendFilter = new vtkImageBlend();
            blendFilter.AddInput(originalImageMap);
            blendFilter.AddInput(image);
        }
        else
        {
            blendFilter.SetInput(0, originalImageMap);
            blendFilter.SetInput(1, image);
            blendFilter.Modified();
        }
        blendFilter.SetOpacity(1, 1.0 - imageMapOpacity);
        blendFilter.Update();

        vtkImageData output = blendFilter.GetOutput();
        displayedImageMap.DeepCopy(output);
    }

    private void generateTextureCoordinates()
    {
        vtkFloatArray textureCoords = new vtkFloatArray();

        int numberOfPoints = smallBodyPolyData.GetNumberOfPoints();

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = smallBodyPolyData.GetPoints();

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            LatLon ll = MathUtil.reclat(pt);

            double u = ll.lon;
            if (u < 0.0)
                u += 2.0 * Math.PI;
            u /= 2.0 * Math.PI;
            double v = (ll.lat + Math.PI/2.0) / Math.PI;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;

            textureCoords.SetTuple2(i, u, v);
        }

        /*
        // The plates that cross the zero longitude meridian might be messed up
        // since some of the points might be to the left and some to right. Fix
        // this here to make all the points either on the left or on the right.
        int numberOfCells = smallBodyPolyData.GetNumberOfCells();
        for (int i=0; i<numberOfCells; ++i)
        {
            int id0 = smallBodyPolyData.GetCell(i).GetPointId(0);
            int id1 = smallBodyPolyData.GetCell(i).GetPointId(1);
            int id2 = smallBodyPolyData.GetCell(i).GetPointId(2);
            double[] lon = new double[3];
            lon[0] = textureCoords.GetTuple2(id0)[0];
            lon[1] = textureCoords.GetTuple2(id1)[0];
            lon[2] = textureCoords.GetTuple2(id2)[0];

            if ( Math.abs(lon[0] - lon[1]) > 0.5 ||
                 Math.abs(lon[1] - lon[2]) > 0.5 ||
                 Math.abs(lon[2] - lon[0]) > 0.5)
            {
                double[] lat = new double[3];
                lat[0] = textureCoords.GetTuple2(id0)[1];
                lat[1] = textureCoords.GetTuple2(id1)[1];
                lat[2] = textureCoords.GetTuple2(id2)[1];

                System.out.println("messed up " + i);
                System.out.println(lon[0] - lon[1]);
                System.out.println(lon[1] - lon[2]);
                System.out.println(lon[2] - lon[0]);
                // First determine which side we're on

                // Do this by first determining which point is the greatest distance from the
                // meridian
                double[] dist = {-1000.0, -1000.0, -1000.0};
                int maxIdx = -1;
                double maxDist = -1000.0;
                for (int j=0; j<3; ++j)
                {
                    if (lon[j] > 0.5)
                        dist[j] = 1.0 - lon[j];
                    else
                        dist[j] = lon[j];

                    if (dist[j] > maxDist)
                    {
                        maxDist = dist[j];
                        maxIdx = j;
                    }
                }

                if (lon[maxIdx] < 0.5) // If true, we're on left
                {
                    // Make sure all the coordinates are on left
                    for (int j=0; j<3; ++j)
                    {
                        if (lon[j] > 0.5)
                            lon[j] = 0.0;
                    }
                }
                else
                {
                    // Make sure all the coordinates are on right
                    for (int j=0; j<3; ++j)
                    {
                        if (lon[j] < 0.5)
                            lon[j] = 1.0;
                    }
                }
                textureCoords.SetTuple2(id0, lon[0], lat[0]);
                textureCoords.SetTuple2(id1, lon[1], lat[1]);
                textureCoords.SetTuple2(id2, lon[2], lat[2]);
            }
        }
        */

        smallBodyPolyData.GetPointData().SetTCoords(textureCoords);
    }

    public boolean isImageMapAvailable()
    {
        return imageMapName != null;
    }

    public void setShowImageMap(boolean b)
    {
        showImageMap = b;

        try
        {
            paintBody();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int getNumberOfColors()
    {
        if (coloringNames == null)
            return 0;
        else
            return coloringNames.length;
    }

    public String getColoringName(int i)
    {
        if (coloringNames != null && i < coloringNames.length)
            return coloringNames[i];
        else
            return null;
    }

    public boolean isFalseColoringSupported()
    {
        return supportsFalseColoring;
    }

    private double getColoringValue(double[] pt, vtkFloatArray pointOrCellData)
    {
        double[] closestPoint = new double[3];
        int cellId = findClosestCell(pt, closestPoint);
        if (coloringValueType == ColoringValueType.POINT_DATA)
        {
            return PolyDataUtil.interpolateWithinCell(
                    smallBodyPolyData, pointOrCellData, cellId, closestPoint, idList);
        }
        else
        {
            return pointOrCellData.GetTuple1(cellId);
        }
    }

    /**
     * Get value assuming pt is exactly on the asteroid and cellId is provided
     * @param pt
     * @param pointOrCellData
     * @param cellId
     * @return
     */
    private double getColoringValue(double[] pt, vtkFloatArray pointOrCellData, int cellId)
    {
        if (coloringValueType == ColoringValueType.POINT_DATA)
        {
            return PolyDataUtil.interpolateWithinCell(
                    smallBodyPolyData, pointOrCellData, cellId, pt, idList);
        }
        else
        {
            return pointOrCellData.GetTuple1(cellId);
        }
    }

    public double getColoringValue(int index, double[] pt)
    {
        try
        {
            if (coloringValues != null && index < coloringValues.length && coloringValues[index] == null)
                loadColoringData();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return getColoringValue(pt, coloringValues[index]);
    }

    public double[] getAllColoringValues(double[] pt)
    {
        try
        {
            loadColoringData();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        double[] closestPoint = new double[3];
        int cellId = findClosestCell(pt, closestPoint);

        double[] values = new double[coloringValues.length];
        for (int i=0; i<coloringValues.length; ++i)
        {
            values[i] = getColoringValue(closestPoint, coloringValues[i], cellId);
        }

        return values;
    }

    public double getImageMapOpacity()
    {
        return imageMapOpacity;
    }

    public void setImageMapOpacity(double imageMapOpacity)
    {
        this.imageMapOpacity = imageMapOpacity;
        try
        {
            paintBody();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Compute the range of an array but account for the fact that for some datasets,
    // some of the data is missing as represented by the lowest valued. So compute
    // the range ignoring this lowest value (i.e take the lowest value to be the value
    // just higher than the lowest value).
    private double[] computeRange(int index, boolean adjustForColorTable)
    {
        double[] range = new double[2];
        coloringValues[index].GetRange(range);

        if (coloringHasNulls == null || !coloringHasNulls[index])
        {
            return range;
        }
        else
        {
            vtkFloatArray array = coloringValues[index];
            int numberValues = array.GetNumberOfTuples();
            double adjustedMin = range[1];
            for (int i=0; i<numberValues; ++i)
            {
                double v = array.GetValue(i);
                if (v < adjustedMin && v > range[0])
                    adjustedMin = v;
            }

            // Subtract a little amount from the adjustedMin. This adds an extra value in the lookup table
            // for the invalid data, which we can map to the color white. The value of 254 seems to work well,
            // for current data, but may need tweaking for future data.
            if (adjustForColorTable)
                adjustedMin -= (range[1]-adjustedMin)/254.0;

            range[0] = adjustedMin;

            return range;
        }
    }

    /**
     *  Update the false color point or cell data if
     */
    private void updateFalseColorArray()
    {
        if (falseColorArray == null)
        {
            falseColorArray = new vtkUnsignedCharArray();
            falseColorArray.SetNumberOfComponents(3);
        }

        vtkFloatArray red = coloringValues[redFalseColor];
        vtkFloatArray green = coloringValues[greenFalseColor];
        vtkFloatArray blue = coloringValues[blueFalseColor];

        double[] redRange = computeRange(redFalseColor, false);
        double[] greenRange = computeRange(greenFalseColor, false);
        double[] blueRange = computeRange(blueFalseColor, false);
        double redExtent = redRange[1] - redRange[0];
        double greenExtent = greenRange[1] - greenRange[0];
        double blueExtent = blueRange[1] - blueRange[0];

        int numberTuples = red.GetNumberOfTuples();
        falseColorArray.SetNumberOfTuples(numberTuples);

        for (int i=0; i<numberTuples; ++i)
        {
            double redValue = 255.0 * (red.GetTuple1(i) - redRange[0]) / redExtent;
            double greenValue = 255.0 * (green.GetTuple1(i) - greenRange[0]) / greenExtent;
            double blueValue = 255.0 * (blue.GetTuple1(i) - blueRange[0]) / blueExtent;

            // Map invalid data to white
            if (redValue < 0.0)   redValue   = 255.0;
            if (greenValue < 0.0) greenValue = 255.0;
            if (blueValue < 0.0)  blueValue  = 255.0;

            falseColorArray.SetTuple3(i, redValue, greenValue, blueValue);
        }
    }

    private void paintBody() throws IOException
    {
        if (resolutionLevel != 0)
            return;

        initializeActorsAndMappers();

        loadColoringData();

        vtkDataArray array = null;

        if (coloringIndex >= 0)
        {
            array = coloringValues[coloringIndex];
            scalarBarActor.SetTitle(coloringNames[coloringIndex] + " (" + coloringUnits[coloringIndex] + ")");
        }
        else if (useFalseColoring)
        {
            updateFalseColorArray();
            array = falseColorArray;
        }

        if (coloringValueType == ColoringValueType.POINT_DATA)
            this.smallBodyPolyData.GetPointData().SetScalars(array);
        else
            this.smallBodyPolyData.GetCellData().SetScalars(array);

        if (coloringIndex < 0)
        {
            if (smallBodyActors.contains(scalarBarActor))
                smallBodyActors.remove(scalarBarActor);
        }
        else
        {
            //smallBodyMapper.GetLookupTable().SetRange(array.GetRange());
            smallBodyMapper.GetLookupTable().SetRange(computeRange(coloringIndex, true));
            ((vtkLookupTable)smallBodyMapper.GetLookupTable()).ForceBuild();
            this.invertLookupTable();

            // If there's missing data, map them to white
            if (coloringHasNulls != null && coloringHasNulls[coloringIndex])
                ((vtkLookupTable)smallBodyMapper.GetLookupTable()).SetTableValue(0, 1.0, 1.0, 1.0, 1.0);

            if (!smallBodyActors.contains(scalarBarActor))
                smallBodyActors.add(scalarBarActor);

            scalarBarActor.SetLookupTable(smallBodyMapper.GetLookupTable());
        }


        if (showImageMap == false)
        {
            smallBodyActor.SetTexture(null);

            if (coloringIndex < 0 && useFalseColoring == false)
            {
                smallBodyMapper.ScalarVisibilityOff();
                smallBodyMapper.SetScalarModeToDefault();
            }
            else
            {
                smallBodyMapper.ScalarVisibilityOn();
                if (coloringValueType == ColoringValueType.POINT_DATA)
                    smallBodyMapper.SetScalarModeToUsePointData();
                else
                    smallBodyMapper.SetScalarModeToUseCellData();
            }
        }
        else
        {
            if (resolutionLevel != 0)
                return;

            if (originalImageMap == null)
            {
                File imageFile = FileCache.getFileFromServer(imageMapName);
                vtkPNGReader reader = new vtkPNGReader();
                reader.SetFileName(imageFile.getAbsolutePath());
                reader.Update();
                vtkImageData readerOutput = reader.GetOutput();

                originalImageMap = new vtkImageData();
                originalImageMap.DeepCopy(readerOutput);
            }

            if (displayedImageMap == null)
            {
                displayedImageMap = new vtkImageData();
            }

            if (imageMapTexture == null)
            {
                imageMapTexture = new vtkTexture();
                imageMapTexture.InterpolateOn();
                imageMapTexture.RepeatOff();
                imageMapTexture.EdgeClampOn();
                imageMapTexture.SetInput(displayedImageMap);

                generateTextureCoordinates();
            }

            smallBodyActor.SetTexture(imageMapTexture);

            smallBodyMapper.ScalarVisibilityOff();
            smallBodyMapper.SetScalarModeToDefault();

            if (coloringIndex < 0 && useFalseColoring == false)
            {
                displayedImageMap.DeepCopy(originalImageMap);
            }
            else
            {
                loadColoringTexture();
                blendImageMapWithColoring();
            }
        }

        this.smallBodyPolyData.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void delete()
    {
        if (smallBodyPolyData != null) smallBodyPolyData.Delete();
        if (lowResSmallBodyPolyData != null) lowResSmallBodyPolyData.Delete();
        if (smallBodyActor != null) smallBodyActor.Delete();
        if (smallBodyMapper != null) smallBodyMapper.Delete();
        for (vtkProp prop : smallBodyActors)
            if (prop != null) prop.Delete();
        if (cellLocator != null) cellLocator.Delete();
        if (pointLocator != null) pointLocator.Delete();
        if (lowResPointLocator != null) lowResPointLocator.Delete();
        if (originalImageMap != null) originalImageMap.Delete();
        if (displayedImageMap != null) displayedImageMap.Delete();
        if (scalarBarActor != null) scalarBarActor.Delete();
        if (genericCell != null) genericCell.Delete();
        if (imageMapTexture != null) imageMapTexture.Delete();
        if (blendFilter != null) blendFilter.Delete();
    }
}
