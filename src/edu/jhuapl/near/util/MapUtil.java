package edu.jhuapl.near.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

// Implementation of a map that's always backed up to a file on disk.
public class MapUtil
{
    // The underlying map
    private Map<String, String> map;

    // File storing the map
    private String filename;

    public MapUtil(String filename)
    {
        try
        {
            this.filename = filename;
            map = loadMap(filename);
        }
        catch (IOException e)
        {
            // If we can't load the map from a file, create an empty map
            map = new LinkedHashMap<String, String>();
        }
    }

    /**
     *
     * @param key
     * @param defaultValue default value to return if key is not contained in map
     * @return
     */
    public String get(String key, String defaultValue)
    {
        if (map.containsKey(key))
            return map.get(key);
        else
            return defaultValue;
    }

    public double getAsDouble(String key, Double defaultValue)
    {
        return Double.parseDouble(get(key, defaultValue.toString()));
    }

    public long getAsLong(String key, Long defaultValue)
    {
        return Long.parseLong(get(key, defaultValue.toString()));
    }

    public boolean getAsBoolean(String key, Boolean defaultValue)
    {
        return Boolean.parseBoolean(get(key, defaultValue.toString()));
    }

    public void put(String key, String value)
    {
        try
        {
            map.put(key, value);
            saveMap(map, filename);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void put(String key, Double value)
    {
        put(key, value.toString());
    }

    public void put(String key, Long value)
    {
        put(key, value.toString());
    }

    public void put(String key, Boolean value)
    {
        put(key, value.toString());
    }

    public void put(Map<String, Object> otherMap)
    {
        try
        {
            for (String key : otherMap.keySet())
                map.put(key, otherMap.get(key).toString());
            saveMap(map, filename);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void saveMap(Map<String, String> map, String filename) throws IOException
    {
        FileWriter fstream = new FileWriter(filename);
        BufferedWriter out = new BufferedWriter(fstream);

        for (String key : map.keySet())
        {
            String value = map.get(key);
            out.write(key.trim() + "=" + value + "\n");
        }

        out.close();
    }

    public static Map<String, String> loadMap(String filename) throws IOException
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

        ArrayList<String> lines = FileUtil.getFileLinesAsStringList(filename);
        for (String line : lines)
        {
            int equalsIdx = line.indexOf('=');
            if (equalsIdx < 0 || line.trim().startsWith("#"))
                continue;

            String key = line.substring(0, equalsIdx).trim();
            if (key.length() == 0)
                continue;

            String value = line.substring(equalsIdx+1);

            map.put(key, value);
        }

        return map;
    }

}
