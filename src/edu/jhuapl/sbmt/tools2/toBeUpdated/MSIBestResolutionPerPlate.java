package edu.jhuapl.sbmt.tools2.toBeUpdated;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkFloatArray;
import vtk.vtkObject;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkTriangle;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.config.ISmallBodyViewConfig;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.FootprintToIlluminationAnglesAtPointOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.ImageIlluminationAtPoint;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.FilenameToRenderableImageFootprintPipeline;
//import edu.jhuapl.sbmt.image.model.bodies.eros.MSIImage;
//import edu.jhuapl.sbmt.image.model.keys.ImageKey;
import edu.jhuapl.sbmt.model.eros.Eros;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class MSIBestResolutionPerPlate
{
    private static SmallBodyModel erosModel;
    private static int resolutionLevel = 0;

    private static class PlateStatistics {
        public double bestResolution = Double.MAX_VALUE;
        public double meanResolution = 0.0;
        public double medianResolution = 0.0;
        public int[] histogram = new int[200];

        public int getNumberOfSamples()
        {
            int count = 0;
            for (int bin : histogram)
                count += bin;
            return count;
        }

        public double getPercentile(double p)
        {
            int numberOfSamples = getNumberOfSamples();
            int count = 0;
            for (int i=0; i<histogram.length; ++i)
            {
                if (count >= (p*numberOfSamples))
                    return i;
                count += histogram[i];
            }
            return histogram.length;
        }

        @Override
        public String toString()
        {
            String s = bestResolution + "," + meanResolution+ "," + medianResolution;
            for (int i = 0; i < 50; ++i)
                s += "," + histogram[i];
            return s;
        }
    }

    private static List<PlateStatistics> statisticsPerPlate = new ArrayList<PlateStatistics>();
    private static List<String> msiFilesWithAtLeastOneGoodPlate = new ArrayList<String>();

    private static boolean checkIfMsiFilesExist(String line, PointingSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

//        String name = line.substring(0, line.length()-4) + ".LBL";
//        file = new File(name);
//        if (!file.exists())
//            return false;

        String name = line.substring(0, line.length()-4) + "_DDR.LBL";
        file = new File(name);
        if (!file.exists())
            return false;

        // Check for the sumfile if source is Gaskell
        if (source.equals(PointingSource.GASKELL))
        {
            File msirootdir = (new File(line)).getParentFile().getParentFile().getParentFile().getParentFile();
            String msiId = (new File(line)).getName().substring(0, 11);
            name = msirootdir.getAbsolutePath() + "/sumfiles/" + msiId + ".SUM";
            file = new File(name);
            if (!file.exists())
                return false;

            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
            // are 1153 bytes long or less
            if (file.length() <= 1153)
                return false;

        }

        return true;
    }

    private static void computeBestResolutionPerPlate(
            List<String> msiFiles, PointingSource msiSource, ImagingInstrument instrument) throws Exception
    {
        int numPlatesInSmallBodyModel = (int)erosModel.getSmallBodyPolyData().GetNumberOfCells();
        for (int i=0; i<numPlatesInSmallBodyModel; ++i)
        {
            statisticsPerPlate.add(new PlateStatistics());
        }
        vtkFloatArray normals = erosModel.getCellNormals();

        int count = 0;
        for (String filename : msiFiles)
        {
            System.out.println("starting msi " + count++ + " / " + msiFiles.size() + " " + filename + "\n");

            boolean filesExist = checkIfMsiFilesExist(filename, msiSource);
            if (filesExist == false)
            {
                System.out.println("MSI files do not exist\n\n");
                continue;
            }

            File origFile = new File(filename);

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
//            ImageKey key = new ImageKey(keyName, msiSource);
//            MSIImage image = new MSIImage(key, erosModel, false);
//
//
//            //System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
//
//            image.loadFootprint();
//            vtkPolyData footprint = image.getUnshiftedFootprint();

            FilenameToRenderableImageFootprintPipeline pipeline = null;
			try
			{
				pipeline = FilenameToRenderableImageFootprintPipeline.of(keyName, msiSource, List.of(erosModel), instrument);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            vtkPolyData footprint = pipeline.getFootprints().get(0);

            if (footprint == null || footprint.GetNumberOfPoints() == 0)
            {
                System.err.println("Error: Footprint generation failed");
                continue;
            }

            RenderablePointedImage image = pipeline.getImages().get(0);
            PointingFileReader pointing = image.getPointing();

            // Go through each plate in the footprint, find the corresponding plate in the original
            // shape model, and calculate the resolution at that plate. Update the bestResolutionPerPlate
            // array if resolution is less.

            double[] spacecraftPosition = pointing.getSpacecraftPosition();
            int imageHeight = image.getImageHeight();
            int imageWidth = image.getImageWidth();
            System.out.println(imageHeight + " " + imageWidth);
            Frustum frustum = new Frustum(spacecraftPosition,
	    			pointing.getFrustum1(),
	    			pointing.getFrustum3(),
	    			pointing.getFrustum4(),
	    			pointing.getFrustum2());
            double[] frustum1 = frustum.ul;
            //double[] frustum2 = frustum.ur;
            double[] frustum3 = frustum.lr;
            double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / imageWidth;
            //double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / imageHeight;


            footprint.BuildCells();
            int numberCells = (int)footprint.GetNumberOfCells();
            System.out.println("number of plates in footprint: " + numberCells);
            double[] pt0 = new double[3];
            double[] pt1 = new double[3];
            double[] pt2 = new double[3];
            double[] center = new double[3];
            double[] vec = new double[3];
            double[] closestPoint = new double[3];
            int numGoodPlates = 0;
            for (int i=0; i<numberCells; ++i)
            {
                vtkTriangle cell = (vtkTriangle) footprint.GetCell(i);
                vtkPoints points = cell.GetPoints();
                points.GetPoint(0, pt0);
                points.GetPoint(1, pt1);
                points.GetPoint(2, pt2);

                cell.TriangleCenter(pt0, pt1, pt2, center);

                vec[0] = center[0] - spacecraftPosition[0];
                vec[1] = center[1] - spacecraftPosition[1];
                vec[2] = center[2] - spacecraftPosition[2];
                double dist = MathUtil.vnorm(vec);

                double horizPixelScale = 1000.0 * dist * horizScaleFactor;
                //double vertPixelScale = 1000.0 * dist * vertScaleFactor;

                int cellId = erosModel.findClosestCell(center,closestPoint);

                if (MathUtil.distanceBetween(center, closestPoint) > 0.001)
                    System.out.println("Warning: distance is " + MathUtil.distanceBetween(center, closestPoint));

                double[] normal = normals.GetTuple3(cellId);
                List<ImageIlluminationAtPoint> illumAtts = Lists.newArrayList();
        		Just.of(Triple.of(pointing, center, normal))
        			.operate(new FootprintToIlluminationAnglesAtPointOperator())
        			.subscribe(Sink.of(illumAtts))
        			.run();


//                double[] illumAngles = image.computeIlluminationAnglesAtPoint(center, normal);

                double incidence = illumAtts.get(0).getIncidence();
                double emission = illumAtts.get(0).getEmission();

                if (incidence >= 0.0 && incidence <= 80.0 &&
                        emission >= 0.0 && emission <= 70.0)
                {
                    if (horizPixelScale < statisticsPerPlate.get(cellId).bestResolution)
                        statisticsPerPlate.get(cellId).bestResolution = horizPixelScale;

                    statisticsPerPlate.get(cellId).meanResolution += horizPixelScale;

                    int binIdx = (int) Math.floor(horizPixelScale);
                    statisticsPerPlate.get(cellId).histogram[binIdx] += 1;

                    //System.out.println(horizPixelScale + " " + vertPixelScale + " " + incidence + " " + emission + " " + dist + " " );

                    ++numGoodPlates;

                }

                points.Delete();
                cell.Delete();
            }

            if (numGoodPlates > 0)
                msiFilesWithAtLeastOneGoodPlate.add(filename);

            System.out.println("num good plates: " + numGoodPlates);
//            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));

            System.out.println("\n\n");

            if (count % 1000 == 0)
                saveSingleDataArray("./eros-best-resolutions-per-plate-" + count + ".txt", statisticsPerPlate);
        }

        for (PlateStatistics ps : statisticsPerPlate)
        {
            ps.meanResolution /= ps.getNumberOfSamples();
            ps.medianResolution = ps.getPercentile(0.5);
        }
    }

    private static void saveDataArrays() throws IOException
    {
        saveSingleDataArray("./eros-images-with-at-least-one-good-plate.txt", msiFilesWithAtLeastOneGoodPlate);
        saveSingleDataArray("./eros-best-resolutions-per-plate.txt", statisticsPerPlate);
    }

    private static void saveSingleDataArray(String filename, List dataarray) throws IOException
    {
        FileWriter fstream = new FileWriter(filename);
        BufferedWriter out = new BufferedWriter(fstream);

        for (Object v : dataarray)
            out.write(v + "\n");

        out.close();
    }

    /**
     * This program takes a file containing a list if FIT images and generates a vtk file containing the
     * footprint of the image in the same directory as the original file.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        if (!Configuration.isLinux())
            System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String msiFileList=args[0];
        ISmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
        ImagingInstrument msiInstrument = Stream.of(config.getImagingInstruments()).filter(inst -> inst.instrumentName == Instrument.MSI).toList().get(0);
        erosModel = new Eros(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL));
        resolutionLevel = Integer.parseInt(args[1]);
        try {
            erosModel.setModelResolution(resolutionLevel);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

//        MSIImage.setGenerateFootprint(true);

        List<String> msiFiles = null;
        try {
            msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            //computeBestResolutionPerPlate(msiFiles, ImageSource.PDS);
            computeBestResolutionPerPlate(msiFiles, PointingSource.GASKELL, msiInstrument);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }

        saveDataArrays();
    }

}
