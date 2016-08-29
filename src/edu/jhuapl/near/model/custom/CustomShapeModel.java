package edu.jhuapl.near.model.custom;

import java.io.File;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;

public class CustomShapeModel extends SmallBodyModel
{
    public CustomShapeModel(SmallBodyConfig config)
    {
        super(config,
                new String[] { config.customName },
                new String[] { getModelFilename(config) },
                null,
                null,
                null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false);
    }

    public boolean isBuiltIn()
    {
        return false;
    }

    private static String getModelFilename(SmallBodyConfig config)
    {
        if (config.customTemporary)
        {
            return FileCache.FILE_PREFIX + config.customName;
        }
        else
        {
            return FileCache.FILE_PREFIX +
                    Configuration.getImportedShapeModelsDir() +
                    File.separator +
                    config.customName +
                    File.separator +
                    "model.vtk";
        }
    }
}
