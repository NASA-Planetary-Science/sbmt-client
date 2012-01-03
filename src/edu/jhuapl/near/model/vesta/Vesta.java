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

    public Vesta()
    {
        super(modelNames,
                Configuration.isAPLVersion() ? modelFilesAplOnly : modelFilesPublic,
                null,
                null,
                null,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false,
                Configuration.isAPLVersion());
    }
}
