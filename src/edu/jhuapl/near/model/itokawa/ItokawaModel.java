package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.SmallBodyModel;

public class ItokawaModel extends SmallBodyModel
{
	private static String[] modelNames = {
		"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver64q",
		"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver128q",
		"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver256q",
		"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver512q"};

	private static String[] modelFiles = {
		"/ITOKAWA/ver64q.vtk.gz",
		"/ITOKAWA/ver128q.vtk.gz",
		"/ITOKAWA/ver256q.vtk.gz",
		"/ITOKAWA/ver512q.vtk.gz"};

	private static String[] coloringFiles = null;

	public ItokawaModel()
	{
		super(modelNames, modelFiles, coloringFiles, false);
	}

	public int getNumberResolutionLevels()
	{
		return modelFiles.length;
	}
}
