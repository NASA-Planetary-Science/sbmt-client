package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;

public class EarthDirectionMarker extends vtkConeSource
{
	private vtkActor earthMarkerHeadActor;
	private double[] white = {1.0, 1.0, 1.0, 1.0};
    private double[] earthMarkerColor = {0.0, 0.0, 1.0, 1.0};
    private double markerRadius;
	private double markerHeight;
	private double centerX, centerY, centerZ;

	public EarthDirectionMarker(double markerRadius, double markerHeight,
			double centerX, double centerY, double centerZ)
	{
		this.markerHeight = markerHeight;
		this.markerRadius = markerRadius;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		updateSource();
	}

	private void updateSource()
	{
		SetRadius(markerRadius);
        SetHeight(markerHeight);
        SetCenter(centerX, centerY, centerZ);
        SetResolution(50);
        Update();
	}

	public EarthDirectionMarker(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	public vtkActor getActor()
	{
		vtkPolyDataMapper earthMarkerHeadMapper = new vtkPolyDataMapper();
        earthMarkerHeadMapper.SetInputData(GetOutput());
        earthMarkerHeadActor = new vtkActor();
        earthMarkerHeadActor.SetMapper(earthMarkerHeadMapper);
        earthMarkerHeadActor.GetProperty().SetDiffuseColor(earthMarkerColor);
        earthMarkerHeadActor.GetProperty().SetSpecularColor(white);
        earthMarkerHeadActor.GetProperty().SetSpecular(0.8);
        earthMarkerHeadActor.GetProperty().SetSpecularPower(80.0);
        earthMarkerHeadActor.GetProperty().ShadingOn();
        earthMarkerHeadActor.GetProperty().SetInterpolationToPhong();
        return earthMarkerHeadActor;
	}


	// set the earth pointer size - Alex W
    public void setEarthPointerSize(int radius)
    {
        double rad = markerRadius * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);//markerRadius * ((4.5/100.0)*(double)radius + 0.5);
        double height = markerHeight * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        SetRadius(rad);
        SetHeight(height);
        Update();
        Modified();
//        updateActorVisibility();
    }
}
