package edu.jhuapl.near.model.eros;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class NLRSearchDataCollection extends LidarSearchDataCollection
{
    public NLRSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    public double getOffsetScale()
    {
        return 0.025;
    }

    @Override
    public Map<String, String> getLidarDataSourceMap()
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("Default", "/NLR/cubes");
        return map;
    }
}
