package edu.jhuapl.near.model.phobos;

import edu.jhuapl.near.model.SmallBodyModel;

public class Phobos extends SmallBodyModel
{
    public static final String NAME = "Phobos";
    public static final String CATEGORY = "Gaskell";

    static private final String[] modelNames = {
        "PHOBOS low",
        "PHOBOS med",
        "PHOBOS high",
        "PHOBOS very high"
    };

    static private final String[] modelFiles = {
        "/GASKELL/PHOBOS/ver64q.vtk.gz",
        "/GASKELL/PHOBOS/ver128q.vtk.gz",
        "/GASKELL/PHOBOS/ver256q.vtk.gz",
        "/GASKELL/PHOBOS/ver512q.vtk.gz"
    };

    static private final String[] coloringFiles = {
        "/GASKELL/PHOBOS/Phobos_Slope",
        "/GASKELL/PHOBOS/Phobos_Elevation",
        "/GASKELL/PHOBOS/Phobos_GravitationalAcceleration",
        "/GASKELL/PHOBOS/Phobos_GravitationalPotential"
    };

    static private final String[] coloringNames = {
        SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
        SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public Phobos()
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
}
