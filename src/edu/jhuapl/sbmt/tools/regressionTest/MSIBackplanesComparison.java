package edu.jhuapl.sbmt.tools.regressionTest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.core.keys.ImageKey;
import edu.jhuapl.sbmt.model.eros.MSIImage;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.tools.BackplanesGenerator;
import edu.jhuapl.sbmt.util.BackplaneInfo;
import edu.jhuapl.sbmt.util.BackplanesFileFormat;
import edu.jhuapl.sbmt.util.FitsBackplanesFile;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;


/**
 * Comparison of Eli's 2013 backplanes .img files archived at http://sbmt.jhuapl.edu/internal/misc/backplanes/
 * and same files generated by the current (2016) state of the BackplanesGenerator.java source code. Note that
 * the original source FITS file used by Eli was not found in the /project/nearsdc directories. It was found in
 * the PDS archive https://pds.nasa.gov/ds-view/pds/viewProfile.jsp?dsid=NEAR-A-MSI-3-EDR-EROS/ORBIT-V1.0
 *
 * The result is that only the actual pixel values differ, the backplanes themselves are identical to within epsilon.
 * the original image is resampled in height from the original FITS image height of 244 to the MSIImage.java
 * height of 412.
 *
 * This class contians hardcoded paths and is not intended to be run on the command line.
 */
public class MSIBackplanesComparison
{
    private static int numBands = BackplaneInfo.values().length;
    private static int numLines = MSIImage.RESAMPLED_IMAGE_HEIGHT;
    private static int numSamples = MSIImage.RESAMPLED_IMAGE_WIDTH;
    private static double EPS = 1E-7;
    private static String cacheDirectory = "/GASKELL/EROS/MSI/images/";
    private static String workingDir = "C:/Users/nguyel1/Projects/SBMT/DaveBlewettNearMsiPds/backplanesGeneratorTest/";
    private static SmallBodyModel smallBodyModel;


    public static void main(String[] args) throws Exception
    {
        // VTK and authentication
        Configuration.setAppName("neartool");
        Configuration.setCacheVersion("2");
        Configuration.setAPLVersion(true);
        SmallBodyViewConfig.initialize();
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();
        Configuration.authenticate();

        smallBodyModel = SbmtModelFactory.createSmallBodyModel(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL, null)).get(0);

        runComparisonTest(workingDir);
//        dumpLatLonValues(workingDir);
    }

    /**
     * Convert the original FITS image to an .img file, and compare it to Eli's archived .img file, from the original backplanes
     * archive he had created before leaving APL. It appears that he used INFO file pointing (SPICE), unlike the MSI PDS backplanes
     * delivery for Dave Blewett/Olivier, which will use SUMFILE pointing. Also unlike the current delivery, Eli's archived
     * backplanes are in .IMG format rather than .FITS, and they have PDS3 labels rather than PDS4.
     */
    private static void runComparisonTest(String testDirectory) throws Exception
    {
        System.err.println("Epsilon value used in comparison: " + EPS);


        // Read in the archived .img file that Eli had created in 2013 (http://sbmt.jhuapl.edu/internal/misc/backplanes/, see email from Dave Blewett February 23, 2016 2:50 PM)

        String archivedImgSpiceBackplanesFilename = "M0131776147F1_2P_CIF_SPICE_res3_ddr.img";
        String archivedImgSpiceBackplanesFile = testDirectory + "archivedBackplanes/" + archivedImgSpiceBackplanesFilename;
        int shapeResolution = Integer.valueOf(parseResolutionLevel(archivedImgSpiceBackplanesFile)); //Need not generate all resolutions, just the one that was used to generate the .img file


        // Generate backplanes from the source FITS image using the current state of the SBMT BackplanesGenerator code.
        // The source images are not located on the SBMT server, I do not know where Eli stored them. I pulled them from PDS.

//        String sourceFile = cacheDirectory + "M0131776147F1_2P_CIF.FIT"; // These files are masked (no black edges), Eli's archived ones are not, so the edge pixels won't agree with Eli's archive if we use these. I also don't think this is the image Eli generated the backplanes for, even though the file names match. The pixel values are very different (which may be why the CIF is much brighter than the CIF DBL).
        String sourceFile = cacheDirectory + "M0131776147F1_2P_IOF_DBL.FIT"; // These are not masked (they have black edges) but they are deblurred. The file names in Eli's archive do not have "DBL" in them, so deblurring may be the cause of the remaining very small discrepancy in the pixel values. USE THESE AS INPUT.
        String outputFolder = testDirectory + "backplanesGeneratorOutput";


        // Run a comparison between the linux vs windows generated backplanes files.

        if (sourceFile.toUpperCase().contains("M0131776147F1_2P_CIF_DBL") )
        {
            String windows = createBackplanes(sourceFile, outputFolder, ImageSource.GASKELL, BackplanesFileFormat.FITS, shapeResolution);
            String linux = "C:/Users/nguyel1/Projects/SBMT/DaveBlewettNearMsiPds/backplanesGeneratorTest/backplanesGeneratorOutput/unix/MSIBackplanesSmall/M0131776147F1_2P_CIF_DBL.FIT"; //This file was generated using sumfiles_to_be_delivered (using the Linux hand-modified and compiled MSIImage.initializeSumfileFullPath())
            System.err.println("Results of comparison (Windows-created vs Linux-created backplanes files)");
            compare(readFitsFile(linux), readFitsFile(windows));
        }

        // Run a comparison between a generated backplanes file and Eli's archived one.

        double[][][] archivedImgSpice = readImgFile(archivedImgSpiceBackplanesFile);
        double[][][] generatedImgSpice = readImgFile(createBackplanes(sourceFile, outputFolder, ImageSource.SPICE, BackplanesFileFormat.IMG, shapeResolution));
        System.err.println("Results of comparison (Eli's archived .img backplanes file v/s newly created .img backplanes file)");
        compare(generatedImgSpice, archivedImgSpice);

        // Also generate a FITS backplanes file from from scratch from the original image FITS file, output to IMG format. Use SPICE pointing, same as archived file.

        String backplanesFile = createBackplanes(sourceFile, outputFolder, ImageSource.SPICE, BackplanesFileFormat.FITS, shapeResolution); //This will use whichever sumfile is specified in MSIImage.initializeSumfileFullPath(), so either cache's sumfiles/ or sumfiles_to_be_delivered/

        // Convert the .img file that Eli had created in 2013 to FITS (for Windows comparison with the file generated from scratch above, use vbindiff).

        createFitsFileFromImg(archivedImgSpiceBackplanesFilename, archivedImgSpice, outputFolder);

        // RESULTS OF COMPARISON:
        // When I compare the original CIF FITS image against its CIF_DBL counterpart, the backplanes match
        // identically, but the pixel values are off by a fraction of a percent (at 7 decimal places).
        // According to PDS dataset catalog at
        // http://sbn.psi.edu/archive/near/NEAR_A_MSI_3_EDR_EROS_ORBIT_V1_0/catalog/msi2erosds.cat, the CIF DBL
        // images began with the CIF images, then were calibrated to units of radiance and deblurred (but the CIF
        // images were also calibratied to units of radiance, so are the CIF DBL images just deblurred versions of
        // the CIF images?) The calibrations/deblurring are described in
        // http://sbn.psi.edu/archive/near/NEAR_A_MSI_3_EDR_EROS_ORBIT_V1_0/document/msical.txt,
        // which in turn is described in Li,H., M.S. Robinson and S.L.Murchie,Preliminary remediation of scattered
        // light in NEAR MSI Images, Icarus, 2000
        // I think that Eli's archive was of the deblurred images, though his archived FITS files are named without
        // the "_DBL_" tag that's applied to deblurred images. I still can't account for the very small pixel
        // difference. Olivier says not to worry about it. I agree. We don't know what sort of resampling Eli used
        // on the original backplanes, it may be related to that. The BackplanesGenerator uses linear interpolation.
        // As long as we document with PDS about how this is done, then I don't think it's an issue.
        // I suspect it may be some conversion in reading in the fits image - see loadFitsFiles in PerspectiveImage.


     // This has both an old and a new sumfile (in sumfiles/ and sumfiles_to_be_delivered). Generate backplanes for both sumfiles and compare them. Also visually check backplanes values in SBMT.

     sourceFile = cacheDirectory + "M0131203683F4_2P_IOF_DBL.FIT";

    }

//    private static void dumpLatLonValues(String testDirectory) throws Exception
//    {
//        // Generate a FITS backplanes file from the original image FITS file. This is the procedure if we need to create the backplanes from scratch.
//
//        String outputFolder = testDirectory + "backplanesGeneratorOutput";
//        String sourceFile = cacheDirectory + "M0157416268F3_2P_IOF_DBL.FIT"; //this is one of the last images MSI took
//        String backplanesFile = createFitsSpiceFile(sourceFile, outputFolder, "3");
//
//        // Dump out the lat/lon backplanes to visually compare with the SBMT lat/lon for the same image
//
//        dumpLatLon(readFitsFile(backplanesFile));
//        System.err.println("To complete this test, run SBMT, map image M0157416268F3_2P_IOF_DBL.FIT, and verify that the Lat Lon shown by the app matches the lat, lon printed above.");
//    }

    /**
     * Create a backplanes file from a FITS sourceFile.
     * (0 lowest resolution, 3 highest).
     */
    private static String createBackplanes(String fitsImageFile, String outputFolder, ImageSource pointing, BackplanesFileFormat fmt, int resolutionLevel) throws Exception
    {
        smallBodyModel.setModelResolution(resolutionLevel);
        ImageKey key = new ImageKey(fitsImageFile.replace(".FIT", ""), pointing, ((SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[0]);

        //Write out the FITS file
        (new BackplanesGenerator()).generateBackplanes(fitsImageFile, key.instrument.getInstrumentName(), outputFolder, smallBodyModel, fmt, pointing);

        //Return the fits backplanes file name
        String backplanesFilename = BackplanesGenerator.getBaseFilename(key, outputFolder) + fmt.getExtension();
        return backplanesFilename;
    }

    /**
     * Create a FITS backplanes file from 3D data, using the shape model resolution level specified
     * (0 lowest resolution, 3 highest).
     * @throws Exception
     */
    private static void createFitsFileFromImg(String sourceFileBasename, double[][][] imgData, String outputFolder) throws Exception
    {
        FitsBackplanesFile fpf = new FitsBackplanesFile();
        String fileName = outputFolder + File.separator + sourceFileBasename + ".fit";
        fpf.write(imgData, sourceFileBasename, fileName, numBands);
        System.err.println("Testing - done creating FITS file from 3D data in Eli's original archived .img backplanes file, written to " + fileName);
   }

    /**
     * Read the 3D data from an .img backplanes file.
     *
     * Read a binary data file consisting of floating point data arranged in multiple 2-D arrays such that each
     * 2D array is the same size and the arrays are stored in sequence, one after another.
     *
     * numLines, numSamples define the size of each 2D array
     * numPlanes defines the number of 2D arrays in the data file.
     * @author espirrc1
     *
     */
    private static double[][][] readImgFile(String imgFile) throws IOException
    {
        DataInputStream is = new DataInputStream(
                new BufferedInputStream(new FileInputStream(imgFile)));

        double[][][] data = new double[numBands][numLines][numSamples];
        for (int pp = 0; pp < numBands; pp++)
        {
            for (int ll = 0; ll < numLines; ll++)
            {
                for (int ss = 0; ss < numSamples; ss++)
                {
                    float dataPoint = is.readFloat();
                    // System.out.printf("%d, %d, %d\n", pp, ll, ss);
                    data[pp][ll][ss] = dataPoint;
                }
            }
        }

        System.out.println("Reading IMG file " + imgFile + ", data array size:");
        System.out.println(data.length + " " + data[0].length + " " + data[0][0].length);
        is.close();
        return data;
    }

    /**
     * Read the 3D data from a .fits backplanes file.
     */
    private static float[][][] readFitsFile(String fitsFile)
            throws FitsException, IOException
    {
        System.err.println("Reading FITS file " + fitsFile);
        float[][] array2D = null;
        float[][][] array3D = null;
        double[][][] array3Ddouble = null;

        int[] fitsAxes = null;
        int fitsNAxes = 0;
        // height is axis 0
        int fitsHeight = 0;
        // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis
        // is 2
        int fitsWidth = 0;
        // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
        int fitsDepth = 0;

        Fits f = new Fits(fitsFile);
        BasicHDU h = f.getHDU(0);

        fitsAxes = h.getAxes();
        fitsNAxes = fitsAxes.length;
        fitsHeight = fitsAxes[0];
        fitsWidth = fitsNAxes == 3 ? fitsAxes[2] : fitsAxes[1];
        fitsDepth = fitsNAxes == 3 ? fitsAxes[1] : 1;

        Object data = h.getData().getData();

        if (data instanceof float[][][])
        {
            array3D = (float[][][]) data;
        }
        return array3D;
    }

//    /**
//     * Dump the latitude/longitude planes, pixel by pixel, from a float data array (e.g. as in a fits backplanes file).
//     */
//    private static void dumpLatLon(float[][][] data)
//    {
//        int latPlane = BackplaneInfo.LAT.ordinal();
//        int lonPlane = BackplaneInfo.LON.ordinal();
//        for (int lines = 0; lines < numLines; lines++)
//        {
//            for (int samples = 0; samples < numSamples; samples++)
//            {
//                System.err.println("lat, lon for pixel [" + lines + "][" + samples + "] = " + data[latPlane][lines][samples] + ", " + data[lonPlane][lines][samples]);
//            }
//        }
//    }

    /**
     * Compare the backplanes data. Output differences greater than EPS to console.
     */
    private static void compare(double[][][] generated, double[][][] archived)
    {
        List<BackplaneInfo> planeList = BackplaneInfo.getPlanes(numBands);
        double maxDiff = 0;
        double value = 1;
        for (int planes = 0; planes < numBands; planes++)
        {
            for (int lines = 0; lines < numLines; lines++)
            {
                for (int samples = 0; samples < numSamples; samples++)
                {
                    double diff = Math.abs(archived[planes][lines][samples] - generated[planes][lines][samples]);
                    if (diff > EPS)
                    {
                        if (diff > maxDiff)
                        {
                            maxDiff = diff;
                            value = archived[planes][lines][samples];
                        }
                        System.err.println("   Diff at index " + planes + ", " + lines + ", " + samples + ". Plane is " + planeList.get(planes).toString());
                        System.err.println("        archived  " + archived[planes][lines][samples]);
                        System.err.println("        generated " + generated[planes][lines][samples]);
                    }
                }
            }
        }
        System.err.println("Max diff " + maxDiff);
        System.err.println("Max percent diff " + (100.0 * maxDiff/value) + " %");
    }

    private static void compare(float[][][] generated, float[][][] archived)
    {
        EPS = 1E-20;
        List<BackplaneInfo> planeList = BackplaneInfo.getPlanes(numBands);
        double maxDiff = 0;
        double value = 1;
        for (int planes = 0; planes < numBands; planes++)
        {
            for (int lines = 0; lines < numLines; lines++)
            {
                for (int samples = 0; samples < numSamples; samples++)
                {
                    double diff = Math.abs(archived[planes][lines][samples] - generated[planes][lines][samples]);
                    if (diff > EPS)
                    {
                        if (diff > maxDiff)
                        {
                            maxDiff = diff;
                            value = archived[planes][lines][samples];
                        }
                        System.err.println("   Diff at index " + planes + ", " + lines + ", " + samples + ". Plane is " + planeList.get(planes).toString());
                        System.err.println("        archived  " + archived[planes][lines][samples]);
                        System.err.println("        generated " + generated[planes][lines][samples]);
                    }
                }
            }
        }
        System.err.println("Max diff " + maxDiff);
        System.err.println("Max percent diff " + (100.0 * maxDiff/value) + "%");
    }

    /**
     * Parse the resolution level from the .img filename.
     */
    private static String parseResolutionLevel(String imgFile)
    {
        String fname = new File(imgFile).getName();
        String[] tokens = fname.split("res");
        return tokens[1].substring(0, 1);
    }

}
