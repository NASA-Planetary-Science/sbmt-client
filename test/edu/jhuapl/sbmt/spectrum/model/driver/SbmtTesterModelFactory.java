//package edu.jhuapl.sbmt.spectrum.model.driver;
//package edu.jhuapl.sbmt.spectrum.model.driver;
//
//import java.io.IOException;
//import java.util.HashMap;
//
//import org.joda.time.DateTime;
//
//import edu.jhuapl.saavtk.gui.render.Renderer;
//import edu.jhuapl.saavtk.model.Graticule;
//import edu.jhuapl.saavtk.model.Model;
//import edu.jhuapl.saavtk.model.ModelNames;
//import edu.jhuapl.saavtk.model.ShapeModelBody;
//import edu.jhuapl.saavtk.model.ShapeModelType;
//import edu.jhuapl.sbmt.client.BodyViewConfig;
//import edu.jhuapl.sbmt.client.SmallBodyModel;
//import edu.jhuapl.sbmt.dtm.model.DEM;
//import edu.jhuapl.sbmt.dtm.model.DEMKey;
//import edu.jhuapl.sbmt.model.bennu.shapeModel.Bennu;
//import edu.jhuapl.sbmt.model.bennu.shapeModel.BennuV4;
//import edu.jhuapl.sbmt.model.custom.CustomGraticule;
//import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
//import edu.jhuapl.sbmt.model.eros.Eros;
//import edu.jhuapl.sbmt.model.eros.ErosThomas;
//import edu.jhuapl.sbmt.model.eros.LineamentModel;
//import edu.jhuapl.sbmt.model.itokawa.Itokawa;
//import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;
//import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;
//import edu.jhuapl.sbmt.model.rosetta.CG;
//import edu.jhuapl.sbmt.model.rosetta.Lutetia;
//import edu.jhuapl.sbmt.model.simple.Sbmt2SimpleSmallBody;
//import edu.jhuapl.sbmt.model.simple.SimpleSmallBody;
//import edu.jhuapl.sbmt.model.time.StateHistoryModel;
//import edu.jhuapl.sbmt.model.time.StateHistoryModel.StateHistoryKey;
//import edu.jhuapl.sbmt.model.vesta_old.VestaOld;
//import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
//import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
//
//import nom.tam.fits.FitsException;
//
//public class SbmtTesterModelFactory
//{
////    static public SimulationRun createSimulationRun(
////            SimulationRunKey key,
////            SmallBodyModel smallBodyModel,
////            boolean loadPointingOnly) throws FitsException, IOException
////    {
////        SmallBodyViewConfigTest config = smallBodyModel.getSmallBodyConfig();
////        return new SimulationRun(key, smallBodyModel);
////    }
//
//    static public StateHistoryModel createStateHistory(
//            StateHistoryKey key,
//            DateTime start,
//            DateTime end,
//            SmallBodyModel smallBodyModel,
//            Renderer renderer,
//            boolean loadPointingOnly) throws FitsException, IOException
//    {
//        SmallBodyViewConfigTest config = (SmallBodyViewConfigTest)smallBodyModel.getSmallBodyConfig();
//        return new StateHistoryModel(key, start, end, smallBodyModel, renderer);
//    }
//
////    static public Image createImage(
////            ImageKeyInterface key,
////            SmallBodyModel smallBodyModel,
////            boolean loadPointingOnly) throws FitsException, IOException
////    {
////        SmallBodyViewConfigTest config = (SmallBodyViewConfigTest)smallBodyModel.getSmallBodyConfig();
////
////        if (ImageSource.SPICE.equals(key.getSource()) ||
////                ImageSource.GASKELL.equals(key.getSource()) ||
////                ImageSource.GASKELL_UPDATED.equals(key.getSource()) ||
////                ImageSource.LABEL.equals(key.getSource()) ||
////                ImageSource.CORRECTED_SPICE.equals(key.getSource()) ||
////                ImageSource.CORRECTED.equals(key.getSource()))
////        {
////            if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralMode.MULTI)
////            {
////                if (key.getInstrument().getType() == SBMTImageType.MVIC_JUPITER_IMAGE)
////                    return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
////                else
////                    return null;
////            }
////            else if (key.getInstrument() != null && key.getInstrument().getSpectralMode() == SpectralMode.HYPER)
////            {
////                if (key.getInstrument().getType() == SBMTImageType.LEISA_JUPITER_IMAGE)
////                    return new LEISAJupiterImage(key, smallBodyModel, loadPointingOnly);
////                else
////                    return null;
////            }
////            else // SpectralMode.MONO
////            {
////                if (key.getInstrument().getType() == SBMTImageType.MSI_IMAGE)
////                    return new MSIImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.AMICA_IMAGE)
////                    return new AmicaImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.FC_IMAGE)
////                    return new FcImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.FCCERES_IMAGE)
////                    return new FcCeresImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.PHOBOS_IMAGE)
////                    return new PhobosImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.DEIMOS_IMAGE)
////                    return new DeimosImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.OSIRIS_IMAGE)
////                    return new OsirisImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.SATURN_MOON_IMAGE)
////                    return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.SSI_GASPRA_IMAGE)
////                    return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.SSI_IDA_IMAGE)
////                    return new SSIIdaImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.MSI_MATHILDE_IMAGE)
////                    return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.LORRI_IMAGE)
////                    return new LorriImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.POLYCAM_V3_IMAGE)
////                    return new PolyCamImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.MAPCAM_V3_IMAGE)
////                    return new MapCamImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.POLYCAM_V4_IMAGE)
////                    return new PolyCamV4Image(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.MAPCAM_V4_IMAGE)
////                    return new MapCamV4Image(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.POLYCAM_EARTH_IMAGE)
////                    return new PolyCamEarthImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.SAMCAM_EARTH_IMAGE)
////                    return new SamCamEarthImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.MAPCAM_EARTH_IMAGE)
////                    return new MapCamEarthImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.POLYCAM_FLIGHT_IMAGE)
////                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.MAPCAM_FLIGHT_IMAGE)
////                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.SAMCAM_FLIGHT_IMAGE)
////                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.NAVCAM_FLIGHT_IMAGE)
////                    return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.ONC_TRUTH_IMAGE)
////                    return new ONCTruthImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.ONC_IMAGE)
////                    return new ONCImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.TIR_IMAGE)
////                    return new TIRImage(key, smallBodyModel, loadPointingOnly);
////                else if (key.getInstrument().getType() == SBMTImageType.GENERIC_IMAGE)
////                    return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
////                else
////                    return null;
////            }
////        }
////        else if (ImageSource.LOCAL_PERSPECTIVE.equals(key.getSource()))
////        {
////            if (key.getImageType() == SBMTImageType.MSI_IMAGE)
////                return new MSIImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.AMICA_IMAGE)
////                return new AmicaImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.FC_IMAGE)
////                return new FcImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.FCCERES_IMAGE)
////                return new FcCeresImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.PHOBOS_IMAGE)
////                return new PhobosImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.DEIMOS_IMAGE)
////                return new DeimosImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.OSIRIS_IMAGE)
////                return new OsirisImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.SATURN_MOON_IMAGE)
////                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.SSI_GASPRA_IMAGE)
////                return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.SSI_IDA_IMAGE)
////                return new SSIIdaImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.MSI_MATHILDE_IMAGE)
////                return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.LORRI_IMAGE)
////                return new LorriImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.POLYCAM_V3_IMAGE)
////                return new PolyCamImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.MAPCAM_V3_IMAGE)
////                return new MapCamImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.POLYCAM_V4_IMAGE)
////                return new PolyCamV4Image(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.MAPCAM_V4_IMAGE)
////                return new MapCamV4Image(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.POLYCAM_EARTH_IMAGE)
////                return new PolyCamEarthImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.SAMCAM_EARTH_IMAGE)
////                return new SamCamEarthImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.MAPCAM_EARTH_IMAGE)
////                return new MapCamEarthImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.POLYCAM_FLIGHT_IMAGE)
////                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.MAPCAM_FLIGHT_IMAGE)
////                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.SAMCAM_FLIGHT_IMAGE)
////                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.NAVCAM_FLIGHT_IMAGE)
////                return OcamsFlightImage.of(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.GENERIC_IMAGE)
////                return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.MVIC_JUPITER_IMAGE)
////              return new MVICQuadJupiterImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.LEISA_JUPITER_IMAGE)
////                return new LEISAJupiterImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.ONC_IMAGE)
////                return new ONCImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.ONC_TRUTH_IMAGE)
////                return new ONCTruthImage(key, smallBodyModel, loadPointingOnly);
////            else if (key.getImageType() == SBMTImageType.TIR_IMAGE)
////                return new TIRImage(key, smallBodyModel, loadPointingOnly);
////            else
////                return null;
////        }
////        else if (key instanceof CustomCylindricalImageKey)
////        {
////            return new CylindricalImage((CustomCylindricalImageKey) key, smallBodyModel);
////        }
////
////        return null;
////    }
//
//    static public SmallBodyModel createSmallBodyModel(BodyViewConfig config)
//    {
//        if (!config.isAccessible())
//        {
//            throw new RuntimeException("Unable to access data for model " + config.getUniqueName());
//        }
//
//        SmallBodyModel result = null;
//        ShapeModelBody name = config.body;
//        ShapeModelType author = config.author;
//
//        if (ShapeModelType.GASKELL == author || ((ShapeModelType.EXPERIMENTAL == author || ShapeModelType.BLENDER == author) && ShapeModelBody.DEIMOS != name))
//        {
//            if (ShapeModelBody.EROS == name)
//            {
//                result = new Eros(config);
//            }
//            else if (ShapeModelBody.ITOKAWA == name)
//            {
//                result = new Itokawa(config);
//            }
////            else if (ShapeModelBody.TEMPEL_1 == name)
////            {
////                String[] names = {
////                        name + " low"
////                };
////
////                result = new SimpleSmallBody(config, names);
////            }
//            else if (ShapeModelBody.RQ36 == name)
//            {
//                if (config.version.equals("V4"))
//                {
//                    result = new BennuV4(config);
//                }
//                else
//                {
//                    result = new Bennu(config);
//                }
//            }
//            else
//            {
//                if (config.rootDirOnServer.toLowerCase().equals(config.rootDirOnServer))
//                {
//                    result = new Sbmt2SimpleSmallBody(config);
//                }
//                else
//                {
//                    String[] names = {
//                            name + " low",
//                            name + " med",
//                            name + " high",
//                            name + " very high"
//                    };
//                    String[] paths = {
//                            config.rootDirOnServer + "/ver64q.vtk.gz",
//                            config.rootDirOnServer + "/ver128q.vtk.gz",
//                            config.rootDirOnServer + "/ver256q.vtk.gz",
//                            config.rootDirOnServer + "/ver512q.vtk.gz"
//                    };
//
//                    result = new SimpleSmallBody(config, names);
//                }
//            }
//        }
//        else if (ShapeModelType.THOMAS == author)
//        {
//            if (ShapeModelBody.EROS == name)
//                result = new ErosThomas(config);
//            else if (ShapeModelBody.VESTA == name)
//                result = new VestaOld(config);
//        }
//        else if (ShapeModelType.JORDA == author)
//        {
//            if (ShapeModelBody.LUTETIA == name)
//                result = new Lutetia(config);
//        }
//        else if (ShapeModelType.DLR == author)
//        {
//            if (ShapeModelBody._67P == name)
//                result = new CG(config);
//        }
//        else if (ShapeModelType.CUSTOM == author)
//        {
//            result = new CustomShapeModel(config);
//        }
//
//        if (result == null)
//        {
//            if (config.rootDirOnServer.toLowerCase().equals(config.rootDirOnServer))
//            {
//                result = new Sbmt2SimpleSmallBody(config);
//            }
//            else
//            {
//                result = new SimpleSmallBody(config);
//            }
//        }
//
//        return result;
//    }
//
//    static public Graticule createGraticule(SmallBodyModel smallBodyModel)
//    {
//        SmallBodyViewConfigTest config = (SmallBodyViewConfigTest)smallBodyModel.getSmallBodyConfig();
//        ShapeModelType author = config.author;
//
//        if (ShapeModelType.GASKELL == author && smallBodyModel.getNumberResolutionLevels() == 4)
//        {
//            String[] graticulePaths = new String[]{
//                    config.rootDirOnServer + "/coordinate_grid_res0.vtk.gz",
//                    config.rootDirOnServer + "/coordinate_grid_res1.vtk.gz",
//                    config.rootDirOnServer + "/coordinate_grid_res2.vtk.gz",
//                    config.rootDirOnServer + "/coordinate_grid_res3.vtk.gz"
//            };
//
//            return new Graticule(smallBodyModel, graticulePaths);
//        }
//        else if (ShapeModelType.CUSTOM == author && !config.customTemporary)
//        {
//            return new CustomGraticule(smallBodyModel);
//        }
//
//        return new Graticule(smallBodyModel);
//    }
//
//    static public LineamentModel createLineament()
//    {
//        return new LineamentModel();
//    }
//
//    static public HashMap<ModelNames, Model> createSpectralModels(SmallBodyModel smallBodyModel)
//    {
//        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();
//
//        ShapeModelBody body=((SmallBodyViewConfigTest)smallBodyModel.getConfig()).body;
//        ShapeModelType author=((SmallBodyViewConfigTest)smallBodyModel.getConfig()).author;
//        String version=((SmallBodyViewConfigTest)smallBodyModel.getConfig()).version;
//
//        models.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));
//
//        models.put(ModelNames.SPECTRA, new SpectraCollection(smallBodyModel));
//        return models;
//    }
//
//    static public HashMap<ModelNames, Model> createLidarModels(SmallBodyModel smallBodyModel)
//    {
//        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();
//
//        models.put(ModelNames.LIDAR_BROWSE, new LidarFileSpecManager(smallBodyModel));
//        models.put(ModelNames.LIDAR_SEARCH, new LidarTrackManager(smallBodyModel));
//        if (smallBodyModel.getSmallBodyConfig().hasHypertreeLidarSearch())
//        {
//            switch (smallBodyModel.getSmallBodyConfig().getLidarInstrument())
//            {
//            case MOLA:
//                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
//                break;
//            case OLA:
//                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
//                break;
//            case LASER:
//                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
//                break;
//                default:
//                	throw new AssertionError();
//            }
//
//
//        }
//
//        return models;
//    }
//
//    static public DEM createDEM(
//            DEMKey key,
//            SmallBodyModel smallBodyModel) //throws IOException, FitsException
//    {
//        return new DEM(key);
//    }
//
//}
