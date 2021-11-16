package edu.jhuapl.sbmt.client.configs;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool.Mission;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.common.ImageType;
import edu.jhuapl.sbmt.image.common.SpectralImageMode;
import edu.jhuapl.sbmt.image.core.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.tools.DBRunInfo;

/**
 * DART mission-specific {@link SmallBodyViewConfig} implementation.
 *
 * @author James Peachey
 *
 */
public class DartConfigs
{

    private static final Mission[] DartClients = new SbmtMultiMissionTool.Mission[] { //
            SbmtMultiMissionTool.Mission.DART_DEV, //
            SbmtMultiMissionTool.Mission.DART_DEPLOY, //
            SbmtMultiMissionTool.Mission.DART_TEST, //
            SbmtMultiMissionTool.Mission.DART_STAGE, //
    };

    private static final Mission[] ClientsWithDartModels = new SbmtMultiMissionTool.Mission[] { //
            SbmtMultiMissionTool.Mission.APL_INTERNAL, //
            SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL, //
            SbmtMultiMissionTool.Mission.STAGE_APL_INTERNAL, //
            SbmtMultiMissionTool.Mission.DART_DEV, //
            SbmtMultiMissionTool.Mission.DART_DEPLOY, //
            SbmtMultiMissionTool.Mission.DART_TEST, //
            SbmtMultiMissionTool.Mission.DART_STAGE, //
    };

    private static final DartConfigs DefaultInstance = new DartConfigs();

    // Note: would strongly prefer *NOT TO DO IT THIS WAY* by hard-coding these
    // values. Doing it this way because currently there is no way to inject
    // special handling of keywords where the images are read in a
    // mission-independent way. A better option may exist in the future, at
    // which time this should be changed.
    private static final LinkedHashSet<Float> DracoFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> LeiaFillValues = null;
    private static final LinkedHashSet<Float> LukeFillValues = null;

    // Months are 0-based: SEPTEMBER 20 is 8, 20, not 9, 20.
    private static final Date ImageSearchDefaultStartDate = new GregorianCalendar(2022, 8, 20, 0, 0, 0).getTime();
    // Months are 0-based: OCTOBER 5 is 9, 5 not 10, 5.
    private static final Date ImageSearchDefaultEndDate = new GregorianCalendar(2022, 9, 5, 0, 0, 0).getTime();

    static
    {
        // The DART ICD defines FITS keywords that should hold the special
        // image values below.
        // These are expressed as floats even though we're dealing with
        // integer images. This is because the code that handles these
        // values is hard-wired to use floats, so we don't have a better
        // option right now. In principle this should work.
        DracoFillValues.add(-32768f); // MISPXVAL for 16-bit integer images.
        DracoFillValues.add(-32767f); // PXOUTWIN for 16-bit integer images.
        DracoFillValues.add(4095f); // SNAVFLAG for 16-bit integer images.
    }

    public static DartConfigs instance()
    {
        return DefaultInstance;
    }

    protected static final ImageSource[] InfoFiles = new ImageSource[] { ImageSource.SPICE };

    protected static final ImageSource[] InfoFilesAndCorrectedInfoFiles = new ImageSource[] { ImageSource.CORRECTED_SPICE, ImageSource.SPICE };

    protected DartConfigs()
    {
        super();
    }

    /**
     * Initialize all DART-specific models, adding them all to the supplied
     * configList.
     *
     * @param configList output list of ViewConfig objects.
     */
    public void initialize(List<ViewConfig> configList)
    {
        Preconditions.checkNotNull(configList);

        SmallBodyViewConfig c;



        // Ideal Didymos models.
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 1 20200629 v01", 1996);
        configList.add(c);

        // Make this the default model.
        final SmallBodyViewConfig defaultConfig = c;

        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 2 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 3 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 4 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Ideal Impact 4 RA 20210211 v01", 3145728, //
                "ideal-impact4-20200629-v01", null, null );
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 5 20200629 v01", 3145728);
        configList.add(c);

        // This one was only partially delivered. It was put on hold when
        // problems surfaced with the SPICE pointings. Leaving it here but not
        // adding it to configList.
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Ideal Impact 6 RA 20201116 v01", 3145728);
//        configList.add(c);

        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Ideal Impact 9 20210630 v01", 1996);
        configList.add(c);



        // Ideal Dimorphos models.
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 1 20200629 v01", 3072);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 2 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 3 20200629 v01", 3366134);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 4 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Ideal Impact 4 RA 20210211 v01", 3145728, //
                "ideal-impact4-20200629-v01", null, null );
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 5 20200629 v01", 3366134);
        configList.add(c);

        // This one was only partially delivered. It was put on hold when
        // problems surfaced with the SPICE pointings. Leaving it here but not
        // adding it to configList.
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Ideal Impact 6 RA 20201116 v01", 3145728);
//        configList.add(c);

        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Ideal Impact 9 20210630 v01", 3072);
        configList.add(c);



        // Errors Didymos models.
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Errors Impact 1 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Errors Impact 2 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Errors Impact 3 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Errors Impact 4 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Errors Impact 5 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Errors Impact 9 20210630 v01", 3072);
        configList.add(c);



        // Errors Dimorphos models.
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Errors Impact 1 20200629 v01", 3072);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Errors Impact 2 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Errors Impact 3 20200629 v01", 3366134);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Errors Impact 4 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Errors Impact 5 20200629 v01", 3366134);
        configList.add(c);
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Errors Impact 9 20210630 v01", 3072);
        configList.add(c);


        //System configs
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 1 20200629 v01 System Didymos Center", 1996);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 1 20200629 v01 System Dimorphos Center", 3072);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 2 20200629 v01 System Didymos Center", 1996);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 2 20200629 v01 System Dimorphos Center", 3145728);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 3 20200629 v01 System Didymos Center", 1996);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 3 20200629 v01 System Dimorphos Center", 3366134);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 4 20200629 v01 System Didymos Center", 3145728);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 4 20200629 v01 System Dimorphos Center", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 4 RA 20210211 v01 System Didymos Center", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 4 RA 20210211 v01 System Dimorphos Center", 3145728);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 5 20200629 v01 System Didymos Center", 3145728);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 5 20200629 v01 System Dimorphos Center", 3366134);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 6 RA 20201116 v01 System Didymos Center", 3145728);
//        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 6 RA 20201116 v01 System Dimorphos Center", 3145728);
//        configList.add(c);

        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 9 20210630 v01 System Didymos Center", 1996, true);
        configList.add(c);

        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 9 20210630 v01 System Dimorphos Center", 3072, true);
        configList.add(c);


//
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 1 20200629 v01 System Didymos Center", 1996);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 1 20200629 v01 System Dimorphos Center", 3072);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 2 20200629 v01 System Didymos Center", 1996);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 2 20200629 v01 System Dimorphos Center", 31457278);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 3 20200629 v01 System Didymos Center", 1996);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 3 20200629 v01 System Dimorphos Center", 3366134);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 4 20200629 v01 System Didymos Center", 3145728);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 4 20200629 v01 System Dimorphos Center", 31457278);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 5 20200629 v01 System Didymos Center", 3145728);
        configList.add(c);
        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 5 20200629 v01 System Dimorphos Center", 3366134);
        configList.add(c);
        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 9 20210630 v01 System Didymos Center", 3072, true);
        configList.add(c);
        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 9 20210630 v01 System Dimorphos Center", 3072, true);
        configList.add(c);

        defaultConfig.defaultForMissions = DartClients;
    }

    protected SmallBodyViewConfig createSingleResolutionSystemConfig_20200629_v01(ShapeModelBody[] body, String label, int numberPlates)
    {
    	SmallBodyViewConfig config = createSingleResolutionConfig_20200629_v01(body[0], label, numberPlates);
    	List<SmallBodyViewConfig> systemConfigs = Lists.newArrayList();
    	for (int i=1; i<body.length; i++)
    	{
    		systemConfigs.add(createSingleResolutionConfig_20200629_v01(body[i], label, numberPlates));
    	};
    	config.systemConfigs = systemConfigs;
    	config.hasSystemBodies = true;
    	config.body = ShapeModelBody.DIDYMOS_SYSTEM;
    	config.hasDTMs = false;
    	config.rootDirOnServer = "/" + body[0].name().replaceAll("[\\s-_]+", "-").toLowerCase() + "/" + config.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
    	config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\(", "");
    	config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\)", "");
    	config.rootDirOnServer = config.rootDirOnServer.replaceAll("-\\w*-center", "");
    	return config;
    }

    protected SmallBodyViewConfig createSingleResMissionImagesSystemConfig(ShapeModelBody[] body, String label, int numberPlates)
    {
    	return createSingleResMissionImagesSystemConfig(body, label, numberPlates, false);
    }

    protected SmallBodyViewConfig createSingleResMissionImagesSystemConfig(ShapeModelBody[] body, String label, int numberPlates, boolean useUpdatedFrameNames)
    {
    	SmallBodyViewConfig config = createSingleResMissionImagesConfig(body[0], label, numberPlates, useUpdatedFrameNames);
    	List<SmallBodyViewConfig> systemConfigs = Lists.newArrayList();
    	for (int i=1; i<body.length; i++)
    	{
    		systemConfigs.add(createSingleResMissionImagesConfig(body[i], label, numberPlates, useUpdatedFrameNames));
    	};
    	config.systemConfigs = systemConfigs;
    	config.hasSystemBodies = true;
    	config.body = ShapeModelBody.DIDYMOS_SYSTEM;
        config.hasDTMs = false;
    	config.rootDirOnServer = "/" + body[0].name().replaceAll("[\\s-_]+", "-").toLowerCase() + "/" + config.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
    	config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\(", "");
    	config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\)", "");
    	config.rootDirOnServer = config.rootDirOnServer.replaceAll("-\\w*-center", "");
    	return config;
    }

    /**
     * Create a single-resolution model for the given input parameters. This
     * creates configurations that are consistent with the set of simulated
     * models, images and SPICE files delivered starting in August, 2020, based
     * on the DART simulations identified as 20200629-v01.
     * <p>
     * These deliveries were initially processed under redmine-2271, using
     * versions of the scripts that located images in the model hierarchy
     * (model/<imager>/images). Some images had one or the other or both of the
     * bodies Didymos and Dimorphos in the FOV, but ALL the images were
     * delivered, processed, archived and stored TWICE: once for each body.
     * <p>
     * Later, under redmine-2361, this method was modified to continue to work
     * for the 20200629-v01 simulations regarding flips and rotations etc., but
     * to use the more modern mission hierarchy (<mission>/<imager>/images) to
     * lay out the directories.
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param label the label exactly as the model should appear in the menu.
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResolutionConfig_20200629_v01(ShapeModelBody body, String label, int numberPlates)
    {
        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates)) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        // ShapeModelType rules: no spaces (replace with underscores). Mixed
        // case, underscores and dashes are all OK. Includes a DART-specific
        // hack to remove one dash that was not present in the early models.
        ShapeModelType author = ShapeModelType.provide(label.replaceAll(" System", "").replaceAll("\\s+", "-").toLowerCase().replace("impact-", "impact"));

        // Model identifier string rules: lowercase, no spaces nor underscores
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        String modelId = author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        modelId = modelId.replaceAll("-\\w*-center", "");

        // Body identifier string rules: lowercase, no spaces nor underscores.
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        String bodyId = body.name().replaceAll("[\\s-_]+", "-").toLowerCase();

        c.body = body;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.system = ShapeModelBody.DIDYMOS_SYSTEM;
        c.dataUsed = ShapeModelDataUsed.SIMULATED;
        c.author = author;
        c.modelLabel = label;
        c.rootDirOnServer = "/" + bodyId + "/" + modelId;
        c.presentInMissions = ClientsWithDartModels;
        // c.defaultForMissions = ...
        c.setShapeModelFileExtension(".obj");

        // Database table rules: lowercase, no dashes (replace with
        // underscores). Underscores are OK.
        String tableBaseName = (bodyId + "_" + modelId + "_").replaceAll("-", "_").toLowerCase();

        String dracoDir = c.rootDirOnServer + "/draco";
        String dracoTable = tableBaseName + "draco";
        String dracoDataDir = "/dart/draco/" + modelId + "/";

        String leiaDir = c.rootDirOnServer + "/leia";
        String leiaTable = tableBaseName + "leia";
        String leiaDataDir = "/dart/leia/" + modelId + "/";

        String lukeDir = c.rootDirOnServer + "/luke";
        String lukeTable = tableBaseName + "luke";
        String lukeDataDir = "/dart/luke/" + modelId + "/";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(dracoDir, dracoTable, dracoTable, dracoDataDir + "gallery", dracoDataDir + "images"), //
                        ImageType.valueOf("DRACO_IMAGE"), //
                        InfoFiles, //
                        Instrument.DRACO, //
                        270., //
                        "None", //
                        DracoFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, leiaDataDir + "gallery", leiaDataDir + "images"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFilesAndCorrectedInfoFiles, //
                        Instrument.LEIA, //
                        270., //
                        "None", //
                        LeiaFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
                        InfoFiles, //
                        Instrument.LUKE, //
                        270., //
                        "None", //
                        LukeFillValues //
                ),
        };

        c.imageSearchDefaultStartDate = ImageSearchDefaultStartDate;
        c.imageSearchDefaultEndDate = ImageSearchDefaultEndDate;
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 1.0e4;
        c.imageSearchDefaultMaxResolution = 1.0e3;

        c.databaseRunInfos = new DBRunInfo[] { //
                new DBRunInfo(ImageSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(ImageSource.CORRECTED_SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };
        generateStateHistoryParameters(c,  body.name());
        return c;
    }

    /**
     * Create a single-resolution model for the given input parameters. This
     * creates configurations that are consistent with the set of simulated
     * models, images and SPICE files delivered starting in December, 2020
     * through March, 2021, based on the DART simulations identified as
     * 20201116-v01 and 20210211-v01.
     * <p>
     * These deliveries were processed using versions of the scripts that were
     * set up to handle images under the mission/instrument directory. These
     * were processed AFTER the Saturnian moon models that were processed in
     * October-Novebmer 2020.
     * <p>
     * For these deliveries, images were located under the
     * <mission>/<instrument>/<model> hierarchy. Some images had one or the
     * other or both of the bodies Didymos and Dimorphos in the FOV. Only one
     * set of images for each instrument were delivered, processed etc. However,
     * since these images are simulation-specific simulated images, needed to
     * put them under one more level of subdirectory, i.e.
     * mission/instrument/model (but note no body in this hierarchy).
     * <p>
     * This method derives the author from the label in a DART simulated
     * model-specific way, then derives the modelId from the author. The same
     * modelId is used for the model and also to locate images for all three
     * imagers (DRACO, LEIA, LUKE).
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param label the label exactly as the model should appear in the menu
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResMissionImagesConfig(ShapeModelBody body, String label, int numberPlates)
    {
        return createSingleResMissionImagesConfig(body, label, numberPlates, null, null, null, false);
    }

    protected SmallBodyViewConfig createSingleResMissionImagesConfig(ShapeModelBody body, String label, int numberPlates, boolean useUpdatedFrameNames)
    {
        return createSingleResMissionImagesConfig(body, label, numberPlates, null, null, null, useUpdatedFrameNames);
    }

    protected SmallBodyViewConfig createSingleResMissionImagesConfig( //
            ShapeModelBody body, //
            String label, //
            int numberPlates, //
            String dracoModelId, //
            String leiaModelId, //
            String lukeModelId //
    )
    {
    	return createSingleResMissionImagesConfig(body, label, numberPlates, dracoModelId, leiaModelId, lukeModelId, false);
    }

    /**
     * /** Create a single-resolution model for the given input parameters. This
     * creates configurations that are consistent with the set of simulated
     * models, images and SPICE files delivered starting in December, 2020
     * through March, 2021, based on the DART simulations identified as
     * 20201116-v01 and 20210211-v01.
     * <p>
     * These deliveries were processed using versions of the scripts that were
     * set up to handle images under the mission/instrument directory. These
     * were processed AFTER the Saturnian moon models that were processed in
     * October-Novebmer 2020.
     * <p>
     * For these deliveries, images were located under the
     * <mission>/<instrument>/<model> hierarchy. Some images had one or the
     * other or both of the bodies Didymos and Dimorphos in the FOV. Only one
     * set of images for each instrument were delivered, processed etc. However,
     * since these images are simulation-specific simulated images, needed to
     * put them under one more level of subdirectory, i.e.
     * mission/instrument/model (but note no body in this hierarchy).
     * <p>
     * This method derives the author from the label in a DART simulated
     * model-specific way, then derives the modelId from the author. Separate
     * modelIds are explicitly specified to allow this model to use images from
     * any other DART simulated model. If null is passed for any/all of these
     * parameters, the main modelId is used to locate those images.
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param label the label exactly as the model should appear in the menu
     * @param numberPlates the number of plates in the single resolution model
     * @param dracoModelId the model used to locate DRACO images, or null
     * @param leiaModelId the model used to locate LEIA images, or null
     * @param lukeModelId the model used to locate LUKE images, or null
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResMissionImagesConfig( //
            ShapeModelBody body, //
            String label, //
            int numberPlates, //
            String dracoModelId, //
            String leiaModelId, //
            String lukeModelId, //
            boolean useUpdatedFrameNames
    )
    {
        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates)) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        // ShapeModelType rules: no spaces (replace with underscores). Mixed
        // case, underscores and dashes are all OK. Includes a DART-specific
        // hack to remove one dash that was not present in the early models.
        ShapeModelType author = ShapeModelType.provide(label.replaceAll(" System", "").replaceAll("\\s+", "-").toLowerCase().replace("impact-", "impact"));

        // Model identifier string rules: lowercase, no spaces nor underscores
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        String modelId = author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        modelId = modelId.replaceAll("-\\w*-center", "");

        // Body identifier string rules: lowercase, no spaces nor underscores.
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        String bodyId = body.name().replaceAll("[\\s-_]+", "-").toLowerCase();

        c.body = body;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.system = ShapeModelBody.DIDYMOS_SYSTEM;
        c.dataUsed = ShapeModelDataUsed.SIMULATED;
        c.author = author;
        c.modelLabel = label;
        c.rootDirOnServer = "/" + bodyId + "/" + modelId;
        c.presentInMissions = ClientsWithDartModels;
        // c.defaultForMissions = ...
        c.setShapeModelFileExtension(".obj");

        // Database table rules: lowercase, no dashes (replace with
        // underscores). Underscores are OK.
        String tableBaseName = (bodyId + "_" + modelId + "_").replaceAll("-", "_").toLowerCase();

        String dracoDir = c.rootDirOnServer + "/draco";
        String dracoTable = tableBaseName + "draco";
        String dracoDataDir = "/dart/draco/" + (dracoModelId != null ? dracoModelId : modelId) + "/";

        String leiaDir = c.rootDirOnServer + "/leia";
        String leiaTable = tableBaseName + "leia";
        String leiaDataDir = "/dart/leia/" + (leiaModelId != null ? leiaModelId : modelId) + "/";

        String lukeDir = c.rootDirOnServer + "/luke";
        String lukeTable = tableBaseName + "luke";
        String lukeDataDir = "/dart/luke/" + (lukeModelId != null ? lukeModelId : modelId) + "/";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(dracoDir, dracoTable, dracoTable, dracoDataDir + "gallery", dracoDataDir + "images"), //
                        ImageType.valueOf("DRACO_IMAGE"), //
                        InfoFiles, //
                        Instrument.DRACO, //
                        0., //
                        "None", //
                        DracoFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, leiaDataDir + "gallery", leiaDataDir + "images"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFiles, //
                        Instrument.LEIA, //
                        0., //
                        "None", //
                        LeiaFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
                        InfoFiles, //
                        Instrument.LUKE, //
                        90., //
                        "X", //
                        LukeFillValues, //
                        false //
                ),
        };

        c.imageSearchDefaultStartDate = ImageSearchDefaultStartDate;
        c.imageSearchDefaultEndDate = ImageSearchDefaultEndDate;
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 1.0e4;
        c.imageSearchDefaultMaxResolution = 1.0e3;

        c.databaseRunInfos = new DBRunInfo[] { //
                new DBRunInfo(ImageSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };
        if (useUpdatedFrameNames)
        	generateUpdatedStateHistoryParameters(c,  body.name());
        else
        	generateStateHistoryParameters(c,  body.name());
        return c;
    }

    private void generateStateHistoryParameters(SmallBodyViewConfig c, String centerBodyName)
	{
        c.hasStateHistory = false;
        c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
        c.stateHistoryStartDate = new GregorianCalendar(2022, 9, 1, 10, 25, 8).getTime();
        c.stateHistoryEndDate = new GregorianCalendar(2022, 9, 1, 10, 28, 0).getTime();
        SpiceInfo spiceInfo1 = new SpiceInfo("DART", "920065803_FIXED", "DART_SPACECRAFT", "DIDYMOS", new String[] {"DIMORPHOS"}, new String[] {"DART_DRACO_2X2", "120065803_FIXED"});
		SpiceInfo spiceInfo2 = new SpiceInfo("DART", "120065803_FIXED", "DART_SPACECRAFT", "DIMORPHOS", new String[] {"DIDYMOS"}, new String[] {"DART_DRACO_2X2", "920065803_FIXED"});
		SpiceInfo[] spiceInfos = new SpiceInfo[] {spiceInfo1, spiceInfo2};
        c.spiceInfo = List.of(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).toList().get(0);
	}

    private void generateUpdatedStateHistoryParameters(SmallBodyViewConfig c, String centerBodyName)
	{
        c.hasStateHistory = false;
        c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
        c.stateHistoryStartDate = new GregorianCalendar(2022, 8, 26, 23, 10, 18).getTime();
        c.stateHistoryEndDate = new GregorianCalendar(2022, 8, 26, 23, 13, 30).getTime();
        SpiceInfo spiceInfo1 = new SpiceInfo("DART", "DIDYMOS_FIXED", "DART_SPACECRAFT", "DIDYMOS", new String[] {"DIMORPHOS"}, new String[] {"DART_DRACO_2X2", "DIMORPHOS_FIXED"});
		SpiceInfo spiceInfo2 = new SpiceInfo("DART", "DIMORPHOS_FIXED", "DART_SPACECRAFT", "DIMORPHOS", new String[] {"DIDYMOS"}, new String[] {"DART_DRACO_2X2", "DIDYMOS_FIXED"});
		SpiceInfo[] spiceInfos = new SpiceInfo[] {spiceInfo1, spiceInfo2};
        c.spiceInfo = List.of(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).toList().get(0);
	}

}
