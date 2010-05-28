package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.Graticule;

public class ErosGraticule extends Graticule
{
	private static String[] gridFiles = {
		"/EROS/coordinate_grid_res0.vtk.gz",
		"/EROS/coordinate_grid_res1.vtk.gz",
		"/EROS/coordinate_grid_res2.vtk.gz",
		"/EROS/coordinate_grid_res3.vtk.gz"};
	
	public ErosGraticule(ErosModel erosModel)
	{
		super(erosModel, gridFiles);
	}

}
