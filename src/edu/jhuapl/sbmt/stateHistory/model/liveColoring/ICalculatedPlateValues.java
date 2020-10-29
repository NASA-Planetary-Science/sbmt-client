package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import vtk.vtkFloatArray;

public interface ICalculatedPlateValues
{
	public vtkFloatArray getValues();

	public void setNumberOfValues(int numValues);

	public void setNumberOfDimensions(int numDimensions);
}
