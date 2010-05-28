package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.Graticule;

public class DeimosGraticule extends Graticule
{
	private static String[] gridFiles = {
		"/edu/jhuapl/near/data/coordinate_grid_res0.vtk",
		"/EROS/coordinate_grid_res1.vtk.gz",
		"/EROS/coordinate_grid_res2.vtk.gz",
		"/EROS/coordinate_grid_res3.vtk.gz"};
	
	public DeimosGraticule(DeimosModel erosModel)
	{
		super(erosModel, gridFiles);
	}

}
