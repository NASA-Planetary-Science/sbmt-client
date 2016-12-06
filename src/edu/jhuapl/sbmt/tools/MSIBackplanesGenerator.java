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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
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
                + "Usage: MSIBackplanesGenerator <root-dir> <image-list> <output-folder> <finish-folder> [<maxArrayJobs>]\n\n"
                + "Where:\n"
                + "  <root-dir>               Path to the scripts that run the SBMT standalone java tools.\n"
                + "  <image-list>             Path to file listing the images to use. Images are\n"
                + "                           specified relative to the /project/nearsdc/data folder.\n"
                + "                           (e.g. /MSI/2000/116/cifdbl/M0132067419F1_2P_CIF_DBL)\n"
                + "  <output-folder>          Path to folder in which to place generated backplanes.\n"
                + "  <finish-folder>          Path to folder to which to move finished backplanes.\n"
                + "  <maxArrayJobs>           Optional maximum number of jobs per qsub call. Each \n"
                + "                           job processes one MSI image backplane.\n\n"
                + "Example: \n"
//        /project/sbmtpipeline/sbmt_msiBackplanes/bin/MSIBackplanesGenerator.sh $SBMTROOT/bin msiImageList.txt.small /project/sbmtpipeline/processed/msiBatchSubmit/MSIBackplanes /project/sbmtpipeline/processed/msiBatchSubmit/MSIBackplanes/older
//                + "/project/sbmtpipeline/sbmt_msiBackplanes/bin/MSIBackplanesGenerator.sh $SBMTROOT/bin msiImageList.txt /disk1/scratch/nguyel1/MSIBackplanes /disk1/scratch/nguyel1/MSIBackplanes/older 500\n\n";
                + "/project/sbmtpipeline/sbmt_msiBackplanes/bin/MSIBackplanesGenerator.sh /project/sbmtpipeline/sbmt_msiBackplanes/bin msiImageList.txt /project/sis/users/nguyel1/MSIBackplanes /project/sis/users/nguyel1/MSIBackplanes/older 500\n\n";

        System.out.println(o);
    }

    private void doMain(String[] args) throws IOException
    {
        int numberRequiredArgs = 4;
        if (args.length < numberRequiredArgs)
        {
            System.out.println("MSIBackplanes: incorrect number of arguments.\n");
            printUsage();
            System.exit(0);
        }

        int maxJobs = 100;
        String rootDir = args[0];
        String imageFileList = args[1];
        String outputFolder = args[2];
        String finishedFolder = args[3];
        if (args.length > 4)
        {
            maxJobs = Integer.valueOf(args[4]);
        }

        //If running in the cluster disk scratch areas, this must be done either
        //in the array job itself (in BackplanesGenerator.java), or in a script
        //that is called ahead of time (e.g. southpark_createDirs.sh script). If
        //done here, it creates the folders only on the machine that this java
        //class is called from, and qsub will error out on all other machines
        //because it can't find the folders. Regardless, the folders must be
        //manually deleted before rerun.
        String qsubErr = new File(outputFolder, "qsubErrorLogs").getAbsolutePath();
        String qsubOut = new File(outputFolder, "qsubOutputLogs").getAbsolutePath();
        if (!outputFolder.toLowerCase().contains("scratch") || outputFolder.toLowerCase().startsWith("/project"))
        {
            createFolder(outputFolder);
            createFolder(qsubErr);
            createFolder(qsubOut);
        }
        else if (!new File(outputFolder).exists())
        {
            System.err.println("MSIBackplanesGenerator.java: Failed to create " + outputFolder + ". Exiting.");
            System.exit(0);
        }
        if (!finishedFolder.toLowerCase().contains("scratch") || finishedFolder.toLowerCase().startsWith("/project"))
        {
            createFolder(finishedFolder);
        }
        else if (!new File(finishedFolder).exists())
        {
            System.err.println("MSIBackplanesGenerator.java: Failed to create " + finishedFolder + ". Exiting.");
            System.exit(0);
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
        System.err.println("MSIBackplanesGenerator.java: Number of images to process: " + imageFiles.size());

        for (String image : imageFiles)
        {
            //Before adding the command to generate the backplanes, check to see if a backplanes
            //file already exists. If yes, do not regenerate.
//            if (!backplanesFileExists(image, finishedFolder))
            {
                //Generate the backplanes for this image
                String command = String.format(rootDir + File.separator + "BackplanesGenerator -c " + camera + " -r " + resolution + " -f -s -p " + ptg + " " + body + " %s %s", image, outputFolder);
//                System.err.println("MSIBackplanesGenerator.java, Command sent to command list is: " + command);
                commandList.add(command);
            }

            if (commandList.size() >= maxJobs)
            {
                executeJobs(commandList, outputFolder, finishedFolder, qsubOut, qsubErr);

                //reset for next batch of commands.
                commandList = new ArrayList<String>();
            }
        }

        executeJobs(commandList, outputFolder, finishedFolder, qsubOut, qsubErr);

    }

    private void createFolder(String folder)
    {
        File outDir = (new File(folder));
        if (!outDir.exists())
        {
            outDir.mkdirs();
            if (!outDir.exists())
            {
                System.err.println("MSIBackplanesGenerator.java: Failed to create " + outDir.getAbsolutePath() + ". Exiting.");
                System.exit(0);
            }
        }
        else
        {
            System.err.println("MSIBackplanesGenerator.java: Directory " + outDir.getAbsolutePath() + " exists. Delete or rename then rerun program. Exiting.");
            System.exit(0);
        }
    }

    private void executeJobs(ArrayList<String> commandList, String outputFolder, final String finishedFolder, final String qsubOut, final String qsubErr)
    {
        //print the time
        Date now = new Date();
        System.out.println("---- Array job assembled. Submitting to grid engine at " + now.toString());

        //submit the command list to the grid engine. the qsub is called with sync -y,
        //so all the jobs will finish before the Java program continues.
        BatchSubmit batchSubmit = new BatchSubmit(commandList, BatchType.GRID_ENGINE);
        try
        {
            batchSubmit.runBatchSubmitinDir(outputFolder);
        }
        catch (Exception e)
        {
            System.err.println("BatchSubmit error in MSIBackplanesGenerator.java:");
            e.printStackTrace();
            return;
        }
//        System.err.println("MSIBackplanesGenerator.java, back from BatchSubmit");

        //Move the finished files before next batch of jobs is qsubbed.
        moveFinishedBackplanes(outputFolder, finishedFolder);
        moveFinishedQsubLogs(outputFolder, "glob:**/*.{bash.o}*", qsubOut);
        moveFinishedQsubLogs(outputFolder, "glob:**/*.{bash.e}*", qsubErr);
    }

    private void moveFinishedBackplanes(String outputFolder, final String finishedFolder)
    {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{fit,xml}");
        try
        {
            Files.walkFileTree(Paths.get(outputFolder), new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException
                {
                    if (pathMatcher.matches(path))
                    {
                        //Move the files here.
//                        System.err.println("MSIBackplanesGenerator.java, path to found file is " + path);
//                        System.err.println("MSIBackplanesGenerator.java, path to parent of found file is " + path.getParent().toString());
//                        System.err.println("MSIBackplanesGenerator.java, name of found file is " + path.getFileName().toString());
                        Path moveTo = Paths.get(finishedFolder, path.getFileName().toString());
//                        System.err.println("MSIBackplanesGenerator.java, moving to " + moveTo.toFile().getAbsolutePath());
                        Files.move(path, moveTo, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
                {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            System.err.println("Backplanes move error in MSIBackplanesGenerator.java:");
            e.printStackTrace();
        }
    }

    private void moveFinishedQsubLogs(String outputFolder, String glob, final String qsubDir)
    {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        try
        {
            Files.walkFileTree(Paths.get(outputFolder), new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException
                {
                    if (pathMatcher.matches(path))
                    {
                        //Move the files here.
                        Path moveTo = Paths.get(qsubDir, path.getFileName().toString());
//                        System.err.println("qsub moving " + path.toFile().getAbsolutePath() + " to " + qsubDir.getAbsolutePath());
                        Files.move(path, moveTo, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
                {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            System.err.println("qsubLog move error in MSIBackplanesGenerator.java:");
            e.printStackTrace();
        }
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
            String backplanesFilename = BackplanesGenerator.getBaseFilename(new MSIImage(key, smallBodyModel, false), key, Integer.valueOf(resolution), BackplanesFileFormat.FITS, outputFolder) + fmt.getExtension();
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
