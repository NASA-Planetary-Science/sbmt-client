package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkPolyDataMapper;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.util.MathUtil;

public class EarthDirectionMarker extends vtkConeSource
{
	private vtkActor earthMarkerHeadActor;
	private double[] white = {1.0, 1.0, 1.0, 1.0};
    private double[] earthMarkerColor = {0.0, 0.0, 1.0, 1.0};
    private double markerRadius;
	private double markerHeight;
	private double centerX, centerY, centerZ;
	double[] zAxis = {1,0,0};
	double[] earthMarkerPosition = new double[3];
	private double scale = 1.0;

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
		if (earthMarkerHeadActor != null) return earthMarkerHeadActor;
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
        earthMarkerHeadActor.SetScale(scale);
        return earthMarkerHeadActor;
	}

	public void setColor(Color color)
	{
		earthMarkerHeadActor.GetProperty().SetDiffuseColor(new double[] {color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0});
	}

	public void updateEarthPosition(double[] earthPosition, double[] earthMarkerPosition)
	{
		this.earthMarkerPosition = earthMarkerPosition;
        //rotates earth pointer to point in direction of earth - Alex W
        double[] earthPosDirection = new double[3];
        MathUtil.unorm(earthPosition, earthPosDirection);
        double[] rotationAxisEarth = new double[3];
        MathUtil.vcrss(earthPosDirection, zAxis, rotationAxisEarth);

        double rotationAngleEarth = ((180.0/Math.PI)*MathUtil.vsep(zAxis, earthPosDirection));

        vtkTransform earthMarkerTransform = new vtkTransform();
        earthMarkerTransform.Translate(earthMarkerPosition);
        earthMarkerTransform.RotateWXYZ(-rotationAngleEarth, rotationAxisEarth[0], rotationAxisEarth[1], rotationAxisEarth[2]);
        earthMarkerHeadActor.SetUserTransform(earthMarkerTransform);
	}


	// set the earth pointer size - Alex W
    public void setEarthPointerSize(int radius)
    {
    	scale = (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
//        double rad = markerRadius * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);//markerRadius * ((4.5/100.0)*(double)radius + 0.5);
//        double height = markerHeight * (2.66e-4 * Math.pow((double)radius,2) + 1e-4*(double)radius + .33);
//        SetRadius(rad);
//        SetHeight(height);
        earthMarkerHeadActor.SetScale(scale);
//        Update();
        earthMarkerHeadActor.Modified();
//        updateActorVisibility();
    }
}
