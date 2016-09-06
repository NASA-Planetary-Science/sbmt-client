package edu.jhuapl.near.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkDebugLeaks;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.util.BackplanesFile;
import edu.jhuapl.near.util.FitsBackplanesFile;
import edu.jhuapl.near.util.ImgBackplanesFile;
import edu.jhuapl.saavtk.model.ImageSource;
import edu.jhuapl.saavtk.model.ImagingInstrument;
import edu.jhuapl.saavtk.model.Instrument;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;


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

    private ArrayList<String> filesProcessed = new ArrayList<String>();

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
            ArrayList<String> imageFiles,
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
                System.err.println("   Unexpected image format. Expecting FITS image");
                System.exit(0);
            }

            PerspectiveImage image = null;
            ImageKey key = null;

            ImagingInstrument imager = null;
            for (int i = 0; i < ((SmallBodyConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments.length; i++)
            {
                if (instr.equals(((SmallBodyConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[i].instrumentName))
                {
                    imager = ((SmallBodyConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[i];
                }
            }

            if (ptg != null)
            {
                try
                {
                    key = new ImageKey(filename.replace("." + ext, ""), ptg, imager);
                    System.setOut(noop);
                    System.setErr(noop);
                    image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
                }
                catch (Exception e)
                {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                    System.out.println("   Gaskell pointing not found for " + filename);
                    noPtg.write(filename);
                    noPtg.newLine();
                    continue;
                }
            }
            else
            {
                // Try Gaskell pointing first and if that fails try SPICE pointing
                try
                {
                    key = new ImageKey(filename.replace("." + ext, ""), ImageSource.GASKELL, imager);
                    System.setOut(noop);
                    System.setErr(noop);
                    image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
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
                        image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
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
                            image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
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
                                image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
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

            // Generate the backplanes binary file
            float[] backplanes = image.generateBackplanes();

            // Save out the backplanes
            new File(outputFolder).mkdirs();

            //determine output ddr filename
            String ddrFilename = getddrFilename(image, key, resolutionLevel, fmt, outputFolder);

            //Write data to the appropriate format (FITS or IMG)

            fmt.getFile().write(backplanes, new File(filename).getName(), ddrFilename, image.getImageWidth(), image.getImageHeight(), image.getNumBackplanes());

            if (image instanceof MSIImage) {
                System.out.println("This is an MSI Image");
            }

            //determine output ddr labelFilename
            String ddrLabelFilename = getddrLblname(image, key, resolutionLevel, fmt, outputFolder);

            // Generate the label
//          String ddrLabelFilename = outputFolder + File.separator + fname.substring(0, fname.length()-4) + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr.lbl";
//          image.generateBackplanesLabel(ddrFilename, ddrLabelFilename);
            generateLabel(image, key, new File(ddrFilename), new File(ddrLabelFilename));

            filesProcessed.add(ddrFilename);
            System.out.println("   Wrote backplanes to " + ddrFilename);

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
     * Generate ddr filename given the parameters. Currently there is only 1 file naming convention, but this method allows for
     * different filenaming conventions based on which PerspectiveImage subclass is being processed.
     *
     * @param image -PerspectiveImage subclass being processed. This may affect naming convention.
     * @param key - base filename.
     * @param resolutionLevel - resolution level
     * @param fmt - output file format
     * @param outputFolder - destination folder for output file
     * @return
     */
    private String getddrFilename(PerspectiveImage image, ImageKey key, int resolutionLevel, BackplanesFileFormat fmt, String outputFolder) {

        //key class is initialized with base filename (i.e. no ".extension")
        String fname = new File(key.name).getName();

//        String ddrFilename = outputFolder + File.separator + fname.substring(0, fname.length()-4) + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr." + fmt.getExtension();
        String ddrFilename = outputFolder + File.separator + fname + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr." + fmt.getExtension();

        return ddrFilename;

    }

    /**
     * Generate ddr label filename given the parameters. Currently there are two file naming conventions, one for PDS3 labels, and one for
     * PDS4 xml labels. The naming convention is based on the PerspectiveImage subclass being processed.
     * @param image -PerspectiveImage subclass being processed. This may affect naming convention.
     * @param key - base filename.
     * @param resolutionLevel - resolution level
     * @param fmt - output file format
     * @param outputFolder - destination folder for output file
     * @return
     */
    private String getddrLblname(PerspectiveImage image, ImageKey key, int resolutionLevel, BackplanesFileFormat fmt, String outputFolder) {

        //key class is initialized with base filename (i.e. no ".extension")
        String fname = new File(key.name).getName();

//        String ddrFilename = outputFolder + File.separator + fname.substring(0, fname.length()-4) + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr." + fmt.getExtension();
//      String ddrLabelFilename = outputFolder + File.separator + fname.substring(0, fname.length()-4) + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr.lbl";

        String ddrLabelFilename = "";
        if (image instanceof MSIImage) {
            System.out.println("This is an MSI Image");
             ddrLabelFilename = outputFolder + File.separator + fname + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr.xml";
        } else {
             ddrLabelFilename = outputFolder + File.separator + fname + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr.lbl";
        }

        return ddrLabelFilename;

    }


    /**
     * Generate the label file. Allows for different, potentially more complicated methods to be called depending on PerspectiveImage subclass
     * @param image
     * @param key
     * @param ddrFilename - full path to the DDR file.
     * @param ddrLabelFilename - full path to the DDR label file.
     * @throws IOException
     */
    private void generateLabel(PerspectiveImage image, ImageKey key, File ddrFilename, File ddrLabelFilename) throws IOException {

//        if (image instanceof MSIImage) {
//
//        } else {
            image.generateBackplanesLabel(ddrFilename, ddrLabelFilename);
//        }
    }


    private void authenticate()
    {
        Configuration.setAPLVersion(true);

        String username = null;
        String password = null;

        try
        {
            // First try to see if there's a password.txt file in ~/.neartool. Then try the folder
            // containing the runsbmt script.
            String jarLocation = SmallBodyMappingToolAPL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String parent = new File(jarLocation).getParentFile().getParent();
            String[] passwordFilesToTry = {
                    Configuration.getApplicationDataDir() + File.separator + "password.txt",
                    parent + File.separator + "password.txt"
            };

            for (String passwordFile : passwordFilesToTry)
            {
                if (new File(passwordFile).exists())
                {
                    ArrayList<String> credentials = FileUtil.getFileLinesAsStringList(passwordFile);
                    if (credentials.size() >= 2)
                    {
                        String user = credentials.get(0);
                        String pass = credentials.get(1);

                        if (user != null && user.trim().length() > 0 && !user.trim().toLowerCase().contains("replace-with-") &&
                            pass != null && pass.trim().length() > 0)
                        {
                            username = user.trim();
                            password = pass.trim();
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        }

        if (username != null && password != null)
        {
            Configuration.setupPasswordAuthentication(username, password);
        }
        else
        {
            System.out.println("Warning: no correctly formatted password file found. "
                    + "Continuing without password. Certain functionality may not work.");
        }
    }

    /**
     * Generates FITS format backplanes for a single image using GASKELL pointing.
     *
     * @param imageFile - FITS 2D image
     * @param instr
     * @param outputFolder
     * @param model
     * @param fmt
     * @throws IOException
     */
    public void generateBackplanes(String imageFile, Instrument instr, String outputFolder, SmallBodyModel model, BackplanesFileFormat fmt) throws Exception
    {
        ArrayList<String> image = new ArrayList<>();
        image.add(imageFile);
        this.smallBodyModel = model;
        generateBackplanes(image, instr, outputFolder, fmt, ImageSource.GASKELL);
    }

    private void printUsage()
    {
        String o = "This program generates blackplanes for a list of image files. A separate\n"
                + "backplane for each resolution level is generated.\n\n"
                + "Usage: BackplanesGenerator [options] <body> <image-list> <output-folder>\n\n"
                + "Where:\n"
                + "  <body>                   The name of the body. If not recognized, the program\n"
                + "                           will list the possible bodies.\n"
                + "  <image-list>             Path to to file listing the images to use. Images are\n"
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
                + "  -p <pointing type>       Pointing type. Must be one of\n"
                + "                           GASKELL,\n"
                + "                           CORRECTED,\n"
                + "                           SPICE,\n"
                + "                           CORRECTED_SPICE.\n"
                + "                           (case insensitive). Default is to search for GASKELL pointing\n"
                + "                           first, and if not found to traverse the other pointing types in\n"
                + "                           the order specified above. Backplanes are generated only for\n"
                + "                           images with pointing information.\n"
                + "  -f                       Save backplanes as FITS file. Default is IMG.\n";
        System.out.println(o);
        System.exit(1);
    }

    public enum BackplanesFileFormat
    {
        FITS(new FitsBackplanesFile(), "fit"), IMG(new ImgBackplanesFile(), "img");

        private BackplanesFile file;
        private String extension;

        private BackplanesFileFormat(BackplanesFile file, String extension)
        {
            this.file = file;
            this.extension = extension;
        }

        public BackplanesFile getFile()
        {
            return file;
        }

        public String getExtension()
        {
            return extension;
        }
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
            System.out.println("Incorrectly formed arguments.\n");
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
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();
        authenticate();

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
        smallBodyModel = ModelFactory.createSmallBodyModel(SmallBodyConfig.getSmallBodyConfig(body, ShapeModelAuthor.GASKELL, version));
        if (instr == null)
        {
            instr = ((SmallBodyConfig)smallBodyModel.getSmallBodyConfig()).imagingInstruments[0].instrumentName;
        }

        // Information for user.
        System.out.println("Body " + body.name());
        System.out.println("Version " + version);
        System.out.println("Imager " + instr);
        System.out.println("Image file list " + imageFileList);
        System.out.println("Pointing type " + ptg);
        System.out.println("Output format " + fmt.name());
        System.out.println("Output folder " + outputFolder);
        System.out.println();

        // Read image file list and process image backplanes.
        PerspectiveImage.setGenerateFootprint(true);
        ArrayList<String> imageFiles = null;
        try {
            imageFiles = FileUtil.getFileLinesAsStringList(imageFileList);
        } catch (IOException e2) {
            System.out.println("Error reading image file list, " + imageFileList);
            e2.printStackTrace();
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
