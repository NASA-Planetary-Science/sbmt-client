package edu.jhuapl.sbmt.stateHistory.rendering;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;

/**
 * vtkConeSource child class representing the FOV of an instrument
 * @author steelrj1
 *
 */
public class SpacecraftFieldOfView extends vtkConeSource
{
	/**
	 * The vtkActor for this object
	 */
	private vtkActor spacecraftFovActor;

    /**
     * The offset of the FOV from the spacecraft
     */
    private double[] spacecraftFovOffset = {0.0, 0.0, 0.5};

    /**
     * The color of the FOV
     */
    private double[] fovColor = {0.3, 0.3, 1.0, 0.5};

	/**
	 * Static property for the white color
	 */
	private static double[] white = {1.0, 1.0, 1.0, 1.0};


	/**
	 * Constructor.  Initializes values for the parent vtkConeSource
	 */
	public SpacecraftFieldOfView()
	{
		SetDirection(0.0, 0.0, -1.0);
        SetRadius(0.5);
        SetCenter(spacecraftFovOffset);
        SetHeight(1.0);
        SetResolution(4);
        Update();
	}

	/**
	 * vtk Constructor
	 * @param id
	 */
	public SpacecraftFieldOfView(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns the vtkActor for this object, creating if needed.
	 * @return
	 */
	public vtkActor getActor()
	{
		if (spacecraftFovActor != null) return spacecraftFovActor;
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
