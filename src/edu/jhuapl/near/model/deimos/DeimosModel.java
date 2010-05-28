package edu.jhuapl.near.model.deimos;

import edu.jhuapl.near.model.SmallBodyModel;

public class DeimosModel extends SmallBodyModel
{
	private static String[] modelNames = {
		"DEIMOS"};

	private static String[] modelFiles = {
		"/DEIMOS/DEIMOS.vtk.gz"};

	private static String[] coloringFiles = {
		"/DEIMOS/DEIMOS_Elevation.txt.gz",
		"/DEIMOS/DEIMOS_GravitationalAcceleration.txt.gz",
		"/DEIMOS/DEIMOS_GravitationalPotential.txt.gz",
		"/DEIMOS/DEIMOS_Slope.txt.gz"};

	public DeimosModel()
	{
		super(modelNames, modelFiles, coloringFiles, false);
	}

	public int getNumberResolutionLevels()
	{
		return modelFiles.length;
	}
}
