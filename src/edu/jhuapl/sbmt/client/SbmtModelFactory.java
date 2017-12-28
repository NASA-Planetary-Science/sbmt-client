package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.util.HashMap;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.model.bennu.Bennu;
import edu.jhuapl.sbmt.model.bennu.MapCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.MapCamImage;
import edu.jhuapl.sbmt.model.bennu.MapCamV4Image;
import edu.jhuapl.sbmt.model.bennu.PolyCamEarthImage;
import edu.jhuapl.sbmt.model.bennu.PolyCamImage;
import edu.jhuapl.sbmt.model.bennu.PolyCamV4Image;
import edu.jhuapl.sbmt.model.bennu.SamCamEarthImage;
import edu.jhuapl.sbmt.model.ceres.FcCeresImage;
import edu.jhuapl.sbmt.model.custom.CustomGraticule;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.deimos.DeimosImage;
import edu.jhuapl.sbmt.model.dem.DEM;
import edu.jhuapl.sbmt.model.dem.DEM.DEMKey;
import edu.jhuapl.sbmt.model.eros.Eros;
import edu.jhuapl.sbmt.model.eros.ErosThomas;
import edu.jhuapl.sbmt.model.eros.LineamentModel;
import edu.jhuapl.sbmt.model.eros.MSIImage;
import edu.jhuapl.sbmt.model.eros.SpectraCollection;
import edu.jhuapl.sbmt.model.gaspra.SSIGaspraImage;
import edu.jhuapl.sbmt.model.ida.SSIIdaImage;
import edu.jhuapl.sbmt.model.image.CustomPerspectiveImage;
import edu.jhuapl.sbmt.model.image.CylindricalImage;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.itokawa.AmicaImage;
import edu.jhuapl.sbmt.model.itokawa.Itokawa;
import edu.jhuapl.sbmt.model.leisa.LEISAJupiterImage;
import edu.jhuapl.sbmt.model.lidar.LaserLidarBrowseDataCollection;
import edu.jhuapl.sbmt.model.lidar.LaserLidarHyperTreeSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.LidarBrowseDataCollection;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.MolaLidarHyperTreeSearchDataCollection;
import edu.jhuapl.sbmt.model.lidar.OlaLidarHyperTreeSearchDataCollection;
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
        SmallBodyViewConfig config = smallBodyModel.getSmallBodyConfig();
        return new StateHistoryModel(key, start, end, smallBodyModel, renderer);
    }

    static public Image createImage(
            ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();

        if (ImageSource.SPICE.equals(key.source) ||
                ImageSource.GASKELL.equals(key.source) ||
                ImageSource.GASKELL_UPDATED.equals(key.source) ||
                ImageSource.LABEL.equals(key.source) ||
                ImageSource.CORRECTED_SPICE.equals(key.source) ||
                ImageSource.CORRECTED.equals(key.source))
        {
            if (key.instrument != null && key.instrument.spectralMode == SpectralMode.MULTI)
            {
                if (key.instrument.type == ImageType.MVIC_JUPITER_IMAGE)
                    return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
                else
                    return null;
            }
            else if (key.instrument != null && key.instrument.spectralMode == SpectralMode.HYPER)
            {
                if (key.instrument.type == ImageType.LEISA_JUPITER_IMAGE)
                    return new LEISAJupiterImage(key, smallBodyModel, loadPointingOnly);
                else
                    return null;
            }
            else // SpectralMode.MONO
            {
                if (key.instrument.type == ImageType.MSI_IMAGE)
                    return new MSIImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.AMICA_IMAGE)
                    return new AmicaImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.FC_IMAGE)
                    return new FcImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.FCCERES_IMAGE)
                    return new FcCeresImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.PHOBOS_IMAGE)
                    return new PhobosImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.DEIMOS_IMAGE)
                    return new DeimosImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.OSIRIS_IMAGE)
                    return new OsirisImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.SATURN_MOON_IMAGE)
                    return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.SSI_GASPRA_IMAGE)
                    return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.SSI_IDA_IMAGE)
                    return new SSIIdaImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.MSI_MATHILDE_IMAGE)
                    return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.LORRI_IMAGE)
                    return new LorriImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.POLYCAM_IMAGE)
                    return new PolyCamImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.MAPCAM_IMAGE)
                    return new MapCamImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.POLYCAM_V4_IMAGE)
                    return new PolyCamV4Image(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.MAPCAM_V4_IMAGE)
                    return new MapCamV4Image(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.POLYCAM_EARTH_IMAGE)
                    return new PolyCamEarthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.SAMCAM_EARTH_IMAGE)
                    return new SamCamEarthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.MAPCAM_EARTH_IMAGE)
                    return new MapCamEarthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.ONC_TRUTH_IMAGE)
                    return new ONCTruthImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.ONC_IMAGE)
                    return new ONCImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.TIR_IMAGE)
                    return new TIRImage(key, smallBodyModel, loadPointingOnly);
                else if (key.instrument.type == ImageType.GENERIC_IMAGE)
                    return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
                else
                    return null;
            }
        }
        else if (ImageSource.LOCAL_PERSPECTIVE.equals(key.source))
        {
            if (key.imageType == ImageType.MSI_IMAGE)
                return new MSIImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.AMICA_IMAGE)
                return new AmicaImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.FC_IMAGE)
                return new FcImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.FCCERES_IMAGE)
                return new FcCeresImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.PHOBOS_IMAGE)
                return new PhobosImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.DEIMOS_IMAGE)
                return new DeimosImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.OSIRIS_IMAGE)
                return new OsirisImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.SATURN_MOON_IMAGE)
                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.SSI_GASPRA_IMAGE)
                return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.SSI_IDA_IMAGE)
                return new SSIIdaImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.MSI_MATHILDE_IMAGE)
                return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.LORRI_IMAGE)
                return new LorriImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.POLYCAM_IMAGE)
                return new PolyCamImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.MAPCAM_IMAGE)
                return new MapCamImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.GENERIC_IMAGE)
                return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.MVIC_JUPITER_IMAGE)
              return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
            else if (key.imageType == ImageType.LEISA_JUPITER_IMAGE)
                return new LEISAJupiterImage(key, smallBodyModel, loadPointingOnly);
            else if (key.instrument.type == ImageType.ONC_IMAGE)
                return new ONCImage(key, smallBodyModel, loadPointingOnly);
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
        // This will throw an exception if access is not authorized. That's the main
    	// check to perform right here so we ignore the result returned. If the top path
    	// is not "gettable" (this returns false) but no exception
    	// is thrown, it's worth trying to create the model.
        FileCache.isFileGettable(config.serverPath(""));
        ShapeModelBody name = config.body;
        ShapeModelType author = config.author;

        if (ShapeModelType.GASKELL == author ||
                (ShapeModelType.EXPERIMENTAL == author && ShapeModelBody.DEIMOS != name))
        {
            if (ShapeModelBody.EROS == name)
                return new Eros(config);
            else if (ShapeModelBody.ITOKAWA == name)
                return new Itokawa(config);
            else if (ShapeModelBody.TEMPEL_1 == name)
            {
                String[] names = {
                        name + " low"
                };
                String[] paths = {
                        config.rootDirOnServer + "/ver64q.vtk.gz",
                };

                return new SimpleSmallBody(config, names, paths);
            }
            else if (ShapeModelBody.RQ36 == name)
            {
                return new Bennu(config);
            }
            else
            {
                if (config.rootDirOnServer.toLowerCase().equals(config.rootDirOnServer))
                {
                    return new Sbmt2SimpleSmallBody(config);
                }
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

                return new SimpleSmallBody(config, names, paths);
            }
        }
        else if (ShapeModelType.THOMAS == author)
        {
            if (ShapeModelBody.EROS == name)
                return new ErosThomas(config);
            else if (ShapeModelBody.VESTA == name)
                return new VestaOld(config);
        }
        else if (ShapeModelType.JORDA == author)
        {
            if (ShapeModelBody.LUTETIA == name)
                return new Lutetia(config);
        }
        else if (ShapeModelType.DLR == author)
        {
            if (ShapeModelBody._67P == name)
                return new CG(config);
        }
        else if (ShapeModelType.CUSTOM == author)
        {
            return new CustomShapeModel(config);
        }

        if (config.rootDirOnServer.toLowerCase().equals(config.rootDirOnServer))
        {
            return new Sbmt2SimpleSmallBody(config);
        }

        return new SimpleSmallBody(config);
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

    static public SpectraCollection createSpectralModel(SmallBodyModel smallBodyModel)
    {
        ShapeModelBody body=((SmallBodyViewConfig)smallBodyModel.getConfig()).body;
        ShapeModelType author=((SmallBodyViewConfig)smallBodyModel.getConfig()).author;
        String version=((SmallBodyViewConfig)smallBodyModel.getConfig()).version;

        return new SpectraCollection(smallBodyModel);
    }

    static public HashMap<ModelNames, Model> createLidarModels(SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        if (smallBodyModel.getSmallBodyConfig().lidarInstrumentName==Instrument.LASER)
        {
            models.put(ModelNames.LIDAR_BROWSE, new LaserLidarBrowseDataCollection(smallBodyModel));
        }
        else
        {
            models.put(ModelNames.LIDAR_BROWSE, new LidarBrowseDataCollection(smallBodyModel));
        }
        models.put(ModelNames.LIDAR_SEARCH, new LidarSearchDataCollection(smallBodyModel));
        if (smallBodyModel.getSmallBodyConfig().hasHypertreeBasedLidarSearch)
        {
            switch (smallBodyModel.getSmallBodyConfig().lidarInstrumentName)
            {
            case MOLA:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new MolaLidarHyperTreeSearchDataCollection(smallBodyModel));
                break;
            case OLA:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new OlaLidarHyperTreeSearchDataCollection(smallBodyModel));
                break;
            case LASER:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LaserLidarHyperTreeSearchDataCollection(smallBodyModel));
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
