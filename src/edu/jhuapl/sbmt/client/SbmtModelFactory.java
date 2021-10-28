package edu.jhuapl.sbmt.client;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.model.bennu.shapeModel.Bennu;
import edu.jhuapl.sbmt.model.bennu.shapeModel.BennuV4;
import edu.jhuapl.sbmt.model.custom.CustomGraticule;
import edu.jhuapl.sbmt.model.custom.CustomShapeModel;
import edu.jhuapl.sbmt.model.eros.Eros;
import edu.jhuapl.sbmt.model.eros.ErosThomas;
import edu.jhuapl.sbmt.model.itokawa.Itokawa;
import edu.jhuapl.sbmt.model.rosetta.CG;
import edu.jhuapl.sbmt.model.rosetta.Lutetia;
import edu.jhuapl.sbmt.model.simple.Sbmt2SimpleSmallBody;
import edu.jhuapl.sbmt.model.simple.SimpleSmallBody;
import edu.jhuapl.sbmt.model.vesta_old.VestaOld;

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

    static public List<SmallBodyModel> createSmallBodyModel(SmallBodyViewConfig config)
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
        System.out.println("SbmtModelFactory: createSmallBodyModel: has system bodies " + config.hasSystemBodies);
        if (config.hasSystemBodies)
        {
        	System.out.println("SbmtModelFactory: createSmallBodyModel: number of system configs " + config.systemConfigs.size());
        	for (SmallBodyViewConfig extra : config.systemConfigs)
        	{
        		allBodies.addAll(createSmallBodyModel(extra));
        	}
        }

        return allBodies;
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
//    static public DEM createDEM(
//            DEMKey key,
//            SmallBodyModel smallBodyModel) //throws IOException, FitsException
//    {
//        return new DEM(key);
//    }

}
