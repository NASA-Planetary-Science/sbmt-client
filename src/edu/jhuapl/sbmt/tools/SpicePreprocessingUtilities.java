package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import edu.jhuapl.sbmt.model.time.FileUtils;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;
import spice.basic.SpiceException;

/**
 * Utility class containing SPICE data preprocessing routines for SBMT.
 * Primary utilities are createInfoFiles and createTimeHistory.
 *
 * Converted into Java from the C++ routines in misc/programs.
 *
 * @author nguyel1
 *
 */
public class SpicePreprocessingUtilities
{
    public static final int MAXBND = 4;
    public static final String TIMFMT = "YYYY-MON-DD HR:MN:SC.###::UTC (UTC)";




//    1. kernelfiles - a SPICE meta-kernel file containing the paths to the kernel files
//    2. body - IAU name of the target body, all caps
//    3. sc - SPICE spacecraft name
//    4. instrframe - SPICE instrument frame name
//    5. fitstimekeyword - FITS header time keyword, UTC assumed
//    6. input file list - path to file in which all image files are listed
//    7. output folder - path to folder where infofiles should be saved to
//    8. output file list - path to file in which all files for which an infofile was
//       created will be listed along with their start times.
//          cerr << "Usage: create_info_files <kernelfiles> <body> <sc> <instrframe> <fitstimekeyword> <inputfilelist> <infofilefolder> <outputfilelist>" << endl;

    /**
     * Reads all .fits files in imageDir and creates an infofile for them using
     * the SPICE metakernel specified. Outputs the infofiles and an imagelist to
     * be used in the SBMT RunInfo enum in DatabaseGeneratorSQL. The imagelist
     * contains the sbmt-relative path for all of the images processed.
     *
     * @param metakernel
     * @param sbmtDir - path relative to /project/sbmt2/data where the images will reside on the sbmt server for this instrument.
     *                  e.g. "/earth/polycam/images"
     *                  this is used only to create an imagelist, it is not necessarily the path to the raw data.
     * @param dataDir - path to the data for which info files will be created
     * @param extensions - comma-separated list of file extensions for this data, for example "fit,fits,FIT,FITS"
     * @param observerBody - spice name of body
     * @param bodyFrame - spice name of body frame
     * @param spacecraftName - spice name of spacecraft
     * @param instrFrame - spice name of instrument
     * @throws SpiceErrorException
     * @throws IOException
     */
    public void createInfoFiles(File metakernel, final String sbmtDir, File imageDir, final String extensions, String observerBody, String bodyFrame, final String spacecraftName, String instrFrame, final String sclkKeyword ) throws SpiceErrorException, IOException
    {
//        if (argc < 9)
//        {
//            cerr << "Usage: create_info_files <kernelfiles> <body> <sc> <instrframe> <fitstimekeyword> <inputfilelist> <infofilefolder> <outputfilelist>" << endl;
//            return 1;
//        }
//
//        String kernelfiles = argv[1];
//        String body = argv[2];
//        String scid = argv[3];
//        String instr = argv[4];
//        String sclkkey = argv[5];
//        String inputfilelist = argv[6];
//        String infofilefolder = argv[7];
//        String outputfilelist = argv[8];


        CSPICE.furnsh(metakernel.getAbsolutePath());

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
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }

        };
        Files.walkFileTree(Paths.get(imageDir.getAbsolutePath()), fitsFileVisitor);



//        for (unsigned int i=0; i<fitfiles.size(); ++i)
//        {
//            reset_c();
//
//            String utc;
//            double et;
//            double[] scposb = new double[3];
//            SpiceDouble unused = new double[3];
//            double boredir = new double[3];
//            double updir = new double[3];
//            double frustum = new double[12];
//            double sunPosition = new double[3];
//
//            getEt(fitfiles[i], sclkkey, utc, et, scid);
//            if (failed_c())
//                continue;
//
//            getSpacecraftState(et, scid, body, scposb, unused);
//            getTargetState(et, scid, body, "SUN", sunPosition, unused);
//            getFov(et, scid, body, instr, boredir, updir, frustum);
//            spiceProc.getSpacecraftState(et, "ORX", "BENNU", "IAU_BENNU", scposb, unused);
//            spiceProc.getTargetState(    et, "ORX", "BENNU", "IAU_BENNU", "SUN", sunPosition, unused);
//            spiceProc.getFov(            et, "ORX", "BENNU", "IAU_BENNU", "ORX_OCAMS_MAPCAM", boredir, updir, frustum);
//            if (failed_c())
//                continue; //cout?
//
//            const size_t last_slash_idx = fitfiles[i].find_last_of("\\/");
//            if (std::String::npos != last_slash_idx)
//            {
//                fitfiles[i].erase(0, last_slash_idx + 1);
//            }
//            int length = fitfiles[i].size();
//            String infofilename = infofilefolder + "/"
//                    + fitfiles[i].substr(0, length-4) + ".INFO";
//            saveInfoFile(infofilename, utc, scposb, boredir, updir, frustum, sunPosition);
//            cout << "created " << infofilename << endl;
//
//            fout << fitfiles[i].substr(0, length) << " " << utc << endl;
//
//        }
//        cout << "done." << endl;
//
//        return 0;

    }

    //TBD: this is going to be a standalone utility to create an input file to be
    //ingested by createInfoFiles. It'll look like this:
    //
    //<fullpath to rawdata image>/20170922T231949S789_pol_iofL2pan_V008.fit 2017-09-22T23:19:49.789
    //<fullpath to rawdata image>/20170923T001041S345_pol_iofL2pan_V008.fit 2017-09-23T00:10:41.345
    //...
    //
    public File makeTimeTableForCreateInfoFiles()
    {
        //TBD move the SimpleFileVisitor code in here
        return null;
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

//    private Vector<File> createImageList(File imageFolder)
//    {
//        File imageList = new File();
//    }
//
//    vector<String> loadFileList(const String& filelist) {
//        ifstream fin(filelist.c_str());
//
//        vector < String > files;
//
//        if (fin.is_open()) {
//            String line;
//            while (getline(fin, line))
//                files.push_back(line);
//        } else {
//            cerr << "Error: Unable to open file '" << filelist << "'" << endl;
//            exit(1);
//        }
//
//        return files;
//    }
//
//    void splitFitsHeaderLineIntoKeyAndValue(const String& line, String& key,
//            String& value) {
//        key = line.substr(0, 8);
//        trim(key);
//        value = line.substr(10);
//        size_t found = value.find_last_of("/");
//        if (found != String::npos)
//            value = value.substr(0, found);
//        trim(value);
//        removeSurroundingQuotes(value);
//        trim(value);
//    }
//
//    void getFieldsFromFitsHeader(const String& labelfilename, String& startmet,
//            String& stopmet, String& target, int& naxis1, int& naxis2)
//    {
//        ifstream fin(labelfilename.c_str());
//
//        if (fin.is_open()) {
//            char buffer[81];
//            String str;
//            String key;
//            String value;
//
//            for (int i = 0; i < 100; ++i) {
//                fin.read(buffer, 80);
//                buffer[80] = '\0';
//                str = buffer;
//                splitFitsHeaderLineIntoKeyAndValue(str, key, value);
//
//                if (key == "NAXIS1") {
//                    naxis1 = atoi(value.c_str());
//                } else if (key == "NAXIS2") {
//                    naxis2 = atoi(value.c_str());
//                } else if (key == "SPCSCLK") {
//                    startmet = value;
//                    stopmet = value;
//                } else if (key == "TARGET") {
//                    target = value;
//                }
//            }
//        } else {
//            cerr << "Error: Unable to open file '" << labelfilename << "'" << endl;
//            exit(1);
//        }
//
//        fin.close();
//    }
//
//    void getStringFieldFromFitsHeader(const String& fitfile,
//               String key,
//               String& value )
//    {
//        ifstream fin(fitfile.c_str());
//
//        if (fin.is_open())
//        {
//            String str;
//            char buffer[81];
//            String currkey;
//            String val;
//            while(true)
//            {
//                fin.read(buffer, 80);
//                buffer[80] = '\0';
//                str = buffer;
//                splitFitsHeaderLineIntoKeyAndValue(str, currkey, val);
//                if (currkey == key)
//                {
//                    value = val.c_str();
//                    break;
//                }
//            }
//        }
//        else
//        {
//            cerr << "Error: Unable to open file '" << fitfile << "'" << endl;
//            exit(1);
//        }
//
//        fin.close();
//    }
//
//    void saveInfoFile(String filename,
//                      String utc,
//                      const double scposb[3],
//                      const double boredir[3],
//                      const double updir[3],
//                      const double frustum[12],
//                      const double sunpos[3])
//    {
//        ofstream fout(filename.c_str());
//
//        if (!fout.is_open())
//        {
//            cerr << "Error: Unable to open file " << filename << " for writing" << endl;
//            exit(1);
//        }
//
//        fout.precision(16);
//
//        fout << "START_TIME          = " << utc << "\n";
//        fout << "STOP_TIME           = " << utc << "\n";
//
//        fout << "SPACECRAFT_POSITION = ( ";
//        fout << scientific << scposb[0] << " , ";
//        fout << scientific << scposb[1] << " , ";
//        fout << scientific << scposb[2] << " )\n";
//
//        fout << "BORESIGHT_DIRECTION = ( ";
//        fout << scientific << boredir[0] << " , ";
//        fout << scientific << boredir[1] << " , ";
//        fout << scientific << boredir[2] << " )\n";
//
//        fout << "UP_DIRECTION        = ( ";
//        fout << scientific << updir[0] << " , ";
//        fout << scientific << updir[1] << " , ";
//        fout << scientific << updir[2] << " )\n";
//
//        fout << "FRUSTUM1            = ( ";
//        fout << scientific << frustum[0] << " , ";
//        fout << scientific << frustum[1] << " , ";
//        fout << scientific << frustum[2] << " )\n";
//
//        fout << "FRUSTUM2            = ( ";
//        fout << scientific << frustum[3] << " , ";
//        fout << scientific << frustum[4] << " , ";
//        fout << scientific << frustum[5] << " )\n";
//
//        fout << "FRUSTUM3            = ( ";
//        fout << scientific << frustum[6] << " , ";
//        fout << scientific << frustum[7] << " , ";
//        fout << scientific << frustum[8] << " )\n";
//
//        fout << "FRUSTUM4            = ( ";
//        fout << scientific << frustum[9] << " , ";
//        fout << scientific << frustum[10] << " , ";
//        fout << scientific << frustum[11] << " )\n";
//
//        fout << "SUN_POSITION_LT     = ( ";
//        fout << scientific << sunpos[0] << " , ";
//        fout << scientific << sunpos[1] << " , ";
//        fout << scientific << sunpos[2] << " )\n";
//    }

    /*
     * This function computes the instrument boresight and frustum vectors in
     * the observer body frame at the time the spacecraft imaged the body.
     *
     * Input: et: Ephemeris time when an image of the body was taken
     * observerBody: Name of observer body (e.g. EROS, PLUTO, PHOEBE) bodyFrame:
     * Name of observer body-fixed frame (e.g. IAU_EROS, IAU_PLUTO, RYUGU_FIXED)
     * spacecraft: Name of the spacecraft that took the image instrFrame: SPICE
     * frame ID of instrument on the observing spacecraft
     *
     * Output: boredir: Boresight direction in bodyframe coordinates updir:
     * frustum: Field of view boundary corner vectors in bodyframe coordinates
     *
     */
    public void getFov(double et, String spacecraft, String observerBody,
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
        double[] bounds = new double[MAXBND * 3];
        double[][] boundssbmt = new double[MAXBND][3];
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
        CSPICE.spkpos(observerBody, et, inertframe, abcorr, spacecraft, notUsed,
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
        boredir = CSPICE.mxv(inst2bf, bsight);

        // transform boundary corners into body frame and pack into frustum
        // array.
        int k = 0;
        for (int i = 0; i < MAXBND; i++)
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
        updir = CSPICE.mxv(inst2bf, tmpvec);
    }

    /*
     * This function computes the state (position and velocity) of the
     * spacecraft in the observer body frame, at the time the spacecraft imaged
     * the body.
     *
     * Input: et: Ephemeris time when the image was taken observerBody: Name of
     * observer body (e.g. EROS, PLUTO) spacecraft: NAIF SPICE name of
     * spacecraft (e.g. NEAR, NH). These can be found at
     * https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/FORTRAN/req/naif_ids.html
     *
     * Output: bodyToSc: The position of the spacecraft in observer body-fixed
     * coordinates corrected for light time velocity: The velocity of the
     * spacecraft in observer body-fixed coordinates corrected for light time
     */
    void getSpacecraftState(double et, String spacecraft, String observerBody,
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
        CSPICE.spkezr(observerBody, et, bodyFrame, abcorr, spacecraft,
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
     * Name of the spacecraft that took the image observerBody: Name of observer
     * body (e.g. EROS, PLUTO, PHOEBE) targetBody: Name of target body (e.g.
     * SUN, EARTH). SPICE names can be found at
     * https://naif.jpl.nasa.gov/pub/naif/toolkit_docs/FORTRAN/req/naif_ids.html
     *
     * Output: bodyToSc: The position of the target in observer body-fixed
     * coordinates corrected for light time velocity: The velocity of the target
     * in observer body-fixed coordinates corrected for light time
     */
    void getTargetState(double et, String spacecraft, String observerBody,
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
        CSPICE.spkezr(observerBody, et, bodyFrame, abcorr, spacecraft, notUsed,
                lt);

        /*
         * Back up the time at the observer body by the light time to the
         * spacecraft. This is the time that light from the target body was
         * received at the observer body when the spacecraft took the image. It
         * is the time at the observer body. Now simply get the position of the
         * target at this time, as seen from the observer body, in the observer
         * body frame.
         */
        CSPICE.spkezr(targetBody, et - lt[0], bodyFrame, abcorr, observerBody,
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

    public static void main(String[] args) throws SpiceException, IOException
    {
        System.loadLibrary("JNISpice");
        File mk = new File("C:/Users/nguyel1/Projects/SBMT/data/createInfoFiles/orxEarth/kernels/spoc-digest-2017-10-05T22_05_46.366Z.mk");
        File imageDir = new File("C:/Users/nguyel1/Projects/SBMT/data/createInfoFiles/orxEarth/polycam/images");

        CSPICE.furnsh(mk.getAbsolutePath());

        SpicePreprocessingUtilities spiceProc = new SpicePreprocessingUtilities();
        //Outputs:
        double[] updir = new double[3];
        double[] boredir = new double[3];
        double[] sunPosition = new double[3];
        double[] scPosition = new double[3];
        double[] unused = new double[3];
        double[] frustum = new double[3 * MAXBND];
        //Inputs:
        double et = CSPICE.str2et("2017 SEP 22 23:00:00.000");

        spiceProc.getFov(            et, "ORX", "BENNU", "IAU_BENNU", "ORX_OCAMS_MAPCAM", boredir, updir, frustum);
        spiceProc.getTargetState(    et, "ORX", "BENNU", "IAU_BENNU", "SUN", sunPosition, unused);
        spiceProc.getSpacecraftState(et, "ORX", "BENNU", "IAU_BENNU", scPosition, unused);

        spiceProc.createInfoFiles(mk, "/earth/polycam/images", imageDir, "fit,fits,FIT,FITS", "BENNU", "IAU_BENNU", "ORX", "ORX_OCAMS_POLYCAM", "SCLK_STR");

    }
}
