package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

public class MathildeQuery extends QueryBase
{
    private static MathildeQuery ref = null;

    public static MathildeQuery getInstance()
    {
        if (ref == null)
            ref = new MathildeQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private MathildeQuery()
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
                "/THOMAS/MATHILDE/MSI/imagelist.txt",
                "/THOMAS/MATHILDE/MSI/images/");
    }

}
