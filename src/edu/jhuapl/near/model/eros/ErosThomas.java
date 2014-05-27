package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;

public class ErosThomas extends SmallBodyModel
{
    static private final String[] modelNames = {
            "433 EROS PLATE MODEL MSI 1708",
            "433 EROS PLATE MODEL MSI 7790",
            "433 EROS PLATE MODEL MSI 10152",
            "433 EROS PLATE MODEL MSI 22540",
            "433 EROS PLATE MODEL MSI 89398",
            "433 EROS PLATE MODEL MSI 200700"
    };

    static private final String[] modelFiles = {
            "/THOMAS/EROS/eros001708.obj.gz",
            "/THOMAS/EROS/eros007790.obj.gz",
            "/THOMAS/EROS/eros010152.obj.gz",
            "/THOMAS/EROS/eros022540.obj.gz",
            "/THOMAS/EROS/eros089398.obj.gz",
            "/THOMAS/EROS/eros200700.obj.gz"
    };

    static private final String[] coloringFiles = {
            "/THOMAS/EROS/Slope",
            "/THOMAS/EROS/Elevation",
            "/THOMAS/EROS/GravitationalAcceleration",
            "/THOMAS/EROS/GravitationalPotential"
    };

    static private final String[] coloringNames = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public ErosThomas(SmallBodyConfig config)
    {
        super(config,
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
