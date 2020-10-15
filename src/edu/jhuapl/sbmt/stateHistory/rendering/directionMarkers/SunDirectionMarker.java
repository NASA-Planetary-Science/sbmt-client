package edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers;

import java.awt.Color;

import vtk.vtkActor;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.util.MathUtil;

/**
 * @author steelrj1
 *
 */
public class SunDirectionMarker extends BaseDirectionMarker
{
	/**
	 * @param markerRadius
	 * @param markerHeight
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 */
	public SunDirectionMarker(double markerRadius, double markerHeight,
			double centerX, double centerY, double centerZ)
	{
		super(markerRadius, markerHeight, centerX, centerY, centerZ, "Sun Direction");
		specularColorValue = 0.8;
		markerColor = new double[] { Color.YELLOW.getRed()/255.0, Color.YELLOW.getGreen()/255.0, Color.YELLOW.getBlue()/255.0 };
	}

	/**
	 * @param id
	 */
	public SunDirectionMarker(long id)
	{
		super(id);
	}

	/**
	 * @return
	 */
	public vtkActor getActor()
	{
		if (markerHeadActor != null) return markerHeadActor;
		markerHeadActor = super.getActor();
        markerHeadActor.GetProperty().SetInterpolationToFlat();
        markerHeadActor.GetProperty().SetRepresentationToSurface();
        return markerHeadActor;
	}

	/**
	 * @param sunPos
	 * @param sunMarkerPosition
	 */
	public void updateSunPosition(double[] sunPos, double[] sunMarkerPosition)
	{
		if (markerHeadActor == null) return;
		double[] zAxis = {1,0,0};
        double[] sunPosDirection = new double[3];
        MathUtil.unorm(sunPos, sunPosDirection);
        double[] rotationAxisSun = new double[3];
        MathUtil.vcrss(sunPosDirection, zAxis, rotationAxisSun);
        double rotationAngleSun = ((180.0/Math.PI)*MathUtil.vsep(zAxis, sunPosDirection));

        vtkTransform sunMarkerTransform = new vtkTransform();
        sunMarkerTransform.Translate(sunMarkerPosition);
        sunMarkerTransform.RotateWXYZ(-rotationAngleSun, rotationAxisSun[0], rotationAxisSun[1], rotationAxisSun[2]);
        markerHeadActor.SetUserTransform(sunMarkerTransform);

	}
}
