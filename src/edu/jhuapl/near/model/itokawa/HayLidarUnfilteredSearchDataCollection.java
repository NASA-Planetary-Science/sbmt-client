package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.SmallBodyModel;

public class HayLidarUnfilteredSearchDataCollection extends HayLidarSearchDataCollection
{
    public HayLidarUnfilteredSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    protected String getCubeFolderPath()
    {
        return "/ITOKAWA/LIDAR/cdr/cubes-unfiltered";
    }

}
