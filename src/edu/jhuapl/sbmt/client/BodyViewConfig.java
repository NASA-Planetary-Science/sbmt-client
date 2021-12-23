package edu.jhuapl.sbmt.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.tools.DBRunInfo;


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
    public Date stateHistoryStartDate, stateHistoryEndDate;
    public double density = 0.0; // in units g/cm^3
    public double rotationRate = 0.0; // in units radians/sec

	public boolean hasFlybyData; // for flyby path data
	public boolean hasStateHistory; // for bodies with state history tabs
	public SpiceInfo spiceInfo;

    public boolean hasColoringData = true;
    public boolean hasImageMap = false;

    public boolean hasMapmaker = false;
    public boolean hasRemoteMapmaker = false;
//    public double bodyDensity = 0.0;
//    public double bodyRotationRate = 0.0;
    public double bodyReferencePotential = 0.0;
    public String bodyLowestResModelName = "";



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
    public boolean hasHierarchicalImageSearch = false;
    public boolean hasHierarchicalSpectraSearch = false;
    public HierarchicalSearchSpecification hierarchicalImageSearchSpecification;
    public SpectrumInstrumentMetadataIO hierarchicalSpectraSearchSpecification;
    public String spectrumMetadataFile;

    public boolean hasHypertreeBasedSpectraSearch=false;
    public Map<String, String> spectraSearchDataSourceMap=Maps.newHashMap();

    public boolean hasHypertreeBasedLidarSearch=false;
    // if hasLidarData is true, the following must be filled in
    public Map<String, String> lidarSearchDataSourceMap=Maps.newHashMap();
    public Map<String, String> lidarBrowseDataSourceMap=Maps.newHashMap();    // overrides lidarBrowseFileListResourcePath for OLA
    public Map<String, String> lidarBrowseWithPointsDataSourceMap=Maps.newHashMap();
    public Map<String, ArrayList<Date>> lidarSearchDataSourceTimeMap = Maps.newHashMap();
	public Map<String, ArrayList<Date>> orexSearchTimeMap = Maps.newHashMap();


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

    //default configs
    public SbmtMultiMissionTool.Mission[] presentInMissions;
    public SbmtMultiMissionTool.Mission[] defaultForMissions;


    //DTMs
    public Map<String, String> dtmBrowseDataSourceMap = Maps.newHashMap();
    public Map<String, String> dtmSearchDataSourceMap = Maps.newHashMap();

	/**
	 * Returns true if this configuration has any remote DTMs.
	 */
	public boolean hasRemoteDtmData()
	{
		if (dtmBrowseDataSourceMap == null || dtmBrowseDataSourceMap.size() == 0)
			return false;

		return true;
	}

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
    public ShapeModelBody system = null;
    public ShapeModelDataUsed dataUsed = ShapeModelDataUsed.NA; // e.g. images, radar, lidar, or enhanced

    public ImagingInstrument[] imagingInstruments = {};
    public Instrument lidarInstrumentName = Instrument.LIDAR;

    public List<BasicSpectrumInstrument> spectralInstruments = new ArrayList<BasicSpectrumInstrument>();

    public DBRunInfo[] databaseRunInfos = {};

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
        c.system = this.system;
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
        if (this.hasSpectralData)
        {
        	c.hasHierarchicalSpectraSearch = this.hasHierarchicalSpectraSearch;
        	c.hasHypertreeBasedSpectraSearch = this.hasHypertreeBasedSpectraSearch;
        	c.spectrumMetadataFile = this.spectrumMetadataFile;

        	c.hierarchicalSpectraSearchSpecification = this.hierarchicalSpectraSearchSpecification;
        	c.spectralInstruments = new ArrayList<BasicSpectrumInstrument>(this.spectralInstruments);
        	c.spectraSearchDataSourceMap = new LinkedHashMap<String, String>(this.spectraSearchDataSourceMap);
        }


        c.hasLineamentData = this.hasLineamentData;

        if (this.hasLidarData)
        {
            c.lidarSearchDefaultStartDate = (Date)this.lidarSearchDefaultStartDate.clone();
            c.lidarSearchDefaultEndDate = (Date)this.lidarSearchDefaultEndDate.clone();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>(this.lidarSearchDataSourceMap);
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>(this.lidarBrowseDataSourceMap);
            c.lidarBrowseWithPointsDataSourceMap = new LinkedHashMap<>(this.lidarBrowseWithPointsDataSourceMap);
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

    @Override
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
//            System.out.println("BodyViewConfig: getShapeModelFileNames: model file name " + modelFiles[index]);
        }

        return modelFiles;
    }

    public String getShapeModelFileExtension()
    {
        return shapeModelFileExtension;
    }

    public void setShapeModelFileExtension(String shapeModelFileExtension)
    {
        this.shapeModelFileExtension = shapeModelFileExtension;
    }

    public Map<String, String> getSpectraSearchDataSourceMap()
	{
		return spectraSearchDataSourceMap;
	}

    protected List<ImageKeyInterface> getImageMapKeys()
    {
        throw new UnsupportedOperationException();
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

    public boolean hasColoringData()
	{
		return hasColoringData;
	}

    public static void main(String[] args)
    {
        System.out.println("serverPath(\"\", \"\") is \"" + serverPath("", "") + "\"");
        System.out.println("serverPath(\"https://sbmt.jhuapl.edu/sbmt\", \"\", \"\") is \"" + serverPath("https://sbmt.jhuapl.edu/sbmt", "", "") + "\"");
        System.out.println("serverPath(\"file://sbmt.jhuapl.edu/sbmt\", \"\", \"filename.txt\") is \"" + serverPath("file://sbmt.jhuapl.edu/sbmt", "", "filename.txt") + "\"");
    }

    public String[] getImageSearchFilterNames()
	{
		return imageSearchFilterNames;
	}


	public String[] getImageSearchUserDefinedCheckBoxesNames()
	{
		return imageSearchUserDefinedCheckBoxesNames;
	}


	public boolean hasHierarchicalImageSearch()
	{
		return hasHierarchicalImageSearch;
	}

	public void setShapeModelFileNames(String[] shapeModelFileNames)
	{
		this.shapeModelFileNames = shapeModelFileNames;
	}

	/**
	 * @return the spiceInfo
	 */
	public SpiceInfo getSpiceInfo()
	{
		return spiceInfo;
	}

	/**
	 * @return the stateHistoryStartDate
	 */
	public Date getStateHistoryStartDate()
	{
		return stateHistoryStartDate;
	}

	/**
	 * @return the stateHistoryEndDate
	 */
	public Date getStateHistoryEndDate()
	{
		return stateHistoryEndDate;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(density);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((bodyLowestResModelName == null) ? 0 : bodyLowestResModelName.hashCode());
		temp = Double.doubleToLongBits(bodyReferencePotential);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((dataUsed == null) ? 0 : dataUsed.hashCode());
		result = prime * result + Arrays.hashCode(databaseRunInfos);
		result = prime * result + Arrays.hashCode(defaultForMissions);
		result = prime * result + ((dtmBrowseDataSourceMap == null) ? 0 : dtmBrowseDataSourceMap.hashCode());
		result = prime * result + ((dtmSearchDataSourceMap == null) ? 0 : dtmSearchDataSourceMap.hashCode());
		result = prime * result + (hasBigmap ? 1231 : 1237);
		result = prime * result + (hasColoringData ? 1231 : 1237);
		result = prime * result + (hasFlybyData ? 1231 : 1237);
		result = prime * result + (hasHierarchicalImageSearch ? 1231 : 1237);
		result = prime * result + (hasHierarchicalSpectraSearch ? 1231 : 1237);
		result = prime * result + (hasHypertreeBasedLidarSearch ? 1231 : 1237);
		result = prime * result + (hasHypertreeBasedSpectraSearch ? 1231 : 1237);
		result = prime * result + (hasImageMap ? 1231 : 1237);
		result = prime * result + (hasLidarData ? 1231 : 1237);
		result = prime * result + (hasLineamentData ? 1231 : 1237);
		result = prime * result + (hasMapmaker ? 1231 : 1237);
		result = prime * result + (hasRemoteMapmaker ? 1231 : 1237);
		result = prime * result + (hasSpectralData ? 1231 : 1237);
		result = prime * result + (hasStateHistory ? 1231 : 1237);
		result = prime * result + ((imageSearchDefaultEndDate == null) ? 0 : imageSearchDefaultEndDate.hashCode());
		temp = Double.doubleToLongBits(imageSearchDefaultMaxResolution);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(imageSearchDefaultMaxSpacecraftDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((imageSearchDefaultStartDate == null) ? 0 : imageSearchDefaultStartDate.hashCode());
		result = prime * result + Arrays.hashCode(imageSearchFilterNames);
		result = prime * result + Arrays.hashCode(imageSearchUserDefinedCheckBoxesNames);
		result = prime * result + Arrays.hashCode(imagingInstruments);
		result = prime * result + lidarBrowseBinaryRecordSize;
		result = prime * result + ((lidarBrowseDataSourceMap == null) ? 0 : lidarBrowseDataSourceMap.hashCode());
		result = prime * result + ((lidarBrowseWithPointsDataSourceMap == null) ? 0 : lidarBrowseWithPointsDataSourceMap.hashCode());
		result = prime * result
				+ ((lidarBrowseFileListResourcePath == null) ? 0 : lidarBrowseFileListResourcePath.hashCode());
		result = prime * result + (lidarBrowseIntensityEnabled ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsBinary ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsInMeters ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsLidarInSphericalCoordinates ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsRangeExplicitInData ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsSpacecraftInSphericalCoordinates ? 1231 : 1237);
		result = prime * result + (lidarBrowseIsTimeInET ? 1231 : 1237);
		result = prime * result + lidarBrowseNoiseIndex;
		result = prime * result + lidarBrowseNumberHeaderLines;
		result = prime * result + ((lidarBrowseOrigPathRegex == null) ? 0 : lidarBrowseOrigPathRegex.hashCode());
		result = prime * result + lidarBrowseOutgoingIntensityIndex;
		result = prime * result + ((lidarBrowsePathTop == null) ? 0 : lidarBrowsePathTop.hashCode());
		result = prime * result + lidarBrowseRangeIndex;
		result = prime * result + lidarBrowseReceivedIntensityIndex;
		result = prime * result + Arrays.hashCode(lidarBrowseSpacecraftIndices);
		result = prime * result + lidarBrowseTimeIndex;
		result = prime * result + Arrays.hashCode(lidarBrowseXYZIndices);
		result = prime * result + ((lidarInstrumentName == null) ? 0 : lidarInstrumentName.hashCode());
		temp = Double.doubleToLongBits(lidarOffsetScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((lidarSearchDataSourceMap == null) ? 0 : lidarSearchDataSourceMap.hashCode());
		result = prime * result
				+ ((lidarSearchDataSourceTimeMap == null) ? 0 : lidarSearchDataSourceTimeMap.hashCode());
		result = prime * result + ((lidarSearchDefaultEndDate == null) ? 0 : lidarSearchDefaultEndDate.hashCode());
		result = prime * result + ((lidarSearchDefaultStartDate == null) ? 0 : lidarSearchDefaultStartDate.hashCode());
		result = prime * result + ((orexSearchTimeMap == null) ? 0 : orexSearchTimeMap.hashCode());
		result = prime * result + ((population == null) ? 0 : population.hashCode());
		result = prime * result + ((system == null) ? 0 : system.hashCode());
		result = prime * result + Arrays.hashCode(presentInMissions);
		result = prime * result + ((rootDirOnServer == null) ? 0 : rootDirOnServer.hashCode());
		temp = Double.doubleToLongBits(rotationRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((shapeModelFileBaseName == null) ? 0 : shapeModelFileBaseName.hashCode());
		result = prime * result + ((shapeModelFileExtension == null) ? 0 : shapeModelFileExtension.hashCode());
		result = prime * result + Arrays.hashCode(shapeModelFileNames);
		result = prime * result + ((spectraSearchDataSourceMap == null) ? 0 : spectraSearchDataSourceMap.hashCode());
		result = prime * result + ((spectralInstruments == null) ? 0 : spectralInstruments.hashCode());
		result = prime * result + ((spectrumMetadataFile == null) ? 0 : spectrumMetadataFile.hashCode());
		result = prime * result + ((timeHistoryFile == null) ? 0 : timeHistoryFile.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
		{
//			System.out.println("BodyViewConfig: equals: view config parent doesn't equal");
			return false;
		}
//		if (getClass() != obj.getClass())
//		{
//			System.out.println("BodyViewConfig: equals: classes differ");
//			return false;
//		}
		BodyViewConfig other = (BodyViewConfig) obj;
		if (bodyLowestResModelName == null)
		{
			if (other.bodyLowestResModelName != null)
			{
//				System.err.println("BodyViewConfig: equals: lowest res model name name; other isn't");
				return false;
			}
		} else if (!bodyLowestResModelName.equals(other.bodyLowestResModelName))
		{
//			System.err.println("BodyViewConfig: equals: lowest res model names don't match " + bodyLowestResModelName + " " + other.bodyLowestResModelName);
			return false;
		}
		if (Double.doubleToLongBits(bodyReferencePotential) != Double.doubleToLongBits(other.bodyReferencePotential))
		{
//			System.err.println("BodyViewConfig: equals: body reference potential doesn't match ");
			return false;
		}

		if (dataUsed != other.dataUsed)
		{
			return false;
		}
		if (!Arrays.equals(databaseRunInfos, other.databaseRunInfos))
		{
			return false;
		}
		if (!Arrays.equals(defaultForMissions, other.defaultForMissions))
		{
			return false;
		}
		if (Double.doubleToLongBits(density) != Double.doubleToLongBits(other.density))
		{
//			System.err.println("BodyViewConfig: equals: density doesn't match");
			return false;
		}
		if (dtmBrowseDataSourceMap == null)
		{
			if (other.dtmBrowseDataSourceMap != null)
			{
				return false;
			}
		} else if (!dtmBrowseDataSourceMap.equals(other.dtmBrowseDataSourceMap))
		{
			return false;
		}
		if (dtmSearchDataSourceMap == null)
		{
			if (other.dtmSearchDataSourceMap != null)
				return false;
		} else if (!dtmSearchDataSourceMap.equals(other.dtmSearchDataSourceMap))
		{
//			System.err.println("BodyViewConfig: equals: dtm search data source map " + dtmSearchDataSourceMap + " " + other.dtmSearchDataSourceMap);
			return false;
		}
		if (hasBigmap != other.hasBigmap)
		{
//			System.err.println("BodyViewConfig: equals: has big map " + hasBigmap + " " + other.hasBigmap);
			return false;
		}
		if (hasColoringData != other.hasColoringData)
		{
//			System.err.println("BodyViewConfig: equals: has coloring data " + hasColoringData + " " + other.hasColoringData);
			return false;
		}
		if (hasFlybyData != other.hasFlybyData)
		{
			return false;
		}
		if (hasHierarchicalImageSearch != other.hasHierarchicalImageSearch)
		{
			return false;
		}
		if (hasHierarchicalSpectraSearch != other.hasHierarchicalSpectraSearch)
		{
			return false;
		}
		if (hasHypertreeBasedLidarSearch != other.hasHypertreeBasedLidarSearch)
		{
			return false;
		}
		if (hasHypertreeBasedSpectraSearch != other.hasHypertreeBasedSpectraSearch)
		{
			return false;
		}
		if (hasImageMap != other.hasImageMap)
		{
			return false;
		}
		if (hasLidarData != other.hasLidarData)
		{
			return false;
		}
		if (hasLineamentData != other.hasLineamentData)
		{
			return false;
		}
		if (hasMapmaker != other.hasMapmaker)
		{
			return false;
		}
		if (hasRemoteMapmaker != other.hasRemoteMapmaker)
		{
			return false;
		}
		if (hasSpectralData != other.hasSpectralData)
		{
			return false;
		}
		if (hasStateHistory != other.hasStateHistory)
		{
			return false;
		}
		if (imageSearchDefaultEndDate == null)
		{
			if (other.imageSearchDefaultEndDate != null)
			{
				return false;
			}
		}
		else if (!imageSearchDefaultEndDate.equals(other.imageSearchDefaultEndDate))
		{
			return false;
		}
		if (Double.doubleToLongBits(imageSearchDefaultMaxResolution) != Double
				.doubleToLongBits(other.imageSearchDefaultMaxResolution))
		{
			return false;
		}
		if (Double.doubleToLongBits(imageSearchDefaultMaxSpacecraftDistance) != Double
				.doubleToLongBits(other.imageSearchDefaultMaxSpacecraftDistance))
		{
			return false;
		}
		if (imageSearchDefaultStartDate == null)
		{
			if (other.imageSearchDefaultStartDate != null)
			{
				return false;
			}
		}
		else if (!imageSearchDefaultStartDate.equals(other.imageSearchDefaultStartDate))
		{
			return false;
		}
		if (!Arrays.equals(imageSearchFilterNames, other.imageSearchFilterNames))
		{
//			System.err.println("BodyViewConfig: equals: image search filter names don't match");
			return false;
		}
		if (!Arrays.equals(imageSearchUserDefinedCheckBoxesNames, other.imageSearchUserDefinedCheckBoxesNames))
		{
//			System.err.println("BodyViewConfig: equals: image search user defined check box names don't match");
			return false;
		}
		if (!Arrays.equals(imagingInstruments, other.imagingInstruments))
		{
//			System.err.println("BodyViewConfig: equals: imaging instruments don't match");
			return false;
		}
		if (lidarBrowseBinaryRecordSize != other.lidarBrowseBinaryRecordSize)
		{
//			System.err.println("BodyViewConfig: equals: lidar browse binary record size doesn't match " + lidarBrowseBinaryRecordSize + " " + other.lidarBrowseBinaryRecordSize);
			return false;
		}
		if (lidarBrowseDataSourceMap == null)
		{
			if (other.lidarBrowseDataSourceMap != null)
			{
				return false;
			}
		} else if (!lidarBrowseDataSourceMap.equals(other.lidarBrowseDataSourceMap))
		{
			return false;
		}
		if (lidarBrowseWithPointsDataSourceMap == null)
		{
			if (other.lidarBrowseWithPointsDataSourceMap != null)
			{
				return false;
			}
		} else if (!lidarBrowseWithPointsDataSourceMap.equals(other.lidarBrowseWithPointsDataSourceMap))
		{
			return false;
		}
		if (lidarBrowseFileListResourcePath == null)
		{
			if (other.lidarBrowseFileListResourcePath != null)
			{
				return false;
			}
		}
		else if (!lidarBrowseFileListResourcePath.equals(other.lidarBrowseFileListResourcePath))
		{
			return false;
		}
		if (lidarBrowseIntensityEnabled != other.lidarBrowseIntensityEnabled)
		{
//			System.err.println("BodyViewConfig: equals: browse intensity enabled don't match ");
			return false;
		}
		if (lidarBrowseIsBinary != other.lidarBrowseIsBinary)
		{
			return false;
		}
		if (lidarBrowseIsInMeters != other.lidarBrowseIsInMeters)
		{
			return false;
		}
		if (lidarBrowseIsLidarInSphericalCoordinates != other.lidarBrowseIsLidarInSphericalCoordinates)
		{
//			System.err.println("BodyViewConfig: equals: is lidar in sph coords unequal " + lidarBrowseIsLidarInSphericalCoordinates + " " + other.lidarBrowseIsLidarInSphericalCoordinates);
			return false;
		}
		if (lidarBrowseIsRangeExplicitInData != other.lidarBrowseIsRangeExplicitInData)
		{
//			System.err.println("BodyViewConfig: equals: is lidar range explicit unequal");
			return false;
		}
		if (lidarBrowseIsSpacecraftInSphericalCoordinates != other.lidarBrowseIsSpacecraftInSphericalCoordinates)
		{
//			System.err.println("BodyViewConfig: equals: lidar is sc in sph coords unequal");
			return false;
		}
		if (lidarBrowseIsTimeInET != other.lidarBrowseIsTimeInET)
		{
//			System.err.println("BodyViewConfig: equals: lidar browse in ET Doesn't match");
			return false;
		}
		if (lidarBrowseNoiseIndex != other.lidarBrowseNoiseIndex)
		{
			return false;
		}
		if (lidarBrowseNumberHeaderLines != other.lidarBrowseNumberHeaderLines)
		{
			return false;
		}
		if (lidarBrowseOrigPathRegex == null)
		{
			if (other.lidarBrowseOrigPathRegex != null)
				return false;
		} else if (!lidarBrowseOrigPathRegex.equals(other.lidarBrowseOrigPathRegex))
		{
			return false;
		}
		if (lidarBrowseOutgoingIntensityIndex != other.lidarBrowseOutgoingIntensityIndex)
		{
			return false;
		}
		if (lidarBrowsePathTop == null)
		{
			if (other.lidarBrowsePathTop != null)
				return false;
		} else if (!lidarBrowsePathTop.equals(other.lidarBrowsePathTop))
		{
			return false;
		}
		if (lidarBrowseRangeIndex != other.lidarBrowseRangeIndex)
		{
			return false;
		}
		if (lidarBrowseReceivedIntensityIndex != other.lidarBrowseReceivedIntensityIndex)
		{
			return false;
		}
		if (!Arrays.equals(lidarBrowseSpacecraftIndices, other.lidarBrowseSpacecraftIndices))
		{
			return false;
		}
		if (lidarBrowseTimeIndex != other.lidarBrowseTimeIndex)
		{
			return false;
		}
		if (!Arrays.equals(lidarBrowseXYZIndices, other.lidarBrowseXYZIndices))
		{
			return false;
		}
		if (lidarInstrumentName != other.lidarInstrumentName)
		{
			return false;
		}
		if (Double.doubleToLongBits(lidarOffsetScale) != Double.doubleToLongBits(other.lidarOffsetScale))
		{
			return false;
		}
		if (lidarSearchDataSourceMap == null)
		{
			if (other.lidarSearchDataSourceMap != null)
				return false;
		} else if (!lidarSearchDataSourceMap.equals(other.lidarSearchDataSourceMap))
			return false;
		if (lidarSearchDataSourceTimeMap == null)
		{
			if (other.lidarSearchDataSourceTimeMap != null)
				return false;
		} else if (!lidarSearchDataSourceTimeMap.equals(other.lidarSearchDataSourceTimeMap))
			return false;
		if (lidarSearchDefaultEndDate == null)
		{
			if (other.lidarSearchDefaultEndDate != null)
				return false;
		} else if (!lidarSearchDefaultEndDate.equals(other.lidarSearchDefaultEndDate))
			return false;
		if (lidarSearchDefaultStartDate == null)
		{
			if (other.lidarSearchDefaultStartDate != null)
				return false;
		} else if (!lidarSearchDefaultStartDate.equals(other.lidarSearchDefaultStartDate))
			return false;
		if (orexSearchTimeMap == null)
		{
			if (other.orexSearchTimeMap != null)
				return false;
		} else if (!orexSearchTimeMap.equals(other.orexSearchTimeMap))
			return false;
		if (population != other.population)
			return false;
		if (system != other.system)
		    return false;
		if (!Arrays.equals(presentInMissions, other.presentInMissions))
			return false;
		if (rootDirOnServer == null)
		{
			if (other.rootDirOnServer != null)
				return false;
		} else if (!rootDirOnServer.equals(other.rootDirOnServer))
			return false;
		if (Double.doubleToLongBits(rotationRate) != Double.doubleToLongBits(other.rotationRate))
		{
//			System.err.println("BodyViewConfig: equals: rotation rate doesn't match");
			return false;
		}
		if (shapeModelFileBaseName == null)
		{
			if (other.shapeModelFileBaseName != null)
				return false;
		} else if (!shapeModelFileBaseName.equals(other.shapeModelFileBaseName))
		{
			return false;
		}
		if (shapeModelFileExtension == null)
		{
			if (other.shapeModelFileExtension != null)
				return false;
		} else if (!shapeModelFileExtension.equals(other.shapeModelFileExtension))
		{
			return false;
		}
		if (!Arrays.equals(shapeModelFileNames, other.shapeModelFileNames))
		{
			return false;
		}
		if (spectraSearchDataSourceMap == null)
		{
			if (other.spectraSearchDataSourceMap != null)
				return false;
		} else if (!spectraSearchDataSourceMap.equals(other.spectraSearchDataSourceMap))
		{
//			System.err.println("BodyViewConfig: equals: spectra search data source map don't match");
			return false;
		}
		if (spectralInstruments == null)
		{
			if (other.spectralInstruments != null)
				return false;
		} else if (!spectralInstruments.equals(other.spectralInstruments))
			return false;
		if (spectrumMetadataFile == null)
		{
			if (other.spectrumMetadataFile != null)
				return false;
		} else if (!spectrumMetadataFile.equals(other.spectrumMetadataFile))
		{
//			System.err.println("BodyViewConfig: equals: spectrum metadata files don't match");
			return false;
		}
		if (timeHistoryFile == null)
		{
			if (other.timeHistoryFile != null)
				return false;
		} else if (!timeHistoryFile.equals(other.timeHistoryFile))
			return false;
		if (type != other.type)
			return false;

//		System.out.println("BodyViewConfig: equals: match!!");
		return true;
	}
}
