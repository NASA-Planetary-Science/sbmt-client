package edu.jhuapl.near.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkDebugLeaks;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.Instrument;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

import nom.tam.fits.FitsException;


/**
 * This program generates blackplanes for a list of image files. A separate
 * backplane for each resolution level is generated. Currently MSI and Itokawa
 * images only are supported. User calls this program with 3 arguments as
 * follows:
 *
 * 1. body - either EROS for MSI images or ITOKAWA for AMICA images
 * 2. filelist - path to file listing images to use. Images are specified same way as SBMT
 *               uses to download files. (e.g. /MSI/2000/116/cifdbl/M0132067419F1_2P_CIF_DBL)
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

    private void generateBackplanes(
            ArrayList<String> imageFiles,
            Instrument instr,
            String outputFolder) throws FitsException, IOException
    {
        filesProcessed.clear();

        int resolutionLevel = smallBodyModel.getModelResolution();

        vtkDebugLeaks debugLeaks = new vtkDebugLeaks();

        int count = 1;
        for (String filename : imageFiles)
        {
            filename = filename.toUpperCase(); //TBD remove
            System.out.println("   Image " + count++ + " of " + imageFiles.size() + ": " + filename);
            String ext = FilenameUtils.getExtension(filename).trim();
            if (ext.compareToIgnoreCase("fit")!=0 && ext.compareToIgnoreCase("fits")!=0)
            {
                System.err.println("   Unexpected image format. Expecting FITS image");
                System.exit(0);
            }

            PerspectiveImage image = null;
            ImageKey key = null;

            ImagingInstrument imager = null;
            for (int i = 0; i < smallBodyModel.getSmallBodyConfig().imagingInstruments.length; i++)
            {
                if (instr.equals(smallBodyModel.getSmallBodyConfig().imagingInstruments[i].instrumentName))
                {
                    imager = smallBodyModel.getSmallBodyConfig().imagingInstruments[i];
                }
            }

            //Suppress exceptions if pointing files do not exist for any of the ImageSources
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            PrintStream noop   = new PrintStream(new OutputStream(){public void write(int b) {} });
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
                            continue;
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

            String fname = new File(filename).getName();
            String ddrFilename = outputFolder + "/" + fname.substring(0, fname.length()-4) + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr.img";
            OutputStream out = new FileOutputStream(ddrFilename);
            byte[] buf = new byte[4 * backplanes.length];
            for (int i=0; i<backplanes.length; ++i)
            {
                int v = Float.floatToIntBits(backplanes[i]);
                buf[4*i + 0] = (byte)(v >>> 24);
                buf[4*i + 1] = (byte)(v >>> 16);
                buf[4*i + 2] = (byte)(v >>>  8);
                buf[4*i + 3] = (byte)(v >>>  0);
            }
            out.write(buf, 0, buf.length);
            out.close();

            // Generate the label file
            String ddrLabelFilename = outputFolder + "/" + fname.substring(0, fname.length()-4) + "_" + key.source.name() + "_res" + resolutionLevel + "_ddr.lbl";
            out = new FileOutputStream(ddrLabelFilename);
            String lblstr = image.generateBackplanesLabel(new File(ddrFilename).getName());
            byte[] bytes = lblstr.getBytes();
            out.write(bytes, 0, bytes.length);
            out.close();

            filesProcessed.add(ddrFilename);
            System.out.println("   Image " + fname + " processing complete.");

            image.Delete();
            System.gc();
//            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
//            debugLeaks.PrintCurrentLeaks();
//            System.out.println("\n\n");
        }

        System.out.println("   Total number of files processed " + filesProcessed.size());
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

    public void generateBackplanes(String imageFile, Instrument instr, String outputFolder, SmallBodyModel model) throws FitsException, IOException
    {
        ArrayList<String> image = new ArrayList<>();
        image.add(imageFile);
        this.smallBodyModel = model;
        generateBackplanes(image, instr, outputFolder);
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
                + "                           all four resolutions.\n";
        System.out.println(o);
        System.exit(1);
    }

    /**
     * @param args
     * @throws IOException
     */
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
            System.out.println("Incorrectly formed arguments. \n");
            printUsage();
        }

        bodyStr = args[i++];
        imageFileList = args[i++];
        outputFolder = args[i++];

        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        authenticate();

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
            instr = smallBodyModel.getSmallBodyConfig().imagingInstruments[0].instrumentName;
        }

        System.out.println("Body " + body.name());
        System.out.println("Version " + version);
        System.out.println("Imager " + instr);
        System.out.println("Image file list " + imageFileList);
        System.out.println("Output folder " + outputFolder);

        PerspectiveImage.setGenerateFootprint(true);

        ArrayList<String> imageFiles = null;
        try {
            imageFiles = FileUtil.getFileLinesAsStringList(imageFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            if (resolution != null)
            {
                System.out.println("Processing backplanes for resolution " + resolution);
                smallBodyModel.setModelResolution(resolution);
                generateBackplanes(imageFiles, instr, outputFolder);
            }
            else
            {
                resolution = smallBodyModel.getNumberResolutionLevels();
                for (int j=0; j<resolution; ++j)
                {
                    System.out.println("Processing backplanes for resolution " + j);
                    smallBodyModel.setModelResolution(j);
                    generateBackplanes(imageFiles, instr, outputFolder);
                }
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException
    {
        new BackplanesGenerator().doMain(args);
    }
}
