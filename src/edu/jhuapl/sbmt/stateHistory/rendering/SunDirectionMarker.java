package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.util.MathUtil;

public class SunDirectionMarker extends vtkConeSource
{
	private double markerRadius;
	private double markerHeight;
	private double centerX, centerY, centerZ;
	private vtkActor sunMarkerActor;
	private double[] sunMarkerColor = {1.0, 1.0, 0.0, 1.0};
	private double[] white = {1.0, 1.0, 1.0, 1.0};
	double[] sunMarkerPosition = new double[3];
	double rotationAngleSun;
	double[] rotationAxisSun;
	double[] sunPos;
	private double scale = 1.0;

	public SunDirectionMarker(double markerRadius, double markerHeight,
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

	public SunDirectionMarker(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	public vtkActor getActor()
	{
		if (sunMarkerActor != null) return sunMarkerActor;
		vtkPolyDataMapper sunMapper = new vtkPolyDataMapper();
        sunMapper.SetInputData(GetOutput());
        sunMarkerActor = new vtkActor();
        sunMarkerActor.SetMapper(sunMapper);
        sunMarkerActor.GetProperty().SetDiffuseColor(sunMarkerColor);
        sunMarkerActor.GetProperty().SetSpecularColor(white);
        sunMarkerActor.GetProperty().SetSpecular(0.8);
        sunMarkerActor.GetProperty().SetSpecularPower(80.0);
        sunMarkerActor.GetProperty().ShadingOn();
        sunMarkerActor.GetProperty().SetInterpolationToFlat();
        sunMarkerActor.GetProperty().SetRepresentationToSurface();
        sunMarkerActor.SetScale(scale);
        return sunMarkerActor;
	}

	public void setColor(Color color)
	{
		sunMarkerActor.GetProperty().SetDiffuseColor(new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0});
	}

	public void updateSunPosition(double[] sunPos, double[] sunMarkerPosition)
	{
		this.sunPos = sunPos;
		this.sunMarkerPosition = sunMarkerPosition;
		if (sunMarkerActor == null) return;
		double[] zAxis = {1,0,0};
        double[] sunPosDirection = new double[3];
        MathUtil.unorm(sunPos, sunPosDirection);
        rotationAxisSun = new double[3];
        MathUtil.vcrss(sunPosDirection, zAxis, rotationAxisSun);
        rotationAngleSun = ((180.0/Math.PI)*MathUtil.vsep(zAxis, sunPosDirection));

        vtkTransform sunMarkerTransform = new vtkTransform();
        sunMarkerTransform.Translate(sunMarkerPosition);
        sunMarkerTransform.RotateWXYZ(-rotationAngleSun, rotationAxisSun[0], rotationAxisSun[1], rotationAxisSun[2]);
        sunMarkerActor.SetUserTransform(sunMarkerTransform);

	}

    // set the sun pointer size - Alex W
    public void setSunPointerSize(int radius)
    {
        scale = (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
        sunMarkerActor.SetScale(scale);
        sunMarkerActor.Modified();
    }

}
