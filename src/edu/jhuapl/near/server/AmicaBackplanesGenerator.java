package edu.jhuapl.near.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.vtkGlobalJavaHash;

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
        }

        return true;
    }


    private static void generateBackplanes(ArrayList<String> amicaFiles, AmicaImage.ImageSource amicaSource) throws FitsException, IOException
    {
        int count = 0;
        for (String filename : amicaFiles)
        {
            System.out.println("\n\nstarting amica " + count++ + " / " + amicaFiles.size() + " " + filename);

            boolean filesExist = checkIfAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
            {
                System.out.println("Could not find sumfile");
                continue;
            }

            File origFile = new File(filename);

            AmicaImage image = new AmicaImage(origFile, itokawaModel, amicaSource);

            // Generate the backplanes binary file
            float[] backplanes = image.generateBackplanes();
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

            // Generate the label file
            String ddrLabelFilename = filename.substring(0, filename.length()-4) + "_ddr.lbl";
            out = new FileOutputStream(ddrLabelFilename);
            String lblstr = image.generateBackplanesLabel();
            byte[] bytes = lblstr.getBytes();
            out.write(bytes, 0, bytes.length);
            out.close();

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println("\n\n");
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        NativeLibraryLoader.loadVtkLibraries();

        String amicaFileList=args[0];

        itokawaModel = new Itokawa();
        try {
            itokawaModel.setModelResolution(3);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        AmicaImage.setGenerateFootprint(true);

        ArrayList<String> amicaFiles = null;
        try {
            amicaFiles = FileUtil.getFileLinesAsStringList(amicaFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            generateBackplanes(amicaFiles, ImageSource.GASKELL);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
