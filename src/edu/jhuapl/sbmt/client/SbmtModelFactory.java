package edu.jhuapl.sbmt.client;

import java.io.IOException;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.model.bennu.imaging.MapCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.imaging.MapCamImage;
import edu.jhuapl.sbmt.model.bennu.imaging.MapCamV4Image;
import edu.jhuapl.sbmt.model.bennu.imaging.OcamsFlightImage;
import edu.jhuapl.sbmt.model.bennu.imaging.PolyCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.imaging.PolyCamImage;
import edu.jhuapl.sbmt.model.bennu.imaging.PolyCamV4Image;
import edu.jhuapl.sbmt.model.bennu.imaging.SamCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.shapeModel.Bennu;
import edu.jhuapl.sbmt.model.bennu.shapeModel.BennuV4;
import edu.jhuapl.sbmt.model.ceres.FcCeresImage;
import edu.jhuapl.sbmt.model.custom.CustomGraticule;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.eros.Eros;
import edu.jhuapl.sbmt.model.eros.ErosThomas;
import edu.jhuapl.sbmt.model.eros.LineamentModel;
import edu.jhuapl.sbmt.model.eros.MSIImage;
import edu.jhuapl.sbmt.model.gaspra.SSIGaspraImage;
import edu.jhuapl.sbmt.model.ida.SSIIdaImage;
import edu.jhuapl.sbmt.model.image.BasicPerspectiveImage;
import edu.jhuapl.sbmt.model.image.CustomPerspectiveImage;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.SpectralImageMode;
import edu.jhuapl.sbmt.model.image.marsmissions.MarsMissionImage;
import edu.jhuapl.sbmt.model.itokawa.AmicaImage;
import edu.jhuapl.sbmt.model.itokawa.Itokawa;
import edu.jhuapl.sbmt.model.leisa.LEISAJupiterImage;
import edu.jhuapl.sbmt.model.lorri.LorriImage;
import edu.jhuapl.sbmt.model.mathilde.MSIMathildeImage;
import edu.jhuapl.sbmt.model.mvic.MVICQuadJupiterImage;
import edu.jhuapl.sbmt.model.rosetta.CG;
import edu.jhuapl.sbmt.model.rosetta.Lutetia;
import edu.jhuapl.sbmt.model.rosetta.OsirisImage;
import edu.jhuapl.sbmt.model.ryugu.onc.ONCImage;
import edu.jhuapl.sbmt.model.ryugu.onc.ONCTruthImage;
import edu.jhuapl.sbmt.model.ryugu.tir.TIRImage;
import edu.jhuapl.sbmt.model.saturnmoon.SaturnMoonImage;
import edu.jhuapl.sbmt.model.simple.Sbmt2SimpleSmallBody;
import edu.jhuapl.sbmt.model.simple.SimpleSmallBody;
import edu.jhuapl.sbmt.model.time.StateHistoryModel;
import edu.jhuapl.sbmt.model.time.StateHistoryModel.StateHistoryKey;
import edu.jhuapl.sbmt.model.vesta.FcImage;
import edu.jhuapl.sbmt.model.vesta_old.VestaOld;

import nom.tam.fits.FitsException;

public class SbmtModelFactory
{
//    static public SimulationRun createSimulationRun(
//            SimulationRunKey key,
//            SmallBodyModel smallBodyModel,
//            boolean loadPointingOnly) throws FitsException, IOException
//    {
//        SmallBodyViewConfig config = smallBodyModel.getSmallBodyConfig();
//        return new SimulationRun(key, smallBodyModel);
//    }

    static public StateHistoryModel createStateHistory(
            StateHistoryKey key,
            DateTime start,
            DateTime end,
            SmallBodyModel smallBodyModel,
            Renderer renderer,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();
        return new StateHistoryModel(key, start, end, smallBodyModel, renderer);
    }

    static public Image createImage(
            ImageKeyInterface key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();

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
                    return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
                else
                    return null;
            }
            else if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralImageMode.HYPER)
            {
                if (key.getInstrument().getType() == ImageType.LEISA_JUPITER_IMAGE)
                    return new LEISAJupiterImage(key, smallBodyModel, loadPointingOnly);
                else
                    return null;
            }
            else // SpectralMode.MONO
            {
                if (key.getInstrument().getType() == ImageType.MSI_IMAGE)
                    return new MSIImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.AMICA_IMAGE)
                    return new AmicaImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.FC_IMAGE)
                    return new FcImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.FCCERES_IMAGE)
                    return new FcCeresImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.valueOf("MARS_MOON_IMAGE"))
                    return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.valueOf("PHOBOS_IMAGE"))
                    return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.valueOf("DEIMOS_IMAGE"))
                    return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.OSIRIS_IMAGE)
                    return new OsirisImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SATURN_MOON_IMAGE)
                    return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SSI_GASPRA_IMAGE)
                    return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SSI_IDA_IMAGE)
                    return new SSIIdaImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MSI_MATHILDE_IMAGE)
                    return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.LORRI_IMAGE)
                    return new LorriImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_V3_IMAGE)
                    return new PolyCamImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_V3_IMAGE)
                    return new MapCamImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_V4_IMAGE)
                    return new PolyCamV4Image(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_V4_IMAGE)
                    return new MapCamV4Image(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_EARTH_IMAGE)
                    return new PolyCamEarthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SAMCAM_EARTH_IMAGE)
                    return new SamCamEarthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_EARTH_IMAGE)
                    return new MapCamEarthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.POLYCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.MAPCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.SAMCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.NAVCAM_FLIGHT_IMAGE)
                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.ONC_TRUTH_IMAGE)
                    return new ONCTruthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.ONC_IMAGE)
                    return new ONCImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.TIR_IMAGE)
                    return new TIRImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.GENERIC_IMAGE)
                    return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
            }
        }
        else if (ImageSource.LOCAL_PERSPECTIVE.equals(key.getSource()))
        {
            if (key.getImageType() == ImageType.MSI_IMAGE)
                return new MSIImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.AMICA_IMAGE)
                return new AmicaImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.FC_IMAGE)
                return new FcImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.FCCERES_IMAGE)
                return new FcCeresImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.valueOf("MARS_MOON_IMAGE"))
                return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.valueOf("PHOBOS_IMAGE"))
                return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.valueOf("DEIMOS_IMAGE"))
                return MarsMissionImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.OSIRIS_IMAGE)
                return new OsirisImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.SATURN_MOON_IMAGE)
                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.SSI_GASPRA_IMAGE)
                return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.SSI_IDA_IMAGE)
                return new SSIIdaImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MSI_MATHILDE_IMAGE)
                return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.LORRI_IMAGE)
                return new LorriImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_V3_IMAGE)
                return new PolyCamImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_V3_IMAGE)
                return new MapCamImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_V4_IMAGE)
                return new PolyCamV4Image(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_V4_IMAGE)
                return new MapCamV4Image(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_EARTH_IMAGE)
                return new PolyCamEarthImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.SAMCAM_EARTH_IMAGE)
                return new SamCamEarthImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_EARTH_IMAGE)
                return new MapCamEarthImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.POLYCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MAPCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.SAMCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.NAVCAM_FLIGHT_IMAGE)
                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.GENERIC_IMAGE)
                return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.MVIC_JUPITER_IMAGE)
              return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.LEISA_JUPITER_IMAGE)
                return new LEISAJupiterImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.ONC_IMAGE)
                return new ONCImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.ONC_TRUTH_IMAGE)
                return new ONCTruthImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.TIR_IMAGE)
                return new TIRImage(key, smallBodyModel, loadPointingOnly);
        }
        else if (key instanceof CustomCylindricalImageKey)
        {
            return new CylindricalImage((CustomCylindricalImageKey) key, smallBodyModel);
        }

        return new BasicPerspectiveImage(key, smallBodyModel, loadPointingOnly);
    }

    static public SmallBodyModel createSmallBodyModel(SmallBodyViewConfig config)
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
//            else if (ShapeModelBody.TEMPEL_1 == name)
//            {
//                String[] names = {
//                        name + " low"
//                };
//
//                result = new SimpleSmallBody(config, names);
//            }
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
        return result;
    }

    static public Graticule createGraticule(SmallBodyModel smallBodyModel)
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();
        ShapeModelType author = config.author;

        if (ShapeModelType.GASKELL == author && smallBodyModel.getNumberResolutionLevels() == 4)
        {
            String[] graticulePaths = new String[]{
                    config.rootDirOnServer + "/coordinate_grid_res0.vtk.gz",
                    config.rootDirOnServer + "/coordinate_grid_res1.vtk.gz",
                    config.rootDirOnServer + "/coordinate_grid_res2.vtk.gz",
                    config.rootDirOnServer + "/coordinate_grid_res3.vtk.gz"
            };

            return new Graticule(smallBodyModel, graticulePaths);
        }
        else if (ShapeModelType.CUSTOM == author && !config.customTemporary)
        {
            return new CustomGraticule(smallBodyModel);
        }

        return new Graticule(smallBodyModel);
    }

    static public LineamentModel createLineament()
    {
        return new LineamentModel();
    }

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

    static public DEM createDEM(
            DEMKey key,
            SmallBodyModel smallBodyModel) //throws IOException, FitsException
    {
        return new DEM(key);
    }

}
