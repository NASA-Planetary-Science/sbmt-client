package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import java.awt.Color;

import vtk.vtkActor;

public interface PlannedDataActor
{
	/**
	 * @param scPos
	 * @param frus1
	 * @param frus2
	 * @param frus3
	 * @param frus4
	 * @param height
	 * @param width
	 * @param depth
	 */
	public void updatePointing(double[] scPos, double[] frus1, double[] frus2, double[] frus3, double[] frus4,
			int height, int width, int depth);

	/**
	 * @param visible
	 */
	public void SetVisibility(int visible);

	/**
	 * @return
	 */
	public vtkActor getFootprintActor();

	/**
	 * @return
	 */
	public vtkActor getFootprintBoundaryActor();

	/**
	 * @return
	 */
	public double getTime();

	public Color getColor();

	public void setColor(Color color);
}
