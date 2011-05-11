package edu.jhuapl.near.model.vesta;

import edu.jhuapl.near.model.SmallBodyModel;

public class Vesta extends SmallBodyModel
{
    static private final String[] modelNames = {
            "VESTA"
    };

    static private final String[] modelFiles = {
            "/VESTA/VESTA.vtk.gz"
    };

    static private final String[] coloringFiles = {
            "/VESTA/VESTA_Slope.txt.gz",
            "/VESTA/VESTA_Elevation.txt.gz",
            "/VESTA/VESTA_GravitationalAcceleration.txt.gz",
            "/VESTA/VESTA_GravitationalPotential.txt.gz",
            "/VESTA/VESTA_439.txt.gz",
            "/VESTA/VESTA_673.txt.gz",
            "/VESTA/VESTA_953.txt.gz",
            "/VESTA/VESTA_1042.txt.gz"
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

    public Vesta()
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
