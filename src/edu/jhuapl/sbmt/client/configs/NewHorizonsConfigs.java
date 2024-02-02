package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.io.DBRunInfo;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.BasemapImageConfig;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class NewHorizonsConfigs extends SmallBodyViewConfig
{

	public NewHorizonsConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}

	private static void setupFeatures(NewHorizonsConfigs c)
	{
		c.addFeatureConfig(ImagingInstrumentConfig.class, new ImagingInstrumentConfig(c));
        c.addFeatureConfig(SpectrumInstrumentConfig.class, new SpectrumInstrumentConfig(c));
        c.addFeatureConfig(LidarInstrumentConfig.class, new LidarInstrumentConfig(c));
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.addFeatureConfig(StateHistoryConfig.class, new StateHistoryConfig(c));

        FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig(c);
	}


	public static void initialize(ConfigArrayList<IBodyViewConfig> configArray)
    {
        NewHorizonsConfigs c = new NewHorizonsConfigs();

        if (Configuration.isAPLVersion())
        {
            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.JUPITER;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/JUPITER";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
            c.hasColoringData = false;
//            c.hasImageMap = false;
            setupFeatures(c);
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            // imaging instruments

            DataQuerySourcesMetadata lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/JUPITER/IMAGING", "", "JUPITER", "JUPITER", "/NEWHORIZONS/JUPITER/IMAGING/images/gallery");
            DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/JUPITER/MVIC", "", null, null, null);

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(SpectralImageMode.MONO,
                    		new ImageDataQuery(lorriMetadata),
//                    		new GenericPhpQuery("/NEWHORIZONS/JUPITER/IMAGING", "JUPITER", "/NEWHORIZONS/JUPITER/IMAGING/images/gallery"),
                    		ImageType.LORRI_IMAGE,
                    		new PointingSource[] { PointingSource.SPICE },
                    		Instrument.LORRI),

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/JUPITER/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
//                            ), //
//
//                    new ImagingInstrument( //
//                            SpectralMode.HYPER, //
//                            new FixedListQuery("/NEWHORIZONS/JUPITER/LEISA"), //
//                            ImageType.LEISA_JUPITER_IMAGE, //
//                            new ImageSource[]{ImageSource.SPICE}, //
//                            Instrument.LEISA //
                            ) //
                    ); //

            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            imagingConfig.imageSearchFilterNames = new String[] {};
            imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out
            // anything else in this block so that Eclipse updates will continue
            // to keep this code intact.
            // configArray.add(c);
            NewHorizonsConfigs callisto = new NewHorizonsConfigs();
            callisto = c.clone();

            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.AMALTHEA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.STOOKE;
            // 2017-12-20: this name will be correct when "the new model" has been brought
            // in.
            // c.modelLabel = "Stooke (2016)";
            c.rootDirOnServer = "/STOOKE/AMALTHEA";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "j5amalthea.llr.gz");
            // 2017-12-12: exclude this body/model for now, but do not comment out
            // anything else in this block so that Eclipse updates will continue
            // to keep this code intact.
            // configArray.add(c);

            c = callisto.clone();
            c.body = ShapeModelBody.CALLISTO;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/CALLISTO";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//            c.hasImageMap = true;
            c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
            setupFeatures(c);
            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/CALLISTO/IMAGING", "", "CALLISTO", "CALLISTO", "/NEWHORIZONS/CALLISTO/IMAGING/images/gallery");

            // imaging instruments
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new ImageDataQuery(lorriMetadata),
//                            new GenericPhpQuery("/NEWHORIZONS/CALLISTO/IMAGING", "CALLISTO", "/NEWHORIZONS/CALLISTO/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.LORRI //
                            ) //
            );

            // 2017-12-12: exclude this body/model for now, but do not comment out
            // anything else in this block so that Eclipse updates will continue
            // to keep this code intact.
            // configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.EUROPA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/EUROPA";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//            c.hasImageMap = true;

            c.hasFlybyData = true;
            setupFeatures(c);
            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/EUROPA/IMAGING", "", "EUROPA", "EUROPA", "/NEWHORIZONS/EUROPA/IMAGING/images/gallery");
            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/EUROPA/MVIC", "", null, null, null);

            // imaging instruments
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new ImageDataQuery(lorriMetadata),
//                            new GenericPhpQuery("/NEWHORIZONS/EUROPA/IMAGING", "EUROPA", "/NEWHORIZONS/EUROPA/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/EUROPA/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
                            ) //
                    );

            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            imagingConfig.imageSearchFilterNames = new String[] {};
            imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out
            // anything else in this block so that Eclipse updates will continue
            // to keep this code intact.
            // configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.GANYMEDE;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/GANYMEDE";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//            c.hasImageMap = true;
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/GANYMEDE/IMAGING", "", "GANYMEDE", "GANYMEDE", "/NEWHORIZONS/GANYMEDE/IMAGING/images/gallery");
            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/GANYMEDE/MVIC", "", null, null, null);

            // imaging instruments
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new ImageDataQuery(lorriMetadata),
//                            new GenericPhpQuery("/NEWHORIZONS/GANYMEDE/IMAGING", "GANYMEDE", "/NEWHORIZONS/GANYMEDE/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/GANYMEDE/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
                            ) //
                    ); //

            imagingConfig.imageSearchFilterNames = new String[] {};
            imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out
            // anything else in this block so that Eclipse updates will continue
            // to keep this code intact.
            // configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.IO;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/IO";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//            c.hasImageMap = true;
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

            // imaging instruments
            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/IO/IMAGING", "", "IO", "IO", "/NEWHORIZONS/IO/IMAGING/images/gallery");
            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/IO/MVIC", "", null, null, null);

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new ImageDataQuery(lorriMetadata),
//                            new GenericPhpQuery("/NEWHORIZONS/IO/IMAGING", "IO", "/NEWHORIZONS/IO/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/IO/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
                            ) //
                    );

            imagingConfig.imageSearchFilterNames = new String[] {};
            imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything
            // else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.PLUTO;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.NIMMO;
            c.modelLabel = "Nimmo et al. (2017)";
//            c.pathOnServer = "/NEWHORIZONS/PLUTO/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/PLUTO";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
            c.hasColoringData = false;
            setupFeatures(c);
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            DataQuerySourcesMetadata lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/PLUTO/IMAGING", "", null, null, null);
            DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/PLUTO/MVIC", "", null, null, null);

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
//                            new GenericPhpQuery("/NEWHORIZONS/PLUTO/IMAGING", "PLUTO"), //
//                            new FixedListQuery("/NEWHORIZONS/PLUTO/IMAGING", true), //
                            new FixedListDataQuery(lorriMetadata),
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED, PointingSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/PLUTO/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
//                            ), //
//
//                    new ImagingInstrument( //
//                            SpectralMode.HYPER, //
//                            new FixedListQuery("/NEWHORIZONS/PLUTO/LEISA"), //
//                            ImageType.LEISA_JUPITER_IMAGE, //
//                            new ImageSource[]{ImageSource.SPICE}, //
//                            Instrument.LEISA //
                            ) //
            );

            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime();
            imagingConfig.imageSearchFilterNames = new String[] {};
            imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
            c.setResolution(ImmutableList.of(128880));

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.PLUTO.toString(), "/project/nearsdc/data/NEWHORIZONS/PLUTO/IMAGING/imagelist-fullpath.txt", ShapeModelBody.PLUTO.toString().toLowerCase()),
            };

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
            															Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.PLUTO;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.provide("pluto-test");
            c.modelLabel = "Pluto (Test)";
//            c.pathOnServer = "/NEWHORIZONS/PLUTO/shape_res0.vtk.gz";
            c.rootDirOnServer = "/project/sbmtpipeline/rawdata/new-horizons/gitlab-178/pluto/pluto-test";
            c.shapeModelFileExtension = ".obj";
            c.hasColoringData = false;
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            lorriMetadata = DataQuerySourcesMetadata.of(c.rootDirOnServer, "new-horizons/lorri/pluto-test/", null, null, null);
//            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/PLUTO/MVIC", "", null, null, null);

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
//                            new GenericPhpQuery("/NEWHORIZONS/PLUTO/IMAGING", "PLUTO"), //
//                            new FixedListQuery("/NEWHORIZONS/PLUTO/IMAGING", true), //
                            new ImageDataQuery(lorriMetadata),
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.GASKELL}, //
                            Instrument.LORRI //
                            ) //

            );

            // kem_science_spice/tm/kem_science_v30.tm for SPICE metakernel

            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime();
            imagingConfig.imageSearchFilterNames = new String[] {};
            imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
            c.setResolution(ImmutableList.of(128880));

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.PLUTO.toString(), "/project/sbmtpipeline/rawdata/new-horizons/gitlab-178/pluto/pluto-test/imagelist-fullpath-sum.txt", ShapeModelBody.PLUTO.toString().toLowerCase()),
            };

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
            															Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.CHARON;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.NIMMO;
            c.modelLabel = "Nimmo et al. (2017)";
//           c.pathOnServer = "/NEWHORIZONS/CHARON/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/CHARON";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
            c.hasColoringData = false;
            setupFeatures(c);
            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/CHARON/IMAGING", "", null, null);
            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/CHARON/MVIC", "", null, null, null);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListDataQuery(lorriMetadata),
//                            new FixedListQuery("/NEWHORIZONS/CHARON/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/CHARON/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
//                            ), //
//
//                    new ImagingInstrument( //
//                            SpectralMode.HYPER, //
//                            new FixedListQuery("/NEWHORIZONS/CHARON/LEISA"), //
//                            ImageType.LEISA_JUPITER_IMAGE, //
//                            new ImageSource[]{ImageSource.SPICE}, //
//                            Instrument.LEISA //
                            ) //
            );

            c.setResolution(ImmutableList.of(128880));

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.CHARON.toString(), "/project/sbmt2/sbmt/data/bodies/charon/nimmo2017/lorri/imagelist-fullpath-sum.txt", "charon_nimmo2017_lorri"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.LORRI, ShapeModelBody.CHARON.toString(), "/project/sbmt2/sbmt/data/bodies/charon/nimmo2017/lorri/imagelist-fullpath-info.txt", "charon_nimmo2017_lorri"),
            };

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            NewHorizonsConfigs hydra = new NewHorizonsConfigs();

            c = c.clone();
            c.body = ShapeModelBody.HYDRA;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.WEAVER;
            c.modelLabel = "Weaver et al. (2016)";
//            c.pathOnServer = "/NEWHORIZONS/HYDRA/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/HYDRA";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
            c.hasColoringData = false;
            setupFeatures(c);
            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/HYDRA/IMAGING", "", null, null, null);
            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/HYDRA/MVIC", "", null, null, null);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListDataQuery(lorriMetadata),
//                            new FixedListQuery("/NEWHORIZONS/HYDRA/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/HYDRA/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
//                            ), //
//
//                    new ImagingInstrument( //
//                            SpectralMode.HYPER, //
//                            new FixedListQuery("/NEWHORIZONS/HYDRA/LEISA"), //
//                            ImageType.LEISA_JUPITER_IMAGE, //
//                            new ImageSource[]{ImageSource.SPICE}, //
//                            Instrument.LEISA //
                            ) //
            );
            hydra = c.clone();
            c.setResolution(ImmutableList.of(128880));

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.KERBEROS;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.WEAVER;
            c.modelLabel = "Weaver et al. (2016)";
            c.rootDirOnServer = "/NEWHORIZONS/KERBEROS";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
            c.hasColoringData = false;
            c.setResolution(ImmutableList.of(128880));

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);

            c = hydra;
            c.body = ShapeModelBody.NIX;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.WEAVER;
            c.modelLabel = "Weaver et al. (2016)";
//            c.pathOnServer = "/NEWHORIZONS/NIX/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/NIX";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
            c.hasColoringData = false;
            setupFeatures(c);
            lorriMetadata =
            		DataQuerySourcesMetadata.of("/NEWHORIZONS/NIX/IMAGING", "", null, null, null);
            mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/NIX/MVIC", "", null, null, null);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListDataQuery(lorriMetadata),
//                            new FixedListQuery("/NEWHORIZONS/NIX/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralImageMode.MULTI, //
                            new FixedListDataQuery(mvicMetadata),
//                            new FixedListQuery("/NEWHORIZONS/NIX/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE}, //
                            Instrument.MVIC //
//                            ), //
//
//                    new ImagingInstrument( //
//                            SpectralMode.HYPER, //
//                            new FixedListQuery("/NEWHORIZONS/NIX/LEISA"), //
//                            ImageType.LEISA_JUPITER_IMAGE, //
//                            new ImageSource[]{ImageSource.SPICE}, //
//                            Instrument.LEISA //
                            ) //
            );
            c.setResolution(ImmutableList.of(128880));

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};


            configArray.add(c);

            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.STYX;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.WEAVER;
            c.modelLabel = "Weaver et al. (2016)";
            c.rootDirOnServer = "/NEWHORIZONS/STYX";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
            c.hasColoringData = false;
            c.setResolution(ImmutableList.of(128880));

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {};


            configArray.add(c);
        }


        {
        	ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.MU69;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.MU69_TEST5H_1_FINAL_ORIENTED;
            c.rootDirOnServer = "/mu69/mu69-test5h-1-final-oriented";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of("Very Low (25708 plates)"), ImmutableList.of(25708));
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2018, 11, 31, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2019, 0, 2, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e6;
            imagingConfig.imageSearchDefaultMaxResolution = 1.0e4;
            c.density = Double.NaN;
            c.useMinimumReferencePotential = true;
            c.rotationRate = Double.NaN;

//            c.hasImageMap = false;

            DataQuerySourcesMetadata lorriMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/lorri", "", null, null, c.rootDirOnServer + "/lorri/gallery");

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument( //
                            SpectralImageMode.MONO, //
                            new FixedListDataQuery(lorriMetadata),
//                            new FixedListQuery(c.rootDirOnServer + "/lorri", c.rootDirOnServer + "/lorri/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new PointingSource[]{PointingSource.SPICE, PointingSource.GASKELL}, //
                            Instrument.LORRI //
                            ) //
            );

            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            spectrumConfig.hasSpectralData = false;
            spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();


            c.hasStateHistory = false;

            c.hasMapmaker = false;
            spectrumConfig.hasHierarchicalSpectraSearch = false;
            spectrumConfig.hasHypertreeBasedSpectraSearch = false;

            lidarConfig.hasLidarData = false;
            lidarConfig.hasHypertreeBasedLidarSearch = false;

//            if (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.NH_DEPLOY)
//            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
//            }

            c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
            c.defaultForMissions = new Mission[] {Mission.NH_DEPLOY};

            configArray.add(c);
        }

    }

    public NewHorizonsConfigs clone() // throws CloneNotSupportedException
    {
        NewHorizonsConfigs c = (NewHorizonsConfigs) super.clone();

        return c;
    }

	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }
}
