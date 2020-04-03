package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;

import vtk.vtkProp;
import vtk.vtkScalarBarActor;

import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryPositionCalculator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftFieldOfView;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.text.SpacecraftLabel;
import edu.jhuapl.sbmt.stateHistory.rendering.text.StatusBarTextActor;
import edu.jhuapl.sbmt.stateHistory.rendering.text.TimeBarTextActor;

public class StateHistoryRendererManager
{
	/**
	*
	*/
	private HashMap<StateHistory, TrajectoryActor> stateHistoryToRendererMap = new HashMap<StateHistory, TrajectoryActor>();

	/**
	*
	*/
	private SpacecraftBody spacecraft;

	// Text Actors
	/**
	*
	*/
	private TimeBarTextActor timeBarActor;

	/**
	*
	*/
	private StatusBarTextActor statusBarTextActor;

	/**
	*
	*/
	private vtkScalarBarActor scalarBarActor;

	/**
	 *
	 */
	private SpacecraftLabel spacecraftLabelActor;

	// FOV Actors
	/**
	 *
	 */
	private SpacecraftFieldOfView spacecraftFov;

	// Direction markers
	/**
	*
	*/
	private SpacecraftDirectionMarker scDirectionMarker;

	/**
	 *
	 */
	private SunDirectionMarker sunDirectionMarker;

	/**
	 *
	 */
	private EarthDirectionMarker earthDirectionMarker;

	/**
	 *
	 */
	private PropertyChangeSupport pcs;

	/**
	 *
	 */
	double markerRadius, markerHeight;

    /**
     *
     */
	private double scalingFactor = 0.0;


	private IStateHistoryPositionCalculator positionCalculator;


	public StateHistoryRendererManager(SmallBodyModel smallBodyModel, PropertyChangeSupport pcs)
	{
		this.positionCalculator = new StateHistoryPositionCalculator(smallBodyModel);
		BoundingBox bb = smallBodyModel.getBoundingBox();

		double width = Math.max((bb.xmax - bb.xmin), Math.max((bb.ymax - bb.ymin), (bb.zmax - bb.zmin)));
		scalingFactor = 30.62*width + -0.0002237;
		markerRadius = 0.02 * width;
		markerHeight = markerRadius * 3.0;
		this.spacecraftLabelActor = new SpacecraftLabel();
		this.spacecraftLabelActor.VisibilityOff();

		this.spacecraft = new SpacecraftBody(ConvertResourceToFile.convertResourceToRealFile(this,
				"/edu/jhuapl/sbmt/data/cassini-9k.stl", Configuration.getApplicationDataDir()).getAbsolutePath());
		this.spacecraft.getActor().VisibilityOff();

		this.scDirectionMarker = new SpacecraftDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
		this.scDirectionMarker.getActor().VisibilityOff();

		this.earthDirectionMarker = new EarthDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
		this.earthDirectionMarker.getActor().VisibilityOff();

		this.sunDirectionMarker = new SunDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
		this.sunDirectionMarker.getActor().VisibilityOff();

		this.timeBarActor = new TimeBarTextActor();

		this.statusBarTextActor = new StatusBarTextActor();

		this.pcs = pcs;
	}

	/**
	 * @param run
	 * @return
	 */
	public TrajectoryActor addRun(StateHistory run)
	{
		if (stateHistoryToRendererMap.get(run) != null)
		{
			TrajectoryActor trajActor = stateHistoryToRendererMap.get(run);
			trajActor.SetVisibility(1);
			return trajActor;
		}

		// Get the trajectory actor the state history segment
		TrajectoryActor trajectoryActor = new TrajectoryActor(run.getTrajectory());
		stateHistoryToRendererMap.put(run, trajectoryActor);

		trajectoryActor.setColoringFunction(StateHistoryColoringFunctions.PER_TABLE.getColoringFunction(),
				Colormaps.getNewInstanceOfBuiltInColormap("Rainbow"));

		trajectoryActor.setMinMaxFraction(run.getMinDisplayFraction(), run.getMaxDisplayFraction());
		trajectoryActor.VisibilityOn();
		trajectoryActor.GetMapper().Update();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajectoryActor);

		return trajectoryActor;
	}

	/**
	 * @param key
	 */
	public void removeRun(StateHistory run)
	{
		stateHistoryToRendererMap.remove(run);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, run);
	}

	/**
	 * @param segment
	 * @return
	 */
	public boolean isStateHistoryMapped(StateHistory segment)
	{
		return stateHistoryToRendererMap.get(segment) != null;
	}

	/**
	 * @param segment
	 * @return
	 */
	public boolean getVisibility(StateHistory segment)
	{
		if (isStateHistoryMapped(segment) == false)
			return false;
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		if (renderer == null)
			return false;
		return (renderer.GetVisibility() == 1);
	}

	/**
	 * @param segment
	 * @param visibility
	 */
	public void setVisibility(StateHistory segment, boolean visibility)
	{
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		int isVisible = (visibility == true) ? 1 : 0;
		renderer.SetVisibility(isVisible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
	}

	/**
	 * @param segment
	 * @param color
	 */
	public void setTrajectoryColor(StateHistory segment, Color color)
	{
		double[] colorAsIntArray = new double[]
		{ color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() };
		double[] colorAsDoubleArray = new double[]
		{ color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0 };
		segment.getTrajectory().setTrajectoryColor(colorAsIntArray);
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		renderer.setColoringFunction(null, null);
		renderer.setTrajectoryColor(colorAsDoubleArray);
		refreshColoring(segment);
		// this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null,
		// renderer);
	}

	/**
	 * @param segment
	 */
	public void refreshColoring(StateHistory segment)
	{
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		if (renderer == null)
			return;
		renderer.setTrajectoryColor(segment.getTrajectoryColor());
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
	}

	// time updates
	/**
	 * @param time
	 */
	public void updateTimeBarValue(double time)
	{
		if (timeBarActor == null) return;
		timeBarActor.updateTimeBarValue(time);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

	}

	/**
	 * @param time
	 */
	public void updateTimeBarLocation(int width, int height)
	{
		if (timeBarActor == null) return;
		timeBarActor.updateTimeBarPosition(width, height);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

	}

	//status bar updates
	/**
	 * @param time
	 */
	public void updateStatusBarValue(String text)
	{
		if (statusBarTextActor == null) return;
		statusBarTextActor.updateStatusBarValue(text);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

	}

	/**
	 * @param time
	 */
	public void updateStatusBarLocation(int width, int height)
	{
		if (statusBarTextActor == null) return;
		statusBarTextActor.updateStatusBarPosition(width, height);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	*
	*/
	public ArrayList<vtkProp> getProps()
	{
		ArrayList<vtkProp> props = new ArrayList<vtkProp>();
		for (StateHistory history : stateHistoryToRendererMap.keySet())
			props.add(stateHistoryToRendererMap.get(history));

		props.add(spacecraft.getActor());
		props.add(scDirectionMarker.getActor());
		props.add(spacecraftLabelActor);
		props.add(earthDirectionMarker.getActor());
		props.add(sunDirectionMarker.getActor());
		props.add(timeBarActor);
		props.add(statusBarTextActor);

		return props;

		// TODO fix
		// if (currentRun != null)
		// return currentRun.getProps();
		// else
		// return new ArrayList<vtkProp>();
	}

	/**
	 * @param time
	 */
	private void updateLighting(double time)
	{
		// // toggle for lighting - Alex W
		// if (timeFraction >= 0.0 && showLighting)
		// {
		// renderer.setFixedLightDirection(sunDirection);
		// renderer.setLighting(LightingType.FIXEDLIGHT);
		// updateActorVisibility();
		// }
		// else
		// renderer.setLighting(LightingType.LIGHT_KIT);
	}

	/**
	*
	*/
	public void setTimeFraction(Double timeFraction, StateHistory state)
	{
		// StateHistory state = getCurrentRun();
		if (state != null && spacecraft.getActor() != null)
		{
			positionCalculator.updateSpacecraftPosition(state, timeFraction, spacecraft, scDirectionMarker, spacecraftLabelActor);
			positionCalculator.updateEarthPosition(state, timeFraction, earthDirectionMarker);
			positionCalculator.updateSunPosition(state, timeFraction, sunDirectionMarker);
			this.pcs.firePropertyChange("POSITION_CHANGED", null, null);
		}
	}

	/**
	 * @param spacecraft
	 */
	public void setSpacecraft(SpacecraftBody spacecraft)
	{
		this.spacecraft = spacecraft;
	}

	/**
	 * @param segment
	 * @return
	 */
	public TrajectoryActor getTrajectoryActorForStateHistory(StateHistory segment)
	{
		return stateHistoryToRendererMap.get(segment);
	}

	/**
	 * @param color
	 */
	public void setSpacecraftColor(Color color)
	{
		spacecraft.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftVisibility(boolean visible)
	{
		spacecraft.getActor().SetVisibility(visible == true ? 1 : 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftLabelVisibility(boolean visible)
	{
		spacecraftLabelActor.SetVisibility(visible == true ? 1 : 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftDirectionMarkerVisibility(boolean visible)
	{
		scDirectionMarker.getActor().SetVisibility(visible == true ? 1 : 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
	}

	/**
	 * @param radius
	 */
	public void setSpacecraftDirectionMarkerSize(int radius)
	{
		scDirectionMarker.setPointerSize(radius);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
	}

	/**
	 * @param visible
	 */
	public void setEarthDirectionMarkerVisibility(boolean visible)
	{
		earthDirectionMarker.getActor().SetVisibility(visible == true ? 1 : 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
	}

	/**
	 * @param scale
	 */
	public void setSpacecraftSize(double scale)
	{
		spacecraft.setScale(scale);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
	}

	/**
	 * @param radius
	 */
	public void setEarthDirectionMarkerSize(int radius)
	{
		earthDirectionMarker.setPointerSize(radius);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
	}

	/**
	 * @param visible
	 */
	public void setSunDirectionMarkerVisibility(boolean visible)
	{
		sunDirectionMarker.getActor().SetVisibility(visible == true ? 1 : 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
	}

	/**
	 * @param radius
	 */
	public void setSunDirectionMarkerSize(int radius)
	{
		sunDirectionMarker.setPointerSize(radius);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
	}

	/**
	 * @param distanceText
	 */
	public void setDistanceText(String distanceText)
	{
		spacecraftLabelActor.setDistanceString(distanceText);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
	}

	/**
	 * @param distanceTextFont
	 */
	public void setDistanceTextFont(Font distanceTextFont)
	{
		spacecraftLabelActor.setDistanceStringFont(distanceTextFont);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
	}

	/**
	 * @param isVisible
	 */
	public void setDistanceTextVisiblity(boolean isVisible)
	{
		int visible = (isVisible) ? 1 : 0;
		spacecraftLabelActor.SetVisibility(visible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftLabelActor);
	}

	/**
	 * @param color
	 */
	public void setEarthDirectionMarkerColor(Color color)
	{
		earthDirectionMarker.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
	}

	/**
	 * @param color
	 */
	public void setSunDirectionMarkerColor(Color color)
	{
		sunDirectionMarker.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
	}

	/**
	 * @param color
	 */
	public void setScDirectionMarkerColor(Color color)
	{
		scDirectionMarker.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
	}

	/**
	 * @param run
	 * @param min
	 * @param max
	 */
	public void setTrajectoryMinMax(StateHistory run, double min, double max)
	{
		TrajectoryActor trajActor = stateHistoryToRendererMap.get(run);
		if (trajActor != null)
		{
			trajActor.setMinMaxFraction(min, max);
			trajActor.Modified();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajActor);
		}
	}

	public double[] updateLookDirection(RendererLookDirection lookDirection)
	{
		return positionCalculator.updateLookDirection(lookDirection, scalingFactor);
	}

	/**
	 * @return
	 */
	public double[] getCurrentLookFromDirection()
	{
		return positionCalculator.getCurrentLookFromDirection();
	}
}
