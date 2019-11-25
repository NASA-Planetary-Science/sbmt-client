package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;

public class SpacecraftFieldOfView extends vtkConeSource
{
	private vtkActor spacecraftFovActor;
    private double[] spacecraftFovOffset = {0.0, 0.0, 0.5};
    private double[] fovColor = {0.3, 0.3, 1.0, 0.5};
	private double[] white = {1.0, 1.0, 1.0, 1.0};


	public SpacecraftFieldOfView()
	{
		SetDirection(0.0, 0.0, -1.0);
        SetRadius(0.5);
        SetCenter(spacecraftFovOffset);
        SetHeight(1.0);
        SetResolution(4);
        Update();
	}

	public SpacecraftFieldOfView(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	public vtkActor getActor()
	{
        vtkPolyDataMapper spacecraftFovMapper = new vtkPolyDataMapper();
        spacecraftFovMapper.SetInputData(GetOutput());
        spacecraftFovActor = new vtkActor();
        spacecraftFovActor.SetMapper(spacecraftFovMapper);
        spacecraftFovActor.GetProperty().SetDiffuseColor(fovColor);
        spacecraftFovActor.GetProperty().SetSpecularColor(white);
        spacecraftFovActor.GetProperty().SetSpecular(0.5);
        spacecraftFovActor.GetProperty().SetSpecularPower(100.0);
        spacecraftFovActor.GetProperty().SetOpacity(fovColor[3]);
        spacecraftFovActor.GetProperty().ShadingOn();
        spacecraftFovActor.GetProperty().SetInterpolationToPhong();
        return spacecraftFovActor;
	}

}
