package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.SpectralImageMode;
import edu.jhuapl.sbmt.core.image.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.image.model.BasicPerspectiveImage;
import edu.jhuapl.sbmt.image.model.CustomPerspectiveImage;
import edu.jhuapl.sbmt.image.model.CylindricalImage;
import edu.jhuapl.sbmt.image.model.bodies.bennu.MapCamEarthImage;
import edu.jhuapl.sbmt.image.model.bodies.bennu.MapCamImage;
import edu.jhuapl.sbmt.image.model.bodies.bennu.MapCamV4Image;
import edu.jhuapl.sbmt.image.model.bodies.bennu.OcamsFlightImage;
import edu.jhuapl.sbmt.image.model.bodies.bennu.PolyCamEarthImage;
import edu.jhuapl.sbmt.image.model.bodies.bennu.PolyCamImage;
import edu.jhuapl.sbmt.image.model.bodies.bennu.PolyCamV4Image;
import edu.jhuapl.sbmt.image.model.bodies.bennu.SamCamEarthImage;
import edu.jhuapl.sbmt.image.model.bodies.ceres.FcCeresImage;
import edu.jhuapl.sbmt.image.model.bodies.eros.MSIImage;
import edu.jhuapl.sbmt.image.model.bodies.gaspra.SSIGaspraImage;
import edu.jhuapl.sbmt.image.model.bodies.ida.SSIIdaImage;
import edu.jhuapl.sbmt.image.model.bodies.itokawa.AmicaImage;
import edu.jhuapl.sbmt.image.model.bodies.leisa.LEISAJupiterImage;
import edu.jhuapl.sbmt.image.model.bodies.lorri.LorriImage;
import edu.jhuapl.sbmt.image.model.bodies.mathilde.MSIMathildeImage;
import edu.jhuapl.sbmt.image.model.bodies.mvic.MVICQuadJupiterImage;
import edu.jhuapl.sbmt.image.model.bodies.rosetta.OsirisImage;
import edu.jhuapl.sbmt.image.model.bodies.ryugu.onc.ONCImage;
import edu.jhuapl.sbmt.image.model.bodies.ryugu.onc.ONCTruthImage;
import edu.jhuapl.sbmt.image.model.bodies.ryugu.tir.TIRImage;
import edu.jhuapl.sbmt.image.model.bodies.saturnmoon.SaturnMoonImage;
import edu.jhuapl.sbmt.image.model.bodies.vesta.FcImage;
import edu.jhuapl.sbmt.image.model.bodies.vesta_old.VestaOld;
import edu.jhuapl.sbmt.image.model.marsmissions.MarsMissionImage;
import edu.jhuapl.sbmt.model.bennu.shapeModel.Bennu;
import edu.jhuapl.sbmt.model.bennu.shapeModel.BennuV4;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.eros.Eros;
import edu.jhuapl.sbmt.model.eros.ErosThomas;
import edu.jhuapl.sbmt.model.itokawa.Itokawa;
import edu.jhuapl.sbmt.model.rosetta.CG;
import edu.jhuapl.sbmt.model.rosetta.Lutetia;
import edu.jhuapl.sbmt.model.simple.Sbmt2SimpleSmallBody;
import edu.jhuapl.sbmt.model.simple.SimpleSmallBody;

import nom.tam.fits.FitsException;

public class SbmtModelFactory
{
//    static public StateHistoryModel createStateHistory(
//            StateHistoryKey key,
//            DateTime start,
//            DateTime end,
//            SmallBodyModel smallBodyModel,
//            Renderer renderer,
//            boolean loadPointingOnly) throws FitsException, IOException
//    {
//        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();
//        return new StateHistoryModel(key, start, end, smallBodyModel, renderer);
//    }

//    static public List<SmallBodyModel> createSystemBodyModels(SystemConfigInfo system, List<ViewConfig> loadedConfigs)
//    {
//    	List<SmallBodyModel> smallBodyModels = new ArrayList<SmallBodyModel>();
//    	ConfigArrayList<IBodyViewConfig> builtInConfigs = SmallBodyViewConfig.getBuiltInConfigs();
//    	for (String uniqueName : system.getUniqueName())
//    	{
//    		List<IBodyViewConfig> configs = builtInConfigs.stream().filter(config -> config.getUniqueName().equals(uniqueName)).collect(Collectors.toList());
//    		if (configs.size() > 0) smallBodyModels.addAll(createSmallBodyModel((SmallBodyViewConfig)(configs.get(0))));
//    	}
//
//    	return smallBodyModels;
//    }

	static public Image createImage(
            ImageKeyInterface key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
		return createImage(key, List.of(smallBodyModel), loadPointingOnly);
    }

	static public Image createImage(
            ImageKeyInterface key,
            List<SmallBodyModel> smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {

//        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();

        if (ImageSource.SPICE.equals(key.getSource()) ||
                ImageSource.GASKELL.equals(key.getSource()) ||
                ImageSource.GASKELL_UPDATED.equals(key.getSource()) ||
                ImageSource.LABEL.equals(key.getSource()) ||
                ImageSource.CORRECTED_SPICE.equals(key.getSource()) ||
                ImageSource.CORRECTED.equals(key.getSource()))
        {
            if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralImageMode.MULTI)
            {
                if (key.getInstrument().getType() == ImageType.MVIC_JUPITER_IMAGE)
                    return new MVICQuadJupiterImage(key, smallBodyModel.get(0), loadPointingOnly);
                else
                    return null;
            }
            else if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralImageMode.HYPER)
            {
                if (key.getInstrument().getType() == ImageType.LEISA_JUPITER_IMAGE)
                    return new LEISAJupiterImage(key, smallBodyModel.get(0), loadPointingOnly);
                else
                    return null;
            }
            else // SpectralMode.MONO
            {
                if (key.getInstrument().getType() == ImageType.MSI_IMAGE)
                    return new MSIImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.AMICA_IMAGE)
                    return new AmicaImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.FC_IMAGE)
                    return new FcImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.FCCERES_IMAGE)
                    return new FcCeresImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.valueOf("MARS_MOON_IMAGE"))
                    return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.valueOf("PHOBOS_IMAGE"))
                    return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.valueOf("DEIMOS_IMAGE"))
                    return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.OSIRIS_IMAGE)
                    return new OsirisImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SATURN_MOON_IMAGE)
                    return new SaturnMoonImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SSI_GASPRA_IMAGE)
                    return new SSIGaspraImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SSI_IDA_IMAGE)
                    return new SSIIdaImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MSI_MATHILDE_IMAGE)
                    return new MSIMathildeImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.LORRI_IMAGE)
                    return new LorriImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_V3_IMAGE)
                    return new PolyCamImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_V3_IMAGE)
                    return new MapCamImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_V4_IMAGE)
                    return new PolyCamV4Image(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_V4_IMAGE)
                    return new MapCamV4Image(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_EARTH_IMAGE)
                    return new PolyCamEarthImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SAMCAM_EARTH_IMAGE)
                    return new SamCamEarthImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_EARTH_IMAGE)
                    return new MapCamEarthImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SAMCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.NAVCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.ONC_TRUTH_IMAGE)
                    return new ONCTruthImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.ONC_IMAGE)
                    return new ONCImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.TIR_IMAGE)
                    return new TIRImage(key, smallBodyModel.get(0), loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.GENERIC_IMAGE)
                    return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
            }
        }
        else if (ImageSource.LOCAL_PERSPECTIVE.equals(key.getSource()))
        {
            if (key.getImageType() == ImageType.MSI_IMAGE)
                return new MSIImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.AMICA_IMAGE)
                return new AmicaImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.FC_IMAGE)
                return new FcImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.FCCERES_IMAGE)
                return new FcCeresImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.valueOf("MARS_MOON_IMAGE"))
                return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.valueOf("PHOBOS_IMAGE"))
                return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.valueOf("DEIMOS_IMAGE"))
                return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.OSIRIS_IMAGE)
                return new OsirisImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.SATURN_MOON_IMAGE)
                return new SaturnMoonImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.SSI_GASPRA_IMAGE)
                return new SSIGaspraImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.SSI_IDA_IMAGE)
                return new SSIIdaImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.MSI_MATHILDE_IMAGE)
                return new MSIMathildeImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.LORRI_IMAGE)
                return new LorriImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_V3_IMAGE)
                return new PolyCamImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_V3_IMAGE)
                return new MapCamImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_V4_IMAGE)
                return new PolyCamV4Image(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_V4_IMAGE)
                return new MapCamV4Image(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_EARTH_IMAGE)
                return new PolyCamEarthImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.SAMCAM_EARTH_IMAGE)
                return new SamCamEarthImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_EARTH_IMAGE)
                return new MapCamEarthImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.SAMCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.NAVCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.GENERIC_IMAGE)
                return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MVIC_JUPITER_IMAGE)
              return new MVICQuadJupiterImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.LEISA_JUPITER_IMAGE)
                return new LEISAJupiterImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.ONC_IMAGE)
                return new ONCImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.ONC_TRUTH_IMAGE)
                return new ONCTruthImage(key, smallBodyModel.get(0), loadPointingOnly);
            else if (key.getImageType() == ImageType.TIR_IMAGE)
                return new TIRImage(key, smallBodyModel.get(0), loadPointingOnly);
        }
        else if (key instanceof CustomCylindricalImageKey)
        {
            return new CylindricalImage((CustomCylindricalImageKey) key, smallBodyModel);
        }
        return new BasicPerspectiveImage(key, smallBodyModel, loadPointingOnly);
    }

	static public SmallBodyModel createSmallBodyModel(SmallBodyViewConfig config)
	{
		return createSmallBodyModels(config).get(0);
	}

    static public List<SmallBodyModel> createSmallBodyModels(SmallBodyViewConfig config)
    {
        SmallBodyModel result = null;
        ShapeModelBody name = config.body;
        ShapeModelType author = config.author;

        if (ShapeModelType.GASKELL == author || ((ShapeModelType.EXPERIMENTAL == author || ShapeModelType.BLENDER == author) && ShapeModelBody.DEIMOS != name))
        {
            if (ShapeModelBody.EROS == name)
            {
                result = new Eros(config);
            }
            else if (ShapeModelBody.ITOKAWA == name)
            {
                result = new Itokawa(config);
            }
            else if (ShapeModelBody.RQ36 == name)
            {
                if (config.version.equals("V4"))
                {
                    result = new BennuV4(config);
                }
                else
                {
                    result = new Bennu(config);
                }
            }
            else
            {
                if (config.rootDirOnServer.toLowerCase().equals(config.rootDirOnServer))
                {
                    result = new Sbmt2SimpleSmallBody(config);
                }
                else
                {
                    String[] names = {
                            name + " low",
                            name + " med",
                            name + " high",
                            name + " very high"
                    };
                    String[] paths = {
                            config.rootDirOnServer + "/ver64q.vtk.gz",
                            config.rootDirOnServer + "/ver128q.vtk.gz",
                            config.rootDirOnServer + "/ver256q.vtk.gz",
                            config.rootDirOnServer + "/ver512q.vtk.gz"
                    };

                    result = new SimpleSmallBody(config, names);
                }
            }
        }
        else if (ShapeModelType.THOMAS == author)
        {
            if (ShapeModelBody.EROS == name)
                result = new ErosThomas(config);
            else if (ShapeModelBody.VESTA == name)
                result = new VestaOld(config);
        }
        else if (ShapeModelType.JORDA == author)
        {
            if (ShapeModelBody.LUTETIA == name)
                result = new Lutetia(config);
        }
        else if (ShapeModelType.DLR == author)
        {
            if (ShapeModelBody._67P == name)
                result = new CG(config);
        }
        else if (ShapeModelType.CUSTOM == author)
        {
            result = new CustomShapeModel(config);
        }

        if (result == null)
        {
            if (config.rootDirOnServer.toLowerCase().equals(config.rootDirOnServer))
            {
                result = new Sbmt2SimpleSmallBody(config);
            }
            else
            {
                result = new SimpleSmallBody(config);
            }
        }
        //check for other bodies in the sytem
        List<SmallBodyModel> allBodies = Lists.newArrayList();
        allBodies.add(result);
        if (config.hasSystemBodies)
        {
        	for (SmallBodyViewConfig extra : config.systemConfigs)
        	{
        		allBodies.addAll(createSmallBodyModels(extra));
        	}
        }

        return allBodies;
    }

//    static public LineamentModel createLineament()
//    {
//        return new LineamentModel();
//    }

//    static public HashMap<ModelNames, Model> createSpectralModels(SmallBodyModel smallBodyModel)
//    {
//        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();
//
//        ShapeModelBody body=((SmallBodyViewConfig)smallBodyModel.getConfig()).body;
//        ShapeModelType author=((SmallBodyViewConfig)smallBodyModel.getConfig()).author;
//        String version=((SmallBodyViewConfig)smallBodyModel.getConfig()).version;
//
//        models.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));
//
//        models.put(ModelNames.SPECTRA, new SpectraCollection(smallBodyModel));
//        return models;
//    }
//
    static public DEM createDEM(
            DEMKey key,
            SmallBodyModel smallBodyModel) //throws IOException, FitsException
    {
        return new DEM(key);
    }

}
