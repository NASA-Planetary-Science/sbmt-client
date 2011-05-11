package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.SmallBodyModel;

public class ItokawaGraticule extends Graticule
{
    static private final String[] gridFiles = {
            "/ITOKAWA/coordinate_grid_res0.vtk.gz",
            "/ITOKAWA/coordinate_grid_res1.vtk.gz",
            "/ITOKAWA/coordinate_grid_res2.vtk.gz",
            "/ITOKAWA/coordinate_grid_res3.vtk.gz"
    };

    public ItokawaGraticule(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, gridFiles);
    }
}
