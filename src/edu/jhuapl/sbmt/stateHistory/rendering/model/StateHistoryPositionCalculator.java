/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.rendering.model;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkMatrix4x4;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.BasicLidarPoint;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFrustum;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.text.SpacecraftLabel;

import crucible.core.math.vectorspace.UnwritableVectorIJK;
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
	private static SmallBodyModel smallBodyModel;

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
		StateHistoryPositionCalculator.smallBodyModel = smallBodyModel;
	}

	@Override
	public void updateSunPosition(StateHistory history, double time, SunDirectionMarker sunDirectionMarker)
	{
		sunPosition = history.getLocationProvider().getSunPosition();
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
		earthPosition = history.getLocationProvider().getEarthPosition();
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

	public static void updateFootprintPointing(StateHistory history, double time, PerspectiveImageFootprint fprint)
	{
		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();
		double[] spacecraftPosition = locationProvider.getSpacecraftPositionAtTime(time);
		FrameID instrumentFrameID = new SimpleFrameID(fprint.getInstrumentName());

		UnwritableVectorIJK frus1Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 0);
		UnwritableVectorIJK frus2Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 1);
		UnwritableVectorIJK frus3Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 2);
		UnwritableVectorIJK frus4Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 3);

		double[] frus1 = new double[] { frus1Vec.getI(), frus1Vec.getJ(), frus1Vec.getK()};
		double[] frus2 = new double[] { frus2Vec.getI(), frus2Vec.getJ(), frus2Vec.getK()};
		double[] frus3 = new double[] { frus3Vec.getI(), frus3Vec.getJ(), frus3Vec.getK()};
		double[] frus4 = new double[] { frus4Vec.getI(), frus4Vec.getJ(), frus4Vec.getK()};

		fprint.setStaticFootprintSet(true);
		fprint.updatePointing(spacecraftPosition, frus1, frus2, frus3, frus4, 1024, 1024, 1);
	}

	public static LidarPoint updateLidarFootprintPointing(StateHistory history, double time, String instrumentName)
	{
		double[] spacecraftPosition = history.getLocationProvider().getSpacecraftPositionAtTime(time);

		double[] boresightInterceptPosition = getSpacecraftBoresightInterceptPosition(history, instrumentName, spacecraftPosition, time);
		if (boresightInterceptPosition == null) return null;


		double[] rangeVector = new double[3];
		MathUtil.vsub(spacecraftPosition, boresightInterceptPosition, rangeVector);
		double range = MathUtil.unorm(rangeVector, rangeVector);
		double intensity = 1.0;
		return new BasicLidarPoint(boresightInterceptPosition, spacecraftPosition, time, range, intensity);
	}

	private static double[] getInstrumentLookDirAtTime(StateHistory history, String instrumentName, double time)
	{
		double[] instLookDir = new double[3];
		MathUtil.vscl(-1.0, history.getLocationProvider().getInstrumentLookDirectionAtTime(instrumentName, time), instLookDir);
		return instLookDir;
	}

	private static double[] getSpacecraftBoresightInterceptPosition(StateHistory history, String instrumentName, double[] spacecraftPosition, double time)
	{
//		double[] spacecraftPosition = history.getLocationProvider().getSpacecraftPositionAtTime(time);
		double[] boresightInterceptPosition = new double[3];
		double[] instLookDir = getInstrumentLookDirAtTime(history, instrumentName, time);

		int result = smallBodyModel.computeRayIntersection(spacecraftPosition, instLookDir,
				boresightInterceptPosition);
		if (result > 0)
			return boresightInterceptPosition;
		else
			return null;
	}

	public static double getIncidenceAngle(StateHistory history, String instrumentName, double[] scPos, double time)
	{
		if (scPos == null)
			scPos = history.getLocationProvider().getSpacecraftPosition();
		double[] boresightInterceptPosition = getSpacecraftBoresightInterceptPosition(history, instrumentName, scPos, time);
		if (boresightInterceptPosition == null) return 0;
		double[] sunPos = history.getLocationProvider().getSunPosition();
		double[] sunBoresightInterceptVector = new double[3];
		MathUtil.vsub(sunPos, boresightInterceptPosition, sunBoresightInterceptVector);
		return Math.toDegrees(Vector3D.angle(new Vector3D(boresightInterceptPosition), new Vector3D(sunBoresightInterceptVector)));
	}

	public static double getEmissionAngle(StateHistory history, String instrumentName, double[] scPos, double time)
	{
		if (scPos == null)
			scPos = history.getLocationProvider().getSpacecraftPosition();
		double[] boresightInterceptPosition = getSpacecraftBoresightInterceptPosition(history, instrumentName, scPos, time);
		if (boresightInterceptPosition == null) return 0;
//		double[] scPos = history.getLocationProvider().getSpacecraftPosition();
		double[] scBoresightInterceptVector = new double[3];
		MathUtil.vsub(scPos, boresightInterceptPosition, scBoresightInterceptVector);
		return Math.toDegrees(Vector3D.angle(new Vector3D(boresightInterceptPosition), new Vector3D(scBoresightInterceptVector)));
	}

	public static double getPhaseAngle(StateHistory history, String instrumentName, double[] scPos, double time)
	{
		if (scPos == null)
			scPos = history.getLocationProvider().getSpacecraftPosition();
		return getIncidenceAngle(history, instrumentName, scPos, time) + getEmissionAngle(history, instrumentName, scPos, time);
	}

	public static double getSpacecraftRange(StateHistory history, String instrumentName, double[] scPos, double time)
	{
		if (scPos == null)
			scPos = history.getLocationProvider().getSpacecraftPosition();
		double[] rangeVector = new double[3];
		double[] boresightInterceptPosition = getSpacecraftBoresightInterceptPosition(history, instrumentName, scPos, time);
		if (boresightInterceptPosition == null) return 0;
		MathUtil.vsub(scPos, boresightInterceptPosition, rangeVector);
		double range = MathUtil.vnorm(rangeVector);
		return range;
	}

	public static double getSpacecraftDistance(StateHistory history, double time)
	{
		return new Vector3D(history.getLocationProvider().getSpacecraftPositionAtTime(time)).getNorm();
	}

	@Override
	public void updateSpacecraftPosition(StateHistory history, double time, SpacecraftBody spacecraft, SpacecraftDirectionMarker scDirectionMarker,
			SpacecraftLabel spacecraftLabelActor)
	{
//		Logger.getAnonymousLogger ().log(Level.INFO, "Updating sc pos at time " + time + " at history step " + TimeUtil.et2str(history.getMetadata().getCurrentTime()));
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
		IStateHistoryMetadata metadata = history.getMetadata();
		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();
		State state = locationProvider.getCurrentState();
		if (state == null) return;
		state.setEphemerisTime(history.getMetadata().getCurrentTime());
		double[][] scAxes = state.getSpacecraftAxes();
		double[] xaxis = scAxes[0];
		double[] yaxis = scAxes[1];
		double[] zaxis = scAxes[2];
		// set body orientation matrix
		for (int i = 0; i < 3; i++)
		{
			if (xaxis == null) continue;
			spacecraftBodyMatrix.SetElement(i, 0, xaxis[i]);
			spacecraftBodyMatrix.SetElement(i, 1, yaxis[i]);
			spacecraftBodyMatrix.SetElement(i, 2, zaxis[i]);
		}

		// create the icon matrix, which is just the body matrix scaled by a
		// factor
		for (int i = 0; i < 3; i++)
			spacecraftIconMatrix.SetElement(i, i, iconScale);
		spacecraftIconMatrix.Multiply4x4(spacecraftIconMatrix, spacecraftBodyMatrix, spacecraftIconMatrix);
		spacecraftPosition = locationProvider.getSpacecraftPosition();
//		System.out.println("StateHistoryPositionCalculator: updateSpacecraftPosition: scpos " + spacecraftPosition);
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
		spacecraft.setUserMatrix(spacecraftIconMatrix);
		spacecraft.setLabelPosition(spacecraftPosition);
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(2);
		if (spacecraft.isLabelVisible() && history.getLocationProvider().getPointingProvider() != null)
		{
			String rangeString = "Range: " + formatter.format(getSpacecraftRange(history, locationProvider.getPointingProvider().getCurrentInstFrameName(), spacecraftPosition, metadata.getCurrentTime())) + " km";
			String distString = "Dist: " + formatter.format(getSpacecraftDistance(history, metadata.getCurrentTime())) + " km";
			spacecraft.setLabel(distString);
		}
//		spacecraftLabelActor.SetAttachmentPoint(spacecraftPosition);
//		spacecraftLabelActor.setDistanceText(history.getCurrentState(), spacecraftPosition, smallBodyModel);

		scDirectionMarker.setUserTransform(spacecraftMarkerTransform);
		scDirectionMarker.setLabelPosition(spacecraftMarkerPosition);
		spacecraft.getActor().forEach(item -> item.Modified());
		scDirectionMarker.getActor().forEach(item -> item.Modified());
		spacecraftLabelActor.Modified();
	}

	public void updateFOVLocations(StateHistory history, ArrayList<PerspectiveImageFrustum> fov)
	{
		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();
		fov.forEach(fieldOfView ->
		{
			if (fieldOfView.getFrustumActor().GetVisibility() == 0) return;
			FrameID instrumentFrameID = new SimpleFrameID(fieldOfView.getInstrumentName());
			UnwritableVectorIJK frus1Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 0);
			UnwritableVectorIJK frus2Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 1);
			UnwritableVectorIJK frus3Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 2);
			UnwritableVectorIJK frus4Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 3);
			if (frus1Vec.equals(new UnwritableVectorIJK(0, 0, 0)) || frus2Vec.equals(new UnwritableVectorIJK(0, 0, 0)) || frus3Vec.equals(new UnwritableVectorIJK(0, 0, 0)) || frus4Vec.equals(new UnwritableVectorIJK(0, 0, 0)))
			{
				return;
			}
			double[] frus1 = new double[] { frus1Vec.getI(), frus1Vec.getJ(), frus1Vec.getK()};
			double[] frus2 = new double[] { frus2Vec.getI(), frus2Vec.getJ(), frus2Vec.getK()};
			double[] frus3 = new double[] { frus3Vec.getI(), frus3Vec.getJ(), frus3Vec.getK()};
			double[] frus4 = new double[] { frus4Vec.getI(), frus4Vec.getJ(), frus4Vec.getK()};

			fieldOfView.updatePointing(spacecraftPosition, frus1, frus2, frus3, frus4);
			fieldOfView.getFrustumActor().Modified();
		});
	}

	public void updateFootprintLocations(StateHistory history, ArrayList<PerspectiveImageFootprint> footprint)
	{
		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();
		if (footprint != null)
			footprint.stream().filter(fprint -> fprint != null).forEach(fprint -> fprint.setSmallBodyModel(smallBodyModel));


			footprint.stream().filter(fprint -> fprint != null).forEach(fprint ->
			{
				if (fprint.isVisible() == false) return;
				FrameID instrumentFrameID = new SimpleFrameID(fprint.getInstrumentName());
				UnwritableVectorIJK frus1Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 0);
				UnwritableVectorIJK frus2Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 1);
				UnwritableVectorIJK frus3Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 2);
				UnwritableVectorIJK frus4Vec = locationProvider.getFrustum(instrumentFrameID.getName(), 3);
				if (frus1Vec.equals(new UnwritableVectorIJK(0, 0, 0)) || frus2Vec.equals(new UnwritableVectorIJK(0, 0, 0)) || frus3Vec.equals(new UnwritableVectorIJK(0, 0, 0)) || frus4Vec.equals(new UnwritableVectorIJK(0, 0, 0)))
				{
					return;
				}

				double[] frus1 = new double[] { frus1Vec.getI(), frus1Vec.getJ(), frus1Vec.getK()};
				double[] frus2 = new double[] { frus2Vec.getI(), frus2Vec.getJ(), frus2Vec.getK()};
				double[] frus3 = new double[] { frus3Vec.getI(), frus3Vec.getJ(), frus3Vec.getK()};
				double[] frus4 = new double[] { frus4Vec.getI(), frus4Vec.getJ(), frus4Vec.getK()};
				fprint.updatePointing(spacecraftPosition, frus1, frus2, frus3, frus4, 1024, 1024, 1);
				fprint.getFootprintActor().Modified();
				fprint.getFootprintBoundaryActor().Modified();
			});

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
//		System.out.println("StateHistoryPositionCalculator: updateLookDirection: scpos " + spacecraftPosition);
		return currentLookFromDirection;
	}

	/**
	 * @return the currentLookFromDirection
	 */
	public double[] getCurrentLookFromDirection()
	{
		return currentLookFromDirection;
	}
}