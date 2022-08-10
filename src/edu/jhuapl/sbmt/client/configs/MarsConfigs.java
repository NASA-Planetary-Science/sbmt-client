package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
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
import edu.jhuapl.sbmt.model.phobos.MEGANE;
import edu.jhuapl.sbmt.model.phobos.PhobosExperimentalSearchSpecification;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.tools.DBRunInfo;

public class MarsConfigs extends SmallBodyViewConfig
{

	public MarsConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(ConfigArrayList configArray, boolean publicOnly)
    {
        MarsConfigs c = new MarsConfigs();

        // Gaskell Phobos
        c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2011)";
        c.density = 1.876;
        c.rotationRate = 0.00022803304110600688;
        c.rootDirOnServer = "/GASKELL/PHOBOS";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListQuery("/GASKELL/PHOBOS/imaging", "/GASKELL/PHOBOS/imaging/images/gallery"), //
//                        new GenericPhpQuery("/GASKELL/PHOBOS/IMAGING", "PHOBOS", "/GASKELL/PHOBOS/IMAGING/images/gallery"), //
                        ImageType.valueOf("MARS_MOON_IMAGE"), //
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE}, //
                        Instrument.IMAGING_DATA //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
//        c.imageSearchFilterNames = new String[] {
//                "VSK, Channel 1",
//                "VSK, Channel 2",
//                "VSK, Channel 3",
//                "VIS, Blue",
//                "VIS, Minus Blue",
//                "VIS, Violet",
//                "VIS, Clear",
//                "VIS, Green",
//                "VIS, Red",
//        };
//        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "Phobos 2", "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC" };
        c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        c.imageSearchDefaultMaxResolution = 300.0;
        c.hasLidarData = true;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
        c.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
        c.lidarBrowseIsLidarInSphericalCoordinates = true;
        c.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
        c.lidarBrowseIsTimeInET = true;
        c.lidarBrowseTimeIndex = 5;
        c.lidarBrowseNoiseIndex = -1;
        c.lidarBrowseIsRangeExplicitInData = true;
        c.lidarBrowseRangeIndex = 3;
        c.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
        c.lidarBrowseNumberHeaderLines = 1;
        c.lidarBrowseIsInMeters = true;
        c.lidarOffsetScale = 0.025;
        c.lidarInstrumentName = Instrument.MOLA;

        // MOLA search is disabled for now. See LidarPanel class.
        c.hasHypertreeBasedLidarSearch = true;
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(ImageSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/nearsdc/data/GASKELL/PHOBOS/IMAGING/pdsImageList.txt", ShapeModelBody.PHOBOS.toString().toLowerCase()),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};



        configArray.add(c);

        // Thomas Phobos
        c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/PHOBOS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "m1phobos.llr.gz");
        c.setResolution(ImmutableList.of(32040));

        c.lidarSearchDataSourceMap = Maps.newHashMap(); // this must be instantiated, but can be empty

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};


        configArray.add(c);

        // New Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new MarsConfigs();
            c.body = ShapeModelBody.PHOBOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.provide("Ernst-hierarchical");
            c.modelLabel = "Ernst et al. (hierarchical)";
            c.rootDirOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery"), //
                            ImageType.valueOf("MARS_MOON_IMAGE"), //
                            new ImageSource[]{ImageSource.GASKELL}, //
                            Instrument.IMAGING_DATA //
                            ) //
            };

            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    "VSK, Channel 1",
                    "VSK, Channel 2",
                    "VSK, Channel 3",
                    "VIS, Blue",
                    "VIS, Minus Blue",
                    "VIS, Violet",
                    "VIS, Clear",
                    "VIS, Green",
                    "VIS, Red",
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {
                    "Phobos 2",
                    "Viking Orbiter 1-A",
                    "Viking Orbiter 1-B",
                    "Viking Orbiter 2-A",
                    "Viking Orbiter 2-B",
                    "MEX HRSC",
                    "MRO HiRISE",
                    "MGS MOC"
            };
            c.hasHierarchicalImageSearch = true;
            c.hierarchicalImageSearchSpecification = new PhobosExperimentalSearchSpecification();
            c.hierarchicalImageSearchSpecification.setSelectionModel(new CheckBoxTree(c.hierarchicalImageSearchSpecification.getTreeModel()).getSelectionModel());
            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.lidarSearchDataSourceMap = Maps.newHashMap();
            // This was causing the "cfg does not equal config" type errors when the generator runs.
            // Just commenting it out for now.
//            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/nearsdc/data/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/imagelist.txt", "phobosexp"),
            };

            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

//            configArray.add(c);
        }

        // Latest Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new MarsConfigs();
            c.body = ShapeModelBody.PHOBOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.EXPERIMENTAL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/phobos/ernst2018";
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
//                            new GenericPhpQuery("/phobos/ernst2018/imaging", "PHOBOS_ERNST_2018", "/phobos/ernst2018/imaging/gallery"), //
                            new FixedListQuery("/phobos/ernst2018/imaging", "/phobos/ernst2018/imaging/gallery"), //
                            ImageType.valueOf("MARS_MOON_IMAGE"), //
                            new ImageSource[]{ ImageSource.GASKELL }, //
                            Instrument.IMAGING_DATA, //
                            0., //
                            "Y" // Note: this means "flip along Y axis". Don't know why, but this flip is needed as of this delivery.
                            ) //
            };

            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime();
//            c.imageSearchFilterNames = new String[]{
//                    "VSK, Channel 1",
//                    "VSK, Channel 2",
//                    "VSK, Channel 3",
//                    "VIS, Blue",
//                    "VIS, Minus Blue",
//                    "VIS, Violet",
//                    "VIS, Clear",
//                    "VIS, Green",
//                    "VIS, Red",
//            };
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                    "Phobos 2",
//                    "Viking Orbiter 1-A",
//                    "Viking Orbiter 1-B",
//                    "Viking Orbiter 2-A",
//                    "Viking Orbiter 2-B",
//                    "MEX HRSC",
//                    "MRO HiRISE",
//                    "MGS MOC"
//            };
//            c.hasHierarchicalImageSearch = true;
//            c.hierarchicalImageSearchSpecification = new PhobosExperimentalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
            c.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
            c.lidarBrowseIsLidarInSphericalCoordinates = true;
            c.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
            c.lidarBrowseIsTimeInET = true;
            c.lidarBrowseTimeIndex = 5;
            c.lidarBrowseNoiseIndex = -1;
            c.lidarBrowseIsRangeExplicitInData = true;
            c.lidarBrowseRangeIndex = 3;
            c.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
            c.lidarBrowseNumberHeaderLines = 1;
            c.lidarBrowseIsInMeters = true;
            c.lidarOffsetScale = 0.025;
            c.lidarInstrumentName = Instrument.MOLA;

            // MOLA search is disabled for now. See LidarPanel class.
            c.hasHypertreeBasedLidarSearch = true;
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/sbmt2/sbmt/data/bodies/phobos/ernst2018/imaging/imagelist-fullpath.txt", "phobos_ernst_2018"),
            };



            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        c = new MarsConfigs();
        c.body = ShapeModelBody.DEIMOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/DEIMOS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "DEIMOS.vtk.gz");
//        c.hasStateHistory = true;
//        c.timeHistoryFile = "/DEIMOS/history/TimeHistory.bth";

        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(49152));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new MarsConfigs();
            c.body = ShapeModelBody.DEIMOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.BLENDER;
            c.modelLabel = "OLD Ernst et al. (in progress)";
            c.rootDirOnServer = "/THOMAS/DEIMOSEXPERIMENTAL";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "DEIMOS.vtk.gz");
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new GenericPhpQuery("/THOMAS/DEIMOSEXPERIMENTAL/IMAGING", "DEIMOS", "/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/viking/gallery"), //
                            ImageType.valueOf("MARS_MOON_IMAGE"), //
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED}, //
                            Instrument.IMAGING_DATA //
                            ) //
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    "VIS, Blue",
                    "VIS, Minus Blue",
                    "VIS, Violet",
                    "VIS, Clear",
                    "VIS, Green",
                    "VIS, Red",
            };

            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
            c.imageSearchDefaultMaxResolution = 800.0;

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.DEIMOS.toString(), "/project/nearsdc/data/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/imagelist-fullpath.txt", "deimos"),
            };

//            configArray.add(c);

        }

        // Latest Gaskell Deimos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new MarsConfigs();
            c.body = ShapeModelBody.DEIMOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.EXPERIMENTAL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/deimos/ernst2018";
            c.shapeModelFileExtension = ".obj";
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
//                            new GenericPhpQuery("/deimos/ernst2018/imaging", "DEIMOS_ERNST_2018", "/deimos/ernst2018/imaging/gallery"), //
                            new FixedListQuery("/deimos/ernst2018/imaging", "/deimos/ernst2018/imaging/gallery"), //
                            ImageType.valueOf("MARS_MOON_IMAGE"), //
                            new ImageSource[]{ ImageSource.GASKELL }, //
                            Instrument.IMAGING_DATA, //
                            0., //
                            "Y", // Note: this means "flip along Y axis". Don't know why, but this flip is needed as of this delivery.
                            null
                            ) //
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
//            c.imageSearchFilterNames = new String[]{
//                    "VIS, Blue",
//                    "VIS, Minus Blue",
//                    "VIS, Violet",
//                    "VIS, Clear",
//                    "VIS, Green",
//                    "VIS, Red",
//            };
//
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
            c.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
            c.imageSearchDefaultMaxResolution = 800.0;

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.DEIMOS.toString(), "/project/sbmt2/sbmt/data/bodies/deimos/ernst2018/imaging/imagelist-fullpath.txt", "deimos_ernst_2018"),
            };

            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);


            c = new MarsConfigs();
            c.body = ShapeModelBody.PHOBOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.EXPERIMENTAL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.version = "with MEGANE";
            c.rootDirOnServer = "/phobos/ernst2018-megane";
            c.shapeModelFileExtension = ".obj";
            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/shared/history/timeHistory.bth";
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new FixedListQuery("/phobos/ernst2018-megane/imaging", "/phobos/ernst2018-megane/imaging/gallery"),
                            ImageType.valueOf("MARS_MOON_IMAGE"),
                            new ImageSource[]{ ImageSource.GASKELL },
                            Instrument.IMAGING_DATA,
                            0.,
                            "Y", // Note: this means "flip along Y axis". Don't know why, but this flip is needed as of this delivery.
                            null
                            )
            };

            c.hasSpectralData=true;
            c.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
            c.spectralInstruments.add(new MEGANE());

            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime();

            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
            c.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
            c.lidarBrowseIsLidarInSphericalCoordinates = true;
            c.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
            c.lidarBrowseIsTimeInET = true;
            c.lidarBrowseTimeIndex = 5;
            c.lidarBrowseNoiseIndex = -1;
            c.lidarBrowseIsRangeExplicitInData = true;
            c.lidarBrowseRangeIndex = 3;
            c.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
            c.lidarBrowseNumberHeaderLines = 1;
            c.lidarBrowseIsInMeters = true;
            c.lidarOffsetScale = 0.025;
            c.lidarInstrumentName = Instrument.MOLA;

            // MOLA search is disabled for now. See LidarPanel class.
            c.hasHypertreeBasedLidarSearch = true;
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
            c.stateHistoryStartDate = new GregorianCalendar(2026, 1, 1, 0, 0, 0).getTime();
            c.stateHistoryEndDate = new GregorianCalendar(2026, 9, 30, 0, 0, 0).getTime();
            c.spiceInfo = new SpiceInfo("MMX", "IAU_PHOBOS", "MMX_SPACECRAFT", "PHOBOS", new String[] {"EARTH" , "SUN", "MARS"}, new String[] {"MMX_MEGANE"});

            c.presentInMissions = new Mission[] {Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
            														  Mission.MEGANE_DEPLOY, Mission.MEGANE_DEV, Mission.MEGANE_STAGE,
            														  Mission.MEGANE_TEST};
            c.defaultForMissions = new Mission[] {Mission.MEGANE_DEPLOY, Mission.MEGANE_DEV, Mission.MEGANE_STAGE,
					  													Mission.MEGANE_TEST};

            if (!publicOnly)
            	configArray.add(c);

        }
    }

	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }

    @Override
    public Instrument getLidarInstrument()
    {
        // TODO Auto-generated method stub
        return lidarInstrumentName;
    }

    public boolean hasHypertreeLidarSearch()
    {
        return hasHypertreeBasedLidarSearch;
    }

    public SpectraHierarchicalSearchSpecification<?> getHierarchicalSpectraSearchSpecification()
    {
        return hierarchicalSpectraSearchSpecification;
    }
}