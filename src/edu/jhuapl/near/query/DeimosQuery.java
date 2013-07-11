package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

public class DeimosQuery extends QueryBase
{
    private static DeimosQuery ref = null;

    public static DeimosQuery getInstance()
    {
        if (ref == null)
            ref = new DeimosQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private DeimosQuery()
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
        if (imageSource == ImageSource.PDS)
        {
            return getResultsFromFileListOnServer(
                    "/THOMAS/DEIMOS/IMAGING/imagelist.txt",
                    "/THOMAS/DEIMOS/IMAGING/images/");
        }

        if (imageSource == ImageSource.CORRECTED)
        {
            return getResultsFromFileListOnServer(
                    "/THOMAS/DEIMOS/IMAGING/sumfiles-corrected/imagelist.txt",
                    "/THOMAS/DEIMOS/IMAGING/images/");
        }

        return null;
    }

}
