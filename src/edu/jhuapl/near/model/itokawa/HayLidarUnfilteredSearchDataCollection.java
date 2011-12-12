package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.SmallBodyModel;

public class HayLidarUnfilteredSearchDataCollection extends HayLidarSearchDataCollection
{
    public HayLidarUnfilteredSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    public String getDatabasePath()
    {
        return "/ITOKAWA/LIDAR/lidar-unfiltered.h2.db.gz";
    }
}
