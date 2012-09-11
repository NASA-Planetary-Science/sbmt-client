package edu.jhuapl.near.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.vtkFloatArray;
import vtk.vtkGlobalJavaHash;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkTriangle;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.Frustum;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class MSIBestResolutionPerPlate
{
    private static SmallBodyModel erosModel;
    private static int resolutionLevel = 0;

    private static ArrayList<Double> bestResolutionPerPlate = new ArrayList<Double>();
    private static ArrayList<Integer> numImagesResLessThan5mpp = new ArrayList<Integer>();
    private static ArrayList<Integer> numImagesResLessThan10mpp = new ArrayList<Integer>();
    private static ArrayList<Integer> numImagesResLessThan50mpp = new ArrayList<Integer>();
    private static ArrayList<Integer> numImagesResLessThan100mpp = new ArrayList<Integer>();
    private static ArrayList<Integer> numImagesResLessThan200mpp = new ArrayList<Integer>();

    private static boolean checkIfMsiFilesExist(String line, MSIImage.ImageSource source)
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
        if (source.equals(ImageSource.GASKELL))
        {
            File msirootdir = (new File(line)).getParentFile().getParentFile().getParentFile().getParentFile();
            String msiId = (new File(line)).getName().substring(0, 11);
            name = msirootdir.getAbsolutePath() + "/sumfiles/" + msiId + ".SUM";
            file = new File(name);
            if (!file.exists())
                return false;
        }

        return true;
    }

    private static void computeBestResolutionPerPlate(
            ArrayList<String> msiFiles, MSIImage.ImageSource msiSource) throws IOException, FitsException
    {
        int numPlatesInSmallBodyModel = erosModel.getSmallBodyPolyData().GetNumberOfCells();
        for (int i=0; i<numPlatesInSmallBodyModel; ++i)
        {
            bestResolutionPerPlate.add(Double.MAX_VALUE);
            numImagesResLessThan5mpp.add(0);
            numImagesResLessThan10mpp.add(0);
            numImagesResLessThan50mpp.add(0);
            numImagesResLessThan100mpp.add(0);
            numImagesResLessThan200mpp.add(0);
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
            ImageKey key = new ImageKey(keyName, msiSource);
            MSIImage image = new MSIImage(key, erosModel, false, rootFolder);


            //System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));

            image.loadFootprint();
            vtkPolyData footprint = image.getUnshiftedFootprint();

            if (footprint == null || footprint.GetNumberOfPoints() == 0)
            {
                System.err.println("Error: Footprint generation failed");
                continue;
            }


            // Go through each plate in the footprint, find the corresponding plate in the original
            // shape model, and calculate the resolution at that plate. Update the bestResolutionPerPlate
            // array if resolution is less.

            double[] spacecraftPosition = image.getSpacecraftPosition();
            int imageHeight = image.getImageHeight();
            int imageWidth = image.getImageWidth();
            System.out.println(imageHeight + " " + imageWidth);
            Frustum frustum = image.getFrustum();
            double[] frustum1 = frustum.ul;
            double[] frustum2 = frustum.ur;
            double[] frustum3 = frustum.lr;
            //double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / imageHeight;
            double horizScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum3) / 2.0 ) / imageWidth;
            //double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / imageWidth;
            double vertScaleFactor = 2.0 * Math.tan( MathUtil.vsep(frustum1, frustum2) / 2.0 ) / imageHeight;


            footprint.BuildCells();
            int numberCells = footprint.GetNumberOfCells();
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
                double vertPixelScale = 1000.0 * dist * vertScaleFactor;

                int cellId = erosModel.findClosestCell(center,closestPoint);

                if (MathUtil.distanceBetween(center, closestPoint) > 0.001)
                    System.out.println("Warning: distance is " + MathUtil.distanceBetween(center, closestPoint));

                double[] normal = normals.GetTuple3(cellId);
                double[] illumAngles = image.computeIlluminationAnglesAtPoint(center, normal);

                double incidence = illumAngles[0];
                double emission = illumAngles[1];

                if (horizPixelScale < bestResolutionPerPlate.get(cellId) &&
                        incidence >= 40.0 && incidence <= 70.0 &&
                        emission >= 0.0 && emission <= 20.0)
                {
                    bestResolutionPerPlate.set(cellId, horizPixelScale);
                    //System.out.println(horizPixelScale + " " + vertPixelScale + " " + incidence + " " + emission + " " + dist + " " );

                    numImagesResLessThan5mpp.set(cellId, numImagesResLessThan5mpp.get(cellId)+1);
                    numImagesResLessThan10mpp.set(cellId, numImagesResLessThan10mpp.get(cellId)+1);
                    numImagesResLessThan50mpp.set(cellId, numImagesResLessThan50mpp.get(cellId)+1);
                    numImagesResLessThan100mpp.set(cellId, numImagesResLessThan100mpp.get(cellId)+1);
                    numImagesResLessThan200mpp.set(cellId, numImagesResLessThan200mpp.get(cellId)+1);

                    ++numGoodPlates;

                }

                points.Delete();
                cell.Delete();
            }

            System.out.println("num good plates: " + numGoodPlates);
            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());

            System.out.println("\n\n");

            if (count % 1000 == 0)
                saveSingleDataArray("./eros-best-resolutions-per-plate-" + count + ".txt", bestResolutionPerPlate);
        }
    }

    private static void saveDataArrays() throws IOException
    {
        saveSingleDataArray("./eros-best-resolutions-per-plate.txt", bestResolutionPerPlate);
        saveSingleDataArray("./eros-num-images-less-than-5mpp-per-plate.txt", numImagesResLessThan5mpp);
        saveSingleDataArray("./eros-num-images-less-than-10mpp-per-plate.txt", numImagesResLessThan10mpp);
        saveSingleDataArray("./eros-num-images-less-than-50mpp-per-plate.txt", numImagesResLessThan50mpp);
        saveSingleDataArray("./eros-num-images-less-than-100mpp-per-plate.txt", numImagesResLessThan100mpp);
        saveSingleDataArray("./eros-num-images-less-than-200mpp-per-plate.txt", numImagesResLessThan200mpp);
    }

    private static void saveSingleDataArray(String filename, ArrayList dataarray) throws IOException
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
        NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

        String msiFileList=args[0];

        erosModel = new Eros();
        resolutionLevel = Integer.parseInt(args[1]);
        try {
            erosModel.setModelResolution(resolutionLevel);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MSIImage.setGenerateFootprint(true);

        ArrayList<String> msiFiles = null;
        try {
            msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            computeBestResolutionPerPlate(msiFiles, ImageSource.PDS);
            //computeBestResolutionPerPlate(msiFiles, ImageSource.GASKELL);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }

        saveDataArrays();
    }

}
