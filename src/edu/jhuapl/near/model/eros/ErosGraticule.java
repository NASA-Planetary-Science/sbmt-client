package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.SmallBodyModel;

public class ErosGraticule extends Graticule
{
    static private final String[] gridFiles = {
            "/EROS/coordinate_grid_res0.vtk.gz",
            "/EROS/coordinate_grid_res1.vtk.gz",
            "/EROS/coordinate_grid_res2.vtk.gz",
            "/EROS/coordinate_grid_res3.vtk.gz"
    };

    public ErosGraticule(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, gridFiles);
    }
}
