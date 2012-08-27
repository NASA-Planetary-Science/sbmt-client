package edu.jhuapl.near.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

public class FileUtil
{
    private static volatile boolean abortUnzip = false;
    private static volatile double unzipProgress = 0.0;

    /**
     * The function takes a file and returns its contents as a list of strings,
     * one line per string.
     * @param filename file to read
     * @return contents of file as list of strings
     * @throws IOException
     */
    public static ArrayList<String> getFileLinesAsStringList(String filename) throws IOException
    {
        InputStream fs = new FileInputStream(filename);
        if (filename.toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        ArrayList<String> lines = new ArrayList<String>();
        String line;

        while ((line = in.readLine()) != null)
        {
            lines.add(line);
        }

        in.close();

        return lines;
    }

    /**
     * The function takes a file and returns its contents as a list of double,
     * assuming the file contains one double value per line.
     * @param filename file to read
     * @return contents of file as list of strings
     * @throws IOException
     */
    public static ArrayList<Double> getFileLinesAsDoubleList(String filename) throws IOException
    {
        InputStream fs = new FileInputStream(filename);
        if (filename.toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        ArrayList<Double> values = new ArrayList<Double>();
        String line;

        while ((line = in.readLine()) != null)
        {
            values.add(Double.parseDouble(line));
        }

        in.close();

        return values;
    }


    /**
     * The function takes a file and returns its contents as a list of strings,
     * one word per string.
     * @param filename file to read
     * @return contents of file as list of strings
     * @throws IOException
     */
    public static ArrayList<String> getFileWordsAsStringList(String filename) throws IOException
    {
        InputStream fs = new FileInputStream(filename);
        if (filename.toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        ArrayList<String> words = new ArrayList<String>();
        String line;

        while ((line = in.readLine()) != null)
        {
            String [] tokens = line.trim().split("\\s+");

            for (String word : tokens)
                words.add(word);
        }

        in.close();

        return words;
    }

    public static ArrayList<String> getFileWordsAsStringList(String filename, String separator) throws IOException
    {
        InputStream fs = new FileInputStream(filename);
        if (filename.toLowerCase().endsWith(".gz"))
            fs = new GZIPInputStream(fs);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        ArrayList<String> words = new ArrayList<String>();
        String line;

        while ((line = in.readLine()) != null)
        {
            String [] tokens = line.trim().split(separator);

            for (String word : tokens)
                words.add(word);
        }

        in.close();

        return words;
    }

    public static void copyInputStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buf = new byte[2048];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    /**
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFile(File inFile, File outFile) throws IOException
    {
        InputStream in = new FileInputStream(inFile);
        OutputStream out = new FileOutputStream(outFile);

        copyInputStream(in, out);
    }

    /**
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFile(String inFile, String outFile) throws IOException
    {
        InputStream in = new FileInputStream(inFile);
        OutputStream out = new FileOutputStream(outFile);

        copyInputStream(in, out);
    }

    /**
     * Copies a range of lines in a file to a new file. E.g. suppose you wanted
     * to copy lines 5 through 10 of a certain file to a new file.
     *
     * @param inFile the file to copy lines from
     * @param outFile the file to copy lines to
     * @param startLine the first line to copy
     * @param endLine the last line to copy
     * @param append if true the lines are appended to outFile. If false, outFile is truncated.
     * @throws IOException
     */
    public static void copyLinesInFile(File inFile, File outFile, int startLine, int endLine, boolean append) throws IOException
    {
        FileReader ifs = new FileReader(inFile);
        BufferedReader in = new BufferedReader(ifs);

        FileWriter ofs = new FileWriter(outFile, append);
        BufferedWriter out = new BufferedWriter(ofs);

        String line;

        int lineNumber = 0;
        while ((line = in.readLine()) != null)
        {
            if (lineNumber >= startLine && lineNumber <= endLine)
            {
                out.write(line + "\n");
            }

            ++lineNumber;
        }

        in.close();
        out.close();
    }

    static public String getTemporarySuffix()
    {
        return ".sbmt_tool";
    }

    /**
     * The following function is adapted from http://www.devx.com/getHelpOn/10MinuteSolution/20447
     *
     * This function assumes the zip file contains a single top level folder of the same name as the
     * zip file without the .zip extension. E.g. if the zip file is called mapmaker.zip, then
     * when unzipped, there will be a single folder called mapmaker in the same folder as mapmaker.zip.
     *
     * @param file
     */
    public static void unzipFile(File file)
    {
        Enumeration entries;
        ZipFile zipFile;

        String zipContainingFolder = file.getParent();
        String zipTopLevelFolderName = file.getName().substring(0, file.getName().length()-4);
        String zipTopLevelFolder = zipContainingFolder + File.separator + zipTopLevelFolderName;
        String tempExtractToFolder = zipTopLevelFolder + getTemporarySuffix();

        try
        {
            FileUtils.deleteQuietly(new File(zipTopLevelFolder));
            FileUtils.deleteQuietly(new File(tempExtractToFolder));

            zipFile = new ZipFile(file);

            // First compute the decompressed size by summing over all entries
            long totalDecompressedSize = 0;
            entries = zipFile.entries();
            while(entries.hasMoreElements())
                totalDecompressedSize += ((ZipEntry)entries.nextElement()).getSize();

            entries = zipFile.entries();

            abortUnzip = false;
            unzipProgress = 0.0;

            boolean unzipAborted = false;
            long numberOfBytesDecompressedSoFar = 0;

            while(entries.hasMoreElements())
            {
                unzipProgress = 100.0 * (double)numberOfBytesDecompressedSoFar / (double)totalDecompressedSize;

                if (abortUnzip)
                {
                    unzipAborted = true;
                    break;
                }

                ZipEntry entry = (ZipEntry)entries.nextElement();

                if(entry.isDirectory())
                {
                    //System.err.println("Extracting directory: " + entry.getName());
                    (new File(tempExtractToFolder + File.separator + entry.getName())).mkdirs();
                }
                else
                {
                    //System.err.println("Extracting file: " + entry.getName());
                    copyInputStream(zipFile.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(tempExtractToFolder + File.separator + entry.getName())));
                }

                numberOfBytesDecompressedSoFar += entry.getSize();
            }

            zipFile.close();

            if (!unzipAborted)
            {
                String tempTopLevelFolder = tempExtractToFolder + File.separator + zipTopLevelFolderName;
                FileUtils.moveDirectory(new File(tempTopLevelFolder), new File(zipTopLevelFolder));
            }

            FileUtils.deleteQuietly(new File(tempExtractToFolder));

            unzipProgress = 100.0;
        }
        catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
    }

    static public void abortUnzip()
    {
        abortUnzip = true;
    }

    static public double getUnzipProgress()
    {
        return unzipProgress;
    }

    static public void resetUnzipProgress()
    {
        unzipProgress = 0.0;
    }
}
