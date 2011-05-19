package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.SmallBodyModel;

public class Itokawa extends SmallBodyModel
{
    static private final String[] modelNames = {
            "HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver64q",
            "HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver128q",
            "HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver256q",
            "HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver512q"
    };

    static private final String[] modelFiles = {
            "/ITOKAWA/ver64q.vtk.gz",
            "/ITOKAWA/ver128q.vtk.gz",
            "/ITOKAWA/ver256q.vtk.gz",
            "/ITOKAWA/ver512q.vtk.gz"
    };

    static private final String[] coloringFiles = {
        "/ITOKAWA/Itokawa_Slope.txt.gz",
        "/ITOKAWA/Itokawa_Elevation.txt.gz",
        "/ITOKAWA/Itokawa_GravitationalAcceleration.txt.gz",
        "/ITOKAWA/Itokawa_GravitationalPotential.txt.gz"
    };

    static private final String[] coloringNames = {
        SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
        SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };


    public Itokawa()
    {
        super(modelNames,
                modelFiles,
                coloringFiles,
                coloringNames,
                coloringUnits,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false);
    }
}
