package edu.jhuapl.sbmt.tools2.toBeUpdated;

import java.io.File;
import java.io.IOException;
import java.util.List;

import vtk.vtkObject;
import vtk.vtkPolyData;
import vtk.vtkXMLPolyDataWriter;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.FilenameToRenderableImageFootprintPipeline;
import edu.jhuapl.sbmt.model.eros.Eros;

import nom.tam.fits.FitsException;

public class MSIFootprintGenerator
{
    private static SmallBodyModel erosModel;
    private static int resolutionLevel = 0;

    private static boolean checkIfMsiFilesExist(String line, PointingSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

        String name = line.substring(0, line.length()-4) + ".LBL";
        file = new File(name);
        if (!file.exists())
            return false;

        name = line.substring(0, line.length()-4) + "_DDR.LBL";
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
        }

        return true;
    }

    private static void generateMSIFootprints(
            List<String> msiFiles, PointingSource msiSource, ImagingInstrument instrument) throws IOException, FitsException
    {
        int count = 0;
        for (String filename : msiFiles)
        {
            System.out.println("starting msi " + count++ + " / " + msiFiles.size() + " " + filename + "\n");

            boolean filesExist = checkIfMsiFilesExist(filename, msiSource);
            if (filesExist == false)
                continue;

            String dayOfYearStr = "";
            String yearStr = "";

            File origFile = new File(filename);
            File f = origFile;

            f = f.getParentFile();

            f = f.getParentFile();
            dayOfYearStr = f.getName();

            f = f.getParentFile();
            yearStr = f.getName();

            String vtkfile = null;
            if (msiSource == PointingSource.SPICE)
                vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT_RES" + resolutionLevel + "_PDS.VTP";
            else
                vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT_RES" + resolutionLevel + "_GASKELL.VTP";

            // If the file we are trying to generate already exists, go on to the next file
            File realVtkFile = new File(vtkfile);
            if (realVtkFile.exists())
            {
                System.out.println("Already exists. Skipping\n\n");
                continue;
            }

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
//            ImageKey key = new ImageKey(keyName, msiSource);
//            MSIImage image = new MSIImage(key, erosModel, false);
//
//
//            System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
//            System.out.println("year: " + yearStr);
//            System.out.println("dayofyear: " + dayOfYearStr);
//            //System.out.println("midtime: " + midtime);
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

//            footprint.GetPointData().SetTCoords(null);
//            footprint.GetPointData().Reset();
//            footprint.GetCellData().Reset();

            // Use a tmp name while saving the file in case we abort during the saving
            vtkXMLPolyDataWriter writer = new vtkXMLPolyDataWriter();
            writer.SetInputData(footprint);
            writer.SetFileName(vtkfile + "_tmp");
            writer.SetCompressorTypeToZLib();
            writer.SetDataModeToBinary();
            writer.Write();

            // Okay, now rename the file to the real name.
            File tmpFile = new File(vtkfile + "_tmp");
            realVtkFile.delete();
            tmpFile.renameTo(realVtkFile);

            writer.Delete();
//            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));

            System.out.println("\n\n");
        }
    }

    /**
     * This program takes a file containing a list if FIT images and generates a vtk file containing the
     * footprint of the image in the same directory as the original file.
     * @param args
     */
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String msiFileList=args[0];
        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)config.getConfigForClass(ImagingInstrumentConfig.class);
        ImagingInstrument msiInstrument = imagingConfig.getImagingInstruments().stream().filter(inst -> inst.instrumentName == Instrument.MSI).toList().get(0);
        erosModel = new Eros((SmallBodyViewConfig)config);
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
            generateMSIFootprints(msiFiles, PointingSource.SPICE, msiInstrument);
            generateMSIFootprints(msiFiles, PointingSource.GASKELL, msiInstrument);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
