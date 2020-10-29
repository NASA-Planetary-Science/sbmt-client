package edu.jhuapl.sbmt.stateHistory.model.liveColoring;

import vtk.vtkDataArray;

public interface ITimeCalculatedPlateValues extends ICalculatedPlateValues
{
	vtkDataArray getPlateValuesForTime(double time);
}
