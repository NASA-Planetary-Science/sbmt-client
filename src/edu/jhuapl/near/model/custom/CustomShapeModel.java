package edu.jhuapl.near.model.custom;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.MapUtil;

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
    public static final String CELL_DATA_PATHS = "CellDataPaths";
    public static final String CELL_DATA_NAMES = "CellDataNames";
    public static final String CELL_DATA_UNITS = "CellDataUnits";
    public static final String CELL_DATA_HAS_NULLS = "CellDataHasNull";
    public static final String LIST_SEPARATOR = ",";

    public static class CellDataInfo
    {
        public String path;
        public String name;
        public String units;
        public boolean hasNulls;

        @Override
        public String toString()
        {
            return path + ", " + name + ", " + units;
        }
    }

    public CustomShapeModel(String name)
    {
        super(
                new String[] { name },
                new String[] { getModelFilename(name) },
                getPlateDataPaths(name),
                getPlateDataInfo(name, CELL_DATA_NAMES),
                getPlateDataInfo(name, CELL_DATA_UNITS),
                stringArrayToBooleanArray(getPlateDataInfo(name, CELL_DATA_HAS_NULLS)),
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

    private static String[] getPlateDataPaths(String name)
    {

        String shapeModelDir = Configuration.getImportedShapeModelsDir() +
                File.separator + name;
        String configfile = shapeModelDir + File.separator + "config.txt";

        Map<String, String> map;
        try
        {
            map = MapUtil.loadMap(configfile);
            if (map.containsKey(CELL_DATA_PATHS))
            {
                String[] paths = map.get(CELL_DATA_PATHS).split(",", -1);
                for (int i=0; i<paths.length; ++i)
                {
                    paths[i] = FileCache.FILE_PREFIX + shapeModelDir + File.separator + "platedata" + i + ".txt";
                }
                return paths;
            }
        }
        catch (IOException e)
        {
        }

        return null;
    }

    private static String[] getPlateDataInfo(String name, String key)
    {
        String configfile = Configuration.getImportedShapeModelsDir() +
        File.separator +
        name +
        File.separator + "config.txt";

        Map<String, String> map;
        try
        {
            map = MapUtil.loadMap(configfile);
            if (map.containsKey(key))
                return map.get(key).split(",", -1);
        }
        catch (IOException e)
        {
        }

        return null;
    }

    private static boolean[] stringArrayToBooleanArray(String[] strArray)
    {
        if (strArray == null)
            return null;

        boolean[] booleanArray = new boolean[strArray.length];
        for (int i=0; i<strArray.length; ++i)
            booleanArray[i] = Boolean.parseBoolean(strArray[i]);

        return booleanArray;
    }
}
