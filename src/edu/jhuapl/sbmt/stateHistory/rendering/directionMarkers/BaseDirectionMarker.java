package edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;

public abstract class BaseDirectionMarker extends vtkConeSource
{

	/**
	 * Easy access for white color
	 */
	protected double[] white = {1.0, 1.0, 1.0, 1.0};

	/**
	 * Easy access for zAxis
	 */
	protected double[] zAxis = {1,0,0};

	/**
	 * Scale of the marker
	 */
	protected double scale = 1.0;

    /**
     * Radius of the marker
     */
	protected double markerRadius;

	/**
	 * Height of the marker above the surface
	 */
	protected double markerHeight;

	/**
	 * Center coordinates for marker
	 */
	protected double centerX, centerY, centerZ;

	/**
	 * The vtkActor representing the marker head
	 */
	protected vtkActor markerHeadActor;

	/**
	 * The color of the marker
	 */
	protected double[] markerColor = {0.0, 0.0, 1.0, 1.0};

	/**
	 *
	 */
	protected double specularColorValue;


	public BaseDirectionMarker(long id)
	{
		super(id);
	}

	/**
	 * @param markerRadius
	 * @param markerHeight
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 */
	public BaseDirectionMarker(double markerRadius, double markerHeight,
			double centerX, double centerY, double centerZ)
	{
		this.markerHeight = markerHeight;
		this.markerRadius = markerRadius;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		updateSource();
	}

	/**
	 * @return
	 */
	public vtkActor getActor()
	{
		if (markerHeadActor != null) return markerHeadActor;
		vtkPolyDataMapper markerHeadMapper = new vtkPolyDataMapper();
        markerHeadMapper.SetInputData(GetOutput());
        markerHeadActor = new vtkActor();
        markerHeadActor.SetMapper(markerHeadMapper);
        markerHeadActor.GetProperty().SetDiffuseColor(markerColor);
        markerHeadActor.GetProperty().SetSpecularColor(white);
        markerHeadActor.GetProperty().SetSpecular(specularColorValue);
        markerHeadActor.GetProperty().SetSpecularPower(80.0);
        markerHeadActor.GetProperty().ShadingOn();
        markerHeadActor.GetProperty().SetInterpolationToPhong();
        markerHeadActor.SetScale(scale);
        return markerHeadActor;
	}

	/**
	 * @param color
	 */
	public void setColor(Color color)
	{
		markerHeadActor.GetProperty().SetDiffuseColor(new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0});
	}

    /**
     * @param radius
     */
    public void setPointerSize(int radius)
    {
    	scale = (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        markerHeadActor.SetScale(scale);
        markerHeadActor.Modified();
    }

	/**
	 *
	 */
	private void updateSource()
	{
		SetRadius(markerRadius);
       SetHeight(markerHeight);
       SetCenter(centerX, centerY, centerZ);
       SetResolution(50);
       Update();
	}

}
