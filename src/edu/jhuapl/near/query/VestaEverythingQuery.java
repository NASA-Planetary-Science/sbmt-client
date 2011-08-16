package edu.jhuapl.near.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.util.DateTimeUtil;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;

public class VestaEverythingQuery extends QueryBase
{
    private static VestaEverythingQuery ref = null;

    public static VestaEverythingQuery getInstance()
    {
        if (ref == null)
            ref = new VestaEverythingQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private VestaEverythingQuery()
    {
    }

    @Override
    public ArrayList<ArrayList<String>> runQuery(
            String type,
            DateTime startDate,
            DateTime stopDate,
            ArrayList<Integer> filters,
            boolean fc1_unused,
            boolean fc2_unused,
            double startDistance,
            double stopDistance,
            double startResolution,
            double stopResolution,
            String searchString,
            ArrayList<Integer> polygonTypes,
            double fromIncidence,
            double toIncidence,
            double fromEmission,
            double toEmission,
            double fromPhase,
            double toPhase,
            TreeSet<Integer> cubeList,
            ImageSource imageSource,
            int limbType)
    {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        File file = FileCache.getFileFromServer("/VESTA/FC/gaskell-all.txt", true);

        if (file != null)
        {
            try
            {
                ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
                for (String line : lines)
                {
                    String[] vals = line.trim().split("\\s+");
                    ArrayList<String> res = new ArrayList<String>();
                    res.add("/VESTA/FC/images/" + vals[0]);
                    String dateTime = DateTimeUtil.convertDateTimeFormat(vals[1]+" "+vals[2]+" "+vals[3]+" "+vals[4]);
                    res.add(String.valueOf(new DateTime(dateTime, DateTimeZone.UTC).getMillis()));
                    results.add(res);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return results;
    }

}
