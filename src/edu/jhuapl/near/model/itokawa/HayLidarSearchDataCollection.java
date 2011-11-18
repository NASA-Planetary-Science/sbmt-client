package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class HayLidarSearchDataCollection extends LidarSearchDataCollection
{
    private static final String dbPath = "/ITOKAWA/LIDAR/lidar.h2.db.gz";

    public HayLidarSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, dbPath);
    }

    @Override
    public double getOffsetScale()
    {
        // The following value is the Itokawa diagonal length divided by 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is 0.025 km.
        return 0.00044228259621279913;
    }

}
