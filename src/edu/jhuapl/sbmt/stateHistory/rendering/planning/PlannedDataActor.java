package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import vtk.vtkActor;

public interface PlannedDataActor
{
	public void updatePointing(double[] scPos, double[] frus1, double[] frus2, double[] frus3, double[] frus4,
			int height, int width, int depth);

	public void SetVisibility(int visible);

	public vtkActor getFootprintActor();

	public vtkActor getFootprintBoundaryActor();

	public double getTime();
}
