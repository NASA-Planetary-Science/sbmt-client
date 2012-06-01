package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

public class GaspraEverythingQuery extends QueryBase
{
    private static GaspraEverythingQuery ref = null;

    public static GaspraEverythingQuery getInstance()
    {
        if (ref == null)
            ref = new GaspraEverythingQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private GaspraEverythingQuery()
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
        return getResultsFromFileListOnServer(
                "/THOMAS/GASPRA/SSI/imagelist.txt",
                "/THOMAS/GASPRA/SSI/images/");
    }

}
