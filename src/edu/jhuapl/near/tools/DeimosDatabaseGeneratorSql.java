package edu.jhuapl.near.tools;

import java.io.IOException;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;

public class DeimosDatabaseGeneratorSql extends DatabaseGeneratorBaseSql
{
    static private final String ImagesGaskellTable = "deimosimages_gaskell_beta";
    static private final String CubesGaskellTable = "deimoscubes_gaskell_beta";
    static private final String ImagesPdsTable = "deimosimages_pds_beta";
    static private final String CubesPdsTable = "deimoscubes_pds_beta";

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
        return ModelFactory.createSmallBodyModel(ModelFactory.getModelConfig(ModelFactory.DEIMOS, ModelFactory.THOMAS));
    }


    @Override
    long getIdFromImageName(String filename)
    {
        if (filename.startsWith("PSP"))
        {
            return Long.parseLong(
                    filename.substring(4, 10) +
                    filename.substring(11, 15) +
                    filename.substring(19, 20) +
                    filename.substring(21, 22),
                    10);
        }
        else if (filename.startsWith("P"))
        {
            return Long.parseLong(filename.substring(1, 8), 10);
        }
        else if (filename.startsWith("V"))
        {
            filename = filename.replace("A", "1");
            filename = filename.replace("B", "2");
            return Long.parseLong(filename.substring(2, 8), 10);
        }
        else if (filename.startsWith("f"))
        {
            filename = filename.replace("a", "1");
            filename = filename.replace("b", "2");
            return Long.parseLong(filename.substring(1, 7), 10);
        }
        else if (filename.startsWith("h"))
        {
            return Long.parseLong(filename.substring(1, 5) + filename.substring(6, 10), 10);
        }
        else if (filename.startsWith("sp"))
        {
            return Long.parseLong(filename.substring(2, 8), 10);
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
        if (filename.startsWith("PSP") || filename.startsWith("sp"))
            return false;
        else
            return true;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        DeimosDatabaseGeneratorSql generator = new DeimosDatabaseGeneratorSql();
        generator.doMain(args);
    }
}
