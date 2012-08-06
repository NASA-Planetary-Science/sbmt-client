package edu.jhuapl.near.model.vesta;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;

public class VestaGraticule extends Graticule
{
    static private final String[] gridFilesPublic = {
        "/VESTA_PUBLIC/coordinate_grid_res0.vtk.gz",
        "/VESTA_PUBLIC/coordinate_grid_res1.vtk.gz",
        "/VESTA_PUBLIC/coordinate_grid_res2.vtk.gz",
        "/VESTA_PUBLIC/coordinate_grid_res3.vtk.gz"
    };

    static private final String[] gridFilesAplOnly = {
        "/VESTA/coordinate_grid_res0.vtk.gz",
        "/VESTA/coordinate_grid_res1.vtk.gz",
        "/VESTA/coordinate_grid_res2.vtk.gz",
        "/VESTA/coordinate_grid_res3.vtk.gz"
    };

    public VestaGraticule(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel,
                Configuration.isAPLVersion() ? gridFilesAplOnly : gridFilesPublic,
                Configuration.isAPLVersion());
    }
}
