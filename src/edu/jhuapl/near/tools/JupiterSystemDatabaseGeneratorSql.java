package edu.jhuapl.near.tools;

import java.io.IOException;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class JupiterSystemDatabaseGeneratorSql extends DatabaseGeneratorBaseSql
{
    private ModelConfig modelConfig;
    private String betaSuffix = "_beta";

    @Override
    String getImagesGaskellTableNames()
    {
        return modelConfig.name.toLowerCase() + "images_gaskell" + betaSuffix;
    }

    @Override
    String getCubesGaskellTableNames()
    {
        return modelConfig.name.toLowerCase() + "cubes_gaskell" + betaSuffix;
    }

    @Override
    String getImagesPdsTableNames()
    {
        return modelConfig.name.toLowerCase() + "images_pds" + betaSuffix;
    }

    @Override
    String getCubesPdsTableNames()
    {
        return modelConfig.name.toLowerCase() + "cubes_pds" + betaSuffix;
    }

    @Override
    SmallBodyModel createSmallBodyModel()
    {
        return ModelFactory.createSmallBodyModel(modelConfig);
    }


    @Override
    long getIdFromImageName(String filename)
    {
        if (filename.startsWith("lor_"))
        {
            return Long.parseLong(filename.substring(4, 14), 10);
        }
        else
        {
            System.err.println("Cannot extract ID from filename");
            return -1;
        }
    }

    @Override
    protected boolean ignoreSumfilesWithNoLandmarks(String filename)
    {
        return true;
    }

    public JupiterSystemDatabaseGeneratorSql(ModelConfig modelConfig)
    {
        this.modelConfig = modelConfig;
    }

    private enum RunInfo
    {
        JUPITER(ModelFactory.getModelConfig(ModelFactory.JUPITER, ModelFactory.JUPITER), "/project/nearsdc/data/NEWHORIZONS/JUPITER/IMAGING/imagelist-fullpath.txt"),
        CALLISTO(ModelFactory.getModelConfig(ModelFactory.CALLISTO, ModelFactory.CALLISTO), "/project/nearsdc/data/NEWHORIZONS/CALLISTO/IMAGING/imagelist-fullpath.txt"),
        EUROPA(ModelFactory.getModelConfig(ModelFactory.EUROPA, ModelFactory.EUROPA), "/project/nearsdc/data/NEWHORIZONS/EUROPA/IMAGING/imagelist-fullpath.txt"),
        GANYMEDE(ModelFactory.getModelConfig(ModelFactory.GANYMEDE, ModelFactory.GANYMEDE), "/project/nearsdc/data/NEWHORIZONS/GANYMEDE/IMAGING/imagelist-fullpath.txt"),
        IO(ModelFactory.getModelConfig(ModelFactory.IO, ModelFactory.IO), "/project/nearsdc/data/NEWHORIZONS/IO/IMAGING/imagelist-fullpath.txt");

        public final ModelConfig config;
        public final String pathToFileList;

        private RunInfo(ModelConfig config, String pathToFileList)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        Configuration.setAPLVersion(true);
        NativeLibraryLoader.loadVtkLibraries();

        for (RunInfo ri : RunInfo.values())
        {
            args = new String[]{ri.pathToFileList, "2"};
            JupiterSystemDatabaseGeneratorSql generator = new JupiterSystemDatabaseGeneratorSql(ri.config);
            generator.doMain(args);
        }
    }
}
