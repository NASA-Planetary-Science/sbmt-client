package edu.jhuapl.sbmt.client.configs;

import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool.Mission;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.image.SpectralImageMode;
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
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Ideal Impact 4 RA 20210211 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "Ideal Impact 5 20200629 v01", 3145728);
        configList.add(c);

        // This one was only partially delivered; problems were uncovered with
        // the SPICE pointings. It was abandoned in favor of "Ideal Impact 4 RA
        // 20210211 v01" above.
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIDYMOS, "Ideal Impact 6 RA 20201116 v01", 3145728);
//        configList.add(c);

        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 1 20200629 v01", 3072);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 2 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 3 20200629 v01", 3366134);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 4 20200629 v01", 3145728);
        configList.add(c);
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Ideal Impact 4 RA 20210211 v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "Ideal Impact 5 20200629 v01", 3366134);
        configList.add(c);

        // This one was only partially delivered; problems were uncovered with
        // the SPICE pointings. It was abandoned in favor of "Ideal Impact 4 RA
        // 20210211 v01" above.
        c = createSingleResMissionImagesConfig(ShapeModelBody.DIMORPHOS, "Ideal Impact 6 RA 20201116 v01", 3145728);
//        configList.add(c);

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

        defaultConfig.defaultForMissions = DartClients;
    }

    /**
     * Create a single-resolution model for the given input parameters. This
     * creates configurations that are consistent with the initial set of
     * simulated models, images and SPICE files delivered starting in August,
     * 2020, based on the DART simulations identified as 20200629-v01.
     * <p>
     * These deliveries were processed using versions of the scripts that were
     * not yet set up to handle images under the mission/instrument directory.
     * These were processed before the Saturnian moon models that were processed
     * in October-Novebmer 2020.
     * <p>
     * For these deliveries, images were located under the models. Some images
     * had one or the other or both of the bodies Didymos and Dimorphos in the
     * FOV, but ALL the images were delivered, processed, archived and stored
     * TWICE: once for each body.
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

        String lukeDir = c.rootDirOnServer + "/luke";
        String lukeTable = tableBaseName + "luke";

        String leiaDir = c.rootDirOnServer + "/leia";
        String leiaTable = tableBaseName + "leia";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(dracoDir, dracoTable, dracoTable, c.rootDirOnServer + "/draco/gallery"), //
                        ImageType.valueOf("DART_IMAGE"), //
                        InfoFiles, //
                        Instrument.DRACO, //
                        270., //
                        "None", //
                        DracoFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, c.rootDirOnServer + "/leia/gallery"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFilesAndCorrectedInfoFiles, //
                        Instrument.LEIA, //
                        270., //
                        "None", //
                        LeiaFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, c.rootDirOnServer + "/luke/gallery"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
//                        InfoFilesAndCorrectedInfoFiles, //
                        InfoFiles, //
                        Instrument.LUKE, //
                        270., //
                        "None", //
                        LukeFillValues //
                ),
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(2022, 9, 1, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2022, 9, 2, 0, 0, 0).getTime();
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
                new DBRunInfo(ImageSource.CORRECTED_SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };

        return c;
    }

    /**
     * Create a single-resolution model for the given input parameters. This
     * creates configurations that are consistent with the initial set of
     * simulated models, images and SPICE files delivered starting in December,
     * 2020 through March, 2021, based on the DART simulations identified as
     * 20201116-v01 and 20210211-v01.
     * <p>
     * These deliveries were processed using versions of the scripts that were
     * set up to handle images under the mission/instrument directory. These
     * were processed AFTER the Saturnian moon models that were processed in
     * October-Novebmer 2020.
     * <p>
     * For these deliveries, images were located under the MISSION/INSTRUMENT
     * hierarchy. Some images had one or the other or both of the bodies Didymos
     * and Dimorphos in the FOV. Only one set of images for each instrument were
     * delivered, processed etc. However, since these images are
     * simulation-specific simulated images, needed to put them under one more
     * level of subdirectory, i.e. mission/instrument/model (but note no body in
     * this hierarchy).
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param label the label exactly as the model should appear in the menu.
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResMissionImagesConfig(ShapeModelBody body, String label, int numberPlates)
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
                        ImageType.valueOf("DART_IMAGE"), //
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

        c.imageSearchDefaultStartDate = new GregorianCalendar(2022, 8, 30, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2022, 9, 1, 0, 0, 0).getTime();
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
                new DBRunInfo(ImageSource.CORRECTED_SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable) //
        };

        return c;
    }

}
