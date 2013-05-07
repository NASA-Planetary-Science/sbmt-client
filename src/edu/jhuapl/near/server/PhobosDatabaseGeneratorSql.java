package edu.jhuapl.near.server;

import java.io.IOException;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.model.SmallBodyModel;

public class PhobosDatabaseGeneratorSql extends DatabaseGeneratorBaseSql
{
    static private final String ImagesGaskellTable = "phobosimages_gaskell_beta";
    static private final String CubesGaskellTable = "phoboscubes_gaskell_beta";
    static private final String ImagesPdsTable = "phobosimages_pds_beta";
    static private final String CubesPdsTable = "phoboscubes_pds_beta";

    @Override
    String getImagesGaskellTableNames()
    {
        return ImagesGaskellTable;
    }

    @Override
    String getCubesGaskellTableNames()
    {
        return CubesGaskellTable;
    }

    @Override
    String getImagesPdsTableNames()
    {
        return ImagesPdsTable;
    }

    @Override
    String getCubesPdsTableNames()
    {
        return CubesPdsTable;
    }

    @Override
    SmallBodyModel createSmallBodyModel()
    {
        ModelConfig config = new ModelConfig(
                ModelFactory.PHOBOS,
                ModelFactory.SATELLITES,
                ModelFactory.MARS,
                ModelFactory.IMAGE_BASED,
                ModelFactory.THOMAS,
                "/THOMAS/PHOBOS/m1phobos.llr.gz");
        return ModelFactory.createSmallBodyModel(config);
    }


    @Override
    long getIdFromImageName(String filename)
    {
        if (filename.startsWith("P"))
        {
            return Long.parseLong(filename.substring(1, 8), 10);
        }
        else
        {
            filename = filename.replace("A", "1");
            filename = filename.replace("B", "2");
            return Long.parseLong(filename.substring(2, 8), 10);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        PhobosDatabaseGeneratorSql generator = new PhobosDatabaseGeneratorSql();
        generator.doMain(args);
    }
}
