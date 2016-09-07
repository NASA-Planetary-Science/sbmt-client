package edu.jhuapl.near.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.saavtk.model.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;



/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public class BodyViewConfig extends ViewConfig
{
    public String rootDirOnServer;
    public double density = 0.0; // in units g/cm^3
    public double rotationRate = 0.0; // in units radians/sec

    public boolean hasColoringData = true;
    public boolean hasImageMap = false;

    public boolean hasMapmaker = false;
    public boolean hasBigmap = false;
    public boolean hasSpectralData = false;
    public boolean hasLineamentData = false;

    // if spectralModes is not empty, the following must be filled in
    public Date imageSearchDefaultStartDate;
    public Date imageSearchDefaultEndDate;
    public String[] imageSearchFilterNames;
    public String[] imageSearchUserDefinedCheckBoxesNames;
    public double imageSearchDefaultMaxSpacecraftDistance;
    public double imageSearchDefaultMaxResolution;

    public boolean hasHypertreeBasedLidarSearch=false;
    // if hasLidarData is true, the following must be filled in
    public Map<String, String> lidarSearchDataSourceMap;
    public int[] lidarBrowseXYZIndices;
    public int[] lidarBrowseSpacecraftIndices;
    public int lidarBrowseOutgoingIntensityIndex;
    public int lidarBrowseReceivedIntensityIndex;
    public boolean lidarBrowseIntensityEnabled = false;
    public boolean lidarBrowseIsSpacecraftInSphericalCoordinates;
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

    static public final int[] DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION = {
        49152,
        196608,
        786432,
        3145728
    };

    //
    // Additional variables not inherited from parent
    //

    public ShapeModelType type; // e.g. asteroid, comet, satellite
    public ShapeModelPopulation population; // e.g. Mars for satellites or main belt for asteroids
    public ShapeModelDataUsed dataUsed; // e.g. images, radar, lidar, or enhanced

    public ImagingInstrument[] imagingInstruments = {};
    public Instrument lidarInstrumentName = Instrument.LIDAR;

    public String getUniqueName()
    {
        if (ShapeModelAuthor.CUSTOM == author)
            return author + "/" + customName;
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

    public String getShapeModelName()
    {
        if (author == ShapeModelAuthor.CUSTOM)
            return customName;
        else
        {
            String ver = "";
            if (version != null)
                ver += " (" + version + ")";
            return body.toString() + ver;
        }
    }


    public String getPathRepresentation()
    {
        if (ShapeModelAuthor.CUSTOM == author)
        {
            return ShapeModelAuthor.CUSTOM + " > " + customName;
        }
        else
        {
            String path = type.str;
            if (population != null)
                path += " > " + population;
            path += " > " + body;
            if (dataUsed != null)
                path += " > " + dataUsed;
            if (author != null)
                path += " > " + author;
            if (version != null)
                path += " (" + version + ")";
            return path;
        }
    }

//
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

    public BodyViewConfig clone() // throws CloneNotSupportedException
    {
//      PolyhedralModelConfig c = new PolyhedralModelConfig();
        BodyViewConfig c = (BodyViewConfig)super.clone();

        c.rootDirOnServer = this.rootDirOnServer;
        c.hasColoringData = this.hasColoringData;
        c.hasImageMap = this.hasImageMap;

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
            c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>(this.lidarSearchDataSourceMap);
            c.lidarBrowseXYZIndices = this.lidarBrowseXYZIndices.clone();
            c.lidarBrowseSpacecraftIndices = this.lidarBrowseSpacecraftIndices.clone();
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = this.lidarBrowseIsSpacecraftInSphericalCoordinates;
            c.lidarBrowseTimeIndex = this.lidarBrowseTimeIndex;
            c.lidarBrowseNoiseIndex = this.lidarBrowseNoiseIndex;
            c.lidarBrowseOutgoingIntensityIndex = this.lidarBrowseOutgoingIntensityIndex;
            c.lidarBrowseReceivedIntensityIndex = this.lidarBrowseReceivedIntensityIndex;
            c.lidarBrowseIntensityEnabled = this.lidarBrowseIntensityEnabled;
            c.lidarBrowseFileListResourcePath = this.lidarBrowseFileListResourcePath;
            c.lidarBrowseNumberHeaderLines = this.lidarBrowseNumberHeaderLines;
            c.lidarBrowseIsInMeters = this.lidarBrowseIsInMeters;
            c.lidarBrowseIsBinary = this.lidarBrowseIsBinary;
            c.lidarBrowseBinaryRecordSize = this.lidarBrowseBinaryRecordSize;
            c.lidarOffsetScale = this.lidarOffsetScale;
        }
        c.customName = this.customName;
        c.customTemporary = this.customTemporary;

        return c;
    }
}
