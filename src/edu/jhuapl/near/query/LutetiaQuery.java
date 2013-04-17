package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

public class LutetiaQuery extends QueryBase
{
    private static LutetiaQuery ref = null;

    public static LutetiaQuery getInstance()
    {
        if (ref == null)
            ref = new LutetiaQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private LutetiaQuery()
    {
    }

    @Override
    public ArrayList<ArrayList<String>> runQuery(
            String type,
            DateTime startDate,
            DateTime stopDate,
            ArrayList<Integer> filters,
            ArrayList<Boolean> userDefined,
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
        return getResultsFromFileListOnServer(
                "/GASKELL/LUTETIA/IMAGING/imagelist.txt",
                "/GASKELL/LUTETIA/IMAGING/images/");
    }

}
