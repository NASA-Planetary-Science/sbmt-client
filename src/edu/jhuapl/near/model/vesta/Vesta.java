package edu.jhuapl.near.model.vesta;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;

public class Vesta extends SmallBodyModel
{
    static private final String[] modelNames = {
        "VESTA low",
        "VESTA med",
        "VESTA high",
        "VESTA very high"
    };

    static private final String[] modelFilesPublic = {
        "/VESTA_PUBLIC/Vesta_res0.vtk.gz",
        "/VESTA_PUBLIC/Vesta_res1.vtk.gz",
        "/VESTA_PUBLIC/Vesta_res2.vtk.gz",
        "/VESTA_PUBLIC/Vesta_res3.vtk.gz"
    };

    static private final String[] modelFilesAplOnly = {
        "/VESTA/Vesta_res0.vtk.gz",
        "/VESTA/Vesta_res1.vtk.gz",
        "/VESTA/Vesta_res2.vtk.gz",
        "/VESTA/Vesta_res3.vtk.gz"
    };

    static private final String[] coloringFiles = {
        "/VESTA/Vesta_Slope",
        "/VESTA/Vesta_Elevation",
        "/VESTA/Vesta_GravitationalAcceleration",
        "/VESTA/Vesta_GravitationalPotential"
    };

    static private final String[] coloringNames = {
        SlopeStr, ElevStr, GravAccStr, GravPotStr
    };

    static private final String[] coloringUnits = {
        SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };

    public Vesta()
    {
        super(modelNames,
                Configuration.isAPLVersion() ? modelFilesAplOnly : modelFilesPublic,
                Configuration.isAPLVersion() ? coloringFiles : null,
                Configuration.isAPLVersion() ? coloringNames : null,
                Configuration.isAPLVersion() ? coloringUnits : null,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false,
                Configuration.isAPLVersion());
    }
}
