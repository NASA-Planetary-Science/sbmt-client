package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.common.client.BodyViewConfig;
import edu.jhuapl.sbmt.common.client.Mission;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.BodyType;
import edu.jhuapl.sbmt.config.Instrument;
import edu.jhuapl.sbmt.config.ShapeModelDataUsed;
import edu.jhuapl.sbmt.config.ShapeModelPopulation;
import edu.jhuapl.sbmt.config.SpectralImageMode;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
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

    // Note: would strongly prefer *NOT TO DO IT THIS WAY* by hard-coding these
    // values. Doing it this way because currently there is no way to inject
    // special handling of keywords where the images are read in a
    // mission-independent way. A better option may exist in the future, at
    // which time this should be changed.
    private static final LinkedHashSet<Float> DracoFlightFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> DracoTestFillValues = new LinkedHashSet<>();
    private static final LinkedHashSet<Float> LeiaFillValues = null;
    private static final LinkedHashSet<Float> LukeFillValues = null;

    // Months are 0-based: SEPTEMBER 20 is 8, 20, not 9, 20.
    private static final Date ImageSearchDefaultStartDate = new GregorianCalendar(2022, 8, 20, 0, 0, 0).getTime();
    // Months are 0-based: OCTOBER 5 is 9, 5 not 10, 5.
    private static final Date ImageSearchDefaultEndDate = new GregorianCalendar(2022, 9, 5, 0, 0, 0).getTime();

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

    protected static final String[] ModelLabels4Levels = BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION;

    protected static final Integer[] ModelResolutions4Levels = BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

    protected static final String[] ModelLabels5Levels = { "Very Low (12288 plates)", //
            BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], //
            BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3] };

    protected static final Integer[] ModelResolutions5Levels = { 12288, //
            BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], //
            BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3] };

    public static DartConfigs instance()
    {
        return DefaultInstance;
    }

    protected static final ImageSource[] InfoFiles = new ImageSource[] { ImageSource.SPICE };

    protected static final ImageSource[] InfoFilesAndCorrectedInfoFiles = new ImageSource[] { ImageSource.CORRECTED_SPICE, ImageSource.SPICE };

    protected static final ImageSource[] SumFilesAndInfoFiles = new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE };

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
        c = createSingleResMissionImagesConfig_20210211_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 4 RA 20210211 v01", 3145728, //
                "ideal-impact4-20200629-v01", null, null);
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
        c = createSingleResMissionImagesConfig_20210211_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 4 RA 20210211 v01", 3145728, //
                "ideal-impact4-20200629-v01", null, null);
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

        defaultConfig.defaultForMissions = DartClients;

        // Jupiter system models.
        c = createMultiResMissionImagesConfig(ShapeModelBody.JUPITER, "DART Jupiter-01", ModelLabels5Levels, ModelResolutions5Levels, SumFilesAndInfoFiles, null, null);
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.system = ShapeModelBody.JUPITER;

        configList.add(c);

        c = createMultiResMissionImagesConfig(ShapeModelBody.GANYMEDE, "DART Ganymede-01", ModelLabels5Levels, ModelResolutions5Levels, SumFilesAndInfoFiles, null, null);
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.system = ShapeModelBody.JUPITER;

        configList.add(c);
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
                        DracoTestFillValues //
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

        return c;
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
            String lukeModelId //
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
                        270., //
                        "None", //
                        DracoTestFillValues //
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

        return c;
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
    protected SmallBodyViewConfig createSingleResMissionImagesConfig(ShapeModelBody body, String label, int numberPlates)
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
                        DracoTestFillValues //
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

        return c;
    }

    /**
     * This version of the creator is based on the method
     * createSingleResMissionImagesConfig but converted to handle multiple
     * resolutions. It was added to at the time of the Jupiter and Ganymede
     * models (redmine 2425 and 2426).
     * <p>
     * Behavioral differences from the single-res version:
     * <ol>
     * Numbers of plates and labels for model resolutions are now argyments. All
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
     */
    protected SmallBodyViewConfig createMultiResMissionImagesConfig( //
            ShapeModelBody body, String label, //
            String[] modelLabels, Integer[] modelResolutions, //
            ImageSource[] dracoImageSources, ImageSource[] leiaImageSources, ImageSource[] lukeImageSources)
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
        String dracoDataDir = "/dart/draco/" + (dracoModelId != null ? dracoModelId : "") + "/";

        String leiaDir = c.rootDirOnServer + "/leia";
        String leiaTable = tableBaseName + "leia";
        String leiaDataDir = "/dart/leia/" + (leiaModelId != null ? leiaModelId : "") + "/";

        String lukeDir = c.rootDirOnServer + "/luke";
        String lukeTable = tableBaseName + "luke";
        String lukeDataDir = "/dart/luke/" + (lukeModelId != null ? lukeModelId : "") + "/";

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
                    SpectralImageMode.MONO, //
                    new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                    ImageType.valueOf("LUKE_IMAGE"), //
                    lukeImageSources, //
                    Instrument.LUKE, //
                    90., //
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
                new DBRunInfo(ImageSource.GASKELL, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-sum.txt", dracoTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(ImageSource.GASKELL, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-sum.txt", leiaTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable), //
                new DBRunInfo(ImageSource.GASKELL, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-sum.txt", lukeTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };

        return c;
    }

}
