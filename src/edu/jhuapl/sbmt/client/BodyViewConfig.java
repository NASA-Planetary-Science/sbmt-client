package edu.jhuapl.sbmt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;


/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public abstract class BodyViewConfig extends ViewConfig
{
    public String rootDirOnServer;
    protected String shapeModelFileBaseName = "shape/shape";
    protected String shapeModelFileExtension = ".vtk";
    protected String[] shapeModelFileNames = null;
    public String timeHistoryFile;
    public double density = 0.0; // in units g/cm^3
    public double rotationRate = 0.0; // in units radians/sec

    public boolean hasColoringData = true;
    public boolean hasImageMap = false;
    public String[] imageMaps = null;

    public boolean hasMapmaker = false;
    public boolean hasBigmap = false;
    public boolean hasSpectralData = false;
    public boolean hasLineamentData = false;

    // if spectralModes is not empty, the following must be filled in
    public Date imageSearchDefaultStartDate;
    public Date imageSearchDefaultEndDate;
    public String[] imageSearchFilterNames = new String[] {};
    public String[] imageSearchUserDefinedCheckBoxesNames = new String[] {};
    public double imageSearchDefaultMaxSpacecraftDistance;
    public double imageSearchDefaultMaxResolution;
    public boolean hasHierarchicalImageSearch;
    public boolean hasHierarchicalSpectraSearch;
    public HierarchicalSearchSpecification hierarchicalImageSearchSpecification;
    public SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification;
    public String spectrumMetadataFile;

    public boolean hasHypertreeBasedSpectraSearch=false;
    public Map<String, String> spectraSearchDataSourceMap=Maps.newHashMap();

    public boolean hasHypertreeBasedLidarSearch=false;
    // if hasLidarData is true, the following must be filled in
    public Map<String, String> lidarSearchDataSourceMap=Maps.newHashMap();
    public Map<String, String> lidarBrowseDataSourceMap=Maps.newHashMap();    // overrides lidarBrowseFileListResourcePath for OLA
    public Map<String, ArrayList<Date>> lidarSearchDataSourceTimeMap = Maps.newHashMap();

    // Required if hasLidarData is true:
    public String lidarBrowseOrigPathRegex; // regular expression to match path prefix from database, which may not be current path. May be null to skip regex.
    public String lidarBrowsePathTop; // current top-of-path for lidar data; replaces the expression given by lidarBrowseOrigPathRegex.

    public int[] lidarBrowseXYZIndices = new int[] {};
    public int[] lidarBrowseSpacecraftIndices = new int[] {};
    public int lidarBrowseOutgoingIntensityIndex;
    public int lidarBrowseReceivedIntensityIndex;
    public int lidarBrowseRangeIndex;
    public boolean lidarBrowseIsRangeExplicitInData = false;
    public boolean lidarBrowseIntensityEnabled = false;
    public boolean lidarBrowseIsLidarInSphericalCoordinates = false;
    public boolean lidarBrowseIsSpacecraftInSphericalCoordinates = false;
    public boolean lidarBrowseIsTimeInET = false;
    public int lidarBrowseTimeIndex;
    public int lidarBrowseNoiseIndex;
    public String lidarBrowseFileListResourcePath;
    public int lidarBrowseNumberHeaderLines;
    public boolean lidarBrowseIsBinary = false;
    public int lidarBrowseBinaryRecordSize; // only required if lidarBrowseIsBinary is true

    // Return whether or not the units of the lidar points are in meters. If false
    // they are assumed to be in kilometers.
    public boolean lidarBrowseIsInMeters;
    public double lidarOffsetScale;

    public boolean hasLidarData = false;
    public Date lidarSearchDefaultStartDate;
    public Date lidarSearchDefaultEndDate;

    //DTMs
    public Map<String, String> dtmBrowseDataSourceMap = Maps.newHashMap();
    public Map<String, String> dtmSearchDataSourceMap = Maps.newHashMap();

    // Flag for beta mode
    public static boolean betaMode = false;

    static public final int LEISA_NBANDS = 256;
    static public final int MVIC_NBANDS = 4;

    static public final String[] DEFAULT_GASKELL_LABELS_PER_RESOLUTION = {
        "Low (49152 plates)",
        "Medium (196608 plates)",
        "High (786432 plates)",
        "Very High (3145728 plates)"
    };

    static public final Integer[] DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION = {
        49152,
        196608,
        786432,
        3145728
    };

    //
    // Additional variables not inherited from parent
    //

    public BodyType type; // e.g. asteroid, comet, satellite
    public ShapeModelPopulation population = ShapeModelPopulation.NA; // e.g. Mars for satellites or main belt for asteroids
    public ShapeModelDataUsed dataUsed = ShapeModelDataUsed.NA; // e.g. images, radar, lidar, or enhanced

    public ImagingInstrument[] imagingInstruments = {};
    public Instrument lidarInstrumentName = Instrument.LIDAR;

    public BasicSpectrumInstrument[] spectralInstruments = {};

    protected BodyViewConfig(Iterable<String> resolutionLabels, Iterable<Integer> resolutionNumberElements)
    {
        super(resolutionLabels, resolutionNumberElements);
    }

    @Override
    public String getUniqueName()
    {
        if (ShapeModelType.CUSTOM == author)
            return author + "/" + modelLabel;
        else if (author != null)
        {
            if (version == null)
                return author + "/" + body;
            else
                return author + "/" + body + " (" + version + ")";
        }
        else
            return body.toString();
    }

    @Override
    public String getShapeModelName()
    {
        if (author == ShapeModelType.CUSTOM)
            return modelLabel;
        else
        {
            String ver = "";
            if (version != null)
                ver += " (" + version + ")";
            return body.toString() + ver;
        }
    }

    public String serverPath(String fileName)
    {
        return serverPath(rootDirOnServer, fileName);
    }

    public String serverPath(String fileName, Instrument instrument)
    {
        return serverPath(rootDirOnServer, instrument.toString().toLowerCase(), fileName);
    }

    public String serverImagePath(String fileName, Instrument instrument)
    {
        return serverPath(fileName, instrument, "images");
    }

    public String serverPath(String fileName, Instrument instrument, String subdir)
    {
        return serverPath(rootDirOnServer, instrument.toString().toLowerCase(), subdir, fileName);
    }

    // methods
    //
     /**
      * Return a unique name for this model. No other model may have this
      * name. Note that only applies within built-in models or custom models
      * but a custom model can share the name of a built-in one or vice versa.
      * By default simply return the author concatenated with the
      * name if the author is not null or just the name if the author
      * is null.
      * @return
      */
    //
    //  Define all the built-in bodies to be loaded from the server
    //

    //
    // Clone operation
    //

    @Override
    public BodyViewConfig clone() // throws CloneNotSupportedException
    {
//      PolyhedralModelConfig c = new PolyhedralModelConfig();
        BodyViewConfig c = (BodyViewConfig)super.clone();

        c.rootDirOnServer = this.rootDirOnServer;
        c.hasColoringData = this.hasColoringData;
        c.hasImageMap = this.hasImageMap;

        c.timeHistoryFile = this.timeHistoryFile;
        c.hasStateHistory = this.hasStateHistory;

        c.body = this.body;
        c.type = this.type;
        c.population = this.population;
        c.dataUsed = this.dataUsed;

        // deep clone imaging instruments
        if (this.imagingInstruments != null)
        {
            int length = this.imagingInstruments.length;
            c.imagingInstruments = new ImagingInstrument[length];
            for (int i = 0; i < length; i++)
                c.imagingInstruments[i] = this.imagingInstruments[i].clone();
        }

        if (this.imagingInstruments != null && this.imagingInstruments.length > 0)
        {
            c.imagingInstruments = this.imagingInstruments.clone();
            c.imageSearchDefaultStartDate = (Date)this.imageSearchDefaultStartDate.clone();
            c.imageSearchDefaultEndDate = (Date)this.imageSearchDefaultEndDate.clone();
            c.imageSearchFilterNames = this.imageSearchFilterNames.clone();
            c.imageSearchUserDefinedCheckBoxesNames = this.imageSearchUserDefinedCheckBoxesNames.clone();
            c.imageSearchDefaultMaxSpacecraftDistance = this.imageSearchDefaultMaxSpacecraftDistance;
            c.imageSearchDefaultMaxResolution = this.imageSearchDefaultMaxResolution;
            c.hasHierarchicalImageSearch = this.hasHierarchicalImageSearch;
            if(this.hierarchicalImageSearchSpecification != null)
            {
                c.hierarchicalImageSearchSpecification = this.hierarchicalImageSearchSpecification.clone();
            }
            else
            {
                c.hierarchicalImageSearchSpecification = null;
            }
        }

        if (this.hasLidarData)
            c.lidarInstrumentName = this.lidarInstrumentName;

        c.hasLidarData = this.hasLidarData;
        c.hasMapmaker = this.hasMapmaker;
        c.hasBigmap = this.hasBigmap;
        c.density = this.density;
        c.rotationRate = this.rotationRate;
        c.hasSpectralData = this.hasSpectralData;
        c.hasLineamentData = this.hasLineamentData;

        if (this.hasLidarData)
        {
            c.lidarSearchDefaultStartDate = (Date)this.lidarSearchDefaultStartDate.clone();
            c.lidarSearchDefaultEndDate = (Date)this.lidarSearchDefaultEndDate.clone();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>(this.lidarSearchDataSourceMap);
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>(this.lidarBrowseDataSourceMap);
            c.lidarBrowseXYZIndices = this.lidarBrowseXYZIndices.clone();
            c.lidarBrowseSpacecraftIndices = this.lidarBrowseSpacecraftIndices.clone();
            c.lidarBrowseIsLidarInSphericalCoordinates = this.lidarBrowseIsLidarInSphericalCoordinates;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = this.lidarBrowseIsSpacecraftInSphericalCoordinates;
            c.lidarBrowseIsTimeInET = this.lidarBrowseIsTimeInET;
            c.lidarBrowseTimeIndex = this.lidarBrowseTimeIndex;
            c.lidarBrowseNoiseIndex = this.lidarBrowseNoiseIndex;
            c.lidarBrowseOutgoingIntensityIndex = this.lidarBrowseOutgoingIntensityIndex;
            c.lidarBrowseReceivedIntensityIndex = this.lidarBrowseReceivedIntensityIndex;
            c.lidarBrowseRangeIndex = this.lidarBrowseRangeIndex;
            c.lidarBrowseIsRangeExplicitInData = this.lidarBrowseIsRangeExplicitInData;
            c.lidarBrowseIntensityEnabled = this.lidarBrowseIntensityEnabled;
            c.lidarBrowseFileListResourcePath = this.lidarBrowseFileListResourcePath;
            c.lidarBrowseNumberHeaderLines = this.lidarBrowseNumberHeaderLines;
            c.lidarBrowseIsInMeters = this.lidarBrowseIsInMeters;
            c.lidarBrowseIsBinary = this.lidarBrowseIsBinary;
            c.lidarBrowseBinaryRecordSize = this.lidarBrowseBinaryRecordSize;
            c.lidarOffsetScale = this.lidarOffsetScale;
        }
        c.modelLabel = this.modelLabel;
        c.customTemporary = this.customTemporary;

        return c;
    }

    public String[] getShapeModelFileNames() {
        if (shapeModelFileNames != null) {
            return shapeModelFileNames;
        }

        // TODO this is an awful hack. Should not cue on the directory case
        // sensitivity pattern to decide where to find the shape model file!
        // There are subtle problems with all the "better" ways to do it.
        if (!rootDirOnServer.toLowerCase().equals(rootDirOnServer))
        {
            int numberResolutions = getResolutionLabels().size();
            // Another awful hack. Assume that if there are 4 resolutions,
            // but no specific names, it must be a legacy SPC model.
            if (numberResolutions == 4)
            {
                return prepend(rootDirOnServer, "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");
            }
            else if (numberResolutions != 1)
            {
                throw new AssertionError("Unable to determine shape file name(s) for " + rootDirOnServer);
            }

            return new String[] { rootDirOnServer };
        }

        if (shapeModelFileBaseName == null || shapeModelFileExtension == null) {
            throw new NullPointerException();
        }

        int numberResolutions = getResolutionLabels().size();

        String[] modelFiles = new String[numberResolutions];
        for (int index = 0; index < numberResolutions; ++index) {
            modelFiles[index] = serverPath(shapeModelFileBaseName + index + shapeModelFileExtension + ".gz");
        }

        return modelFiles;
    }

    public Map<String, String> getSpectraSearchDataSourceMap()
	{
		return spectraSearchDataSourceMap;
	}

    protected static String[] prepend(String prefix, String... strings)
    {
        String[] result = new String[strings.length];
        int index = 0;
        for (String string : strings)
        {
            result[index++] = SafeURLPaths.instance().getString(prefix, string);
        }

        return result;
    }

	private static String serverPath(String firstSegment, String... segments)
    {
        // Prevent trailing delimiters coming from empty segments at the end.
        int length = segments.length;
        while (length > 0)
        {
            if (segments[length - 1].isEmpty())
            {
                --length;
            }
            else
            {
                break;
            }
        }
        if (length < segments.length)
        {
            segments = Arrays.copyOfRange(segments, 0, length);
        }
        return SafeURLPaths.instance().getString(firstSegment, segments);
    }

    public static void main(String[] args)
    {
        System.out.println("serverPath(\"\", \"\") is \"" + serverPath("", "") + "\"");
        System.out.println("serverPath(\"http://sbmt.jhuapl.edu/sbmt\", \"\", \"\") is \"" + serverPath("http://sbmt.jhuapl.edu/sbmt", "", "") + "\"");
        System.out.println("serverPath(\"file://sbmt.jhuapl.edu/sbmt\", \"\", \"filename.txt\") is \"" + serverPath("file://sbmt.jhuapl.edu/sbmt", "", "filename.txt") + "\"");
    }
}
