package edu.jhuapl.near.tools;

import java.io.IOException;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class JupiterSystemDatabaseGeneratorSql extends DatabaseGeneratorBaseSql
{
    private SmallBodyConfig smallBodyConfig;
    private String betaSuffix = "_beta";

    @Override
    String getImagesGaskellTableNames()
    {
        return smallBodyConfig.body.toString().toLowerCase() + "images_gaskell" + betaSuffix;
    }

    @Override
    String getCubesGaskellTableNames()
    {
        return smallBodyConfig.body.toString().toLowerCase() + "cubes_gaskell" + betaSuffix;
    }

    @Override
    String getImagesPdsTableNames()
    {
        return smallBodyConfig.body.toString().toLowerCase() + "images_pds" + betaSuffix;
    }

    @Override
    String getCubesPdsTableNames()
    {
        return smallBodyConfig.body.toString().toLowerCase() + "cubes_pds" + betaSuffix;
    }

    @Override
    SmallBodyModel createSmallBodyModel()
    {
        return ModelFactory.createSmallBodyModel(smallBodyConfig);
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

    public JupiterSystemDatabaseGeneratorSql(SmallBodyConfig smallBodyConfig)
    {
        this.smallBodyConfig = smallBodyConfig;
    }

    private enum RunInfo
    {
        JUPITER(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.JUPITER, null), "/project/nearsdc/data/NEWHORIZONS/JUPITER/IMAGING/imagelist-fullpath.txt"),
        CALLISTO(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.CALLISTO, null), "/project/nearsdc/data/NEWHORIZONS/CALLISTO/IMAGING/imagelist-fullpath.txt"),
        EUROPA(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.EUROPA, null), "/project/nearsdc/data/NEWHORIZONS/EUROPA/IMAGING/imagelist-fullpath.txt"),
        GANYMEDE(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.GANYMEDE, null), "/project/nearsdc/data/NEWHORIZONS/GANYMEDE/IMAGING/imagelist-fullpath.txt"),
        IO(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.IO, null), "/project/nearsdc/data/NEWHORIZONS/IO/IMAGING/imagelist-fullpath.txt");

        public final SmallBodyConfig config;
        public final String pathToFileList;

        private RunInfo(SmallBodyConfig config, String pathToFileList)
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
