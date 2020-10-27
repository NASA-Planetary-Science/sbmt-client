package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import vtk.vtkFloatArray;

public interface ITimeCalculatedPlateValues extends ICalculatedPlateValues
{
	vtkFloatArray getPlateValuesForTime(double time);
}
