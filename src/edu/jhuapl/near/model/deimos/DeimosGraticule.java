package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.SmallBodyModel;

public class DeimosGraticule extends Graticule
{
    static private final String[] gridFiles = {
            "/DEIMOS/coordinate_grid_res0.vtk.gz"
    };

    public DeimosGraticule(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, gridFiles);
    }
}
