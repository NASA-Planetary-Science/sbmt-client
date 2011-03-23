package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import vtk.vtkActor;
import vtk.vtkCell;
import vtk.vtkCellArray;
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

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.Frustum;
import edu.jhuapl.near.util.IntensityRange;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

/**
 * This class represents an image of the NEAR MSI instrument. It allows
 * retrieving the image itself as well as the associated IMG file.
 *
 * @author kahneg1
 *
 */
public class MSIImage extends Model implements PropertyChangeListener
{

    public static final int IMAGE_WIDTH = 537;
    public static final int IMAGE_HEIGHT = 412;

    // Number of pixels on each side of the image that are
    // masked out (invalid) due to filtering.
    public static final int LEFT_MASK = 14;
    public static final int RIGHT_MASK = 14;
    public static final int TOP_MASK = 2;
    public static final int BOTTOM_MASK = 2;

    public static final float PDS_NA = -1.e32f;
    //public static final int NUM_LAYERS = 14;
    //public static final int TEXTURE_SIZE = 128;
    public static final String START_TIME = "START_TIME";
    public static final String STOP_TIME = "STOP_TIME";
    //public static final String TARGET_CENTER_DISTANCE = "TARGET_CENTER_DISTANCE";
    //public static final String HORIZONTAL_PIXEL_SCALE = "HORIZONTAL_PIXEL_SCALE";
    public static final String SPACECRAFT_POSITION = "SPACECRAFT_POSITION";
    public static final String MSI_FRUSTUM1 = "MSI_FRUSTUM1";
    public static final String MSI_FRUSTUM2 = "MSI_FRUSTUM2";
    public static final String MSI_FRUSTUM3 = "MSI_FRUSTUM3";
    public static final String MSI_FRUSTUM4 = "MSI_FRUSTUM4";
    public static final String MSI_BORESIGHT_DIRECTION = "MSI_BORESIGHT_DIRECTION";
    public static final String MSI_UP_DIRECTION = "MSI_UP_DIRECTION";
    public static final String SUN_POSITION_LT = "SUN_POSITION_LT";

    private SmallBodyModel erosModel;

    private vtkImageData rawImage;
    private vtkImageData displayedImage;

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
    private double minVerticalPixelScale = Double.MAX_VALUE;
    private double maxVerticalPixelScale = -Double.MAX_VALUE;

    private String name = ""; // The name is a 9 digit number at the beginning of the filename
                               // (starting after the initial M0). This is how the lineament
                              // model names them.

    private float minValue;
    private float maxValue;
    private IntensityRange displayedRange = new IntensityRange(1,0);

    private String fullpath; // The actual path of the image stored on the local disk (after downloading from the server)

    private int filter; // 1 through 7

    private double[] spacecraftPosition = new double[3];
    private double[] frustum1 = new double[3];
    private double[] frustum2 = new double[3];
    private double[] frustum3 = new double[3];
    private double[] frustum4 = new double[3];
    private double[] sunPosition = new double[3];

    private double[] boresightDirection = new double[3];
    private double[] upVector = new double[3];

    private boolean showFrustum = false;

    private String startTime = "";
    private String stopTime = "";

    private MSIKey key;
    private vtkImageCanvasSource2D maskSource;

    // If true, then the footprint is generated by intersecting a frustum with the asteroid.
    // This setting is used when generating the files on the server.
    // If false, then the footprint is downloaded from the server. This setting is used by the GUI.
    private static boolean generateFootprint = true;

    // If true the footprint will be loaded from the local disk rather than being
    // downloaded from from the server
    private static boolean footprintIsOnLocalDisk = false;

    public enum MSISource
    {
        PDS {
            public String toString()
            {
                return "PDS derived";
            }
        },
        GASKELL {
            public String toString()
            {
                return "Gaskell derived";
            }
        }
    };

    /**
     * An MSIKey should be used to uniquely distinguish one MSI image from another.
     * No two MSI images will have the same values for the fields of this class.
     * @author kahn
     *
     */
    public static class MSIKey
    {
        // The path of the image as passed into the constructor. This is not the
        // same as fullpath but instead corresponds to the name needed to download
        // the file from the server (excluding the hostname and extension).
        public String name;

        public MSISource source;

        public MSIKey()
        {
        }

        public MSIKey(String name, MSISource source)
        {
            this.name = name;
            this.source = source;
        }

        @Override
        public boolean equals(Object obj)
        {
            return name.equals(((MSIKey)obj).name) && source == ((MSIKey)obj).source;
        }
    }

    /**
     * Because instances of MSIImage can be expensive, we want there to be
     * no more than one instance of this class per image file on the server.
     * Hence this class was created to manage the creation and deletion of
     * MSIImage's. Anyone needing a MSIImage should use this factory class to
     * create MSIImages's and should NOT call the constructor directly.
     */
//    public static class MSIImageFactory
//    {
//        static private WeakHashMap<MSIImage, Object> images =
//            new WeakHashMap<MSIImage, Object>();
//
//        static /*public*/ MSIImage createImage(MSIKey key, SmallBodyModel eros) throws FitsException, IOException
//        {
//            for (MSIImage image : images.keySet())
//            {
//                if (image.key.equals(key))
//                    return image;
//            }
//
//            MSIImage image = new MSIImage(key, eros);
//            images.put(image, null);
//            return image;
//        }
//    }

    /**
     * This constructor should only be used in the GUI program since
     * this constructor makes sure the relevant files get downloaded
     * from the server.
     * @param filename name of fit file
     * @throws FitsException
     * @throws IOException
     */
    public MSIImage(MSIKey key, SmallBodyModel eros) throws FitsException, IOException
    {
        this.key = key;

        // Download the image, and all the companion files if necessary.
        File fitFile = FileCache.getFileFromServer(key.name + ".FIT");

        if (fitFile == null)
            throw new IOException("Could not download " + key.name);

        String imgLblFilename = key.name + "_DDR.LBL";
        FileCache.getFileFromServer(imgLblFilename);

        if (key.source.equals(MSISource.GASKELL))
        {
            // Try to load a sumfile if there is one
            File tmp = new File(key.name);
            String sumFilename = "/MSI/sumfiles/" + tmp.getName().substring(0, 11) + ".SUM";
            FileCache.getFileFromServer(sumFilename);
        }

        //String footprintFilename = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";
        //FileCache.getFileFromServer(footprintFilename);

        this.erosModel = eros;

        this.initialize(fitFile);
    }

    /**
     * This constructor should only be used in the database/server generation/processing
     * where there is no need to download files from the server (since we're running on
     * server)
     * @param fitFile
     * @throws FitsException
     * @throws IOException
     */
    public MSIImage(File fitFile, SmallBodyModel eros, MSISource source) throws FitsException, IOException
    {
        this.erosModel = eros;
        this.key = new MSIKey(null, source);
        this.initialize(fitFile);
    }

    private void initialize(File fitFile) throws FitsException, IOException
    {
        String filename = fitFile.getAbsolutePath();
        this.fullpath = filename;

        name = fitFile.getName().substring(2, 11);

        // Set the filter name
        filter = Integer.parseInt(fitFile.getName().substring(12,13));

        Fits f = new Fits(filename);
        BasicHDU h = f.getHDU(0);

        float[][] array = null;

        int[] axes = h.getAxes();
        int originalWidth = axes[1];
        int originalHeight = axes[0];

        //HashMap<Short, Integer> values = new HashMap<Short, Integer>();

        // For now only support images with type float or short since that what MSI images
        // seem to be in.
        try
        {
            array = (float[][])h.getData().getData();
        }
        catch (ClassCastException e)
        {
            short[][] arrayS = (short[][])h.getData().getData();
            array = new float[originalHeight][originalWidth];

            for (int i=0; i<originalHeight; ++i)
                for (int j=0; j<originalWidth; ++j)
                {
                    //values.put(arrayS[i][j], 0);
                    array[i][j] = arrayS[i][j];
                }
        }

        rawImage = new vtkImageData();
        rawImage.SetScalarTypeToFloat();
        rawImage.SetDimensions(originalWidth, originalHeight, 1);
        rawImage.SetSpacing(1.0, 1.0, 1.0);
        rawImage.SetOrigin(0.0, 0.0, 0.0);
        rawImage.SetNumberOfScalarComponents(1);

        maxValue = -Float.MAX_VALUE;
        minValue = Float.MAX_VALUE;
        for (int i=0; i<originalHeight; ++i)
            for (int j=0; j<originalWidth; ++j)
            {
                rawImage.SetScalarComponentFromDouble(j, originalHeight-1-i, 0, 0, array[i][j]);

                if (array[i][j] > maxValue)
                    maxValue = array[i][j];
                if (array[i][j] < minValue)
                    minValue = array[i][j];
            }

        // Now scale this image
        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(rawImage);
        reslice.SetInterpolationModeToLinear();
        reslice.SetOutputSpacing(1.0, (double)originalHeight/(double)IMAGE_HEIGHT, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, IMAGE_WIDTH-1, 0, IMAGE_HEIGHT-1, 0, 0);
        reslice.Update();

        vtkImageData resliceOutput = reslice.GetOutput();
        rawImage.DeepCopy(resliceOutput);
        rawImage.SetSpacing(1, 1, 1);

        maskSource = new vtkImageCanvasSource2D();
        maskSource.SetScalarTypeToUnsignedChar();
        maskSource.SetNumberOfScalarComponents(1);
        maskSource.SetExtent(0, IMAGE_WIDTH-1, 0, IMAGE_HEIGHT-1, 0, 0);
        // Initialize the mask to black which masks out the image
        maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
        maskSource.FillBox(0, IMAGE_WIDTH-1, 0, IMAGE_HEIGHT-1);
        // Create a square inside mask which passes through the image.
        maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
        maskSource.FillBox(LEFT_MASK, IMAGE_WIDTH-1-RIGHT_MASK, BOTTOM_MASK, IMAGE_HEIGHT-1-TOP_MASK);
        maskSource.Update();


        setDisplayedImageRange(new IntensityRange(0, 255));

        loadImageInfo();

        // If the sumfile exists, then load it after we load the LBL file
        // so as to overwrite whatever was loaded from the LBL file.
        if (key.source.equals(MSISource.GASKELL))
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

        footprint = new vtkPolyData();
        shiftedFootprint = new vtkPolyData();
        textureCoords = new vtkFloatArray();
        normalsFilter = new vtkPolyDataNormals();
    }

    public MSIKey getKey()
    {
        return key;
    }

    public vtkImageData getRawImage()
    {
        return rawImage;
    }

    public vtkImageData getDisplayedImage()
    {
        return displayedImage;
    }

    public int getFilter()
    {
        return filter;
    }

    public static void setGenerateFootprint(boolean b)
    {
        generateFootprint = b;
    }

    public static void setFootprintIsOnLocalDisk(boolean b)
    {
        footprintIsOnLocalDisk = b;
    }

    public ArrayList<vtkProp> getProps()
    {
        if (footprintActor == null)
        {
            loadFootprint();

            vtkTexture texture = new vtkTexture();
            texture.InterpolateOn();
            texture.RepeatOff();
            texture.EdgeClampOn();
            texture.SetInput(displayedImage);

            vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
            footprintMapper.SetInput(shiftedFootprint);
            footprintMapper.Update();

            footprintActor = new vtkActor();
            footprintActor.SetMapper(footprintMapper);
            footprintActor.SetTexture(texture);
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

            double dx = MathUtil.vnorm(spacecraftPosition) + erosModel.getBoundingBoxDiagonalLength();
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
    }

    public boolean isFrustumShowing()
    {
        return showFrustum;
    }

    public String getName()
    {
        return name;
    }

    public String getFullPath()
    {
        return fullpath;
    }

    public String getServerPath()
    {
        return key.name;
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

            vtkImageMapToColors mapToColors = new vtkImageMapToColors();
            mapToColors.SetInput(rawImage);
            mapToColors.SetOutputFormatToRGBA();
            mapToColors.SetLookupTable(lut);
            mapToColors.Update();

            vtkImageMask maskFilter = new vtkImageMask();
            maskFilter.SetImageInput(mapToColors.GetOutput());
            maskFilter.SetMaskInput(maskSource.GetOutput());
            maskFilter.Update();

            if (displayedImage == null)
                displayedImage = new vtkImageData();
            vtkImageData mapToColorsOutput = maskFilter.GetOutput();
            displayedImage.DeepCopy(mapToColorsOutput);

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

    /**
     *     Make this static so it can be called without needing access
     *     to and MSIImage object.
     */
    static public void loadImageInfo(
            String lblFilename,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunPosition,
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
                if (SPACECRAFT_POSITION.equals(token) ||
                        MSI_FRUSTUM1.equals(token) ||
                        MSI_FRUSTUM2.equals(token) ||
                        MSI_FRUSTUM3.equals(token) ||
                        MSI_FRUSTUM4.equals(token) ||
                        SUN_POSITION_LT.equals(token) ||
                        MSI_BORESIGHT_DIRECTION.equals(token) ||
                        MSI_UP_DIRECTION.equals(token))
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
                    else if (MSI_FRUSTUM1.equals(token))
                    {
                        frustum1[0] = x;
                        frustum1[1] = y;
                        frustum1[2] = z;
                        MathUtil.vhat(frustum1, frustum1);
                    }
                    else if (MSI_FRUSTUM2.equals(token))
                    {
                        frustum2[0] = x;
                        frustum2[1] = y;
                        frustum2[2] = z;
                        MathUtil.vhat(frustum2, frustum2);
                    }
                    else if (MSI_FRUSTUM3.equals(token))
                    {
                        frustum3[0] = x;
                        frustum3[1] = y;
                        frustum3[2] = z;
                        MathUtil.vhat(frustum3, frustum3);
                    }
                    else if (MSI_FRUSTUM4.equals(token))
                    {
                        frustum4[0] = x;
                        frustum4[1] = y;
                        frustum4[2] = z;
                        MathUtil.vhat(frustum4, frustum4);
                    }
                    if (SUN_POSITION_LT.equals(token))
                    {
                        sunPosition[0] = x;
                        sunPosition[1] = y;
                        sunPosition[2] = z;
                    }
                    if (MSI_BORESIGHT_DIRECTION.equals(token))
                    {
                        boresightDirection[0] = x;
                        boresightDirection[1] = y;
                        boresightDirection[2] = z;
                    }
                    if (MSI_UP_DIRECTION.equals(token))
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

    private void loadImageInfo() throws NumberFormatException, IOException
    {
        String lblFilename = fullpath.substring(0, fullpath.length()-4) + "_DDR.LBL";

        String[] start = new String[1];
        String[] stop = new String[1];
        loadImageInfo(
                lblFilename,
                start,
                stop,
                spacecraftPosition,
                sunPosition,
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
    private static void replaceDwithE(String[] s)
    {
        for (int i=0; i<s.length; ++i)
            s[i] = s[i].replace('D', 'E');
    }

    public static void loadSumfile(
            String sumfilename,
            double[] spacecraftPosition,
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
        in.readLine();
        in.readLine();
        in.readLine();

        String[] tmp = in.readLine().trim().split("\\s+");
        replaceDwithE(tmp);
        spacecraftPosition[0] = -Double.parseDouble(tmp[0]);
        spacecraftPosition[1] = -Double.parseDouble(tmp[1]);
        spacecraftPosition[2] = -Double.parseDouble(tmp[2]);

        double[] cx = new double[3];
        double[] cy = new double[3];
        double[] cz = new double[3];

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

        final double zo = -0.025753661240;
        final double yo = -0.019744857140;
        double fx = zo;
        double fy = yo;
        double fz = 1.0;
        frustum3[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum3[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum3[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = -zo;
        fy = yo;
        fz = 1.0;
        frustum4[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum4[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum4[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = zo;
        fy = -yo;
        fz = 1.0;
        frustum1[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum1[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum1[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        fx = -zo;
        fy = -yo;
        fz = 1.0;
        frustum2[0] = fx*cx[0] + fy*cy[0] + fz*cz[0];
        frustum2[1] = fx*cx[1] + fy*cy[1] + fz*cz[1];
        frustum2[2] = fx*cx[2] + fy*cy[2] + fz*cz[2];

        MathUtil.vhat(frustum1, frustum1);
        MathUtil.vhat(frustum2, frustum2);
        MathUtil.vhat(frustum3, frustum3);
        MathUtil.vhat(frustum4, frustum4);

        MathUtil.vhat(cz, boresightDirection);
        MathUtil.vhat(cx, upVector);
    }

    private void loadSumfile() throws NumberFormatException, IOException
    {
        File sumfile = new File(fullpath);
        String sumname = sumfile.getName().substring(0, 11);
        sumfile = sumfile.getParentFile().getParentFile().getParentFile().getParentFile();
        String sumfilename = sumfile.getAbsolutePath() + "/sumfiles/" + sumname + ".SUM";

        loadSumfile(
                sumfilename,
                spacecraftPosition,
                frustum1,
                frustum2,
                frustum3,
                frustum4,
                boresightDirection,
                upVector);

//        printpt(frustum1, "gas frustum1 ");
//        printpt(frustum2, "gas frustum2 ");
//        printpt(frustum3, "gas frustum3 ");
//        printpt(frustum4, "gas frustum4 ");
    }

    public HashMap<String, String> getProperties() throws IOException
    {
        HashMap<String, String> properties = new HashMap<String, String>();

        if (maxPhase < minPhase)
        {
            this.computeIlluminationAngles();
            this.computePixelScale();
        }

        DecimalFormat df = new DecimalFormat("#.######");

        properties.put("Spacecraft Distance", df.format(getSpacecraftDistance()) + " km");
        properties.put("Name", name);
        properties.put("Start Time", startTime);
        properties.put("Stop Time", stopTime);
        properties.put("Filter", String.valueOf(filter));
        properties.put("Day of Year", (new File(this.fullpath)).getParentFile().getParentFile().getName());
        properties.put("Year", (new File(this.fullpath)).getParentFile().getParentFile().getParentFile().getName());
        properties.put("Deblur Type", (new File(this.fullpath)).getParentFile().getName());

        // Note \u00B0 is the unicode degree symbol
        String deg = "\u00B0";
        properties.put("Minimum Incidence", df.format(minIncidence)+deg);
        properties.put("Maximum Incidence", df.format(maxIncidence)+deg);
        properties.put("Minimum Emission", df.format(minEmission)+deg);
        properties.put("Maximum Emission", df.format(maxIncidence)+deg);
        properties.put("Minimum Phase", df.format(minPhase)+deg);
        properties.put("Maximum Phase", df.format(maxPhase)+deg);
        properties.put("Minimum Horizontal Pixel Scale", df.format(1000.0*minHorizontalPixelScale) + " meters/pixel");
        properties.put("Maximum Horizontal Pixel Scale", df.format(1000.0*maxHorizontalPixelScale) + " meters/pixel");
        properties.put("Minimum Vertical Pixel Scale", df.format(1000.0*minVerticalPixelScale) + " meters/pixel");
        properties.put("Maximum Vertical Pixel Scale", df.format(1000.0*maxVerticalPixelScale) + " meters/pixel");

        return properties;
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
            vtkPolyData tmp = erosModel.computeFrustumIntersection(spacecraftPosition,
                    frustum1, frustum3, frustum4, frustum2);

            if (tmp == null)
                return;

            footprint.DeepCopy(tmp);

            vtkPointData pointData = footprint.GetPointData();
            pointData.SetTCoords(textureCoords);
            PolyDataUtil.generateTextureCoordinates(getFrustum(), footprint);
        }
        else
        {
            int resolutionLevel = erosModel.getModelResolution();

            String footprintFilename = null;
            File file = null;

            if (footprintIsOnLocalDisk)
            {
                if (key.source == MSISource.PDS)
                    footprintFilename = fullpath.substring(0, fullpath.length()-4) + "_FOOTPRINT_RES" + resolutionLevel + "_PDS.VTP";
                else
                    footprintFilename = fullpath.substring(0, fullpath.length()-4) + "_FOOTPRINT_RES" + resolutionLevel + "_GASKELL.VTP";

                file = new File(footprintFilename);
            }
            else
            {
                if (key.source == MSISource.PDS)
                    footprintFilename = key.name + "_FOOTPRINT_RES" + resolutionLevel + "_PDS.VTP";
                else
                    footprintFilename = key.name + "_FOOTPRINT_RES" + resolutionLevel + "_GASKELL.VTP";

                file = FileCache.getFileFromServer(footprintFilename);
            }

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
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, 0.002);

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

    public double getMinimumVerticalPixelScale()
    {
        return minVerticalPixelScale;
    }

    public double getMaximumVerticalPixelScale()
    {
        return maxVerticalPixelScale;
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

    // Computes the incidence, emission, and phase at a point with a given normal.
    // The output is a 3-vector with the first component equal to the incidence,
    // the second component equal to the emission and the third component equal to
    // the phase.
    private double[] computeIlluminationAnglesAtPoint(
            double[] pt,
            double[] normal)
    {
        double[] scvec = {
            spacecraftPosition[0] - pt[0],
            spacecraftPosition[1] - pt[1],
            spacecraftPosition[2] - pt[2]};


        double[] sunvec = {
                sunPosition[0] - pt[0],
                sunPosition[1] - pt[1],
                sunPosition[2] - pt[2]};

        double incidence = MathUtil.vsep(normal, sunvec) * 180.0 / Math.PI;
        double emission = MathUtil.vsep(normal, scvec) * 180.0 / Math.PI;
        double phase = MathUtil.vsep(sunvec, scvec) * 180.0 / Math.PI;

        double[] angles = {incidence, emission, phase};

        return angles;
    }

    private void computeIlluminationAngles()
    {
        if (footprintGenerated == false)
            loadFootprint();

        computeCellNormals();

        int numberOfCells = footprint.GetNumberOfCells();

        vtkPoints points = footprint.GetPoints();
        vtkDataArray normals = footprint.GetCellData().GetNormals();

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
        }
    }

    private void computePixelScale()
    {
        if (footprintGenerated == false)
            loadFootprint();

        int numberOfPoints = footprint.GetNumberOfPoints();

        vtkPoints points = footprint.GetPoints();

        minHorizontalPixelScale = Double.MAX_VALUE;
        maxHorizontalPixelScale = -Double.MAX_VALUE;
        minVerticalPixelScale = Double.MAX_VALUE;
        maxVerticalPixelScale = -Double.MAX_VALUE;

        double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
        double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

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
        }
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
        if (footprintGenerated == false)
            loadFootprint();

        computeCellNormals();

        //vtkBar2 cellLocator = new vtkBar2();
        //vbar.testme();

        int numLayers = 16;
        float[] data = new float[numLayers*IMAGE_HEIGHT*IMAGE_WIDTH];

        // If we are searching for a limb, use the locator of eros itself
        // since the locator of the footprint might not intersect along the boundary
        // even if there is no limb.
        vtksbCellLocator cellLocator = null;
        if (returnNullIfContainsLimb)
        {
            cellLocator = erosModel.getCellLocator();
        }
        else
        {
            //vtkCellLocator cellLocator = new vtkCellLocator();
            cellLocator = new vtksbCellLocator();
            cellLocator.SetDataSet(footprint);
            cellLocator.CacheCellBoundsOn();
            cellLocator.AutomaticOn();
            //cellLocator.SetMaxLevel(10);
            //cellLocator.SetNumberOfCellsPerNode(15);
            cellLocator.BuildLocator();
        }

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

        double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
        double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

        double scdist = MathUtil.vnorm(spacecraftPosition);

        vtkDataArray normals = footprint.GetCellData().GetNormals();

        for (int i=0; i<IMAGE_HEIGHT; ++i)
        {
            // Compute the vector on the left of the row.
            double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
            double[] left = {
                    corner1[0] + fracHeight*vec13[0],
                    corner1[1] + fracHeight*vec13[1],
                    corner1[2] + fracHeight*vec13[2]
            };

            for (int j=0; j<IMAGE_WIDTH; ++j)
            {
                // If we're just trying to know if there is a limb, we
                // only need to do intersections around the boundary of
                // the backplane, not the interior pixels.
                if (returnNullIfContainsLimb)
                {
                    if (j == 1 && i > 0 && i < IMAGE_HEIGHT-1)
                    {
                        j = IMAGE_WIDTH-2;
                        continue;
                    }
                }

                double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
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
                    double lon = llr.lon*180/Math.PI;
                    if (lon < 0.0)
                        lon += 360.0;

                    double[] normal = normals.GetTuple3(closestCell);
                    double[] illumAngles = computeIlluminationAnglesAtPoint(closestPoint, normal);

                    double horizPixelScale = closestDist * horizScaleFactor;
                    double vertPixelScale = closestDist * vertScaleFactor;

                    double[] coloringValues = erosModel.getAllColoringValues(closestPoint);

                    data[index(j,i,0)]  = (float)rawImage.GetScalarComponentAsFloat(j, i, 0, 0);
                    data[index(j,i,1)]  = (float)closestPoint[0];
                    data[index(j,i,2)]  = (float)closestPoint[1];
                    data[index(j,i,3)]  = (float)closestPoint[2];
                    data[index(j,i,4)]  = (float)(llr.lat * 180.0 / Math.PI);
                    data[index(j,i,5)]  = (float)(lon);
                    data[index(j,i,6)]  = (float)(llr.rad);
                    data[index(j,i,7)]  = (float)(illumAngles[0] * 180.0 / Math.PI);
                    data[index(j,i,8)]  = (float)(illumAngles[1] * 180.0 / Math.PI);
                    data[index(j,i,9)]  = (float)(illumAngles[2] * 180.0 / Math.PI);
                    data[index(j,i,10)] = (float)(horizPixelScale);
                    data[index(j,i,11)] = (float)(vertPixelScale);
                    data[index(j,i,12)] = (float)coloringValues[0]; // slope
                    data[index(j,i,13)] = (float)coloringValues[1]; // elevation;
                    data[index(j,i,14)] = (float)coloringValues[2]; // grav acc;
                    data[index(j,i,15)] = (float)coloringValues[3]; // grav pot;
                }
                else
                {
                    if (returnNullIfContainsLimb)
                        return null;

                    for (int k=0; k<numLayers; ++k)
                        data[index(j,i,k)] = PDS_NA;
                }
            }
        }

        return data;
    }

    public String generateBackplanesLabel() throws IOException
    {
        String lblFilename = fullpath.substring(0, fullpath.length()-4) + "_DDR.LBL";

        FileInputStream fs = null;
        try {
            fs = new FileInputStream(lblFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        StringBuffer strbuf = new StringBuffer("");

        String str;
        while ((str = in.readLine()) != null)
        {
            // The software name and version in the downloaded ddr is not correct
            if (str.trim().startsWith("SOFTWARE_NAME"))
            {
                strbuf.append("SOFTWARE_NAME                = \"Small Body Mapping Tool\"\r\n");
                continue;
            }

            if (str.trim().startsWith("SOFTWARE_VERSION_ID"))
            {
                strbuf.append("SOFTWARE_VERSION_ID          = \"2.0\"\r\n");
                continue;
            }

            // The planes in the downloaded ddr are all wrong
            if (str.trim().startsWith("BANDS"))
            {
                strbuf.append("    BANDS                    = 16\r\n");
                strbuf.append("    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL\r\n");
                strbuf.append("    BAND_NAME                = (\"MSI pixel value\",\r\n");
                strbuf.append("                                \"x coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
                strbuf.append("                                \"y coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
                strbuf.append("                                \"z coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
                strbuf.append("                                \"Latitude, deg\",\r\n");
                strbuf.append("                                \"Longitude, deg\",\r\n");
                strbuf.append("                                \"Distance from center of body, km\",\r\n");
                strbuf.append("                                \"Incidence angle, measured against the plate model, deg\",\r\n");
                strbuf.append("                                \"Emission angle, measured against the plate model, deg\",\r\n");
                strbuf.append("                                \"Phase angle, measured against the plate model, deg\",\r\n");
                strbuf.append("                                \"Horizontal pixel scale, km per pixel\",\r\n");
                strbuf.append("                                \"Vertical pixel scale, km per pixel\",\r\n");
                strbuf.append("                                \"Slope, deg\",\r\n");
                strbuf.append("                                \"Elevation, m\",\r\n");
                strbuf.append("                                \"Gravitational acceleration, m/s^2\",\r\n");
                strbuf.append("                                \"Gravitational potential, J/kg\")\r\n");
                strbuf.append("\r\n");
                strbuf.append("  END_OBJECT                 = IMAGE\r\n");
                strbuf.append("\r\n");
                strbuf.append("END_OBJECT                   = FILE\r\n");
                strbuf.append("\r\n");
                strbuf.append("END\r\n");

                break;
            }

            strbuf.append(str).append("\r\n");

            // Add the shape model used for generating the ddr right aftet the sun position
            // since this is not in the label file on the server
            if (str.trim().startsWith("SUN_POSITION_LT"))
                strbuf.append("\r\nSHAPE_MODEL = \"" + erosModel.getModelName() + "\"\r\n");
        }

        return strbuf.toString();

        /*
         TODO consider generating the entire lbl file on the fly

        StringBuffer str = new StringBuffer("");

        String nl = System.getProperty("line.separator");

        str.append("PDS_VERSION_ID               = PDS3").append(nl);
        str.append("").append(nl);
        str.append("/* DDR Identification *//*").append(nl);
        str.append("").append(nl);
        str.append("INSTRUMENT_HOST_NAME         = \"NEAR EARTH ASTEROID RENDEZVOUS\"").append(nl);
        str.append("SPACECRAFT_ID                = NEAR").append(nl);
        str.append("INSTRUMENT_NAME              = \"MULTI-SPECTRAL IMAGER\"").append(nl);
        str.append("INSTRUMENT_ID                = MSI").append(nl);
        str.append("TARGET_NAME                  = EROS").append(nl);
        str.append("PRODUCT_TYPE                 = DDR").append(nl);
        str.append("PRODUCT_CREATION_TIME        = ").append(nl);
        str.append("START_TIME                   = ").append(nl);
        str.append("STOP_TIME                    = ").append(nl);
        str.append("SPACECRAFT_CLOCK_START_COUNT = ").append(nl);
        str.append("SPACECRAFT_CLOCK_STOP_COUNT  = ").append(nl);
        str.append("").append(nl);

        return str.toString();
        */
    }

    public int index(int i, int j, int k)
    {
        return ((k * IMAGE_HEIGHT + j) * IMAGE_WIDTH + i);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            System.out.println("updating msi image");
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

        int cellId = erosModel.computeRayIntersection(spacecraftPosition, direction, focalPoint);

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

    public Frustum getFrustum()
    {
        return new Frustum(spacecraftPosition, frustum1, frustum3, frustum4, frustum2);
    }

}
