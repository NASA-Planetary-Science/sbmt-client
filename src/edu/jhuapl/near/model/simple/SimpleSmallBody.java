package edu.jhuapl.near.model.simple;

import edu.jhuapl.near.model.SmallBodyModel;

public class SimpleSmallBody extends SmallBodyModel
{
    public SimpleSmallBody(String name, String path)
    {
        super(
                new String[] {name},
                new String[] {path},
                null,
                null,
                null,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false);
    }

    public SimpleSmallBody(String[] name, String[] path)
    {
        super(
                name,
                path,
                null,
                null,
                null,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false);
    }
}
