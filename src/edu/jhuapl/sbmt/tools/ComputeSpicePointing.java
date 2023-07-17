package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.core.pointing.InstrumentPointing;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;

import crucible.core.math.vectorspace.UnwritableVectorIJK;
import crucible.core.mechanics.CelestialBodies;
import crucible.core.mechanics.EphemerisID;
import crucible.core.time.TimeSystems;
import crucible.core.time.UTCEpoch;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.HeaderCard;

public class ComputeSpicePointing
{
    protected static final TimeSystems DefaultTimeSystems = TimeSystems.builder().build();

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

            String bodyName = args[index++];
            String bodyFrameName = args[index++];
            String scName = args[index++];
            String scFrameName = args[index++];
            String instName = args[index++];

            String mkPathString = args[index++];
            ImmutableList<Path> mkPaths;
            mkPaths = ImmutableList.of(Paths.get(mkPathString));

            Path inputFilePath = Paths.get(args[index++]);
            Path inputDir = Paths.get(args[index++]);
            Path outputDir = Paths.get(args[index++]);
            String fitsTimeKey = args[index++];

            SpicePointingProvider.Builder builder = SpicePointingProvider.builder(mkPaths, bodyName, bodyFrameName, scName, scFrameName);

            EphemerisID bodyId = builder.bindEphemeris(bodyName);
            // Note: if you are using this code as an example, you probably 
            // don't want to include all instruments here...
            builder.includeAllInstruments();

            SpicePointingProvider provider = builder.build();

            provider.setCurrentInstrumentName(instName);

            return new ComputeSpicePointing(provider, bodyId, inputFilePath, inputDir, outputDir, fitsTimeKey);
        }
        catch (Exception e)
        {
            usage(System.err, 1);
            throw e;
        }
    }

    private final SpicePointingProvider provider;
    private final Path inputFilePath;
    private final Path inputDir;
    private final Path outputDir;
    private final String fitsTimeKey;

    protected ComputeSpicePointing(SpicePointingProvider provider, EphemerisID bodyId, Path inputFilePath, Path inputDir, Path outputDir, String fitsTimeKey)
    {
        super();

        this.provider = provider;
        this.inputFilePath = inputFilePath;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.fitsTimeKey = fitsTimeKey;
    }

    public void run() throws Exception
    {
        LinkedHashMap<String, String> outputFileMap = getOutputFilesKeyedOnTime(inputDir, inputFilePath, fitsTimeKey);

        createOutputDirectory();

        for (Entry<String, String> entry : outputFileMap.entrySet())
        {
            String utcTimeString = entry.getKey();
            String fileName = entry.getValue();

            writePointingFile(utcTimeString, fileName);
        }
    }

    protected static void usage(PrintStream stream, int exitCode)
    {
        stream.println("--------------------------------------------------------------------------------");
        stream.println("Usage: ComputeSpicePointing bodyId bodyFrame scId scFrame instName");
        stream.println("                     mkFile inputFile inputDir outputDir fitsTimeKey\n");
        stream.println("               mkFile - metakernel file\n");
        stream.println("               inputFile - a text file with a list of input FITS files.\n");
        stream.println("               inputDir - top directory containing files listed in inputFile.\n");
        stream.println("               outputDir - output directory only; tool will populate this.\n");
        stream.println("               fitsTimeKey - time keyword.");
        stream.println("--------------------------------------------------------------------------------");
        if (exitCode == 0)
        {
            System.exit(exitCode);
        }
    }

    /**
     * Read the specified fitsFileList, using the first value found for the
     * timeKeyword in each file to construct a time stamp. The file list is
     * assumed to contain just paths to FITS files relative to the inputDir
     * path.
     *
     * @param inputDir path relative to which both fitsFileList and its content
     *            FITS files are located
     * @param fitsFileList path to file containing the list of FITS files, one
     *            per line
     * @param timeKeyName the time keyword used to get the times from the files
     * @return a map of output file names, keyed on the time string read from
     *         each input FITS file
     * @throws IOException if any I/O errors or {@link FitsException} occurs.
     */
    protected LinkedHashMap<String, String> getOutputFilesKeyedOnTime(Path inputDir, Path fitsFileList, String timeKeyName) throws IOException
    {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fitsFileList.toFile())))
        {
            String fitsFileName;
            while ((fitsFileName = reader.readLine()) != null)
            {
                File file = inputDir.resolve(fitsFileName).toFile();
                try (Fits fits = new Fits(file))
                {
                    HeaderCard keyword = null;
                    fits.read();
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
        outputDir.mkdirs();
        if (!outputDir.isDirectory())
        {
            throw new IOException("Output area " + outputDir + " is not a directory. Can't create output");
        }
    }

    protected void writePointingFile(String utcTimeString, String fileName) throws IOException, ParseException
    {
        UTCEpoch utcTime = getUTC(utcTimeString);
        double time = DefaultTimeSystems.getTDB().getTime(DefaultTimeSystems.getUTC().getTSEpoch(utcTime));

        InstrumentPointing pointing = provider.provide(time);
        DecimalFormat formatter = new DecimalFormat("0.0000000000000000E00");
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputDir.resolve(fileName).toFile())))
        {
            List<UnwritableVectorIJK> frustum = pointing.getFrustum();

            writer.println("START_TIME          = " + utcTimeString);
            writer.println("STOP_TIME           = " + utcTimeString);
            writer.println("SPACECRAFT_POSITION = " + format(formatter, pointing.getScPosition()));
            writer.println("BORESIGHT_DIRECTION = " + format(formatter, pointing.getBoresight()));
            writer.println("UP_DIRECTION        = " + format(formatter, pointing.getUpDirection()));
            writer.println("FRUSTUM1            = " + format(formatter, frustum.get(0)));
            writer.println("FRUSTUM2            = " + format(formatter, frustum.get(1)));
            writer.println("FRUSTUM3            = " + format(formatter, frustum.get(2)));
            writer.println("FRUSTUM4            = " + format(formatter, frustum.get(3)));
            writer.println("SUN_POSITION_LT     = " + format(formatter, pointing.getPosition(CelestialBodies.SUN)));
        }

    }

    protected String format(DecimalFormat formatter, UnwritableVectorIJK vector)
    {
        if (vector == null)
        {
            vector = new UnwritableVectorIJK(-1., -1., -1.);
        }

        StringBuilder builder = new StringBuilder("( ");

        builder.append(formatter.format(vector.getI()));
        builder.append(" , ");
        builder.append(formatter.format(vector.getJ()));
        builder.append(" , ");
        builder.append(formatter.format(vector.getK()));
        builder.append(" )");

        return builder.toString().toLowerCase().replaceAll("e(\\d)", "e+$1");
    }

    public static UTCEpoch getUTC(String utcString) throws ParseException
    {
        Preconditions.checkNotNull(utcString);

        UTCEpoch result;

        String[] fields = utcString.split("[^\\d\\.]");
        if (fields.length == 5 || fields.length == 6)
        {
            // UTC fields separated by non-numeric characters, either
            // yyyy-doy... or yyyy-mm-dd.
            // Parse all but the last field as integers.
            List<Integer> integers = new ArrayList<>();
            for (int index = 0; index < fields.length - 1; ++index)
            {
                integers.add(Integer.parseInt(fields[index]));
            }

            // Parse the last field as a double (seconds).
            double sec = 0.;
            if (fields.length > integers.size())
            {
                sec = Double.parseDouble(fields[integers.size()]);
            }

            if (integers.size() == 4)
            {
                result = new UTCEpoch(integers.get(0), integers.get(1), integers.get(2), integers.get(3), sec);
            }
            else if (integers.size() == 5)
            {
                result = new UTCEpoch(integers.get(0), integers.get(1), integers.get(2), integers.get(3), integers.get(4), sec);
            }
            else
            {
                throw new AssertionError("fields was either 5 or 6 elements, so array must be 4 or 5");
            }
        }
        else if (utcString.length() == 13)
        {
            // yyyydddhhmmss
            int y = Integer.parseInt(utcString.substring(0, 4));
            int doy = Integer.parseInt(utcString.substring(4, 7));
            int hr = Integer.parseInt(utcString.substring(7, 9));
            int mn = Integer.parseInt(utcString.substring(9, 11));
            int s = Integer.parseInt(utcString.substring(11, 13));
            result = new UTCEpoch(y, doy, hr, mn, s);
        }
        else if (utcString.length() == 14)
        {
            // yyyymmddhhmmss
            int y = Integer.parseInt(utcString.substring(0, 4));
            int m = Integer.parseInt(utcString.substring(4, 6));
            int d = Integer.parseInt(utcString.substring(6, 8));
            int h = Integer.parseInt(utcString.substring(8, 10));
            int mn = Integer.parseInt(utcString.substring(10, 12));
            int s = Integer.parseInt(utcString.substring(12, 14));
            result = new UTCEpoch(y, m, d, h, mn, s);
        }
        else
        {
            throw new ParseException("Can't parse string as time: " + utcString, 0);
        }

        return result;
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
