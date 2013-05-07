package edu.jhuapl.near.model.simple;

import edu.jhuapl.near.model.SmallBodyModel;

public class SimpleSmallBody extends SmallBodyModel
{
    static private String[] getColoringFiles(String name)
    {
        return new String[] {
        "/GASKELL/" + name.toUpperCase().replace(" ", "") + "/Slope",
        "/GASKELL/" + name.toUpperCase().replace(" ", "") + "/Elevation",
        "/GASKELL/" + name.toUpperCase().replace(" ", "") + "/GravitationalAcceleration",
        "/GASKELL/" + name.toUpperCase().replace(" ", "") + "/GravitationalPotential"
        };
    }

    static private final String[] coloringNames = {
        SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
        SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public SimpleSmallBody(
            String name,
            String author,
            String[] modelNames,
            String[] paths,
            boolean hasColoringData,
            boolean useAPLServer)
    {
        super(name,
                author,
                modelNames,
                paths,
                hasColoringData ? getColoringFiles(name) : null,
                hasColoringData ? coloringNames : null,
                hasColoringData ? coloringUnits : null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false,
                useAPLServer);
    }

    public SimpleSmallBody(String name, String author, String path, String imageMap)
    {
        super(name,
                author,
                new String[] {name},
                new String[] {path},
                null,
                null,
                null,
                null,
                imageMap != null ? new String[] {imageMap} : null,
                ColoringValueType.CELLDATA,
                false);
    }
}
