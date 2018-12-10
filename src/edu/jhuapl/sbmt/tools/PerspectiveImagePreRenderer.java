package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache.NonexistentRemoteFile;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.OffLimbPlaneCalculator;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

import nom.tam.fits.FitsException;

public class PerspectiveImagePreRenderer
{
    private PerspectiveImage image;
    private int resolutionIndex;
    private String outputDir;
    private boolean reprocess = false;


    public PerspectiveImagePreRenderer(PerspectiveImage image, String outputDir, boolean reprocess)
    {
        this.image = image;
        this.resolutionIndex = image.getSmallBodyModel().getModelResolution();
        this.outputDir = outputDir;
        this.reprocess = reprocess;
        calculateFootprint();
        calculateOffLimb();
    }

    private void compressFile(String filePath)
    {
        try
        {
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(new File(filePath + ".gz"));
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            FileInputStream is = new FileInputStream(filePath);
            int bytesRead;

            while ((bytesRead = is.read(buffer)) > 0)
            {
                gos.write(buffer, 0, bytesRead);
            }
            is.close();
            gos.finish();
            gos.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void calculateFootprint()
    {
        String intersectionFileName = outputDir + File.separator  + FilenameUtils.getBaseName(image.getFitFileFullPath()) + "_" + resolutionIndex + "_frustumIntersection.vtk";
        File intersectionFile = new File(intersectionFileName);
        if (intersectionFile.exists() && (reprocess == false))
        {
            System.out.println(
                    "PerspectiveImagePreRenderer: calculateFootprint: intersection file exists and reprocess is false");
            return;
        }

        SmallBodyModel smallBodyModel = image.getSmallBodyModel();
        double[] frustum1Adjusted = image.getFrustum1Adjusted()[image.getDefaultSlice()];
        double[] frustum2Adjusted = image.getFrustum2Adjusted()[image.getDefaultSlice()];
        double[] frustum3Adjusted = image.getFrustum3Adjusted()[image.getDefaultSlice()];
        double[] frustum4Adjusted = image.getFrustum4Adjusted()[image.getDefaultSlice()];
        double[] spacecraftPositionAdjusted = image.getSpacecraftPositionAdjusted()[image.getDefaultSlice()];
        vtkPolyData footprint = smallBodyModel.computeFrustumIntersection(spacecraftPositionAdjusted,
                frustum1Adjusted, frustum3Adjusted, frustum4Adjusted, frustum2Adjusted);

        footprint.GetCellData().SetScalars(null);
        footprint.GetPointData().SetScalars(null);

//        System.out.println("PerspectiveImagePreRenderer: calculateFootprint: footprint is " + footprint);

        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(footprint);
        System.out.println("PerspectiveImage: loadFootprint: fit file full path " + image.getFitFileFullPath());
//        String intersectionFileName = new File(image.getFitFileFullPath()).getParent() + File.separator  + FilenameUtils.getBaseName(image.getFitFileFullPath()) + "_" + resolutionIndex + "_frustumIntersection.vtk";

        System.out.println("PerspectiveImage: loadFootprint: saving footprint to " + intersectionFileName);
        if (!(new File(intersectionFileName).exists()))new File(intersectionFileName).getParentFile().mkdir();
        writer.SetFileName(new File(intersectionFileName).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
        compressFile(intersectionFileName);
    }

    private void calculateOffLimb()
    {
//      String filename = new File(image.getFitFileFullPath()).getParent() +  File.separator  + FilenameUtils.getBaseName(image.getFitFileFullPath()) + "_" + resolutionIndex + "_offLimbImageData.vtk";
        String filename = outputDir +  File.separator  + FilenameUtils.getBaseName(image.getFitFileFullPath()) + "_" + resolutionIndex + "_offLimbImageData.vtk";
        File file = new File(filename);
        if (file.exists() && (reprocess == false)) return;

        OffLimbPlaneCalculator calculator = new OffLimbPlaneCalculator(image);
        calculator.generateOffLimbPlane(image);
        if (!(new File(filename).exists())) new File(filename).getParentFile().mkdirs();
        calculator.saveToDisk(filename);
        compressFile(filename);
    }

    public static void main(String[] args) throws FitsException, IOException
    {
        String inputDirectory = args[0];
        final ImageSource source = ImageSource.valueOf(args[1]);
        ShapeModelBody body = ShapeModelBody.valueOf(args[2]);
        ShapeModelType type = ShapeModelType.valueOf(args[3]);
        int imagerIndex = Integer.parseInt(args[4]);
        String outputDirectory = args[5] + "/" + args[1];
        boolean reprocess = Boolean.parseBoolean(args[6]);

        boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
//        String rootURL = "http://sbmt.jhuapl.edu/sbmt/prod/";

        Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        SbmtMultiMissionTool.configureMission();

        // authentication
        Authenticator.authenticate();

        // initialize view config
        SmallBodyViewConfig.initialize();

        // VTK
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibrariesHeadless();

        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(body, type);
        ImagingInstrument instrument = config.imagingInstruments[imagerIndex];
        System.out.println("PerspectiveImagePreRenderer: main: input directory is " + inputDirectory);
        File[] fileList = new File(inputDirectory).listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return FilenameUtils.getExtension(name).contains("fit");
            }
        });
        Arrays.sort(fileList);
        ArrayList<File> imagesWithPointing = new ArrayList<File>();
        SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
        PerspectiveImage image;
        PerspectiveImagePreRenderer preRenderer;
        ImageKey key;
        for (int i=0; i<smallBodyModel.getNumberResolutionLevels(); i++)
        {
            System.out.println("PerspectiveImagePreRenderer: main: running model resolution " + (i+1) + " of " + smallBodyModel.getNumberResolutionLevels());
            smallBodyModel.setModelResolution(i);
            if (imagesWithPointing.isEmpty())
            {
                for (File filename : fileList)
                {
                    //may need to massage name here, need it to be /bennu/jfkfjksf, also need to strip .fits
                    String basename = filename.getParent() + File.separator + FilenameUtils.getBaseName(filename.getAbsolutePath());
        //            basename = basename.substring(basename.indexOf("2") + 2);
                    basename = basename.substring(basename.indexOf("prod/") + 4);
                    key = new ImageKey(basename, source, instrument);
                    System.out.println("PerspectiveImagePreRenderer: main: filename is " + basename);
                    try
                    {
                        image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                        String pointingFileString = "";
                        if (source == ImageSource.SPICE)
                        {
                            pointingFileString = image.getInfoFileFullPath();
                        }
                        else if (source == ImageSource.GASKELL)
                        {
                            pointingFileString = image.getSumfileFullPath();

                        }
                        System.out.println("PerspectiveImagePreRenderer: main: pointing file is " + pointingFileString + " and reprocess is " + reprocess);
                        imagesWithPointing.add(filename);

                    }
                    catch (NonexistentRemoteFile nerf)
                    {
                        continue;
                    }
                }
            }

            for (File filename : imagesWithPointing)
            {
                System.out.println("PerspectiveImagePreRenderer: main: processing file " + filename.getAbsolutePath());
                String basename = filename.getParent() + File.separator + FilenameUtils.getBaseName(filename.getAbsolutePath());
                basename = basename.substring(basename.indexOf("prod/") + 4);
                key = new ImageKey(basename, source, instrument);
                image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                preRenderer = new PerspectiveImagePreRenderer(image, outputDirectory, reprocess);
            }
        }
    }
}
