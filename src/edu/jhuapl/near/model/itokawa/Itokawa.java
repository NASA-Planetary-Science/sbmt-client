package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.SmallBodyModel;

public class Itokawa extends SmallBodyModel
{
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

    public Itokawa()
    {
        super(modelNames, modelFiles, null, null, null, null, false, null, ColoringValueType.CELLDATA, false);
    }
}
