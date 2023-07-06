package edu.jhuapl.sbmt.client2.configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImageFlip;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.OrientationFactory;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.tools.DBRunInfo;

/**
 * DART mission-specific {@link SmallBodyViewConfig} implementation.
 *
 * @author James Peachey
 *
 */
public class DartConfigs extends SmallBodyViewConfigBuilder
{

    private static final Mission[] APLClients = new Mission[] { //
            Mission.APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
            Mission.STAGE_APL_INTERNAL, //
    };

    private static final Mission[] DartClients = new Mission[] { //
            Mission.DART_DEV, //
            Mission.DART_DEPLOY, //
            Mission.DART_TEST, //
            Mission.DART_STAGE, //
    };

    private static final Mission[] ClientsWithDartModels = new Mission[] { //
            Mission.APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
            Mission.STAGE_APL_INTERNAL, //
            Mission.DART_DEV, //
            Mission.DART_DEPLOY, //
            Mission.DART_TEST, //
            Mission.DART_STAGE, //
    };

    private static final DartConfigs DefaultInstance = new DartConfigs();

    private static final String MissionPrefix = "DART";

    // Note: would strongly prefer *NOT TO DO IT THIS WAY* by hard-coding these
    // values. Doing it this way because currently there is no way to inject
    // special handling of keywords where the images are read in a
    // mission-independent way. A better option may exist in the future, at
    // which time this should be changed.
    private static final LinkedHashSet<Float> DracoFlightFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> LeiaFlightFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> LukeFlightFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> DracoTestFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> LeiaFillValues = null;
    private static final LinkedHashSet<Float> LukeFillValues = null;

    // Months (only) are 0-based: SEPTEMBER 20 is 8, 20, not 9, 20.
    private static final Date ImageSearchDefaultStartDate = new GregorianCalendar(2022, 8, 20, 0, 0, 0).getTime();
    // Months (only) are 0-based: OCTOBER 5 is 9, 5 not 10, 5.
    private static final Date ImageSearchDefaultEndDate = new GregorianCalendar(2022, 9, 5, 0, 0, 0).getTime();

    // Months (only) are 0-based: JULY 1 is 6, 1, not 7, 1.
    private static final Date JupiterSearchStartDate = new GregorianCalendar(2022, 6, 1, 0, 0, 0).getTime();
    // Months (only) are 0-based: JULY 2 is 6, 2 not 7, 2.
    private static final Date JupiterSearchEndDate = new GregorianCalendar(2022, 6, 2, 0, 0, 0).getTime();

    // Months (only) are 0-based: SEPTEMBER 26 is 8, 26.
    // These values were specified in an update to Redmine issue #2472.
    private static final Date DimorphosImpactSearchStartDate = new GregorianCalendar(2022, 8, 26, 22, 0, 0).getTime();
    private static final Date DimorphosImpactSearchEndDate = new GregorianCalendar(2022, 8, 26, 23, 14, 25).getTime();
    private static final double DimorphosImpactMaxScDistance = 10000.0; // km
    private static final double DimorphosImpactResolution = 300.0; // mpp

    // These values were specified in an update to Redmine issue #2496.
    private static final Date DidymosImpactSearchStartDate = new GregorianCalendar(2022, 8, 26, 23, 10, 39).getTime();
    private static final Date DidymosImpactSearchEndDate = new GregorianCalendar(2022, 8, 26, 23, 12, 57).getTime();
    private static final double DidymosImpactMaxScDistance = 1500.0; // km
    private static final double DidymosImpactResolution = 7.0; // mpp

    static
    {
        // Constants for flight images:
        DracoFlightFillValues.add(+1e10f); // MISPXVAL
        DracoFlightFillValues.add(-1e10f); // PXOUTWIN
        DracoFlightFillValues.add(4095f); // SNAVFLAG
        DracoFlightFillValues.add(-1e09f); // BADMASKV
        DracoFlightFillValues.add(+1e09f); // SATPXVAL

        // Constants for test images:
        // The DART ICD defines FITS keywords that should hold the special
        // image values below.
        // These are expressed as floats even though we're dealing with
        // integer images. This is because the code that handles these
        // values is hard-wired to use floats, so we don't have a better
        // option right now. In principle this should work.
        DracoTestFillValues.add(-32768f); // MISPXVAL for 16-bit integer images.
        DracoTestFillValues.add(-32767f); // PXOUTWIN for 16-bit integer images.
        DracoTestFillValues.add(4095f); // SNAVFLAG for 16-bit integer images.
    }

    public static DartConfigs instance()
    {
        return DefaultInstance;
    }

    protected static final PointingSource[] InfoFiles = new PointingSource[] { PointingSource.SPICE };

    protected static final PointingSource[] InfoFilesAndCorrectedInfoFiles = new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE };

    protected static final PointingSource[] SumFilesAndInfoFiles = new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE };

    protected DartConfigs()
    {
        super();
    }

    @Override
    protected void doInit()
    {
        super.doInit();

        // Set up DART-specific fields.
        clients(ClientsWithDartModels);
        imageSearchRanges(ImageSearchDefaultStartDate, ImageSearchDefaultEndDate, 1.0e4, 1.0e3);
    }

    /**
     * Initialize all DART-specific models, adding them all to the supplied
     * configList.
     *
     * @param configList output list of ViewConfig objects.
     */
    public void initialize(List<IBodyViewConfig> configList)
    {
        Preconditions.checkNotNull(configList);

        SmallBodyViewConfig c;

        // There must be exactly one defaultConfig set somewhere below.
        final SmallBodyViewConfig defaultConfig;

        // Ideal Didymos models.
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 1 20200629 v01", 1996);
        configList.add(c);

        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 2 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 3 20200629 v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 4 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesConfig_20210211_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 4 RA 20210211 v01", 3145728, //
                "ideal-impact4-20200629-v01", null, null);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 5 20200629 v01", 3145728);
        configList.add(c);

        // This one was only partially delivered. It was put on hold when
        // problems surfaced with the SPICE pointings. Leaving it here but not
        // adding it to configList.
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Ideal Impact 6 RA 20201116 v01", 3145728);
        // configList.add(c);

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
        c = createSingleResMissionImagesConfig_20210211_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 4 RA 20210211 v01", 3145728, //
                "ideal-impact4-20200629-v01", null, null);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 5 20200629 v01", 3366134);
        configList.add(c);

        // This one was only partially delivered. It was put on hold when
        // problems surfaced with the SPICE pointings. Leaving it here but not
        // adding it to configList.
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Ideal Impact 6 RA 20201116 v01", 3145728);
        // configList.add(c);

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

        // As of initial delivery, no LEIA or LUKE images for this model, so
        // suppress tabs for those imagers.
        c.imagingInstruments = new ImagingInstrument[] { c.imagingInstruments[0] };

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

        // As of initial delivery, no LEIA or LUKE images for this model, so
        // suppress tabs for those imagers.
        c.imagingInstruments = new ImagingInstrument[] { c.imagingInstruments[0] };

        configList.add(c);

        // Flight models below this point.
        final PointingSource[] spcSources = { PointingSource.GASKELL };
        final PointingSource[] imageSources = { PointingSource.GASKELL, PointingSource.SPICE };

        Map<PointingSource, Orientation> dracoFlightOrientations = new LinkedHashMap<>();
        dracoFlightOrientations.put(PointingSource.GASKELL, new OrientationFactory().of(ImageFlip.X, 0.0, true));
        dracoFlightOrientations.put(PointingSource.SPICE, new OrientationFactory().of(ImageFlip.X, 180.0, true));

        // We have no LUKE images of Jupiter, but putting this here as a
        // reminder that LUKE images are multi-spectral for when we do have
        // LUKE images.
        @SuppressWarnings("unused")
        SpectralImageMode lukeImageMode = SpectralImageMode.MULTI;

        // Similarly, in past models LUKE images alone were not transposed,
        // so putting this here as a reminder...
        @SuppressWarnings("unused")
        boolean lukeTranspose = false;

//        // Jupiter system models.
//        {
//            String label = MissionPrefix + " " + "Jupiter-v01";
//            ShapeModelType author = author(label);
//
//            init(ShapeModelBody.JUPITER, author, ShapeModelDataUsed.SIMULATED, label);
//            imageSearchRanges(JupiterSearchStartDate, JupiterSearchEndDate, 1.0e9, 1.0e7);
//
//            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.JUPITER, author, Instrument.DRACO, dracoFlightOrientations, imageSources);
//            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.JUPITER, author, Instrument.DRACO, imageSources);
//            add(instrument, dbRunInfos);
//
//            clients(APLClients);
//
//            c = build();
//            configList.add(c);
//        }
//
//        {
//            String label = MissionPrefix + " " + "Ganymede-v01";
//            ShapeModelType author = author(label);
//
//            init(ShapeModelBody.GANYMEDE, author, ShapeModelDataUsed.SIMULATED, label);
//            imageSearchRanges(JupiterSearchStartDate, JupiterSearchEndDate, 1.0e9, 1.0e7);
//
//            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.GANYMEDE, author, Instrument.DRACO, dracoFlightOrientations, imageSources);
//            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.GANYMEDE, author, Instrument.DRACO, imageSources);
//            add(instrument, dbRunInfos);
//
//            clients(APLClients);
//
//            c = build();
//            configList.add(c);
//        }

        // Dimorphos version 002 (flight).
        {
            String label = "Dimorphos-v002";
            ShapeModelType author = author(MissionPrefix + " " + label);

            init(ShapeModelBody.DIMORPHOS, author, ShapeModelDataUsed.IMAGE_BASED, label);
            imageSearchRanges(DimorphosImpactSearchStartDate, DimorphosImpactSearchEndDate, DimorphosImpactMaxScDistance, DimorphosImpactResolution);

            modelRes(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION);

            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.DIMORPHOS, author, Instrument.DRACO, dracoFlightOrientations, imageSources);
            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.DIMORPHOS, author, Instrument.DRACO, imageSources);
            add(instrument, dbRunInfos);

            c = build();
            // This model was abandoned after import. Do NOT include it.
            // configList.add(c);
            generateUpdatedStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());
        }

        // Dimorphos version 003 (flight).
        {
            String label = "Dimorphos-v003";
            ShapeModelType author = author(MissionPrefix + " " + label);

            init(ShapeModelBody.DIMORPHOS, author, ShapeModelDataUsed.IMAGE_BASED, label);
            imageSearchRanges(DimorphosImpactSearchStartDate, DimorphosImpactSearchEndDate, DimorphosImpactMaxScDistance, DimorphosImpactResolution);

            modelRes(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION);

            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.DIMORPHOS, author, Instrument.DRACO, dracoFlightOrientations, imageSources);
            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.DIMORPHOS, author, Instrument.DRACO, imageSources);
            add(instrument, dbRunInfos);

            gravityInputs(2300.0, 1.464e-4);

            dtmCatalogs(true);

            c = build();
            generateUpdatedStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());
            configList.add(c);

            defaultConfig = c;
        }

        // Didymos version 001 (flight).
        {
            String label = "Didymos-v001";
            ShapeModelType author = author(MissionPrefix + " " + label);

            init(ShapeModelBody.DIDYMOS, author, ShapeModelDataUsed.IMAGE_BASED, label);
            imageSearchRanges(DidymosImpactSearchStartDate, DidymosImpactSearchEndDate, DidymosImpactMaxScDistance, DidymosImpactResolution);

            modelRes(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION);

            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.DIDYMOS, author, Instrument.DRACO, dracoFlightOrientations, spcSources);
            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.DIDYMOS, author, Instrument.DRACO, spcSources);
            add(instrument, dbRunInfos);

            gravityInputs(2400.0, 7.72e-4);

            // No DTMs with initial delivery.
            // dtmCatalogs(true);

            c = build();
            generateUpdatedStateHistoryParameters(c, ShapeModelBody.DIDYMOS.name());
            configList.add(c);
        }

        defaultConfig.defaultForMissions = DartClients;

        buildSystemConfigs(configList);
    }

    /**
     * Not called, not sure what this is for. Leaving it just in case we
     * remember later, but it could also be removed after some time.
     *
     * @param body
     * @param label
     * @param numberPlates
     * @return
     */
    @Deprecated
    protected SmallBodyViewConfig createSingleResolutionConfig_20210630_v01(ShapeModelBody body, String label, int numberPlates)
    {
        return createSingleResolutionConfig_20200629_v01(body, label, numberPlates);
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

        c.imagingInstruments = new ImagingInstrument[] { //
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(dracoDir, dracoTable, dracoTable, dracoDataDir + "gallery", dracoDataDir + "images"), //
                        ImageType.valueOf("DRACO_IMAGE"), //
                        InfoFiles, //
                        Instrument.DRACO, //
                        270.0, //
                        "None", //
                        DracoTestFillValues //
                ), new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, leiaDataDir + "gallery", leiaDataDir + "images"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFilesAndCorrectedInfoFiles, //
                        Instrument.LEIA, //
                        270.0, //
                        "None", //
                        LeiaFillValues //
                ), new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
                        InfoFiles, //
                        Instrument.LUKE, //
                        270.0, //
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
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };
        generateStateHistoryParameters(c, body.name());
        return c;
    }

    protected SmallBodyViewConfig createSingleResMissionImagesConfig_20210211_v01( //
            ShapeModelBody body, //
            String label, //
            int numberPlates, //
            String dracoModelId, //
            String leiaModelId, //
            String lukeModelId) //
    {
        return createSingleResMissionImagesConfig_20210211_v01(body, label, numberPlates, dracoModelId, leiaModelId, lukeModelId, false);
    }

    /**
     * This version was created to handle the pathology of the first delivery of
     * ideal-impact4-ra-20210211-v01, which gets its DRACO images (only) from
     * the ideal-impact4-20200629-v01 model.
     * <p>
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
    protected SmallBodyViewConfig createSingleResMissionImagesConfig_20210211_v01( //
            ShapeModelBody body, //
            String label, //
            int numberPlates, //
            String dracoModelId, //
            String leiaModelId, //
            String lukeModelId, //
            boolean useUpdatedFrameNames)
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
        ShapeModelType author = ShapeModelType.provide(label.replaceAll("\\s+", "-").toLowerCase().replace("impact-", "impact"));

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

        c.imagingInstruments = new ImagingInstrument[] { //
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(dracoDir, dracoTable, dracoTable, dracoDataDir + "gallery", dracoDataDir + "images"), //
                        ImageType.valueOf("DRACO_IMAGE"), //
                        InfoFiles, //
                        Instrument.DRACO, //
                        270., //
                        "None", //
                        DracoTestFillValues //
                ), new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, leiaDataDir + "gallery", leiaDataDir + "images"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFiles, //
                        Instrument.LEIA, //
                        0., //
                        "None", //
                        LeiaFillValues //
                ), new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
                        InfoFiles, //
                        Instrument.LUKE, //
                        90.0, //
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
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };

        return c;
    }

    protected SmallBodyViewConfig createSingleResMissionImagesConfig(ShapeModelBody body, String label, int numberPlates)
    {
        return createSingleResMissionImagesConfig(body, label, numberPlates, false);
    }

    /**
     * This version of the creator is based on the method
     * createSingleResMissionImagesConfig_20210211_v01 but with the final three
     * parameters converted into local variables because the need for those
     * three parameters is probably peculiar to the Ideal 4 RA version of the
     * model. This version was added at the time of the impact 9 models.
     * <p>
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param label the label exactly as the model should appear in the menu
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResMissionImagesConfig(ShapeModelBody body, String label, int numberPlates, boolean useUpdatedFrameNames)
    {
        String dracoModelId = null;
        String leiaModelId = null;
        String lukeModelId = null;

        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates)) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        // ShapeModelType rules: no spaces (replace with underscores). Mixed
        // case, underscores and dashes are all OK. Includes a DART-specific
        // hack to remove one dash that was not present in the early models.
        // ShapeModelType author =
        // ShapeModelType.provide(label.replaceAll("\\s+",
        // "-").toLowerCase().replace("impact-", "impact"));
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

        c.imagingInstruments = new ImagingInstrument[] { //
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(dracoDir, dracoTable, dracoTable, dracoDataDir + "gallery", dracoDataDir + "images"), //
                        ImageType.valueOf("DRACO_IMAGE"), //
                        InfoFiles, //
                        Instrument.DRACO, //
                        0., //
                        "None", //
                        DracoTestFillValues //
                ), new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, leiaDataDir + "gallery", leiaDataDir + "images"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFiles, //
                        Instrument.LEIA, //
                        0., //
                        "None", //
                        LeiaFillValues //
                ), new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
                        InfoFiles, //
                        Instrument.LUKE, //
                        90.0, //
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
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };
        if (useUpdatedFrameNames)
            generateUpdatedStateHistoryParameters(c, body.name());
        else
            generateStateHistoryParameters(c, body.name());
        return c;

    }

    private void generateStateHistoryParameters(SmallBodyViewConfig c, String centerBodyName)
    {
        c.hasStateHistory = true;
        //c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
        c.stateHistoryStartDate = new GregorianCalendar(2022, 9, 1, 10, 25, 8).getTime();
        c.stateHistoryEndDate = new GregorianCalendar(2022, 9, 1, 10, 28, 36).getTime();
        SpiceInfo spiceInfo1 = new SpiceInfo(MissionPrefix, "920065803_FIXED", "DART_SPACECRAFT", "DIDYMOS", new String[] { "EARTH", "SUN", "DIMORPHOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIMORPHOS"}, new String[] { "DART_DRACO_2X2", "120065803_FIXED" }, new String[] {});
        SpiceInfo spiceInfo2 = new SpiceInfo(MissionPrefix, "120065803_FIXED", "DART_SPACECRAFT", "DIMORPHOS", new String[] { "EARTH", "SUN", "DIDYMOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIDYMOS"}, new String[] { "DART_DRACO_2X2", "920065803_FIXED" }, new String[] {});
        SpiceInfo[] spiceInfos = new SpiceInfo[] { spiceInfo1, spiceInfo2 };
        c.spiceInfo = List.of(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).toList().get(0);
    }

    private void generateUpdatedStateHistoryParameters(SmallBodyViewConfig c, String centerBodyName)
    {
        c.hasStateHistory = true;
        //c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
        c.stateHistoryStartDate = new GregorianCalendar(2022, 8, 26, 23, 10, 18).getTime();
        c.stateHistoryEndDate = new GregorianCalendar(2022, 8, 26, 23, 14, 25).getTime();
        SpiceInfo spiceInfo1 = new SpiceInfo(MissionPrefix, "IAU_DIDYMOS", "DART_SPACECRAFT", "DIDYMOS", new String[] { "EARTH", "SUN", "DIMORPHOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIMORPHOS"}, new String[] { "DART_DRACO_2X2" }, new String[] {});
        SpiceInfo spiceInfo2 = new SpiceInfo(MissionPrefix, "IAU_DIMORPHOS", "DART_SPACECRAFT", "DIMORPHOS", new String[] { "EARTH", "SUN", "DIDYMOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIDYMOS"}, new String[] { "DART_DRACO_2X2" }, new String[] {});
        SpiceInfo[] spiceInfos = new SpiceInfo[] { spiceInfo1, spiceInfo2 };
        c.spiceInfo = List.of(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).toList().get(0);
    }

    /**
     * DO NOT USE!!!
     * <p>
     * This is deprecated because in hindsight, the whole approach taken for
     * DART configs here didn't work well. The problem is that there was/is too
     * much variability among the models as the deliveries evolved, leading to a
     * confusing collection of createSingleRes... with steadily growing numbers
     * of arguments, each of which was copy-and-pasted from a predecessor. So
     * starting midway through importing the Jupiter and Ganymede test models
     * (redmine 2425 and 2426), moved to an approach based on
     * {@link SmallBodyViewConfigBuilder}, which preserves flexibility but (it
     * is hoped) reduces boilerplate code somewhat.
     * <p>
     * This version of the creator is based on the method
     * createSingleResMissionImagesConfig but converted to handle multiple
     * resolutions. It was added to at the time of the Jupiter and Ganymede
     * models (redmine 2425 and 2426).
     * <p>
     * Behavioral differences from the single-res version:
     * <ol>
     * Numbers of plates and labels for model resolutions are now arguments. All
     * 3 imagers expect both sumfiles and infofiles for pointings. The model is
     * no longer embedded in the path to the image directories by default.
     * </ol>
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param label the label exactly as the model should appear in the menu
     * @param modelLabels labels for each model resolution level
     * @param modelResolutions plate count for each model resolution level
     * @param dracoImageSources image sources available for DRACO
     * @param leiaImageSources image sources available for LEIA
     * @param lukeImageSources image sources available for LUKE
     * @return the config
     *
     *         DO NOT USE!!!
     */
    @Deprecated
    protected SmallBodyViewConfig createMultiResMissionImagesConfig( //
            ShapeModelBody body, String label, //
            String[] modelLabels, Integer[] modelResolutions, //
            PointingSource[] dracoImageSources, PointingSource[] leiaImageSources, PointingSource[] lukeImageSources)
    {
        String dracoModelId = null;
        String leiaModelId = null;
        String lukeModelId = null;

        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.copyOf(modelLabels), ImmutableList.copyOf(modelResolutions)) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        // ShapeModelType rules: no spaces (replace with underscores). Mixed
        // case, underscores and dashes are all OK. Includes a DART-specific
        // hack to remove one dash that was not present in the early models.
        ShapeModelType author = ShapeModelType.provide(label.replaceAll("\\s+", "-").toLowerCase().replace("impact-", "impact"));

        // Model identifier string rules: lowercase, no spaces nor underscores
        // (replace with dashes). Single dashes are OK. Valid for building
        // server-side paths.
        String modelId = author.name().replaceAll("[\\s-_]+", "-").toLowerCase();

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
        String dracoDataDir = "/dart/draco/" + (dracoModelId != null ? dracoModelId + "/" : "");

        String leiaDir = c.rootDirOnServer + "/leia";
        String leiaTable = tableBaseName + "leia";
        String leiaDataDir = "/dart/leia/" + (leiaModelId != null ? leiaModelId + "/" : "");

        String lukeDir = c.rootDirOnServer + "/luke";
        String lukeTable = tableBaseName + "luke";
        String lukeDataDir = "/dart/luke/" + (lukeModelId != null ? lukeModelId + "/" : "");

        List<ImagingInstrument> imagingInstruments = new ArrayList<>();
        if (dracoImageSources != null)
        {
            imagingInstruments.add(new ImagingInstrument( //
                    SpectralImageMode.MONO, //
                    new GenericPhpQuery(dracoDir, dracoTable, dracoTable, dracoDataDir + "gallery", dracoDataDir + "images"), //
                    ImageType.valueOf("DRACO_IMAGE"), //
                    dracoImageSources, //
                    Instrument.DRACO, //
                    0., //
                    "None", //
                    DracoFlightFillValues //
            ));
        }
        if (leiaImageSources != null)
        {
            imagingInstruments.add(new ImagingInstrument( //
                    SpectralImageMode.MONO, //
                    new GenericPhpQuery(leiaDir, leiaTable, leiaTable, leiaDataDir + "gallery", leiaDataDir + "images"), //
                    ImageType.valueOf("LEIA_IMAGE"), //
                    leiaImageSources, //
                    Instrument.LEIA, //
                    0., //
                    "None", //
                    LeiaFillValues //
            ));
        }
        if (lukeImageSources != null)
        {
            imagingInstruments.add(new ImagingInstrument( //
                    SpectralImageMode.MULTI, //
                    new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                    ImageType.valueOf("LUKE_IMAGE"), //
                    lukeImageSources, //
                    Instrument.LUKE, //
                    90.0, //
                    "X", //
                    LukeFillValues, //
                    false //
            ));
        }

        c.imagingInstruments = imagingInstruments.toArray(new ImagingInstrument[imagingInstruments.size()]);

        c.imageSearchDefaultStartDate = ImageSearchDefaultStartDate;
        c.imageSearchDefaultEndDate = ImageSearchDefaultEndDate;
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 1.0e4;
        c.imageSearchDefaultMaxResolution = 1.0e3;

        c.databaseRunInfos = new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-sum.txt", dracoTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(PointingSource.GASKELL, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-sum.txt", leiaTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(PointingSource.GASKELL, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-sum.txt", lukeTable), //
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };

        return c;
    }

    protected void init(ShapeModelBody body, ShapeModelType author, ShapeModelDataUsed dataUsed, String label)
    {
        init();

        BodyType bodyType;
        ShapeModelPopulation population;
        ShapeModelBody system;
        if (ShapeModelBody.DIDYMOS.equals(body) || ShapeModelBody.DIMORPHOS.equals(body))
        {
            bodyType = BodyType.ASTEROID;
            population = ShapeModelPopulation.NEO;
            system = ShapeModelBody.DIDYMOS_SYSTEM;
        }
        else if (ShapeModelBody.JUPITER.equals(body) || ShapeModelBody.GANYMEDE.equals(body))
        {
            bodyType = BodyType.PLANETS_AND_SATELLITES;
            population = ShapeModelPopulation.JUPITER;
            system = ShapeModelBody.JUPITER;
        }
        else
        {
            throw new IllegalArgumentException("Dont know how to set up a config builder for body " + body);
        }

        body(body, bodyType, population, system);
        model(author, dataUsed, label);
        modelTopDir(modelTopDir(body, author));
    }

    protected ImagingInstrument createFlightInstrument(ShapeModelBody body, ShapeModelType author, Instrument instrument, Map<PointingSource, Orientation> orientationMap, PointingSource... sources)
    {
        String tablePrefix = dbTablePrefix(body, author, instrument).replaceAll("_center", "");
        String lcInstrument = instrument.name().toLowerCase();

        String modelImageDir = modelTopDir(body, author).replaceAll("-center", "") + "/" + lcInstrument;
        String imageDataDir = "/dart/" + lcInstrument;

        return createFlightInstrument(instrument, tablePrefix, modelImageDir, imageDataDir, orientationMap, sources);
    }

    protected ImagingInstrument createFlightInstrument(Instrument instrument, String tablePrefix, String modelImageDir, String imageDataDir, Map<PointingSource, Orientation> orientationMap, PointingSource... sources)
    {
        Preconditions.checkArgument(sources.length > 0);

        ImageType imageType;
        Collection<Float> fillValues;
        SpectralImageMode spectralImageMode = SpectralImageMode.MONO;
        if (instrument == Instrument.DRACO)
        {
            imageType = ImageType.valueOf("DRACO_IMAGE");
            fillValues = DracoFlightFillValues;
        }
        else if (instrument == Instrument.LEIA)
        {
            imageType = ImageType.valueOf("LEIA_IMAGE");
            fillValues = LeiaFlightFillValues;
        }
        else if (instrument == Instrument.LUKE)
        {
            imageType = ImageType.valueOf("LUKE_IMAGE");
            fillValues = LukeFlightFillValues;
            spectralImageMode = SpectralImageMode.MULTI;
        }
        else
        {
            throw new IllegalArgumentException("Unable to handle instrument " + instrument);
        }

        int[] linearInterpDims = null;
        int[] maskValues = null;

        double rotation = 0.0;
        String flip = ImageFlip.NONE.flip();
        boolean transpose = true;

        return new ImagingInstrument(spectralImageMode, //
                new GenericPhpQuery(modelImageDir, tablePrefix, tablePrefix, imageDataDir + "/gallery", imageDataDir + "/images"), //
                imageType, //
                sources, //
                instrument, //
                rotation, //
                flip, //
                fillValues, //
                linearInterpDims, //
                maskValues, //
                transpose, //
                orientationMap //
        );
    }

    protected ImagingInstrument createFlightInstrument(ShapeModelBody body, ShapeModelType author, Instrument instrument, double rotation, ImageFlip flip, boolean transpose, PointingSource... sources)
    {
        String tablePrefix = dbTablePrefix(body, author, instrument).replaceAll("_center", "");

        String lcInstrument = instrument.name().toLowerCase();

        String modelImageDir = modelTopDir(body, author).replaceAll("-center", "") + "/" + lcInstrument;
        String imageDataDir = "/dart/" + lcInstrument;

        return createFlightInstrument(instrument, tablePrefix, modelImageDir, imageDataDir, rotation, flip, transpose, sources);
    }

    protected ImagingInstrument createFlightInstrument(Instrument instrument, String tablePrefix, String modelImageDir, String imageDataDir, double rotation, ImageFlip flip, boolean transpose, PointingSource... sources)
    {
        ImageType imageType;
        Collection<Float> fillValues;
        SpectralImageMode spectralImageMode = SpectralImageMode.MONO;
        if (instrument == Instrument.DRACO)
        {
            imageType = ImageType.valueOf("DRACO_IMAGE");
            fillValues = DracoFlightFillValues;
        }
        else if (instrument == Instrument.LEIA)
        {
            imageType = ImageType.valueOf("LEIA_IMAGE");
            fillValues = LeiaFlightFillValues;
        }
        else if (instrument == Instrument.LUKE)
        {
            imageType = ImageType.valueOf("LUKE_IMAGE");
            fillValues = LukeFlightFillValues;
            spectralImageMode = SpectralImageMode.MULTI;
        }
        else
        {
            throw new IllegalArgumentException("Unable to handle instrument " + instrument);
        }

        if (sources.length == 0)
        {
            sources = new PointingSource[] { PointingSource.GASKELL };
        }

        int[] linearInterpDims = null;
        int[] maskValues = null;
        Map<PointingSource, Orientation> orientationMap = null;

        return new ImagingInstrument(spectralImageMode, //
                new GenericPhpQuery(modelImageDir, tablePrefix, tablePrefix, imageDataDir + "/gallery", imageDataDir + "/images"), //
                imageType, //
                sources, //
                instrument, //
                rotation, //
                flip.flip(), //
                fillValues, //
                linearInterpDims, //
                maskValues, //
                transpose, //
                orientationMap //
        );
    }

    protected SmallBodyViewConfig createSingleResMissionImagesSystemConfig(ShapeModelBody[] body, String label, int numberPlates)
    {
        return createSingleResMissionImagesSystemConfig(body, label, numberPlates, false);
    }

    protected SmallBodyViewConfig createSingleResMissionImagesSystemConfig(ShapeModelBody[] body, String label, int numberPlates, boolean useUpdatedFrameNames)
    {
        SmallBodyViewConfig config = createSingleResMissionImagesConfig(body[0], label, numberPlates, useUpdatedFrameNames);
        List<SmallBodyViewConfig> systemConfigs = new ArrayList<>();
        for (int i = 1; i < body.length; i++)
        {
            systemConfigs.add(createSingleResMissionImagesConfig(body[i], label, numberPlates, useUpdatedFrameNames));
        }
        ;
        config.systemConfigs = systemConfigs;
        config.hasSystemBodies = true;
        config.body = ShapeModelBody.DIDYMOS_SYSTEM;
        config.hasDTMs = false;
        config.rootDirOnServer = "/" + body[0].name().replaceAll("[\\s-_]+", "-").toLowerCase() + "/" + config.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\(", "");
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\)", "");
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("-\\w*-center", "");
        config.defaultForMissions = APLClients;
        return config;
    }

    protected SmallBodyViewConfig createMissionImagesSystemConfig(ShapeModelBody[] bodies, String[] labels, int numberPlates, boolean useUpdatedFrameNames)
    {
        SmallBodyViewConfig config = createSingleResMissionImagesConfig(bodies[0], labels[0], numberPlates, useUpdatedFrameNames);
        List<SmallBodyViewConfig> systemConfigs = new ArrayList<>();
        for (int i = 1; i < bodies.length; i++)
        {
            systemConfigs.add(createSingleResMissionImagesConfig(bodies[i], labels[i], numberPlates, useUpdatedFrameNames));
        }
        ;
        config.systemConfigs = systemConfigs;
        config.hasSystemBodies = true;
        config.body = ShapeModelBody.DIDYMOS_SYSTEM;
        config.hasDTMs = false;
        config.rootDirOnServer = "/" + bodies[0].name().replaceAll("[\\s-_]+", "-").toLowerCase() + "/" + config.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\(", "");
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\)", "");
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("-\\w*-center", "");
        return config;
    }

    /**
     * Builds a system configuration
     *
     * @param body
     * @param label
     * @param numberPlates
     * @return
     */
    protected SmallBodyViewConfig createSingleResolutionSystemConfig_20200629_v01(ShapeModelBody[] body, String label, int numberPlates)
    {
        SmallBodyViewConfig config = createSingleResolutionConfig_20200629_v01(body[0], label, numberPlates);
        List<SmallBodyViewConfig> systemConfigs = new ArrayList<>();
        for (int i = 1; i < body.length; i++)
        {
            systemConfigs.add(createSingleResolutionConfig_20200629_v01(body[i], label, numberPlates));
        }
        ;
        config.systemConfigs = systemConfigs;
        config.hasSystemBodies = true;
        config.body = ShapeModelBody.DIDYMOS_SYSTEM;
        config.hasDTMs = false;
        config.rootDirOnServer = "/" + body[0].name().replaceAll("[\\s-_]+", "-").toLowerCase() + "/" + config.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\(", "");
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("\\)", "");
        config.rootDirOnServer = config.rootDirOnServer.replaceAll("-\\w*-center", "");
        config.defaultForMissions = APLClients;
        return config;
    }

    private void buildSystemConfigs(List<IBodyViewConfig> configList)
    {
        SmallBodyViewConfig c;
        // System configs
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 1 20200629 v01 System Didymos Center", 1996);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 1 20200629 v01 System Dimorphos Center", 3072);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 2 20200629 v01 System Didymos Center", 1996);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 2 20200629 v01 System Dimorphos Center", 3145728);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 3 20200629 v01 System Didymos Center", 1996);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 3 20200629 v01 System Dimorphos Center", 3366134);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 4 20200629 v01 System Didymos Center", 3145728);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 4 20200629 v01 System Dimorphos Center", 3145728);
//        configList.add(c);
//        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 4 RA 20210211 v01 System Didymos Center", 3145728);
//        configList.add(c);
//        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 4 RA 20210211 v01 System Dimorphos Center", 3145728);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 5 20200629 v01 System Didymos Center", 3145728);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 5 20200629 v01 System Dimorphos Center", 3366134);
//        configList.add(c);
        // c = createSingleResolutionSystemConfig_20200629_v01(new
        // ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS
        // }, "Ideal Impact 6 RA 20201116 v01 System Didymos Center", 3145728);
        // configList.add(c);
        // c = createSingleResolutionSystemConfig_20200629_v01(new
        // ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS
        // }, "Ideal Impact 6 RA 20201116 v01 System Dimorphos Center",
        // 3145728);
        // configList.add(c);

//        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Ideal Impact 9 20210630 v01 System Didymos Center", 1996, true);
//        configList.add(c);
//
//        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Ideal Impact 9 20210630 v01 System Dimorphos Center", 3072, true);
//        configList.add(c);
//
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 1 20200629 v01 System Didymos Center", 1996);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 1 20200629 v01 System Dimorphos Center", 3072);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 2 20200629 v01 System Didymos Center", 1996);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 2 20200629 v01 System Dimorphos Center", 31457278);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 3 20200629 v01 System Didymos Center", 1996);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 3 20200629 v01 System Dimorphos Center", 3366134);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 4 20200629 v01 System Didymos Center", 3145728);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 4 20200629 v01 System Dimorphos Center", 31457278);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 5 20200629 v01 System Didymos Center", 3145728);
//        configList.add(c);
//        c = createSingleResolutionSystemConfig_20200629_v01(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 5 20200629 v01 System Dimorphos Center", 3366134);
//        configList.add(c);
//        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIDYMOS, ShapeModelBody.DIMORPHOS }, "Errors Impact 9 20210630 v01 System Didymos Center", 3072, true);
//        configList.add(c);
//        c = createSingleResMissionImagesSystemConfig(new ShapeModelBody[] { ShapeModelBody.DIMORPHOS, ShapeModelBody.DIDYMOS }, "Errors Impact 9 20210630 v01 System Dimorphos Center", 3072, true);
//        configList.add(c);


        //Official flight systems
        final PointingSource[] imageSources = { PointingSource.GASKELL, PointingSource.SPICE };
        final PointingSource[] spcSources = { PointingSource.GASKELL };
        Map<PointingSource, Orientation> dracoFlightOrientations = new LinkedHashMap<>();
        dracoFlightOrientations.put(PointingSource.GASKELL, new OrientationFactory().of(ImageFlip.X, 0.0, true));
        dracoFlightOrientations.put(PointingSource.SPICE, new OrientationFactory().of(ImageFlip.X, 180.0, true));
     // Dimorphos version 003 (flight).

        SmallBodyViewConfig dimorphosConfig;
        {

            String label = "Dimorphos-v003 Center";
            ShapeModelType author = author(MissionPrefix + " " + label);

            init(ShapeModelBody.DIMORPHOS, author, ShapeModelDataUsed.IMAGE_BASED, label);
            imageSearchRanges(DimorphosImpactSearchStartDate, DimorphosImpactSearchEndDate, DimorphosImpactMaxScDistance, DimorphosImpactResolution);

            modelRes(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION);

            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.DIMORPHOS, author, Instrument.DRACO, dracoFlightOrientations, imageSources);
            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.DIMORPHOS, author, Instrument.DRACO, imageSources);
            add(instrument, dbRunInfos);

            gravityInputs(2300.0, 1.464e-4);

            dtmCatalogs(true);

            dimorphosConfig = build();
            generateUpdatedStateHistoryParameters(dimorphosConfig, ShapeModelBody.DIMORPHOS.name());
        }

        // Didymos version 001 (flight).
        SmallBodyViewConfig didymosConfig;
        {

            String label = "Didymos-v001 Center";
            ShapeModelType author = author(MissionPrefix + " " + label);

            init(ShapeModelBody.DIDYMOS, author, ShapeModelDataUsed.IMAGE_BASED, label);
            imageSearchRanges(DidymosImpactSearchStartDate, DidymosImpactSearchEndDate, DidymosImpactMaxScDistance, DidymosImpactResolution);

            modelRes(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION);

            ImagingInstrument instrument = createFlightInstrument(ShapeModelBody.DIDYMOS, author, Instrument.DRACO, dracoFlightOrientations, spcSources);
            DBRunInfo[] dbRunInfos = createDbInfos(ShapeModelBody.DIDYMOS, author, Instrument.DRACO, spcSources);
            add(instrument, dbRunInfos);

            gravityInputs(2400.0, 7.72e-4);

            // No DTMs with initial delivery.
            // dtmCatalogs(true);

            didymosConfig = build();
            generateUpdatedStateHistoryParameters(didymosConfig, ShapeModelBody.DIDYMOS.name());
        }

        //Build didymos centric
        c = buildSystemConfig(didymosConfig, new SmallBodyViewConfig[] {dimorphosConfig});
        configList.add(c);

        //Build dimorphos centric
        c = buildSystemConfig(dimorphosConfig, new SmallBodyViewConfig[] {didymosConfig});
        configList.add(c);
    }

    private SmallBodyViewConfig buildSystemConfig(SmallBodyViewConfig primary, SmallBodyViewConfig[] secondaries)
    {
    	List<SmallBodyViewConfig> systemConfigs = new ArrayList<>();
        for (int i = 0; i < secondaries.length; i++)
        {
            systemConfigs.add(secondaries[i]);
        }
        String bodyName = primary.body.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        String authorName = primary.author.name().replaceAll("[\\s-_]+", "-").replaceAll("-center", "").toLowerCase();

        primary.body = ShapeModelBody.DIDYMOS_SYSTEM;
        primary.systemConfigs = systemConfigs;
        primary.hasSystemBodies = true;
        primary.hasDTMs = false;
        primary.rootDirOnServer = "/" + bodyName + "/" + authorName;
        primary.rootDirOnServer = primary.rootDirOnServer.replaceAll("\\(", "");
        primary.rootDirOnServer = primary.rootDirOnServer.replaceAll("\\)", "");

        return primary;
    }
}
