package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.eros.MSIImage;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.util.BackplanesFileFormat;

import altwg.util.BatchSubmit;
import altwg.util.BatchType;

/**
 * Distributed processing of Eros MSI backplanes.
 *
 * @author nguyel1
 *
 */
public class MSIBackplanesGenerator
{
    public void printUsage()
    {
        String o = "This program generates blackplanes for a list of MSI image files using the"
                + "Eros V1 shape model at highest resolution and with GASKELL pointing. It also"
                + "generates PDS version 4 labels for each backplanes file.\n"
                + "Usage: MSIBackplanesGenerator <root-dir> <image-list> <output-folder>\n\n"
                + "Where:\n"
                + "  <root-dir>               Path to the scripts that run the SBMT standalone java tools.\n"
                + "  <image-list>             Path to file listing the images to use. Images are\n"
                + "                           specified relative to the /project/nearsdc/data folder.\n"
                + "                           (e.g. /MSI/2000/116/cifdbl/M0132067419F1_2P_CIF_DBL)\n"
                + "  <output-folder>          Path to folder in which to place generated backplanes.\n\n";
        System.out.println(o);
    }

    private void doMain(String[] args) throws IOException, InterruptedException
    {
        int numberRequiredArgs = 3;
        if (args.length != numberRequiredArgs)
        {
            System.out.println("Incorrectly formed arguments.\n");
            printUsage();
            System.exit(0);
        }

        String rootDir = args[0];
        String imageFileList = args[1];
        String outputFolder = args[2];
        File outDir = (new File(outputFolder));
        if (!outDir.exists())
        {
            outDir.mkdirs();
        }

        // VTK and authentication
        Configuration.setAppName("neartool");
        Configuration.setCacheVersion("2");
        Configuration.setAPLVersion(true);
        SmallBodyViewConfig.initialize();
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Authenticator.authenticate();

        //Set up the distributed environment
        ArrayList<String> commandList = new ArrayList<String>();

        //Set up the configuration for MSI GASKELL resolution 3
        String resolution = "3";
        String camera = Instrument.MSI.name();
        String ptg = ImageSource.GASKELL.name();
        String body = ShapeModelBody.EROS.name();

        //Read each line in the input image list and form the command line
        //call to BackplanesGenerator using the image on that line.
        List<String> imageFiles = FileUtil.getFileLinesAsStringList(imageFileList);
        System.err.println("Number of images to process: " + imageFiles.size());

        for (String image : imageFiles)
        {
            //Before generating the backplanes, check to see if a backplanes file already exists. If yes, do not regenerate.
            if (!backplanesFileExists(image, outputFolder))
            {
                //Generate the backplanes for this image
                String command = String.format(rootDir + File.separator + "BackplanesGenerator -c " + camera + " -r " + resolution + " -f -s -p " + ptg + " " + body + " %s %s", image, outputFolder);
                System.err.println("Command:" + command);
                commandList.add(command);
            }
        }

        BatchSubmit batchSubmit = new BatchSubmit(commandList, BatchType.GRID_ENGINE);
        batchSubmit.runBatchSubmitinDir(outputFolder);

    }

    /**
     * Test if the backplanes file already exists. If not do not recreate it.
     * This is used if there is a crash and we need to restart - don't want
     * to reprocess finished backplanes file. Note - it is recommended to
     * delete the last file that was processed, as it might not be complete.
     *
     * @param image - original FITS image file with cache path prepended
     * @param outputFolder - folder to which the backplanes are written
     * @return true if a backplanes file is already in the output folder
     */
    private boolean backplanesFileExists(String image, String outputFolder)
    {
        BackplanesFileFormat fmt = BackplanesFileFormat.FITS;
        String resolution = "3";
        ImageSource ptg = ImageSource.GASKELL;

        try
        {
            SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL, null));
            ImageKey key = new ImageKey(image.replace(".FIT", ""), ptg, smallBodyModel.getSmallBodyConfig().imagingInstruments[0]);
            String backplanesFilename = BackplanesGenerator.getBaseFilename(new MSIImage(key, smallBodyModel, false), key, Integer.valueOf(resolution), BackplanesFileFormat.IMG, outputFolder) + fmt.getExtension();
            File f = new File(backplanesFilename);
            if (f.exists())
            {
                return true;
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }

    public static void main(String[] args) throws Exception
    {
        new MSIBackplanesGenerator().doMain(args);
    }

}
