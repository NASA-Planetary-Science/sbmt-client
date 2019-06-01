package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.util.HashMap;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.model.bennu.Bennu;
import edu.jhuapl.sbmt.model.bennu.BennuV4;
import edu.jhuapl.sbmt.model.bennu.MapCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.MapCamImage;
import edu.jhuapl.sbmt.model.bennu.MapCamV4Image;
import edu.jhuapl.sbmt.model.bennu.OcamsFlightImage;
import edu.jhuapl.sbmt.model.bennu.PolyCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.PolyCamImage;
import edu.jhuapl.sbmt.model.bennu.PolyCamV4Image;
import edu.jhuapl.sbmt.model.bennu.SamCamEarthImage;
import edu.jhuapl.sbmt.model.ceres.FcCeresImage;
import edu.jhuapl.sbmt.model.custom.CustomGraticule;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.deimos.DeimosImage;
import edu.jhuapl.sbmt.model.dem.DEM;
import edu.jhuapl.sbmt.model.dem.DEMKey;
import edu.jhuapl.sbmt.model.eros.Eros;
import edu.jhuapl.sbmt.model.eros.ErosThomas;
import edu.jhuapl.sbmt.model.eros.LineamentModel;
import edu.jhuapl.sbmt.model.eros.MSIImage;
import edu.jhuapl.sbmt.model.gaspra.SSIGaspraImage;
import edu.jhuapl.sbmt.model.ida.SSIIdaImage;
import edu.jhuapl.sbmt.model.image.CustomPerspectiveImage;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.itokawa.AmicaImage;
import edu.jhuapl.sbmt.model.itokawa.Itokawa;
import edu.jhuapl.sbmt.model.leisa.LEISAJupiterImage;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.model.lorri.LorriImage;
import edu.jhuapl.sbmt.model.mathilde.MSIMathildeImage;
import edu.jhuapl.sbmt.model.mvic.MVICQuadJupiterImage;
import edu.jhuapl.sbmt.model.phobos.PhobosImage;
import edu.jhuapl.sbmt.model.rosetta.CG;
import edu.jhuapl.sbmt.model.rosetta.Lutetia;
import edu.jhuapl.sbmt.model.rosetta.OsirisImage;
import edu.jhuapl.sbmt.model.ryugu.ONCImage;
import edu.jhuapl.sbmt.model.ryugu.ONCTruthImage;
import edu.jhuapl.sbmt.model.ryugu.TIRImage;
import edu.jhuapl.sbmt.model.saturnmoon.SaturnMoonImage;
import edu.jhuapl.sbmt.model.simple.Sbmt2SimpleSmallBody;
import edu.jhuapl.sbmt.model.simple.SimpleSmallBody;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.SpectraSearchDataCollection;
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
            if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralMode.MULTI)
            {
                if (key.getInstrument().getType() == ImageType.MVIC_JUPITER_IMAGE)
                    return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
                else
                    return null;
            }
            else if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralMode.HYPER)
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
                else if (key.getInstrument().getType() == ImageType.PHOBOS_IMAGE)
                    return new PhobosImage(key, smallBodyModel, loadPointingOnly);
                else if (key.getInstrument().getType() == ImageType.DEIMOS_IMAGE)
                    return new DeimosImage(key, smallBodyModel, loadPointingOnly);
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
                else
                    return null;
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
            else if (key.getImageType() == ImageType.PHOBOS_IMAGE)
                return new PhobosImage(key, smallBodyModel, loadPointingOnly);
            else if (key.getImageType() == ImageType.DEIMOS_IMAGE)
                return new DeimosImage(key, smallBodyModel, loadPointingOnly);
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
            else
                return null;
        }
        else
        {
            return new CylindricalImage(key, smallBodyModel);
        }
    }

    static public SmallBodyModel createSmallBodyModel(SmallBodyViewConfig config)
    {
        if (!config.isAccessible())
        {
            throw new RuntimeException("Unable to access data for model " + config.getUniqueName());
        }

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
            else if (ShapeModelBody.TEMPEL_1 == name)
            {
                String[] names = {
                        name + " low"
                };
                String[] paths = {
                        config.rootDirOnServer + "/ver64q.vtk.gz",
                };

                result = new SimpleSmallBody(config, names, paths);
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

                    result = new SimpleSmallBody(config, names, paths);
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

    static public HashMap<ModelNames, Model> createSpectralModels(SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        ShapeModelBody body=((SmallBodyViewConfig)smallBodyModel.getConfig()).body;
        ShapeModelType author=((SmallBodyViewConfig)smallBodyModel.getConfig()).author;
        String version=((SmallBodyViewConfig)smallBodyModel.getConfig()).version;

        models.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));

        models.put(ModelNames.SPECTRA, new SpectraCollection(smallBodyModel));
        return models;
    }

    static public HashMap<ModelNames, Model> createLidarModels(SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        models.put(ModelNames.LIDAR_BROWSE, new LidarFileSpecManager(smallBodyModel));
        models.put(ModelNames.LIDAR_SEARCH, new LidarTrackManager(smallBodyModel));
        if (smallBodyModel.getSmallBodyConfig().hasHypertreeLidarSearch())
        {
            switch (smallBodyModel.getSmallBodyConfig().getLidarInstrument())
            {
            case MOLA:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
                break;
            case OLA:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
                break;
            case LASER:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
                break;
                default:
                	throw new AssertionError();
            }


        }

        return models;
    }

    static public DEM createDEM(
            DEMKey key,
            SmallBodyModel smallBodyModel) throws IOException, FitsException
    {
        return new DEM(key);
    }

}
