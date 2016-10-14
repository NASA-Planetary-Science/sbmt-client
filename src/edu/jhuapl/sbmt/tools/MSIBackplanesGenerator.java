package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;

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
                + "Usage: MSIBackplanesGenerator <image-list> <output-folder>\n\n"
                + "Where:\n"
                + "  <image-list>             Path to file listing the images to use. Images are\n"
                + "                           specified relative to the /project/nearsdc/data folder.\n"
                + "                           (e.g. /MSI/2000/116/cifdbl/M0132067419F1_2P_CIF_DBL)\n"
                + "  <output-folder>          Path to folder which where to place generated backplanes.\n\n";
        System.out.println(o);
    }

    private void doMain(String[] args) throws IOException, InterruptedException
    {
        int numberRequiredArgs = 2;
        if (args.length != numberRequiredArgs)
        {
            System.out.println("Incorrectly formed arguments.\n");
            printUsage();
        }

        String imageFileList = args[0];
        String outputFolder = args[1];
        File outDir = (new File(outputFolder));
        if (!outDir.exists())
        {
            outDir.mkdirs();
        }

        // VTK and authentication
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();
        Authenticator.authenticate();

        //Set up the distributed environment
        ArrayList<String> commandList = new ArrayList<String>();

        //Read each line in the input image list and form the command line
        //call to BackplanesGenerator using the image on that line.
        List<String> imageFiles = FileUtil.getFileLinesAsStringList(imageFileList);
        for (String image : imageFiles)
        {
            String command = String.format("BackplanesGenerator -c msi -r 3  -f -s -p gaskell eros %s %s", image, outputFolder);
            commandList.add(command);
        }

        BatchSubmit batchSubmit = new BatchSubmit(commandList, BatchType.GRID_ENGINE);
        batchSubmit.runBatchSubmitinDir(outputFolder);

    }

    public static void main(String[] args) throws Exception
    {
        new MSIBackplanesGenerator().doMain(args);
    }

}
