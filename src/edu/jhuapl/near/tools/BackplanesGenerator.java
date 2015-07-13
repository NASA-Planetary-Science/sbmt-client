package edu.jhuapl.near.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.vtkDebugLeaks;
import vtk.vtkObject;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;


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
            String outputFolder) throws FitsException, IOException
    {
        filesProcessed.clear();

        int resolutionLevel = smallBodyModel.getModelResolution();

        vtkDebugLeaks debugLeaks = new vtkDebugLeaks();

        int count = 1;
        for (String filename : imageFiles)
        {
            System.out.println("----------------------------------------------------------------------");
            System.out.println("starting image " + count++ + " / " + imageFiles.size() + " " + filename);

            // Try Gaskell pointing first and if that fails try SPICE pointing
            PerspectiveImage image = null;
            ImageKey key = null;
            try
            {
                key = new ImageKey(filename.replace(".fit", ""), ImageSource.GASKELL);
                image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
            }
            catch (Exception e)
            {
                try
                {
                    key = new ImageKey(filename.replace(".fit", ""), ImageSource.SPICE);
                    image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
                }
                catch (Exception e1)
                {
                    continue;
                }
            }

            image.loadFootprint();
            if (image.getUnshiftedFootprint().GetNumberOfCells() == 0)
            {
                System.out.println("skipping this image since no intersecting cells");
                image.Delete();
                System.gc();
                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
                System.out.println(" ");
                System.out.println(" ");
                continue;
            }

            // Generate the backplanes binary file
            float[] backplanes = image.generateBackplanes();

            // Save out the backplanes
            new File(outputFolder).mkdirs();

            String source = "";
            if (key.source == ImageSource.GASKELL)
                source = "Gaskell";
            else if (key.source == ImageSource.SPICE)
                source = "SPICE";

            String fname = new File(filename).getName();
            String ddrFilename = outputFolder + "/" + fname.substring(0, fname.length()-4) + "_" + source + "_res" + resolutionLevel + "_ddr.img";
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
            String ddrLabelFilename = outputFolder + "/" + fname.substring(0, fname.length()-4) + "_" + source + "_res" + resolutionLevel + "_ddr.lbl";
            out = new FileOutputStream(ddrLabelFilename);
            String lblstr = image.generateBackplanesLabel(new File(ddrFilename).getName());
            byte[] bytes = lblstr.getBytes();
            out.write(bytes, 0, bytes.length);
            out.close();

            filesProcessed.add(ddrFilename);
            System.out.println("Processed " + filesProcessed.size() + " images so far");

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            debugLeaks.PrintCurrentLeaks();
            System.out.println("\n\n");
        }

        System.out.println("Total number of files processed " + filesProcessed.size());
    }


    /**
     * @param args
     * @throws IOException
     */
    public void doMain(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String body = args[0];

        String imageFileList=args[1];

        String outputFolder = args[2];

        if (body.toUpperCase().equals("EROS"))
            smallBodyModel = ModelFactory.createSmallBodyModel(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL));
        else if (body.toUpperCase().equals("ITOKAWA"))
            smallBodyModel = ModelFactory.createSmallBodyModel(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.ITOKAWA, ShapeModelAuthor.GASKELL));

        PerspectiveImage.setGenerateFootprint(true);

        ArrayList<String> imageFiles = null;
        try {
            imageFiles = FileUtil.getFileLinesAsStringList(imageFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            int numberResolutionLevels = smallBodyModel.getNumberResolutionLevels();
            for (int i=0; i<numberResolutionLevels; ++i)
            {
                smallBodyModel.setModelResolution(i);
                generateBackplanes(imageFiles, outputFolder);
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
