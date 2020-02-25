package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;

public class SpacecraftDirectionMarker extends vtkConeSource
{
	private vtkActor spacecraftMarkerHeadActor;
    private double[] spacecraftMarkerColor = {0.0, 1.0, 0.0, 1.0};
    private double markerRadius;
	private double markerHeight;
	private double centerX, centerY, centerZ;
    private double[] white = {1.0, 1.0, 1.0, 1.0};


	public SpacecraftDirectionMarker(double markerRadius, double markerHeight,
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

	public SpacecraftDirectionMarker(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}


	public vtkActor getActor()
	{
		if (spacecraftMarkerHeadActor != null) return spacecraftMarkerHeadActor;
		vtkPolyDataMapper spacecrafterMarkerHeadMapper = new vtkPolyDataMapper();
        spacecrafterMarkerHeadMapper.SetInputData(GetOutput());
        spacecraftMarkerHeadActor = new vtkActor();
        spacecraftMarkerHeadActor.SetMapper(spacecrafterMarkerHeadMapper);
        spacecraftMarkerHeadActor.GetProperty().SetDiffuseColor(spacecraftMarkerColor);
        spacecraftMarkerHeadActor.GetProperty().SetSpecularColor(white);
        spacecraftMarkerHeadActor.GetProperty().SetSpecular(0.1);
        spacecraftMarkerHeadActor.GetProperty().SetSpecularPower(80.0);
        spacecraftMarkerHeadActor.GetProperty().ShadingOn();
        spacecraftMarkerHeadActor.GetProperty().SetInterpolationToPhong();
        return spacecraftMarkerHeadActor;
	}

	public void setColor(Color color)
	{
		spacecraftMarkerHeadActor.GetProperty().SetDiffuseColor(new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0});
	}


    // set the spacecraft pointer size - Alex W
    public void setSpacecraftPointerSize(int radius)
    {
        double rad = markerRadius *(2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        double height = markerHeight * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        SetRadius(rad);
        SetHeight(height);
        Update();
        Modified();
//        updateActorVisibility();
    }
}
