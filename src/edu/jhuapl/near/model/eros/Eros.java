package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.SmallBodyModel;

public class Eros extends SmallBodyModel
{
    static private final String[] modelNames = {
            "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver64q",
            "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver128q",
            "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver256q",
            "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver512q"
    };

    static private final String[] modelFiles = {
            "/edu/jhuapl/near/data/Eros_ver64q.vtk",
            "/EROS/ver128q.vtk.gz",
            "/EROS/ver256q.vtk.gz",
            "/EROS/ver512q.vtk.gz"
    };

    static private final String[] coloringFiles = {
            "/EROS/Eros_Dec2006_0_Slope.txt.gz",
            "/EROS/Eros_Dec2006_0_Elevation.txt.gz",
            "/EROS/Eros_Dec2006_0_GravitationalAcceleration.txt.gz",
            "/EROS/Eros_Dec2006_0_GravitationalPotential.txt.gz"
    };

    static private final String[] coloringNames = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public Eros()
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
                true);
    }
}
