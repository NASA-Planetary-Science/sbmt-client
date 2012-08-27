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
            "/EROS/Eros_Slope",
            "/EROS/Eros_Elevation",
            "/EROS/Eros_GravitationalAcceleration",
            "/EROS/Eros_GravitationalPotential"
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


    @Override
    public double getDensity()
    {
        return 2.67;
    }

    @Override
    public double getRotationRate()
    {
        return 0.00033116576167064;
    }

    @Override
    public double getReferencePotential()
    {
        return -5.3754370226447428e+01;
    }

    @Override
    public String getServerPathToShapeModelFileInPlateFormat()
    {
        return "/EROS/ver64q.tab.gz";
    }
}
