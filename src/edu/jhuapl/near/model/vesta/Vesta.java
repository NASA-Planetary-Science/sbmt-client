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
            "/VESTA/VESTA_Slope",
            "/VESTA/VESTA_Elevation",
            "/VESTA/VESTA_GravitationalAcceleration",
            "/VESTA/VESTA_GravitationalPotential",
            "/VESTA/VESTA_439",
            "/VESTA/VESTA_673",
            "/VESTA/VESTA_953",
            "/VESTA/VESTA_1042"
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
