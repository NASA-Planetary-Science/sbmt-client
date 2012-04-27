package edu.jhuapl.near.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;

public class MathildeEverythingQuery extends QueryBase
{
    private static MathildeEverythingQuery ref = null;

    public static MathildeEverythingQuery getInstance()
    {
        if (ref == null)
            ref = new MathildeEverythingQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private MathildeEverythingQuery()
    {
    }

    @Override
    public ArrayList<ArrayList<String>> runQuery(
            String type,
            DateTime startDate,
            DateTime stopDate,
            ArrayList<Integer> filters,
            boolean unused1,
            boolean unused2,
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

        File file = FileCache.getFileFromServer("/THOMAS/MATHILDE/MSI/imagelist.txt", true);

        if (file != null)
        {
            try
            {
                ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
                for (String line : lines)
                {
                    String[] vals = line.trim().split("\\s+");
                    ArrayList<String> res = new ArrayList<String>();
                    res.add("/THOMAS/MATHILDE/MSI/images/" + vals[0]);
                    res.add(vals[1]);
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
