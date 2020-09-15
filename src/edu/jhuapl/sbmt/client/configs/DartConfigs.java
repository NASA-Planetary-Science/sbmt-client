package edu.jhuapl.sbmt.client.configs;

import java.util.GregorianCalendar;
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

    public static DartConfigs instance()
    {
        return DefaultInstance;
    }

    protected static final ImageSource[] InfoFiles = new ImageSource[] { ImageSource.SPICE };

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

        c = createSingleResolutionConfig(ShapeModelBody.DIDYMOS, "ideal-impact1-20200629-v01", 1996);
        configList.add(c);

        // Move this line around as needed to select the current default model.
        final SmallBodyViewConfig defaultConfig = c;

        defaultConfig.defaultForMissions = DartClients;
    }

    /**
     * Create a single-resolution model for the given input parameters.
     *
     * @param body the {@link ShapeModelBody} associated with this model
     * @param modelId the model identifier, which must be unique to this client,
     *            and may not contain any spaces
     * @param numberPlates the number of plates in the single resolution model
     * @return the config
     */
    protected SmallBodyViewConfig createSingleResolutionConfig(ShapeModelBody body, String modelId, int numberPlates)
    {
        SmallBodyViewConfig c = new SmallBodyViewConfig(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates)) {
            public SmallBodyViewConfig clone() {
                throw new UnsupportedOperationException("This implementation does not support cloning");
            }
        };

        c.body = body;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
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
                        Instrument.DRACO //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(lukeDir, lukeTable, lukeTable, c.rootDirOnServer + "/luke/gallery"), //
                        ImageType.valueOf("LUKE_IMAGE"), //
                        InfoFiles, //
                        Instrument.LUKE //
                ),
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new GenericPhpQuery(leiaDir, leiaTable, leiaTable, c.rootDirOnServer + "/leia/gallery"), //
                        ImageType.valueOf("LEIA_IMAGE"), //
                        InfoFiles, //
                        Instrument.LEIA //
                ),
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(2022, 10, 1, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2022, 10, 2, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
        c.imageSearchDefaultMaxResolution = 1.0e3;

        c.databaseRunInfos = new DBRunInfo[] { //
                new DBRunInfo(ImageSource.SPICE, Instrument.DRACO, body.toString(), //
                        dracoDir + "/imagelist-fullpath-info.txt", dracoTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LUKE, body.toString(), //
                        lukeDir + "/imagelist-fullpath-info.txt", lukeTable), //
                new DBRunInfo(ImageSource.SPICE, Instrument.LEIA, body.toString(), //
                        leiaDir + "/imagelist-fullpath-info.txt", leiaTable) //
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
