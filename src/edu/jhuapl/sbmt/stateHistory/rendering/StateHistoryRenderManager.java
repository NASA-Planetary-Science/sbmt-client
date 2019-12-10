package edu.jhuapl.sbmt.stateHistory.rendering;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import vtk.vtkActor;
import vtk.vtkAssembly;
import vtk.vtkConeSource;
import vtk.vtkMatrix4x4;
import vtk.vtkProp;
import vtk.vtkScalarBarActor;
import vtk.vtkTransform;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.AnimatorFrameRunnable;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.animator.AnimationFrame;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;
import edu.jhuapl.sbmt.stateHistory.rendering.animator.Animator;
import edu.jhuapl.sbmt.stateHistory.ui.AnimationFileDialog;
import edu.jhuapl.sbmt.stateHistory.ui.version2.IStateHistoryPanel;

public class StateHistoryRenderManager extends AbstractModel // implements
																// HasTime
{
	StateHistoryRenderModel renderModel = new StateHistoryRenderModel();

    //Use approximate radius of largest solar system body as scale for surface intercept vector.
    private static final double JupiterScale = 75000;

	private double timeStep;
	private TimeBarTextActor timeBarTextActor;
	private StatusBarTextActor statusBarTextActor;
	private vtkScalarBarActor scalarBarActor;
	private Renderer renderer;

	private Trajectory trajectory;
	private TrajectoryActor trajectoryActor;

	// the 3D representation of the s/c
	private SpacecraftBody spacecraftBody;

	// the cone pointing to where the spacecraft current is
	private SpacecraftDirectionMarker spacecraftMarkerHead;

	private SpacecraftFieldOfView spacecraftFov;

	private SpacecraftLabel spacecraftLabelActor;

	private vtkConeSource earthMarkerHead;
	private vtkActor earthMarkerHeadActor;

	private SunDirectionMarker sunDirectionMarker;
	private SpacecraftDirectionMarker scDirectionMarker;
	private EarthDirectionMarker earthDirectionMarker;
	private vtkAssembly sunAssembly;

	private ArrayList<vtkProp> stateHistoryActors = new ArrayList<vtkProp>();
	private StateHistoryModel historyModel;
	private SmallBodyModel smallBodyModel;
	double scalingFactor;

	private double[] sunDirection;

	public StateHistoryRenderManager(Renderer renderer, StateHistoryModel historyModel)
	{
		this.renderer = renderer;
		this.historyModel = historyModel;
		this.smallBodyModel = historyModel.getSmallBodyModel();
	}

	private void initialize()
	{
//		initialized = true;
		BoundingBox bb = smallBodyModel.getBoundingBox();
		double width = Math.max((bb.xmax - bb.xmin), Math.max((bb.ymax - bb.ymin), (bb.zmax - bb.zmin)));
		scalingFactor = 30.62 * width + -0.0002237;
		if (trajectory == null)
		{
			trajectory = new StandardTrajectory();
		}
		if (trajectoryActor == null)
		{
			trajectoryActor = new TrajectoryActor(trajectory);
		}

		if (spacecraftBody == null)
		{
			try
			{
				createSpacecraftPolyData();
				// spacecraft label
				spacecraftLabelActor = new SpacecraftLabel();
			} catch (NumberFormatException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			renderModel.setShowSpacecraftMarker(false);
			renderModel.setShowSpacecraft(false);
			updateActorVisibility();
		}
		if (statusBarTextActor == null)
		{
			setupStatusBar();
		}

		statusBarTextActor.updateStatusBarPosition(renderer.getPanelWidth(), renderer.getPanelHeight());

		if (timeBarTextActor == null)
			setupTimeBar();
	}

	public ArrayList<vtkProp> getProps()
	{
		return stateHistoryActors;
	}

	private void initializeSpacecraftBody(File modelFile)
	{
		spacecraftBody = new SpacecraftBody(modelFile.getAbsolutePath());
	}

	private void createSpacecraftPolyData()
	{
		String spacecraftFileName = "/edu/jhuapl/sbmt/data/cassini-9k.stl";
		initializeSpacecraftBody(ConvertResourceToFile.convertResourceToRealFile(this, spacecraftFileName,
				Configuration.getApplicationDataDir()));

		spacecraftFov = new SpacecraftFieldOfView();

		// Scale subsolar and subearth point markers to body size
		BoundingBox bb = smallBodyModel.getBoundingBox();
		double width = Math.max((bb.xmax - bb.xmin), Math.max((bb.ymax - bb.ymin), (bb.zmax - bb.zmin)));
		renderModel.setMarkerRadius(0.02 * width);
		renderModel.setMarkerHeight(renderModel.getMarkerRadius() * 3.0);


		earthDirectionMarker = new EarthDirectionMarker(renderModel.getMarkerRadius() / 16.0, renderModel.getMarkerHeight(), 0, 0, 0);
		sunDirectionMarker = new SunDirectionMarker(renderModel.getMarkerRadius() / 16.0, renderModel.getMarkerHeight(), 0, 0, 0);
		scDirectionMarker = new SpacecraftDirectionMarker(renderModel.getMarkerRadius() / 16.0, renderModel.getMarkerHeight(), 0, 0, 0);
	}

	public void setTimeFraction(Double timeFraction)
	{
		StateHistory currentFlybyStateHistory = historyModel.getCurrentFlybyStateHistory();
		if (currentFlybyStateHistory != null && spacecraftBody.getActor() != null)
		{
			// set the time
			currentFlybyStateHistory.setTimeFraction(timeFraction);
			historyModel.setTime(currentFlybyStateHistory.getTime());

			if (timeBarTextActor != null)
			{
				timeBarTextActor.updateTimeBarPosition(renderer.getPanelWidth(), renderer.getPanelHeight());
				timeBarTextActor.updateTimeBarValue(historyModel.getTime());
			}

			// get the current FlybyState
			State state = currentFlybyStateHistory.getCurrentValue();
			double[] spacecraftPosition = currentFlybyStateHistory.getSpacecraftPosition();

			double[] sunPosition = currentFlybyStateHistory.getSunPosition();
			double[] sunMarkerPosition = new double[3];
			sunDirection = new double[3];
			double[] sunViewpoint = new double[3];
			double[] sunViewDirection = new double[3];
			MathUtil.unorm(sunPosition, sunDirection);
			MathUtil.vscl(JupiterScale, sunDirection, sunViewpoint);
			MathUtil.vscl(-1.0, sunDirection, sunViewDirection);
			int result = smallBodyModel.computeRayIntersection(sunViewpoint, sunViewDirection, sunMarkerPosition);

			// toggle for lighting - Alex W
			if (timeFraction >= 0.0 && renderModel.isShowLighting())
			{
				renderer.setFixedLightDirection(sunDirection);
				renderer.setLighting(LightingType.FIXEDLIGHT);
				updateActorVisibility();
			} else
				renderer.setLighting(LightingType.LIGHT_KIT);

			double[] earthPosition = currentFlybyStateHistory.getEarthPosition();
			double[] earthMarkerPosition = new double[3];
			double[] earthDirection = new double[3];
			double[] earthViewpoint = new double[3];
			double[] earthViewDirection = new double[3];
			MathUtil.unorm(earthPosition, earthDirection);
			MathUtil.vscl(JupiterScale, earthDirection, earthViewpoint);
			MathUtil.vscl(-1.0, earthDirection, earthViewDirection);
			result = smallBodyModel.computeRayIntersection(earthViewpoint, earthViewDirection, earthMarkerPosition);

			// double[] spacecraftPosition =
			// currentFlybyStateHistory.getSpacecraftPosition();
			double[] spacecraftMarkerPosition = new double[3];
			double[] spacecraftDirection = new double[3];
			double[] spacecraftViewpoint = new double[3];
			double[] spacecraftViewDirection = new double[3];
			MathUtil.unorm(spacecraftPosition, spacecraftDirection);
			MathUtil.vscl(JupiterScale, spacecraftDirection, spacecraftViewpoint);
			MathUtil.vscl(-1.0, spacecraftDirection, spacecraftViewDirection);
			result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection,
					spacecraftMarkerPosition);

			// rotates sun pointer to point in direction of sun - Alex W
			double[] zAxis =
			{ 1, 0, 0 };
			double[] sunPos = currentFlybyStateHistory.getSunPosition();
			sunDirectionMarker.updateSunPosition(sunPos);
			// double[] sunPosDirection = new double[3];
			// MathUtil.unorm(sunPos, sunPosDirection);
			// double[] rotationAxisSun = new double[3];
			// MathUtil.vcrss(sunPosDirection, zAxis, rotationAxisSun);
			// double rotationAngleSun = ((180.0/Math.PI)*MathUtil.vsep(zAxis,
			// sunPosDirection));

			// vtkTransform sunMarkerTransform = new vtkTransform();
			// //sunMarkerTransform.PostMultiply();
			// sunMarkerTransform.Translate(sunMarkerPosition);
			// sunMarkerTransform.RotateWXYZ(-rotationAngleSun,
			// rotationAxisSun[0], rotationAxisSun[1], rotationAxisSun[2]);
			// sunAssembly.SetUserTransform(sunMarkerTransform);

			// rotates earth pointer to point in direction of earth - Alex W
			double[] earthPos = currentFlybyStateHistory.getEarthPosition();
			double[] earthPosDirection = new double[3];
			MathUtil.unorm(earthPos, earthPosDirection);
			double[] rotationAxisEarth = new double[3];
			MathUtil.vcrss(earthPosDirection, zAxis, rotationAxisEarth);

			double rotationAngleEarth = ((180.0 / Math.PI) * MathUtil.vsep(zAxis, earthPosDirection));

			vtkTransform earthMarkerTransform = new vtkTransform();
			earthMarkerTransform.Translate(earthMarkerPosition);
			earthMarkerTransform.RotateWXYZ(-rotationAngleEarth, rotationAxisEarth[0], rotationAxisEarth[1],
					rotationAxisEarth[2]);
			earthMarkerHeadActor.SetUserTransform(earthMarkerTransform);

			// rotates spacecraft pointer to point in direction of spacecraft -
			// Alex W
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
			// spacecraftMarkerHeadActor.SetUserTransform(spacecraftMarkerTransform);

			// set camera to earth, spacecraft, or sun views - Alex W
			if (renderModel.isShowEarthView())
			{
				double[] focalpoint =
				{ 0, 0, 0 };
				double[] upVector =
				{ 0, 1, 0 };
				double[] newEarthPos = new double[3];
				MathUtil.unorm(earthPos, newEarthPos);
				MathUtil.vscl(scalingFactor, newEarthPos, newEarthPos);
				renderer.setCameraOrientation(newEarthPos, renderer.getCameraFocalPoint(), upVector,
						renderer.getCameraViewAngle());
			} else if (renderModel.isMove())
			{
				double[] focalpoint =
				{ 0, 0, 0 };
				double[] upVector =
				{ 0, 1, 0 };
				renderer.setCameraOrientation(currentFlybyStateHistory.getSpacecraftPosition(),
						renderer.getCameraFocalPoint(), upVector, renderer.getCameraViewAngle());
			} else if (renderModel.isShowSunView())
			{
				double[] focalpoint =
				{ 0, 0, 0 };
				double[] upVector =
				{ 0, 1, 0 };
				double[] newSunPos = new double[3];
				MathUtil.unorm(sunPos, newSunPos);
				MathUtil.vscl(scalingFactor, newSunPos, newSunPos);
				renderer.setCameraOrientation(newSunPos, renderer.getCameraFocalPoint(), upVector,
						renderer.getCameraViewAngle());
			}

			double velocity[] = state.getSpacecraftVelocity();

			double speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1] + velocity[2] * velocity[2]);

			double radius = Math.sqrt(spacecraftPosition[0] * spacecraftPosition[0]
					+ spacecraftPosition[1] * spacecraftPosition[1] + spacecraftPosition[2] * spacecraftPosition[2]);
			result = smallBodyModel.computeRayIntersection(spacecraftViewpoint, spacecraftViewDirection,
					spacecraftMarkerPosition);
			double smallBodyRadius = Math.sqrt(spacecraftMarkerPosition[0] * spacecraftMarkerPosition[0]
					+ spacecraftMarkerPosition[1] * spacecraftMarkerPosition[1]
					+ spacecraftMarkerPosition[2] * spacecraftMarkerPosition[2]);
			// if(distanceOption==1)
			// {
			// radius = radius - smallBodyRadius;
			// }

			String speedText = String.format("%7.1f km %7.3f km/sec   .", radius, speed);

			// set the current orientation
			double[] xaxis = state.getSpacecraftXAxis();
			double[] yaxis = state.getSpacecraftYAxis();
			double[] zaxis = state.getSpacecraftZAxis();

			// create spacecraft matrices
			vtkMatrix4x4 spacecraftBodyMatrix = new vtkMatrix4x4();
			vtkMatrix4x4 spacecraftIconMatrix = new vtkMatrix4x4();

			vtkMatrix4x4 fovMatrix = new vtkMatrix4x4();
			vtkMatrix4x4 fovRotateXMatrix = new vtkMatrix4x4();
			vtkMatrix4x4 fovRotateYMatrix = new vtkMatrix4x4();
			vtkMatrix4x4 fovRotateZMatrix = new vtkMatrix4x4();
			vtkMatrix4x4 fovScaleMatrix = new vtkMatrix4x4();

			vtkMatrix4x4 sunMarkerMatrix = new vtkMatrix4x4();
			vtkMatrix4x4 earthMarkerMatrix = new vtkMatrix4x4();

			vtkMatrix4x4 spacecraftInstrumentMatrix = new vtkMatrix4x4();

			// set to identity
			spacecraftBodyMatrix.Identity();
			spacecraftIconMatrix.Identity();
			fovMatrix.Identity();
			fovRotateXMatrix.Identity();
			fovRotateYMatrix.Identity();
			fovRotateZMatrix.Identity();
			sunMarkerMatrix.Identity();
			earthMarkerMatrix.Identity();
			// fovMatrix = new vtkMatrix4x4

			// set body orientation matrix
			for (int i = 0; i < 3; i++)
			{
				spacecraftBodyMatrix.SetElement(i, 0, xaxis[i]);
				spacecraftBodyMatrix.SetElement(i, 1, yaxis[i]);
				spacecraftBodyMatrix.SetElement(i, 2, zaxis[i]);
			}

			// create the icon matrix, which is just the body matrix scaled by a
			// factor
			for (int i = 0; i < 3; i++)
				spacecraftIconMatrix.SetElement(i, i, renderModel.getIconScale());
			spacecraftIconMatrix.Multiply4x4(spacecraftIconMatrix, spacecraftBodyMatrix, spacecraftIconMatrix);

			// set translation
			for (int i = 0; i < 3; i++)
			{
				spacecraftBodyMatrix.SetElement(i, 3, spacecraftPosition[i]);
				spacecraftIconMatrix.SetElement(i, 3, spacecraftPosition[i]);
				fovMatrix.SetElement(i, 3, spacecraftPosition[i]);

				sunMarkerMatrix.SetElement(i, 3, sunMarkerPosition[i]);
				earthMarkerMatrix.SetElement(i, 3, earthMarkerPosition[i]);
			}

			// spacecraftBodyActor.SetUserMatrix(spacecraftIconMatrix);

			spacecraftLabelActor.SetAttachmentPoint(spacecraftPosition);
			spacecraftLabelActor.SetCaption(speedText);

			// spacecraftFovActor.SetUserMatrix(fovMatrix);

			// spacecraftMarkerActor.SetUserMatrix(spacecraftBodyMatrix);
			// earthMarkerActor.SetUserMatrix(earthMarkerMatrix);
			spacecraftBody.Modified();
			spacecraftFov.Modified();
			// spacecraftMarkerBody.Modified();
			earthMarkerHead.Modified();
			// earthMarkerBody.Modified();
			// sunAssembly.Modified();
			sunDirectionMarker.Modified();

			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	// returns renderer - Alex W
	public Renderer getRenderer()
	{
		return renderer;
	}

	// sets view angle - Alex W
	public void setViewAngle(double angle)
	{
		renderer.setCameraViewAngle(angle);
	}

	// starts the process for creating the movie frames
	public void saveAnimation(IStateHistoryPanel panel, String start, String end)
	{
		AnimationFileDialog dialog = new AnimationFileDialog(start, end);
		int result = dialog.showSaveDialog(panel.getView());

		if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION)
		{
			return;
		}

		File file = dialog.getSelectedFile();

		int frameNum = (Integer) dialog.getNumFrames().getValue();
		timeStep = 1.0 / (double) frameNum;

		Animator animator = new Animator(renderer);
		animator.saveAnimation(frameNum, file, new AnimatorFrameRunnable()
		{
			@Override
			public void run(AnimationFrame frame)
			{
				// TODO Auto-generated method stub
				super.run(frame);
				run();
			}

			@Override
			public void run()
			{
				setTimeFraction(getFrame().timeFraction);
				getFrame().panel.setTimeSlider(getFrame().timeFraction);
			}
		});

	}

	// updated method so that it is togglable for pointers, trajectory etc. -
	// Alex W
	public void updateActorVisibility()
	{
		stateHistoryActors.clear();
		initialize();

//		if (visible)
		{
			stateHistoryActors.add(trajectoryActor);
		}

		if (renderModel.isShowSpacecraftBody())
			stateHistoryActors.add(spacecraftBody.getActor());
		if (renderModel.isShowSpacecraftLabel())
			stateHistoryActors.add(spacecraftLabelActor);
		if (renderModel.isShowSpacecraftFov())
			stateHistoryActors.add(spacecraftFov.getActor());
		if (renderModel.isShowEarthMarker() && !(historyModel.getTime() == 0.0))
			stateHistoryActors.add(earthMarkerHeadActor);
		if (renderModel.isShowSunMarker() && !(historyModel.getTime() == 0.0))
			stateHistoryActors.add(sunDirectionMarker.getActor());
		if (renderModel.isShowSpacecraftMarker())
			stateHistoryActors.add(spacecraftMarkerHead.getActor());

		if (renderModel.isShowTimeBar())
		{
			stateHistoryActors.add(timeBarTextActor);
		}

		// displays extra messages at the bottom of the display
		stateHistoryActors.add(statusBarTextActor);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setTrajectoryLineThickness(double value)
	{
		trajectoryActor.setTrajectoryLineThickness(value);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setTrajectoryColor(double[] color)
	{
		this.trajectoryActor.setTrajectoryColor(color);
		// recreate poly data with new color
		// if (showing) {
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		// }
	}

	public void updateTimeBarPosition(int windowWidth, int windowHeight)
	{
		this.timeBarTextActor.updateTimeBarPosition(windowWidth, windowHeight);
	}

	public void updateTimeBarValue(double time)
	{
		this.timeBarTextActor.updateTimeBarValue(time);
	}

	public void updateStatusBarPosition(int windowWidth, int windowHeight)
	{
		this.statusBarTextActor.updateStatusBarPosition(windowWidth, windowHeight);
	}

	public void updateStatusBarValue(String text)
	{
		this.statusBarTextActor.updateStatusBarValue(text);
	}

	public void setSpacecraftPointerSize(int size)
	{
		this.spacecraftMarkerHead.setSpacecraftPointerSize(size);
	}

	public void setSunPointerSize(int size)
	{
		this.sunDirectionMarker.setSunPointerSize(size);
	}

	public void setEarthPointerSize(int size)
	{
		this.earthDirectionMarker.setEarthPointerSize(size);
	}

	private void setupStatusBar()
	{
		statusBarTextActor = new StatusBarTextActor();
		stateHistoryActors.add(statusBarTextActor);

		statusBarTextActor.GetTextProperty().SetColor(1.0, 1.0, 1.0);
		statusBarTextActor.GetTextProperty().SetJustificationToCentered();
		statusBarTextActor.GetTextProperty().BoldOn();

		statusBarTextActor.VisibilityOn();
	}

	private void setupTimeBar()
	{
//		timeBarTextActor = new TimeBarTextActor();
//		stateHistoryActors.add(timeBarTextActor);
//		showTimeBar = Preferences.getInstance().getAsBoolean(Preferences.SHOW_SCALE_BAR, true);
	}

	// sets the renderer to move along the spacecraft trajectory - Alex W
    public void setSpacecraftMovement(boolean move)
    {
//        renderModel.setMove(move);
//        if(move)
//        {
//            double[] focalpoint = {0,0,0};
//            double[] upVector = {0,1,0};
//            renderer.setCameraOrientation(currentFlybyStateHistory.getSpacecraftPosition(), renderer.getCameraFocalPoint(), upVector, 30);
//        }
    }

 // sets the renderer to the earth position and plays the animation with camera fixed to earth - Alex W
    public void setEarthView(boolean move, boolean showSpacecraft)
    {
//        renderModel.setEarthView(move);
//        if(renderModel.isEarthView())
//        {
//            double[] focalpoint = {0,0,0};
//            double[] upVector = {0,1,0};
//            double[] earthPos = currentFlybyStateHistory.getEarthPosition();
//            double[] newEarthPos = new double[3];
//            MathUtil.unorm(earthPos, newEarthPos);
//            MathUtil.vscl(scalingFactor, newEarthPos, newEarthPos);
//            renderer.setCameraOrientation(newEarthPos, renderer.getCameraFocalPoint(), upVector, 5);
//        }
//        if (showSpacecraft) {
//            this.setActorVisibility("Spacecraft", true);
//        }
    }

    // sets the renderer to the sun position and plays the animation with camera fixed to sun - Alex W
    public void setSunView(boolean move, boolean showSpacecraft)
    {
//        renderModel.setSunView(move);
//        if(renderModel.isSunView())
//        {
//            double[] focalpoint = {0,0,0};
//            double[] upVector = {0,1,0};
//            double[] sunPos = currentFlybyStateHistory.getSunPosition();
//            double[] newSunPos = new double[3];
//            MathUtil.unorm(sunPos, newSunPos);
//            MathUtil.vscl(scalingFactor, newSunPos, newSunPos);
//            renderer.setCameraOrientation(newSunPos, renderer.getCameraFocalPoint(), upVector, 5);
//        }
//        if (showSpacecraft) {
//            this.setActorVisibility("Spacecraft", true);
//        }
    }


}
