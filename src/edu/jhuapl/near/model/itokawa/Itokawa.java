package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.SmallBodyModel;

public class Itokawa extends SmallBodyModel
{
    public static final String NAME = "Itokawa";
    public static final String CATEGORY = "Gaskell";

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
        "/ITOKAWA/Itokawa_Slope",
        "/ITOKAWA/Itokawa_Elevation",
        "/ITOKAWA/Itokawa_GravitationalAcceleration",
        "/ITOKAWA/Itokawa_GravitationalPotential"
    };

    static private final String[] coloringNames = {
        SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
        SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };


    public Itokawa()
    {
        super(NAME,
                CATEGORY,
                modelNames,
                modelFiles,
                coloringFiles,
                coloringNames,
                coloringUnits,
                null,
                null,
                ColoringValueType.CELLDATA,
                false);
    }

    protected String getGravityVectorFilePath(int resolutionLevel)
    {
        if (resolutionLevel <= 1)
            return "/ITOKAWA/Itokawa_GravityVector" + "_res" + resolutionLevel + ".txt.gz";
        else
            return null;
    }

    @Override
    public double getDensity()
    {
        return 1.95;
    }

    @Override
    public double getRotationRate()
    {
        return 0.000143857148947075;
    }

    @Override
    public double getReferencePotential()
    {
        return -1.3692877423961147e-02;
    }

    @Override
    public String getServerPathToShapeModelFileInPlateFormat()
    {
        return "/ITOKAWA/ver64q.tab.gz";
    }
}
