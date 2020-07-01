package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.pointing.Pointing;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;

import crucible.core.mechanics.EphemerisID;
import crucible.core.mechanics.FrameID;
import crucible.core.mechanics.utilities.SimpleEphemerisID;
import crucible.core.mechanics.utilities.SimpleFrameID;
import crucible.core.time.TimeSystems;
import crucible.core.time.UTCEpoch;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCard;

public abstract class ComputeSpicePointing
{
    private static final Pattern MetaKernelPattern = Pattern.compile(".*\\.mk", Pattern.CASE_INSENSITIVE);

    public static ComputeSpicePointing of(String[] args) throws Exception
    {
        int index = 0;

        if (args.length == 1 && (args[0].startsWith("-h") || args[0].startsWith("--h")))
        {
            usage(System.out, 0);
        }
        try
        {
            if (args.length != 10)
            {
                throw new IllegalArgumentException("Must have 10 command line arguments");
            }

            EphemerisID bodyId = new SimpleEphemerisID(args[index++]);
            FrameID bodyFrame = new SimpleFrameID(args[index++]);
            EphemerisID scId = new SimpleEphemerisID(args[index++]);
            int sclkIdCode = Integer.parseInt(args[index++]);
            FrameID instrumentFrame = new SimpleFrameID(args[index++]);

            String inputString = args[index++];
            String mkPathString = args[index++];
            ImmutableList<Path> mkPaths;
            if (MetaKernelPattern.asPredicate().test(mkPathString))
            {
                mkPaths = ImmutableList.of(Paths.get(mkPathString));
            }
            else
            {
                System.err.println("Metakernel file path argument " + mkPathString + " must be a single metakernel file ending with .mk for now");
                throw new UnsupportedOperationException("TODO: implement reader for a list of MK files");
            }

            Path inputDir = Paths.get(args[index++]);
            Path outputDir = Paths.get(args[index++]);
            String fitsTimeKey = args[index++];

            SpicePointingProvider provider = SpicePointingProvider.of(mkPaths, bodyId, bodyFrame, scId, sclkIdCode, instrumentFrame);
            return new ComputeSpicePointing(provider, inputString, inputDir, outputDir, fitsTimeKey) {

            };

        }
        catch (Exception e)
        {
            usage(System.err, 1);
            throw e;
        }
    }

    private final SpicePointingProvider provider;
    private final String inputString;
    private final Path inputDir;
    private final Path outputDir;
    private final String fitsTimeKey;

    protected ComputeSpicePointing(SpicePointingProvider provider, String inputString, Path inputDir, Path outputDir, String fitsTimeKey)
    {
        super();
        this.provider = provider;
        this.inputString = inputString;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.fitsTimeKey = fitsTimeKey;
    }

    public void run() throws Exception
    {
        LinkedHashMap<String, String> outputFileMap = getOutputFilesKeyedOnTime(inputDir, inputString, fitsTimeKey);
        createOutputDirectory();
        TimeSystems timeSystems = provider.getTimeSystems();
        for (Entry<String, String> entry : outputFileMap.entrySet())
        {
            UTCEpoch utcTime = SpicePointingProvider.getUTC(entry.getKey());
            writePointingFile(provider.provide(timeSystems.getUTC().getTSEpoch(utcTime)), entry.getValue());
        }
    }

    protected static void usage(PrintStream stream, int exitCode)
    {
        stream.println("--------------------------------------------------------------------------------");
        stream.println("Usage: ComputeSpicePointing bodyId bodyFrame scId sclkIdCode instrumentFrame");
        stream.println("                      mkFile inputFile inputDir outputDir fitsTimeKey\n");
        stream.println("             All input paths are relative to inputDir.");
        stream.println("               sclkIdCode - this is a mission-specific int. Take a guess;");
        stream.println("                      program will probably fail but will advise you");
        stream.println("                      of your options.\n");
        stream.println("               mkFile - metakernel file\n");
        stream.println("               inputFile - a text file with a list of input FITS files.\n");
        stream.println("               outputDir - output directory only; tool will populate this.\n");
        stream.println("               fitsTimeKey - time keyword.");
        stream.println("--------------------------------------------------------------------------------");
        System.exit(exitCode);
    }

    /**
     * Read the specified fitsFileList, using the first value found for the
     * timeKeyword in each file to construct a time stamp. The file list is
     * assumed to contain just paths to FITS files relative to the inputDir
     * path.
     *
     * @param inputDir path relative to which both fitsFileList and its content
     *            FITS files are located
     * @param fitsFileList the file containing the list of FITS files, one per
     *            line
     * @param timeKeyName the time keyword used to get the times from the files
     * @return a map of output file names, keyed on the time string read from
     *         each input FITS file
     * @throws IOException if any I/O errors or {@link FitsException} occurs.
     */
    protected LinkedHashMap<String, String> getOutputFilesKeyedOnTime(Path inputDir, String fitsFileList, String timeKeyName) throws IOException
    {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputDir.resolve(fitsFileList).toFile())))
        {
            String fitsFileName;
            while ((fitsFileName = reader.readLine()) != null)
            {
                File file = inputDir.resolve(fitsFileName).toFile();
                try (Fits fits = new Fits(file))
                {
                    HeaderCard keyword = null;
                    for (int hduIndex = 0; hduIndex < fits.getNumberOfHDUs() && keyword == null; ++hduIndex)
                    {
                        keyword = fits.getHDU(hduIndex).getHeader().findCard(timeKeyName);
                    }
                    if (keyword != null)
                    {
                        String outFileName = file.getName().replaceFirst("\\.[^\\.]+$", "").concat(".INFO");
                        result.put(keyword.getValue(), outFileName);
                    }
                    else
                    {
                        System.err.println("Could not find time keyword " + timeKeyName + "; skipping FITS file " + file);
                    }
                }
                catch (FitsException e)
                {
                    throw new IOException(e);
                }
            }
        }

        return result;
    }

    protected void createOutputDirectory() throws IOException
    {
        File outputDir = this.outputDir.toFile();
        if (outputDir.exists() && !outputDir.isDirectory())
        {
            throw new IOException("Output area " + outputDir + " exists but is not a directory. Can't create output");
        }
        File parentDir = outputDir.getParentFile();
        if (parentDir.exists() && !parentDir.isDirectory())
        {
            throw new IOException("Output area parent " + parentDir + " exists but is not a directory. Can't create output");
        }
        outputDir.mkdirs();
    }

    protected void writePointingFile(Pointing pointing, String fileName) throws IOException
    {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir.resolve(fileName).toFile())))
        {

        }

    }

    public static void main(String[] args)
    {
        int exitCode;
        try
        {
            System.setProperty("awt.headless", "true");
            ComputeSpicePointing theApp = ComputeSpicePointing.of(args);
            theApp.run();
            exitCode = 0;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            exitCode = 1;
        }

        if (exitCode == 0)
        {
            System.out.println("Done.");
        }
        else
        {
            System.err.println("ERROR.");
        }

        System.exit(exitCode);
    }

}
