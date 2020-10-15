package edu.jhuapl.sbmt.stateHistory.rendering.model;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import vtk.vtkProp;
import vtk.vtkScalarBarActor;

import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFrustum;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.text.SpacecraftLabel;
import edu.jhuapl.sbmt.stateHistory.rendering.text.StatusBarTextActor;
import edu.jhuapl.sbmt.stateHistory.rendering.text.TimeBarTextActor;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.GroupColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryFeatureType;

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
	private PerspectiveImageFrustum[] spacecraftFov;

	private PerspectiveImageFootprint[] footprint;

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

	private double diagonalLength;

	private HashMap<String, PerspectiveImageFootprint> instrumentNameToFootprintMap = new HashMap<String, PerspectiveImageFootprint>();
	private HashMap<String, PerspectiveImageFrustum> instrumentNameToFovMap = new HashMap<String, PerspectiveImageFrustum>();

	private boolean displayFootprints = true, displayBoundaries = true, displayFrusta = true;
	private Vector<String> selectedFOVs = new Vector<String>();

	private List<vtkProp> plannedScienceActors = new ArrayList<vtkProp>();

//	private GroupColorProvider sourceGCP;
//
//	private double begPercent;
//	private double endPercent;
//	private final BodyViewConfig refBodyViewConfig;
//
//	private GroupColorProvider colorProvider;


	private IStateHistoryPositionCalculator positionCalculator;


	public StateHistoryRendererManager(SmallBodyModel smallBodyModel, PropertyChangeSupport pcs)
	{
		this.positionCalculator = new StateHistoryPositionCalculator(smallBodyModel);
		BoundingBox bb = smallBodyModel.getBoundingBox();
		diagonalLength = smallBodyModel.getBoundingBoxDiagonalLength();
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

		footprint = new PerspectiveImageFootprint[] {};
		spacecraftFov = new PerspectiveImageFrustum[] {};
		this.pcs = pcs;

//		refBodyViewConfig = (SmallBodyViewConfig) smallBodyModel.getSmallBodyConfig();
//		sourceGCP = new ConstGroupColorProvider(new ConstColorProvider(Color.GREEN));
//		begPercent = 0.0;
//		endPercent = 1.0;
//		propM = new HashMap<>();
//		vAuxM = new HashMap<>();
//		vPainterM = new HashMap<>();
//		vActorToPainterM = new HashMap<>();
	}

	public void addPlannedScienceActors(List<vtkProp> actors)
	{
		plannedScienceActors.addAll(actors);
	}

	public void clearPlannedScience()
	{
		plannedScienceActors.clear();
	}

	public Vector<DisplayableItem> getDisplayableItems()
	{
		Vector<DisplayableItem> items = new Vector<DisplayableItem>();
		items.add(earthDirectionMarker);
		items.add(sunDirectionMarker);
		items.add(scDirectionMarker);
		items.add(spacecraft);

		return items;
	}

	/**
	 * @param run
	 * @return
	 */
	public TrajectoryActor addRun(StateHistory run)
	{
		updateFovs(run);
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

		trajectoryActor.setMinMaxFraction(run.getTrajectory().getMinDisplayFraction(), run.getTrajectory().getMaxDisplayFraction());
		trajectoryActor.VisibilityOn();
		trajectoryActor.GetMapper().Update();



		boolean instrumentPointingAvailable = run.getTrajectory().isHasInstrumentPointingInfo();
//		Arrays.stream(this.spacecraftFov).filter(fov -> fov.getFrustumActor() != null).forEach(fov -> {
////			fov.getFrustumActor().SetVisibility(instrumentPointingAvailable ? 1 : 0);
//		});

		Arrays.stream(this.footprint).filter(fprint -> fprint != null).filter(footprint -> footprint.getFootprintActor() != null).forEach(footprint -> {
//			footprint.getFootprintActor().SetVisibility(instrumentPointingAvailable ? 1 : 0);
			if (instrumentPointingAvailable) footprint.setFootprintColor();
		});

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
		Arrays.stream(this.spacecraftFov).forEach(fov -> fov.getFrustumActor().VisibilityOff());
		Arrays.stream(this.footprint).forEach(footprint -> footprint.getFootprintActor().VisibilityOff());
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, run);
	}

	private void updateFovs(StateHistory run)
	{
//		if (footprint.length != 0) return;
		int numberOfInstruments = run.getPointingProvider().getInstrumentNames().length;
		if (numberOfInstruments > 0)
		{
			footprint = new PerspectiveImageFootprint[numberOfInstruments];
			spacecraftFov = new PerspectiveImageFrustum[numberOfInstruments];
			int i=0;
			PerspectiveImageFrustum fov = null;
			PerspectiveImageFootprint fprint = null;
			for (String instName : run.getPointingProvider().getInstrumentNames())
			{
				Color color = new Color((int)(Math.random() * 0x1000000));
				fov = instrumentNameToFovMap.get(instName);
				if (fov == null)
				{
					fov = new PerspectiveImageFrustum(1, 0, 0, true, diagonalLength);
					instrumentNameToFovMap.put(instName, fov);
					fov.getFrustumActor().VisibilityOff();
					fov.setColor(color);
					fov.setInstrumentName(instName);
				}

				spacecraftFov[i] = fov;
//				if (spacecraftFov[i].getFrustumActor() != null)
//					spacecraftFov[i].getFrustumActor().VisibilityOff();
				fprint = instrumentNameToFootprintMap.get(instName);
				if (fprint == null)
				{
					fprint = new PerspectiveImageFootprint();
					instrumentNameToFootprintMap.put(instName, fprint);
					fprint.setBoundaryVisible(false);
					fprint.setVisible(false);
					fprint.setInstrumentName(instName);
					fprint.setColor(color);
				}

				footprint[i] = fprint;
//				if (footprint[i].getFootprintActor() != null)
//				{
//					footprint[i].setBoundaryVisible(false);
//					footprint[i].setVisible(false);
//				}
				i++;
			}
		}

	}

	public void setFootprintPlateColoring(String name)
	{
		Arrays.stream(footprint).forEach(item -> {
			if (name.equals("")) item.setPlateColoringName(null);
			else item.setPlateColoringName(name);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		updateVisibilities();
	}

	public void updateFOVVisibility(Vector<String> selectedFOVs)
	{
		this.selectedFOVs = selectedFOVs;
		updateVisibilities();
	}

	public void updateFootprintBoundaryVisibility(Vector<String> selectedFOVs)
	{
		this.selectedFOVs = selectedFOVs;
		updateVisibilities();
	}

	public void updateFootprintVisibility(Vector<String> selectedFOVs)
	{
		this.selectedFOVs = selectedFOVs;
		updateVisibilities();
	}

	private void updateVisibilities()
	{
		Arrays.stream(spacecraftFov).filter(item -> item.getFrustumActor() != null).forEach(item -> {
			item.getFrustumActor().SetVisibility(displayFrusta && selectedFOVs.contains(item.getInstrumentName()) ? 1 : 0);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		Arrays.stream(footprint).filter(fprint -> fprint != null).forEach(item -> {
			item.setBoundaryVisible(displayBoundaries && selectedFOVs.contains(item.getInstrumentName()));
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		Arrays.stream(footprint).filter(fprint -> fprint != null).forEach(item -> {
			item.setVisible(displayFootprints && selectedFOVs.contains(item.getInstrumentName()));
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
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
	 * @param stateHistory
	 * @param visibility
	 */
	public void setVisibility(StateHistory stateHistory, boolean visibility)
	{
		TrajectoryActor renderer = stateHistoryToRendererMap.get(stateHistory);
		int isVisible = (visibility == true) ? 1 : 0;
		renderer.SetVisibility(isVisible);
		Arrays.stream(this.spacecraftFov).forEach(fov -> fov.getFrustumActor().SetVisibility(isVisible));
		Arrays.stream(this.footprint).forEach(footprint -> footprint.getFootprintActor().SetVisibility(isVisible));
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);

//		StateHistoryRenderProperties tmpProp = propM.get(stateHistory);
//		if (tmpProp == null)
//			continue;
//
//		tmpProp.isVisible = visibility;
//
//		if (visibility)
//			loadVtkPainter(stateHistory);
	}

	/**
	 * @param segment
	 * @param color
	 */
	public void setTrajectoryColor(StateHistory segment, Color color)
	{
//		GroupColorProvider colorSource = colorProvider;

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
		renderer.setTrajectoryColor(segment.getTrajectory().getTrajectoryColor());
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
//		if (timeBarActor == null) return;
//		timeBarActor.updateTimeBarPosition(width, height);
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

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
		{
			props.add(stateHistoryToRendererMap.get(history));

		}
		if (spacecraftFov != null)
		{
//			if (this.spacecraftFov.length > 0)
//				System.out.println("StateHistoryRendererManager: getProps: frustum actor " + this.spacecraftFov[0].getFrustumActor().GetClassName());
			Arrays.stream(this.spacecraftFov).filter(fov -> fov.getFrustumActor() != null).forEach(fov -> props.add(fov.getFrustumActor()));
			Arrays.stream(this.footprint).filter(fprint -> fprint != null).filter(fov -> fov.getFootprintActor() != null).forEach(footprint -> props.add(footprint.getFootprintActor()));
			Arrays.stream(this.footprint).filter(fprint -> fprint != null).filter(fov -> fov.getFootprintActor() != null).forEach(footprint -> props.add(footprint.getFootprintBoundaryActor()));
		}

		props.add(spacecraft.getActor());
		props.add(scDirectionMarker.getActor());
		props.add(spacecraftLabelActor);
		props.add(earthDirectionMarker.getActor());
		props.add(sunDirectionMarker.getActor());
		props.add(timeBarActor);
		props.add(statusBarTextActor);
//		System.out.println("StateHistoryRendererManager: getProps: adding planned science count " + plannedScienceActors.size());
		props.addAll(plannedScienceActors);

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
		updateFovs(state);
		if (state != null && spacecraft.getActor() != null )
		{
			positionCalculator.updateSpacecraftPosition(state, timeFraction, spacecraft, scDirectionMarker,
														spacecraftLabelActor, spacecraftFov, footprint);
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
	 * @param visible
	 */
	public void setInstrumentFootprintVisibility(boolean visible)
	{
		displayFootprints = visible;
		updateVisibilities();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
	}

	public void setInstrumentFootprintColor(String name, Color color)
	{

		Arrays.stream(footprint).filter(fprint -> fprint.getInstrumentName().equals(name)).forEach(item -> {
			item.setBoundaryColor(color);
			item.setColor(color);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
	}

	public Color getInstrumentFootprintColor(String name)
	{
		List<PerspectiveImageFootprint> fp = Arrays.stream(footprint).filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		return fp.get(0).getColor();
	}


	/**
	 * @param visible
	 */
	public void setInstrumentFootprintBorderVisibility(boolean visible)
	{
		displayBoundaries = visible;
		updateVisibilities();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
	}

	public boolean getInstrumentFootprintVisibility(String name)
	{
		List<PerspectiveImageFootprint> fp = Arrays.stream(footprint).filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		return fp.get(0).isVisible();
	}

	public void setInstrumentFootprintVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFootprint> fp = Arrays.stream(footprint).filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		fp.get(0).setVisible(isVisible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, fp.get(0));
	}

	public boolean getInstrumentFootprintBorderVisibility(String name)
	{
		List<PerspectiveImageFootprint> fp = Arrays.stream(footprint).filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		return fp.get(0).getFootprintBoundaryActor().GetVisibility() == 1 ? true : false;
	}

	public void setInstrumentFootprintBorderVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFootprint> fp = Arrays.stream(footprint).filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		fp.get(0).setBoundaryVisible(isVisible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, fp.get(0));
	}

	public boolean getInstrumentFrustumVisibility(String name)
	{
		List<PerspectiveImageFrustum> fovs = Arrays.stream(spacecraftFov).filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		return fovs.get(0).getFrustumActor().GetVisibility() == 1 ? true : false;
	}

	public void setInstrumentFrustumVisibility(String name, boolean isVisible)
	{
//		Arrays.stream(this.spacecraftFov).filter(fov -> fov.getFrustumActor() != null).forEach(fov -> {
//			fov.getFrustumActor().SetVisibility(visible == true ? 1 : 0);
//			setSpacecraftFOVFootprintVisibility(visible);
//		});
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftFov);

		List<PerspectiveImageFrustum> fovs = Arrays.stream(spacecraftFov).filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		System.out.println("StateHistoryRendererManager: setInstrumentFrustumVisibility: got a frustum for " + name);
		fovs.get(0).getFrustumActor().SetVisibility(isVisible? 1: 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftFov);
	}

	/**
	 * @param visible
	 */
	public void setInstrumentFrustumVisibility(boolean visible)
	{
		displayFrusta = visible;
		updateVisibilities();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public Color getInstrumentFrustumColor(String name)
	{
		List<PerspectiveImageFrustum> fovs = Arrays.stream(spacecraftFov).filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		return fovs.get(0).getColor();
	}

	public void setInstrumentFrustumColor(String name, Color color)
	{
		Arrays.stream(spacecraftFov).filter(item -> item.getInstrumentName().equals(name)).forEach(item -> {
			item.setColor(color);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
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

	/**
	 * @param visible
	 */
	public void setSpacecraftFOVVisibility(boolean visible)
	{
		Arrays.stream(this.spacecraftFov).filter(fov -> fov.getFrustumActor() != null).forEach(fov -> {
			fov.getFrustumActor().SetVisibility(visible == true ? 1 : 0);
			setSpacecraftFOVFootprintVisibility(visible);
		});
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraftFov);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftFOVFootprintVisibility(boolean visible)
	{
		Arrays.stream(this.footprint).filter(footprint -> footprint.getFootprintActor() != null).forEach(footprint -> {
			footprint.getFootprintActor().SetVisibility(visible == true ? 1 : 0);
		});

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, footprint);
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

	//TODO reimplement once colorization is in place
	public FeatureAttr getFeatureAttrFor(StateHistory item, StateHistoryFeatureType aFeatureType)
	{
		return null;
//		if (aFeatureType == StateHistoryFeatureType.Time)
//		{
//			vtkDoubleArray timeArray = new vtkDoubleArray();
//			for (Double nextTime : item.getTrajectory().getTime())
//			{
//				timeArray.InsertNextValue(nextTime);
//			}
//			return new VtkFeatureAttr(timeArray);
//		}
//		else if (aFeatureType == StateHistoryFeatureType.Distance)
//		{
//			vtkDoubleArray distanceArray = new vtkDoubleArray();
//			for (int i=0; i<item.getTrajectory().getX().size(); i++)
//			{
//				double distance = Math.sqrt(Math.pow(item.getTrajectory().getX().get(i), 2) +
//											Math.pow(item.getTrajectory().getY().get(i), 2) +
//											Math.pow(item.getTrajectory().getZ().get(i), 2));
//				distanceArray.InsertNextValue(distance);
//			}
//			return new VtkFeatureAttr(distanceArray);
//		}
//		else return null;
	}

//	private Map<StateHistory, VtkStateHistoryPainter<StateHistory>> vPainterM;
//	private Map<StateHistory, StateHistoryRenderProperties> propM;
//	private Map<StateHistory, VtkStateHistoryPointProvider> vAuxM;
//	private Map<vtkProp, VtkStateHistoryPainter<StateHistory>> vActorToPainterM;
//
//
//	public void setAllItems(Collection<StateHistory> aItemC)
//	{
//		// Clear relevant state vars
//		propM = new HashMap<>();
//
//		// Setup the initial props for all the items
//		int tmpIdx = 0;
//		int numItems = aItemC.size();
//		for (StateHistory aItem : aItemC)
//		{
//			ColorProvider tmpSrcCP = sourceGCP.getColorProviderFor(aItem, tmpIdx, numItems);
//
//			StateHistoryRenderProperties tmpProp = new StateHistoryRenderProperties();
//			tmpProp.isVisible = false;
//			tmpProp.srcCP = tmpSrcCP;
//			tmpIdx++;
//
//			propM.put(aItem, tmpProp);
//		}
//
//		updateVtkVars(aItemC);
//	}

	public void installGroupColorProviders(GroupColorProvider aSrcGCP/*, StateHistoryCollection runs*/)
	{
//		this.colorProvider = aSrcGCP;
//		int tmpIdx = -1;
//		int numItems = runs.getNumItems();
//		for (StateHistory aItem : runs.getAllItems())
//		{
//			tmpIdx++;
//
//			// Skip to next if no RenderProp
//			StateHistoryRenderProperties tmpProp = propM.get(aItem);
//			if (tmpProp == null)
//				continue;
//
//			// Skip to next if custom
//			if (tmpProp.isCustomCP == true)
//				continue;
//
//			tmpProp.srcCP = aSrcGCP.getColorProviderFor(aItem, tmpIdx, numItems);
//		}
//
//		runs.notify(this, ItemEventType.ItemsMutated);
//		updateVtkVars(runs.getAllItems());
	}

//	/**
//	 * Notification method that the lidar data associated with aFileSpec has been
//	 * loaded. The provided VtkLidarDataPainter will contain the loaded state.
//	 */
//	protected void markStateHistoryLoadComplete(StateHistory stateHistory, VtkStateHistoryPointProvider aStateHistoryPointProvider,
//			VtkStateHistoryPainter<StateHistory> aPainter, StateHistoryCollection runs)
//	{
//		vAuxM.put(stateHistory, aStateHistoryPointProvider);
//
//		vPainterM.put(stateHistory, aPainter);
//		for (vtkProp prop : aPainter.getProps())
//			vActorToPainterM.put(prop, aPainter);
//
//		aPainter.setPercentageShown(begPercent, endPercent);
//
//		runs.notify(this, ItemEventType.ItemsMutated);
//		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//	}
//
//	/**
//	 * Helper method to load the lidar data into a VtkLidarDataPainter.
//	 * <P>
//	 * The actual loading of the lidar data may happen asynchronously.
//	 */
//	private void loadVtkPainter(StateHistory history, StateHistoryCollection runs)
//	{
//		// Bail if the corresponding VTK data has already been created
//		VtkStateHistoryPainter<?> tmpData = vPainterM.get(history);
//		if (tmpData != null)
//			return;
//
//		try
//		{
//			VtkStateHistoryStruct tmpVLS = LidarFileSpecLoadUtil.loadAsciiLidarData(tmpFile, refBodyViewConfig);
//
//			VtkStateHistoryPointProvider tmpLPP = new VtkStateHistoryPointProvider(tmpVLS.vSrcP, tmpVLS.vTgtP);
//			VtkStateHistoryUniPainter<StateHistory> tmpPainter = new VtkStateHistoryUniPainter<>(runs, history, tmpVLS);
//
//			runs.markStateHistoryLoadComplete(history, tmpLPP, tmpPainter);
//		}
//		catch (IOException aExp)
//		{
//			aExp.printStackTrace();
//		}
//	}
//
//	/**
//	 * Helper method that will update all relevant VTK vars.
//	 * <P>
//	 * A notification will be sent out to PropertyChange listeners of the
//	 * {@link Properties#MODEL_CHANGED} event.
//	 */
//	private void updateVtkVars(Collection<StateHistory> aUpdateC)
//	{
//		for (StateHistory aItem : aUpdateC)
//		{
//			// Skip to next if no installed painter
//			VtkStateHistoryPainter<?> tmpPainter = vPainterM.get(aItem);
//			if (tmpPainter == null)
//				continue;
//
//			tmpPainter.vtkUpdateState();
//		}
//
//		for (VtkStateHistoryPainter<?> aPainter : vPainterM.values())
//			aPainter.vtkUpdateState();
//
//		// Notify our PropertyChangeListeners
//		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//	}




}
