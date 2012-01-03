package edu.jhuapl.near.model.vesta_old;

import edu.jhuapl.near.model.SmallBodyModel;

public class VestaOld extends SmallBodyModel
{
    static private final String[] modelNames = {
        "VESTA-old"
    };

    static private final String[] modelFiles = {
        "/VESTA_OLD/VESTA.vtk.gz"
    };

    static private final String[] coloringFiles = {
        "/VESTA_OLD/VESTA_Slope",
        "/VESTA_OLD/VESTA_Elevation",
        "/VESTA_OLD/VESTA_GravitationalAcceleration",
        "/VESTA_OLD/VESTA_GravitationalPotential",
        "/VESTA_OLD/VESTA_439",
        "/VESTA_OLD/VESTA_673",
        "/VESTA_OLD/VESTA_953",
        "/VESTA_OLD/VESTA_1042"
    };

    static private final String[] coloringNames = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr, "439 nm", "673 nm", "953 nm", "1042 nm"
    };

    static private final String[] coloringUnits = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr, "", "", "", ""
    };

    static private final boolean[] coloringHasNulls = {
            false, false, false, false, true, true, true, true
    };

    public VestaOld()
    {
        super(modelNames,
                modelFiles,
                coloringFiles,
                coloringNames,
                coloringUnits,
                coloringHasNulls,
                true,
                null,
                ColoringValueType.CELLDATA,
                false);
    }
}
