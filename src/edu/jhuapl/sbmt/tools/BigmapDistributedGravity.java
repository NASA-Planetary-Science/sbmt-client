package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCard;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import altwg.Fits.HeaderTags;
import altwg.tools.ToolsVersion;
import altwg.util.AltwgFits;
import altwg.util.AltwgProductType;
import altwg.util.BatchType;
import altwg.util.FileUtil;
import altwg.util.FitsUtil;
import altwg.util.MathUtil;
import altwg.util.NativeLibraryLoader;
import altwg.util.PolyDataUtil;
import altwg.util.PolyDataUtil.CellInfo;
import altwg.util.PolyDataUtil2;
import altwg.util.SmallBodyModel;
import altwg.util.SrcProductType;
import altwg.util.Tilt;

import vtk.vtkIdList;
import vtk.vtkOctreePointLocator;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

import edu.jhuapl.sbmt.util.DistributedGravityBatchSubmission;

/**
 * DistributedGravity program. See the usage string for more information about this program.
 *
 * @author Eli Kahn
 * @version 1.0
 *
 * Note: This is a modified version of DistributedGravity.java from projects/osirisrex/ola/altwg/trunk/java-tools
 *       svn revision 59134, changed to make Bigmap work with the SBMT
 *
 */
public class BigmapDistributedGravity {

    public static enum GravityAlgorithmType {
        WERNER, CHENG
    };

    public static enum HowToEvaluate {
        EVALUATE_AT_CENTERS, EVALUATE_AT_POINTS_IN_FITS_FILE
    };

    public static class GravityValues {
        public double[] acc = new double[3];
        public double potential;
    }


    private static vtkPolyData globalShapeModelPolyData;
    private static double density;
    private static double rotationRate;
    private static GravityAlgorithmType gravityType;
    private static HowToEvaluate howToEvalute;
    private static String fieldpointsfile;
    private static double refPotential;
    private static boolean refPotentialProvided;
    private static boolean minRefPotential;
    private static boolean saveRefPotential;
    private static String refPotentialFile;
    private static int numCores;
    private static String objfile;
    private static String outfile;
    private static String rootDir;
    private static String gravityExecutableName;
    private static String outputFolder;
    private static BatchType batchType;
    private static double tiltRadius;

    // From
    // https://stackoverflow.com/questions/523871/best-way-to-concatenate-list-of-string-objects
    private static String concatStringsWSep(List<Double> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Double s : strings) {
            if (s == null)
                sb.append(sep).append("NA");
            else
                sb.append(sep).append(String.format("% 30.16f", s));
            sep = separator;
        }
        return sb.toString();
    }

    private static String padString(String str, int maxLength) {
        while (str.length() < maxLength) {
            str += " ";
        }
        return str;
    }

    private static void saveResultsAtCenters(String gravityfile, vtkPolyData polydata, List<GravityValues> results)
            throws IOException, FitsException {
        FileWriter ofs = new FileWriter(gravityfile);
        BufferedWriter out = new BufferedWriter(ofs);

        String columnDescriptions = String.format(
                "%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s,%21s",
                "X (km)", "Y (km)", "Z (km)", "Latitude (deg)", "Longitude (deg)", "Radius (km)",
                AltwgProductType.NORMAL_VECTOR_X.getHeaderValueWithUnits(),
                AltwgProductType.NORMAL_VECTOR_Y.getHeaderValueWithUnits(),
                AltwgProductType.NORMAL_VECTOR_Z.getHeaderValueWithUnits(), "Area (km^2)",
                AltwgProductType.GRAVITY_VECTOR_X.getHeaderValueWithUnits(),
                AltwgProductType.GRAVITY_VECTOR_Y.getHeaderValueWithUnits(),
                AltwgProductType.GRAVITY_VECTOR_Z.getHeaderValueWithUnits(),
                AltwgProductType.GRAVITATIONAL_MAGNITUDE.getHeaderValueWithUnits(),
                AltwgProductType.GRAVITATIONAL_POTENTIAL.getHeaderValueWithUnits(),
                AltwgProductType.ELEVATION.getHeaderValueWithUnits(),
                AltwgProductType.SLOPE.getHeaderValueWithUnits(),
                AltwgProductType.TILT.getHeaderValueWithUnits(),
                AltwgProductType.MEAN_TILT.getHeaderValueWithUnits(),
                AltwgProductType.STDEV_TILT.getHeaderValueWithUnits());
        out.write(padString(String.format("%-26s %-21s", "Target", "Unknown"), columnDescriptions.length()) + "\r\n");
        out.write(padString(String.format("%-26s %-21s", "Density (kg/m^3)", String.valueOf(1000.0 * density)),
                columnDescriptions.length()) + "\r\n");
        out.write(padString(String.format("%-26s %-21s", "Rotation Rate (rad/sec)", String.valueOf(rotationRate)),
                columnDescriptions.length()) + "\r\n");
        out.write(padString(String.format("%-26s %-21s", "Reference Potential (J/kg)", String.valueOf(refPotential)),
                columnDescriptions.length()) + "\r\n");
        out.write(padString(String.format("%-26s %-21s", "Tilt Radius (meters)", String.valueOf(1000.0 * tiltRadius)),
                columnDescriptions.length()) + "\r\n");
        out.write(columnDescriptions + "\r\n");

        vtkIdList idList = new vtkIdList();
        vtkPolyData polyDataCenters = PolyDataUtil2.getPlateCenters(polydata);
        vtkOctreePointLocator pointLocator = new vtkOctreePointLocator();
        pointLocator.FreeSearchStructure();
        pointLocator.SetDataSet(polyDataCenters);
        pointLocator.BuildLocator();

        int numCells = polydata.GetNumberOfCells();

        Tilt tilt = new Tilt(tiltRadius);

        for (int i = 0; i < numCells; ++i) {
            CellInfo ci = PolyDataUtil.getCellInfo(polydata, i, idList);
            List<Double> row = new ArrayList<Double>();
            row.add(ci.center[0]);
            row.add(ci.center[1]);
            row.add(ci.center[2]);
            row.add(ci.latitude);
            row.add(ci.longitude);
            row.add(ci.radius);
            row.add(ci.normal[0]);
            row.add(ci.normal[1]);
            row.add(ci.normal[2]);
            row.add(ci.area);
            row.add(results.get(i).acc[0]);
            row.add(results.get(i).acc[1]);
            row.add(results.get(i).acc[2]);
            double slope = getSlope(results.get(i).acc, ci.normal);
            double elevation = getElevation(refPotential, results.get(i).acc, results.get(i).potential);
            double accMag = getAccelerationMagnitude(results.get(i).acc, slope);
            row.add(accMag);
            row.add(results.get(i).potential);
            row.add(elevation);
            row.add(slope);
            row.add(ci.tilt);
            DescriptiveStatistics tiltStats = tilt.getTiltStatistics(pointLocator, polyDataCenters, ci.center, ci.normal,
                    idList);
            row.add(tiltStats.getMean());
            row.add(tiltStats.getStandardDeviation());
            out.write(concatStringsWSep(row, ",") + "\r\n");
        }

        out.close();
    }

    /**
     * This method computes the normal vector and x,y,z vector in line with the reading of the
     * gravity values.OLD CODE - BACKUP
     *
     * @param altwgName
     * @param inputfitsfile
     * @param fitspolydata
     * @param outputfitsfile
     * @param results
     * @throws Exception
     */
//  private static void saveResultsAtPointsInFitsFile(boolean altwgName, String inputfitsfile, vtkPolyData fitspolydata,
//          String outputfitsfile, List<GravityValues> results) throws Exception {
//      FileReader ifs = new FileReader(fieldpointsfile);
//      BufferedReader in = new BufferedReader(ifs);
//
//      // Get the dimensions of the input fits file
//      int[] axes = new int[3];
//      double[][][] indata = FitsUtil.loadFits(inputfitsfile, axes);
//      int inputNumPlanes = axes[0];
//      double[][][] outdata = new double[inputNumPlanes + 15][axes[1]][axes[2]];
//      vtkIdList idList = new vtkIdList();
//      SmallBodyModel smallBodyModel = new SmallBodyModel(fitspolydata);
//
//      double[] pointOnPlane = new double[3];
//      Rotation rot = PolyDataUtil2.fitPlaneToPolyData(fitspolydata, pointOnPlane);
//      double[][] mat = rot.getMatrix();
//      double[] ux = { mat[0][0], mat[1][0], mat[2][0] };
//      double[] uz = { mat[0][2], mat[1][2], mat[2][2] };
//      // Put the sun pointing in direction vector that bisects ux and uz
//      double[] sun = { pointOnPlane[0] + ux[0] + uz[0], pointOnPlane[1] + ux[1] + uz[1],
//              pointOnPlane[2] + ux[2] + uz[2] };
//      MathUtil.vhat(sun, sun);
//      // Put the eye pointing in direction vector that bisects -ux and uz
//      double[] eye = { pointOnPlane[0] - ux[0] + uz[0], pointOnPlane[1] - ux[1] + uz[1],
//              pointOnPlane[2] - ux[2] + uz[2] };
//      MathUtil.vhat(eye, eye);
//
//      Tilt tiltClass = new Tilt(tiltRadius);
//
//      double[] pt = new double[3];
//      double[] normal = new double[3];
//      String line;
//      int i = 0;
//      while ((line = in.readLine()) != null) {
//          String[] tokens = line.trim().split("\\s+");
//          pt[0] = Double.parseDouble(tokens[0]);
//          pt[1] = Double.parseDouble(tokens[1]);
//          pt[2] = Double.parseDouble(tokens[2]);
//          normal[0] = Double.parseDouble(tokens[3]);
//          normal[1] = Double.parseDouble(tokens[4]);
//          normal[2] = Double.parseDouble(tokens[5]);
//          int m = Integer.parseInt(tokens[6]);
//          int n = Integer.parseInt(tokens[7]);
//          int k = 0;
//          for (; k < inputNumPlanes; ++k)
//              outdata[k][m][n] = indata[k][m][n];
//          outdata[k++][m][n] = normal[0];
//          outdata[k++][m][n] = normal[1];
//          outdata[k++][m][n] = normal[2];
//          outdata[k++][m][n] = results.get(i).acc[0];
//          outdata[k++][m][n] = results.get(i).acc[1];
//          outdata[k++][m][n] = results.get(i).acc[2];
//          double slope = getSlope(results.get(i).acc, normal);
//          double elevation = getElevation(refPotential, results.get(i).acc, results.get(i).potential);
//          double accMag = getAccelerationMagnitude(results.get(i).acc, slope);
//          outdata[k++][m][n] = accMag;
//          outdata[k++][m][n] = results.get(i).potential;
//          outdata[k++][m][n] = elevation;
//          outdata[k++][m][n] = slope;
//
//          //calculate angular separation between vector to point and vector normal. call this tilt.
//          double tilt = Tilt.basicTiltDeg(pt, normal);
//          outdata[k++][m][n] = tilt;
//
////            public static double basicTiltDir(double lonDeg, double[] vecPlt, double[] normal) {
//          CellInfo ci = PolyDataUtil.getCellInfo(fitspolydata, i, idList);
//
//          DescriptiveStatistics tiltStats = tiltClass.getTiltStatistics(smallBodyModel.getPointLocator(), fitspolydata, pt,
//                  normal, idList);
//          outdata[k++][m][n] = tiltStats.getMean();
//          outdata[k++][m][n] = tiltStats.getStandardDeviation();
//          outdata[k++][m][n] = PolyDataUtil2.getDistanceToPlane(pt, pointOnPlane, rot);
//          outdata[k++][m][n] = getShadedRelief(normal, eye, sun);
//          ++i;
//      }
//
//      in.close();
//
//      Map<String, HeaderCard> originalHeaderValues = FitsUtil.getFitsHeaderAsMap(inputfitsfile);
//
//      //see if original header values specifies the data source. Use this instead.
//      HeaderCard srcCard = FitsUtil.cardInMap(originalHeaderValues, HeaderTags.DATASRC.toString());
//      //default value;
//      SrcProductType dataSrc = SrcProductType.OLA;
//      if (srcCard != null) {
//          dataSrc = SrcProductType.getType(srcCard.getValue());
//          if (dataSrc == SrcProductType.UNKNOWN) {
//              System.out.println("Error: DATASRC value in inputfits file is:" + srcCard.getValue());
//              System.out.println("This does not correspond to an existing SrcProductType");
//              System.out.println("Will use UNKNOWN");
//          }
//          System.out.println("parsing data source from input fits file:" + dataSrc.toString() + ".");
//      }
//
//      AltwgProductType altwgProduct = AltwgProductType.DTM;
//      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//      Date date = new Date();
//      System.out.println(dateFormat.format(date) + ":saving local fits");
//
//      //save ALTWG keywords from previous Fits header and use them in output fits header.
//      boolean savePrevHeader = true;
//      AltwgFits.saveLocalFits(outdata, outfile, altwgProduct, dataSrc, inputfitsfile, null, null, null,
//              null, 0.0, true, density, rotationRate, refPotential, tiltRadius, savePrevHeader, altwgName, originalHeaderValues);
//  }


    /**
     * Save results in FITS file. Since this is also a standalone tool there is no option for modifying the
     * outputfitsfile to the ALTWG naming convention yet.
     *
     * @param altwgName
     * @param inputfitsfile
     * @param fitspolydata
     * @param outputfitsfile
     * @param gravAtLocations
     * @throws Exception
     */
    private static void saveResultsAtPointsInFitsFile(boolean altwgName, String inputfitsfile, vtkPolyData fitspolydata,
            String outputfitsfile, List<GravityValues> gravAtLocations) throws Exception {

        // Get the dimensions of the input fits file
        int[] axes = new int[3];
        double[][][] indata = FitsUtil.loadFits(inputfitsfile, axes);
        int inputNumPlanes = axes[0];
        double[][][] outdata = new double[inputNumPlanes + 15][axes[1]][axes[2]];
        vtkIdList idList = new vtkIdList();
        SmallBodyModel smallBodyModel = new SmallBodyModel(fitspolydata);

        double[] pointOnPlane = new double[3];
        Rotation rot = PolyDataUtil2.fitPlaneToPolyData(fitspolydata, pointOnPlane);
        double[][] mat = rot.getMatrix();
        double[] ux = { mat[0][0], mat[1][0], mat[2][0] };
        double[] uz = { mat[0][2], mat[1][2], mat[2][2] };
        // Put the sun pointing in direction vector that bisects ux and uz
        double[] sun = { pointOnPlane[0] + ux[0] + uz[0], pointOnPlane[1] + ux[1] + uz[1],
                pointOnPlane[2] + ux[2] + uz[2] };
        MathUtil.vhat(sun, sun);
        // Put the eye pointing in direction vector that bisects -ux and uz
        double[] eye = { pointOnPlane[0] - ux[0] + uz[0], pointOnPlane[1] - ux[1] + uz[1],
                pointOnPlane[2] - ux[2] + uz[2] };
        MathUtil.vhat(eye, eye);

        double[] pt = new double[3];
        double[] normal = new double[3];
        Tilt tiltClass = new Tilt(tiltRadius);
        String line;
        int i = 0;

        //read gravity values from fieldpoints file
        FileReader ifs = new FileReader(fieldpointsfile);
        BufferedReader in = new BufferedReader(ifs);
        while ((line = in.readLine()) != null) {

            String[] tokens = line.trim().split("\\s+");
            pt[0] = Double.parseDouble(tokens[0]);
            pt[1] = Double.parseDouble(tokens[1]);
            pt[2] = Double.parseDouble(tokens[2]);
            normal[0] = Double.parseDouble(tokens[3]);
            normal[1] = Double.parseDouble(tokens[4]);
            normal[2] = Double.parseDouble(tokens[5]);
            int m = Integer.parseInt(tokens[6]);
            int n = Integer.parseInt(tokens[7]);
            int k = 0;

            for (; k < inputNumPlanes; ++k)
                outdata[k][m][n] = indata[k][m][n];

            outdata[k++][m][n] = normal[0];
            outdata[k++][m][n] = normal[1];
            outdata[k++][m][n] = normal[2];
            outdata[k++][m][n] = gravAtLocations.get(i).acc[0];
            outdata[k++][m][n] = gravAtLocations.get(i).acc[1];
            outdata[k++][m][n] = gravAtLocations.get(i).acc[2];
            double slope = getSlope(gravAtLocations.get(i).acc, normal);
            double elevation = getElevation(refPotential, gravAtLocations.get(i).acc, gravAtLocations.get(i).potential);
            double accMag = getAccelerationMagnitude(gravAtLocations.get(i).acc, slope);
            outdata[k++][m][n] = accMag;
            outdata[k++][m][n] = gravAtLocations.get(i).potential;
            outdata[k++][m][n] = elevation;
            outdata[k++][m][n] = slope;

            //calculate vector between vector to point and normal. call this tilt
            double tilt = Tilt.basicTiltDeg(pt, normal);
            outdata[k++][m][n] = tilt;

            //calculate tilt direction. Note: assume longitude is 2nd plane in input fits file!
            //The following will fail if this is not true!
            double lon = indata[1][m][n];
            double tiltDir = Tilt.basicTiltDir(lon, pt, normal);

            //TODO need to add code to handle tiltDir extra plane

            // twupy1: Commented this out
            //calculate tilt statistics: mean and standard deviation.
            //StatisticalSummaryValues tiltStats = tiltClass.regionalTiltStats(smallBodyModel.getPointLocator(), fitspolydata, pt,
            //      normal, idList);

            // twupy1: Hardcoded zero values, this was giving us problems
            //outdata[k++][m][n] = tiltStats.getMean();
            //outdata[k++][m][n] = tiltStats.getStandardDeviation();
            outdata[k++][m][n] = 0;
            outdata[k++][m][n] = 0;

            outdata[k++][m][n] = PolyDataUtil2.getDistanceToPlane(pt, pointOnPlane, rot);
            outdata[k++][m][n] = getShadedRelief(normal, eye, sun);
            ++i;
        }

        in.close();

        Map<String, HeaderCard> originalHeaderValues = FitsUtil.getFitsHeaderAsMap(inputfitsfile);

        //see if original header values specifies the data source. Use this instead.
        HeaderCard srcCard = FitsUtil.cardInMap(originalHeaderValues, HeaderTags.DATASRC.toString());
        //default value;
        SrcProductType dataSrc = SrcProductType.OLA;

        if (srcCard != null) {
            dataSrc = SrcProductType.getType(srcCard.getValue());
            if (dataSrc == SrcProductType.UNKNOWN) {
                System.out.println("Error: DATASRC value in inputfits file is:" + srcCard.getValue());
                System.out.println("This does not correspond to an existing SrcProductType");
                System.out.println("Will use UNKNOWN");
            }
            System.out.println("parsing data source from input fits file:" + dataSrc.toString() + ".");
        }

        AltwgProductType altwgProduct = AltwgProductType.DTM;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + ":saving local fits");

        //save ALTWG keywords from previous Fits header and use them in output fits header.
        boolean savePrevHeader = true;
        AltwgFits.saveLocalFits(outdata, outfile, altwgProduct, dataSrc, inputfitsfile, null, null, null,
                null, 0.0, true, density, rotationRate, refPotential, tiltRadius, savePrevHeader, altwgName, originalHeaderValues);
    }

    private static List<GravityValues> readGravityResults(File accFile, File potFile) throws IOException {

        List<double[]> accelerationVector = new ArrayList<double[]>();
        List<Double> potential = new ArrayList<Double>();

        accelerationVector.addAll(FileUtil.loadPointArray(accFile.getAbsolutePath()));
        potential.addAll(FileUtil.getFileLinesAsDoubleList(potFile.getAbsolutePath()));

        List<GravityValues> results = new ArrayList<GravityValues>();

        int numLines = potential.size();
        for (int i = 0; i < numLines; ++i) {
            GravityValues r = new GravityValues();
            r.potential = potential.get(i);
            r.acc[0] = accelerationVector.get(i)[0];
            r.acc[1] = accelerationVector.get(i)[1];
            r.acc[2] = accelerationVector.get(i)[2];
            results.add(r);
        }

        return results;
    }

    private static List<GravityValues> getGravityAtLocations() throws InterruptedException, ExecutionException,
            IOException {

        List<String> commandList = new ArrayList<String>();

        long size = 0;
        String howToEvaluateSwitch = "";
        if (howToEvalute == HowToEvaluate.EVALUATE_AT_CENTERS) {
            howToEvaluateSwitch = "centers";
            size = globalShapeModelPolyData.GetNumberOfCells();
            System.out.println("preparing to evaluate gravity for " + String.valueOf(size) + " centers");
        } else if (howToEvalute == HowToEvaluate.EVALUATE_AT_POINTS_IN_FITS_FILE) {
            howToEvaluateSwitch = "file " + fieldpointsfile;
            size = FileUtil.getNumberOfLinesInfile(fieldpointsfile);
            System.out.println("preparing to evaluate gravity for " + String.valueOf(size) + " records"
                    + " in fits file");
        }

        String outfilename = new File(outfile).getName();

        // create the list of commands which we will submit to the batch queuing
        // system
        long chunk = size / numCores;
        for (int i = 0; i < numCores; i++) {
            final long startId = i * chunk;
            final long stopId = i < numCores - 1 ? (i + 1) * chunk : size;

            // Call the gravity executable
            String command = String
                    .format("%s -d %.16e -r %.16e --%s --%s --start-index %d --end-index %d --suffix %s%d --output-folder %s %s",
                            rootDir + File.separator + gravityExecutableName, density, rotationRate,
                            gravityType.name().toLowerCase(), howToEvaluateSwitch, startId,
                            stopId, outfilename, i, outputFolder, objfile);
            commandList.add(command);
        }

        // Submit the batches and wait till they're finished
        //if (numCores > 1)
        //  DistributedGravityBatchSubmission.runBatchSubmitPrograms(commandList, batchType);
        //else
        //DistributedGravityBatchSubmission.runBatchSubmitPrograms(commandList, rootDir, BatchType.LOCAL_SEQUENTIAL);
        DistributedGravityBatchSubmission.runBatchSubmitPrograms(commandList, rootDir, BatchType.LOCAL_PARALLEL);

        // Now read in all results
        List<GravityValues> results = new ArrayList<GravityValues>();
        for (int i = 0; i < numCores; i++) {
            String basename = new File(objfile).getName();
            File accFile = new File(outputFolder + File.separator + basename + "-acceleration.txt" + outfilename + i);
            File potFile = new File(outputFolder + File.separator + basename + "-potential.txt" + outfilename + i);
            results.addAll(readGravityResults(accFile, potFile));

            // we don't need these files so delete them
            accFile.delete();
            potFile.delete();
        }

        if (howToEvalute != HowToEvaluate.EVALUATE_AT_POINTS_IN_FITS_FILE) {
            refPotential = getRefPotential(results, minRefPotential);
            System.out.println("Reference Potential = " + refPotential);
            // save out reference potential to file so it can be loaded in again
            if (saveRefPotential)
                FileUtils.writeStringToFile(new File(refPotentialFile), String.valueOf(refPotential));
        }

        return results;
    }

    // This function reads the reference potential from a file. It is assumed
    // the reference potential is the only word in the file.
    private static double getRefPotential(String filename) throws IOException {
        List<String> words = FileUtil.getFileWordsAsStringList(filename);
        return Double.parseDouble(words.get(0));
    }

    private static double getRefPotential(List<GravityValues> results, boolean minRefPotential) {
        int numFaces = globalShapeModelPolyData.GetNumberOfCells();

        if (howToEvalute == HowToEvaluate.EVALUATE_AT_CENTERS && results.size() != numFaces) {
            System.err.println("Error: Size of array not equal to number of plates");
            System.exit(1);
        }

        vtkIdList idList = new vtkIdList();

        double[] pt1 = new double[3];
        double[] pt2 = new double[3];
        double[] pt3 = new double[3];
        double potTimesAreaSum = 0.0;
        double totalArea = 0.0;

        if (minRefPotential) {

            double minRefPot = Double.NaN;
            for (GravityValues thisGrav : results) {
                if ((Double.isNaN(minRefPot)) || (minRefPot > thisGrav.potential)) {
                    minRefPot = thisGrav.potential;
                }
            }

            //stop with error if for some reason this is still NaN
            if (Double.isNaN(minRefPot)) {
                System.out.println("ERROR! Could not find minimum reference potential in DistributedGravity.getRefPotential()!");
                System.out.println("STOPPING WITH ERROR!");
                System.exit(1);
            }
            return minRefPot;

        } else {
            for (int i = 0; i < numFaces; ++i) {
                PolyDataUtil.getCellPoints(globalShapeModelPolyData, i, idList, pt1, pt2, pt3);

                double potential = 0.0;
                if (howToEvalute == HowToEvaluate.EVALUATE_AT_CENTERS) {
                    potential = results.get(i).potential;
                }

                double area = MathUtil.triangleArea(pt1, pt2, pt3);

                potTimesAreaSum += potential * area;
                totalArea += area;
            }
            return potTimesAreaSum / totalArea;

        }

    }

    private static double getSlope(double[] acc, double[] normal) {
        double[] negativeAcc = new double[3];
        negativeAcc[0] = -acc[0];
        negativeAcc[1] = -acc[1];
        negativeAcc[2] = -acc[2];
        return Math.toDegrees(MathUtil.vsep(normal, negativeAcc));
    }

    private static double getElevation(double refPotential, double[] acc, double potential) {
        double accMag = MathUtil.vnorm(acc);
        return (potential - refPotential) / accMag;
    }

    private static double getAccelerationMagnitude(double[] acc, double slope) {
        double accMag = MathUtil.vnorm(acc);
        if (slope > 90.0)
            accMag = -Math.abs(accMag);
        else
            accMag = Math.abs(accMag);
        return accMag;
    }

    private static double getShadedRelief(double[] normal, double[] eye, double[] sun) {
        double emissionAngle = MathUtil.vsep(eye, normal);
        double incidenceAngle = MathUtil.vsep(sun, normal);
        double phaseAngle = MathUtil.vsep(eye, sun);

        double cose = Math.cos(emissionAngle);
        double cosi = Math.cos(incidenceAngle);

        double beta = Math.exp(-Math.toDegrees(phaseAngle) / 60.0);
        double result = (1.0 - beta) * cosi + beta * cosi / (cosi + cose);

        if (result > 1.0)
            result = 1.0;
        if (result < 0.0)
            result = 0.0;

        return result;
    }

    private static void usage() {
        // @formatter:off
        String o = ToolsVersion.getVersionString()
                + "\n\nThis program computes the gravitational acceleration and potential of a\n"
                + "shape model at specified points and saves the values to files. Unlike the\n"
                + "gravity program which is a single threaded program, this one is designed\n"
                + "run in distributed manner by dividing the computation into multiple jobs\n"
                + "and running them in parallel on one or more machines.\n\n"
                + "Usage: DistributedGravity [options] <platemodelfile> <out-file>\n\n"
                + "Where:\n"
                + "  <platemodelfile>         Path to global shape model file in OBJ format.\n"
                + "  <out-file>               Path to output file which will contain all results.\n\n"
                + "Options:\n"
                + "  -d <value>               Density of shape model in g/cm^3 (default is 1)\n"
                + "  -r <value>               Rotation rate of shape model in radians/sec (default is 0)\n"
                + "  --werner                 Use the Werner algorithm for computing the gravity (this is the\n"
                + "                           default if neither --werner or --cheng option provided)\n"
                + "  --cheng                  Use Andy Cheng's algorithm for computing the gravity (default is to\n"
                + "                           use Werner method if neither --werner or --cheng option provided)\n"
                + "  --centers                Evaluate gravity directly at the centers of plates of <platemodelfile>\n"
                + "                           (this is the default unless the --fits-local option is provided)\n"
                + "  --min-ref-potential      If provided the code will use the minimum reference potential as \n"
                + "                           the reference potential. By default the code calculates an average gravitational\n"
                + "                           potential as the reference potential. Note that the --fits-local option still\n"
                + "                           requires the user to specify --ref-potential as the code cannot calculate a reference\n"
                + "                           potential from the fits file.\n"
                + "  --fits-local <filename>  Evaluate gravity at points specified in a FITS file rather than\n"
                + "                           at the plate centers of <platemodelfile> (default is to evaluate at centers).\n"
                + "                           It is assumed the FITS file represents a local surface region\n"
                + "                           (e.g. a maplet or mapola) and contains at least 6 planes with the first 6\n"
                + "                           planes being lat, lon, rad, x, y, and z.\n"
                + "                           The output FITS file will try to follow the ALTWG fits header and file naming\n"
                + "                           convention. It will try to preserve the input fits header tags which follow the ALTWG\n"
                + "                           fits header convention, i.e. 'mphase', 'datasrc', 'datasrcv'.\n"
                + "  --ref-potential <value>  If the --fits-local option is provided, then you must use this option to specify the\n"
                + "                           reference potential (in J/kg) which is needed for calculating elevation. This option is\n"
                + "                           ignored if --fits-local is not provided. <value> can be either a number or a path to a\n"
                + "                           file containing the number (the number must be the only contents of the file)\n"
                + "  --save-ref-potential <path> Save the reference potential computed by this program to a file at <path>.\n"
                + "  --output-folder <folder> Path to folder in which to place output files. These are temporary files created by"
                + "                           gravity executable (default is current directory).\n"
                + "  --num-jobs <num>         Number of jobs to divide processing into. This should be equal to\n"
                + "                           number of cores across all machines. Default is 1.\n"
                + "  --tilt-radius <value>    Radius to use for computing mean and standard deviation of tilt in kilometers.\n"
                + "                           At each point the tilt of all plates within the specified radius is used to\n"
                + "                           compute the mean and standard deviation tilt. (Default is 0.05 kilometers).\n"
                + "                           The tilt radius should be no bigger than about 2 to 3 times the mean spacing of\n"
                + "                           the points where the gravity is being evaluated. A larger tilt radius will result\n"
                + "                           in very long running times. Note also that all plates within the radius are included\n"
                + "                           even if those plates are not connected via some path along the surface to the tilt\n"
                + "                           point. This would only happen with highly irregular geometry in which the surface\n"
                + "                           somehow almost folds over itself. Defaults to 0.05km\n"
                + "  --batch-type grid|local  Because the gravity program can take a very long time for large shape models,\n"
                + "                           this program supports 2 forms of parallelization, grid or local. In either case,\n"
                + "                           the program first divides the computation into chunks (as specified with the\n"
                + "                           --num-jobs option). If 'local' (the default) is specified, the program utilizes\n"
                + "                           all available processors on the local machine only to process the individual chunks.\n"
                + "                           If 'grid' option the program uses Grid Engine (http://gridscheduler.sourceforge.net/)\n"
                + "                           to process the chunks. Note that it is assumed that Grid Engine has already been\n"
                + "                           installed on your system and that the qsub command is on your PATH.\n"
                + " --altwgNaming             If the --fits-local option is provided, the output is saved to a fits file. Enabling\n"
                + "                           this option names the output fits file according to the ALTWG Naming convention. This\n"
                + "                           supercedes the <out-file> specified. If <out-file> contains a path then the altwg named\n"
                + "                           output file will be saved in that same path. Otherwise the altwg named output file will\n"
                + "                           be written in the current working directory.\n";
        // @formatter:on
        System.out.println(o);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        density = 1.0;
        rotationRate = 0.0;
        gravityType = GravityAlgorithmType.CHENG;
        howToEvalute = HowToEvaluate.EVALUATE_AT_CENTERS;
        fieldpointsfile = null;
        String inputfitsfile = null;
        minRefPotential = false;
        refPotential = 0.0;
        refPotentialProvided = false;
        //numCores = 1;
        numCores = Runtime.getRuntime().availableProcessors(); // twupy1
        outputFolder = ".";
        batchType = BatchType.LOCAL_PARALLEL_MAKE;
        tiltRadius = 0.05;
        boolean altwgName = false;

        int i = 0;
        for (; i < args.length; ++i) {
            if (args[i].equals("-d")) {
                density = Double.parseDouble(args[++i]);
            } else if (args[i].equals("-r")) {
                rotationRate = Double.parseDouble(args[++i]);
            } else if (args[i].equals("--werner")) {
                gravityType = GravityAlgorithmType.WERNER;
            } else if (args[i].equals("--cheng")) {
                gravityType = GravityAlgorithmType.CHENG;
            } else if (args[i].equals("--centers")) {
                howToEvalute = HowToEvaluate.EVALUATE_AT_CENTERS;
            } else if (args[i].equals("--fits-local")) {
                howToEvalute = HowToEvaluate.EVALUATE_AT_POINTS_IN_FITS_FILE;
                inputfitsfile = args[++i];
            } else if (args[i].equals("--min-ref-potential")) {
                minRefPotential = true;
            } else if (args[i].equals("--ref-potential")) {
                // The argument is either a double or a file that contains the
                // double as a single value
                String str = args[++i];
                try {
                    refPotential = Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    refPotential = getRefPotential(str);
                }
                refPotentialProvided = true;
            } else if (args[i].equals("--save-ref-potential")) {
                refPotentialFile = args[++i];
                saveRefPotential = true;
            } else if (args[i].equals("--output-folder")) {
                outputFolder = args[++i];
            } else if (args[i].equals("--num-jobs")) {
                numCores = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--batch-type")) {
                String type = args[++i];
                if (type.equals("local"))
                    batchType = BatchType.LOCAL_PARALLEL_MAKE;
                else if (type.equals("grid"))
                    batchType = BatchType.GRID_ENGINE_6;
            } else if (args[i].equals("--tilt-radius")) {
                tiltRadius = Double.parseDouble(args[++i]);
            } else if (args[i].equals("--altwgNaming")) {
                altwgName = true;
            } else {
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options.
        // Otherwise abort.
        int numberRequiredArgs = 4;
        if (args.length - i != numberRequiredArgs)
            usage();

        if (howToEvalute == HowToEvaluate.EVALUATE_AT_POINTS_IN_FITS_FILE && refPotentialProvided == false) {
            System.out.println("Error: When evaluating at points in a file, you must provide a value for the\n"
                    + "reference potential with the --ref-potential option.");
            System.exit(1);
        }

        objfile = args[i];
        outfile = args[i + 1];
        rootDir = args[i + 2];
        gravityExecutableName = args[i + 3];
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        System.out.println(dateFormat.format(date) + ":starting DistributedGravity");

        NativeLibraryLoader.loadVtkLibraries();

        globalShapeModelPolyData = PolyDataUtil.loadOBJShapeModel(objfile);

        if (globalShapeModelPolyData.GetPointData().GetNormals() == null
                || globalShapeModelPolyData.GetCellData().GetNormals() == null) {
            // Add normal vectors
            vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
            normalsFilter.SetInputData(globalShapeModelPolyData);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(1);
            normalsFilter.SplittingOff();
            normalsFilter.ConsistencyOn();
            normalsFilter.AutoOrientNormalsOff();
            normalsFilter.Update();

            vtkPolyData normalsOutput = normalsFilter.GetOutput();
            globalShapeModelPolyData.DeepCopy(normalsOutput);

            normalsFilter.Delete();
        }

        List<GravityValues> gravAtLocations = null;

        if (howToEvalute == HowToEvaluate.EVALUATE_AT_CENTERS) {
            gravAtLocations = getGravityAtLocations();
            saveResultsAtCenters(outfile, globalShapeModelPolyData, gravAtLocations);
        } else if (howToEvalute == HowToEvaluate.EVALUATE_AT_POINTS_IN_FITS_FILE) {
            if (!new File(inputfitsfile).exists()) {
                System.out.println("Error: " + inputfitsfile + " does not exist.");
                System.exit(1);
            }
            // Convert the fits file to ASCII
            fieldpointsfile = outfile + ".ascii";
            boolean localFits = true;
            boolean convertToxyz = true;
            PolyDataUtil2.convertFitsLLRModelToAscii(inputfitsfile, fieldpointsfile, convertToxyz, localFits);
            gravAtLocations = getGravityAtLocations();
            vtkPolyData fitspolydata = PolyDataUtil2.loadLocalFitsLLRModel(inputfitsfile, null);
            saveResultsAtPointsInFitsFile(altwgName, inputfitsfile, fitspolydata, outfile, gravAtLocations);
            new File(fieldpointsfile).delete();
        }
    }
}
