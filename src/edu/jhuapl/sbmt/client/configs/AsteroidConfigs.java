package edu.jhuapl.sbmt.client.configs;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SpectralMode;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.tools.DBRunInfo;

public class AsteroidConfigs extends SmallBodyViewConfig
{

	public AsteroidConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(ConfigArrayList configArray)
    {
        AsteroidConfigs c = new AsteroidConfigs();

        // Gaskell Eros
        c.body = ShapeModelBody.EROS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2008)";
        c.rootDirOnServer = "/GASKELL/EROS";
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth";
        c.hasImageMap = true;
        c.hasStateHistory = true;
        c.shapeModelFileNames = prepend("/EROS", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralMode.MONO, //
                        new GenericPhpQuery("/GASKELL/EROS/MSI", "EROS", "/GASKELL/EROS/MSI/gallery"), //
                        ImageType.MSI_IMAGE, //
                        new ImageSource[]{ImageSource.GASKELL_UPDATED, ImageSource.SPICE}, //
                        Instrument.MSI //
                        ) //
        };

        c.hasLidarData = true;
        c.hasMapmaker = true;
        c.hasRemoteMapmaker = false;
        c.bodyDensity = 2.67;
        c.bodyRotationRate = 0.000331165761670640;
        c.bodyReferencePotential = -53.765039959572114;
        c.bodyLowestResModelName = "EROS/shape/shape0.obj";

        c.hasSpectralData = true;
        c.spectralInstruments = new BasicSpectrumInstrument[] {
                new NIS()
        };

        c.hasLineamentData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "iofdbl", "cifdbl" };
        c.imageSearchDefaultMaxSpacecraftDistance = 1000.0;
        c.imageSearchDefaultMaxResolution = 50.0;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
        c.lidarBrowseXYZIndices = new int[] { 14, 15, 16 };
        c.lidarBrowseSpacecraftIndices = new int[] { 8, 9, 10 };
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = true;
        c.lidarBrowseTimeIndex = 4;
        c.lidarBrowseNoiseIndex = 7;
        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/NlrFiles.txt";
        c.lidarBrowseNumberHeaderLines = 2;
        c.lidarBrowseIsInMeters = true;
        c.lidarOffsetScale = 0.025;
        c.lidarInstrumentName = Instrument.NLR;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(ImageSource.GASKELL, Instrument.MSI, ShapeModelBody.EROS.toString(), "/project/nearsdc/data/GASKELL/EROS/MSI/msiImageList.txt", "eros"),
        };

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE,
        															SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL,
        															SbmtMultiMissionTool.Mission.HAYABUSA2_DEPLOY, SbmtMultiMissionTool.Mission.HAYABUSA2_DEV,
        															SbmtMultiMissionTool.Mission.OSIRIS_REX, SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY,
        															SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY, SbmtMultiMissionTool.Mission.NH_DEPLOY};

        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};

        configArray.add(c);

        // Thomas Eros
        c = c.clone();
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2001)";
        c.rootDirOnServer = "/THOMAS/EROS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "eros001708.obj.gz", "eros007790.obj.gz", "eros010152.obj.gz", "eros022540.obj.gz", "eros089398.obj.gz", "eros200700.obj.gz");
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        c.setResolution(ImmutableList.of( //
                "1708 plates", "7790 plates", "10152 plates", //
                "22540 plates", "89398 plates", "200700 plates" //
        ), ImmutableList.of( //
                1708, 7790, 10152, 22540, 89398, 200700 //
        ));
        c.hasMapmaker = false;
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        // Eros NLR
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelType.EROSNLR;
        c.modelLabel = "Neumann et al. (2001)";
        c.rootDirOnServer = "/OTHER/EROSNLR";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "nlrshape.llr2.gz");
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO
        c.setResolution(ImmutableList.of(129600));

        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};

        configArray.add(c);

        // Eros NAV
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelType.EROSNAV;
        c.modelLabel = "NAV team (2001)";
        c.rootDirOnServer = "/OTHER/EROSNAV";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "navplate.obj.gz");
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        c.setResolution(ImmutableList.of(56644));
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        // Gaskell Itokawa
        c = new AsteroidConfigs();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell et al. (2008)";
        c.rootDirOnServer = "/GASKELL/ITOKAWA";
        c.shapeModelFileNames = prepend("/ITOKAWA", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");

        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/ITOKAWA/history/TimeHistory.bth";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralMode.MONO, //
                        new GenericPhpQuery("/GASKELL/ITOKAWA/AMICA", "AMICA", "/GASKELL/ITOKAWA/AMICA/gallery"), //
                        ImageType.AMICA_IMAGE, //
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE, ImageSource.CORRECTED}, //
                        Instrument.AMICA //
                        ) //
        };

        c.hasLidarData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter ul (381 nm)",
                "Filter b (429 nm)",
                "Filter v (553 nm)",
                "Filter w (700 nm)",
                "Filter x (861 nm)",
                "Filter p (960 nm)",
                "Filter zs (1008 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 26.0;
        c.imageSearchDefaultMaxResolution = 3.0;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2005, 10, 30, 0, 0, 0).getTime();
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Optimized", "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        c.lidarSearchDataSourceMap.put("Unfiltered", "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        c.lidarBrowseXYZIndices = new int[] { 6, 7, 8 };
        c.lidarBrowseSpacecraftIndices = new int[] { 3, 4, 5 };
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
        c.lidarBrowseTimeIndex = 1;
        c.lidarBrowseNoiseIndex = -1;
        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/HayLidarFiles.txt";
        c.lidarBrowseNumberHeaderLines = 0;
        c.lidarBrowseIsInMeters = false;
        // The following value is the Itokawa diagonal length divided by
        // 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is
        // 0.025 km.
        c.lidarOffsetScale = 0.00044228259621279913;
        c.lidarInstrumentName = Instrument.LIDAR;

        c.spectralInstruments = new BasicSpectrumInstrument[] {};

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(ImageSource.GASKELL, Instrument.AMICA, ShapeModelBody.ITOKAWA.toString(), "/project/nearsdc/data/GASKELL/ITOKAWA/AMICA/imagelist.txt", "amica"),
        };

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE,
        															SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL,
        															SbmtMultiMissionTool.Mission.HAYABUSA2_DEPLOY, SbmtMultiMissionTool.Mission.HAYABUSA2_DEV,
        															SbmtMultiMissionTool.Mission.OSIRIS_REX, SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY,
        															SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY, SbmtMultiMissionTool.Mission.NH_DEPLOY};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        // Ostro Itokawa
        c = new AsteroidConfigs();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.OSTRO;
        c.modelLabel = "Ostro et al. (2004)";
        c.rootDirOnServer = "/HUDSON/ITOKAWA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "25143itokawa.obj.gz");
        c.setResolution(ImmutableList.of(12192));
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);



        c = new AsteroidConfigs();
        c.body = ShapeModelBody.TOUTATIS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Hudson et al. (2004)";
        c.rootDirOnServer = "/toutatis/hudson";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of("Low (12796 plates)", "High (39996 plates)"), ImmutableList.of(12796, 39996));
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new AsteroidConfigs();
            c.body = ShapeModelBody.CERES;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "SPC";
            c.rootDirOnServer = "/GASKELL/CERES";
            c.hasMapmaker = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new GenericPhpQuery("/GASKELL/CERES/FC", "Ceres", "/GASKELL/CERES/FC/gallery"), //
                            ImageType.FCCERES_IMAGE, //
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE}, //
                            Instrument.FC //
                    ) //
            };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2015, GregorianCalendar.APRIL, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, GregorianCalendar.JULY, 1, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    "Filter 1 (735 nm)",
                    "Filter 2 (548 nm)",
                    "Filter 3 (749 nm)",
                    "Filter 4 (918 nm)",
                    "Filter 5 (978 nm)",
                    "Filter 6 (829 nm)",
                    "Filter 7 (650 nm)",
                    "Filter 8 (428 nm)"
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "FC1", "FC2" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.FC, ShapeModelBody.CERES.toString(), "/project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt", "ceres"),
            };

            c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

            configArray.add(c);
        }



        c = new AsteroidConfigs();
        c.body = ShapeModelBody.VESTA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013)";
        c.rootDirOnServer = "/GASKELL/VESTA";
        c.hasMapmaker = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralMode.MONO, //
                        new GenericPhpQuery("/GASKELL/VESTA/FC", "FC", "/GASKELL/VESTA/FC/gallery"), //
                        ImageType.FC_IMAGE, //
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE}, //
                        Instrument.FC //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(2011, 4, 3, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "FC1", "FC2" };
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(ImageSource.GASKELL, Instrument.FC, ShapeModelBody.VESTA.toString(), "/project/nearsdc/data/GASKELL/VESTA/FC/uniqFcFiles.txt", "fc"),
        };

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        c = new AsteroidConfigs();
        c.body = ShapeModelBody.VESTA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/VESTA_OLD";
        c.shapeModelFileNames = new String[] { "/VESTA_OLD/VESTA.vtk.gz" };
        c.setResolution(ImmutableList.of(49152));
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new AsteroidConfigs();
            c.body = ShapeModelBody.LUTETIA;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "SPC";
            c.rootDirOnServer = "/GASKELL/LUTETIA";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new FixedListQuery("/GASKELL/LUTETIA/IMAGING", "/GASKELL/LUTETIA/IMAGING/gallery"), //
                            ImageType.OSIRIS_IMAGE, //
                            new ImageSource[]{ImageSource.GASKELL}, //
                            Instrument.OSIRIS //
                            ) //
            };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

            configArray.add(c);
        }

        c = new AsteroidConfigs();
        c.body = ShapeModelBody.LUTETIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.JORDA;
        c.modelLabel = "Farnham et al. (2013)";
        c.rootDirOnServer = "/JORDA/LUTETIA";
        c.setResolution(ImmutableList.of( //
                "2962 plates ", "5824 plates ", "11954 plates ", "24526 plates ", //
                "47784 plates ", "98280 plates ", "189724 plates ", "244128 plates ", //
                "382620 plates ", "784510 plates ", "1586194 plates ", "3145728 plates" //
            ), ImmutableList.of( //
                2962, 5824, 11954, 24526, 47784, 98280, 189724, //
                244128, 382620, 784510, 1586194, 3145728 //
            )); //
        c.shapeModelFileNames = prepend(c.rootDirOnServer,
                "shape_res0.vtk.gz", //
                "shape_res1.vtk.gz", //
                "shape_res2.vtk.gz", //
                "shape_res3.vtk.gz", //
                "shape_res4.vtk.gz", //
                "shape_res5.vtk.gz", //
                "shape_res6.vtk.gz", //
                "shape_res7.vtk.gz", //
                "shape_res8.vtk.gz", //
                "shape_res9.vtk.gz", //
                "shape_res10.vtk.gz", //
                "shape_res11.vtk.gz" //
            );
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        c = new AsteroidConfigs();
        c.body = ShapeModelBody.IDA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/IDA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "243ida.llr.gz");
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(32040));

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralMode.MONO, //
                        new FixedListQuery("/THOMAS/IDA/SSI", "/THOMAS/IDA/SSI/images/gallery"), //
                        ImageType.SSI_IDA_IMAGE, //
                        new ImageSource[]{ImageSource.CORRECTED}, //
                        Instrument.SSI //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1993, 7, 28, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1993, 7, 29, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        // This model was delivered on 2018-03-08 to replace the previous model of
        // unknown specific origin.
        c = new AsteroidConfigs();
        c.body = ShapeModelBody.IDA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/ida/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of(5040));
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 2600.;
        c.rotationRate = 0.0003766655;
        c.hasImageMap = true;
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        c = new AsteroidConfigs();
        c.body = ShapeModelBody.MATHILDE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/MATHILDE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "253mathilde.llr.gz");
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(14160));

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralMode.MONO, //
                        new FixedListQuery("/THOMAS/MATHILDE/MSI", "/THOMAS/MATHILDE/MSI/images/gallery"), //
                        ImageType.MSI_MATHILDE_IMAGE, //
                        new ImageSource[]{ImageSource.CORRECTED}, //
                        Instrument.MSI //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1997, 5, 27, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1997, 5, 28, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        // This new model was delivered on 2018-03-08.
        c = new AsteroidConfigs();
        c.body = ShapeModelBody.MATHILDE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/mathilde/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 1300.;
        c.rotationRate = 0.0000041780;
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(5040));

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        c = new AsteroidConfigs();
        c.body = ShapeModelBody.GASPRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/GASPRA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "951gaspra.llr.gz");
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(32040));

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralMode.MONO, //
                        new FixedListQuery("/THOMAS/GASPRA/SSI", "/THOMAS/GASPRA/SSI/images/gallery"), //
                        ImageType.SSI_GASPRA_IMAGE, //
                        new ImageSource[]{ImageSource.CORRECTED}, //
                        Instrument.SSI //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1991, 9, 29, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1991, 9, 30, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        // This model was delivered on 2018-03-08 to replace the previous model of
        // unknown specific origin.
        c = new AsteroidConfigs();
        c.body = ShapeModelBody.GASPRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/gaspra/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 2700.;
        c.rotationRate = 0.0002478;
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(5040));

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);

        c = new AsteroidConfigs();
        c.body = ShapeModelBody.STEINS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.JORDA;
        c.modelLabel = "Farnham and Jorda (2013)";
        c.rootDirOnServer = "/JORDA/STEINS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "steins_cart.plt.gz");
        c.setResolution(ImmutableList.of(20480));

        c.presentInMissions = new SbmtMultiMissionTool.Mission[] {SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new SbmtMultiMissionTool.Mission[] {};

        configArray.add(c);
    }

    public AsteroidConfigs clone() // throws CloneNotSupportedException
    {
        AsteroidConfigs c = (AsteroidConfigs) super.clone();

        return c;
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