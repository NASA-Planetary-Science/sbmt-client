package edu.jhuapl.sbmt.client.configs;

import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.text.WordUtils;

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

        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "ideal-impact1-20200629-v01", 1996);
        configList.add(c);

        // Make this the default model.
        final SmallBodyViewConfig defaultConfig = c;

        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "ideal-impact2-20200629-v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "ideal-impact3-20200629-v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "ideal-impact4-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "ideal-impact5-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20201116_v01(ShapeModelBody.DIDYMOS, "ideal-impact6-ra-20201116-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "ideal-impact1-20200629-v01", 3072);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "ideal-impact2-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "ideal-impact3-20200629-v01", 3366134);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "ideal-impact4-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "ideal-impact5-20200629-v01", 3366134);
        configList.add(c);
        c = createSingleResolutionConfig_20201116_v01(ShapeModelBody.DIMORPHOS, "ideal-impact6-ra-20201116-v01", 3145728);
        configList.add(c);

        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "errors-impact1-20200629-v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "errors-impact2-20200629-v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "errors-impact3-20200629-v01", 1996);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "errors-impact4-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIDYMOS, "errors-impact5-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "errors-impact1-20200629-v01", 3072);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "errors-impact2-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "errors-impact3-20200629-v01", 3366134);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "errors-impact4-20200629-v01", 3145728);
        configList.add(c);
        c = createSingleResolutionConfig_20200629_v01(ShapeModelBody.DIMORPHOS, "errors-impact5-20200629-v01", 3366134);
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
     * @param modelId the model identifier, which must be unique to this client,
     *            and may not contain any spaces
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResolutionConfig_20200629_v01(ShapeModelBody body, String modelId, int numberPlates)
    {
        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates)) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        c.body = body;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.system = ShapeModelBody.DIDYMOS_SYSTEM;
        c.dataUsed = ShapeModelDataUsed.SIMULATED;
        c.author = ShapeModelType.provide(modelId);
        c.modelLabel = createLabel(c.author);
        c.rootDirOnServer = ("/" + body.name() + "/" + modelId.replaceAll("_", "-")).toLowerCase();
        c.presentInMissions = ClientsWithDartModels;
        // c.defaultForMissions = ...
        c.setShapeModelFileExtension(".obj");

        String tableBaseName = (body.name() + "_" + modelId + "_").replaceAll("[\\s-]", "_").toLowerCase();

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
     * 2020, based on the DART simulations identified as 20201116-v01.
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
     * @param modelId the model identifier, which must be unique to this client,
     *            and may not contain any spaces
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResolutionConfig_20201116_v01(ShapeModelBody body, String modelId, int numberPlates)
    {
        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates)) {
            public SmallBodyViewConfig clone()
            {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        c.body = body;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.system = ShapeModelBody.DIDYMOS_SYSTEM;
        c.dataUsed = ShapeModelDataUsed.SIMULATED;
        c.author = ShapeModelType.provide(modelId);
        c.modelLabel = createLabel(c.author);
        c.rootDirOnServer = ("/" + body.name() + "/" + modelId.replaceAll("_", "-")).toLowerCase();
        c.presentInMissions = ClientsWithDartModels;
        // c.defaultForMissions = ...
        c.setShapeModelFileExtension(".obj");

        String tableBaseName = (body.name() + "_" + modelId + "_").replaceAll("[\\s-]", "_").toLowerCase();

        String dracoDir = c.rootDirOnServer + "/draco";
        String dracoTable = tableBaseName + "draco";
        String dracoDataDir = "/dart/draco/" + modelId.replaceAll("_", "-").toLowerCase() + "/";

        String leiaDir = c.rootDirOnServer + "/leia";
        String leiaTable = tableBaseName + "leia";
        String leiaDataDir = "/dart/leia/" + modelId.replaceAll("_", "-").toLowerCase() + "/";

        String lukeDir = c.rootDirOnServer + "/luke";
        String lukeTable = tableBaseName + "luke";
        String lukeDataDir = "/dart/luke/" + modelId.replaceAll("_", "-").toLowerCase() + "/";

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
                        270., //
                        "None", //
                        LeiaFillValues //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, lukeDataDir + "gallery", lukeDataDir + "images"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
//                        InfoFilesAndCorrectedInfoFiles, //
                        InfoFiles, //
                        Instrument.LUKE, //
                        270., //
                        "None", //
                        LukeFillValues //
                ),
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(2022, 8, 29, 0, 0, 0).getTime();
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

    /**
     * Create a cosmetically improved version of the shape model author for
     * displaying in the tool.
     *
     * @param author the {@link ShapeModelType}
     * @return the label
     */
    protected String createLabel(ShapeModelType author)
    {
        // Replace all separators with a single space.
        String label = author.toString().replaceAll("[-_\\s]+", " ");

        // Capitalize the first letter of every word.
        label = WordUtils.capitalizeFully(label);

        // DART-specific corrections: version should be lowercase v, dash after
        // word "impact":
        label = label.replaceAll("V(\\d)", "v$1").replaceAll("Impact", "Impact ");

        return label;
    }

    public static void main(String[] args)
    {
        DartConfigs configs = new DartConfigs();
        System.err.println(configs.createLabel(ShapeModelType.provide("ideal_impact1-20200629-v01")));
    }
}
