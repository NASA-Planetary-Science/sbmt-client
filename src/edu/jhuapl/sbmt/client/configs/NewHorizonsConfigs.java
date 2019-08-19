package edu.jhuapl.sbmt.client.configs;

import java.util.GregorianCalendar;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.ViewConfig;
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
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;

public class NewHorizonsConfigs extends SmallBodyViewConfig
{

	public NewHorizonsConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(ConfigArrayList configArray)
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
            c.hasImageMap = false;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(SpectralMode.MONO, new GenericPhpQuery("/NEWHORIZONS/JUPITER/IMAGING", "JUPITER", "/NEWHORIZONS/JUPITER/IMAGING/images/gallery"), ImageType.LORRI_IMAGE, new ImageSource[] { ImageSource.SPICE }, Instrument.LORRI),

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/JUPITER/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
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
                    }; //

            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
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
            c.hasImageMap = true;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new GenericPhpQuery("/NEWHORIZONS/CALLISTO/IMAGING", "CALLISTO", "/NEWHORIZONS/CALLISTO/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.LORRI //
                            ) //
            };

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
            c.hasImageMap = true;
            c.hasFlybyData = true;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new GenericPhpQuery("/NEWHORIZONS/EUROPA/IMAGING", "EUROPA", "/NEWHORIZONS/EUROPA/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/EUROPA/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.MVIC //
                            ) //
                    };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
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
            c.hasImageMap = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new GenericPhpQuery("/NEWHORIZONS/GANYMEDE/IMAGING", "GANYMEDE", "/NEWHORIZONS/GANYMEDE/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/GANYMEDE/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.MVIC //
                            ) //
                    }; //

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
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
            c.hasImageMap = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new GenericPhpQuery("/NEWHORIZONS/IO/IMAGING", "IO", "/NEWHORIZONS/IO/IMAGING/images/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/IO/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
                            Instrument.MVIC //
                            ) //
                    };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
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

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
//                            new GenericPhpQuery("/NEWHORIZONS/PLUTO/IMAGING", "PLUTO"), //
                            new FixedListQuery("/NEWHORIZONS/PLUTO/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED, ImageSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/PLUTO/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
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
            };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            c.setResolution(ImmutableList.of(128880));
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

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new FixedListQuery("/NEWHORIZONS/CHARON/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/CHARON/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
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
            };

            c.setResolution(ImmutableList.of(128880));
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
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new FixedListQuery("/NEWHORIZONS/HYDRA/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/HYDRA/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
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
            };
            hydra = c.clone();
            c.setResolution(ImmutableList.of(128880));
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
            configArray.add(c);

            c = hydra;
            c.body = ShapeModelBody.NIX;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = ShapeModelType.WEAVER;
            c.modelLabel = "Weaver et al. (2016)";
//            c.pathOnServer = "/NEWHORIZONS/NIX/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/NIX/";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
            c.hasColoringData = false;
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new FixedListQuery("/NEWHORIZONS/NIX/IMAGING", true), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED_SPICE}, //
                            Instrument.LORRI //
                            ), //

                    new ImagingInstrument( //
                            SpectralMode.MULTI, //
                            new FixedListQuery("/NEWHORIZONS/NIX/MVIC"), //
                            ImageType.MVIC_JUPITER_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE}, //
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
            };
            c.setResolution(ImmutableList.of(128880));
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
            configArray.add(c);
        }


        {
            c = new NewHorizonsConfigs();
            c.body = ShapeModelBody.MU69;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.MU69_TEST5H_1_FINAL_ORIENTED;
            c.rootDirOnServer = "/mu69/mu69-test5h-1-final-oriented";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of("Very Low (25708 plates)"), ImmutableList.of(25708));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 11, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2019, 0, 2, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e6;
            c.imageSearchDefaultMaxResolution = 1.0e4;
            c.density = Double.NaN;
            c.useMinimumReferencePotential = true;
            c.rotationRate = Double.NaN;

            c.hasImageMap = false;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument( //
                            SpectralMode.MONO, //
                            new FixedListQuery(c.rootDirOnServer + "/lorri", c.rootDirOnServer + "/lorri/gallery"), //
                            ImageType.LORRI_IMAGE, //
                            new ImageSource[]{ImageSource.SPICE, ImageSource.GASKELL}, //
                            Instrument.LORRI //
                            ), //
            };

            c.hasSpectralData = false;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
            };

            c.hasStateHistory = false;

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = false;
            c.hasHypertreeBasedSpectraSearch = false;

            c.hasLidarData = false;
            c.hasHypertreeBasedLidarSearch = false;

            if (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.NH_DEPLOY)
            {
                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }

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
