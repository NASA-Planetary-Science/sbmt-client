package edu.jhuapl.near.model.custom;

import java.io.File;

import edu.jhuapl.near.app.SmallBodyModel;
import edu.jhuapl.near.app.SmallBodyViewConfig;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;

public class CustomShapeModel extends SmallBodyModel
{
    public CustomShapeModel(SmallBodyViewConfig config)
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

    private static String getModelFilename(SmallBodyViewConfig config)
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
