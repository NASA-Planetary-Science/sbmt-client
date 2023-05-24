package edu.jhuapl.sbmt.client.configs;

import java.util.Date;
import java.util.GregorianCalendar;

import com.google.common.collect.ImmutableList;

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
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.tools.DBRunInfo;

public class CometConfigs extends SmallBodyViewConfig
{

    // Months are 0-based: JULY 4 is 6, 4, not 7, 4.
    private static final Date ImageSearchDefaultStartDate = new GregorianCalendar(2005, 6, 4, 0, 0, 0).getTime();
    // Months are 0-based: FEBRUARY 16 is 1, 16 not 2, 16.
    private static final Date ImageSearchDefaultEndDate = new GregorianCalendar(2011, 1, 16, 0, 0, 0).getTime();

    private static final ImageSource[] SumFiles = new ImageSource[] { ImageSource.GASKELL };

	public CometConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}

    public static void initialize(ConfigArrayList configArray)
    {
        CometConfigs c = new CometConfigs();

        // This model was delivered on 2018-03-08 to replace the existing model
        // of unknown specific origin.
        c = new CometConfigs();
        c.body = ShapeModelBody.HALLEY;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/halley/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 600;
        c.rotationRate = 0.0000323209;
        c.setResolution(ImmutableList.of(5040));

        c.presentInMissions =
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new CometConfigs();
            c.body = ShapeModelBody.TEMPEL_1;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell et al. (in progress)";
            c.rootDirOnServer = "/tempel1/gaskell";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "ver64q.vtk.gz");
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.presentInMissions =
                    new Mission[] { Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
            c.defaultForMissions = new Mission[] {};

            // Model identifier string rules: lowercase, no spaces nor
            // underscores
            // (replace with dashes). Single dashes are OK. Valid for building
            // server-side paths.
            String modelId = c.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();

            // Body identifier string rules: lowercase, no spaces nor
            // underscores.
            // (replace with dashes). Single dashes are OK. Valid for building
            // server-side paths.
            String bodyId = c.body.name().replaceAll("[\\s-_]+", "-").toLowerCase();

            // Database table rules: lowercase, no dashes (replace with
            // underscores). Underscores are OK.
            String tableBaseName = (bodyId + "_" + modelId + "_").replaceAll("-", "_").toLowerCase();

            String itsDir = c.rootDirOnServer + "/its";
            String itsTable = tableBaseName + "its";
            String itsDataDir = "/deep-impact/its/";

            String hriDir = c.rootDirOnServer + "/hri";
            String hriTable = tableBaseName + "hri";
            String hriDataDir = "/deep-impact/hri/";

            String mriDir = c.rootDirOnServer + "/mri";
            String mriTable = tableBaseName + "mri";
            String mriDataDir = "/deep-impact/mri/";

            String navcamDir = c.rootDirOnServer + "/navcam";
            String navcamTable = tableBaseName + "navcam";
            String navcamDataDir = "/stardust/navcam/";

            c.imagingInstruments = new ImagingInstrument[] { //
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListQuery<>(itsDir), //
                            // new GenericPhpQuery(itsDir, itsTable, itsTable,
                            // itsDataDir + "gallery"), //
                            ImageType.valueOf("ITS_IMAGE"), //
                            SumFiles, //
                            Instrument.valueFor("ITS"), //
                            0.0, //
                            "None"), //
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListQuery<>(hriDir), //
                            // new GenericPhpQuery(hriDir, hriTable, hriTable,
                            // hriDataDir + "gallery"), //
                            ImageType.valueOf("HRI_IMAGE"), //
                            SumFiles, //
                            Instrument.valueFor("HRI"), //
                            0.0, //
                            "None"), //
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListQuery<>(mriDir), //
                            // new GenericPhpQuery(mriDir, mriTable, mriTable,
                            // mriDataDir + "gallery"), //
                            ImageType.valueOf("MRI_IMAGE"), //
                            SumFiles, //
                            Instrument.valueFor("MRI"), //
                            0.0, //
                            "None"), //
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListQuery<>(navcamDir), //
                            // new GenericPhpQuery(navcamDir, navcamTable,
                            // navcamTable, navcamDataDir + "gallery"), //
                            ImageType.valueOf("NAVCAM_IMAGE"), //
                            SumFiles, //
                            Instrument.valueFor("NAVCAM"), //
                            180.0, //
                            "None"), //
            };

            c.imageSearchDefaultStartDate = ImageSearchDefaultStartDate;
            c.imageSearchDefaultEndDate = ImageSearchDefaultEndDate;
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e4;
            c.imageSearchDefaultMaxResolution = 1.0e3;

            c.databaseRunInfos = new DBRunInfo[] { //
                    new DBRunInfo(ImageSource.GASKELL, Instrument.ITS, bodyId, //
                            itsDir + "/imagelist-fullpath-sum.txt", itsTable), //
                    new DBRunInfo(ImageSource.GASKELL, Instrument.HRI, bodyId, //
                            hriDir + "/imagelist-fullpath-sum.txt", hriTable), //
                    new DBRunInfo(ImageSource.GASKELL, Instrument.MRI, bodyId, //
                            mriDir + "/imagelist-fullpath-sum.txt", mriTable), //
                    new DBRunInfo(ImageSource.GASKELL, Instrument.NAVCAM, bodyId, //
                            navcamDir + "/imagelist-fullpath-sum.txt", navcamTable), //
            };

            configArray.add(c);
        }

        // An update was delivered to this model on 2018-02-22.
        c = new CometConfigs();
        c.body = ShapeModelBody.TEMPEL_1;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Farnham and Thomas (2013)";
        c.rootDirOnServer = "/tempel1/farnham";
        c.shapeModelFileExtension = ".obj";
        // Number of plates found by inspection of files.
        c.setResolution(ImmutableList.of("32040 plates"), ImmutableList.of(32040));
        // Density and rotation rate were provided with delivery manifest.
        c.density = 470.0;
        c.rotationRate = 4.28434129815435E-5;
        c.presentInMissions =
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        c = new CometConfigs();
        c.body = ShapeModelBody.WILD_2;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.DUXBURY;
        c.modelLabel = "Farnham et al. (2005)";
        c.rootDirOnServer = "/OTHER/WILD2";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "wild2_cart_full.w2.gz");
        c.setResolution(ImmutableList.of(17518));
        c.presentInMissions =
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new CometConfigs();
            c.body = ShapeModelBody._67P;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.version = "SHAP5 V0.3";
            c.modelLabel = "Gaskell (SHAP5 V0.3)";
            c.rootDirOnServer = "/GASKELL/67P";

            c.imagingInstruments = new ImagingInstrument[] { new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new GenericPhpQuery("/GASKELL/67P/IMAGING", "67P", "/GASKELL/67P/IMAGING/images/gallery"), //
                            ImageType.OSIRIS_IMAGE, //
                            new ImageSource[]{ImageSource.GASKELL}, //
                    Instrument.OSIRIS, //
                    0.0,
                    "Y"
                            ) //
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 7, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2014, 11, 31, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    // If a name begins with a star, it is not selected by
                    // default
                    "*Filter 1,2", //
                    "*Filter 1,6", //
                    "*Filter 1,8", //
                    "Filter 2,2", //
                    "*Filter 2,3", //
                    "*Filter 2,4", //
                    "*Filter 2,7", //
                    "*Filter 2,8", //
                    "*Filter 4,1", //
                    "*Filter 5,1", //
                    "*Filter 5,4", //
                    "*Filter 6,1" //
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "NAC", "*WAC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;

            c.databaseRunInfos = new DBRunInfo[] { new DBRunInfo(ImageSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/GASKELL/67P/IMAGING/imagelist-fullpath.txt", "67p"),
            };

            c.presentInMissions =
                    new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            c = c.clone();
            c.author = ShapeModelType.DLR;
            c.rootDirOnServer = "/DLR/67P";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, //
                    "cg-dlr_spg-shap4s-v0.9_64m.ply.gz", "cg-dlr_spg-shap4s-v0.9_32m.ply.gz", "cg-dlr_spg-shap4s-v0.9_16m.ply.gz", "cg-dlr_spg-shap4s-v0.9_8m.ply.gz", "cg-dlr_spg-shap4s-v0.9_4m.ply.gz", "cg-dlr_spg-shap4s-v0.9.ply.gz"); //

            c.version = "SHAP4S";
            c.imagingInstruments[0].searchQuery = new GenericPhpQuery("/DLR/67P/IMAGING", "67P_DLR", "/DLR/67P/IMAGING/images/gallery");
            c.setResolution(ImmutableList.of( //
                    "17442 plates ", "72770 plates ", "298442 plates ", "1214922 plates ", //
                    "4895631 plates ", "16745283 plates " //
            ), ImmutableList.of( //
                    17442, 72770, 298442, 1214922, 4895631, 16745283 //
            )); //
            c.hasColoringData = false;

            c.databaseRunInfos = new DBRunInfo[] { new DBRunInfo(ImageSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/DLR/67P/IMAGING/imagelist-fullpath.txt", "67p_dlr"),
            };

            c.presentInMissions =
                    new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            // 67P_V2
            c = new CometConfigs();
            c.body = ShapeModelBody._67P;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.version = "V2";
            c.rootDirOnServer = "/GASKELL/67P_V2";

            c.imagingInstruments = new ImagingInstrument[] { new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                    new GenericPhpQuery("/GASKELL/67P_V2/IMAGING", "67P_V2", "/GASKELL/67P_V3/IMAGING/gallery"), // V2
                                                                                                                 // has
                                                                                                                 // no
                                                                                                                 // gallery
                                                                                                                 // but
                                                                                                                 // images
                                                                                                                 // are
                                                                                                                 // in
                                                                                                                 // V3
                                                                                                                 // gallery
                                                                                                                 // //
                            //new FixedListQuery("/GASKELL/67P_V2/IMAGING"), //
                            ImageType.OSIRIS_IMAGE, //
                            new ImageSource[]{ImageSource.GASKELL}, //
		                    Instrument.OSIRIS , //
		                    0.0, //
		    				"Y"), //
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    // If a name, begins with a star, it is not selected by
                    // default
                    "*Filter 1,2", //
                    "*Filter 1,6", //
                    "*Filter 1,8", //
                    "Filter 2,2", //
                    "*Filter 2,3", //
                    "*Filter 2,4", //
                    "*Filter 2,7", //
                    "*Filter 2,8", //
                    "*Filter 4,1", //
                    "*Filter 5,1", //
                    "*Filter 5,4", //
                    "*Filter 6,1", //
                    "*Filter 1,3", //
                    "*Filter 1,5", //
                    "*Filter 1,7", //
                    "*Filter 3,1", //
                    "*Filter 7,1", //
                    "*Filter 8,2", //
                    "*Filter 8,4", //
                    "*Filter 8,7", //
                    "*Filter 8,8" //
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "NAC", "*WAC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;

            c.databaseRunInfos = new DBRunInfo[] { new DBRunInfo(ImageSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/GASKELL/67P_V2/IMAGING/imagelist-fullpath.txt", "67p_v2"),
            };

            c.presentInMissions =
                    new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            // 67P_V3
            c = new CometConfigs();
            c.body = ShapeModelBody._67P;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/67P_V3";

            c.hasCustomBodyCubeSize = true;
            c.customBodyCubeSize = 0.10; // km

            c.imagingInstruments = new ImagingInstrument[] { new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new GenericPhpQuery("/GASKELL/67P_V3/IMAGING", "67P_V3", "/GASKELL/67P_V3/IMAGING/gallery"), //
                            //new FixedListQuery("/GASKELL/67P_V3/IMAGING"), //
                            ImageType.OSIRIS_IMAGE, //
                            new ImageSource[]{ImageSource.GASKELL}, //
                            Instrument.OSIRIS, //
                            0.0,
                            "Y"
                            ) //
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 0, 31, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    // If a name, begins with a star, it is not selected by
                    // default
                    "*Filter 1,2", //
                    "*Filter 1,6", //
                    "*Filter 1,8", //
                    "Filter 2,2", //
                    "*Filter 2,3", //
                    "*Filter 2,4", //
                    "*Filter 2,7", //
                    "*Filter 2,8", //
                    "*Filter 4,1", //
                    "*Filter 5,1", //
                    "*Filter 5,4", //
                    "*Filter 6,1", //
                    "*Filter 1,3", //
                    "*Filter 1,5", //
                    "*Filter 1,7", //
                    "*Filter 3,1", //
                    "*Filter 7,1", //
                    "*Filter 8,2", //
                    "*Filter 8,4", //
                    "*Filter 8,7", //
                    "*Filter 8,8", //
                    "*Filter 2,1" //
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "NAC", "*WAC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;

            c.databaseRunInfos = new DBRunInfo[] { new DBRunInfo(ImageSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/GASKELL/67P_V3/IMAGING/imagelist-fullpath.txt", "67p_v3"),
            };

            c.presentInMissions =
                    new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        c = new CometConfigs();
        c.body = ShapeModelBody.HARTLEY;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Farnham and Thomas (2013)";
        c.rootDirOnServer = "/THOMAS/HARTLEY";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "hartley2_2012_cart.plt.gz");
        c.setResolution(ImmutableList.of(32040));
        c.presentInMissions =
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

    }

    public CometConfigs clone() // throws CloneNotSupportedException
    {
        CometConfigs c = (CometConfigs) super.clone();

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