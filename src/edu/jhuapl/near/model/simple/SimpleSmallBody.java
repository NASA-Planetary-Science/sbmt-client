package edu.jhuapl.near.model.simple;

import edu.jhuapl.near.model.SmallBodyModel;

public class SimpleSmallBody extends SmallBodyModel
{
    public SimpleSmallBody(String name, String category, String[] modelNames, String[] paths, boolean useAPLServer)
    {
        super(name,
                category,
                modelNames,
                paths,
                null,
                null,
                null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false,
                useAPLServer);
    }

    public SimpleSmallBody(String name, String category, String path, String imageMap)
    {
        super(name,
                category,
                new String[] {name},
                new String[] {path},
                null,
                null,
                null,
                null,
                new String[] {imageMap},
                ColoringValueType.CELLDATA,
                false);
    }
}
