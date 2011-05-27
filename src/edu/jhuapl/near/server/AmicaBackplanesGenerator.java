package edu.jhuapl.near.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.vtkGlobalJavaHash;

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
            System.out.println("\n\nstarting amica " + count++ + " / " + amicaFiles.size() + " " + filename);

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

        System.out.println("Total number of files processed " + numberValidFiles);
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
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
