package edu.jhuapl.near.model.vesta;

import edu.jhuapl.near.model.SmallBodyModel;

public class Vesta extends SmallBodyModel
{
    static private final String[] modelNames = {
            "VESTA low",
            "VESTA med",
            "VESTA high",
            "VESTA very high"
    };

    static private final String[] modelFiles = {
            "/VESTA2/Vesta_res0.vtk.gz",
            "/VESTA2/Vesta_res1.vtk.gz",
            "/VESTA2/Vesta_res2.vtk.gz",
            "/VESTA2/Vesta_res3.vtk.gz"
    };

    public Vesta()
    {
        super(modelNames,
                modelFiles,
                null,
                null,
                null,
                null,
                false,
                null,
                ColoringValueType.CELLDATA,
                false);
    }
}
