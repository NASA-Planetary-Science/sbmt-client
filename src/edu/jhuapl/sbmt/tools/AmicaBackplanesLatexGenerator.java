package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmicaBackplanesLatexGenerator
{
    private static void generateLatex(String name, List<File> files) throws IOException
    {
        String dir = files.get(0).getParentFile().getAbsolutePath();
        FileWriter fstream = new FileWriter(dir + "/" + name);
        BufferedWriter o = new BufferedWriter(fstream);

        o.write("\\documentclass[12pt]{article}\n");

        o.write("\\usepackage{graphicx}\n");
        o.write("\\usepackage{subfig}\n");
        o.write("\\usepackage{fullpage}\n");

        o.write("\\begin{document}\n");

        String[] bands = {
                "Pixel value",
                "X coordinate",
                "Y coordinate",
                "Z coordinate",
                "Latitude",
                "Longitude",
                "Distance from center",
                "Incidence angle",
                "Emission angle",
                "Phase angle",
                "Horiz pixel scale",
                "Vertical pixel scale",
                "Slope",
                "Elevation",
                "Grav acceleration",
                "Grav potential"
        };

        for (File f : files)
        {
            String filename = f.getAbsolutePath();
            o.write("\\begin{figure}\n");
            o.write("  \\begin{center}\n");

            for (int i=0; i<16; ++i)
            {
                String bf = new File(filename.replace('.', '_') + "_" + i + ".jpg").getName().replace("_gz", "");
                o.write("    \\subfloat["+ bands[i] + "]{\\makebox[.25\\textwidth]{\\includegraphics[scale=0.35]{" + bf + "}}}");
                if (i < 15 && ((i+1)%4 == 0))
                    o.write("\\\\");
                o.write("\n");
            }

            o.write("  \\end{center}\n");
            o.write("\\caption{" + new File(filename).getName().replace("_", "\\_").replace(".gz", "") + ", " + getStartTime(filename) + "}\n");
            o.write("\\end{figure}\n");
            o.write("\\clearpage\n");
        }

        o.write("\\end{document}\n");

        o.close();
    }

    private static String getStartTime(String ddrFile)
    {
        try
        {
            String lblFile = null;
            if (ddrFile.endsWith("ddr.img"))
                lblFile = ddrFile.substring(0, ddrFile.length()-4) + ".lbl";
            else if (ddrFile.endsWith("ddr.img.gz"))
                lblFile = ddrFile.substring(0, ddrFile.length()-7) + ".lbl";

            FileInputStream fs = new FileInputStream(lblFile);
            InputStreamReader isr = new InputStreamReader(fs);
            BufferedReader in = new BufferedReader(isr);

            String str;
            while ((str = in.readLine()) != null)
            {
                if (str.startsWith("START_TIME"))
                {
                    String[] tokens = str.split("=");
                    in.close();
                    return tokens[1].trim();
                }
            }

            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }

    private static List<File> getAllDdrFiles(File imageDir)
    {
        List<File> ddrfiles = new ArrayList<File>();
        File[] files = imageDir.listFiles();
        Arrays.sort(files);
        for (File f : files)
        {
            if (f.getAbsolutePath().endsWith("ddr.img") || f.getAbsolutePath().endsWith("ddr.img.gz"))
            {
                ddrfiles.add(f);
            }
        }

        return ddrfiles;
    }

    private static void saveFileList(String name, List<File> files) throws IOException
    {
        String dir = files.get(0).getParentFile().getAbsolutePath();
        FileWriter fstream = new FileWriter(dir + "/" + name);
        BufferedWriter out = new BufferedWriter(fstream);

        for (File f : files)
        {
            String filename = f.getName();
            if (filename.endsWith(".gz"))
                filename = filename.substring(0, filename.length()-3);

            out.write(filename + "\n");
        }

        out.close();
    }


    /**
     * This program generates 2 latex documents which show the 16 backplanes of all images,
     * one set of image backplanes per page. One documents shows only the gaskell images
     * and the other shows the remaining images which used the spice pointing info.
     *
     * In addition, this program creates 2 text files, one listing the backplane filenames for
     * the gaskell images and the other listing the backplane filenames for the remaining
     * images.
     *
     * This program takes to command line arguments. The first is the directory path containing
     * the backplanes. The second is the file path to Gaskell's INERTIAL.TXT file which is needed
     * to distinguish between between the gaskell and non-gaskell images.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        File amicaImagesDir = new File(args[0]);
        String inertialFilename = args[1];

        try
        {
            AmicaBackplanesGenerator.loadInertialFile(inertialFilename);
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }

        List<File> ddrfiles = getAllDdrFiles(amicaImagesDir);

        // partition the list of ddr files into gaskell and pds files. If the file
        // is listed in the INERTIAL.TXT file, then it is a gaskell image. Otherwise
        // it is not.
        List<File> gaskellfiles = new ArrayList<File>();
        List<File> pdsfiles = new ArrayList<File>();
        for (File f : ddrfiles)
        {
            String amicaId = f.getName().substring(3, 13);
            if (AmicaBackplanesGenerator.inertialFileList.contains("N" + amicaId))
                gaskellfiles.add(f);
            else
                pdsfiles.add(f);
        }

        try
        {
            generateLatex("amica_backplanes_summary_gaskell.tex", gaskellfiles);
            generateLatex("amica_backplanes_summary_pds.tex", pdsfiles);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        // Now generate 2 files, one listing the gaskell backplanes and
        // another listing the pds backplanes
        saveFileList("amica_backplanes_file_list_gaskell.txt", gaskellfiles);
        saveFileList("amica_backplanes_file_list_pds.txt", pdsfiles);
    }
}
