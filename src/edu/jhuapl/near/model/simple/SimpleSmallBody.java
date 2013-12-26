package edu.jhuapl.near.model.simple;

import java.io.File;

import edu.jhuapl.near.model.ModelFactory.ModelConfig;
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
            ModelConfig config,
            String[] modelNames,
            String[] paths,
            boolean hasColoringData)
    {
        super(config,
                config.name,
                config.author,
                modelNames,
                paths,
                hasColoringData ? getColoringFiles(paths[0]) : null,
                hasColoringData ? coloringNames : null,
                hasColoringData ? coloringUnits : null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false);
    }

    public SimpleSmallBody(ModelConfig config, String imageMap, boolean hasColoringData)
    {
        super(config,
                config.name,
                config.author,
                new String[] {config.name},
                new String[] {config.pathOnServer},
                hasColoringData ? getColoringFiles(config.pathOnServer) : null,
                hasColoringData ? coloringNames : null,
                hasColoringData ? coloringUnits : null,
                null,
                imageMap != null ? new String[] {imageMap} : null,
                ColoringValueType.CELLDATA,
                false);
    }
}
