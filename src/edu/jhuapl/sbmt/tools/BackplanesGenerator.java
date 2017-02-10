package edu.jhuapl.sbmt.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkDebugLeaks;

import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.util.BackplanesFileFormat;


/**
 * This program generates blackplanes for a list of image files. A separate
 * backplane for each resolution level is generated. Currently MSI and Itokawa
 * images only are supported. User calls this program with 3 arguments as
 * follows:
 *
 * 1. body - name of the shape model body, in SBMT naming conventions.
 * 2. filelist - path to file listing images to use. Images are specified same way as SBMT
 *               uses to download files. (e.g. /GASKELL/EROS/MSI/images/M0131776147F1_2P_CIF.FIT)
 * 3. outputfolder - folder where to place generated backplanes
 *
 * Note that there is another program in this package called
 * AmicaBackplanesGenerator. That program is a mess and was meant for a specific
 * deliverable of Itokawa backplanes and will hopefully never be used again. This one is
 * meant to be more general and should be independent of shape model used.
 */
public class BackplanesGenerator
{
    private SmallBodyModel smallBodyModel;

    private List<String> filesProcessed = new ArrayList<String>();

    /**
     * Generates backplanes for each file in an input image list.
     *
     * @param imageFiles
     * @param instr
     * @param outputFolder
     * @param fmt
     * @throws IOException
     */
    private void generateBackplanes(
    		List<String> imageFiles,
            Instrument instr,
            String outputFolder,
            BackplanesFileFormat fmt,
            ImageSource ptg) throws Exception
    {
        filesProcessed.clear();
        int resolutionLevel = smallBodyModel.getModelResolution();
        vtkDebugLeaks debugLeaks = new vtkDebugLeaks();

        // Suppress exception output if pointing files do not exist for any of the ImageSources
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        PrintStream noop   = new PrintStream(new OutputStream(){public void write(int b) {} });

        // Output images which do not have associated pointing information.
        FileWriter fstream = new FileWriter(outputFolder + File.separator + "pointingNotFound.txt");
        BufferedWriter noPtg = new BufferedWriter(fstream);


        int count = 1;
        for (String filename : imageFiles)
        {
            if (filename.startsWith("#") || filename.trim().length() == 0)
            {
                //Ignore comments and newlines
                continue;
            }

            System.out.println("Image " + count++ + " of " + imageFiles.size() + ": " + filename);
            String ext = FilenameUtils.getExtension(filename).trim();
            if (ext.compareToIgnoreCase("fit")!=0 && ext.compareToIgnoreCase("fits")!=0)
            {
                System.err.println("   BackplanesGenerator.java: Unexpected image format for file " + filename + ". Expecting FITS image");
                System.exit(0);
            }

            PerspectiveImage image = null;
            ImageKey key = null;

            ImagingInstrument imager = null;
            for (int i = 0; i < ((SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments.length; i++)
            {
                if (instr.equals(((SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[i].instrumentName))
                {
                    imager = ((SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[i];
                }
            }

            if (ptg != null)
            {
                try
                {
                    key = new ImageKey(filename.replace("." + ext, ""), ptg, imager);
                    System.setOut(noop);
                    System.setErr(noop);
                    image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                }
                catch (Exception e)
                {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                    System.out.println("   " + ptg.name() + " pointing not found for " + filename);
                    noPtg.write(filename);
                    noPtg.newLine();
                    continue;
                }
            }
            else
            {
                // Try Gaskell pointing first and if that fails try SPICE pointing. TBD: update this
                // to loop through ImageSource.values().
                try
                {
                    key = new ImageKey(filename.replace("." + ext, ""), ImageSource.GASKELL, imager);
                    System.setOut(noop);
                    System.setErr(noop);
                    image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                }
                catch (Exception e)
                {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                    try
                    {
                        key = new ImageKey(filename.replace("." + ext, ""), ImageSource.CORRECTED, imager);
                        System.setOut(noop);
                        System.setErr(noop);
                        image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                    }
                    catch (Exception e1)
                    {
                        System.setOut(oldOut);
                        System.setErr(oldErr);
                        try
                        {
                            key = new ImageKey(filename.replace("." + ext, ""), ImageSource.SPICE, imager);
                            System.setOut(noop);
                            System.setErr(noop);
                            image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                        }
                        catch (Exception e2)
                        {
                            System.setOut(oldOut);
                            System.setErr(oldErr);
                            try
                            {
                                key = new ImageKey(filename.replace("." + ext, ""), ImageSource.CORRECTED_SPICE, imager);
                                System.setOut(noop);
                                System.setErr(noop);
                                image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                            }
                            catch (Exception e3)
                            {
                                System.setOut(oldOut);
                                System.setErr(oldErr);
                                System.out.println("   Pointing not found for " + filename);
                                noPtg.write(filename);
                                noPtg.newLine();
                                continue;
                            }
                        }
                    }
                }
            }
            System.setOut(oldOut);
            System.setErr(oldErr);

           image.loadFootprint();
            if (image.getUnshiftedFootprint().GetNumberOfCells() == 0)
            {
                System.out.println("   Skipping this image since no intersecting cells");
                image.Delete();
                System.gc();
//                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
//                System.out.println(" ");
//                System.out.println(" ");
                continue;
            }

            // Generate the backplanes binary data
            float[] backplanes = image.generateBackplanes();

            // Prepare folders for output backplanes files and labels
            new File(outputFolder).mkdirs();

            // Backplanes file name
            String baseFilename = getBaseFilename(key, outputFolder);
            String ddrFilename = baseFilename + fmt.getExtension();

            // Write backplanes data to the appropriate format (FITS or IMG)
            fmt.getFile().write(backplanes, new File(filename).getName(), ddrFilename, image.getImageWidth(), image.getImageHeight(), image.getNumBackplanes());

            // Write the backplanes file label.
            image.generateBackplanesLabel(new File(ddrFilename), new File(baseFilename));

            filesProcessed.add(ddrFilename);
            System.out.println("Wrote backplanes to " + new File(ddrFilename).getAbsolutePath());

            image.Delete();
            System.gc();
//            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
//            debugLeaks.PrintCurrentLeaks();
//            System.out.println("\n\n");
        }

        noPtg.close();
        System.out.println("   Total number of files processed " + filesProcessed.size());
    }

    /**
     * Generate base backplanes filename given the parameters. Currently there is only 1 file naming convention, but this method allows for
     * different filenaming conventions based on which PerspectiveImage subclass is being processed.
     *
     * @param image -PerspectiveImage subclass being processed. This may affect naming convention.
     * @param key - base filename.
     * @param resolutionLevel - resolution level
     * @param fmt - output file format
     * @param outputFolder - destination folder for output file
     * @return base name of ddr backplanes file, no extension
     */
    public static String getBaseFilename(ImageKey key, String outputFolder)
    {
        //key class is initialized with base filename (i.e. no ".extension")
        String fname = new File(key.name).getName();

//        return outputFolder + File.separator + fname + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr";
        //PDS requested a simpler naming convention and to remove the obsolete reference to DDR (see email from Carol Neese, 8/26/16 3:41 PM)
        //Use the suffix "BP" appended to the original image name to indicate that this is a backplanes file.
        return outputFolder + File.separator + fname + "_BP";
    }

    /**
     * Generates FITS format backplanes for a single image.
     *
     * @param imageFile - FITS 2D image
     * @param instr
     * @param outputFolder
     * @param model
     * @param fmt
     * @throws IOException
     */
    public void generateBackplanes(String imageFile, Instrument instr, String outputFolder, SmallBodyModel model, BackplanesFileFormat fmt, ImageSource src) throws Exception
    {
    	List<String> image = new ArrayList<>();
        image.add(imageFile);
        this.smallBodyModel = model;
        generateBackplanes(image, instr, outputFolder, fmt, src);
    }

    private void printUsage()
    {
        String o = "This program generates blackplanes for a single image or a list of image files.\n"
                + "A separate backplane file for each shape model resolution level is generated.\n\n"
                + "Usage: BackplanesGenerator [options] <body> <image-list> <output-folder>\n\n"
                + "Where:\n"
                + "  <body>                   The name of the body. If not recognized, the program\n"
                + "                           will list the possible bodies.\n"
                + "  <image-list>             Path to file listing the images to use. Images are\n"
                + "                           specified relative to the /project/nearsdc/data folder.\n"
                + "                           (e.g. /MSI/2000/116/cifdbl/M0132067419F1_2P_CIF_DBL)\n"
                + "  <output-folder>          Path to folder which where to place generated backplanes.\n\n"
                + "Options:\n"
                + "  -c <camera>              The name of the imaging instrument. Default value is the\n"
                + "                           first one stored internally in SBMT for the body. If not\n"
                + "                           recognized, the program will list possible cameras.\n"
                + "  -v <version>             The body shape model version. This string must be identical to\n"
                + "                           one of the versions for the body in SBMT's configuration.\n"
                + "                           If only one version exists, then this input is not needed.\n"
                + "  -r <resolution>          Shape model resolution for which the backplanes are generated.\n"
                + "                           Resolution is an integer value ranging from 0 (lowest resolution)\n"
                + "                           to 3 (highest resolution). Default is to generate backplanes for\n"
                + "                           all four resolutions.\n"
                + "  -p <pointing type>       Pointing type. Allowed values are (case insensitive)\n"
                + ImageSource.printSources(34)
                + "                           Default is to search for GASKELL pointing first and if not found,\n"
                + "                           to traverse the other pointing types in the order specified above.\n"
                + "                           Backplanes are generated only for images with pointing information.\n"
                + "  -s                       Process a single file only. In this case, parameter <image-list>\n"
                + "                           is the name of a single image file, path is relative to\n"
                + "                           /project/nearsdc/data/.\n"
                + "  -f                       Save backplanes as FITS file. Default is IMG.\n"
                + "Example:\n"
                + "BackplanesGenerator.sh -r 3 -p GASKELL -f -s eros /GASKELL/EROS/MSI/images/M0126589036F4_2P_IOF_DBL.FIT .\n";
        System.out.println(o);
        System.exit(0);
    }

    public void doMain(String[] args) throws IOException
    {
        String bodyStr;
        String imageFileList;
        String outputFolder;
        String camera = null;
        String version = null;
        Integer resolution = null;
        ShapeModelBody body = null;
        Instrument instr = null;
        BackplanesFileFormat fmt = BackplanesFileFormat.IMG;
        ImageSource ptg = null;
        boolean singleImage = false;

        int i;
        for (i = 0; i < args.length; ++i)
        {
            if (args[i].equals("-c"))
            {
                camera = args[++i];
            }
            else if (args[i].equals("-v"))
            {
                version = args[++i];
            }
            else if (args[i].equals("-r"))
            {
                resolution = Integer.valueOf(args[++i]);
            }
            else if (args[i].equals("-f"))
            {
                fmt = BackplanesFileFormat.FITS;
            }
            else if (args[i].equals("-p"))
            {
                ptg = ImageSource.valueOf(args[++i].toUpperCase());
            }
            else if (args[i].equals("-s"))
            {
                singleImage = true;
            }
            else
            {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options.
        // Otherwise abort.
        int numberRequiredArgs = 3;
        if (args.length - i != numberRequiredArgs)
        {
            String argStr = new String();
            for (String s : args)
            {
                argStr = argStr + " " + s;
            }
            System.out.println("Incorrectly formed arguments: " + argStr + "\n");
            printUsage();
        }

        bodyStr = args[i++];
        imageFileList = args[i++];
        outputFolder = args[i++];
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

        // Parse parameters to create SBMT classes.
        try
        {
            body = ShapeModelBody.valueOf(bodyStr.toUpperCase());
        }
        catch (Exception e)
        {
            System.err.println("Body " + bodyStr + " not found. Body must be one of: ");
            for (ShapeModelBody s : ShapeModelBody.values())
            {
                System.err.println("   " + s.name());
            }
            System.err.println("Exiting.");
            System.exit(0);
        }

        try
        {
            if (camera != null)
            {
                try
                {
                    instr = Instrument.valueOf(camera.toUpperCase());
                }
                catch (Exception e)
                {
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Instrument " + camera + " for body " + body + " not found, exiting.");
            System.exit(0);
        }
        smallBodyModel = SbmtModelFactory.createSmallBodyModel(SmallBodyViewConfig.getSmallBodyConfig(body, ShapeModelAuthor.GASKELL, version));
        if (instr == null)
        {
            instr = ((SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[0].instrumentName;
        }

        // Information for user.
        System.out.println("Body " + body.name());
        System.out.println("Version " + version);
        System.out.println("Imager " + instr);
        if (singleImage)
        {
            System.out.println("Image file " + imageFileList);
        }
        else
        {
            System.out.println("Image file list " + imageFileList);
        }
        System.out.println("Pointing type " + ptg);
        System.out.println("Output format " + fmt.name());
        System.out.println("Output folder " + outputFolder);
        System.out.println();

        // Read image file list and process image backplanes.
        PerspectiveImage.setGenerateFootprint(true);
        List<String> imageFiles = null;
        if (singleImage)
        {
            imageFiles = new ArrayList<>();
            imageFiles.add(imageFileList);
        }
        else
        {
            try {
                imageFiles = FileUtil.getFileLinesAsStringList(imageFileList);
            } catch (IOException e2) {
                System.out.println("Error reading image file list, " + imageFileList);
                e2.printStackTrace();
            }
        }

        try
        {
            if (resolution != null)
            {
                System.out.println("Processing backplanes for resolution " + resolution);
                smallBodyModel.setModelResolution(resolution);
                generateBackplanes(imageFiles, instr, outputFolder, fmt, ptg);
            }
            else
            {
                resolution = smallBodyModel.getNumberResolutionLevels();
                for (int j=0; j<resolution; ++j)
                {
                    System.out.println("Processing backplanes for resolution " + j);
                    smallBodyModel.setModelResolution(j);
                    generateBackplanes(imageFiles, instr, outputFolder, fmt, ptg);
                }
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException
    {
        System.out.println("PWD = " + new File(".").getAbsolutePath());
        new BackplanesGenerator().doMain(args);
    }

}
