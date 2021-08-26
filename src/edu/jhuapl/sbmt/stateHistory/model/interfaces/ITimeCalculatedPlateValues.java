package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import vtk.vtkFloatArray;

public interface ITimeCalculatedPlateValues extends ICalculatedPlateValues
{
	vtkFloatArray getPlateValuesForTime(double time);
}
