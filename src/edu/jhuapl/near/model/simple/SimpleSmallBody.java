package edu.jhuapl.near.model.simple;

import java.io.File;

import edu.jhuapl.near.model.SmallBodyModel;

public class SimpleSmallBody extends SmallBodyModel
{
    static private String[] getColoringFiles(String path)
    {
        return new String[] {
                new File(path).getParent() + "/Slope",
                new File(path).getParent() + "/Elevation",
                new File(path).getParent() + "/GravitationalAcceleration",
                new File(path).getParent() + "/GravitationalPotential"
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
                hasColoringData ? getColoringFiles(paths[0]) : null,
                hasColoringData ? coloringNames : null,
                hasColoringData ? coloringUnits : null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false,
                useAPLServer);
    }

    public SimpleSmallBody(String name, String author, String path, String imageMap, boolean hasColoringData)
    {
        super(name,
                author,
                new String[] {name},
                new String[] {path},
                hasColoringData ? getColoringFiles(path) : null,
                hasColoringData ? coloringNames : null,
                hasColoringData ? coloringUnits : null,
                null,
                imageMap != null ? new String[] {imageMap} : null,
                ColoringValueType.CELLDATA,
                false);
    }
}
