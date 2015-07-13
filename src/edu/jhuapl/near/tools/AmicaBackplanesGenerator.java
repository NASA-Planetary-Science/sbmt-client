package edu.jhuapl.near.tools;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import nom.tam.fits.FitsException;

import vtk.vtkObject;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class AmicaBackplanesGenerator
{
    private static SmallBodyModel itokawaModel;

    // Files listed in Gaskell's INERTIAL.TXT file. Only these are processed.
    static ArrayList<String> inertialFileList = new ArrayList<String>();

    private static double[] meanPlateSizes;

    private static ArrayList<String> filesProcessed = new ArrayList<String>();

    private static int currentCroppedWidth = -1;
    private static int currentCroppedHeight = -1;

    static void loadInertialFile(String inertialFilename) throws IOException
    {
        InputStream fs = new FileInputStream(inertialFilename);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String line;

        while ((line = in.readLine()) != null)
        {
            if (line.startsWith("N"))
            {
                String[] tokens = line.trim().split("\\s+");
                inertialFileList.add(tokens[0]);
            }
        }

        System.out.println("number of inertial files: " + inertialFileList.size());

        in.close();
    }

    private static boolean checkIfAmicaFilesExist(String line, MSIImage.ImageSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

        // If source is Gaskell, only process it if a sumfile exists.
        // If source is SPICE, only process it if an infofile exists and a sumfile DOES NOT exist.

        // First check if it's a valid Gaskell file
        File amicarootdir = (new File(line)).getParentFile().getParentFile();
        System.out.println(line);
        String amicaId = (new File(line)).getName().substring(3, 13);
        String name = amicarootdir.getAbsolutePath() + "/sumfiles/N" + amicaId + ".SUM";
        System.out.println(name);
        file = new File(name);
        boolean hasSumfile = file.exists();
        // Only process files that are listed in Gaskell's INERTIAL.TXT file.
        if (!inertialFileList.contains("N" + amicaId))
        {
            System.out.println("N" + amicaId + " not in INERTIAL.TXT");
            hasSumfile = false;
        }

        // Next check if it's a valid infofile file
        String filename = (new File(line)).getName();
        amicaId = filename.substring(0, filename.indexOf('.'));
        name = amicarootdir.getAbsolutePath() + "/infofiles/" + amicaId + ".INFO";
        System.out.println(name);
        file = new File(name);
        boolean hasInfofile = file.exists();

        if (source.equals(ImageSource.GASKELL))
            return hasSumfile;
        else
            return !hasSumfile && hasInfofile;
    }

    /*
     * This function was used to crop the backplanes after they had been generated which was a requirment
     * by PDS for archiving the backplanes. Future calls to generateBackplanes should produce cropped
     * backplanes, so if regenerating the backplanes from scratch, then this function should not be
     * needed.
    private static void cropGeneratedBackplanes(ArrayList<String> amicaFiles, AmicaImage.ImageSource amicaSource) throws FitsException, IOException
    {
        int count = 0;
        for (String filename : amicaFiles)
        {
            System.out.println("\n\n----------------------------------------------------------------------");
            System.out.println("\n\n----------second pass");
            System.out.println("starting amica " + count++ + " / " + amicaFiles.size() + " " + filename);

            boolean filesExist = checkIfAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
            {
                System.out.println("Could not find sumfile");
                continue;
            }

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".fit", "");

            ImageKey key = new ImageKey(keyName, amicaSource);


            AmicaImage image = new AmicaImage(key, itokawaModel, false, rootFolder);
            int[] croppedSize = image.getCroppedSize();
            currentCroppedWidth = croppedSize[1];
            currentCroppedHeight = croppedSize[0];

            System.out.println("cropped size: " + croppedSize[1] + " " + croppedSize[0]);

            OutputStream out = null;

            String ddrFilename = filename.substring(0, filename.length()-4) + "_ddr.img";
            if (!(new File(ddrFilename).exists()))
                continue;

            if (croppedSize[0] != 1024 || croppedSize[1] != 1024)
            {
                float[] backplanes = new float[(int)(new File(ddrFilename).length())/4];
                System.out.println(ddrFilename + " " + backplanes.length);
                {
                    // fix backplanes
                    InputStream isr = new FileInputStream(ddrFilename);
                    BufferedInputStream in = new BufferedInputStream(isr);
                    DataInputStream dis = new DataInputStream(in);
                    for (int i=0; i<backplanes.length; ++i)
                    {
                        backplanes[i] = dis.readFloat();
                    }
                    in.close();

                    backplanes = image.cropBackplanes(backplanes);
                }

                // Save out the backplanes
                out = new FileOutputStream(ddrFilename + "-cropped");
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

                // Generate a jpeg for each backplane
                //generateFitsFileForEachBackPlane(backplanes, ddrFilename);
                generateJpegFileForEachBackPlane(backplanes, ddrFilename);
            }

            // Generate the label file
            String ddrLabelFilename = filename.substring(0, filename.length()-4) + "_ddr.lbl";
            out = new FileOutputStream(ddrLabelFilename);
            String lblstr = image.generateBackplanesLabel(ddrFilename);
            byte[] bytes = lblstr.getBytes();
            out.write(bytes, 0, bytes.length);
            out.close();

            filesProcessed.add(ddrFilename);
            System.out.println("Processed " + filesProcessed.size() + " images so far");

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println("\n\n");
        }
    }
    */

    private static void generateBackplanes(ArrayList<String> amicaFiles, AmicaImage.ImageSource amicaSource) throws FitsException, IOException
    {
        // First compute the optimal resolution of all images using the highest
        // resolution shape model
        itokawaModel.setModelResolution(3);
        HashMap<String, Integer> optimalResMap = new HashMap<String, Integer>();
        int count = 0;
        for (String filename : amicaFiles)
        {
            System.out.println("\n\n----------------------------------------------------------------------");
            System.out.println("\n\n----------first pass");
            System.out.println("starting amica " + count++ + " / " + amicaFiles.size() + " " + filename);

            boolean filesExist = checkIfAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
            {
                System.out.println("Could not find sumfile");
                continue;
            }

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, amicaSource);
            AmicaImage image = new AmicaImage(key, itokawaModel, false);
            int[] croppedSize = image.getCroppedSize();
            currentCroppedWidth = croppedSize[1];
            currentCroppedHeight = croppedSize[0];

            // Generate the backplanes binary file
            float[] backplanes = image.generateBackplanes();

            int res = findOptimalResolution(backplanes);

            System.out.println("Optimal resolution " + res);

            optimalResMap.put(filename, res);

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println("\n\n");
        }

        filesProcessed.clear();

        // Now that we know the optimal resolutions to, recompute
        // and save off backplanes
        count = 0;
        for (String filename : amicaFiles)
        {
            System.out.println("\n\n----------------------------------------------------------------------");
            System.out.println("\n\n----------second pass");
            System.out.println("starting amica " + count++ + " / " + amicaFiles.size() + " " + filename);

            boolean filesExist = checkIfAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
            {
                System.out.println("Could not find sumfile");
                continue;
            }

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, amicaSource);

            int res = optimalResMap.get(filename);

            System.out.println("Optimal resolution " + res);

            if (res != itokawaModel.getModelResolution())
            {
                System.out.println("Changing resolution from " +
                        itokawaModel.getModelResolution() + " to " + res);

                itokawaModel.setModelResolution(res);
            }

            AmicaImage image = new AmicaImage(key, itokawaModel, false);
            int[] croppedSize = image.getCroppedSize();
            currentCroppedWidth = croppedSize[1];
            currentCroppedHeight = croppedSize[0];

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
            String ddrFilename = filename.substring(0, filename.length()-4) + "_ddr.img";
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

            // Generate a jpeg for each backplane
            //generateFitsFileForEachBackPlane(backplanes, ddrFilename);
            generateJpegFileForEachBackPlane(backplanes, ddrFilename);

            // Generate the label file
            String ddrLabelFilename = filename.substring(0, filename.length()-4) + "_ddr.lbl";
            out = new FileOutputStream(ddrLabelFilename);
            String lblstr = image.generateBackplanesLabel(ddrFilename);
            byte[] bytes = lblstr.getBytes();
            out.write(bytes, 0, bytes.length);
            out.close();

            filesProcessed.add(ddrFilename);
            System.out.println("Processed " + filesProcessed.size() + " images so far");

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println("\n\n");
        }

        System.out.println("Total number of files processed " + filesProcessed.size());
    }

    /*
    private static void generateFitsFileForEachBackPlane(float[] array, String ddrFilename)
    {
        try
        {
            float[][] data = new float[currentCroppedWidth][currentCroppedHeight];
            int c = 0;
            for (int k=0; k<16; ++k)
            {
                for (int i=0; i<currentCroppedWidth; ++i)
                    for (int j=0; j<currentCroppedHeight; ++j)
                        data[i][j] = array[c++];

                Fits f = new Fits();
                f.addHDU(FitsFactory.HDUFactory(data));
                BufferedFile bf = new BufferedFile(ddrFilename + "_" + k + ".fit", "rw");
                f.write(bf);
                bf.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (FitsException e)
        {
            e.printStackTrace();
        }
    }
     */

    private static void generateJpegFileForEachBackPlane(float[] array, String ddrFilename)
    {
        try
        {
            for (int k=0; k<16; ++k)
            {
                int pixelStart = currentCroppedWidth*currentCroppedHeight*k;
                int pixelEnd = currentCroppedWidth*currentCroppedHeight*(k+1);

                float minValue = Float.MAX_VALUE;
                float maxValue = -Float.MAX_VALUE;
                for (int i=pixelStart; i<pixelEnd; ++i)
                {
                    if (array[i] == PerspectiveImage.PDS_NA) continue;
                    if (array[i] < minValue) minValue = array[i];
                    if (array[i] > maxValue) maxValue = array[i];
                }

                BufferedImage bi = new BufferedImage(currentCroppedWidth, currentCroppedHeight, BufferedImage.TYPE_INT_RGB);
                int c = pixelStart;
                for (int i=0; i<currentCroppedWidth; ++i)
                    for (int j=0; j<currentCroppedHeight; ++j)
                    {
                        float v = array[c++];
                        if (v == PerspectiveImage.PDS_NA)
                            v = minValue;
                        else
                            v = (v-minValue) * 255.0f / (maxValue - minValue);
                        bi.getRaster().setSample(i, j, 0, v);
                        bi.getRaster().setSample(i, j, 1, v);
                        bi.getRaster().setSample(i, j, 2, v);
                    }

                int jpegWidth = 300;
                int jpegHeight = 300;
                if (currentCroppedWidth < currentCroppedHeight)
                    jpegWidth = (int) Math.round( 300.0 * ( ((double)currentCroppedWidth) / ((double)currentCroppedHeight) ) );
                else if (currentCroppedHeight < currentCroppedWidth)
                    jpegHeight = (int) Math.round( 300.0 * ( ((double)currentCroppedHeight) / ((double)currentCroppedWidth) ) );

                BufferedImage scaledBI = new BufferedImage(jpegWidth, jpegHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaledBI.createGraphics();
                g.drawImage(bi, 0, 0, jpegWidth, jpegHeight, null);
                g.setComposite(AlphaComposite.Src);
                g.dispose();

                File outputfile = new File(ddrFilename.replace('.', '_') + "_" + k + ".jpg");
                ImageIO.write(scaledBI, "jpg", outputfile);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void computeMeanPlateSizeAtAllResolutions() throws IOException
    {
        int numRes = itokawaModel.getNumberResolutionLevels();

        meanPlateSizes = new double[numRes];

        for (int i=0; i<numRes; ++i)
        {
            itokawaModel.setModelResolution(i);

            meanPlateSizes[i] = itokawaModel.computeLargestSmallestMeanEdgeLength()[2];
        }
    }

    private static double computePixelScaleFromBackplanes(float[] bp)
    {
        // Get the average value of the horizontal and vertical pixel scale planes
        // These are planes 10 and 11 (zero based)
        double scale = 0.0;
        int c = 10*currentCroppedWidth*currentCroppedHeight;
        int total = 0;
        for (int k=10; k<=11; ++k)
        {
            for (int i=0; i<currentCroppedWidth; ++i)
                for (int j=0; j<currentCroppedHeight; ++j)
                {
                    double val = bp[c++];
                    if (val != PerspectiveImage.PDS_NA)
                    {
                        scale += val;
                        ++total;
                    }
                }
        }
        scale = scale / (double)total;
        return scale;
    }

    private static int findOptimalResolution(float[] bp)
    {
        // First get the pixel size.
        double pixelScale = computePixelScaleFromBackplanes(bp);

        System.out.println("pixel size: " + pixelScale);
        if (pixelScale <= 0.0)
            System.exit(1);

        int numRes = itokawaModel.getNumberResolutionLevels();
        for (int i=0; i<numRes-1; ++i)
        {
            if (pixelScale >= (meanPlateSizes[i]+meanPlateSizes[i+1])/2.0)
                return i;
        }

        return numRes - 1;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String amicaFileList=args[0];
        String inertialFilename = args[1];
        int mode = Integer.parseInt(args[2]);

        itokawaModel = new Itokawa(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.ITOKAWA, ShapeModelAuthor.GASKELL));

        computeMeanPlateSizeAtAllResolutions();

        AmicaImage.setGenerateFootprint(true);

        ArrayList<String> amicaFiles = null;
        try {
            amicaFiles = FileUtil.getFileLinesAsStringList(amicaFileList);
            loadInertialFile(inertialFilename);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            if (mode == 1 || mode == 0)
            {
                generateBackplanes(amicaFiles, ImageSource.GASKELL);
            }
            else if (mode == 2 || mode == 0)
            {
                generateBackplanes(amicaFiles, ImageSource.SPICE);
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
