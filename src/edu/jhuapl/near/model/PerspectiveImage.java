package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkDataArray;
import vtk.vtkFeatureEdges;
import vtk.vtkFloatArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkImageCanvasSource2D;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkImageMask;
import vtk.vtkImageReslice;
import vtk.vtkLookupTable;
import vtk.vtkPNGReader;
import vtk.vtkPointData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataNormals;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTexture;
import vtk.vtkXMLPolyDataReader;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.DateTimeUtil;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.Frustum;
import edu.jhuapl.near.util.IntensityRange;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.VtkDataTypes;

/**
 * This class represents an absract image of a spacecraft imager instrument.
 */
abstract public class PerspectiveImage extends Image implements PropertyChangeListener
{
    public static final float PDS_NA = -1.e32f;
    public static final String FRUSTUM1 = "FRUSTUM1";
    public static final String FRUSTUM2 = "FRUSTUM2";
    public static final String FRUSTUM3 = "FRUSTUM3";
    public static final String FRUSTUM4 = "FRUSTUM4";
    public static final String BORESIGHT_DIRECTION = "BORESIGHT_DIRECTION";
    public static final String UP_DIRECTION = "UP_DIRECTION";
    public static final String NUMBER_EXPOSURES = "NUMBER_EXPOSURES";
    public static final String START_TIME = "START_TIME";
    public static final String STOP_TIME = "STOP_TIME";
    public static final String SPACECRAFT_POSITION = "SPACECRAFT_POSITION";
    public static final String SUN_POSITION_LT = "SUN_POSITION_LT";
    public static final String TARGET_PIXEL_COORD = "TARGET_PIXEL_COORD";
    public static final String TARGET_ROTATION = "TARGET_ROTATION";
    public static final String TARGET_ZOOM_FACTOR = "TARGET_ZOOM_FACTOR";
    public static final String APPLY_ADJUSTMENTS = "APPLY_ADJUSTMENTS";

    public static final double[] bodyOrigin = { 0.0, 0.0, 0.0 };

    private SmallBodyModel smallBodyModel;

    private vtkImageData rawImage;
    private vtkImageData displayedImage;
    private int currentSlice = 0;

    private boolean useDefaultFootprint = true;
    private vtkPolyData[] footprint = new vtkPolyData[1];
    private boolean[] footprintGenerated = new boolean[1];
    private vtkPolyData[] shiftedFootprint = new vtkPolyData[1];

    private vtkActor footprintActor;
    private ArrayList<vtkProp> footprintActors = new ArrayList<vtkProp>();

    private vtkActor frustumActor;

    private vtkPolyDataNormals normalsFilter;

    private vtkFloatArray textureCoords;

    private boolean normalsGenerated = false;

    private double minIncidence = Double.MAX_VALUE;
    private double maxIncidence = -Double.MAX_VALUE;
    private double minEmission = Double.MAX_VALUE;
    private double maxEmission = -Double.MAX_VALUE;
    private double minPhase = Double.MAX_VALUE;
    private double maxPhase = -Double.MAX_VALUE;
    private double minHorizontalPixelScale = Double.MAX_VALUE;
    private double maxHorizontalPixelScale = -Double.MAX_VALUE;
    private double meanHorizontalPixelScale = 0.0;
    private double minVerticalPixelScale = Double.MAX_VALUE;
    private double maxVerticalPixelScale = -Double.MAX_VALUE;
    private double meanVerticalPixelScale = 0.0;

    private float[] minValue = new float[1];
    private float[] maxValue = new float[1];

    private int[] currentMask = new int[4];

    private IntensityRange[] displayedRange = new IntensityRange[1];
    private double imageOpacity = 1.0;

    private double[][] spacecraftPositionOriginal = new double[1][3];
    private double[][] frustum1Original = new double[1][3];
    private double[][] frustum2Original = new double[1][3];
    private double[][] frustum3Original = new double[1][3];
    private double[][] frustum4Original = new double[1][3];
    private double[][] boresightDirectionOriginal = new double[1][3];
    private double[][] upVectorOriginal = new double[1][3];
    private double[][] sunPositionOriginal = new double[1][3];

    private double[][] spacecraftPositionAdjusted = new double[1][3];
    private double[][] frustum1Adjusted = new double[1][3];
    private double[][] frustum2Adjusted = new double[1][3];
    private double[][] frustum3Adjusted = new double[1][3];
    private double[][] frustum4Adjusted = new double[1][3];
    private double[][] boresightDirectionAdjusted = new double[1][3];
    private double[][] upVectorAdjusted = new double[1][3];
    private double[][] sunPositionAdjusted = new double[1][3];

    // offset in world coordinates of the adjusted frustum from the loaded frustum
    private double[] targetPixelCoordinates = { -1.0, -1.0 };

    private double[] zoomFactor = { 1.0 };

    private double[] rotationOffset = { 0.0 };

    // apply all frame adjustments if true
    private boolean[] applyFrameAdjustments = { true };

    private Frustum[] frusta = new Frustum[1];

    private boolean showFrustum = false;

    private String startTime = "";
    private String stopTime = "";

    private vtkImageCanvasSource2D maskSource;

    private int imageWidth;
    private int imageHeight;
    private int imageDepth;

    private String pngFileFullPath; // The actual path of the PNG image stored on the local disk (after downloading from the server)
    private String fitFileFullPath; // The actual path of the FITS image stored on the local disk (after downloading from the server)
    private String labelFileFullPath;
    private String infoFileFullPath;
    private String sumfileFullPath;
    private String labelfileFullPath;

    private double offset;
    private vtkTexture imageTexture;

    // If true, then the footprint is generated by intersecting a frustum with the asteroid.
    // This setting is used when generating the files on the server.
    // If false, then the footprint is downloaded from the server. This setting is used by the GUI.
    private static boolean generateFootprint = true;

    private boolean loadPointingOnly;

    /**
     * If loadPointingOnly is true then only pointing information about this
     * image will be downloaded/loaded. The image itself will not be loaded.
     * Used by ImageBoundary to get pointing info.
     */
    public PerspectiveImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
            this(key, smallBodyModel, loadPointingOnly, 0);
    }

    /**
     * If loadPointingOnly is true then only pointing information about this
     * image will be downloaded/loaded. The image itself will not be loaded.
     * Used by ImageBoundary to get pointing info.
     */
    public PerspectiveImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly, int currentSlice) throws FitsException, IOException
    {
        super(key);
        this.currentSlice = currentSlice;
        this.smallBodyModel = smallBodyModel;
        this.loadPointingOnly = loadPointingOnly;
        this.offset = getDefaultOffset();

        initialize();
    }

    protected void initialize() throws FitsException, IOException
    {
        footprint[0] = new vtkPolyData();
        shiftedFootprint[0] = new vtkPolyData();
        displayedRange[0] = new IntensityRange(1,0);


        if (!loadPointingOnly)
        {
            fitFileFullPath = initializeFitFileFullPath();
            pngFileFullPath = initializePngFileFullPath();
        }

        if (key.source.equals(ImageSource.SPICE) || key.source.equals(ImageSource.LOCAL_PERSPECTIVE))
            infoFileFullPath = initializeInfoFileFullPath();
        else if (key.source.equals(ImageSource.LABEL))
            labelFileFullPath = initializeLabelFileFullPath();
        else
            sumfileFullPath = initializeSumfileFullPath();

        loadPointing();

        if (!loadPointingOnly)
        {
            loadImage();
            updateFrameAdjustments();
        }
    }

    private void copySpacecraftState()
    {
        int nslices = getNumberBands();
        for (int i = 0; i<nslices; i++)
        {
            spacecraftPositionAdjusted = MathUtil.copy(spacecraftPositionOriginal);
            frustum1Adjusted = MathUtil.copy(frustum1Original);
            frustum2Adjusted = MathUtil.copy(frustum2Original);
            frustum3Adjusted = MathUtil.copy(frustum3Original);
            frustum4Adjusted = MathUtil.copy(frustum4Original);
            boresightDirectionAdjusted = MathUtil.copy(boresightDirectionOriginal);
            upVectorAdjusted = MathUtil.copy(upVectorOriginal);
            sunPositionAdjusted = MathUtil.copy(sunPositionOriginal);
        }
    }

    public void resetSpacecraftState()
    {
        copySpacecraftState();
        int nslices = getNumberBands();
        for (int i = 0; i<nslices; i++)
        {
            frusta[i] = null;
            footprintGenerated[i] = false;
        }

        targetPixelCoordinates[0] = -1.0;
        targetPixelCoordinates[1] = -1.0;
        rotationOffset[0] = 0.0;
        zoomFactor[0] = 1.0;

        updateFrameAdjustments();

        loadFootprint();
        calculateFrustum();
        saveImageInfo();
    }

    protected double getFocalLength() { return 0.0; }

    private double numberOfPixels = 0.0;
    protected double getNumberOfPixels() { return numberOfPixels; }

    private double numberOfLines = 0.0;
    protected double getNumberOfLines() { return numberOfLines; }

    protected double getPixelWidth() { return 0.0; }

    protected double getPixelHeight() { return 0.0; }

    public float getMinValue()
    {
        return minValue[currentSlice];
    }

    public float getMinValue(int slice)
    {
        return minValue[slice];
    }

    public void setMinValue(float minValue)
    {
        this.minValue[currentSlice] = minValue;
    }

    public float getMaxValue()
    {
        return maxValue[currentSlice];
    }

    public float getMaxValue(int slice)
    {
        return maxValue[slice];
    }

    public void setMaxValue(float maxValue)
    {
        this.maxValue[currentSlice] = maxValue;
    }

    public double[] getScalarRange(int slice)
    {
        double[] result = { minValue[slice], maxValue[slice] };
        return result;
    }

    public boolean shiftBands() { return false; }

    protected int getNumberBands()
    {
        return 1;
    }

    /**
     * Returns the number of spectra the image contains
     *
     * @return number of spectra
     */
    public int getNumberOfSpectralSegments() { return 0; }

    /**
     * For a multispectral image, returns an array of doubles containing the wavelengths for each point
     * on the image's spectrum.
     *
     * @return array of spectrum wavelengths
     */
    public double[] getSpectrumWavelengths(int segment) { return null; }

    /**
     * For a multispectral image, returns an array of doubles containing the bandwidths for each point
     * on the image's spectrum.
     *
     * @return array of spectrum wavelengths
     */
    public double[] getSpectrumBandwidths(int segment) { return null; }

    /**
     * For a multispectral image, returns an array of doubles containing the values for each point
     * on the image's spectrum.
     *
     * @return array of spectrum values
     */
    public double[] getSpectrumValues(int segment) { return null; }

    public String getSpectrumWavelengthUnits() { return null; }

    public String getSpectrumValueUnits() { return null; }

    /**
     * For a multispectral image, specify a region in pixel space over which to calculate the spectrum values.
     * The array is an Nx2 array of 2-dimensional vertices in pixel coordinates.
     * First index indicates the vertex, the second index indicates which of the two pixel coordinates.
     * A vertices array of height 1 will specify a single pixel region. An array of h 2 will specify a circular
     * region where the first value is the center and the second value is a point the circle. An array of size
     * 3 or more will specify a polygonal region.
     *
     * @param vertices of region
     */
    public void setSpectrumRegion(double[][] vertices) { }

    public void setFrustumOffset(double[] frustumCenterPixel)
    {
//        System.out.println("setFrustumOffset(): " + frustumCenterPixel[1] + " " + frustumCenterPixel[0]);

        this.targetPixelCoordinates[0] = frustumCenterPixel[0];
        this.targetPixelCoordinates[1] = frustumCenterPixel[1];

        updateFrameAdjustments();

        loadFootprint();
        calculateFrustum();
        saveImageInfo();
    }

    public void setRotationOffset(double offset)
    {
//        System.out.println("setRotationOffset(): " + offset);

        if (rotationOffset == null)
            rotationOffset = new double[1];

        rotationOffset[0] = offset;

        updateFrameAdjustments();

        loadFootprint();
        calculateFrustum();
        saveImageInfo();
    }

    public void setZoomFactor(double offset)
    {
//        System.out.println("setZoomFactor(): " + offset);

        if (zoomFactor == null)
        {
            zoomFactor = new double[1];
            zoomFactor[0] = 1.0;
        }

        zoomFactor[0] = offset;

        updateFrameAdjustments();

        loadFootprint();
        calculateFrustum();
        saveImageInfo();
    }

    public void setApplyFrameAdjustments(boolean state)
    {
//        System.out.println("setApplyFrameAdjustments(): " + state);
        applyFrameAdjustments[0] = state;
        updateFrameAdjustments();
        loadFootprint();
        calculateFrustum();
        saveImageInfo();
    }

    public boolean getApplyFramedAdjustments() { return applyFrameAdjustments[0]; }

    private void updateFrameAdjustments()
    {
        // adjust wrt the original spacecraft pointing direction, not the previous adjusted one
        copySpacecraftState();

        if (applyFrameAdjustments[0])
        {
            if (targetPixelCoordinates[0] >= 0.0 && targetPixelCoordinates[1] >= 0.0)
            {
                int height = getImageHeight();
                int width = getImageWidth();
                double line = height - 1 - targetPixelCoordinates[0];
                double sample = targetPixelCoordinates[1];

                if (line >= 0 && line < height && sample >= 0 && sample < width)
                {
                    double[] newTargetPixelDirection = getPixelDirection(sample, line);
                    rotateTargetPixelDirectionToLocalOrigin(newTargetPixelDirection);
                }
            }
            if (rotationOffset[0] != 0.0)
            {
                rotateFrameAboutTarget(rotationOffset[0]);
            }
            if (zoomFactor[0] != 1.0)
            {
                zoomFrame(zoomFactor[0]);
            }
        }

//        int slice = getCurrentSlice();
        int nslices = getNumberBands();
        for (int slice = 0; slice<nslices; slice++)
        {
            frusta[slice] = null;
            footprintGenerated[slice] = false;
        }
    }




    private void zoomFrame(double zoomFactor)
    {
//        System.out.println("zoomFrame(" + zoomFactor + ")");
        Vector3D spacecraftPositionVector = new Vector3D(spacecraftPositionOriginal[currentSlice]);
        Vector3D spacecraftToOriginVector = spacecraftPositionVector.scalarMultiply(-1.0);
        Vector3D originPointingVector = spacecraftToOriginVector.normalize();
        double distance = spacecraftToOriginVector.getNorm();
        Vector3D deltaVector = originPointingVector.scalarMultiply(distance * (zoomFactor - 1.0));
        double[] delta = { deltaVector.getX(), deltaVector.getY(), deltaVector.getZ() };

//        int slice = getCurrentSlice();
        int nslices = getNumberBands();
        for (int slice = 0; slice<nslices; slice++)
        {
            MathUtil.vadd(spacecraftPositionOriginal[currentSlice], delta, spacecraftPositionAdjusted[currentSlice]);

            frusta[slice] = null;
            footprintGenerated[slice] = false;
        }
    }


    private void rotateFrameAboutTarget(double angleDegrees)
    {
//        Vector3D axis = new Vector3D(boresightDirectionOriginal[currentSlice]);
        Vector3D axis = new Vector3D(spacecraftPositionAdjusted[currentSlice]);
        axis.normalize();
        axis.negate();
        Rotation rotation = new Rotation(axis, Math.toRadians(angleDegrees));

//        int slice = getCurrentSlice();
        int nslices = getNumberBands();
        for (int slice = 0; slice<nslices; slice++)
        {
            MathUtil.rotateVector(frustum1Adjusted[slice], rotation, frustum1Adjusted[slice]);
            MathUtil.rotateVector(frustum2Adjusted[slice], rotation, frustum2Adjusted[slice]);
            MathUtil.rotateVector(frustum3Adjusted[slice], rotation, frustum3Adjusted[slice]);
            MathUtil.rotateVector(frustum4Adjusted[slice], rotation, frustum4Adjusted[slice]);
            MathUtil.rotateVector(boresightDirectionAdjusted[slice], rotation, boresightDirectionAdjusted[slice]);

            frusta[slice] = null;
            footprintGenerated[slice] = false;
        }
    }

    public void moveTargetPixelCoordinates(double[] pixelDelta)
    {
//        System.out.println("moveFrustumOffset(): " + pixelDelta[1] + " " + pixelDelta[0]);

        double height = (double)getImageHeight();
        double width = (double)getImageWidth();
        if (targetPixelCoordinates[0] < 0.0 || targetPixelCoordinates[1] < 0.0)
        {
            targetPixelCoordinates = getPixelFromPoint(bodyOrigin);
            targetPixelCoordinates[0] = height - 1 - targetPixelCoordinates[0];
        }
        double line = this.targetPixelCoordinates[0] + pixelDelta[0];
        double sample = targetPixelCoordinates[1] + pixelDelta[1];
        double[] newFrustumCenterPixel = { line, sample };

        if (line >= 0.0 && line < height && sample >= 0.0 && sample < width)
        {
            setFrustumOffset(newFrustumCenterPixel);
        }
    }

    public void moveRotationAngleBy(double rotationDelta)
    {
//        System.out.println("moveRotationAngleBy(): " + rotationDelta);

        double newRotationOffset = rotationOffset[0] + rotationDelta;

        setRotationOffset(newRotationOffset);
    }

    public void moveZoomFactorBy(double zoomDelta)
    {
//        System.out.println("moveZoomDeltaBy(): " + zoomDelta);

        double newZoomFactor = zoomFactor[0] * (1.0 + zoomDelta);

        setZoomFactor(newZoomFactor);
    }

//    private void rotateBoresightDirectionTo(double[] newDirection)
//    {
//        Vector3D oldDirectionVector = new Vector3D(boresightDirectionOriginal[currentSlice]);
//        Vector3D newDirectionVector = new Vector3D(newDirection);
//
//        Rotation rotation = new Rotation(oldDirectionVector, newDirectionVector);
//
//        int nslices = getNumberBands();
//        for (int i = 0; i<nslices; i++)
//        {
//            MathUtil.rotateVector(frustum1Adjusted[i], rotation, frustum1Adjusted[i]);
//            MathUtil.rotateVector(frustum2Adjusted[i], rotation, frustum2Adjusted[i]);
//            MathUtil.rotateVector(frustum3Adjusted[i], rotation, frustum3Adjusted[i]);
//            MathUtil.rotateVector(frustum4Adjusted[i], rotation, frustum4Adjusted[i]);
//            MathUtil.rotateVector(boresightDirectionAdjusted[i], rotation, boresightDirectionAdjusted[i]);
//
//            frusta[i] = null;
//            footprintGenerated[i] = false;
//        }
//
////        loadFootprint();
////        calculateFrustum();
//    }

    private static double[] origin = { 0.0, 0.0, 0.0 };

    private void rotateTargetPixelDirectionToLocalOrigin(double[] direction)
    {
        Vector3D directionVector = new Vector3D(direction);
        Vector3D spacecraftPositionVector = new Vector3D(spacecraftPositionOriginal[currentSlice]);
        Vector3D spacecraftToOriginVector = spacecraftPositionVector.scalarMultiply(-1.0);
        Vector3D originPointingVector = spacecraftToOriginVector.normalize();

        Rotation rotation = new Rotation(directionVector, originPointingVector);

//        int slice = getCurrentSlice();
        int nslices = getNumberBands();
        for (int slice = 0; slice<nslices; slice++)
        {
            MathUtil.rotateVector(frustum1Adjusted[slice], rotation, frustum1Adjusted[slice]);
            MathUtil.rotateVector(frustum2Adjusted[slice], rotation, frustum2Adjusted[slice]);
            MathUtil.rotateVector(frustum3Adjusted[slice], rotation, frustum3Adjusted[slice]);
            MathUtil.rotateVector(frustum4Adjusted[slice], rotation, frustum4Adjusted[slice]);
            MathUtil.rotateVector(boresightDirectionAdjusted[slice], rotation, boresightDirectionAdjusted[slice]);

            frusta[slice] = null;
            footprintGenerated[slice] = false;
        }
    }

    public void calculateFrustum()
    {
//        System.out.println("recalculateFrustum()");
        vtkPolyData frus = new vtkPolyData();

        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(2);

        double dx = MathUtil.vnorm(spacecraftPositionAdjusted[currentSlice]) + smallBodyModel.getBoundingBoxDiagonalLength();
        double[] origin = spacecraftPositionAdjusted[currentSlice];
        double[] UL = {origin[0]+frustum1Adjusted[currentSlice][0]*dx, origin[1]+frustum1Adjusted[currentSlice][1]*dx, origin[2]+frustum1Adjusted[currentSlice][2]*dx};
        double[] UR = {origin[0]+frustum2Adjusted[currentSlice][0]*dx, origin[1]+frustum2Adjusted[currentSlice][1]*dx, origin[2]+frustum2Adjusted[currentSlice][2]*dx};
        double[] LL = {origin[0]+frustum3Adjusted[currentSlice][0]*dx, origin[1]+frustum3Adjusted[currentSlice][1]*dx, origin[2]+frustum3Adjusted[currentSlice][2]*dx};
        double[] LR = {origin[0]+frustum4Adjusted[currentSlice][0]*dx, origin[1]+frustum4Adjusted[currentSlice][1]*dx, origin[2]+frustum4Adjusted[currentSlice][2]*dx};

        points.InsertNextPoint(spacecraftPositionAdjusted[currentSlice]);
        points.InsertNextPoint(UL);
        points.InsertNextPoint(UR);
        points.InsertNextPoint(LL);
        points.InsertNextPoint(LR);

        idList.SetId(0, 0);
        idList.SetId(1, 1);
        lines.InsertNextCell(idList);
        idList.SetId(0, 0);
        idList.SetId(1, 2);
        lines.InsertNextCell(idList);
        idList.SetId(0, 0);
        idList.SetId(1, 3);
        lines.InsertNextCell(idList);
        idList.SetId(0, 0);
        idList.SetId(1, 4);
        lines.InsertNextCell(idList);

        frus.SetPoints(points);
        frus.SetLines(lines);


        vtkPolyDataMapper frusMapper = new vtkPolyDataMapper();
        frusMapper.SetInputData(frus);

        frustumActor.SetMapper(frusMapper);
    }

    /**
     * Return the multispectral image's spectrum region in pixel space.
     *
     * @return array describing region over which the spectrum is calculated.
     */
    public double[][] getSpectrumRegion() { return null; }

    public void setPickedPosition(double[] position)
    {
//        System.out.println("PerspectiveImage.setPickedPosition(): " + position[0] + ", " + position[1] + ", " + position[2]);
        double[] pixelPosition = getPixelFromPoint(position);
        double[][] region = { { pixelPosition[0], pixelPosition[1] } };
        setSpectrumRegion(region);
    }

    public double[] getPixelFromPoint(double[] pt)
    {
        double[] uv = new double[2];
        Frustum frustum = getFrustum();
        frustum.computeTextureCoordinatesFromPoint(pt, getImageWidth(), getImageHeight(), uv);

        double[] pixel = new double[2];
        pixel[0] = uv[0] * getImageHeight();
        pixel[1] = uv[1] * getImageWidth();

        return pixel;
    }

    public double getPixelDistance(double[] pt1, double[] pt2)
    {
        double[] pixel1 = getPixelFromPoint(pt1);
        double[] pixel2 = getPixelFromPoint(pt2);

        return MathUtil.distanceBetween(pixel1, pixel2);
    }

    protected void loadImageInfo(
            String infoFilename,
            int startSlice,        // for loading multiple info files, the starting array index to put the info into
            boolean pad,           // if true, will pad out the rest of the array with the same info
            String[] startTime,
            String[] stopTime,
            double[][] spacecraftPosition,
            double[][] sunPosition,
            double[][] frustum1,
            double[][] frustum2,
            double[][] frustum3,
            double[][] frustum4,
            double[][] boresightDirection,
            double[][] upVector,
            double[] targetPixelCoordinates,
            boolean[] applyFrameAdjustments) throws NumberFormatException, IOException
    {
        boolean offset = true;

        FileInputStream fs = null;
        try {
            fs = new FileInputStream(infoFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        // for multispectral images, the image slice being currently parsed
        int slice = startSlice - 1;

        String str;
        while ((str = in.readLine()) != null)
        {
            StringTokenizer st = new StringTokenizer(str);
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                if (token == null)
                    continue;

                if (START_TIME.equals(token))
                {
                    st.nextToken();
                    startTime[0] = st.nextToken();
                }
                if (STOP_TIME.equals(token))
                {
                    st.nextToken();
                    stopTime[0] = st.nextToken();
                }
// eventually, we should parse the number of exposures from the INFO file, for now it is hard-coded -turnerj1
//                if (NUMBER_EXPOSURES.equals(token))
//                {
//                    numberExposures = Integer.parseInt(st.nextToken());
//                    if (numberExposures > 1)
//                    {
//                        spacecraftPosition = new double[numberExposures][3];
//                        frustum1 = new double[numberExposures][3];
//                        frustum2 = new double[numberExposures][3];
//                        frustum3 = new double[numberExposures][3];
//                        frustum4 = new double[numberExposures][3];
//                        sunVector = new double[numberExposures][3];
//                        boresightDirection = new double[numberExposures][3];
//                        upVector = new double[numberExposures][3];
//                        frusta = new Frustum[numberExposures];
//                        footprint = new vtkPolyData[numberExposures];
//                        footprintCreated = new boolean[numberExposures];
//                        shiftedFootprint = new vtkPolyData[numberExposures];
//                    }
//                }
                // For backwards compatibility with MSI images we use the endsWith function
                // rather than equals for FRUSTUM1, FRUSTUM2, FRUSTUM3, FRUSTUM4, BORESIGHT_DIRECTION
                // and UP_DIRECTION since these are all prefixed with MSI_ in the info file.
                if (token.equals(TARGET_PIXEL_COORD))
                {
                    st.nextToken();
                    st.nextToken();
                    double x = Double.parseDouble(st.nextToken());
                    st.nextToken();
                    double y = Double.parseDouble(st.nextToken());
                    targetPixelCoordinates[0] = x;
                    targetPixelCoordinates[1] = y;
                }
                if (token.equals(TARGET_ROTATION))
                {
                    st.nextToken();
                    double x = Double.parseDouble(st.nextToken());
                    rotationOffset[0] = x;
                }
                if (token.equals(TARGET_ZOOM_FACTOR))
                {
                    st.nextToken();
                    double x = Double.parseDouble(st.nextToken());
                    zoomFactor[0] = x;
                }
                if (token.equals(APPLY_ADJUSTMENTS))
                {
                    st.nextToken();
                    offset = Boolean.parseBoolean(st.nextToken());
                    applyFrameAdjustments[0] = offset;
                }

                if (SPACECRAFT_POSITION.equals(token) ||
                        SUN_POSITION_LT.equals(token) ||
                        token.endsWith(FRUSTUM1) ||
                        token.endsWith(FRUSTUM2) ||
                        token.endsWith(FRUSTUM3) ||
                        token.endsWith(FRUSTUM4) ||
                        token.endsWith(BORESIGHT_DIRECTION) ||
                        token.endsWith(UP_DIRECTION))
                {
                    st.nextToken();
                    st.nextToken();
                    double x = Double.parseDouble(st.nextToken());
                    st.nextToken();
                    double y = Double.parseDouble(st.nextToken());
                    st.nextToken();
                    double z = Double.parseDouble(st.nextToken());
                    if (SPACECRAFT_POSITION.equals(token))
                    {
                        // SPACECRAFT_POSITION is assumed to be at the start of a frame, so increment slice count
                        slice++;
                        spacecraftPosition[slice][0] = x;
                        spacecraftPosition[slice][1] = y;
                        spacecraftPosition[slice][2] = z;
                    }
                    if (SUN_POSITION_LT.equals(token))
                    {
                        sunPosition[slice][0] = x;
                        sunPosition[slice][1] = y;
                        sunPosition[slice][2] = z;
//                        MathUtil.vhat(sunPosition[slice], sunPosition[slice]);
                    }
                    else if (token.endsWith(FRUSTUM1))
                    {
                        frustum1[slice][0] = x;
                        frustum1[slice][1] = y;
                        frustum1[slice][2] = z;
                        MathUtil.vhat(frustum1[slice], frustum1[slice]);
                    }
                    else if (token.endsWith(FRUSTUM2))
                    {
                        frustum2[slice][0] = x;
                        frustum2[slice][1] = y;
                        frustum2[slice][2] = z;
                        MathUtil.vhat(frustum2[slice], frustum2[slice]);
                    }
                    else if (token.endsWith(FRUSTUM3))
                    {
                        frustum3[slice][0] = x;
                        frustum3[slice][1] = y;
                        frustum3[slice][2] = z;
                        MathUtil.vhat(frustum3[slice], frustum3[slice]);
                    }
                    else if (token.endsWith(FRUSTUM4))
                    {
                        frustum4[slice][0] = x;
                        frustum4[slice][1] = y;
                        frustum4[slice][2] = z;
                        MathUtil.vhat(frustum4[slice], frustum4[slice]);
                    }
                    if (token.endsWith(BORESIGHT_DIRECTION))
                    {
                        boresightDirection[slice][0] = x;
                        boresightDirection[slice][1] = y;
                        boresightDirection[slice][2] = z;
                    }
                    if (token.endsWith(UP_DIRECTION))
                    {
                        upVector[slice][0] = x;
                        upVector[slice][1] = y;
                        upVector[slice][2] = z;
                    }
                }
            }
        }

        // once we've read in all the frames, pad out any additional missing frames
        if (pad)
        {
            int nslices = getNumberBands();
            for (int i=slice+1; i<nslices; i++)
            {
                spacecraftPosition[i][0] = spacecraftPosition[slice][0];
                spacecraftPosition[i][1] = spacecraftPosition[slice][1];
                spacecraftPosition[i][2] = spacecraftPosition[slice][2];

                sunPosition[i][0] = sunPosition[slice][0];
                sunPosition[i][1] = sunPosition[slice][1];
                sunPosition[i][2] = sunPosition[slice][2];

                frustum1[i][0] = frustum1[slice][0];
                frustum1[i][1] = frustum1[slice][1];
                frustum1[i][2] = frustum1[slice][2];

                frustum2[i][0] = frustum2[slice][0];
                frustum2[i][1] = frustum2[slice][1];
                frustum2[i][2] = frustum2[slice][2];

                frustum3[i][0] = frustum3[slice][0];
                frustum3[i][1] = frustum3[slice][1];
                frustum3[i][2] = frustum3[slice][2];

                frustum4[i][0] = frustum4[slice][0];
                frustum4[i][1] = frustum4[slice][1];
                frustum4[i][2] = frustum4[slice][2];

                boresightDirection[i][0] = boresightDirection[slice][0];
                boresightDirection[i][1] = boresightDirection[slice][1];
                boresightDirection[i][2] = boresightDirection[slice][2];

                upVector[slice][0] = upVector[slice][0];
                upVector[slice][1] = upVector[slice][1];
                upVector[slice][2] = upVector[slice][2];
            }
        }

        in.close();
    }



    public void saveImageInfo(
            String infoFilename,
            int slice,        // currently, we only support single-frame INFO files
            String startTime,
            String stopTime,
            double[][] spacecraftPosition,
            double[][] sunPosition,
            double[][] frustum1,
            double[][] frustum2,
            double[][] frustum3,
            double[][] frustum4,
            double[][] boresightDirection,
            double[][] upVector,
            double[] targetPixelCoordinates,
            boolean applyFrameAdjustments) throws NumberFormatException, IOException
    {
        // for testing purposes only:
//        infoFilename = infoFilename + ".txt";
//        System.out.println("Saving infofile to: " + infoFilename);
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(infoFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        OutputStreamWriter osw = new OutputStreamWriter(fs);
        BufferedWriter out = new BufferedWriter(osw);

        out.write(String.format("%-20s= %s\n", START_TIME, startTime));
        out.write(String.format("%-20s= %s\n", STOP_TIME, stopTime));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", SPACECRAFT_POSITION, spacecraftPosition[slice][0], spacecraftPosition[slice][1], spacecraftPosition[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", BORESIGHT_DIRECTION, boresightDirection[slice][0], boresightDirection[slice][1], boresightDirection[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", UP_DIRECTION, upVector[slice][0], upVector[slice][1], upVector[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", FRUSTUM1, frustum1[slice][0], frustum1[slice][1], frustum1[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", FRUSTUM2, frustum2[slice][0], frustum2[slice][1], frustum2[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", FRUSTUM3, frustum3[slice][0], frustum3[slice][1], frustum3[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", FRUSTUM4, frustum4[slice][0], frustum4[slice][1], frustum4[slice][2]));
        out.write(String.format("%-20s= ( %1.16e , %1.16e , %1.16e )\n", SUN_POSITION_LT, sunPosition[slice][0], sunPosition[slice][1], sunPosition[slice][2]));

        boolean writeApplyAdustments = false;

        if (targetPixelCoordinates[0] != 0.0 && targetPixelCoordinates[1] != 0.0)
        {
            out.write(String.format("%-20s= ( %1.16e , %1.16e )\n", TARGET_PIXEL_COORD, targetPixelCoordinates[0], targetPixelCoordinates[1]));
            writeApplyAdustments = true;
        }

        if (zoomFactor[0] != 0.0)
        {
            out.write(String.format("%-20s= %1.16e\n", TARGET_ZOOM_FACTOR, zoomFactor[0]));
            writeApplyAdustments = true;
        }

        if (rotationOffset[0] != 0.0)
        {
            out.write(String.format("%-20s= %1.16e\n", TARGET_ROTATION, rotationOffset[0]));
            writeApplyAdustments = true;
        }

        // only write out user-modified offsets if the image info has been modified
        if (writeApplyAdustments)
            out.write(String.format("%-20s= %b\n", APPLY_ADJUSTMENTS, applyFrameAdjustments));

        out.close();
    }



    /**
     * Return the default mask sizes as a 4 element integer array where the:
     * first  element is the top    mask size,
     * second element is the right  mask size,
     * third  element is the bottom mask size,
     * fourth element is the left   mask size.
     * @return
     */
    abstract protected int[] getMaskSizes();

    abstract protected String initializeFitFileFullPath();
    protected String initializePngFileFullPath() { return null; }
    abstract protected String initializeLabelFileFullPath();
    abstract protected String initializeInfoFileFullPath();
    abstract protected String initializeSumfileFullPath();

    protected void appendWithPadding(StringBuffer strbuf, String str)
    {
        strbuf.append(str);

        int length = str.length();
        while(length < 78)
        {
            strbuf.append(' ');
            ++length;
        }

        strbuf.append("\r\n");
    }

    public String generateBackplanesLabel(String imgName) throws IOException
    {
        StringBuffer strbuf = new StringBuffer("");

        int numBands = 16;

        appendWithPadding(strbuf, "PDS_VERSION_ID               = PDS3");
        appendWithPadding(strbuf, "");

        appendWithPadding(strbuf, "PRODUCT_TYPE                 = DDR");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String dateStr = sdf.format(date).replace(' ', 'T');
        appendWithPadding(strbuf, "PRODUCT_CREATION_TIME        = " + dateStr);
        appendWithPadding(strbuf, "PRODUCER_INSTITUTION_NAME    = \"APPLIED PHYSICS LABORATORY\"");
        appendWithPadding(strbuf, "SOFTWARE_NAME                = \"Small Body Mapping Tool\"");
        appendWithPadding(strbuf, "SHAPE_MODEL                  = \"" + smallBodyModel.getModelName() + "\"");

        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "/* This DDR label describes one data file:                               */");
        appendWithPadding(strbuf, "/* 1. A multiple-band backplane image file with wavelength-independent,  */");
        appendWithPadding(strbuf, "/* spatial pixel-dependent geometric and timing information.             */");
        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "OBJECT                       = FILE");

        appendWithPadding(strbuf, "  ^IMAGE                     = \"" + imgName + "\"");

        appendWithPadding(strbuf, "  RECORD_TYPE                = FIXED_LENGTH");
        appendWithPadding(strbuf, "  RECORD_BYTES               = " + (imageHeight * 4));
        appendWithPadding(strbuf, "  FILE_RECORDS               = " + (imageWidth * numBands));
        appendWithPadding(strbuf, "");

        appendWithPadding(strbuf, "  OBJECT                     = IMAGE");
        appendWithPadding(strbuf, "    LINES                    = " + imageHeight);
        appendWithPadding(strbuf, "    LINE_SAMPLES             = " + imageWidth);
        appendWithPadding(strbuf, "    SAMPLE_TYPE              = IEEE_REAL");
        appendWithPadding(strbuf, "    SAMPLE_BITS              = 32");
        appendWithPadding(strbuf, "    CORE_NULL                = 16#F49DC5AE#"); // bit pattern of -1.0e32 in hex

        appendWithPadding(strbuf, "    BANDS                    = " + numBands);
        appendWithPadding(strbuf, "    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL");
        appendWithPadding(strbuf, "    BAND_NAME                = (\"Pixel value\",");
        appendWithPadding(strbuf, "                                \"x coordinate of center of pixel, km\",");
        appendWithPadding(strbuf, "                                \"y coordinate of center of pixel, km\",");
        appendWithPadding(strbuf, "                                \"z coordinate of center of pixel, km\",");
        appendWithPadding(strbuf, "                                \"Latitude, deg\",");
        appendWithPadding(strbuf, "                                \"Longitude, deg\",");
        appendWithPadding(strbuf, "                                \"Distance from center of body, km\",");
        appendWithPadding(strbuf, "                                \"Incidence angle, deg\",");
        appendWithPadding(strbuf, "                                \"Emission angle, deg\",");
        appendWithPadding(strbuf, "                                \"Phase angle, deg\",");
        appendWithPadding(strbuf, "                                \"Horizontal pixel scale, km per pixel\",");
        appendWithPadding(strbuf, "                                \"Vertical pixel scale, km per pixel\",");
        appendWithPadding(strbuf, "                                \"Slope, deg\",");
        appendWithPadding(strbuf, "                                \"Elevation, m\",");
        appendWithPadding(strbuf, "                                \"Gravitational acceleration, m/s^2\",");
        appendWithPadding(strbuf, "                                \"Gravitational potential, J/kg\")");
        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "  END_OBJECT                 = IMAGE");
        appendWithPadding(strbuf, "END_OBJECT                   = FILE");

        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "END");

        return strbuf.toString();
    }

    /**
     * Get filter as an integer id. Return -1 if no filter is available.
     * @return
     */
    public int getFilter()
    {
        return -1;
    }

    /**
     * Get filter name as string. By default cast filter id to string.
     * Return null if filter id is negative.
     * @return
     */
    public String getFilterName()
    {
        int filter = getFilter();
        if (filter < 0)
            return null;
        else
            return String.valueOf(filter);
    }

    /**
     * Return the camera id. We assign an integer id to each camera.
     * For example, if there are 2 cameras on the spacecraft, return
     * either 1 or 2. If there are 2 spacecrafts each with a single
     * camera, then also return either 1 or 2. Return -1 if camera is
     * not available.
     *
     * @return
     */
    public int getCamera()
    {
        return -1;
    }

    /**
     * Get camera name as string. By default cast camera id to string.
     * Return null if camera id is negative.
     * @return
     */
    public String getCameraName()
    {
        int camera = getCamera();
        if (camera < 0)
            return null;
        else
            return String.valueOf(camera);
    }

    public int getImageWidth()
    {
        return imageWidth;
    }

    public int getImageHeight()
    {
        return imageHeight;
    }

    public int getImageDepth()
    {
        return imageDepth;
    }

    public String getPngFileFullPath()
    {
        return pngFileFullPath;
    }

    public String getFitFileFullPath()
    {
        return fitFileFullPath;
    }

    public String getImageFileFullPath()
    {
        return fitFileFullPath != null ? fitFileFullPath : pngFileFullPath;
    }

    public String[] getFitFilesFullPath()
    {
        String[] result = { fitFileFullPath };
        return result;
    }

    public String getLabelFileFullPath()
    {
        return labelFileFullPath;
    }

    public String getInfoFileFullPath()
    {
        return infoFileFullPath;
    }

    public String[] getInfoFilesFullPath()
    {
        String[] result = { infoFileFullPath };
        return result;
    }

    public String getSumfileFullPath()
    {
        return sumfileFullPath;
    }

    public String getLabelfileFullPath()
    {
        return labelfileFullPath;
    }

    /**
     *  Give oppurtunity to subclass to do some processing on the raw
     *  image such as resizing, flipping, masking, etc.
     *
     * @param rawImage
     */
    protected void processRawImage(vtkImageData rawImage)
    {
        // By default do nothing
    }

    protected vtkImageData createRawImage(int height, int width, int depth, float[][] array2D, float[][][] array3D)
    {
        return createRawImage(height, width, depth, true, array2D, array3D);
    }

    protected vtkImageData createRawImage(int height, int width, int depth, boolean transpose, float[][] array2D, float[][][] array3D)
    {
        vtkImageData image = new vtkImageData();
        if (transpose)
            image.SetDimensions(width, height, depth);
        else
            image.SetDimensions(height, width, depth);
        image.SetSpacing(1.0, 1.0, 1.0);
        image.SetOrigin(0.0, 0.0, 0.0);
        image.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);

        maxValue = new float[depth];
        minValue = new float[depth];

        for (int k=0; k<depth; k++)
        {
            maxValue[k] = -Float.MAX_VALUE;
            minValue[k] = Float.MAX_VALUE;
        }

        // For performance, flatten out the 2D or 3D array into a 1D array and call
        // SetJavaArray directly on the pixel data since calling SetScalarComponentFromDouble
        // for every pixel takes too long.
        float[] array1D = new float[height * width * depth];

        for (int i=0; i<height; ++i)
            for (int j=0; j<width; ++j)
                for (int k=0; k<depth; k++)
                {
                    float value = 0.0f;
                    if (array2D != null)
                        value = array2D[i][j];
                    else if (array3D != null)
                        value = array3D[i][k][j];

                    if (transpose)
                        //image.SetScalarComponentFromDouble(j, height-1-i, k, 0, value);
                        array1D[(k * height + (height-1-i)) * width + j] = value;
                    else
                        //image.SetScalarComponentFromDouble(i, width-1-j, k, 0, value);
                        array1D[(k * width + (width-1-j)) * height + i] = value;

                    if (value > maxValue[k])
                        maxValue[k] = value;
                    if (value < minValue[k])
                        minValue[k] = value;
                }

        ((vtkFloatArray)image.GetPointData().GetScalars()).SetJavaArray(array1D);

        return image;
    }


    protected void loadImageCalibrationData(Fits f) throws FitsException, IOException
    {
        // to be overridden by subclasses that load calibration data
    }

    protected void loadPngFile()
    {
        String name = getPngFileFullPath();

        String imageFile = null;
        if (getKey().source == ImageSource.IMAGE_MAP)
            imageFile = FileCache.getFileFromServer(name).getAbsolutePath();
        else
            imageFile = getKey().name;

        if (rawImage == null)
            rawImage = new vtkImageData();

        vtkPNGReader reader = new vtkPNGReader();
        reader.SetFileName(imageFile);
        reader.Update();
        rawImage.DeepCopy(reader.GetOutput());

    }


    protected void loadFitsFiles() throws FitsException, IOException
    {
        String[] filenames = getFitFilesFullPath();
        String filename = filenames[0];

        float[][] array2D = null;
        float[][][] array3D = null;

        int[] fitsAxes = null;
        int fitsNAxes = 0;
        // height is axis 0
        int fitsHeight = 0;
        // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis is 2
        int fitsWidth = 0;
        // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
        int fitsDepth = 0;

        // single file images (e.g. LORRI and LEISA)
        if (filenames.length == 1)
        {
            Fits f = new Fits(filename);
            BasicHDU h = f.getHDU(0);

            fitsAxes = h.getAxes();
            fitsNAxes = fitsAxes.length;
            fitsHeight = fitsAxes[0];
            fitsWidth = fitsNAxes == 3 ? fitsAxes[2] : fitsAxes[1];
            fitsDepth = fitsNAxes == 3 ? fitsAxes[1] : 1;

            Object data = h.getData().getData();

            // for 3D arrays we consider the second axis the "spectral" axis
            if (data instanceof float[][][])
            {
                if (shiftBands())
                {
                    array3D = new float[fitsHeight][fitsWidth][fitsDepth];
                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                            for (int k=0; k<fitsDepth; ++k)
                            {
                                int w = i + j - fitsDepth / 2;
                                if (w >= 0 && w < fitsHeight)
                                    array3D[w][j][k] = ((float[][][])data)[i][j][k];
                            }

                }
                else
                    array3D = (float[][][])data;

//               System.out.println("3D pixel array detected: " + array3D.length + "x" + array3D[0].length + "x" + array3D[0][0].length);
            }
            else if (data instanceof float[][])
            {
                array2D = (float[][])data;
            }
            else if (data instanceof short[][])
            {
                short[][] arrayS = (short[][])data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i=0; i<fitsHeight; ++i)
                    for (int j=0; j<fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayS[i][j];
                    }
            }
            else if (data instanceof byte[][])
            {
                byte[][] arrayB = (byte[][])data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i=0; i<fitsHeight; ++i)
                    for (int j=0; j<fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayB[i][j] & 0xFF;
                    }
            }
            else
            {
                System.out.println("Data type not supported!");
                return;
            }

            // load in calibration info
            loadImageCalibrationData(f);

            f.getStream().close();
        }
        // for multi-file images (e.g. MVIC)
        else if (filenames.length > 1)
        {
            fitsDepth = filenames.length;
            fitsAxes = new int[3];
            fitsAxes[2] = fitsDepth;
            fitsNAxes = 3;

            for (int k=0; k<fitsDepth; k++)
            {
                Fits f = new Fits(filenames[k]);
                BasicHDU h = f.getHDU(0);

                int[] multiImageAxes = h.getAxes();
                int multiImageNAxes = multiImageAxes.length;

                if (multiImageNAxes > 2)
                {
                    System.out.println("Multi-file images must be 2D.");
                    return;
                }

                // height is axis 0, width is axis 1
                fitsHeight = fitsAxes[0] = multiImageAxes[0];
                fitsWidth = fitsAxes[2] = multiImageAxes[1];

                if (array3D == null)
                    array3D = new float[fitsHeight][fitsDepth][fitsWidth];


                Object data = h.getData().getData();

                if (data instanceof float[][])
                {
                    // NOTE: could performance be improved if depth was the first index and the entire 2D array could be assigned to a each slice? -turnerj1
                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                        {
                            array3D[i][k][j] = ((float[][])data)[i][j];
                        }
                }
                else if (data instanceof short[][])
                {
                    short[][] arrayS = (short[][])data;

                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                        {
                            array3D[i][k][j] = arrayS[i][j];
                        }
                }
                else if (data instanceof byte[][])
                {
                    byte[][] arrayB = (byte[][])data;

                    for (int i=0; i<fitsHeight; ++i)
                        for (int j=0; j<fitsWidth; ++j)
                        {
                            array3D[i][k][j] = arrayB[i][j] & 0xFF;
                        }
                }
                else
                {
                    System.out.println("Data type not supported!");
                    return;
                }

                f.getStream().close();
            }
        }

        rawImage = createRawImage(fitsHeight, fitsWidth, fitsDepth, array2D, array3D);
    }

    protected void loadImage() throws FitsException, IOException
    {
        if (getFitFileFullPath() != null)
            loadFitsFiles();
        else if (getPngFileFullPath() != null)
            loadPngFile();

        if (rawImage == null)
            return;

        processRawImage(rawImage);

        int[] dims = rawImage.GetDimensions();
        imageWidth = dims[0];
        imageHeight = dims[1];
        imageDepth = dims[2];

        int[] masking = getMaskSizes();
        int topMask =    masking[0];
        int rightMask =  masking[1];
        int bottomMask = masking[2];
        int leftMask =   masking[3];
        for (int i=0; i<masking.length; ++i)
            currentMask[i] = masking[i];

        maskSource = new vtkImageCanvasSource2D();
        maskSource.SetScalarTypeToUnsignedChar();
        maskSource.SetNumberOfScalarComponents(1);
//        maskSource.SetExtent(0, imageWidth-1, 0, imageHeight-1, 0, imageDepth-1);
        maskSource.SetExtent(0, imageWidth-1, 0, imageHeight-1, 0, 0);
        // Initialize the mask to black which masks out the image
        maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
        maskSource.FillBox(0, imageWidth-1, 0, imageHeight-1);
        // Create a square inside mask which passes through the image.
        maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
        maskSource.FillBox(leftMask, imageWidth-1-rightMask, bottomMask, imageHeight-1-topMask);
        maskSource.Update();

        for (int k=0; k<imageDepth; k++)
        {
            footprint[k] = new vtkPolyData();
//            displayedRange[k] = new IntensityRange(1,0);
            displayedRange[k] = new IntensityRange(0,255);
        }

        shiftedFootprint[0] = new vtkPolyData();
        textureCoords = new vtkFloatArray();
        normalsFilter = new vtkPolyDataNormals();


        if (getFitFileFullPath() != null)
            setDisplayedImageRange(null);
        else if (getPngFileFullPath() != null)
        {
            double[] scalarRange = rawImage.GetScalarRange();
            minValue[0] = (float)scalarRange[0];
            maxValue[0] = (float)scalarRange[1];
//            setDisplayedImageRange(new IntensityRange(0, 255));
            setDisplayedImageRange(null);
        }


//        setDisplayedImageRange(new IntensityRange(0, 255));
    }

    protected void loadPointing() throws FitsException, IOException
    {
        if (key.source.equals(ImageSource.SPICE))
        {
            loadImageInfo();
        }
        else if (key.source.equals(ImageSource.LABEL))
        {
            try
            {
                loadLabelFile();
            }
            catch(IOException ex)
            {
                System.out.println("Label file not available");
            }
        }
        else if (key.source.equals(ImageSource.LOCAL_PERSPECTIVE))
        {
            loadImageInfo();
        }
        else
        {
            try
            {
                loadSumfile();
            }
            catch(IOException ex)
            {
                System.out.println("Sumfile not available");
            }
        }

        // copy loaded state values into the adjusted values
        copySpacecraftState();
    }

    public vtkImageData getRawImage()
    {
        return rawImage;
    }

    public vtkImageData getDisplayedImage()
    {
        return displayedImage;
    }

    public void setCurrentSlice(int slice)
    {
        this.currentSlice = slice;
    }

    public int getCurrentSlice()
    {
        return currentSlice;
    }

    public int getDefaultSlice() { return 0; }

    public void setUseDefaultFootprint(boolean useDefaultFootprint)
    {
        this.useDefaultFootprint = useDefaultFootprint;
        for (int i=0; i<getImageDepth(); i++)
        {
            footprintGenerated[i] = false;
        }
    }

    public boolean useDefaultFootprint() { return useDefaultFootprint; }

    public String getCurrentBand()
    {
        return Integer.toString(currentSlice);
    }

    public vtkTexture getTexture()
    {
        return imageTexture;
    }

    public static void setGenerateFootprint(boolean b)
    {
        generateFootprint = b;
    }

    public ArrayList<vtkProp> getProps()
    {
//        System.out.println("getProps()");
        if (footprintActor == null)
        {
            loadFootprint();

            imageTexture = new vtkTexture();
            imageTexture.InterpolateOn();
            imageTexture.RepeatOff();
            imageTexture.EdgeClampOn();
            imageTexture.SetInputData(displayedImage);

            vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
            footprintMapper.SetInputData(shiftedFootprint[0]);
            footprintMapper.Update();

            footprintActor = new vtkActor();
            footprintActor.SetMapper(footprintMapper);
            footprintActor.SetTexture(imageTexture);
            vtkProperty footprintProperty = footprintActor.GetProperty();
            footprintProperty.LightingOff();

            footprintActors.add(footprintActor);
        }

        if (frustumActor == null)
        {
            frustumActor = new vtkActor();

            calculateFrustum();

            vtkProperty frustumProperty = frustumActor.GetProperty();
            frustumProperty.SetColor(0.0, 1.0, 0.0);
            frustumProperty.SetLineWidth(2.0);
            frustumActor.VisibilityOff();

            footprintActors.add(frustumActor);
        }

        return footprintActors;
    }

    public void setShowFrustum(boolean b)
    {
        showFrustum = b;

        if (showFrustum)
        {
            frustumActor.VisibilityOn();
        }
        else
        {
            frustumActor.VisibilityOff();
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public boolean isFrustumShowing()
    {
        return showFrustum;
    }

    public double getMinIncidence()
    {
        return minIncidence;
    }

    public double getMaxIncidence()
    {
        return maxIncidence;
    }

    public double getMinEmission()
    {
        return minEmission;
    }

    public double getMaxEmission()
    {
        return maxEmission;
    }

    public double getMinPhase()
    {
        return minPhase;
    }

    public double getMaxPhase()
    {
        return maxPhase;
    }

    public IntensityRange getDisplayedRange()
    {
        return displayedRange[currentSlice];
    }

    public IntensityRange getDisplayedRange(int slice)
    {
        return displayedRange[slice];
    }

    public void setDisplayedImageRange()
    {
         setDisplayedImageRange(displayedRange[currentSlice]);
    }

    public void setDisplayedImageRange(IntensityRange range)
    {
        if (range == null || displayedRange[currentSlice].min != range.min || displayedRange[currentSlice].max != range.max)
        {
//            displayedRange[currentSlice] = range != null ? range : new IntensityRange(0, 255);
            if (range != null)
                displayedRange[currentSlice] = range;

            float minValue = getMinValue();
            float maxValue = getMaxValue();
            float dx = (maxValue-minValue)/255.0f;
            float min = minValue + displayedRange[currentSlice].min*dx;
            float max = minValue + displayedRange[currentSlice].max*dx;

            // Update the displayed image
            vtkLookupTable lut = new vtkLookupTable();
            lut.SetTableRange(min, max);
            lut.SetValueRange(0.0, 1.0);
            lut.SetHueRange(0.0, 0.0);
            lut.SetSaturationRange(0.0, 0.0);
            //lut.SetNumberOfTableValues(402);
            lut.SetRampToLinear();
            lut.Build();

            // for 3D images, take the current slice
            vtkImageData image2D = rawImage;
            if (imageDepth > 1)
            {
                vtkImageReslice slicer = new vtkImageReslice();
                slicer.SetInputData(rawImage);
                slicer.SetOutputDimensionality(2);
                slicer.SetInterpolationModeToNearestNeighbor();
                slicer.SetOutputSpacing(1.0, 1.0, 1.0);
                slicer.SetResliceAxesDirectionCosines(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);

                slicer.SetOutputOrigin(0.0, 0.0, (double)currentSlice);
                slicer. SetResliceAxesOrigin(0.0, 0.0, (double)currentSlice);

                slicer.SetOutputExtent(0, imageWidth-1, 0, imageHeight-1, 0, 0);

                slicer.Update();
                image2D = slicer.GetOutput();
            }

            vtkImageMapToColors mapToColors = new vtkImageMapToColors();
            mapToColors.SetInputData(image2D);
            mapToColors.SetOutputFormatToRGBA();
            mapToColors.SetLookupTable(lut);
            mapToColors.Update();

            vtkImageData mapToColorsOutput = mapToColors.GetOutput();
            vtkImageData maskSourceOutput = maskSource.GetOutput();

            vtkImageMask maskFilter = new vtkImageMask();
            maskFilter.SetImageInputData(mapToColorsOutput);
            maskFilter.SetMaskInputData(maskSourceOutput);
            maskFilter.Update();

            if (displayedImage == null)
                displayedImage = new vtkImageData();
            vtkImageData maskFilterOutput = maskFilter.GetOutput();
            displayedImage.DeepCopy(maskFilterOutput);

            maskFilter.Delete();
            mapToColors.Delete();
            lut.Delete();
            mapToColorsOutput.Delete();
            maskSourceOutput.Delete();
            maskFilterOutput.Delete();

            //vtkPNGWriter writer = new vtkPNGWriter();
            //writer.SetFileName("fit.png");
            //writer.SetInput(displayedImage);
            //writer.Write();

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

//    private static void printpt(double[] p, String s)
//    {
//        System.out.println(s + " " + p[0] + " " + p[1] + " " + p[2]);
//    }

    private void initSpacecraftStateVariables()
    {
        int nslices = getNumberBands();
        spacecraftPositionOriginal = new double[nslices][3];
        frustum1Original = new double[nslices][3];
        frustum2Original = new double[nslices][3];
        frustum3Original = new double[nslices][3];
        frustum4Original = new double[nslices][3];
        sunPositionOriginal = new double[nslices][3];
        boresightDirectionOriginal = new double[nslices][3];
        upVectorOriginal = new double[nslices][3];
        frusta = new Frustum[nslices];
        footprint = new vtkPolyData[nslices];
        footprintGenerated = new boolean[nslices];
        displayedRange = new IntensityRange[nslices];
    }

    private void loadImageInfo() throws NumberFormatException, IOException
    {
        String[] infoFileNames = getInfoFilesFullPath();

        int nfiles = infoFileNames.length;
        int nslices = getNumberBands();

        if (nslices > 1)
            initSpacecraftStateVariables();

        boolean pad = nfiles > 1;

        for (int k=0; k<nfiles; k++)
        {
            String[] start = new String[1];
            String[] stop = new String[1];
            boolean[] ato = new boolean[1];
            ato[0] = true;

            loadImageInfo(
                    infoFileNames[k],
                    k,
                    pad,
                    start,
                    stop,
                    spacecraftPositionOriginal,
                    sunPositionOriginal,
                    frustum1Original,
                    frustum2Original,
                    frustum3Original,
                    frustum4Original,
                    boresightDirectionOriginal,
                    upVectorOriginal,
                    targetPixelCoordinates,
                    ato);

            // should startTime and stopTime be an array? -turnerj1
            startTime = start[0];
            stopTime = stop[0];
            applyFrameAdjustments[0] = ato[0];

//            updateFrustumOffset();

//        printpt(frustum1, "pds frustum1 ");
//        printpt(frustum2, "pds frustum2 ");
//        printpt(frustum3, "pds frustum3 ");
//        printpt(frustum4, "pds frustum4 ");
        }
    }


    private void saveImageInfo()
    {
        String[] infoFileNames = getInfoFilesFullPath();

//        int slice = getCurrentSlice();
//        System.out.println("Saving current slice: " + slice);
        try
        {
//            int nslices = getNumberBands();
            int nslices = infoFileNames.length;
            for (int slice=0; slice<nslices; slice++)
            {
                saveImageInfo(
                        infoFileNames[slice],
                        slice,
                        startTime,
                        stopTime,
                        spacecraftPositionOriginal,
                        sunPositionOriginal,
                        frustum1Original,
                        frustum2Original,
                        frustum3Original,
                        frustum4Original,
                        boresightDirectionOriginal,
                        upVectorOriginal,
                        targetPixelCoordinates,
                        applyFrameAdjustments[0]);
            }
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void saveImageInfo(String infoFileName)
    {
        try
        {
                saveImageInfo(
                        infoFileName,
                        0,
                        startTime,
                        stopTime,
                        spacecraftPositionOriginal,
                        sunPositionOriginal,
                        frustum1Original,
                        frustum2Original,
                        frustum3Original,
                        frustum4Original,
                        boresightDirectionOriginal,
                        upVectorOriginal,
                        targetPixelCoordinates,
                        applyFrameAdjustments[0]);
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sometimes Bob Gaskell sumfiles contain numbers of the form
     * .1192696009D+03 rather than .1192696009E+03 (i.e. a D instead
     * of an E). This function replaces D's with E's.
     * @param s
     * @return
     */
    private void replaceDwithE(String[] s)
    {
        for (int i=0; i<s.length; ++i)
            s[i] = s[i].replace('D', 'E');
    }

    protected void loadSumfile(
            String sumfilename,
            String[] startTime,
            String[] stopTime,
            double[][] spacecraftPosition,
            double[][] sunVector,
            double[][] frustum1,
            double[][] frustum2,
            double[][] frustum3,
            double[][] frustum4,
            double[][] boresightDirection,
            double[][] upVector) throws IOException
    {
        FileInputStream fs = new FileInputStream(sumfilename);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        // for multispectral images, the image slice being currently parsed
        int slice = 0;

        in.readLine();

        String datetime = in.readLine().trim();
        datetime = DateTimeUtil.convertDateTimeFormat(datetime);
        startTime[0] = datetime;
        stopTime[0] = datetime;

        String[] tmp = in.readLine().trim().split("\\s+");
        double npx = Integer.parseInt(tmp[0]);
        double nln = Integer.parseInt(tmp[1]);

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        double focalLengthMillimeters = Double.parseDouble(tmp[0]);

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        spacecraftPosition[slice][0] = -Double.parseDouble(tmp[0]);
        spacecraftPosition[slice][1] = -Double.parseDouble(tmp[1]);
        spacecraftPosition[slice][2] = -Double.parseDouble(tmp[2]);

        double[] cx = new double[3];
        double[] cy = new double[3];
        double[] cz = new double[3];
        double[] sz = new double[3];

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        cx[0] = Double.parseDouble(tmp[0]);
        cx[1] = Double.parseDouble(tmp[1]);
        cx[2] = Double.parseDouble(tmp[2]);

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        cy[0] = Double.parseDouble(tmp[0]);
        cy[1] = Double.parseDouble(tmp[1]);
        cy[2] = Double.parseDouble(tmp[2]);

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        cz[0] = Double.parseDouble(tmp[0]);
        cz[1] = Double.parseDouble(tmp[1]);
        cz[2] = Double.parseDouble(tmp[2]);

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        sz[0] = Double.parseDouble(tmp[0]);
        sz[1] = Double.parseDouble(tmp[1]);
        sz[2] = Double.parseDouble(tmp[2]);

        tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        double kmatrix00 = Math.abs(Double.parseDouble(tmp[0]));
        double kmatrix11 = Math.abs(Double.parseDouble(tmp[4]));

        // Here we calculate the image width and height using the K-matrix values.
        // This is used only when the constructor of this function was called with
        // loadPointingOnly set to true. When set to false, the image width and
        // and height is set in the loadImage function (after this function is called
        // and will overwrite these values here--though they should not be different).
        // But when in pointing-only mode, the loadImage function is not called so
        // we therefore set the image width and height here since some functions need it.
        imageWidth = (int)npx;
        imageHeight = (int)nln;
        if (kmatrix00 > kmatrix11)
            imageHeight = (int)Math.round(nln * (kmatrix00 / kmatrix11));
        else if (kmatrix11 > kmatrix00)
            imageWidth = (int)Math.round(npx * (kmatrix11 / kmatrix00));

        double[] cornerVector = new double[3];
        double fov1 = Math.atan(npx/(2.0*focalLengthMillimeters*kmatrix00));
        double fov2 = Math.atan(nln/(2.0*focalLengthMillimeters*kmatrix11));
        cornerVector[0] = -Math.tan(fov1);
        cornerVector[1] = -Math.tan(fov2);
        cornerVector[2] = 1.0;

        double fx = cornerVector[0];
        double fy = cornerVector[1];
        double fz = cornerVector[2];
        frustum3[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum3[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum3[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = -cornerVector[0];
        fy = cornerVector[1];
        fz = cornerVector[2];
        frustum4[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum4[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum4[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = cornerVector[0];
        fy = -cornerVector[1];
        fz = cornerVector[2];
        frustum1[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum1[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum1[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = -cornerVector[0];
        fy = -cornerVector[1];
        fz = cornerVector[2];
        frustum2[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum2[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum2[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        MathUtil.vhat(frustum1[slice], frustum1[slice]);
        MathUtil.vhat(frustum2[slice], frustum2[slice]);
        MathUtil.vhat(frustum3[slice], frustum3[slice]);
        MathUtil.vhat(frustum4[slice], frustum4[slice]);

        MathUtil.vhat(cz, boresightDirection[slice]);
        MathUtil.vhat(cx, upVector[slice]);
        MathUtil.vhat(sz, sunVector[slice]);

        in.close();
    }

    //
    // Label (.lbl) file parsing methods
    //

    private static final Vector3D i = new Vector3D(1.0, 0.0, 0.0);
    private static final Vector3D j = new Vector3D(0.0, 1.0, 0.0);
    private static final Vector3D k = new Vector3D(0.0, 0.0, 1.0);

    private String targetName = null;
    private String instrumentId = null;
    private String filterName = null;
    private String objectName = null;

    private String startTimeString = null;
    private String stopTimeString = null;
    private double exposureDuration = 0.0;

    private String scTargetPositionString = null;
    private String targetSunPositionString = null;
    private String scOrientationString = null;
    private Rotation scOrientation = null;
    private double[] q = new double[4];
    private double[] cx = new double[3];
    private double[] cy = new double[3];
    private double[] cz = new double[3];

    private double focalLengthMillimeters = 100.0;
    private double npx = 4096.0;
    private double nln = 32.0;
    private double kmatrix00 = 1.0;
    private double kmatrix11 = 1.0;

    private void parseLabelKeyValuePair(
            String key,
            String value,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunVector,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws IOException
    {
        System.out.println("Label file key: " + key + " = " + value);

        if (key.equals("TARGET_NAME"))
            targetName = value;
        else if (key.equals("INSTRUMENT_ID"))
            instrumentId = value;
        else if (key.equals("FILTER_NAME"))
            filterName = value;
        else if (key.equals("OBJECT"))
            objectName = value;
        else if (key.equals("LINE_SAMPLES"))
        {
            if (objectName.equals("EXTENSION_CALGEOM_IMAGE"))
                numberOfPixels = Double.parseDouble(value);
        }
        else if (key.equals("LINES"))
        {
            if (objectName.equals("EXTENSION_CALGEOM_IMAGE"))
                numberOfLines = Double.parseDouble(value);
        }
        else if (key.equals("START_TIME"))
        {
            startTimeString = value;
            startTime[0] = startTimeString;
        }
        else if (key.equals("STOP_TIME"))
        {
            stopTimeString = value;
            stopTime[0] = stopTimeString;
        }
        else if (key.equals("SC_TARGET_POSITION_VECTOR"))
        {
            scTargetPositionString = value;
            String p[] = scTargetPositionString.split(",");
            spacecraftPosition[0] = Double.parseDouble(p[0].trim().split("\\s+")[0].trim());
            spacecraftPosition[1] = Double.parseDouble(p[1].trim().split("\\s+")[0].trim());
            spacecraftPosition[2] = Double.parseDouble(p[2].trim().split("\\s+")[0].trim());
        }
        else if (key.equals("TARGET_SUN_POSITION_VECTOR"))
        {
            targetSunPositionString = value;
            String p[] = targetSunPositionString.split(",");
            sunVector[0] = -Double.parseDouble(p[0].trim().split("\\s+")[0].trim());
            sunVector[1] = -Double.parseDouble(p[1].trim().split("\\s+")[0].trim());
            sunVector[2] = -Double.parseDouble(p[2].trim().split("\\s+")[0].trim());
        }
        else if (key.equals("QUATERNION"))
        {
            scOrientationString = value;
            String qstr[] = scOrientationString.split(",");
            q[0] = Double.parseDouble(qstr[0].trim().split("\\s+")[0].trim());
            q[1] = Double.parseDouble(qstr[1].trim().split("\\s+")[0].trim());
            q[2] = Double.parseDouble(qstr[2].trim().split("\\s+")[0].trim());
            q[3] = Double.parseDouble(qstr[3].trim().split("\\s+")[0].trim());
            scOrientation = new Rotation(q[0], q[1], q[2], q[3], false);
        }

    }

    protected void loadLabelFile(
            String labelFileName,
            String[] startTime,
            String[] stopTime,
            double[][] spacecraftPosition,
            double[][] sunVector,
            double[][] frustum1,
            double[][] frustum2,
            double[][] frustum3,
            double[][] frustum4,
            double[][] boresightDirection,
            double[][] upVector) throws IOException
    {
        System.out.println(labelFileName);

        // for multispectral images, the image slice being currently parsed
        int slice = 0;

        // open a file input stream
        FileInputStream fs = new FileInputStream(labelFileName);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        //
        // Parse each line of the stream and process each key-value pair,
        // merging multiline numeric ("vector") values into a single-line
        // string. Multi-line quoted strings are ignored.
        //
        boolean inStringLiteral = false;
        boolean inVector = false;
        List<String> vector = new ArrayList<String>();
        String key = null;
        String value = null;
        String line = null;
        while ((line = in.readLine()) != null)
        {
            if (line.length() == 0)
                continue;

            // for now, multi-line quoted strings are ignored (i.e. treated as comments)
            if (line.trim().equals("\""))
            {
                inStringLiteral = false;
                continue;
            }

            if (inStringLiteral)
                continue;

            // terminate a multi-line numeric value (a "vector")
            if (line.trim().equals(")"))
            {
                inVector = false;
                value = "";
                for (String element : vector)
                    value = value + element;

                parseLabelKeyValuePair(
                        key,
                        value,
                        startTime,
                        stopTime,
                        spacecraftPosition[slice],
                        sunVector[slice],
                        frustum1[slice],
                        frustum2[slice],
                        frustum3[slice],
                        frustum4[slice],
                        boresightDirection[slice],
                        upVector[slice]);

                vector.clear();
                continue;
            }

            // add a line to the current vector
            if (inVector)
            {
                vector.add(line.trim());
                continue;
            }

            // extract key value pair
            String tokens[] = line.split("=");
            if (tokens.length < 2)
                continue;

            key = tokens[0].trim();
            value = tokens[1].trim();

            // detect and ignore comments
            if (value.equals("\""))
            {
                inStringLiteral = true;
                continue;
            }

            // start to accumulate numeric vector values
            if (value.equals("("))
            {
                inVector = true;
                continue;
            }

            if (value.startsWith("("))
                value = stripBraces(value);
            else
                value = stripQuotes(value);

            parseLabelKeyValuePair(
                    key,
                    value,
                    startTime,
                    stopTime,
                    spacecraftPosition[slice],
                    sunVector[slice],
                    frustum1[slice],
                    frustum2[slice],
                    frustum3[slice],
                    frustum4[slice],
                    boresightDirection[slice],
                    upVector[slice]);

        }

        in.close();

        //
        // calculate image projection from the parsed parameters
        //
        this.focalLengthMillimeters = getFocalLength();
        this.npx = getNumberOfPixels();
        this.nln = getNumberOfLines();
        this.kmatrix00 = 1.0 / getPixelWidth();
        this.kmatrix11 = 1.0 / getPixelHeight();

        Vector3D boresightVector3D = scOrientation.applyTo(i);
        boresightDirection[slice][0] = cz[0] = boresightVector3D.getX();
        boresightDirection[slice][1] = cz[1] = boresightVector3D.getY();
        boresightDirection[slice][2] = cz[2] = boresightVector3D.getZ();

        Vector3D upVector3D = scOrientation.applyTo(j);
        upVector[slice][0] = cy[0] = upVector3D.getX();
        upVector[slice][1] = cy[1] = upVector3D.getY();
        upVector[slice][2] = cy[2] = upVector3D.getZ();

        Vector3D leftVector3D = scOrientation.applyTo(k);
        cx[0] = -leftVector3D.getX();
        cx[1] = -leftVector3D.getY();
        cx[2] = -leftVector3D.getZ();

//      double kmatrix00 = Math.abs(Double.parseDouble(tmp[0]));
//      double kmatrix11 = Math.abs(Double.parseDouble(tmp[4]));

      // Here we calculate the image width and height using the K-matrix values.
      // This is used only when the constructor of this function was called with
      // loadPointingOnly set to true. When set to false, the image width and
      // and height is set in the loadImage function (after this function is called
      // and will overwrite these values here--though they should not be different).
      // But when in pointing-only mode, the loadImage function is not called so
      // we therefore set the image width and height here since some functions need it.
      imageWidth = (int)npx;
      imageHeight = (int)nln;
//      if (kmatrix00 > kmatrix11)
//          imageHeight = (int)Math.round(nln * (kmatrix00 / kmatrix11));
//      else if (kmatrix11 > kmatrix00)
//          imageWidth = (int)Math.round(npx * (kmatrix11 / kmatrix00));

      double[] cornerVector = new double[3];
      double fov1 = Math.atan(npx/(2.0*focalLengthMillimeters*kmatrix00));
      double fov2 = Math.atan(nln/(2.0*focalLengthMillimeters*kmatrix11));
      cornerVector[0] = -Math.tan(fov1);
      cornerVector[1] = -Math.tan(fov2);
      cornerVector[2] = 1.0;

      double fx = cornerVector[0];
      double fy = cornerVector[1];
      double fz = cornerVector[2];
      frustum3[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum3[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum3[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = -cornerVector[0];
      fy = cornerVector[1];
      fz = cornerVector[2];
      frustum4[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum4[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum4[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = cornerVector[0];
      fy = -cornerVector[1];
      fz = cornerVector[2];
      frustum1[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum1[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum1[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = -cornerVector[0];
      fy = -cornerVector[1];
      fz = cornerVector[2];
      frustum2[slice][0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum2[slice][1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum2[slice][2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      MathUtil.vhat(frustum1[slice], frustum1[slice]);
      MathUtil.vhat(frustum2[slice], frustum2[slice]);
      MathUtil.vhat(frustum3[slice], frustum3[slice]);
      MathUtil.vhat(frustum4[slice], frustum4[slice]);


    }

    private String stripQuotes(String input)
    {
        String result = input;
        if (input.startsWith("\""))
            result = result.substring(1);
        if (input.endsWith("\""))
            result = result.substring(0, input.length()-2);
        return result;
    }

    private String stripBraces(String input)
    {
        String result = input;
        if (input.startsWith("("))
            result = result.substring(1);
        if (input.endsWith(")"))
            result = result.substring(0, input.length()-2);
        return result;
    }

    private void loadSumfile() throws NumberFormatException, IOException
    {
        String[] start = new String[1];
        String[] stop = new String[1];

        loadSumfile(
                getSumfileFullPath(),
                start,
                stop,
                spacecraftPositionOriginal,
                sunPositionOriginal,
                frustum1Original,
                frustum2Original,
                frustum3Original,
                frustum4Original,
                boresightDirectionOriginal,
                upVectorOriginal);

        startTime = start[0];
        stopTime = stop[0];

//        printpt(frustum1, "gas frustum1 ");
//        printpt(frustum2, "gas frustum2 ");
//        printpt(frustum3, "gas frustum3 ");
//        printpt(frustum4, "gas frustum4 ");
    }

    private void loadLabelFile() throws NumberFormatException, IOException
    {
        System.out.println("Loading label (.lbl) file...");
        String[] start = new String[1];
        String[] stop = new String[1];

        loadLabelFile(
                getLabelFileFullPath(),
                start,
                stop,
                spacecraftPositionOriginal,
                sunPositionOriginal,
                frustum1Original,
                frustum2Original,
                frustum3Original,
                frustum4Original,
                boresightDirectionOriginal,
                upVectorOriginal);

        startTime = start[0];
        stopTime = stop[0];

    }

    public boolean containsLimb()
    {
        //TODO Speed this up: Determine if there is a limb without computing the entire backplane.

        float[] bp = generateBackplanes(true);
        if (bp == null)
            return true;
        else
            return false;
    }

    public void loadFootprint()
    {
        if (generateFootprint)
        {
            vtkPolyData tmp = null;

            if (!footprintGenerated[currentSlice])
            {
                if (useDefaultFootprint())
                {
                    int defaultSlice = getDefaultSlice();
                    if (footprintGenerated[defaultSlice] == false)
                    {
                        footprint[defaultSlice] = smallBodyModel.computeFrustumIntersection(spacecraftPositionAdjusted[defaultSlice],
                                frustum1Adjusted[defaultSlice], frustum3Adjusted[defaultSlice], frustum4Adjusted[defaultSlice], frustum2Adjusted[defaultSlice]);
                        if (footprint[defaultSlice] == null)
                            return;

                        // Need to clear out scalar data since if coloring data is being shown,
                        // then the color might mix-in with the image.
                        footprint[defaultSlice].GetCellData().SetScalars(null);
                        footprint[defaultSlice].GetPointData().SetScalars(null);

                        footprintGenerated[defaultSlice] = true;
                    }

                    tmp = footprint[defaultSlice];

                }
                else
                {
                    tmp = smallBodyModel.computeFrustumIntersection(spacecraftPositionAdjusted[currentSlice],
                            frustum1Adjusted[currentSlice], frustum3Adjusted[currentSlice], frustum4Adjusted[currentSlice], frustum2Adjusted[currentSlice]);
                    if (tmp == null)
                        return;

                    // Need to clear out scalar data since if coloring data is being shown,
                    // then the color might mix-in with the image.
                    tmp.GetCellData().SetScalars(null);
                    tmp.GetPointData().SetScalars(null);
                }


                footprint[currentSlice].DeepCopy(tmp);

                footprintGenerated[currentSlice] = true;
            }

            vtkPointData pointData = footprint[currentSlice].GetPointData();
            pointData.SetTCoords(textureCoords);
            PolyDataUtil.generateTextureCoordinates(getFrustum(), getImageWidth(), getImageHeight(), footprint[currentSlice]);
            pointData.Delete();
        }
        else
        {
            int resolutionLevel = smallBodyModel.getModelResolution();

            String footprintFilename = null;
            File file = null;

            if (key.source == ImageSource.SPICE)
                footprintFilename = key.name + "_FOOTPRINT_RES" + resolutionLevel + "_PDS.VTP";
            else
                footprintFilename = key.name + "_FOOTPRINT_RES" + resolutionLevel + "_GASKELL.VTP";

            file = FileCache.getFileFromServer(footprintFilename);

            if (file == null || !file.exists())
            {
                System.out.println("Warning: " + footprintFilename + " not found");
                return;
            }

            vtkXMLPolyDataReader footprintReader = new vtkXMLPolyDataReader();
            footprintReader.SetFileName(file.getAbsolutePath());
            footprintReader.Update();

            vtkPolyData footprintReaderOutput = footprintReader.GetOutput();
            footprint[currentSlice].DeepCopy(footprintReaderOutput);
        }


        shiftedFootprint[0].DeepCopy(footprint[currentSlice]);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint[0], offset);
    }

    public vtkPolyData generateBoundary()
    {
        loadFootprint();

        if (footprint[currentSlice].GetNumberOfPoints() == 0)
            return null;

        vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
        edgeExtracter.SetInputData(footprint[currentSlice]);
        edgeExtracter.BoundaryEdgesOn();
        edgeExtracter.FeatureEdgesOff();
        edgeExtracter.NonManifoldEdgesOff();
        edgeExtracter.ManifoldEdgesOff();
        edgeExtracter.Update();

        vtkPolyData boundary = new vtkPolyData();
        vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
        boundary.DeepCopy(edgeExtracterOutput);

        return boundary;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public String getStopTime()
    {
        return stopTime;
    }

    public double getMinimumHorizontalPixelScale()
    {
        return minHorizontalPixelScale;
    }

    public double getMaximumHorizontalPixelScale()
    {
        return maxHorizontalPixelScale;
    }

    public double getMeanHorizontalPixelScale()
    {
        return meanHorizontalPixelScale;
    }

    public double getMinimumVerticalPixelScale()
    {
        return minVerticalPixelScale;
    }

    public double getMaximumVerticalPixelScale()
    {
        return maxVerticalPixelScale;
    }

    public double getMeanVerticalPixelScale()
    {
        return meanVerticalPixelScale;
    }

    public double getSpacecraftDistance()
    {
        return MathUtil.vnorm(spacecraftPositionAdjusted[currentSlice]);
     }

    private void computeCellNormals()
    {
        if (normalsGenerated == false)
        {
            normalsFilter.SetInputData(footprint[currentSlice]);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(0);
            normalsFilter.SplittingOff();
            normalsFilter.Update();

            vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
            footprint[currentSlice].DeepCopy(normalsFilterOutput);
            normalsGenerated = true;
        }
    }

    // Computes the incidence, emission, and phase at a point on the footprint with a given normal.
    // (I.e. the normal of the plate which the point is lying on).
    // The output is a 3-vector with the first component equal to the incidence,
    // the second component equal to the emission and the third component equal to
    // the phase.
    public double[] computeIlluminationAnglesAtPoint(
            double[] pt,
            double[] normal)
    {
        double[] scvec = {
            spacecraftPositionAdjusted[currentSlice][0] - pt[0],
            spacecraftPositionAdjusted[currentSlice][1] - pt[1],
            spacecraftPositionAdjusted[currentSlice][2] - pt[2]};

        double[] sunVectorAdjusted = getSunVector();
        double incidence = MathUtil.vsep(normal, sunVectorAdjusted) * 180.0 / Math.PI;
        double emission = MathUtil.vsep(normal, scvec) * 180.0 / Math.PI;
        double phase = MathUtil.vsep(sunVectorAdjusted, scvec) * 180.0 / Math.PI;

        double[] angles = {incidence, emission, phase};

        return angles;
    }

    protected void computeIlluminationAngles()
    {
        if (footprintGenerated[currentSlice] == false)
            loadFootprint();

        computeCellNormals();

        int numberOfCells = footprint[currentSlice].GetNumberOfCells();

        vtkPoints points = footprint[currentSlice].GetPoints();
        vtkCellData footprintCellData = footprint[currentSlice].GetCellData();
        vtkDataArray normals = footprintCellData.GetNormals();

        this.minEmission  =  Double.MAX_VALUE;
        this.maxEmission  = -Double.MAX_VALUE;
        this.minIncidence =  Double.MAX_VALUE;
        this.maxIncidence = -Double.MAX_VALUE;
        this.minPhase     =  Double.MAX_VALUE;
        this.maxPhase     = -Double.MAX_VALUE;

        for (int i=0; i<numberOfCells; ++i)
        {
            vtkCell cell = footprint[currentSlice].GetCell(i);
            double[] pt0 = points.GetPoint( cell.GetPointId(0) );
            double[] pt1 = points.GetPoint( cell.GetPointId(1) );
            double[] pt2 = points.GetPoint( cell.GetPointId(2) );
            double[] centroid = {
                    (pt0[0] + pt1[0] + pt2[0]) / 3.0,
                    (pt0[1] + pt1[1] + pt2[1]) / 3.0,
                    (pt0[2] + pt1[2] + pt2[2]) / 3.0
            };
            double[] normal = normals.GetTuple3(i);

            double[] angles = computeIlluminationAnglesAtPoint(centroid, normal);
            double incidence = angles[0];
            double emission  = angles[1];
            double phase     = angles[2];

            if (incidence < minIncidence)
                minIncidence = incidence;
            if (incidence > maxIncidence)
                maxIncidence = incidence;
            if (emission < minEmission)
                minEmission = emission;
            if (emission > maxEmission)
                maxEmission = emission;
            if (phase < minPhase)
                minPhase = phase;
            if (phase > maxPhase)
                maxPhase = phase;
            cell.Delete();
        }

        points.Delete();
        footprintCellData.Delete();
        if (normals != null)
            normals.Delete();
    }

    protected void computePixelScale()
    {
        if (footprintGenerated[currentSlice] == false)
            loadFootprint();

        int numberOfPoints = footprint[currentSlice].GetNumberOfPoints();

        vtkPoints points = footprint[currentSlice].GetPoints();

        minHorizontalPixelScale = Double.MAX_VALUE;
        maxHorizontalPixelScale = -Double.MAX_VALUE;
        meanHorizontalPixelScale = 0.0;
        minVerticalPixelScale = Double.MAX_VALUE;
        maxVerticalPixelScale = -Double.MAX_VALUE;
        meanVerticalPixelScale = 0.0;

        double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1Adjusted[currentSlice], frustum3Adjusted[currentSlice]) / 2.0 ) / imageHeight;
        double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1Adjusted[currentSlice], frustum2Adjusted[currentSlice]) / 2.0 ) / imageWidth;

        double[] vec = new double[3];

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            vec[0] = pt[0] - spacecraftPositionAdjusted[currentSlice][0];
            vec[1] = pt[1] - spacecraftPositionAdjusted[currentSlice][1];
            vec[2] = pt[2] - spacecraftPositionAdjusted[currentSlice][2];
            double dist = MathUtil.vnorm(vec);

            double horizPixelScale = dist * horizScaleFactor;
            double vertPixelScale = dist * vertScaleFactor;

            if (horizPixelScale < minHorizontalPixelScale)
                minHorizontalPixelScale = horizPixelScale;
            if (horizPixelScale > maxHorizontalPixelScale)
                maxHorizontalPixelScale = horizPixelScale;
            if (vertPixelScale < minVerticalPixelScale)
                minVerticalPixelScale = vertPixelScale;
            if (vertPixelScale > maxVerticalPixelScale)
                maxVerticalPixelScale = vertPixelScale;

            meanHorizontalPixelScale += horizPixelScale;
            meanVerticalPixelScale += vertPixelScale;
        }

        meanHorizontalPixelScale /= (double)numberOfPoints;
        meanVerticalPixelScale /= (double)numberOfPoints;

        points.Delete();
    }

    public float[] generateBackplanes()
    {
        return generateBackplanes(false);
    }

    /**
     * If <code>returnNullIfContainsLimb</code> then return null if any ray
     * in the direction of a pixel in the image does not intersect the asteroid.
     * By setting this boolean to true, you can (usually) determine whether or not the
     * image contains a limb without having to compute the entire backplane. Note
     * that this is a bit of a hack and a better way is needed to quickly determine
     * if there is a limb.
     *
     * @param returnNullIfContainsLimb
     * @return
     */
    private float[] generateBackplanes(boolean returnNullIfContainsLimb)
    {
        // We need to use cell normals not point normals for the calculations
        vtkDataArray normals = null;
        if (!returnNullIfContainsLimb)
            normals = smallBodyModel.getCellNormals();

        int numLayers = 16;
        float[] data = new float[numLayers*imageHeight*imageWidth];

        vtksbCellLocator cellLocator = smallBodyModel.getCellLocator();

        //vtkPoints intersectPoints = new vtkPoints();
        //vtkIdList intersectCells = new vtkIdList();
        vtkGenericCell cell = new vtkGenericCell();

        // For each pixel in the image we need to compute the vector
        // from the spacecraft pointing in the direction of that pixel.
        // To do this, for each row in the image compute the left and
        // right vectors of the entire row. Then for each pixel in
        // the row use the two vectors from either side to compute
        // the vector of that pixel.
        double[] corner1 = {
                spacecraftPositionAdjusted[currentSlice][0] + frustum1Adjusted[currentSlice][0],
                spacecraftPositionAdjusted[currentSlice][1] + frustum1Adjusted[currentSlice][1],
                spacecraftPositionAdjusted[currentSlice][2] + frustum1Adjusted[currentSlice][2]
        };
        double[] corner2 = {
                spacecraftPositionAdjusted[currentSlice][0] + frustum2Adjusted[currentSlice][0],
                spacecraftPositionAdjusted[currentSlice][1] + frustum2Adjusted[currentSlice][1],
                spacecraftPositionAdjusted[currentSlice][2] + frustum2Adjusted[currentSlice][2]
        };
        double[] corner3 = {
                spacecraftPositionAdjusted[currentSlice][0] + frustum3Adjusted[currentSlice][0],
                spacecraftPositionAdjusted[currentSlice][1] + frustum3Adjusted[currentSlice][1],
                spacecraftPositionAdjusted[currentSlice][2] + frustum3Adjusted[currentSlice][2]
        };
        double[] vec12 = {
                corner2[0] - corner1[0],
                corner2[1] - corner1[1],
                corner2[2] - corner1[2]
        };
        double[] vec13 = {
                corner3[0] - corner1[0],
                corner3[1] - corner1[1],
                corner3[2] - corner1[2]
        };

        double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1Adjusted[currentSlice], frustum3Adjusted[currentSlice]) / 2.0 ) / imageHeight;
        double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1Adjusted[currentSlice], frustum2Adjusted[currentSlice]) / 2.0 ) / imageWidth;

        double scdist = MathUtil.vnorm(spacecraftPositionAdjusted[currentSlice]);

        for (int i=0; i<imageHeight; ++i)
        {
            // Compute the vector on the left of the row.
            double fracHeight = ((double)i / (double)(imageHeight-1));
            double[] left = {
                    corner1[0] + fracHeight*vec13[0],
                    corner1[1] + fracHeight*vec13[1],
                    corner1[2] + fracHeight*vec13[2]
            };

            for (int j=0; j<imageWidth; ++j)
            {
                // If we're just trying to know if there is a limb, we
                // only need to do intersections around the boundary of
                // the backplane, not the interior pixels.
                if (returnNullIfContainsLimb)
                {
                    if (j == 1 && i > 0 && i < imageHeight-1)
                    {
                        j = imageWidth-2;
                        continue;
                    }
                }

                double fracWidth = ((double)j / (double)(imageWidth-1));
                double[] vec = {
                        left[0] + fracWidth*vec12[0],
                        left[1] + fracWidth*vec12[1],
                        left[2] + fracWidth*vec12[2]
                };
                vec[0] -= spacecraftPositionAdjusted[currentSlice][0];
                vec[1] -= spacecraftPositionAdjusted[currentSlice][1];
                vec[2] -= spacecraftPositionAdjusted[currentSlice][2];
                MathUtil.unorm(vec, vec);

                double[] lookPt = {
                        spacecraftPositionAdjusted[currentSlice][0] + 2.0*scdist*vec[0],
                        spacecraftPositionAdjusted[currentSlice][1] + 2.0*scdist*vec[1],
                        spacecraftPositionAdjusted[currentSlice][2] + 2.0*scdist*vec[2]
                };

                //cellLocator.IntersectWithLine(spacecraftPosition, lookPt, intersectPoints, intersectCells);
                double tol = 1e-6;
                double[] t = new double[1];
                double[] x = new double[3];
                double[] pcoords = new double[3];
                int[] subId = new int[1];
                int[] cellId = new int[1];
                int result = cellLocator.IntersectWithLine(spacecraftPositionAdjusted[currentSlice], lookPt, tol, t, x, pcoords, subId, cellId, cell);

                //if (intersectPoints.GetNumberOfPoints() == 0)
                //    System.out.println(i + " " + j + " " + intersectPoints.GetNumberOfPoints());

                //int numberOfPoints = intersectPoints.GetNumberOfPoints();

                if (result > 0)
                {
                    // If we're just trying to know if there is a limb, do not
                    // compute the values of the backplane (It will crash since
                    // we don't have normals of the asteroid itself)
                    if (returnNullIfContainsLimb)
                        continue;

                    //double[] closestPoint = intersectPoints.GetPoint(0);
                    //int closestCell = intersectCells.GetId(0);
                    double[] closestPoint = x;
                    int closestCell = cellId[0];
                    double closestDist = MathUtil.distanceBetween(closestPoint, spacecraftPositionAdjusted[currentSlice]);

                    /*
                    // compute the closest point to the spacecraft of all the intersecting points.
                    if (numberOfPoints > 1)
                    {
                        for (int k=1; k<numberOfPoints; ++k)
                        {
                            double[] pt = intersectPoints.GetPoint(k);
                            double dist = GeometryUtil.distanceBetween(pt, spacecraftPosition);
                            if (dist < closestDist)
                            {
                                closestDist = dist;
                                closestCell = intersectCells.GetId(k);
                                closestPoint = pt;
                            }
                        }
                    }
                    */

                    LatLon llr = MathUtil.reclat(closestPoint);
                    double lat = llr.lat * 180.0 / Math.PI;
                    double lon = llr.lon * 180.0 / Math.PI;
                    if (lon < 0.0)
                        lon += 360.0;

                    double[] normal = normals.GetTuple3(closestCell);
                    double[] illumAngles = computeIlluminationAnglesAtPoint(closestPoint, normal);

                    double horizPixelScale = closestDist * horizScaleFactor;
                    double vertPixelScale = closestDist * vertScaleFactor;

                    double[] coloringValues = smallBodyModel.getAllColoringValues(closestPoint);

                    data[index(j,i,0)]  = (float)rawImage.GetScalarComponentAsFloat(j, i, 0, 0);
                    data[index(j,i,1)]  = (float)closestPoint[0];
                    data[index(j,i,2)]  = (float)closestPoint[1];
                    data[index(j,i,3)]  = (float)closestPoint[2];
                    data[index(j,i,4)]  = (float)lat;
                    data[index(j,i,5)]  = (float)lon;
                    data[index(j,i,6)]  = (float)llr.rad;
                    data[index(j,i,7)]  = (float)illumAngles[0];
                    data[index(j,i,8)]  = (float)illumAngles[1];
                    data[index(j,i,9)]  = (float)illumAngles[2];
                    data[index(j,i,10)] = (float)horizPixelScale;
                    data[index(j,i,11)] = (float)vertPixelScale;
                    data[index(j,i,12)] = (float)coloringValues[0]; // slope
                    data[index(j,i,13)] = (float)coloringValues[1]; // elevation;
                    data[index(j,i,14)] = (float)coloringValues[2]; // grav acc;
                    data[index(j,i,15)] = (float)coloringValues[3]; // grav pot;
                }
                else
                {
                    if (returnNullIfContainsLimb)
                        return null;

                    data[index(j,i,0)]  = (float)rawImage.GetScalarComponentAsFloat(j, i, 0, 0);
                    for (int k=1; k<numLayers; ++k)
                        data[index(j,i,k)] = PDS_NA;
                }
            }
        }

        return data;
    }

    public int index(int i, int j, int k)
    {
        return ((k * imageHeight + j) * imageWidth + i);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            loadFootprint();
            normalsGenerated = false;
            this.minEmission  =  Double.MAX_VALUE;
            this.maxEmission  = -Double.MAX_VALUE;
            this.minIncidence =  Double.MAX_VALUE;
            this.maxIncidence = -Double.MAX_VALUE;
            this.minPhase     =  Double.MAX_VALUE;
            this.maxPhase     = -Double.MAX_VALUE;
            this.minHorizontalPixelScale = Double.MAX_VALUE;
            this.maxHorizontalPixelScale = -Double.MAX_VALUE;
            this.minVerticalPixelScale = Double.MAX_VALUE;
            this.maxVerticalPixelScale = -Double.MAX_VALUE;
            this.meanHorizontalPixelScale = 0.0;
            this.meanVerticalPixelScale = 0.0;

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    /**
     * The shifted footprint is the original footprint shifted slightly in the
     * normal direction so that it will be rendered correctly and not obscured
     * by the asteroid.
     * @return
     */
    public vtkPolyData getShiftedFootprint()
    {
        return shiftedFootprint[0];
    }

    /**
     * The original footprint whose cells exactly overlap the original asteroid.
     * If rendered as is, it would interfere with the asteroid.
     * @return
     */
    public vtkPolyData getUnshiftedFootprint()
    {
       return footprint[currentSlice];
    }

    public void Delete()
    {
        displayedImage.Delete();
        rawImage.Delete();
        for (int i=0; i<imageDepth; i++)
        {
            footprint[i].Delete();
            shiftedFootprint[i].Delete();
        }

        textureCoords.Delete();
        normalsFilter.Delete();
        maskSource.Delete();
    }

    public void getCameraOrientation(double[] spacecraftPosition,
            double[] focalPoint, double[] upVector)
    {

        for (int i=0; i<3; ++i)
        {
            spacecraftPosition[i] = this.spacecraftPositionAdjusted[currentSlice][i];
            upVector[i] = this.upVectorAdjusted[currentSlice][i];
        }

        // Normalize the direction vector
        double[] direction = new double[3];
        MathUtil.unorm(boresightDirectionAdjusted[currentSlice], direction);

        int cellId = smallBodyModel.computeRayIntersection(spacecraftPosition, direction, focalPoint);

        if (cellId < 0)
        {
            BoundingBox bb = new BoundingBox(footprint[currentSlice].GetBounds());
            double[] centerPoint = bb.getCenterPoint();
            //double[] centerPoint = footprint[currentSlice].GetPoint(0);
            double distanceToCenter = MathUtil.distanceBetween(spacecraftPosition, centerPoint);

            focalPoint[0] = spacecraftPosition[0] + distanceToCenter*direction[0];
            focalPoint[1] = spacecraftPosition[1] + distanceToCenter*direction[1];
            focalPoint[2] = spacecraftPosition[2] + distanceToCenter*direction[2];
        }
    }

    /**
     * Same as previous but return a (4 element) quaternion instead.
     * First element is the scalar followed by the 3 element vector.
     * Also returns a rotation matrix.
     * @param spacecraftPosition
     * @param quaternion
     * @return Rotation matrix
     */
    public Rotation getCameraOrientation(double[] spacecraftPosition,
            double[] quaternion)
    {
        double[] cx = upVectorAdjusted[currentSlice];
        double[] cz = new double[3];
        MathUtil.unorm(boresightDirectionAdjusted[currentSlice], cz);

        double[] cy = new double[3];
        MathUtil.vcrss(cz, cx, cy);

        double[][] m = {
                {cx[0], cx[1], cx[2]},
                {cy[0], cy[1], cy[2]},
                {cz[0], cz[1], cz[2]}
        };

        Rotation rotation = new Rotation(m, 1.0e-6);

        for (int i=0; i<3; ++i)
            spacecraftPosition[i] = this.spacecraftPositionAdjusted[currentSlice][i];

        quaternion[0] = rotation.getQ0();
        quaternion[1] = rotation.getQ1();
        quaternion[2] = rotation.getQ2();
        quaternion[3] = rotation.getQ3();

        return rotation;
    }

    public Frustum getFrustum(int slice)
    {
        if (useDefaultFootprint())
        {
            int defaultSlice = getDefaultSlice();
            if (frusta[defaultSlice] == null)
                frusta[defaultSlice] = new Frustum(spacecraftPositionAdjusted[defaultSlice], frustum1Adjusted[defaultSlice], frustum3Adjusted[defaultSlice], frustum4Adjusted[defaultSlice], frustum2Adjusted[defaultSlice]);
            return frusta[defaultSlice];
        }

        if (frusta[slice] == null)
            frusta[slice] = new Frustum(spacecraftPositionAdjusted[slice], frustum1Adjusted[slice], frustum3Adjusted[slice], frustum4Adjusted[slice], frustum2Adjusted[slice]);
        return frusta[slice];
    }

    public Frustum getFrustum()
    {
        return getFrustum(currentSlice);
    }

    /**
     *  Get the maximum FOV angle in degrees of the image (the max of either
     *  the horizontal or vetical FOV). I.e., return the
     *  angular separation in degrees between two corners of the frustum where the
     *  two corners are both on the longer side.
     *
     * @return
     */
    public double getMaxFovAngle()
    {
        return Math.max(getHorizontalFovAngle(), getVerticalFovAngle());
    }

    public double getHorizontalFovAngle()
    {
        double fovHoriz = MathUtil.vsep(frustum1Adjusted[currentSlice], frustum3Adjusted[currentSlice]) * 180.0 / Math.PI;
        return fovHoriz;
    }

    public double getVerticalFovAngle()
    {
        double fovVert = MathUtil.vsep(frustum1Adjusted[currentSlice], frustum2Adjusted[currentSlice]) * 180.0 / Math.PI;
        return fovVert;
    }

    public double[] getSpacecraftPosition()
    {
        return spacecraftPositionAdjusted[currentSlice];
    }

    public double[] getSunPosition()
    {
        return sunPositionAdjusted[currentSlice];
    }

    public double[] getSunVector()
    {
        double[] result = new double[3];
        MathUtil.vhat(sunPositionAdjusted[currentSlice], result);
        return result;
    }

    public double[] getBoresightDirection()
    {
        return boresightDirectionAdjusted[currentSlice];
    }

    public double[] getUpVector()
    {
        return upVectorAdjusted[currentSlice];
    }

    public double[] getPixelDirection(int sample, int line)
    {
        return getPixelDirection((double)sample, (double)line, currentSlice);
    }

    public double[] getPixelDirection(double sample, double line)
    {
        return getPixelDirection((double)sample, (double)line, currentSlice);
    }


    /**
     * Get the direction from the spacecraft of pixel with specified sample and line.
     * Note that sample is along image width and line is along image height.
     */
    public double[] getPixelDirection(double sample, double line, int slice)
    {
        double[] corner1 = {
                spacecraftPositionAdjusted[slice][0] + frustum1Adjusted[slice][0],
                spacecraftPositionAdjusted[slice][1] + frustum1Adjusted[slice][1],
                spacecraftPositionAdjusted[slice][2] + frustum1Adjusted[slice][2]
        };
        double[] corner2 = {
                spacecraftPositionAdjusted[slice][0] + frustum2Adjusted[slice][0],
                spacecraftPositionAdjusted[slice][1] + frustum2Adjusted[slice][1],
                spacecraftPositionAdjusted[slice][2] + frustum2Adjusted[slice][2]
        };
        double[] corner3 = {
                spacecraftPositionAdjusted[slice][0] + frustum3Adjusted[slice][0],
                spacecraftPositionAdjusted[slice][1] + frustum3Adjusted[slice][1],
                spacecraftPositionAdjusted[slice][2] + frustum3Adjusted[slice][2]
        };
        double[] vec12 = {
                corner2[0] - corner1[0],
                corner2[1] - corner1[1],
                corner2[2] - corner1[2]
        };
        double[] vec13 = {
                corner3[0] - corner1[0],
                corner3[1] - corner1[1],
                corner3[2] - corner1[2]
        };

        // Compute the vector on the left of the row.
        double fracHeight = ((double)line / (double)(imageHeight-1));
        double[] left = {
                corner1[0] + fracHeight*vec13[0],
                corner1[1] + fracHeight*vec13[1],
                corner1[2] + fracHeight*vec13[2]
        };

        double fracWidth = ((double)sample / (double)(imageWidth-1));
        double[] dir = {
                left[0] + fracWidth*vec12[0],
                left[1] + fracWidth*vec12[1],
                left[2] + fracWidth*vec12[2]
        };
        dir[0] -= spacecraftPositionAdjusted[slice][0];
        dir[1] -= spacecraftPositionAdjusted[slice][1];
        dir[2] -= spacecraftPositionAdjusted[slice][2];
        MathUtil.unorm(dir, dir);

        return dir;
    }

    /**
     * Get point on surface that intersects a ray originating from spacecraft
     * in direction of pixel with specified sample and line.
     * Note that sample is along image width and line is along image height.
     * If there is no intersect point, null is returned.
     */
    public double[] getPixelSurfaceIntercept(int sample, int line)
    {
        double[] dir = getPixelDirection(sample, line);

        double[] intersectPoint = new double[3];

        int result = smallBodyModel.computeRayIntersection(spacecraftPositionAdjusted[currentSlice], dir, intersectPoint);

        if (result >= 0)
            return intersectPoint;
        else
            return null;
    }

    public void setVisible(boolean b)
    {
        footprintActor.SetVisibility(b ? 1 : 0);
        super.setVisible(b);
    }

    public double getDefaultOffset()
    {
        return 3.0*smallBodyModel.getMinShiftAmount();
    }

    public void setOffset(double offset)
    {
        this.offset = offset;

        shiftedFootprint[0].DeepCopy(footprint[currentSlice]);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint[0], offset);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getOffset()
    {
        return offset;
    }

    public void imageAboutToBeRemoved()
    {
        setShowFrustum(false);
    }

    public int getNumberOfComponentsOfOriginalImage()
    {
        return rawImage.GetNumberOfScalarComponents();
    }

    /**
     * Return surface area of footprint (unshifted) of image.
     * @return
     */
    public double getSurfaceArea()
    {
        return PolyDataUtil.getSurfaceArea(footprint[currentSlice]);
    }

    public double getOpacity()
    {
        return imageOpacity;
    }

    public void setOpacity(double imageOpacity)
    {
        this.imageOpacity = imageOpacity;
        vtkProperty smallBodyProperty = footprintActor.GetProperty();
        smallBodyProperty.SetOpacity(imageOpacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public LinkedHashMap<String, String> getProperties() throws IOException
    {
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();

        if (getMaxPhase() < getMinPhase())
        {
            this.computeIlluminationAngles();
            this.computePixelScale();
        }

        DecimalFormat df = new DecimalFormat("#.######");

        properties.put("Name", new File(getImageFileFullPath()).getName()); //TODO remove extension and possibly prefix
        properties.put("Start Time", getStartTime());
        properties.put("Stop Time", getStopTime());
        properties.put("Spacecraft Distance", df.format(getSpacecraftDistance()) + " km");
        properties.put("Spacecraft Position",
                df.format(spacecraftPositionAdjusted[currentSlice][0]) + ", " + df.format(spacecraftPositionAdjusted[currentSlice][1]) + ", " + df.format(spacecraftPositionAdjusted[currentSlice][2]) + " km");
        double[] quaternion = new double[4];
        double[] notused = new double[4];
        getCameraOrientation(notused, quaternion);
        properties.put("Spacecraft Orientation (quaternion)",
                "(" + df.format(quaternion[0]) + ", [" + df.format(quaternion[1]) + ", " + df.format(quaternion[2]) + ", " + df.format(quaternion[3]) + "])");
        double[] sunVectorAdjusted = getSunVector();
        properties.put("Sun Vector",
                df.format(sunVectorAdjusted[0]) + ", " + df.format(sunVectorAdjusted[1]) + ", " + df.format(sunVectorAdjusted[2]));
        if (getCameraName() != null)
            properties.put("Camera", getCameraName());
        if (getFilterName() != null)
            properties.put("Filter", getFilterName());

        // Note \u00B2 is the unicode superscript 2 symbol
        String ss2 = "\u00B2";
        properties.put("Footprint Surface Area", df.format(getSurfaceArea()) + " km" + ss2);

        // Note \u00B0 is the unicode degree symbol
        String deg = "\u00B0";
        properties.put("FOV", df.format(getHorizontalFovAngle())+deg + " x " + df.format(getVerticalFovAngle())+deg);

        properties.put("Minimum Incidence", df.format(getMinIncidence())+deg);
        properties.put("Maximum Incidence", df.format(getMaxIncidence())+deg);
        properties.put("Minimum Emission", df.format(getMinEmission())+deg);
        properties.put("Maximum Emission", df.format(getMaxIncidence())+deg);
        properties.put("Minimum Phase", df.format(getMinPhase())+deg);
        properties.put("Maximum Phase", df.format(getMaxPhase())+deg);
        properties.put("Minimum Horizontal Pixel Scale", df.format(1000.0*getMinimumHorizontalPixelScale()) + " meters/pixel");
        properties.put("Maximum Horizontal Pixel Scale", df.format(1000.0*getMaximumHorizontalPixelScale()) + " meters/pixel");
        properties.put("Minimum Vertical Pixel Scale", df.format(1000.0*getMinimumVerticalPixelScale()) + " meters/pixel");
        properties.put("Maximum Vertical Pixel Scale", df.format(1000.0*getMaximumVerticalPixelScale()) + " meters/pixel");

        return properties;
    }

    public void firePropertyChange()
    {
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setCurrentMask(int[] masking)
    {
        int topMask =    masking[0];
        int rightMask =  masking[1];
        int bottomMask = masking[2];
        int leftMask =   masking[3];
        // Initialize the mask to black which masks out the image
        maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
        maskSource.FillBox(0, imageWidth-1, 0, imageHeight-1);
        // Create a square inside mask which passes through the image.
        maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
        maskSource.FillBox(leftMask, imageWidth-1-rightMask, bottomMask, imageHeight-1-topMask);
        maskSource.Update();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        setDisplayedImageRange(null);

        for (int i=0; i<masking.length; ++i)
            currentMask[i] = masking[i];
    }

    public int[] getCurrentMask()
    {
        return currentMask.clone();
    }
}
