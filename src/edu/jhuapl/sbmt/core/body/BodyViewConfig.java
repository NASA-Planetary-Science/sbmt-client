package edu.jhuapl.sbmt.core.body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.IFeatureConfig;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.io.DBRunInfo;


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

	public boolean hasStateHistory; // for bodies with state history tabs

    public boolean hasColoringData = true;

    public boolean hasDTMs = true;
    public boolean hasMapmaker = false;
    public boolean hasRemoteMapmaker = false;
    public double bodyReferencePotential = 0.0;
    public String bodyLowestResModelName = "";

    protected final Map<Class<?>, List<IFeatureConfig>> featureConfigs = new HashMap<Class<?>, List<IFeatureConfig>>();

    public boolean hasBigmap = false;
    public boolean hasLineamentData = false;

    //default configs
    public Mission[] presentInMissions;
    public Mission[] defaultForMissions;


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
        BodyViewConfig c = (BodyViewConfig)super.clone();

        c.rootDirOnServer = this.rootDirOnServer;
        c.hasColoringData = this.hasColoringData;

        c.timeHistoryFile = this.timeHistoryFile;
        c.hasStateHistory = this.hasStateHistory;

        c.body = this.body;
        c.type = this.type;
        c.population = this.population;
        c.system = this.system;
        c.dataUsed = this.dataUsed;

        c.hasMapmaker = this.hasMapmaker;
        c.hasBigmap = this.hasBigmap;
        c.density = this.density;
        c.rotationRate = this.rotationRate;


        c.hasLineamentData = this.hasLineamentData;

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

    public void addFeatureConfig(Class<?> configClass, IFeatureConfig instrumentConfig)
    {
    	List<IFeatureConfig> tmpInstrumentConfigs;
    	if (featureConfigs.containsKey(configClass))
    	{
    		tmpInstrumentConfigs = featureConfigs.get(configClass);
    		tmpInstrumentConfigs.add(instrumentConfig);
    		featureConfigs.replace(configClass, tmpInstrumentConfigs);
    	}
    	else
    	{
    		tmpInstrumentConfigs = new ArrayList<IFeatureConfig>();
    		tmpInstrumentConfigs.add(instrumentConfig);
    		featureConfigs.put(configClass, tmpInstrumentConfigs);
    	}
    }

    protected void clearInstrumentConfig()
    {
    	featureConfigs.clear();
    }

	public Map<Class<?>, List<IFeatureConfig>> getFeatureConfigs()
	{
		return featureConfigs;
	}

	public List<IFeatureConfig> getConfigsForClass(Class<?> configClass)
	{
		if (featureConfigs.containsKey(configClass))
    	{
    		return featureConfigs.get(configClass);
    	}
		else
			return null;
	}

	public IFeatureConfig getConfigForClass(Class<?> configClass)
	{
		if (featureConfigs.containsKey(configClass))
    	{
    		return featureConfigs.get(configClass).get(0);
    	}
		else
			return null;
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

	public void setShapeModelFileNames(String[] shapeModelFileNames)
	{
		this.shapeModelFileNames = shapeModelFileNames;
	}

	public String getShapeModelFileBaseName()
	{
		return shapeModelFileBaseName;
	}

	public void setShapeModelFileBaseName(String shapeModelFileBaseName)
	{
		this.shapeModelFileBaseName = shapeModelFileBaseName;
	}

	public Mission[] getPresentInMissions()
	{
		return presentInMissions;
	}

	public Mission[] getDefaultForMissions()
	{
		return defaultForMissions;
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
		result = prime * result + (hasLineamentData ? 1231 : 1237);
		result = prime * result + (hasMapmaker ? 1231 : 1237);
		result = prime * result + (hasRemoteMapmaker ? 1231 : 1237);
		result = prime * result + (hasStateHistory ? 1231 : 1237);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((population == null) ? 0 : population.hashCode());
		result = prime * result + ((system == null) ? 0 : system.hashCode());
		result = prime * result + Arrays.hashCode(presentInMissions);
		result = prime * result + ((rootDirOnServer == null) ? 0 : rootDirOnServer.hashCode());
		temp = Double.doubleToLongBits(rotationRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((shapeModelFileBaseName == null) ? 0 : shapeModelFileBaseName.hashCode());
		result = prime * result + ((shapeModelFileExtension == null) ? 0 : shapeModelFileExtension.hashCode());
		result = prime * result + Arrays.hashCode(shapeModelFileNames);
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
//			System.err.println("BodyViewConfig: equals: lowest res model names don't match '" + bodyLowestResModelName + "' " + other.bodyLowestResModelName);
			return false;
		}
		if (Double.doubleToLongBits(bodyReferencePotential) != Double.doubleToLongBits(other.bodyReferencePotential))
		{
//			System.err.println("BodyViewConfig: equals: body reference potential doesn't match ");
			return false;
		}

		if (dataUsed != other.dataUsed)
		{
//			System.out.println("BodyViewConfig: equals: data used differs");
			return false;
		}
		if (!Arrays.equals(defaultForMissions, other.defaultForMissions))
		{
//			System.out.println("BodyViewConfig: equals: defaults for missions differ");
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
//				System.out.println("BodyViewConfig: equals: dtm browse");
				return false;
			}
		} else if (!dtmBrowseDataSourceMap.equals(other.dtmBrowseDataSourceMap))
		{
//			System.out.println("BodyViewConfig: equals: dtm browse ");
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
		if (hasStateHistory != other.hasStateHistory)
		{
			return false;
		}
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
		if (!Arrays.equals(shapeModelFileNames, other.getShapeModelFileNames()))
		{
//			System.out.println("BodyViewConfig: equals: shape model file name " + Arrays.toString(shapeModelFileNames) + " " + Arrays.toString(other.getShapeModelFileNames()));
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
