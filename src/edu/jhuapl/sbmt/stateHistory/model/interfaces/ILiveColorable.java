package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import vtk.vtkFloatArray;

public interface ILiveColorable
{

	public vtkFloatArray getValues(ICalculatedPlateValues calculator);
}
