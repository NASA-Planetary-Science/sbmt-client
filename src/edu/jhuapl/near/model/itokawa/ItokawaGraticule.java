package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.Graticule;

public class ItokawaGraticule extends Graticule
{
	private static String[] gridFiles = {
		"/ITOKAWA/coordinate_grid_res0.vtk.gz",
		"/ITOKAWA/coordinate_grid_res1.vtk.gz",
		"/ITOKAWA/coordinate_grid_res2.vtk.gz",
		"/ITOKAWA/coordinate_grid_res3.vtk.gz"};
	
	public ItokawaGraticule(ItokawaModel erosModel)
	{
		super(erosModel, gridFiles);
		setShiftAmount(0.0005);
	}

}
