package edu.jhuapl.near.model.vesta_old;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.SmallBodyModel;

public class VestaOldGraticule extends Graticule
{
    static private final String[] gridFiles = {
        "/VESTA_OLD/coordinate_grid_res0.vtk.gz"
    };

    public VestaOldGraticule(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, gridFiles);
    }
}
