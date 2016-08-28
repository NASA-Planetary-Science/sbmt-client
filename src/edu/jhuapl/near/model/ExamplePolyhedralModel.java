package edu.jhuapl.near.model;

import java.io.File;

import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;

public class ExamplePolyhedralModel extends GenericPolyhedralModel
{
    public ExamplePolyhedralModel(PolyhedralModelConfig config)
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

    private static String getModelFilename(PolyhedralModelConfig config)
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
