package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.SmallBodyModel;

public class DeimosModel extends SmallBodyModel
{
	private static String[] modelNames = {
		"NEAR-A-MSI-5-EROSSHAPE-V1.0 ver64q"};

	private static String[] modelFiles = {
		"/edu/jhuapl/near/data/ver64q.vtk"};

	private static String[] coloringFiles = {
		"/edu/jhuapl/near/data/Eros_Dec2006_0_Elevation.txt",
		"/edu/jhuapl/near/data/Eros_Dec2006_0_GravitationalAcceleration.txt",
		"/edu/jhuapl/near/data/Eros_Dec2006_0_GravitationalPotential.txt",
		"/edu/jhuapl/near/data/Eros_Dec2006_0_Slope.txt"};

	public DeimosModel()
	{
		super(modelNames, modelFiles, coloringFiles);
	}
}
