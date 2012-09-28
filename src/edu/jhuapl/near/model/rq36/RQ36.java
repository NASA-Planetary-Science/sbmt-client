package edu.jhuapl.near.model.rq36;

import edu.jhuapl.near.model.SmallBodyModel;

public class RQ36 extends SmallBodyModel
{
    static private final String[] modelNames = {
        "RQ36 low",
        "RQ36 med",
        "RQ36 high",
        "RQ36 very high"
    };

    static private final String[] modelFilesAplOnly = {
        "/RQ36/RQ36_res0.vtk.gz",
        "/RQ36/RQ36_res1.vtk.gz",
        "/RQ36/RQ36_res2.vtk.gz",
        "/RQ36/RQ36_res3.vtk.gz"
    };

    static private final String[] coloringFiles = {
        "/RQ36/RQ36_Slope",
        "/RQ36/RQ36_Elevation",
        "/RQ36/RQ36_GravitationalAcceleration",
        "/RQ36/RQ36_GravitationalPotential"
    };

    static private final String[] coloringNames = {
        SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
        SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public RQ36()
    {
        super(modelNames,
                modelFilesAplOnly,
                coloringFiles,
                coloringNames,
                coloringUnits,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false,
                true);
    }
}
