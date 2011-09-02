package edu.jhuapl.near.model.custom;

import java.io.File;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;

public class CustomShapeModel extends SmallBodyModel
{
    public static final String NAME = "Name";
    public static final String TYPE = "Type";
    public static final String ELLIPSOID = "ellipsoid";
    public static final String CUSTOM = "custom";
    public static final String EQUATORIAL_RADIUS = "EquatorialRadius";
    public static final String POLAR_RADIUS = "PolarRadius";
    public static final String RESOLUTION = "Resolution";
    public static final String CUSTOM_SHAPE_MODEL_PATH = "CustomShapeModelPath";
    public static final String CUSTOM_SHAPE_MODEL_FORMAT = "CustomShapeModelFormat";
    public static final String PDS_FORMAT = "PDS";
    public static final String OBJ_FORMAT = "OBJ";
    public static final String VTK_FORMAT = "VTK";
    public static final String IMAGE_MAP_PATHS = "ImageMapPaths";
    public static final String LOWER_LEFT_LATITUDES = "LLLat";
    public static final String LOWER_LEFT_LONGITUDES = "LLLon";
    public static final String UPPER_RIGHT_LATITUDES = "URLat";
    public static final String UPPER_RIGHT_LONGITUDES = "URLon";
    public static final String LIST_SEPARATOR = ",";


    public CustomShapeModel(String name)
    {
        super(
                new String[] { name },
                new String[] { getModelFilename(name) },
                null,
                null,
                null,
                null,
                false,
                getModelFolder(name),
                ColoringValueType.CELLDATA,
                false);
    }

    private static String getModelFilename(String name)
    {
        return FileCache.FILE_PREFIX +
                Configuration.getImportedShapeModelsDir() +
                File.separator +
                name +
                File.separator +
                "model.vtk";
    }

    private static String[] getModelFolder(String name)
    {
        String path = Configuration.getImportedShapeModelsDir() +
                File.separator +
                name +
                File.separator;

        return new String[]{path};
    }
}
