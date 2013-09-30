package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

public class PhobosExperimentalQuery extends QueryBase
{
    private static PhobosExperimentalQuery ref = null;

    private void changePathToFullPath(ArrayList<String> result)
    {
        result.set(0, "/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/images/" + result.get(0));
    }

    public static PhobosExperimentalQuery getInstance()
    {
        if (ref == null)
            ref = new PhobosExperimentalQuery();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private PhobosExperimentalQuery()
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
        if (imageSource == ImageSource.CORRECTED)
        {
            return getResultsFromFileListOnServer(
                    "/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/sumfiles-corrected/imagelist.txt",
                    "/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/images/", true);
        }

        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        double minIncidence = Math.min(fromIncidence, toIncidence);
        double maxIncidence = Math.max(fromIncidence, toIncidence);
        double minEmission = Math.min(fromEmission, toEmission);
        double maxEmission = Math.max(fromEmission, toEmission);
        double minPhase = Math.min(fromPhase, toPhase);
        double maxPhase = Math.max(fromPhase, toPhase);

        //if ("".equals(type))
        {
            if (searchString != null)
            {
                try
                {
                    // Note that the image name for viking images has an A or B
                    // in the name, but in the database it is stored as an integer with the
                    // A replaced with 1 and B replaced with 2.
                    searchString = searchString.toUpperCase().replace("A", "1");
                    searchString = searchString.toUpperCase().replace("B", "2");

                    long id = Long.parseLong(searchString);

                    HashMap<String, String> args = new HashMap<String, String>();
                    args.put("imageSource", imageSource.toString());
                    args.put("id", String.valueOf(id));

                    results = doQuery("searchphobosexpimages_id.php", constructUrlArguments(args));
                }
                catch (NumberFormatException e)
                {

                }

                if (results != null && results.size() > 0)
                {
                    this.changePathToFullPath(results.get(0));
                }
                return results;
            }

            boolean phobos2 = userDefined.get(0);
            boolean vikingOrbiter1A = userDefined.get(1);
            boolean vikingOrbiter1B = userDefined.get(2);
            boolean vikingOrbiter2A = userDefined.get(3);
            boolean vikingOrbiter2B = userDefined.get(4);
            boolean mexHrsc = userDefined.get(5);

            // TODO the following is confusing
            if ((filters.isEmpty() && !mexHrsc) || (phobos2 == false &&
                    vikingOrbiter1A == false && vikingOrbiter1B == false &&
                    vikingOrbiter2A == false && vikingOrbiter2B == false &&
                    mexHrsc == false))
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
                args.put("phobos2", phobos2==true ? "1" : "0");
                args.put("vikingOrbiter1A", vikingOrbiter1A==true ? "1" : "0");
                args.put("vikingOrbiter1B", vikingOrbiter1B==true ? "1" : "0");
                args.put("vikingOrbiter2A", vikingOrbiter2A==true ? "1" : "0");
                args.put("vikingOrbiter2B", vikingOrbiter2B==true ? "1" : "0");
                args.put("mexHrsc", mexHrsc==true ? "1" : "0");
                for (int i=1; i<=9; ++i)
                {
                    if (filters.contains(i))
                        args.put("filterType"+i, "1");
                    else
                        args.put("filterType"+i, "0");
                }
                if (mexHrsc)
                    args.put("filterType10", "1");
                else
                    args.put("filterType10", "0");
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

                results = doQuery("searchphobosexpimages.php", constructUrlArguments(args));

                for (ArrayList<String> res : results)
                {
                    this.changePathToFullPath(res);
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
