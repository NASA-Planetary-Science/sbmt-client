package edu.jhuapl.near.query;

import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageSource;

/**
 * A query which simply returns a fixed list of images. No actual search is done.
 * Useful for getting a quick search working without having to update the database.
 */
public class FixedListQuery extends QueryBase
{
    private String rootPath;

    public FixedListQuery(String rootPath)
    {
        this.rootPath = rootPath;
    }

    @Override
    public String getImagesPath()
    {
        return rootPath + "/images";
    }

    @Override
    public ArrayList<ArrayList<String>> runQuery(
            String type,
            DateTime startDate,
            DateTime stopDate,
            ArrayList<Boolean> filtersChecked,
            ArrayList<Boolean> camerasChecked,
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
        String imageListPrefix = "";

        if (imageSource == ImageSource.CORRECTED)
            imageListPrefix = "sumfiles-corrected";
        else if (imageSource == ImageSource.ADJUSTED)
            imageListPrefix = "infofiles-adjusted";

        ArrayList<ArrayList<String>> result = getResultsFromFileListOnServer(rootPath + "/" + imageListPrefix + "/imagelist.txt", rootPath + "/images/");

        return result;
    }

}
