package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import vtk.vtkFloatArray;

public interface ICalculatedPlateValues
{
	public vtkFloatArray getValues();

	public void setNumberOfValues(int numValues);

	public void setNumberOfDimensions(int numDimensions);
}
