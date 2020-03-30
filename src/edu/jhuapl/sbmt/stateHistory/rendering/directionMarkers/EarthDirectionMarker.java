package edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers;

import vtk.vtkTransform;

import edu.jhuapl.saavtk.util.MathUtil;

/**
 * @author steelrj1
 *
 */
public class EarthDirectionMarker extends BaseDirectionMarker
{
	/**
	 * @param markerRadius
	 * @param markerHeight
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 */
	public EarthDirectionMarker(double markerRadius, double markerHeight,
			double centerX, double centerY, double centerZ)
	{
		super(markerRadius, markerHeight, centerX, centerY, centerZ);
		specularColorValue = 0.8;
	}

	/**
	 * @param id
	 */
	public EarthDirectionMarker(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param earthPosition
	 * @param earthMarkerPosition
	 */
	public void updateEarthPosition(double[] earthPosition, double[] earthMarkerPosition)
	{
        //rotates earth pointer to point in direction of earth - Alex W
        double[] earthPosDirection = new double[3];
        MathUtil.unorm(earthPosition, earthPosDirection);
        double[] rotationAxisEarth = new double[3];
        MathUtil.vcrss(earthPosDirection, zAxis, rotationAxisEarth);

        double rotationAngleEarth = ((180.0/Math.PI)*MathUtil.vsep(zAxis, earthPosDirection));

        vtkTransform earthMarkerTransform = new vtkTransform();
        earthMarkerTransform.Translate(earthMarkerPosition);
        earthMarkerTransform.RotateWXYZ(-rotationAngleEarth, rotationAxisEarth[0], rotationAxisEarth[1], rotationAxisEarth[2]);
        markerHeadActor.SetUserTransform(earthMarkerTransform);
	}

}
