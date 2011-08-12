package edu.jhuapl.near.model.vesta;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.SmallBodyModel;

public class VestaGraticule extends Graticule
{
    static private final String[] gridFiles = {
            "/VESTA2/coordinate_grid_res0.vtk.gz",
            "/VESTA2/coordinate_grid_res1.vtk.gz",
            "/VESTA2/coordinate_grid_res2.vtk.gz",
            "/VESTA2/coordinate_grid_res3.vtk.gz"
    };

    public VestaGraticule(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, gridFiles);
    }
}
