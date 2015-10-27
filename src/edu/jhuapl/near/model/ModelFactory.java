package edu.jhuapl.near.model;

import java.io.IOException;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.Image.SpectralMode;
import edu.jhuapl.near.model.SmallBodyConfig.ImageType;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.bennu.MapCamImage;
import edu.jhuapl.near.model.bennu.PolyCamImage;
import edu.jhuapl.near.model.ceres.FcCeresImage;
import edu.jhuapl.near.model.custom.CustomGraticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.model.deimos.DeimosImage;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.ErosThomas;
import edu.jhuapl.near.model.eros.LineamentModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.gaspra.SSIGaspraImage;
import edu.jhuapl.near.model.ida.SSIIdaImage;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.model.leisa.LEISAJupiterImage;
import edu.jhuapl.near.model.lorri.LorriImage;
import edu.jhuapl.near.model.mathilde.MSIMathildeImage;
import edu.jhuapl.near.model.mvic.MVICQuadJupiterImage;
import edu.jhuapl.near.model.phobos.PhobosImage;
import edu.jhuapl.near.model.rosetta.CG;
import edu.jhuapl.near.model.rosetta.Lutetia;
import edu.jhuapl.near.model.rosetta.OsirisImage;
import edu.jhuapl.near.model.saturnmoon.SaturnMoonImage;
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.model.vesta.FcImage;
import edu.jhuapl.near.model.vesta_old.VestaOld;

public class ModelFactory
{
    static public Image createImage(
            ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        SmallBodyConfig config = smallBodyModel.getSmallBodyConfig();

        if (ImageSource.SPICE.equals(key.source) ||
                ImageSource.GASKELL.equals(key.source) ||
                ImageSource.LABEL.equals(key.source) ||
                ImageSource.CORRECTED_SPICE.equals(key.source) ||
                ImageSource.CORRECTED.equals(key.source))
        {
            if (key.instrument != null && key.instrument.spectralMode == SpectralMode.MULTI)
            {
                if (key.instrument.type == ImageType.MVIC_JUPITER_IMAGE)
//                    return new MVICJupiterImage(key, smallBodyModel, loadPointingOnly);
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
                else
                    return null;
            }
        }
        else if (ImageSource.LOCAL_PERSPECTIVE.equals(key.source))
        {
            return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly);
        }
        else
        {
            return new CylindricalImage(key, smallBodyModel);
        }
    }

    static public SmallBodyModel createSmallBodyModel(SmallBodyConfig config)
    {
        ShapeModelBody name = config.body;
        ShapeModelAuthor author = config.author;

        if (ShapeModelAuthor.GASKELL == author ||
                (ShapeModelAuthor.EXPERIMENTAL == author && ShapeModelBody.DEIMOS != name))
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
                        config.pathOnServer + "/ver64q.vtk.gz",
                };

                return new SimpleSmallBody(config, names, paths);
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
                        config.pathOnServer + "/ver64q.vtk.gz",
                        config.pathOnServer + "/ver128q.vtk.gz",
                        config.pathOnServer + "/ver256q.vtk.gz",
                        config.pathOnServer + "/ver512q.vtk.gz"
                };

                return new SimpleSmallBody(config, names, paths);
            }
        }
        else if (ShapeModelAuthor.THOMAS == author)
        {
            if (ShapeModelBody.EROS == name)
                return new ErosThomas(config);
            else if (ShapeModelBody.VESTA == name)
                return new VestaOld(config);
        }
        else if (ShapeModelAuthor.JORDA == author)
        {
            if (ShapeModelBody.LUTETIA == name)
                return new Lutetia(config);
        }
        else if (ShapeModelAuthor.DLR == author)
        {
            if (ShapeModelBody._67P == name)
                return new CG(config);
        }
        else if (ShapeModelAuthor.CUSTOM == author)
        {
            return new CustomShapeModel(config);
        }

        return new SimpleSmallBody(config);
    }

    static public Graticule createGraticule(SmallBodyModel smallBodyModel)
    {
        SmallBodyConfig config = smallBodyModel.getSmallBodyConfig();
        ShapeModelAuthor author = config.author;

        if (ShapeModelAuthor.GASKELL == author && smallBodyModel.getNumberResolutionLevels() == 4)
        {
            String[] graticulePaths = new String[]{
                    config.pathOnServer + "/coordinate_grid_res0.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res1.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res2.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res3.vtk.gz"
            };

            return new Graticule(smallBodyModel, graticulePaths);
        }
        else if (ShapeModelAuthor.CUSTOM == author && !config.customTemporary)
        {
            return new CustomGraticule(smallBodyModel);
        }

        return new Graticule(smallBodyModel);
    }

    static public LineamentModel createLineament()
    {
        return new LineamentModel();
    }

    static public NISSpectraCollection createSpectralModel(SmallBodyModel smallBodyModel)
    {
        return new NISSpectraCollection(smallBodyModel);
    }

    static public HashMap<ModelNames, Model> createLidarModels(SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        models.put(ModelNames.LIDAR_BROWSE, new LidarBrowseDataCollection(smallBodyModel));
        models.put(ModelNames.LIDAR_SEARCH, new LidarSearchDataCollection(smallBodyModel));

        return models;
    }
}
