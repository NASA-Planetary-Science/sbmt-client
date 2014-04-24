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
            String[] paths)
    {
        super(config,
                modelNames,
                paths,
                config.hasColoringData ? getColoringFiles(paths[0]) : null,
                config.hasColoringData ? coloringNames : null,
                config.hasColoringData ? coloringUnits : null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false);
    }

    public SimpleSmallBody(ModelConfig config, String imageMap)
    {
        super(config,
                new String[] {config.body.toString()},
                new String[] {config.pathOnServer},
                config.hasColoringData ? getColoringFiles(config.pathOnServer) : null,
                config.hasColoringData ? coloringNames : null,
                config.hasColoringData ? coloringUnits : null,
                null,
                imageMap != null ? new String[] {imageMap} : null,
                ColoringValueType.CELLDATA,
                false);
    }
}
