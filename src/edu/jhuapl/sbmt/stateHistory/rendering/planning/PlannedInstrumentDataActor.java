package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.IPositionOrientation;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedInstrumentDataActor extends vtkActor
{
	protected vtkPolyData frustumPolyData;
	protected vtkActor frustumActor;
	protected vtkActor boundaryActor;
	protected vtkPolyData boundaryPolydata;
	protected vtkPolyDataMapper boundaryMapper;
	protected PlannedInstrumentData data;
	protected IPositionOrientation positionOrientationManager;
	protected SmallBodyModel model;
	protected double maxFrustumDepth;
    protected double minFrustumDepth;
    protected double offset = 0.003;
    protected double[] spacecraftPosition;
    protected double[] frustum1;
    protected double[] frustum2;
    protected double[] frustum3;
    protected double[] frustum4;
    protected double[] boresightDirection;
    protected double[] upVector;

	public PlannedInstrumentDataActor(PlannedInstrumentData data, SmallBodyModel smallBodyModel, IPositionOrientation positionOrientationManager)
	{
		this.data = data;
		this.model = smallBodyModel;
		this.positionOrientationManager = positionOrientationManager;
		boundaryPolydata = new vtkPolyData();
		boundaryPolydata.SetPoints(new vtkPoints());
		boundaryPolydata.SetVerts(new vtkCellArray());

		boundaryMapper = new vtkPolyDataMapper();
		boundaryActor = new vtkActor();
	}

	protected void calculatePosOrientationAtTime(double time)
	{
		spacecraftPosition = positionOrientationManager.getSpacecraftPosition(time).toArray();

		boresightDirection = positionOrientationManager.getLookDirectionAtTimeForInstrument(time, data.getInstrument()).toArray();
		upVector = positionOrientationManager.getUpDirection(time).toArray();
		double fovxDeg = positionOrientationManager.getInstrumentFOVWidth(data.getInstrument());
		double fovyDeg = positionOrientationManager.getInstrumentFOVHeight(data.getInstrument());
		Frustum frustum = new Frustum(spacecraftPosition, boresightDirection, upVector, fovxDeg, fovyDeg);
		frustum1 = frustum.ul;
		frustum2 = frustum.ll;
		frustum3 = frustum.lr;
		frustum4 = frustum.ur;

	}

}
