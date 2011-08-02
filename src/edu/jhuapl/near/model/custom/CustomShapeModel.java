package edu.jhuapl.near.model.custom;

import java.io.File;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;

public class CustomShapeModel extends SmallBodyModel
{
    public CustomShapeModel(String name)
    {
        super(
                new String[] { name },
                new String[] { getModelPath(name) },
                null,
                null,
                null,
                null,
                false,
                getImagePath(name),
                ColoringValueType.CELLDATA,
                false);
    }

    private static String getModelPath(String name)
    {
        return FileCache.FILE_PREFIX +
                Configuration.getImportedShapeModelsDir() +
                File.separator +
                name +
                File.separator +
                "model.vtk";
    }

    private static String getImagePath(String name)
    {
        String path = Configuration.getImportedShapeModelsDir() +
                File.separator +
                name +
                File.separator +
                "image.png";

        if (!(new File(path)).exists())
            return null;
        else
            return FileCache.FILE_PREFIX + path;
    }
}
