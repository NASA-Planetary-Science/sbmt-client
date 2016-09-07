package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vtk.vtkObject;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.eros.MSIImage;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.util.BatchSubmission;
import edu.jhuapl.sbmt.util.BatchSubmission.BatchType;


/**
 * This program should be run prior to running CompareGaskellAndNLR.
 * It runs mapmaker centered at each intersection point of pixel 260/220
 * and converts the map to a triangular plate model.
 */
public class CompareGaskellAndNLRGenerateMapletsForImages
{
    static void testIntersect(String mapletFile,
            ImageKey key,
            int sampleOffset,
            int lineOffset
            ) throws Exception
    {
        vtkPolyData smallBodyPolyData = PolyDataUtil.loadShapeModel(mapletFile);
        SmallBodyModel smallBodyModel = new SmallBodyModel(smallBodyPolyData);

        MSIImage image = new MSIImage(key, smallBodyModel, true);

        double[] imageSurfacePoint = image.getPixelSurfaceIntercept(259 + sampleOffset, 411 - (219 + lineOffset));
        if (imageSurfacePoint == null)
        {
            System.out.println("Error: no intercept to maplet just created! Very bad");
        }

        smallBodyModel.delete();
        System.gc();
        vtkObject.JAVA_OBJECT_MANAGER.gc(true);
    }

    static void doComparison(
            List<String> msiFiles,
            SmallBodyModel smallBodyModel,
            int sampleOffset,
            int lineOffset
            ) throws Exception
    {
        String mapmakerFolder = Configuration.getCacheDir() + "/GASKELL/EROS/mapmaker";
        String outputFolder = mapmakerFolder+"/OUTPUT/";
        List<String> mapmakerCommands = new ArrayList<String>();
        List<String> convertCommands = new ArrayList<String>();

        int count = 1;
        for (String keyName : msiFiles)
        {
            //if (count % 2500 == 0)
            System.out.println("starting msi " + count + " / " + msiFiles.size() + " " + keyName);
            ++count;

            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, ImageSource.GASKELL);
            MSIImage image = new MSIImage(key, smallBodyModel, true);

            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
            // are 1153 bytes long or less
            File sumfile = new File(image.getSumfileFullPath());
            if (!sumfile.exists() || sumfile.length() <= 1153)
            {
                System.out.println("no sumfile or no landmarks");
                continue;
            }

            String imageId = new File(key.name).getName();
            imageId = imageId.substring(0, imageId.length()-4);

            double[] imageSurfacePoint = image.getPixelSurfaceIntercept(259 + sampleOffset, 411 - (219 + lineOffset));
            if (imageSurfacePoint == null)
            {
                System.out.println("no intercept");
                continue;
            }

            LatLon llr = MathUtil.reclat(imageSurfacePoint).toDegrees();
            String name = imageId;
            mapmakerCommands.add(String.format("RunMapmaker --delete-cub %s %s %s %s %s %s %s", mapmakerFolder, name, "100", "5.0", String.valueOf(llr.lat), String.valueOf(llr.lon), outputFolder));

            String mapletFile = outputFolder + "/" + name + ".FIT";
            String mapletFileVtk = outputFolder + "/" + name + ".vtk";
            convertCommands.add(String.format("ConvertMaplet %s %s %s", "--vtk", mapletFile, mapletFileVtk));

            // Uncomment this and move the BatchSubmission.runBatchSubmitPrograms call
            // (later on) to here to test the the generated map is in the right location.
            //testIntersect(mapletFileVtk, key, sampleOffset, lineOffset);
        }

        BatchSubmission.runBatchSubmitPrograms(mapmakerCommands, BatchType.GRID_ENGINE);
        BatchSubmission.runBatchSubmitPrograms(convertCommands, BatchType.GRID_ENGINE);
    }


    public static void main(String[] args) throws Exception
    {
        NativeLibraryLoader.loadVtkLibrariesHeadless();

        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL);
        SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
        smallBodyModel.setModelResolution(3);

        FileCache.setOfflineMode(true, Configuration.getCacheDir());

        // Get list of gaskell files
        String msiFileList=args[0];
        List<String> msiFiles = null;
        try {
            msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        doComparison(msiFiles, smallBodyModel, 0, 0);
    }
}
