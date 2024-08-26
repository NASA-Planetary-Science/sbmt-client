package edu.jhuapl.sbmt.client.configs;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.io.DBRunInfo;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.IDataQuery;

/**
 * Configurations for the Earth-Moon system.
 *
 * @author James.Peachey@jhuapl.edu
 *
 */
public class LucyConfigs
{
    private static final String MissionName = "Lucy";
    private static final ImageType LlorriImageType = ImageType.valueOf("LLORRI_IMAGE");

    private static final Mission[] LucyClients = new Mission[] { //
            Mission.LUCY_DEPLOY, //
            Mission.LUCY_TEST, //
            Mission.LUCY_STAGE, //
    };

    private static final Mission[] InternalClientsWithLucyModels = new Mission[] {
            Mission.APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
            Mission.STAGE_APL_INTERNAL, //
            Mission.LUCY_DEPLOY, //
            Mission.LUCY_TEST, //
            Mission.LUCY_STAGE, //
    };

    /**
     * Add all known configs to the specified {@link ConfigArrayList}.
     *
     * @param configArray list of configs to which to add the configs if they are are not already present
     */
    public static void initialize(ConfigArrayList<IBodyViewConfig> configArray)
    {
        buildDinkineshV001Config(configArray);
    }

    /**
     * Configure the single resolution LUCY Dinkinesh V001 model delivered 2024-05-02. Number of facets determined by inspection of
     * OBJ file. See GitLab issue sbmt/missions/sbmt-ddap#127,
     *
     * @param configArray list of configs to which to add the configs if they are are not already present
     */
    private static void buildDinkineshV001Config(ConfigArrayList<IBodyViewConfig> configArray)
    {
        String missionName = MissionName;

        // This section contains all the model-specific attributes.
        String modelLabel = "Lucy Dinkinesh V001";
        String modelName = DBRunInfo.createServerPath(modelLabel);
        String modelTopDir = DBRunInfo.createServerPath("/dinkinesh", modelName);
        List<Integer> resolutions = ImmutableList.of(82964);
        double density = 2800.0;
        double rotationRate = 4.66666E-4;

        // This section should be common to most, if not all Lucy models.
        ShapeModelBody body = ShapeModelBody.DINKINESH;
        ShapeModelType modelId = ShapeModelType.provide(modelName);

        SmallBodyViewConfigBuilder builder = new SmallBodyViewConfigBuilder();

        // TODO: after branches are merged, should be safe and preferable to do
        // this instead of the 4-argument overload of this method:
        // builder.body(body, BodyType.ASTEROID, ShapeModelPopulation.MAIN_BELT);
        builder.body(body, BodyType.ASTEROID, ShapeModelPopulation.MAIN_BELT, null);

        builder.model(modelId, ShapeModelDataUsed.IMAGE_BASED, modelLabel);
        builder.modelTopDir(modelTopDir);

        builder.modelRes(resolutions);
        builder.gravityInputs(density, rotationRate);

        // This section should be common to models that have LLORRI images,
        // assuming both SPC and SPICE pointings are available.
        Date startDate = new GregorianCalendar(2023, 10, 1, 16, 44, 39).getTime();
        Date endDate = new GregorianCalendar(2023, 10, 1, 17, 17, 42).getTime();
        double maxScDistance = 100000000.0;
        double maxResolution = 100000.0;

        Instrument instrumentId = Instrument.LLORRI;
        ImageType imageType = LlorriImageType;
        String instrumentName = instrumentId.name();
        String modelImageDir = DBRunInfo.createServerPath(modelTopDir, instrumentName);
        String imageDataDir = DBRunInfo.createServerPath(missionName, instrumentName);
        String tablePrefix = DBRunInfo.createTablePrefix(body.name(), modelId.name(), instrumentName);

        DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(modelImageDir, imageDataDir
                + "/images", tablePrefix, tablePrefix, imageDataDir + "/gallery");
        IDataQuery searchQuery = new ImageDataQuery(metadata);

        PointingSource[] searchImageSources = { PointingSource.GASKELL, PointingSource.SPICE };

        // If a single combination of rotation, flip, and transpose work for
        // both image sources, just use those and leave the map == null. If not,
        // create and populate the orientationMap; rotation, flip and transpose
        // will then be ignored.
        double rotation = 0.0;
        String flip = "None";
        boolean transpose = false;
        Map<PointingSource, Orientation> orientationMap = null;

        Collection<Float> fillValues = null;
        int[] linearInterpDims = null;
        int[] maskValues = null;

        ImagingInstrument instrument =
                new ImagingInstrument(SpectralImageMode.MONO, searchQuery, imageType, searchImageSources, instrumentId, rotation, flip, fillValues, linearInterpDims, maskValues, transpose, orientationMap);

        DBRunInfo spcDbInfo =
                DBRunInfo.fromModelImageDir(PointingSource.GASKELL, instrumentId, body.toString(), modelImageDir, tablePrefix);
        DBRunInfo spiceDbInfo =
                DBRunInfo.fromModelImageDir(PointingSource.SPICE, instrumentId, body.toString(), modelImageDir, tablePrefix);
        DBRunInfo[] runInfos = new DBRunInfo[] { spcDbInfo, spiceDbInfo };

        builder.imageSearchRanges(startDate, endDate, maxScDistance, maxResolution);
        builder.add(instrument, runInfos);

        // Final details.
        builder.clients(InternalClientsWithLucyModels);

        SmallBodyViewConfig c = builder.build();
        c.defaultForMissions = LucyClients;
        configArray.add(c);
    }

    /**
     * Prevent instantiation.
     */
    private LucyConfigs()
    {
        throw new AssertionError("All static class");
    }

}
