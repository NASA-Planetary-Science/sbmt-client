package edu.jhuapl.near.model.bennu;

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

    static private final String[] modelFiles = {
            "/GASKELL/RQ36_V3/ver64q.vtk",
            "/GASKELL/RQ36_V3/ver128q.vtk.gz",
            "/GASKELL/RQ36_V3/ver256q.vtk.gz",
            "/GASKELL/RQ36_V3/ver512q.vtk.gz"
    };

    static private final String[] coloringFiles = {
            "/GASKELL/RQ36_V3/Slope",
            "/GASKELL/RQ36_V3/Elevation",
            "/GASKELL/RQ36_V3/GravitationalAcceleration",
            "/GASKELL/RQ36_V3/GravitationalPotential"
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
                modelFiles,
                coloringFiles,
                coloringNames,
                coloringUnits,
                null,
                imageMap,
                ColoringValueType.CELLDATA,
                false);
    }


    @Override
    public double getDensity()
    {
        return 1.26; //Steve Chesley, "Orbit and bulk density of the OSIRIS-REx target Asteroid"
    }

    @Override
    public double getRotationRate()
    {
        double period = 4.297461; //hours, from bennu_v10.tpc
        return (2 * Math.PI)/(period * 60 * 60);
    }

    @Override
    public String getServerPathToShapeModelFileInPlateFormat()
    {
        return modelFilesInPlateFormat[getModelResolution()];
    }

}
