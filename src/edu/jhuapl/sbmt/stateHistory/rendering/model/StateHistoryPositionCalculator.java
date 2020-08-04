/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.rendering.model;

import vtk.vtkMatrix4x4;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFrustum;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.text.SpacecraftLabel;

import crucible.core.mechanics.FrameID;
import crucible.core.mechanics.utilities.SimpleFrameID;

/**
 * @author steelrj1
 *
 */
public class StateHistoryPositionCalculator implements IStateHistoryPositionCalculator
{
	/**
	*
	*/
	private static final double JupiterScale = 75000;

	/**
	*
	*/
	private double[] sunDirection;

	/**
	*
	*/
	private double[] earthPosition;

	/**
	 *
	 */
	private double[] sunPosition;

	/**
	*
	*/
	private double[] spacecraftPosition;

	/**
	 *
	 */
	private SmallBodyModel smallBodyModel;

	/**
	*
	*/
	private double[] currentLookFromDirection;

	/**
	 *
	 */
	private static double[] zAxis = { 1, 0, 0 };

	/**
	 *
	 */
	public StateHistoryPositionCalculator(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void updateSunPosition(StateHistory history, double time, SunDirectionMarker sunDirectionMarker)
	{
		sunPosition = history.getSunPosition();
		if (sunPosition == null) return;
		vtkMatrix4x4 sunMarkerMatrix = new vtkMatrix4x4();

		double[] sunMarkerPosition = new double[3];
		sunDirection = new double[3];
		double[] sunViewpoint = new double[3];
		double[] sunViewDirection = new double[3];
		MathUtil.unorm(sunPosition, sunDirection);
		MathUtil.vscl(JupiterScale, sunDirection, sunViewpoint);
		MathUtil.vscl(-1.0, sunDirection, sunViewDirection);
		int result = smallBodyModel.computeRayIntersection(sunViewpoint, sunViewDirection, sunMarkerPosition);
		if (result == -1) return;
		for (int i = 0; i < 3; i++)
		{
			sunMarkerMatrix.SetElement(i, 3, sunMarkerPosition[i]);
		}

		sunDirectionMarker.updateSunPosition(sunPosition, sunMarkerPosition);
	}

	@Override
	public void updateEarthPosition(StateHistory history, double time, EarthDirectionMarker earthDirectionMarker)
	{
		earthPosition = history.getEarthPosition();
		if (earthPosition == null) return;
		vtkMatrix4x4 earthMarkerMatrix = new vtkMatrix4x4();

		double[] earthMarkerPosition = new double[3];
		double[] earthDirection = new double[3];
		double[] earthViewpoint = new double[3];
		double[] earthViewDirection = new double[3];
		MathUtil.unorm(earthPosition, earthDirection);
		MathUtil.vscl(JupiterScale, earthDirection, earthViewpoint);
		MathUtil.vscl(-1.0, earthDirection, earthViewDirection);
		int result = smallBodyModel.computeRayIntersection(earthViewpoint, earthViewDirection, earthMarkerPosition);
		if (result == -1) return;
		for (int i = 0; i < 3; i++)
		{
			earthMarkerMatrix.SetElement(i, 3, earthMarkerPosition[i]);
		}

		earthDirectionMarker.updateEarthPosition(earthPosition, earthMarkerPosition);

	}

	@Override
	public void updateSpacecraftPosition(StateHistory history, double time, SpacecraftBody spacecraft, SpacecraftDirectionMarker scDirectionMarker,
			SpacecraftLabel spacecraftLabelActor, PerspectiveImageFrustum fov)
	{
		vtkMatrix4x4 spacecraftBodyMatrix = new vtkMatrix4x4();
		vtkMatrix4x4 spacecraftIconMatrix = new vtkMatrix4x4();
		vtkMatrix4x4 fovMatrix = new vtkMatrix4x4();
		vtkMatrix4x4 fovRotateXMatrix = new vtkMatrix4x4();
		vtkMatrix4x4 fovRotateYMatrix = new vtkMatrix4x4();
		vtkMatrix4x4 fovRotateZMatrix = new vtkMatrix4x4();
		vtkMatrix4x4 fovScaleMatrix = new vtkMatrix4x4();

		double iconScale = 1.0;
		// set to identity
		spacecraftBodyMatrix.Identity();
		spacecraftIconMatrix.Identity();
		fovMatrix.Identity();
		fovRotateXMatrix.Identity();
		fovRotateYMatrix.Identity();
		fovRotateZMatrix.Identity();
		double[] xaxis = history.getCurrentState().getSpacecraftXAxis();
		double[] yaxis = history.getCurrentState().getSpacecraftYAxis();
		double[] zaxis = history.getCurrentState().getSpacecraftZAxis();
		// set body orientation matrix
		for (int i = 0; i < 3; i++)
		{
			if (history.getCurrentState().getSpacecraftXAxis() == null) continue;
			spacecraftBodyMatrix.SetElement(i, 0, xaxis[i]);
			spacecraftBodyMatrix.SetElement(i, 1, yaxis[i]);
			spacecraftBodyMatrix.SetElement(i, 2, zaxis[i]);
		}

		// create the icon matrix, which is just the body matrix scaled by a
		// factor
		for (int i = 0; i < 3; i++)
			spacecraftIconMatrix.SetElement(i, i, iconScale);
		spacecraftIconMatrix.Multiply4x4(spacecraftIconMatrix, spacecraftBodyMatrix, spacecraftIconMatrix);

		spacecraftPosition = history.getSpacecraftPosition();
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: sc pos " + spacecraftPosition[0] + " " +  spacecraftPosition[1] + " " + spacecraftPosition[2]);
		double[] spacecraftMarkerPosition = new double[3];
		double[] spacecraftDirection = new double[3];
		double[] spacecraftViewpoint = new double[3];
		double[] spacecraftViewDirection = new double[3];
		MathUtil.unorm(spacecraftPosition, spacecraftDirection);
		MathUtil.vscl(JupiterScale, spacecraftDirection, spacecraftViewpoint);
		MathUtil.vscl(-1.0, spacecraftDirection, spacecraftViewDirection);
		int result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection,
				spacecraftMarkerPosition);
		if (result == -1) return;
		// rotates spacecraft pointer to point in direction of spacecraft - Alex
		// W
		double[] spacecraftPos = spacecraftMarkerPosition;
		double[] spacecraftPosDirection = new double[3];
		MathUtil.unorm(spacecraftPos, spacecraftPosDirection);
		double[] rotationAxisSpacecraft = new double[3];
		MathUtil.vcrss(spacecraftPosDirection, zAxis, rotationAxisSpacecraft);

		double rotationAngleSpacecraft = ((180.0 / Math.PI) * MathUtil.vsep(zAxis, spacecraftPosDirection));

		vtkTransform spacecraftMarkerTransform = new vtkTransform();
		spacecraftMarkerTransform.Translate(spacecraftPos);
		spacecraftMarkerTransform.RotateWXYZ(-rotationAngleSpacecraft, rotationAxisSpacecraft[0],
				rotationAxisSpacecraft[1], rotationAxisSpacecraft[2]);

		// set translation
		for (int i = 0; i < 3; i++)
		{
			spacecraftBodyMatrix.SetElement(i, 3, spacecraftPosition[i]);
			spacecraftIconMatrix.SetElement(i, 3, spacecraftPosition[i]);
			fovMatrix.SetElement(i, 3, spacecraftPosition[i]);

		}
		spacecraft.getActor().SetUserMatrix(spacecraftIconMatrix);

		spacecraftLabelActor.SetAttachmentPoint(spacecraftPosition);
		spacecraftLabelActor.setDistanceText(history.getCurrentState(), spacecraftPosition, smallBodyModel);

//		fov.getActor().SetUserMatrix(fovMatrix);
//		fov.getActor().SetUserMatrix(spacecraftBodyMatrix);
		FrameID instrumentFrameID = new SimpleFrameID("ORX_OCAMS_POLYCAM");
		double[] lookDirection = history.getInstrumentLookDirection(instrumentFrameID);
		double[] frus1 = new double[] { history.getFrustum(instrumentFrameID, 0).getI(), history.getFrustum(instrumentFrameID, 0).getJ(), history.getFrustum(instrumentFrameID, 0).getK()};
		double[] frus2 = new double[] { history.getFrustum(instrumentFrameID, 1).getI(), history.getFrustum(instrumentFrameID, 1).getJ(), history.getFrustum(instrumentFrameID, 1).getK()};
		double[] frus3 = new double[] { history.getFrustum(instrumentFrameID, 2).getI(), history.getFrustum(instrumentFrameID, 2).getJ(), history.getFrustum(instrumentFrameID, 2).getK()};
		double[] frus4 = new double[] { history.getFrustum(instrumentFrameID, 3).getI(), history.getFrustum(instrumentFrameID, 3).getJ(), history.getFrustum(instrumentFrameID, 3).getK()};
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: fov actor " + fov.getFrustumActor());
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: scpos" + spacecraftPosition[0] + " " + spacecraftPosition[1] + " " + spacecraftPosition[2]);
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: frus 1" + frus1[0] + " " + frus1[1] + " " + frus1[2]);
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: frus 2" + frus2[0] + " " + frus2[1] + " " + frus2[2]);
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: frus 3" + frus3[0] + " " + frus3[1] + " " + frus3[2]);
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: frus 4" + frus4[0] + " " + frus4[1] + " " + frus4[2]);

		fov.updatePointing(spacecraftPosition, frus1, frus2, frus3, frus4);
//		fov.SetAngle(0.794);
//		fov.SetCenter(new double[] {lookDirection[0]/2.0, lookDirection[1]/2.0, lookDirection[2]/2.0});
////		fov.SetCenter(new double[] {spacecraftPosition[0]/2.0, spacecraftPosition[1]/2.0, spacecraftPosition[2]/2.0});
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: fov center " + fov.GetCenter()[0] + " " + fov.GetCenter()[1] + " " + fov.GetCenter()[2]);
//		fov.SetDirection(lookDirection);
//		fov.SetHeight(MathUtil.vnorm(spacecraftPosition));
//		fov.Update();

		scDirectionMarker.getActor().SetUserTransform(spacecraftMarkerTransform);

		// spacecraftBoresight.Modified();
		spacecraft.getActor().Modified();
		scDirectionMarker.getActor().Modified();
		spacecraftLabelActor.Modified();
		if (fov.getFrustumActor() != null)
			fov.getFrustumActor().Modified();

	}

	@Override
	public double[] updateLookDirection(RendererLookDirection lookDirection, double scalingFactor)
	{
		// set camera to earth, spacecraft, or sun views - Alex W
		if (lookDirection == RendererLookDirection.EARTH)
		{
			double[] newEarthPos = new double[3];
			MathUtil.unorm(earthPosition, newEarthPos);
			MathUtil.vscl(scalingFactor, newEarthPos, newEarthPos);
			currentLookFromDirection = newEarthPos;
		}
		else if (lookDirection == RendererLookDirection.SPACECRAFT)
		{
			double[] boresight = new double[]
			{ spacecraftPosition[0] * 0.9, spacecraftPosition[1] * 0.9, spacecraftPosition[2] * 0.9 };
			currentLookFromDirection = boresight;
		}
		else if (lookDirection == RendererLookDirection.SUN)
		{
			double[] newSunPos = new double[3];
			MathUtil.unorm(sunPosition, newSunPos);
			MathUtil.vscl(scalingFactor, newSunPos, newSunPos);
			currentLookFromDirection = newSunPos;
		}
		else if (lookDirection == RendererLookDirection.SPACECRAFT_THIRD)
		{
			double[] thirdPerson = new double[]
			{ spacecraftPosition[0] * 1.1, spacecraftPosition[1] * 1.1, spacecraftPosition[2] * 1.1 };
			currentLookFromDirection = thirdPerson;
		}
		else // free view mode
		{
			currentLookFromDirection = spacecraftPosition;
		}
		return currentLookFromDirection;
	}

	@Override
	public double[] getCurrentLookFromDirection()
	{
		return currentLookFromDirection;
	}
}
