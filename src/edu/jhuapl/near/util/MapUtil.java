package edu.jhuapl.near.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtil
{
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
