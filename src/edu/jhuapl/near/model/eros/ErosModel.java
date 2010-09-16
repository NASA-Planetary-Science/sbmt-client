package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

public class ErosModel extends SmallBodyModel
{
	private static String[] modelNames = {
		"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver64q",
		"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver128q",
		"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver256q",
		"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver512q"};

	private static String[] modelFiles = {
		"/edu/jhuapl/near/data/Eros_ver64q.vtk",
		"/EROS/ver128q.vtk.gz",
		"/EROS/ver256q.vtk.gz",
		"/EROS/ver512q.vtk.gz"};

	private static String[] coloringFiles = {
		"/EROS/Eros_Dec2006_0_Elevation.txt.gz",
		"/EROS/Eros_Dec2006_0_GravitationalAcceleration.txt.gz",
		"/EROS/Eros_Dec2006_0_GravitationalPotential.txt.gz",
		"/EROS/Eros_Dec2006_0_Slope.txt.gz"};

	public ErosModel()
	{
		super(modelNames, modelFiles, coloringFiles, null, true, ModelNames.EROS);
	}

	public int getNumberResolutionLevels()
	{
		return modelFiles.length;
	}
}
