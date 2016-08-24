package edu.jhuapl.near.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.SmallBodyConfig.Instrument;


/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public abstract class PolyhedralModelConfig extends Config
{
    public String version; // e.g. 2.0
    public String rootDirOnServer;
    public int[] smallBodyNumberOfPlatesPerResolutionLevel; // only needed when number resolution levels > 1
    public boolean useMinimumReferencePotential = false; // uses average otherwise
    public double density = 0.0; // in units g/cm^3
    public double rotationRate = 0.0; // in units radians/sec

    public boolean hasColoringData = true;
    public boolean hasImageMap = false;
    public ImagingInstrument[] imagingInstruments = {};

    public boolean hasMapmaker = false;
    public boolean hasBigmap = false;
    public boolean hasSpectralData = false;
    public boolean hasLineamentData = false;
    public boolean hasCustomBodyCubeSize = false;

    // if hasCustomBodyCubeSize is true, the following must be filled in and valid
    public double customBodyCubeSize; // km

    // if spectralModes is not empty, the following must be filled in
    public Date imageSearchDefaultStartDate;
    public Date imageSearchDefaultEndDate;
    public String[] imageSearchFilterNames;
    public String[] imageSearchUserDefinedCheckBoxesNames;
    public double imageSearchDefaultMaxSpacecraftDistance;
    public double imageSearchDefaultMaxResolution;

    public boolean hasHypertreeBasedLidarSearch=false;
    // if hasLidarData is true, the following must be filled in
    public Instrument lidarInstrumentName = Instrument.LIDAR;
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
    public String customName;
    public boolean customTemporary = false;

    public boolean hasLidarData = false;
    public Date lidarSearchDefaultStartDate;
    public Date lidarSearchDefaultEndDate;

    public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1

    public String getPathRepresentation()
    {
        return "Default Path Representation";
    }

    //
    // Clone operation
    //

    protected PolyhedralModelConfig clone()
    {
        PolyhedralModelConfig c = new SmallBodyConfig();

        c.rootDirOnServer = this.rootDirOnServer;
        c.hasColoringData = this.hasColoringData;
        c.hasImageMap = this.hasImageMap;

        // deep clone imaging instruments
        if (this.imagingInstruments != null)
        {
            int length = this.imagingInstruments.length;
            c.imagingInstruments = new ImagingInstrument[length];
            for (int i = 0; i < length; i++)
                c.imagingInstruments[i] = this.imagingInstruments[i].clone();
        }

        c.hasLidarData = this.hasLidarData;
        c.hasMapmaker = this.hasMapmaker;
        c.hasBigmap = this.hasBigmap;
        c.density = this.density;
        c.rotationRate = this.rotationRate;
        c.useMinimumReferencePotential = this.useMinimumReferencePotential;
        c.hasSpectralData = this.hasSpectralData;
        c.hasLineamentData = this.hasLineamentData;
        c.hasCustomBodyCubeSize = this.hasCustomBodyCubeSize;
        c.customBodyCubeSize = this.customBodyCubeSize;
        if (this.smallBodyLabelPerResolutionLevel != null)
            c.smallBodyLabelPerResolutionLevel = this.smallBodyLabelPerResolutionLevel.clone();
        if (this.smallBodyNumberOfPlatesPerResolutionLevel != null)
            c.smallBodyNumberOfPlatesPerResolutionLevel = this.smallBodyNumberOfPlatesPerResolutionLevel.clone();

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
            c.lidarInstrumentName = this.lidarInstrumentName;
        }
        c.customName = this.customName;
        c.customTemporary = this.customTemporary;
        return c;
    }


    /**
     * Return a unique name for this model. No other model may have this
     * name. Note that only applies within built-in models or custom models
     * but a custom model can share the name of a built-in one or vice versa.
     * By default simply return the author concatenated with the
     * name if the author is not null or just the name if the author
     * is null.
     * @return
     */
    public String getUniqueName()
    {
        return "Default Unique Name";
    }

    public String getShapeModelName()
    {
        return "Default Shape Model Name";
    }
}
