package edu.jhuapl.near.model.bennu;

import java.io.File;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;

public class Bennu extends SmallBodyModel
{
    static private final String[] modelNames = {
            "ver64q",
            "ver128q",
            "ver256q",
            "ver512q"
    };

    static private final String[] modelFilesInPlateFormat = null;

    static private final String[] imageMap = null;


    static private final String[] coloringNames = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public Bennu(SmallBodyConfig config)
    {
        super(config,
                modelNames,
                getModelFiles(config),
                getColoringFiles(config.rootDirOnServer),
                coloringNames,
                coloringUnits,
                null,
                imageMap,
                ColoringValueType.CELLDATA,
                false);
    }

    private static final String[] getModelFiles(SmallBodyConfig config)
    {
        String[] paths = {
                config.rootDirOnServer + "/ver64q.vtk.gz",
                config.rootDirOnServer + "/ver128q.vtk.gz",
                config.rootDirOnServer + "/ver256q.vtk.gz",
                config.rootDirOnServer + "/ver512q.vtk.gz"
        };
        return paths;
    };

    private static final String[] getColoringFiles(String path)
    {
        return new String[] {
                new File(path).getParent() + "/Slope",
                new File(path).getParent() + "/Elevation",
                new File(path).getParent() + "/GravitationalAcceleration",
                new File(path).getParent() + "/GravitationalPotential"
        };
    }

    @Override
    public double getDensity()
    {
        return getSmallBodyConfig().density;
    }

    @Override
    public double getRotationRate()
    {
        return getSmallBodyConfig().rotationRate;
    }

    @Override
    public String getServerPathToShapeModelFileInPlateFormat()
    {
        return modelFilesInPlateFormat[getModelResolution()];
    }

}
