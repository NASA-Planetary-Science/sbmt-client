package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.Graticule;

public class DeimosGraticule extends Graticule
{
	private static String[] gridFiles = {
		"/DEIMOS/coordinate_grid_res0.vtk.gz"};
	
	public DeimosGraticule(DeimosModel erosModel)
	{
		super(erosModel, gridFiles);
	}

}
