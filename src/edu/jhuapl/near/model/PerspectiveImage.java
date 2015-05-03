package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import vtk.vtkLookupTable;
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
    public static final String START_TIME = "START_TIME";
    public static final String STOP_TIME = "STOP_TIME";
    public static final String SPACECRAFT_POSITION = "SPACECRAFT_POSITION";
    public static final String SUN_POSITION_LT = "SUN_POSITION_LT";

    private SmallBodyModel smallBodyModel;

//    private vtkImageData raw3DImage;
    private vtkImageData rawImage;
    private vtkImageData displayedImage;
    private int[] axes;
    private int naxes = 0;
    private int currentSlice = 127;

    private vtkPolyData footprint;
    private vtkPolyData shiftedFootprint;
    private vtkActor footprintActor;
    private ArrayList<vtkProp> footprintActors = new ArrayList<vtkProp>();

    private vtkActor frustumActor;

    private vtkPolyDataNormals normalsFilter;

    private vtkFloatArray textureCoords;

    private boolean footprintGenerated = false;
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

    private float minValue;
    private float maxValue;

    private IntensityRange displayedRange = new IntensityRange(1,0);
    private double imageOpacity = 1.0;

    private double[] spacecraftPosition = new double[3];
    private double[] frustum1 = new double[3];
    private double[] frustum2 = new double[3];
    private double[] frustum3 = new double[3];
    private double[] frustum4 = new double[3];
    private double[] sunVector = new double[3];

    private double[] boresightDirection = new double[3];
    private double[] upVector = new double[3];

    private boolean showFrustum = false;

    private String startTime = "";
    private String stopTime = "";

    private vtkImageCanvasSource2D maskSource;

    private int imageWidth;

    private int imageHeight;

    private String fitFileFullPath; // The actual path of the image stored on the local disk (after downloading from the server)
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

    /**
     * If loadPointingOnly is true then only pointing information about this
     * image will be downloaded/loaded. The image itself will not be loaded.
     * Used by ImageBoundary to get pointing info.
     */
    public PerspectiveImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key);
        this.smallBodyModel = smallBodyModel;

        this.offset = getDefaultOffset();

        if (!loadPointingOnly)
        {
            fitFileFullPath = initializeFitFileFullPath();
        }

        if (key.source.equals(ImageSource.SPICE))
            infoFileFullPath = initializeInfoFileFullPath();
        else if (key.source.equals(ImageSource.LABEL))
            labelFileFullPath = initializeLabelFileFullPath();
        else
            sumfileFullPath = initializeSumfileFullPath();

        loadPointing();

        if (!loadPointingOnly)
            loadImage();
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
        return minValue;
    }

    public void setMinValue(float minValue)
    {
        this.minValue = minValue;
    }

    public float getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(float maxValue)
    {
        this.maxValue = maxValue;
    }

    protected void loadImageInfo(
            String lblFilename,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunVector,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws NumberFormatException, IOException
    {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(lblFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

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
                // For backwards compatibility with MSI images we use the endsWith function
                // rather than equals for FRUSTUM1, FRUSTUM2, FRUSTUM3, FRUSTUM4, BORESIGHT_DIRECTION
                // and UP_DIRECTION since these are all prefixed with MSI_ in the info file.
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
                        spacecraftPosition[0] = x;
                        spacecraftPosition[1] = y;
                        spacecraftPosition[2] = z;
                    }
                    if (SUN_POSITION_LT.equals(token))
                    {
                        sunVector[0] = x;
                        sunVector[1] = y;
                        sunVector[2] = z;
                        MathUtil.vhat(sunVector, sunVector);
                    }
                    else if (token.endsWith(FRUSTUM1))
                    {
                        frustum1[0] = x;
                        frustum1[1] = y;
                        frustum1[2] = z;
                        MathUtil.vhat(frustum1, frustum1);
                    }
                    else if (token.endsWith(FRUSTUM2))
                    {
                        frustum2[0] = x;
                        frustum2[1] = y;
                        frustum2[2] = z;
                        MathUtil.vhat(frustum2, frustum2);
                    }
                    else if (token.endsWith(FRUSTUM3))
                    {
                        frustum3[0] = x;
                        frustum3[1] = y;
                        frustum3[2] = z;
                        MathUtil.vhat(frustum3, frustum3);
                    }
                    else if (token.endsWith(FRUSTUM4))
                    {
                        frustum4[0] = x;
                        frustum4[1] = y;
                        frustum4[2] = z;
                        MathUtil.vhat(frustum4, frustum4);
                    }
                    if (token.endsWith(BORESIGHT_DIRECTION))
                    {
                        boresightDirection[0] = x;
                        boresightDirection[1] = y;
                        boresightDirection[2] = z;
                    }
                    if (token.endsWith(UP_DIRECTION))
                    {
                        upVector[0] = x;
                        upVector[1] = y;
                        upVector[2] = z;
                    }
                }
            }
        }

        in.close();
    }


    /**
     * Return the mask sizes as a 4 element integer array where the:
     * first  element is the top    mask size,
     * second element is the right  mask size,
     * third  element is the bottom mask size,
     * fourth element is the left   mask size.
     * @return
     */
    abstract protected int[] getMaskSizes();

    abstract protected String initializeFitFileFullPath();
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

    public String getFitFileFullPath()
    {
        return fitFileFullPath;
    }

    public String getLabelFileFullPath()
    {
        return labelFileFullPath;
    }

    public String getInfoFileFullPath()
    {
        return infoFileFullPath;
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
        image.SetScalarTypeToFloat();
        if (transpose)
            image.SetDimensions(width, height, 1);
        else
            image.SetDimensions(height, width, 1);
        image.SetSpacing(1.0, 1.0, 1.0);
        image.SetOrigin(0.0, 0.0, 0.0);
        image.SetNumberOfScalarComponents(1);

        float maxValue = -Float.MAX_VALUE;
        float minValue = Float.MAX_VALUE;
        for (int i=0; i<height; ++i)
            for (int j=0; j<width; ++j)
            {
                if (transpose)
                    image.SetScalarComponentFromDouble(j, height-1-i, 0, 0, array2D[i][j]);
                else
                    image.SetScalarComponentFromDouble(i, width-1-j, 0, 0, array2D[i][j]);

                if (array2D[i][j] > maxValue)
                    maxValue = array2D[i][j];
                if (array2D[i][j] < minValue)
                    minValue = array2D[i][j];
            }

        setMaxValue(maxValue);
        setMinValue(minValue);

        return image;
    }

    protected vtkImageData createRawImage(int height, int width, int depth, float[][][] array)
    {
        vtkImageData image = new vtkImageData();
        image.SetScalarTypeToFloat();
        image.SetDimensions(width, height, depth);
        image.SetSpacing(1.0, 1.0, 1.0);
        image.SetOrigin(0.0, 0.0, 0.0);
        image.SetNumberOfScalarComponents(1);

        float maxValue = -Float.MAX_VALUE;
        float minValue = Float.MAX_VALUE;
        for (int i=0; i<height; ++i)
            for (int j=0; j<width; ++j)
            {
                for (int k=0; k<depth; ++k)
                {
                    image.SetScalarComponentFromDouble(j, height-1-i, k, 0, array[i][j][k]);

                    if (array[i][j][k] > maxValue)
                        maxValue = array[i][j][k];
                    if (array[i][j][k] < minValue)
                        minValue = array[i][j][k];
                }
            }

        setMaxValue(maxValue);
        setMinValue(minValue);

        return image;
    }

    protected void loadImage() throws FitsException, IOException
    {
        String filename = getFitFileFullPath();

        Fits f = new Fits(filename);
        BasicHDU h = f.getHDU(0);

        float[][] array2D = null;
        float[][][] array3D = null;
        axes = h.getAxes();
        naxes = axes.length;
        // height is axis 0
        int height = axes[0];
        int width = axes[1];
        int depth = naxes == 3 ? axes[2] : 1;
//        // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis is 2
//        int width = naxes == 2 ? axes[1] : axes[2];
//        // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
//        int depth = naxes == 2 ? 0 : axes[1];

        Object data = h.getData().getData();

        // for 3D arrays we consider the second axis the "spectral" axis
        if (data instanceof float[][][])
        {
            System.out.println("Image cube");
            array3D = (float[][][])data;
            System.out.println("3D pixel array detected: " + array3D.length + "x" + array3D[0].length + "x" + array3D[0][0].length);
            array2D = new float[height][depth];

            for (int i=0; i<height; ++i)
                for (int j=0; j<depth; ++j)
                    array2D[i][j] = array3D[i][currentSlice][j];
//                    for (int k=0; k<originalDepth; k++)
//                    {
//                        array2D[i][j] = array3D[i][k][j];
//                    }
        }
        else if (data instanceof float[][])
        {
            array2D = (float[][])data;
        }
        else if (data instanceof short[][])
        {
            short[][] arrayS = (short[][])data;
            array2D = new float[height][width];

            for (int i=0; i<height; ++i)
                for (int j=0; j<width; ++j)
                {
                    array2D[i][j] = arrayS[i][j];
                }
        }
        else if (data instanceof byte[][])
        {
            byte[][] arrayB = (byte[][])data;
            array2D = new float[height][width];

            for (int i=0; i<height; ++i)
                for (int j=0; j<width; ++j)
                {
                    array2D[i][j] = arrayB[i][j] & 0xFF;
                }
        }
        else
        {
            System.out.println("Data type not supported!");
            return;
        }

        f.getStream().close();

        if (data instanceof float[][][])
            // image cube
            rawImage = createRawImage(height, depth, 1, false, array2D, null);
        else
            rawImage = createRawImage(height, width, 1, array2D, null);

        processRawImage(rawImage);

        int[] dims = rawImage.GetDimensions();
        imageWidth = dims[0];
        imageHeight = dims[1];

        int[] masking = getMaskSizes();
        int topMask =    masking[0];
        int rightMask =  masking[1];
        int bottomMask = masking[2];
        int leftMask =   masking[3];

        maskSource = new vtkImageCanvasSource2D();
        maskSource.SetScalarTypeToUnsignedChar();
        maskSource.SetNumberOfScalarComponents(1);
        maskSource.SetExtent(0, imageWidth-1, 0, imageHeight-1, 0, 0);
        // Initialize the mask to black which masks out the image
        maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
        maskSource.FillBox(0, imageWidth-1, 0, imageHeight-1);
        // Create a square inside mask which passes through the image.
        maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
        maskSource.FillBox(leftMask, imageWidth-1-rightMask, bottomMask, imageHeight-1-topMask);
        maskSource.Update();

        setDisplayedImageRange(new IntensityRange(0, 255));


        footprint = new vtkPolyData();
        shiftedFootprint = new vtkPolyData();
        textureCoords = new vtkFloatArray();
        normalsFilter = new vtkPolyDataNormals();
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
//        displayedImage = .....;
//        fireProp...
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
        if (footprintActor == null)
        {
            loadFootprint();

            imageTexture = new vtkTexture();
            imageTexture.InterpolateOn();
            imageTexture.RepeatOff();
            imageTexture.EdgeClampOn();
            imageTexture.SetInput(displayedImage);

            vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
            footprintMapper.SetInput(shiftedFootprint);
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
            vtkPolyData frus = new vtkPolyData();

            vtkPoints points = new vtkPoints();
            vtkCellArray lines = new vtkCellArray();

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(2);

            double dx = MathUtil.vnorm(spacecraftPosition) + smallBodyModel.getBoundingBoxDiagonalLength();
            double[] origin = spacecraftPosition;
            double[] UL = {origin[0]+frustum1[0]*dx, origin[1]+frustum1[1]*dx, origin[2]+frustum1[2]*dx};
            double[] UR = {origin[0]+frustum2[0]*dx, origin[1]+frustum2[1]*dx, origin[2]+frustum2[2]*dx};
            double[] LL = {origin[0]+frustum3[0]*dx, origin[1]+frustum3[1]*dx, origin[2]+frustum3[2]*dx};
            double[] LR = {origin[0]+frustum4[0]*dx, origin[1]+frustum4[1]*dx, origin[2]+frustum4[2]*dx};

            points.InsertNextPoint(spacecraftPosition);
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
            frusMapper.SetInput(frus);

            frustumActor = new vtkActor();
            frustumActor.SetMapper(frusMapper);
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
        return displayedRange;
    }

    public void setDisplayedImageRange(IntensityRange range)
    {
        if (displayedRange.min != range.min ||
                displayedRange.max != range.max)
        {
            displayedRange = range;

            float dx = (maxValue-minValue)/255.0f;
            float min = minValue + range.min*dx;
            float max = minValue + range.max*dx;

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
//            if (naxes == 3)
//            {
//                System.out.println("Slicing image...");
//                vtkImageReslice slicer = new vtkImageReslice();
//                slicer.SetInput(rawImage);
//                slicer.SetInformationInput(rawImage);
//                slicer.SetOutputDimensionality(2);
//                slicer.SetInterpolationModeToNearestNeighbor();
//                slicer.SetOutputSpacing(1.0, 1.0, 1.0);
//                slicer.SetResliceAxesDirectionCosines(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
////                slicer.SetOutputOrigin(0.0, 0.0, (double)currentSlice);
//                slicer. SetResliceAxesOrigin(0.0, 0.0, (double)currentSlice);
////                slicer. SetResliceAxesOrigin(0.0, 0.0, 0.0);
//                slicer.SetOutputExtent(0, axes[1]-1, 0, axes[0]-1, 0, 0);
//                slicer.Update();
//                image2D = slicer.GetOutput();
//            }

            vtkImageMapToColors mapToColors = new vtkImageMapToColors();
            mapToColors.SetInput(image2D);
            mapToColors.SetOutputFormatToRGBA();
            mapToColors.SetLookupTable(lut);
            mapToColors.Update();

            vtkImageData mapToColorsOutput = mapToColors.GetOutput();
            vtkImageData maskSourceOutput = maskSource.GetOutput();

            vtkImageMask maskFilter = new vtkImageMask();
            maskFilter.SetImageInput(mapToColorsOutput);
            maskFilter.SetMaskInput(maskSourceOutput);
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


    private void loadImageInfo() throws NumberFormatException, IOException
    {
        String infoFilename = getInfoFileFullPath();

        String[] start = new String[1];
        String[] stop = new String[1];
        loadImageInfo(
                infoFilename,
                start,
                stop,
                spacecraftPosition,
                sunVector,
                frustum1,
                frustum2,
                frustum3,
                frustum4,
                boresightDirection,
                upVector);

        startTime = start[0];
        stopTime = stop[0];

//        printpt(frustum1, "pds frustum1 ");
//        printpt(frustum2, "pds frustum2 ");
//        printpt(frustum3, "pds frustum3 ");
//        printpt(frustum4, "pds frustum4 ");
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
            double[] spacecraftPosition,
            double[] sunVector,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws IOException
    {
        FileInputStream fs = new FileInputStream(sumfilename);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

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
        spacecraftPosition[0] = -Double.parseDouble(tmp[0]);
        spacecraftPosition[1] = -Double.parseDouble(tmp[1]);
        spacecraftPosition[2] = -Double.parseDouble(tmp[2]);

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
        frustum3[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum3[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum3[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = -cornerVector[0];
        fy = cornerVector[1];
        fz = cornerVector[2];
        frustum4[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum4[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum4[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = cornerVector[0];
        fy = -cornerVector[1];
        fz = cornerVector[2];
        frustum1[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum1[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum1[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = -cornerVector[0];
        fy = -cornerVector[1];
        fz = cornerVector[2];
        frustum2[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum2[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum2[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        MathUtil.vhat(frustum1, frustum1);
        MathUtil.vhat(frustum2, frustum2);
        MathUtil.vhat(frustum3, frustum3);
        MathUtil.vhat(frustum4, frustum4);

        MathUtil.vhat(cz, boresightDirection);
        MathUtil.vhat(cx, upVector);
        MathUtil.vhat(sz, sunVector);

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
            double[] spacecraftPosition,
            double[] sunVector,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws IOException
    {
        System.out.println(labelFileName);

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
                        spacecraftPosition,
                        sunVector,
                        frustum1,
                        frustum2,
                        frustum3,
                        frustum4,
                        boresightDirection,
                        upVector);

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
                    spacecraftPosition,
                    sunVector,
                    frustum1,
                    frustum2,
                    frustum3,
                    frustum4,
                    boresightDirection,
                    upVector);

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
        boresightDirection[0] = cz[0] = boresightVector3D.getX();
        boresightDirection[1] = cz[1] = boresightVector3D.getY();
        boresightDirection[2] = cz[2] = boresightVector3D.getZ();

        Vector3D upVector3D = scOrientation.applyTo(j);
        upVector[0] = cy[0] = upVector3D.getX();
        upVector[1] = cy[1] = upVector3D.getY();
        upVector[2] = cy[2] = upVector3D.getZ();

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
      frustum3[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum3[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum3[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = -cornerVector[0];
      fy = cornerVector[1];
      fz = cornerVector[2];
      frustum4[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum4[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum4[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = cornerVector[0];
      fy = -cornerVector[1];
      fz = cornerVector[2];
      frustum1[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum1[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum1[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      fx = -cornerVector[0];
      fy = -cornerVector[1];
      fz = cornerVector[2];
      frustum2[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
      frustum2[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
      frustum2[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

      MathUtil.vhat(frustum1, frustum1);
      MathUtil.vhat(frustum2, frustum2);
      MathUtil.vhat(frustum3, frustum3);
      MathUtil.vhat(frustum4, frustum4);


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
                spacecraftPosition,
                sunVector,
                frustum1,
                frustum2,
                frustum3,
                frustum4,
                boresightDirection,
                upVector);

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
                spacecraftPosition,
                sunVector,
                frustum1,
                frustum2,
                frustum3,
                frustum4,
                boresightDirection,
                upVector);

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
            vtkPolyData tmp = smallBodyModel.computeFrustumIntersection(spacecraftPosition,
                    frustum1, frustum3, frustum4, frustum2);

            if (tmp == null)
                return;

            // Need to clear out scalar data since if coloring data is being shown,
            // then the color might mix-in with the image.
            tmp.GetCellData().SetScalars(null);
            tmp.GetPointData().SetScalars(null);

            footprint.DeepCopy(tmp);

            vtkPointData pointData = footprint.GetPointData();
            pointData.SetTCoords(textureCoords);
            PolyDataUtil.generateTextureCoordinates(getFrustum(), getImageWidth(), getImageHeight(), footprint);
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
            footprint.DeepCopy(footprintReaderOutput);
        }


        shiftedFootprint.DeepCopy(footprint);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, offset);

        footprintGenerated = true;
    }

    public vtkPolyData generateBoundary()
    {
        loadFootprint();

        if (footprint.GetNumberOfPoints() == 0)
            return null;

        vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
        edgeExtracter.SetInput(footprint);
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
        return MathUtil.vnorm(spacecraftPosition);
     }

    private void computeCellNormals()
    {
        if (normalsGenerated == false)
        {
            normalsFilter.SetInput(footprint);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(0);
            normalsFilter.SplittingOff();
            normalsFilter.Update();

            vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
            footprint.DeepCopy(normalsFilterOutput);
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
            spacecraftPosition[0] - pt[0],
            spacecraftPosition[1] - pt[1],
            spacecraftPosition[2] - pt[2]};

        double incidence = MathUtil.vsep(normal, sunVector) * 180.0 / Math.PI;
        double emission = MathUtil.vsep(normal, scvec) * 180.0 / Math.PI;
        double phase = MathUtil.vsep(sunVector, scvec) * 180.0 / Math.PI;

        double[] angles = {incidence, emission, phase};

        return angles;
    }

    protected void computeIlluminationAngles()
    {
        if (footprintGenerated == false)
            loadFootprint();

        computeCellNormals();

        int numberOfCells = footprint.GetNumberOfCells();

        vtkPoints points = footprint.GetPoints();
        vtkCellData footprintCellData = footprint.GetCellData();
        vtkDataArray normals = footprintCellData.GetNormals();

        this.minEmission  =  Double.MAX_VALUE;
        this.maxEmission  = -Double.MAX_VALUE;
        this.minIncidence =  Double.MAX_VALUE;
        this.maxIncidence = -Double.MAX_VALUE;
        this.minPhase     =  Double.MAX_VALUE;
        this.maxPhase     = -Double.MAX_VALUE;

        for (int i=0; i<numberOfCells; ++i)
        {
            vtkCell cell = footprint.GetCell(i);
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
        if (footprintGenerated == false)
            loadFootprint();

        int numberOfPoints = footprint.GetNumberOfPoints();

        vtkPoints points = footprint.GetPoints();

        minHorizontalPixelScale = Double.MAX_VALUE;
        maxHorizontalPixelScale = -Double.MAX_VALUE;
        meanHorizontalPixelScale = 0.0;
        minVerticalPixelScale = Double.MAX_VALUE;
        maxVerticalPixelScale = -Double.MAX_VALUE;
        meanVerticalPixelScale = 0.0;

        double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / imageHeight;
        double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / imageWidth;

        double[] vec = new double[3];

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            vec[0] = pt[0] - spacecraftPosition[0];
            vec[1] = pt[1] - spacecraftPosition[1];
            vec[2] = pt[2] - spacecraftPosition[2];
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
                spacecraftPosition[0] + frustum1[0],
                spacecraftPosition[1] + frustum1[1],
                spacecraftPosition[2] + frustum1[2]
        };
        double[] corner2 = {
                spacecraftPosition[0] + frustum2[0],
                spacecraftPosition[1] + frustum2[1],
                spacecraftPosition[2] + frustum2[2]
        };
        double[] corner3 = {
                spacecraftPosition[0] + frustum3[0],
                spacecraftPosition[1] + frustum3[1],
                spacecraftPosition[2] + frustum3[2]
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

        double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / imageHeight;
        double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / imageWidth;

        double scdist = MathUtil.vnorm(spacecraftPosition);

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
                vec[0] -= spacecraftPosition[0];
                vec[1] -= spacecraftPosition[1];
                vec[2] -= spacecraftPosition[2];
                MathUtil.unorm(vec, vec);

                double[] lookPt = {
                        spacecraftPosition[0] + 2.0*scdist*vec[0],
                        spacecraftPosition[1] + 2.0*scdist*vec[1],
                        spacecraftPosition[2] + 2.0*scdist*vec[2]
                };

                //cellLocator.IntersectWithLine(spacecraftPosition, lookPt, intersectPoints, intersectCells);
                double tol = 1e-6;
                double[] t = new double[1];
                double[] x = new double[3];
                double[] pcoords = new double[3];
                int[] subId = new int[1];
                int[] cellId = new int[1];
                int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);

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
                    double closestDist = MathUtil.distanceBetween(closestPoint, spacecraftPosition);

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
        return shiftedFootprint;
    }

    /**
     * The original footprint whose cells exactly overlap the original asteroid.
     * If rendered as is, it would interfere with the asteroid.
     * @return
     */
    public vtkPolyData getUnshiftedFootprint()
    {
        return footprint;
    }

    public void Delete()
    {
        displayedImage.Delete();
        rawImage.Delete();
        footprint.Delete();
        shiftedFootprint.Delete();
        textureCoords.Delete();
        normalsFilter.Delete();
        maskSource.Delete();
    }

    public void getCameraOrientation(double[] spacecraftPosition,
            double[] focalPoint, double[] upVector)
    {

        for (int i=0; i<3; ++i)
        {
            spacecraftPosition[i] = this.spacecraftPosition[i];
            upVector[i] = this.upVector[i];
        }

        // Normalize the direction vector
        double[] direction = new double[3];
        MathUtil.unorm(boresightDirection, direction);

        int cellId = smallBodyModel.computeRayIntersection(spacecraftPosition, direction, focalPoint);

        if (cellId < 0)
        {
            BoundingBox bb = new BoundingBox(footprint.GetBounds());
            double[] centerPoint = bb.getCenterPoint();
            //double[] centerPoint = footprint.GetPoint(0);
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
        double[] cx = upVector;
        double[] cz = new double[3];
        MathUtil.unorm(boresightDirection, cz);

        double[] cy = new double[3];
        MathUtil.vcrss(cz, cx, cy);

        double[][] m = {
                {cx[0], cx[1], cx[2]},
                {cy[0], cy[1], cy[2]},
                {cz[0], cz[1], cz[2]}
        };

        Rotation rotation = new Rotation(m, 1.0e-6);

        for (int i=0; i<3; ++i)
            spacecraftPosition[i] = this.spacecraftPosition[i];

        quaternion[0] = rotation.getQ0();
        quaternion[1] = rotation.getQ1();
        quaternion[2] = rotation.getQ2();
        quaternion[3] = rotation.getQ3();

        return rotation;
    }

    public Frustum getFrustum()
    {
        return new Frustum(spacecraftPosition, frustum1, frustum3, frustum4, frustum2);
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
        double fovHoriz = MathUtil.vsep(frustum1, frustum3) * 180.0 / Math.PI;
        return fovHoriz;
    }

    public double getVerticalFovAngle()
    {
        double fovVert = MathUtil.vsep(frustum1, frustum2) * 180.0 / Math.PI;
        return fovVert;
    }

    public double[] getSpacecraftPosition()
    {
        return spacecraftPosition;
    }

    public double[] getSunVector()
    {
        return sunVector;
    }

    public double[] getBoresightDirection()
    {
        return boresightDirection;
    }

    public double[] getUpVector()
    {
        return upVector;
    }

    /**
     * Get the direction from the spacecraft of pixel with specified sample and line.
     * Note that sample is along image width and line is along image height.
     */
    public double[] getPixelDirection(int sample, int line)
    {
        double[] corner1 = {
                spacecraftPosition[0] + frustum1[0],
                spacecraftPosition[1] + frustum1[1],
                spacecraftPosition[2] + frustum1[2]
        };
        double[] corner2 = {
                spacecraftPosition[0] + frustum2[0],
                spacecraftPosition[1] + frustum2[1],
                spacecraftPosition[2] + frustum2[2]
        };
        double[] corner3 = {
                spacecraftPosition[0] + frustum3[0],
                spacecraftPosition[1] + frustum3[1],
                spacecraftPosition[2] + frustum3[2]
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
        dir[0] -= spacecraftPosition[0];
        dir[1] -= spacecraftPosition[1];
        dir[2] -= spacecraftPosition[2];
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

        int result = smallBodyModel.computeRayIntersection(spacecraftPosition, dir, intersectPoint);

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

        shiftedFootprint.DeepCopy(footprint);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, offset);

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
        return PolyDataUtil.getSurfaceArea(footprint);
    }

    public double getImageOpacity()
    {
        return imageOpacity;
    }

    public void setImageOpacity(double imageOpacity)
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

        properties.put("Name", new File(getFitFileFullPath()).getName()); //TODO remove extension and possibly prefix
        properties.put("Start Time", getStartTime());
        properties.put("Stop Time", getStopTime());
        properties.put("Spacecraft Distance", df.format(getSpacecraftDistance()) + " km");
        properties.put("Spacecraft Position",
                df.format(spacecraftPosition[0]) + ", " + df.format(spacecraftPosition[1]) + ", " + df.format(spacecraftPosition[2]) + " km");
        double[] quaternion = new double[4];
        double[] notused = new double[4];
        getCameraOrientation(notused, quaternion);
        properties.put("Spacecraft Orientation (quaternion)",
                "(" + df.format(quaternion[0]) + ", [" + df.format(quaternion[1]) + ", " + df.format(quaternion[2]) + ", " + df.format(quaternion[3]) + "])");
        properties.put("Sun Vector",
                df.format(sunVector[0]) + ", " + df.format(sunVector[1]) + ", " + df.format(sunVector[2]));
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
}
