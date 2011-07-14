package edu.jhuapl.near.server;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import nom.tam.fits.FitsException;

import vtk.vtkGlobalJavaHash;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
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
    private static ArrayList<String> inertialFileList = new ArrayList<String>();

    private static int numberValidFiles = 0;

    private static double[] meanPlateSizes;

    private static ArrayList<String> filesProcessed = new ArrayList<String>();

    private static void loadInertialFile(String inertialFilename) throws IOException
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

        // Check for the sumfile if source is Gaskell
        if (source.equals(ImageSource.GASKELL))
        {
            File amicarootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String amicaId = (new File(line)).getName().substring(3, 13);
            String name = amicarootdir.getAbsolutePath() + "/sumfiles/N" + amicaId + ".SUM";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;

            // Only process files that are listed in Gaskell's INERTIAL.TXT file.
            if (!inertialFileList.contains("N" + amicaId))
            {
                System.out.println("N" + amicaId + " not in INERTIAL.TXT");
                return false;
            }
        }

        return true;
    }

    private static void generateBackplanes(ArrayList<String> amicaFiles, AmicaImage.ImageSource amicaSource) throws FitsException, IOException
    {
        int count = 0;
        for (String filename : amicaFiles)
        {
            System.out.println("\n\n----------------------------------------------------------------------");
            System.out.println("starting amica " + count++ + " / " + amicaFiles.size() + " " + filename);

            boolean filesExist = checkIfAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
            {
                System.out.println("Could not find sumfile");
                continue;
            }

            ++numberValidFiles;

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, amicaSource);
            AmicaImage image = new AmicaImage(key, itokawaModel, false, rootFolder);

            // Generate the backplanes binary file
            float[] backplanes = image.generateBackplanes();

            int res = findOptimalResolution(backplanes);

            System.out.println("Optimal resolution " + res);

            if (res == 3)
                res = 2;

            // If we used the wrong resolution, recompute the blackplanes using the right one.
            if (res != itokawaModel.getModelResolution())
            {
                System.out.println("Changing resolution to " + res);
                itokawaModel.setModelResolution(res);
                image.Delete();
                System.gc();
                System.out.println("deleted " + vtkGlobalJavaHash.GC());

                image = new AmicaImage(key, itokawaModel, false, rootFolder);
                backplanes = image.generateBackplanes();
            }

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
            String lblstr = image.generateBackplanesLabel();
            byte[] bytes = lblstr.getBytes();
            out.write(bytes, 0, bytes.length);
            out.close();

            filesProcessed.add(ddrFilename);

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println("\n\n");
        }

        System.out.println("Total number of files processed " + numberValidFiles);
    }

    /*
    private static void generateFitsFileForEachBackPlane(float[] array, String ddrFilename)
    {
        try
        {
            float[][] data = new float[1024][1024];
            int c = 0;
            for (int k=0; k<16; ++k)
            {
                for (int i=0; i<1024; ++i)
                    for (int j=0; j<1024; ++j)
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
                int pixelStart = 1024*1024*k;
                int pixelEnd = 1024*1024*(k+1);

                float minValue = Float.MAX_VALUE;
                float maxValue = -Float.MAX_VALUE;
                for (int i=pixelStart; i<pixelEnd; ++i)
                {
                    if (array[i] == Image.PDS_NA) continue;
                    if (array[i] < minValue) minValue = array[i];
                    if (array[i] > maxValue) maxValue = array[i];
                }

                BufferedImage bi = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
                int c = pixelStart;
                for (int i=0; i<1024; ++i)
                    for (int j=0; j<1024; ++j)
                    {
                        float v = array[c++];
                        if (v == Image.PDS_NA)
                            v = minValue;
                        else
                            v = (v-minValue) * 255.0f / (maxValue - minValue);
                        bi.getRaster().setSample(i, j, 0, v);
                        bi.getRaster().setSample(i, j, 1, v);
                        bi.getRaster().setSample(i, j, 2, v);
                    }

                BufferedImage scaledBI = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaledBI.createGraphics();
                g.drawImage(bi, 0, 0, 300, 300, null);
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

    private static void generateLatex() throws IOException
    {
        String dir = new File(filesProcessed.get(0)).getParentFile().getAbsolutePath();
        FileWriter fstream = new FileWriter(dir + "/amica_backplanes_summary.tex");
        BufferedWriter o = new BufferedWriter(fstream);

        o.write("\\documentclass[12pt]{article}\n");

        o.write("\\usepackage{graphicx}\n");
        o.write("\\usepackage{subfigure}\n");
        o.write("\\usepackage{fullpage}\n");

        o.write("\\begin{document}\n");

        String[] bands = {
                "pixel value",
                "x coordinate",
                "y coordinate",
                "z coordinate",
                "Latitude",
                "Longitude",
                "Distance from center",
                "Incidence angle",
                "Emission angle",
                "Phase angle",
                "Horizontal pixel scale",
                "Vertical pixel scale",
                "Slope",
                "Elevation",
                "Gravitational Acc",
                "Gravitational Pot"
        };

        for (String filename : filesProcessed)
        {
            o.write("\\begin{figure}\n");
            o.write("  \\begin{center}\n");

            for (int i=0; i<16; ++i)
            {
                String bf = filename.replace('.', '_') + "_" + i + ".jpg";
                o.write("    \\subfigure["+ bands[i] + "]{\\includegraphics[scale=0.35]{" + bf + "}}\n");
            }

            o.write("  \\end{center}\n");
            o.write("\\caption{" + new File(filename).getName().replace("_", "\\_") + "}\n");
            o.write("\\end{figure}\n");
            o.write("\\clearpage\n");
            o.write("\\setcounter{subfigure}{0}\n");
        }

        o.write("\\end{document}\n");

        o.close();
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
        int c = 10*1024*1024;
        int total = 0;
        for (int k=10; k<=11; ++k)
        {
            for (int i=0; i<1024; ++i)
                for (int j=0; j<1024; ++j)
                {
                    double val = bp[c++];
                    if (val != Image.PDS_NA)
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
        NativeLibraryLoader.loadVtkLibraries();

        String amicaFileList=args[0];
        String inertialFilename = args[1];

        itokawaModel = new Itokawa();

        computeMeanPlateSizeAtAllResolutions();

        try {
            itokawaModel.setModelResolution(0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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
            generateBackplanes(amicaFiles, ImageSource.GASKELL);
            generateLatex();
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
