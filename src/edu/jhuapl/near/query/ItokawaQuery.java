package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.PerspectiveImage.ImageSource;


/**
 * This class represents a database storing information about all the
 * data. It also provides functions for querying the database.
 *
 * @author kahneg1
 *
 */
public class ItokawaQuery extends QueryBase
{
    public enum Datatype {AMICA};

    private static ItokawaQuery ref = null;

    private void changeAmicaPathToFullPath(ArrayList<String> result)
    {
        result.set(0, "/ITOKAWA/AMICA/images/" + result.get(0));
    }

    public static ItokawaQuery getInstance()
    {
        if (ref == null)
            ref = new ItokawaQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private ItokawaQuery()
    {
    }


    /**
     * Run a query which searches for amica images between the specified dates.
     * Returns a list of URL's of the fit files that match.
     *
     * @param startDate
     * @param endDate
     */
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
            PerspectiveImage.ImageSource imageSource,
            int limbType)
    {
        if (imageSource == ImageSource.CORRECTED)
        {
            return getResultsFromFileListOnServer(
                    "/ITOKAWA/AMICA/sumfiles-corrected/imagelist.txt",
                    "/ITOKAWA/AMICA/images/");
        }

        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        double minIncidence = Math.min(fromIncidence, toIncidence);
        double maxIncidence = Math.max(fromIncidence, toIncidence);
        double minEmission = Math.min(fromEmission, toEmission);
        double maxEmission = Math.max(fromEmission, toEmission);
        double minPhase = Math.min(fromPhase, toPhase);
        double maxPhase = Math.max(fromPhase, toPhase);

        //if ("AMICA".equals(type))
        {
            if (searchString != null)
            {
                try
                {
                    long id = Long.parseLong(searchString);

                    HashMap<String, String> args = new HashMap<String, String>();
                    args.put("imageSource", imageSource.toString());
                    args.put("id", String.valueOf(id));

                    results = doQuery("searchamica_id.php", constructUrlArguments(args));
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }

                if (results != null && results.size() > 0)
                {
                    this.changeAmicaPathToFullPath(results.get(0));
                }
                return results;
            }

            if (filters.isEmpty())
                return results;

            try
            {
                double minScDistance = Math.min(startDistance, stopDistance);
                double maxScDistance = Math.max(startDistance, stopDistance);
                double minResolution = Math.min(startResolution, stopResolution) / 1000.0;
                double maxResolution = Math.max(startResolution, stopResolution) / 1000.0;

                HashMap<String, String> args = new HashMap<String, String>();
                args.put("imageSource", imageSource.toString());
                args.put("minResolution", String.valueOf(minResolution));
                args.put("maxResolution", String.valueOf(maxResolution));
                args.put("minScDistance", String.valueOf(minScDistance));
                args.put("maxScDistance", String.valueOf(maxScDistance));
                args.put("startDate", String.valueOf(startDate.getMillis()));
                args.put("stopDate", String.valueOf(stopDate.getMillis()));
                args.put("minIncidence", String.valueOf(minIncidence));
                args.put("maxIncidence", String.valueOf(maxIncidence));
                args.put("minEmission", String.valueOf(minEmission));
                args.put("maxEmission", String.valueOf(maxEmission));
                args.put("minPhase", String.valueOf(minPhase));
                args.put("maxPhase", String.valueOf(maxPhase));
                args.put("limbType", String.valueOf(limbType));
                for (int i=1; i<=7; ++i)
                {
                    if (filters.contains(i))
                        args.put("filterType"+i, "1");
                    else
                        args.put("filterType"+i, "0");
                }
                if (cubeList != null && cubeList.size() > 0)
                {
                    String cubes = "";
                    int size = cubeList.size();
                    int count = 0;
                    for (Integer i : cubeList)
                    {
                        cubes += "" + i;
                        if (count < size-1)
                            cubes += ",";
                        ++count;
                    }
                    args.put("cubes", cubes);
                }

                results = doQuery("searchamica.php", constructUrlArguments(args));

                for (ArrayList<String> res : results)
                {
                    this.changeAmicaPathToFullPath(res);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return results;
    }


}
