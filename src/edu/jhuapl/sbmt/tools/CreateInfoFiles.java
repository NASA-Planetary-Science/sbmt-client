package edu.jhuapl.sbmt.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.model.time.FileUtils;

import altwg.util.FileUtil;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

/**
 * Class to generate infofiles (SBMT-formatted pointing files).
 *
 * Converted into Java from the C++ routines in misc/programs.
 *
 * @author nguyel1
 *
 */
public class CreateInfoFiles
{
    public static final int MAX_BOUNDS = 4;
    public static final String TIME_FORMAT = "YYYY-MON-DD HR:MN:SC.###::UTC (UTC)";
    public static final String FITS_EXTENSIONS = "fit,fits,FIT,FITS";

    public CreateInfoFiles(File mk) throws SpiceErrorException
    {
        System.loadLibrary("JNISpice");
        CSPICE.furnsh(mk.getAbsolutePath());
    }

    public void processFits(File metakernel, final String sbmtDir, File imageDir, File outputDir, String body, String bodyFrame, final String spacecraftName, String instrFrame, final String sclkKeyword ) throws Exception
    {
        Vector<ImageInfo> imageTable = getFitsImageList(imageDir, sbmtDir, FITS_EXTENSIONS, spacecraftName, sclkKeyword);
        createInfoFiles(metakernel, sbmtDir, imageDir, outputDir, body, bodyFrame, spacecraftName, instrFrame, imageTable);

    }
    public void processImageTable(File metakernel, final String sbmtDir, File imageDir, File outputDir, String body, String bodyFrame, final String spacecraftName, String instrFrame, File imageTimesFile, TimeFormat timeFormat) throws Exception
    {
        Vector<ImageInfo> imageTable = readTimesFile(imageDir, imageTimesFile, timeFormat, spacecraftName);
        createInfoFiles(metakernel, sbmtDir, imageDir, outputDir, body, bodyFrame, spacecraftName, instrFrame, imageTable);
    }

    /**
     * Reads all .fits files in imageDir and creates an infofile for them using
     * the SPICE metakernel specified. Outputs the infofiles and an imagelist to
     * be used in the SBMT RunInfo enum in DatabaseGeneratorSQL. The imagelist
     * contains the sbmt-relative path for all of the images processed.
     * @throws Exception
     */
    public void createInfoFiles(File metakernel, final String sbmtDir, File imageDir, File outputDir, String body, String bodyFrame, final String spacecraftName, String instrFrame, Vector<ImageInfo> imageTimes ) throws Exception
    {
        for (ImageInfo imageInfo : imageTimes)
        {
          //Outputs:
          double[] updir = new double[3];
          double[] boredir = new double[3];
          double[] sunPosition = new double[3];
          double[] scPosition = new double[3];
          double[] unused = new double[3];
          double[] frustum = new double[3 * MAX_BOUNDS];
          //Inputs:
          double et = imageInfo.et;
          String utc = CSPICE.et2utc(et, "ISOC", 10);

          getTargetState(    et, "ORX", "BENNU", "IAU_BENNU", "SUN", sunPosition, unused);
          getSpacecraftState(et, "ORX", "BENNU", "IAU_BENNU", scPosition, unused);
          getFov(            et, "ORX", "BENNU", "IAU_BENNU", "ORX_OCAMS_MAPCAM", boredir, updir, frustum);

          String fileBaseName = FilenameUtils.getBaseName(imageInfo.imageFile.getName());
          String infofileName = fileBaseName + ".INFO";
          File infoFile = new File(outputDir, infofileName);

          saveInfoFile(infoFile, utc, scPosition, boredir, updir, frustum, sunPosition);
        }
    }

    private Vector<ImageInfo> getFitsImageList(File imageDir, final String sbmtDir, final String extensions, final String spacecraftName, final String sclkKeyword) throws Exception
    {
        final Vector<ImageInfo> imageTable = new Vector<ImageInfo>();

        //Generate the file used by DatabaseGeneratorSQL.RunInfo.
        final File imageListFullPath = new File(imageDir.getParent(), "imagelist-fullpath.txt");
        org.apache.commons.io.FileUtils.deleteQuietly(imageListFullPath);
        //Generate the file used by FixedListQuery.
        final File imageList = new File(imageDir.getParent(), "imagelist.txt");
        org.apache.commons.io.FileUtils.deleteQuietly(imageList);

        SimpleFileVisitor<Path> fitsFileVisitor = new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException
            {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{" + extensions + "}");
                if (pathMatcher.matches(path))
                {
                    FileUtils.appendTextToFile(imageListFullPath.getAbsolutePath(), sbmtDir + "/" + path.getFileName());
                    try
                    {
                        FileUtils.appendTextToFile(imageList.getAbsolutePath(), path.getFileName() + " " + getFitsUtc(path.toFile(), sclkKeyword, spacecraftName));
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error adding " + path.getFileName() + " to image list.");
                    }
                    try
                    {
                        imageTable.add(new ImageInfo(path.toFile(), getFitsEt(path.toFile(), sclkKeyword, spacecraftName)));
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error getting time keyword for " + path.getFileName() + ". Infofile will not be generated.");
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(Paths.get(imageDir.getAbsolutePath()), fitsFileVisitor);

        return imageTable;
    }

    public Vector<ImageInfo> readTimesFile(File inputDir, File timesFile, TimeFormat timeFormat, String spacecraftName) throws Exception
    {
        Vector<ImageInfo> imageInfo = new Vector<ImageInfo>();
        ArrayList<String> list = FileUtil.getFileLinesAsStringList(timesFile.getAbsolutePath());
        for (String str : list)
        {
            if (!str.trim().startsWith("#"))
            {
                StringTokenizer tok = new StringTokenizer(str.trim(), " ");
                String filename = String.valueOf(tok.nextElement());
                File f = new File(inputDir,filename);
                double et = Double.NaN;
                switch (timeFormat)
                {
                case et:
                    et = Double.valueOf(tok.nextElement().toString());
                    break;
                case utc:
                    et = CSPICE.str2et(String.valueOf(tok.nextElement()));
                    break;
                case sclk:
                    int clkid = CSPICE.bodn2c(spacecraftName);
                    et = CSPICE.scs2e(clkid, String.valueOf(tok.nextElement()));
                    break;
                default:
                    break;
                }
                ImageInfo imgInfo = new ImageInfo(f, et);
                if (!Double.isNaN(et))
                {
                    imageInfo.add(imgInfo);
                }
            }
        }
        return imageInfo;
    }

    private enum TimeFormat
    {
        utc, et, sclk;
    }

    //Not necessarily an image, can be spectra, e.g.
    private class ImageInfo
    {
        File imageFile;
        double et;
        public ImageInfo(File imageFile, double et)
        {
            this.imageFile = imageFile;
            this.et = et;
        }
    }

    private double getFitsEt(File fitsFile, String sclkKeyword, String spacecraftName) throws Exception
    {
        int clockID = CSPICE.bodn2c(spacecraftName);

        Fits thisFits = new Fits(fitsFile);
        BasicHDU thisHDU = thisFits.getHDU(0);
        String clockString = thisHDU.getHeader().getStringValue(sclkKeyword);
        thisFits.close();

        // SCLK_STR= '3/0559394223.31' / Spacecraft clock string
        double et = CSPICE.scs2e(clockID, clockString);
        String utc = CSPICE.et2utc(et, "ISOC", 3);

        return et;
    }
    private String getFitsUtc(File fitsFile, String sclkKeyword, String spacecraftName) throws Exception
    {
        double et = getFitsEt(fitsFile, sclkKeyword, spacecraftName);
        String utc = CSPICE.et2utc(et, "ISOC", 3);

        return utc;
    }

    private void saveInfoFile(File filename,
                      String utc,
                      double scposb[],
                      double boredir[],
                      double updir[],
                      double frustum[],
                      double sunpos[]) throws IOException
    {
        FileOutputStream fs = null;
        try
        {
            fs = new FileOutputStream(filename);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: Unable to open file " + filename.getAbsolutePath() + " for writing.");
            return;
        }
        OutputStreamWriter osw = new OutputStreamWriter(fs);
        BufferedWriter out = new BufferedWriter(osw);

        //Trucate the subseconds field to milliseconds
        out.write("START_TIME          = " + utc.substring(0, 23) + "\n");
        out.write("STOP_TIME           = " + utc.substring(0, 23) + "\n");

        out.write("SPACECRAFT_POSITION = ( ");
        out.write(String.format("%1.16e", scposb[0]) + " , ");
        out.write(String.format("%1.16e", scposb[1]) + " , ");
        out.write(String.format("%1.16e", scposb[2]) + " )\n");

        out.write("BORESIGHT_DIRECTION = ( ");
        out.write(String.format("%1.16e", boredir[0]) + " , ");
        out.write(String.format("%1.16e", boredir[1]) + " , ");
        out.write(String.format("%1.16e", boredir[2]) + " )\n");

        out.write("UP_DIRECTION        = ( ");
        out.write(String.format("%1.16e", updir[0]) + " , ");
        out.write(String.format("%1.16e", updir[1]) + " , ");
        out.write(String.format("%1.16e", updir[2]) + " )\n");

        out.write("FRUSTUM1            = ( ");
        out.write(String.format("%1.16e", frustum[0]) + " , ");
        out.write(String.format("%1.16e", frustum[1]) + " , ");
        out.write(String.format("%1.16e", frustum[2]) + " )\n");

        out.write("FRUSTUM2            = ( ");
        out.write(String.format("%1.16e", frustum[3]) + " , ");
        out.write(String.format("%1.16e", frustum[4]) + " , ");
        out.write(String.format("%1.16e", frustum[5]) + " )\n");

        out.write("FRUSTUM3            = ( ");
        out.write(String.format("%1.16e", frustum[6]) + " , ");
        out.write(String.format("%1.16e", frustum[7]) + " , ");
        out.write(String.format("%1.16e", frustum[8]) + " )\n");

        out.write("FRUSTUM4            = ( ");
        out.write(String.format("%1.16e", frustum[9]) + " , ");
        out.write(String.format("%1.16e", frustum[10]) + " , ");
        out.write(String.format("%1.16e", frustum[11]) + " )\n");

        out.write("SUN_POSITION_LT     = ( ");
        out.write(String.format("%1.16e", sunpos[0]) + " , ");
        out.write(String.format("%1.16e", sunpos[1]) + " , ");
        out.write(String.format("%1.16e", sunpos[2]) + " )\n");

        out.close();
    }

    /*
     * This function computes the instrument boresight and frustum vectors in
     * the observer body frame at the time the spacecraft imaged the body.
     *
     * Input: et: Ephemeris time when an image of the body was taken
     * body: Name of observer body (e.g. EROS, PLUTO, PHOEBE) bodyFrame:
     * Name of observer body-fixed frame (e.g. IAU_EROS, IAU_PLUTO, RYUGU_FIXED)
     * spacecraft: Name of the spacecraft that took the image instrFrame: SPICE
     * frame ID of instrument on the observing spacecraft
     *
     * Output: boredir: Boresight direction in bodyframe coordinates updir:
     * frustum: Field of view boundary corner vectors in bodyframe coordinates
     *
     */
    public void getFov(double et, String spacecraft, String body,
            String bodyFrame, String instrFrame, double[] boredir,
            double[] updir, double[] frustum) throws SpiceErrorException
    {
        double[] lt = new double[6];
        double[] notUsed = new double[3];
        double[][] inst2inert = new double[3][3];
        double[][] inert2bf = new double[3][3];
        double[][] inst2bf = new double[3][3];
        String[] shape = new String[30];
        String[] frame = new String[30];
        double[] bsight = new double[3];
        int n;
        int[] size = new int[5];
        double[] bounds = new double[MAX_BOUNDS * 3];
        double[][] boundssbmt = new double[MAX_BOUNDS][3];
        // The celestial body is the target when dealing with light time
        String abcorr = "LT+S";
        String inertframe = "J2000";
        int instid;
        double[] tmpvec = new double[] { 1.0, 0.0, 0.0 };

        /*
         * Compute the apparent position of the center of the observer body as
         * seen from the spacecraft at the epoch of observation (et), and the
         * one-way light time from the observer to the spacecraft. Only the
         * returned light time will be used from this call, as such, the
         * reference frame does not matter here. Use J2000.
         */
        CSPICE.spkpos(body, et, inertframe, abcorr, spacecraft, notUsed,
                lt);

        /*
         * Get field of view boresight and boundary corners
         */
        instid = CSPICE.namfrm(instrFrame);
        CSPICE.getfov(instid, shape, frame, bsight, size, bounds);

        /*
         * Get the coordinate transformation from instrument to inertial frame
         * at time ET
         */
        inst2inert = CSPICE.pxform(instrFrame, inertframe, et);

        /*
         * Get the coordinate transformation from inertial to body-fixed
         * coordinates at ET minus one light time ago. This subtraction is
         * neccessary because the body is the observer in SBMT, but et is the
         * time at the spacecraft. The light time here is the time it takes
         * light to travel between the body and the spacecraft.
         */
        inert2bf = CSPICE.pxform(inertframe, bodyFrame, et - lt[0]);

        /*
         * Compute complete transformation to go from instrument-fixed
         * coordinates to body-fixed coords
         */
        inst2bf = CSPICE.mxm(inert2bf, inst2inert);

        // swap the boundary corner vectors so they are in the correct order for
        // SBMT
        // getfov returns them in the following order (quadrants): I, II, III,
        // IV.
        // SBMT expects them in the following order (quadrants): II, I, III, IV.
        // So the vector index mapping is
        // SBMT SPICE
        // 0 1
        // 1 0
        // 2 2
        // 3 3
        // boundssbmt[0][0] = bounds[1][0];
        // boundssbmt[0][1] = bounds[1][1];
        // boundssbmt[0][2] = bounds[1][2];
        // boundssbmt[1][0] = bounds[0][0];
        // boundssbmt[1][1] = bounds[0][1];
        // boundssbmt[1][2] = bounds[0][2];
        // boundssbmt[2][0] = bounds[2][0];
        // boundssbmt[2][1] = bounds[2][1];
        // boundssbmt[2][2] = bounds[2][2];
        // boundssbmt[3][0] = bounds[3][0];
        // boundssbmt[3][1] = bounds[3][1];
        // boundssbmt[3][2] = bounds[3][2];
        boundssbmt[0][0] = bounds[3];
        boundssbmt[0][1] = bounds[4];
        boundssbmt[0][2] = bounds[5];
        boundssbmt[1][0] = bounds[0];
        boundssbmt[1][1] = bounds[1];
        boundssbmt[1][2] = bounds[2];
        boundssbmt[2][0] = bounds[6];
        boundssbmt[2][1] = bounds[7];
        boundssbmt[2][2] = bounds[8];
        boundssbmt[3][0] = bounds[9];
        boundssbmt[3][1] = bounds[10];
        boundssbmt[3][2] = bounds[11];

        // transform boresight into body frame.
        double[] bs = CSPICE.mxv(inst2bf, bsight);

        // transform boundary corners into body frame and pack into frustum
        // array.
        int k = 0;
        for (int i = 0; i < MAX_BOUNDS; i++)
        {
            double[] bdyCorner = new double[3];
            double[] bdyCornerBodyFrm = new double[3];
            bdyCorner[0] = boundssbmt[i][0];
            bdyCorner[1] = boundssbmt[i][1];
            bdyCorner[2] = boundssbmt[i][2];
            bdyCornerBodyFrm = CSPICE.mxv(inst2bf, bdyCorner);
            for (int j = 0; j < 3; j++)
            {
                frustum[k] = bdyCornerBodyFrm[j];
                k++;
            }
        }

        /* Then compute the up direction */
        double[] up = CSPICE.mxv(inst2bf, tmpvec);

        for (int j = 0; j < 3; j++)
        {
            boredir[j] = bs[j];
            updir[j] = up[j];
        }
    }

    /*
     * This function computes the state (position and velocity) of the
     * spacecraft in the observer body frame, at the time the spacecraft imaged
     * the body.
     *
     * Input: et: Ephemeris time when the image was taken body: Name of
     * observer body (e.g. EROS, PLUTO) spacecraft: NAIF SPICE name of
     * spacecraft (e.g. NEAR, NH). These can be found at
     * https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/FORTRAN/req/naif_ids.html
     *
     * Output: bodyToSc: The position of the spacecraft in observer body-fixed
     * coordinates corrected for light time velocity: The velocity of the
     * spacecraft in observer body-fixed coordinates corrected for light time
     */
    void getSpacecraftState(double et, String spacecraft, String body,
            String bodyFrame, double bodyToSc[], double velocity[]) throws SpiceErrorException
    {
        double[] lt = new double[6];
        double[] scToBodyState = new double[6];
        String abcorr = "LT+S";

        /*
         * Compute the apparent state of the body as seen from the spacecraft at
         * the epoch of observation, in the body-fixed frame, corrected for
         * stellar aberration and light time. Note that the time entered is the
         * time at the spacecraft, who is the observer.
         */
        CSPICE.spkezr(body, et, bodyFrame, abcorr, spacecraft,
                scToBodyState, lt);

        /*
         * The state of the spacecraft (apparent position and velocity) relative
         * to the body is just the negative of this state. Note that this is not
         * the same as the apparent position and velocity of the spacecraft as
         * seen from the body at time et, because et is the time at the
         * spacecraft not the body.
         */
        bodyToSc[0] = -scToBodyState[0];
        bodyToSc[1] = -scToBodyState[1];
        bodyToSc[2] = -scToBodyState[2];
        velocity[0] = -scToBodyState[3];
        velocity[1] = -scToBodyState[4];
        velocity[2] = -scToBodyState[5];
    }

    /*
     * This function computes the position of the target in the observer body
     * frame, at the time the spacecraft observed the body.
     *
     * Input: et: Ephemeris time when an image of the body was taken spacecraft:
     * Name of the spacecraft that took the image body: Name of observer
     * body (e.g. EROS, PLUTO, PHOEBE) targetBody: Name of target body (e.g.
     * SUN, EARTH). SPICE names can be found at
     * https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/FORTRAN/req/naif_ids.html
     *
     * Output: bodyToSc: The position of the target in observer body-fixed
     * coordinates corrected for light time velocity: The velocity of the target
     * in observer body-fixed coordinates corrected for light time
     */
    void getTargetState(double et, String spacecraft, String body,
            String bodyFrame, String targetBody, double bodyToTarget[], double velocity[]) throws SpiceErrorException
    {
        double[] lt = new double[6];
        double[] notUsed = new double[6];
        double[] bodyToTargetState = new double[6];
        String abcorr = "LT+S";

        /*
         * Compute the apparent state of the center of the observer body as seen
         * from the spacecraft at the epoch of observation (et), and the one-way
         * light time from the observer to the spacecraft. Only the returned
         * light time will be used from this call, as such, the reference frame
         * does not matter here. Use the body fixed frame.
         */
        CSPICE.spkezr(body, et, bodyFrame, abcorr, spacecraft, notUsed,
                lt);

        /*
         * Back up the time at the observer body by the light time to the
         * spacecraft. This is the time that light from the target body was
         * received at the observer body when the spacecraft took the image. It
         * is the time at the observer body. Now simply get the position of the
         * target at this time, as seen from the observer body, in the observer
         * body frame.
         */
        CSPICE.spkezr(targetBody, et - lt[0], bodyFrame, abcorr, body,
                bodyToTargetState, lt);

        /*
         * Assign the output variables.
         */
        bodyToTarget[0] = bodyToTargetState[0];
        bodyToTarget[1] = bodyToTargetState[1];
        bodyToTarget[2] = bodyToTargetState[2];
        velocity[0] = bodyToTargetState[3];
        velocity[1] = bodyToTargetState[4];
        velocity[2] = bodyToTargetState[5];
    }

    public static void printUsage()
    {
        String o = "\nThis program generates infofiles (SBMT-formatted SPICE pointing) for the input\n"
                + "data files. It also creates an imagelist.txt file for SBMT's FixedListQuery, and\n"
                + "an imagelist-fullpath.txt file for SBMT's DatabaseGeneratorSQL.RunInfo for FITS data.\n\n"
                + "Usage: CreateInfoFiles.sh <required-option> <input-folder> <output-folder> <metakernel> <sbmt-folder> <sc-name> <instrument-frame> <body-name> <body-frame>\n\n"
                + "Where:\n"
                + "  <input-folder>            Path to the data for which to create infofiles.\n"
                + "  <output-folder>           Path to folder in which to place generated infofiles and\n"
                + "                            image lists.\n"
                + "  <sbmt-folder>             Path relative to /project/sbmt2/data. This is the path where\n"
                + "                            the data will reside on the sbmt server for this instrument.\n"
                + "                            For example, \"/earth/polycam/images\". This is used only to\n"
                + "                            create the image lists.\n"
                + "  <sc-name>                 SPICE name for the spacecraft, found in the frames kernel. This\n"
                + "                            is not the same as the name of the spacecraft frame. A list of\n"
                + "                            valid spacecraft names can be found in the NAIF IDs required reading.\n"
                + "  <instrument-frame>        SPICE name for the reference frame of the observing instrument."
                + "                            This can be found in the frames kernel.\n"
                + "  <body-name>               SPICE name of the body. A list of valid body names can be found\n"
                + "                            in the NAIF IDs required reading.\n"
                + "  <body-frame>              SPICE name of the body-fixed reference frame. This is generally\n"
                + "                            found in the frames kernel or the PCK kernel.\n"
                + "One of the following two options is required:\n"
                + "  <-f sclkKeyword>          The input data is in FITS format and the FITS header\n"
                + "                            contains a SPICE SCLK (spacecraft clock) keyword whose\n"
                + "                            value is the exposure time. This keyword is passed on the\n"
                + "                            command line. In this case, all FITS files in the input\n"
                + "                            folder will be processed.\n"
                + "  <-t timesFile timeFormat> Data files and exposure times are listed in input timesFile.\n"
                + "                            The first column contains the data file name and the second\n"
                + "                            column contains the exposure time in one of three valid time\n"
                + "                            formats: utc, et, sclk. Lines beginning with \"#\" indicate a\n"
                + "                            comment and are not read. For example:\n\n"
                + "                                #This is a sample timesFile.\n"
                + "                                20170922T231949S789_sample_image.dat 2017-09-22T23:19:49.789\n"
                + "                                20170923T001041S345_sample_image.dat 2017-09-23T00:10:41.345\n"
                + "                                ...\n\n"
                + "                            The valid time formats are\n"
                + "                                utc  - a SPICE recognized UTC time string\n"
                + "                                et   - a double-precision ephemeris time\n"
                + "                                sclk - a SPICE-recognized mission-dependent spacecraft clock string\n"
                + "                            In the example above, the time format is utc.\n"
                + "                            For more information, refer to NAIF's SPICE time documentation.\n"
                + "Examples: \n"
                + "CreateInfoFiles.sh -f SCLK_STR               /project/sbmtpipeline/rawdata/test/images /project/sbmtpipeline/rawdata/test/infofiles /project/sbmtpipeline/rawdata/test/kernels/spoc-digest-2017-10-05T22_05_46.366Z.mk /project/sbmt2/data ORX ORX_OCAMS_POLYCAM BENNU IAU_BENNU\n"
                + "CreateInfoFiles.sh -t imageTimesFile.txt utc /project/sbmtpipeline/rawdata/test/images /project/sbmtpipeline/rawdata/test/infofiles /project/sbmtpipeline/rawdata/test/kernels/spoc-digest-2017-10-05T22_05_46.366Z.mk /project/sbmt2/data ORX ORX_OTES          BENNU IAU_BENNU\n\n";
        System.out.println(o);
    }

    public static void execute(String[] args) throws Exception
    {
        File inputFolder;
        File outputFolder;
        boolean fits = false;
        boolean timesFile = false;
        String sclkString = null;
        File timeTableFile = null;
        TimeFormat timeFormat = null;
        File metakernel = null;
        String sbmtRelPath = null;
        String sc = null;
        String instrFrame = null;
        String body = null;
        String bodyFrame = null;

        int i;
        for (i = 0; i < args.length; ++i)
        {
            if (args[i].equals("-f"))
            {
                fits = true;
            }
            else if (args[i].equals("-t"))
            {
                timesFile = true;
            }
            else
            {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options.
        // Otherwise abort.
        int numberRequiredArgs = 9;
        if (fits)
        {
            numberRequiredArgs = 9;
        }
        else if (timesFile)
        {
            numberRequiredArgs = 10;
        }
        else
        {
            System.out.println("Error: Required option is missing.");
            printUsage();
            System.exit(1);
        }
        if (args.length - i != numberRequiredArgs)
        {
            String argStr = new String();
            for (String s : args)
            {
                argStr = argStr + " " + s;
            }
            printUsage();
            System.exit(1);
        }

        //Parse the arguments
        if (fits)
        {
            sclkString = String.valueOf(args[i++]);
        }
        else if (timesFile)
        {
            timeTableFile = new File(args[i++]);
            timeFormat = TimeFormat.valueOf(args[i++]);
        }
        else
        {
            printUsage();
            System.exit(1);
        }
        inputFolder = new File(args[i++]);
        outputFolder = new File(args[i++]);
        metakernel = new File(args[i++]);
        sbmtRelPath = String.valueOf(args[i++]);
        sc = String.valueOf(args[i++]);
        instrFrame = String.valueOf(args[i++]);
        body = String.valueOf(args[i++]);
        bodyFrame = String.valueOf(args[i++]);

        //Check folder paths
        if (!inputFolder.exists())
        {
            System.out.println("Error: Input data path not found, " + inputFolder.getAbsolutePath());
            printUsage();
            System.exit(1);
        }
        if (!outputFolder.exists())
        {
            outputFolder.mkdirs();
        }

        System.err.println("Debug. Arguments:");
        if (timesFile)
        {
            System.err.println("    " + timeTableFile.getAbsolutePath() + " " + timeFormat + " " + inputFolder.getAbsolutePath() + " " + outputFolder.getAbsolutePath());
            System.err.println("    " + metakernel + " "  + sbmtRelPath + " " + sc + " " + instrFrame + " " + body + " " + bodyFrame);
        }
        if (fits)
        {
            System.err.println("    " + sclkString + " " + inputFolder.getAbsolutePath() + " " + outputFolder.getAbsolutePath());
            System.err.println("    " + metakernel + " "  + sbmtRelPath + " " + sc + " " + instrFrame + " " + body + " " + bodyFrame);
        }

        CreateInfoFiles app = new CreateInfoFiles(metakernel);
        if (fits)
        {
            app.processFits(metakernel, sbmtRelPath, inputFolder, outputFolder, body, bodyFrame, sc, instrFrame, sclkString);
        }
        else if (timesFile)
        {
            app.processImageTable(metakernel, sbmtRelPath, inputFolder, outputFolder, body, bodyFrame, sc, instrFrame, timeTableFile, timeFormat);
        }
    }

    public static void main(String[] args) throws Exception
    {
        //Input files. Pulled from the sbmt2 data server and sbmtpipeline rawdata.
        File mk = new File("C:/Users/nguyel1/Projects/SBMT/data/createInfoFiles/orxEarth/kernels/spoc-digest-2017-10-28T13_15_48.608Z.mk");
        File imageDir = new File("C:/Users/nguyel1/Projects/SBMT/data/createInfoFiles/orxEarth/polycam/images");
        File timeTable = new File("C:/Users/nguyel1/Projects/SBMT/data/createInfoFiles/orxEarth/polycam/imagelist.txt");
        //Output folder
        File infofileDir = new File("C:/Users/nguyel1/Projects/SBMT/data/createInfoFiles/orxEarth/polycam/infofiles");

        //Uncomment this to do a test run with FITS data.
        String[] testArgs = {"-f", "SCLK_STR", imageDir.getAbsolutePath(), infofileDir.getAbsolutePath(), mk.getAbsolutePath(), "/project/sbmt2/data", "ORX", "ORX_OCAMS_POLYCAM", "BENNU", "IAU_BENNU"};

        //Uncomment this to do a test run on generic data. Requires an input file containing data file name and exposure time. See printUsage() for details.
//        String[] testArgs = {"-t", timeTable.getAbsolutePath(), "utc", imageDir.getAbsolutePath(), infofileDir.getAbsolutePath(), mk.getAbsolutePath(), "/project/sbmt2/data", "ORX", "ORX_OCAMS_POLYCAM", "BENNU", "IAU_BENNU"};

        CreateInfoFiles.execute(testArgs);
    }
}
