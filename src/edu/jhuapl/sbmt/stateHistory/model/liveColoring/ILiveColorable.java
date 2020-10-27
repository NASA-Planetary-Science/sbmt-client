package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import vtk.vtkFloatArray;

public interface ILiveColorable
{

	public vtkFloatArray getValues(ICalculatedPlateValues calculator);
}
