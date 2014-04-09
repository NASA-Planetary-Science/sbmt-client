package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

public class GenericPhpQuery extends QueryBase
{
    private String imagesPath;
    private String tablePrefix;

    private void changePathToFullPath(ArrayList<String> result)
    {
        result.set(0, imagesPath + "/" + result.get(0));
    }

    public GenericPhpQuery(String imagesPath, String tablePrefix)
    {
        this.imagesPath = imagesPath;
        this.tablePrefix = tablePrefix.toLowerCase();
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
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        double minIncidence = Math.min(fromIncidence, toIncidence);
        double maxIncidence = Math.max(fromIncidence, toIncidence);
        double minEmission = Math.min(fromEmission, toEmission);
        double maxEmission = Math.max(fromEmission, toEmission);
        double minPhase = Math.min(fromPhase, toPhase);
        double maxPhase = Math.max(fromPhase, toPhase);

        String imagesDatabase = "";
        String cubesDatabase = "";
        if (imageSource == ImageSource.GASKELL)
        {
            imagesDatabase = tablePrefix + "images_gaskell";
            cubesDatabase = tablePrefix + "cubes_gaskell";
        }
        else
        {
            imagesDatabase = tablePrefix + "images_pds";
            cubesDatabase = tablePrefix + "cubes_pds";
        }

        if (searchString != null)
        {
            if (searchString.isEmpty())
                return results;

            HashMap<String, String> args = new HashMap<String, String>();
            args.put("imagesDatabase", imagesDatabase);
            args.put("imageSource", imageSource.toString());
            args.put("searchString", searchString);

            results = doQuery("searchimages.php", constructUrlArguments(args));

            if (results != null && results.size() > 0)
            {
                for (ArrayList<String> res : results)
                {
                    this.changePathToFullPath(res);
                }
            }
            return results;
        }

        ArrayList<Integer> cameras = new ArrayList<Integer>();
        for (int i=0; i<userDefined.size(); ++i)
        {
            if (userDefined.get(i))
                cameras.add(i);
        }

        try
        {
            double minScDistance = Math.min(startDistance, stopDistance);
            double maxScDistance = Math.max(startDistance, stopDistance);
            double minResolution = Math.min(startResolution, stopResolution) / 1000.0;
            double maxResolution = Math.max(startResolution, stopResolution) / 1000.0;

            HashMap<String, String> args = new HashMap<String, String>();
            args.put("imagesDatabase", imagesDatabase);
            args.put("cubesDatabase", cubesDatabase);
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
            for (int i=1; i<=10; ++i)
            {
                if (filters.contains(i))
                    args.put("filterType"+i, "1");
                else
                    args.put("filterType"+i, "0");
            }
            for (int i=1; i<=10; ++i)
            {
                if (cameras.contains(i))
                    args.put("cameraType"+i, "1");
                else
                    args.put("cameraType"+i, "0");
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

            results = doQuery("searchimages.php", constructUrlArguments(args));

            for (ArrayList<String> res : results)
            {
                this.changePathToFullPath(res);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return results;
    }

}
