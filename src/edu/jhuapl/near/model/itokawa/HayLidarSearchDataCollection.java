package edu.jhuapl.near.model.itokawa;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class HayLidarSearchDataCollection extends LidarSearchDataCollection
{
    public HayLidarSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    public double getOffsetScale()
    {
        // The following value is the Itokawa diagonal length divided by 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is 0.025 km.
        return 0.00044228259621279913;
    }

    @Override
    public Map<String, String> getLidarDataSourceMap()
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("Optimized", "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        map.put("Unfiltered", "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        return map;
    }
}
