package edu.jhuapl.sbmt.stateHistory.rendering.model;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import vtk.vtkDoubleArray;
import vtk.vtkProp;
import vtk.vtkScalarBarActor;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.feature.VtkFeatureAttr;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.LightingType;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFrustum;
import edu.jhuapl.sbmt.pointing.InstrumentPointing;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.liveColoring.LiveColorableManager;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
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
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryFeatureType;

import glum.gui.panel.itemList.ItemProcessor;
import glum.item.IdGenerator;
import glum.item.IncrIdGenerator;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;

public class StateHistoryRendererManager extends SaavtkItemManager<StateHistory>
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
	private HashMap<StateHistory, ArrayList<PerspectiveImageFrustum>> historySpacecraftFovMap;

	/**
	 *
	 */
	private HashMap<StateHistory, ArrayList<PerspectiveImageFootprint>> historyFootprintMap;

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
	double markerRadius, markerHeight;

    /**
     *
     */
	private double scalingFactor = 0.0;

	private double diagonalLength;

	private HashMap<String, PerspectiveImageFootprint> instrumentNameToFootprintMap = new HashMap<String, PerspectiveImageFootprint>();
	private HashMap<String, PerspectiveImageFrustum> instrumentNameToFovMap = new HashMap<String, PerspectiveImageFrustum>();

	private Vector<String> selectedFOVs = new Vector<String>();

	private List<vtkProp> plannedScienceActors = new ArrayList<vtkProp>();

	private GroupColorProvider sourceGCP;

	IdGenerator indexGen = new IncrIdGenerator(-1);

	private IStateHistoryPositionCalculator positionCalculator;

	private SmallBodyModel smallBodyModel;

	private StateHistoryCollection runs;

	private Renderer renderer;

	private double currentTimeFraction = -1;

	private RendererLookDirection lookDirection = RendererLookDirection.FREE_VIEW;

	private boolean syncImages = false, syncSpectra = false, syncLidar = false;

	public void setRendererLookDirection(RendererLookDirection rendererLookDirection)
	{
		this.lookDirection = rendererLookDirection;
	}

	public Renderer getRenderer()
	{
		return renderer;
	}

	public void setRenderer(Renderer renderer)
	{
		this.renderer = renderer;
	}

	public StateHistoryCollection getRuns()
	{
		return runs;
	}

	public StateHistoryRendererManager(SmallBodyModel smallBodyModel, StateHistoryCollection runs, Renderer renderer)
	{
		this.smallBodyModel = smallBodyModel;
		this.runs = runs;
		this.renderer = renderer;
		StateHistoryTimeModel.getInstance().setPcs(pcs);
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
		this.spacecraft.getActor().forEach(item -> item.VisibilityOff());

		this.scDirectionMarker = new SpacecraftDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
		this.scDirectionMarker.getActor().forEach(item -> item.VisibilityOff());

		this.earthDirectionMarker = new EarthDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
		this.earthDirectionMarker.getActor().forEach(item -> item.VisibilityOff());

		this.sunDirectionMarker = new SunDirectionMarker(markerRadius, markerHeight, 0, 0, 0);
		this.sunDirectionMarker.getActor().forEach(item -> item.VisibilityOff());

		this.timeBarActor = new TimeBarTextActor();

		this.statusBarTextActor = new StatusBarTextActor();

		historyFootprintMap = new HashMap<StateHistory, ArrayList<PerspectiveImageFootprint>>();
		historySpacecraftFovMap = new HashMap<StateHistory, ArrayList<PerspectiveImageFrustum>>();

		sourceGCP = new ConstGroupColorProvider(new ConstColorProvider(new Color(0, 255, 255)));

		propM = new HashMap<>();
//		vPainterM = new HashMap<>();
//		vActorToPainterM = new HashMap<>();
		addListener((aSource, aEventType) ->
		{
			try {
				if (aEventType != ItemEventType.ItemsSelected) return;
				for (StateHistory history : getAllItems())
				{
					if (history.getTrajectory() == null) continue;
					history.getTrajectory().setFaded(!getSelectedItems().contains(history));
					refreshColoring(history);
				}
				updateTimeBarValue();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	//***************************************
	//Sun related
	//***************************************
	/**
	 * @param visible
	 */
	public void setSunDirectionMarkerVisibility(boolean visible)
	{
		sunDirectionMarker.getActor().forEach(item -> item.SetVisibility(visible == true ? 1 : 0));
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
	 * @param color
	 */
	public void setSunDirectionMarkerColor(Color color)
	{
		sunDirectionMarker.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, sunDirectionMarker);
	}

	//***************************************
	//Earth related
	//***************************************
	/**
	 * @param color
	 */
	public void setEarthDirectionMarkerColor(Color color)
	{
		earthDirectionMarker.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
	}

	/**
	 * @param visible
	 */
	public void setEarthDirectionMarkerVisibility(boolean visible)
	{
		earthDirectionMarker.getActor().forEach(item ->
		{
			System.out.println("StateHistoryRendererManager: setEarthDirectionMarkerVisibility: setting vis to " + visible);
			item.SetVisibility(visible == true ? 1 : 0);
		});
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
	}


	/**
	 * @param radius
	 */
	public void setEarthDirectionMarkerSize(int radius)
	{
		earthDirectionMarker.setPointerSize(radius);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, earthDirectionMarker);
	}


	//***************************************
	//SC Related
	//***************************************
	/**
	 * @param spacecraft
	 */
	public void setSpacecraft(SpacecraftBody spacecraft)
	{
		this.spacecraft = spacecraft;
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
		spacecraft.getActor().forEach(item -> item.SetVisibility(visible == true ? 1 : 0));
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
		scDirectionMarker.getActor().forEach(item -> item.SetVisibility(visible == true ? 1 : 0));
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
	 * @param color
	 */
	public void setScDirectionMarkerColor(Color color)
	{
		scDirectionMarker.setColor(color);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
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

	//***************************************
	//Trajectory Related
	//***************************************
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
		run.setMapped(true);

		runs.addRun(run);
		updateFovs(run);

		// Get the trajectory actor the state history segment
		TrajectoryActor trajectoryActor = new TrajectoryActor(run.getTrajectory());
		stateHistoryToRendererMap.put(run, trajectoryActor);

		ColorProvider tmpSrcCP = sourceGCP.getColorProviderFor(run, indexGen.getNextId(), stateHistoryToRendererMap.size());
		StateHistoryRenderProperties tmpProp = new StateHistoryRenderProperties();
		tmpProp.isVisible = false;
		tmpProp.srcCP = tmpSrcCP;
		propM.put(run, tmpProp);

		trajectoryActor.setColoringFunction(StateHistoryColoringFunctions.PER_TABLE.getColoringFunction(),
				Colormaps.getNewInstanceOfBuiltInColormap("Rainbow"));

		trajectoryActor.setMinMaxFraction(run.getTrajectory().getMinDisplayFraction(), run.getTrajectory().getMaxDisplayFraction());
		trajectoryActor.VisibilityOn();
		trajectoryActor.GetMapper().Update();

		boolean instrumentPointingAvailable = run.getTrajectory().isHasInstrumentPointingInfo();

		if (historyFootprintMap.get(runs.getCurrentRun()) != null)
			historyFootprintMap.get(run).stream().filter(fprint -> fprint != null).filter(footprint -> footprint.getFootprintActor() != null).forEach(footprint -> {
				if (instrumentPointingAvailable) footprint.setFootprintColor();
			});
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajectoryActor);

		notifyListeners(this, ItemEventType.ItemsChanged);
		notifyListeners(this, ItemEventType.ItemsSelected);
		return trajectoryActor;
	}

	/**
	 * @param key
	 */
	public void removeRun(StateHistory run)
	{
		stateHistoryToRendererMap.remove(run);
		if (historyFootprintMap.get(run) != null)
		{
			historySpacecraftFovMap.get(run).forEach(fov -> { if (fov.getFrustumActor() != null) fov.getFrustumActor().VisibilityOff(); } );
			historyFootprintMap.get(run).forEach(footprint -> { if (footprint.getFootprintActor() != null)  footprint.getFootprintActor().VisibilityOff(); });
		}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, run);
		notifyListeners(this, ItemEventType.ItemsChanged);
		run.setMapped(false);
		run.setVisible(false);
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
		stateHistory.setVisible(visibility);
		TrajectoryActor renderer = stateHistoryToRendererMap.get(stateHistory);
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
		segment.getTrajectory().setColor(color);
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		renderer.setColoringFunction(null, null);
		renderer.setTrajectoryColor(color);
		refreshColoring(segment);
	}

	public void refreshColoring()
	{
		refreshColoring(runs.getCurrentRun());
	}

	/**
	 * @param segment
	 */
	public void refreshColoring(StateHistory segment)
	{
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		if (renderer == null)
			return;
		StateHistoryRenderProperties tmpProp = propM.get(segment);
		renderer.setColoringProvider(tmpProp.srcCP);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
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
	 * @param segment
	 * @return
	 */
	public TrajectoryActor getTrajectoryActorForStateHistory(StateHistory segment)
	{
		return stateHistoryToRendererMap.get(segment);
	}

	public void makeFrustum(StateHistory run, String instName)
	{
		if (run.getPointingProvider() == null) return;
		int numberOfInstruments = run.getPointingProvider().getInstrumentNames().length;
		if (numberOfInstruments == 0) return;
		Color color = new Color((int)(Math.random() * 0x1000000));
		PerspectiveImageFrustum fov = instrumentNameToFovMap.get(instName);
		if (fov == null)
		{
			fov = new PerspectiveImageFrustum(1, 0, 0, true, diagonalLength);
			instrumentNameToFovMap.put(instName, fov);
			fov.getFrustumActor().VisibilityOff();
			fov.setColor(color);
			fov.setInstrumentName(instName);
			ArrayList<PerspectiveImageFrustum> frusta = historySpacecraftFovMap.get(run);
			if (frusta == null) frusta = new ArrayList<PerspectiveImageFrustum>();
			frusta.add(fov);
			historySpacecraftFovMap.put(run, frusta);
			positionCalculator.updateFOVLocations(run, historySpacecraftFovMap.get(runs.getCurrentRun()));
		}

	}

	public void makeFootprint(StateHistory run, String instName)
	{
		if (run.getPointingProvider() == null) return;
		int numberOfInstruments = run.getPointingProvider().getInstrumentNames().length;
		if (numberOfInstruments == 0) return;
		PerspectiveImageFootprint fprint = instrumentNameToFootprintMap.get(instName);
		if (fprint == null)
		{
			Color color = historySpacecraftFovMap.get(run).stream().filter(fov -> fov.getInstrumentName().equals(instName)).collect(Collectors.toList()).get(0).getColor();
			fprint = new PerspectiveImageFootprint();
			fprint.setStaticFootprint(true);
			instrumentNameToFootprintMap.put(instName, fprint);
			fprint.setBoundaryVisible(false);
			fprint.setVisible(false);
			fprint.setInstrumentName(instName);
			fprint.setColor(color);
			fprint.setSmallBodyModel(smallBodyModel);
			ArrayList<PerspectiveImageFootprint> footprints = historyFootprintMap.get(run);
			if (footprints == null) footprints = new ArrayList<PerspectiveImageFootprint>();
			footprints.add(fprint);
			historyFootprintMap.put(run, footprints);
			positionCalculator.updateFootprintLocations(run, historyFootprintMap.get(runs.getCurrentRun()));
			LiveColorableManager.updateFootprint(fprint);
		}
	}

	//***************************************
	//FOV/Frustum related
	//***************************************
	private void updateFovs(StateHistory run)
	{
		ArrayList<PerspectiveImageFootprint> footprints = historyFootprintMap.get(run);
		if (footprints == null) return;
		for (PerspectiveImageFootprint fp : footprints)
		{
			if (fp.isVisible())
				LiveColorableManager.updateFootprint(fp);
		}
	}

//	/**
//	 * @param visible
//	 */
//	public void setSpacecraftFOVVisibility(boolean visible)
//	{
//		historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov.getFrustumActor() != null).forEach(fov -> {
//			fov.getFrustumActor().SetVisibility(visible == true ? 1 : 0);
//			setSpacecraftFOVFootprintVisibility(visible);
//		});
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, historySpacecraftFovMap.get(runs.getCurrentRun()));
//	}
//
//	/**
//	 * @param visible
//	 */
//	public void setSpacecraftFOVFootprintVisibility(boolean visible)
//	{
//		this.historyFootprintMap.get(runs.getCurrentRun()).stream().filter(footprint -> footprint.getFootprintActor() != null).forEach(footprint -> {
//			footprint.getFootprintActor().SetVisibility(visible == true ? 1 : 0);
//		});
//
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, historyFootprintMap.get(runs.getCurrentRun()));
//	}

	public boolean getInstrumentFrustumVisibility(String name)
	{
		if (historySpacecraftFovMap.get(runs.getCurrentRun()) == null || historySpacecraftFovMap.get(runs.getCurrentRun()).isEmpty()) return false;

		List<PerspectiveImageFrustum> fovs = historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fovs.size() == 0) return false;

		return fovs.get(0).getFrustumActor().GetVisibility() == 1 ? true : false;
	}

	public void setInstrumentFrustumVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFrustum> fovs = historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fovs.size() == 0) return;
		fovs.get(0).getFrustumActor().SetVisibility(isVisible? 1: 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, historySpacecraftFovMap.get(runs.getCurrentRun()));
	}

//	/**
//	 * @param visible
//	 */
//	public void setInstrumentFrustumVisibility(boolean visible)
//	{
//		updateVisibilities();
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//	}

	public Color getInstrumentFrustumColor(String name)
	{
		if (historySpacecraftFovMap.get(runs.getCurrentRun()) == null || historySpacecraftFovMap.get(runs.getCurrentRun()).isEmpty()) return Color.white;
		List<PerspectiveImageFrustum> fovs = historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fovs.size() == 0) return Color.white;
		return fovs.get(0).getColor();
	}

	public void setInstrumentFrustumColor(String name, Color color)
	{
		historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).forEach(item -> {
			item.setColor(color);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
	}


	//***************************************
	//Footprint related
	//***************************************
	public void setFootprintPlateColoring(String name)
	{
		historyFootprintMap.get(runs.getCurrentRun()).forEach(item -> {
			if (name.equals("")) item.setPlateColoringName(null);
			else item.setPlateColoringName(name);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		updateVisibilities();
	}


//	public void updateFootprintBoundaryVisibility(Vector<String> selectedFOVs)
//	{
//		this.selectedFOVs = selectedFOVs;
//		updateVisibilities();
//	}
//
//	public void updateFootprintVisibility(Vector<String> selectedFOVs)
//	{
//		this.selectedFOVs = selectedFOVs;
//		updateVisibilities();
//	}
//
//
//	/**
//	 * @param visible
//	 */
//	public void setInstrumentFootprintVisibility(boolean visible)
//	{
//		updateVisibilities();
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
//	}

	public void setInstrumentFootprintColor(String name, Color color)
	{

		System.out.println("StateHistoryRendererManager: setInstrumentFootprintColor: looking for name " + name);
		System.out.println("StateHistoryRendererManager: setInstrumentFootprintColor: " + historyFootprintMap.get(runs.getCurrentRun()).get(0).getInstrumentName());
		historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).forEach(item -> {
			item.setBoundaryColor(color);
			item.setColor(color);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
	}
//
//	public Color getInstrumentFootprintColor(String name)
//	{
//		if (historyFootprintMap.get(runs.getCurrentRun()) == null || historyFootprintMap.get(runs.getCurrentRun()).isEmpty()) return Color.white;
//		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
//		if (fp.size() == 0) return Color.white;
//		return fp.get(0).getColor();
//	}
//
//
//	/**
//	 * @param visible
//	 */
//	public void setInstrumentFootprintBorderVisibility(boolean visible)
//	{
//		updateVisibilities();
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
//	}

	public boolean getInstrumentFootprintVisibility(String name)
	{
		if (historyFootprintMap.get(runs.getCurrentRun()) == null || historyFootprintMap.get(runs.getCurrentRun()).isEmpty()) return false;
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fp.size() == 0) return false;
		return fp.get(0).isVisible();
	}

	public void setInstrumentFootprintVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		fp.get(0).setVisible(isVisible);
		LiveColorableManager.updateFootprint(fp.get(0));
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, fp.get(0));
	}

	public boolean getInstrumentFootprintBorderVisibility(String name)
	{
		if (historyFootprintMap.get(runs.getCurrentRun()) == null || historyFootprintMap.get(runs.getCurrentRun()).isEmpty()) return false;
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fp.size() == 0) return false;
		return fp.get(0).getFootprintBoundaryActor().GetVisibility() == 1 ? true : false;
	}

	public void setInstrumentFootprintBorderVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		fp.get(0).setBoundaryVisible(isVisible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, fp.get(0));
	}

	//***************************************
	//Planned Science Related
	//***************************************
	public void addPlannedScienceActors(List<vtkProp> actors)
	{
		plannedScienceActors.addAll(actors);
	}

	public void clearPlannedScience()
	{
		plannedScienceActors.clear();
	}

	//***************************************
	//Misc
	//***************************************
	public Vector<DisplayableItem> getDisplayableItems()
	{
		Vector<DisplayableItem> items = new Vector<DisplayableItem>();
		items.add(earthDirectionMarker);
		items.add(sunDirectionMarker);
		items.add(scDirectionMarker);
		items.add(spacecraft);

		return items;
	}


	private void updateVisibilities()
	{
		historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(item -> item.getFrustumActor() != null).forEach(item -> {
			item.getFrustumActor().SetVisibility(selectedFOVs.contains(item.getInstrumentName()) ? 1 : 0);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint != null).forEach(item -> {
			item.setBoundaryVisible(selectedFOVs.contains(item.getInstrumentName()));
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint != null).forEach(item -> {
			item.setVisible(selectedFOVs.contains(item.getInstrumentName()));
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
	}

	//***************************************
	// time updates
	//***************************************
	/**
	 * @param time
	 */
	public void updateTimeBarValue()
	{
		if (runs.getCurrentRun() == null) return;
		if (timeBarActor == null) return;
		double time = runs.getCurrentRun().getCurrentTime();
		timeBarActor.updateTimeBarValue(time);
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

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

	//***************************************
	//status bar updates
	//***************************************
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
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
		if (!historySpacecraftFovMap.isEmpty() && (historySpacecraftFovMap.get(runs.getCurrentRun()) != null))
		{
//			if (this.spacecraftFov.length > 0)
//				System.out.println("StateHistoryRendererManager: getProps: frustum actor " + this.spacecraftFov[0].getFrustumActor().GetClassName());
			ArrayList<PerspectiveImageFrustum> arrayList = historySpacecraftFovMap.get(runs.getCurrentRun());
			historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov != null && fov.getFrustumActor() != null).forEach(fov -> props.add(fov.getFrustumActor()));
//			historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov.getFrustumActor2() != null).forEach(fov -> props.add(fov.getFrustumActor2()));
//			historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov.getFrustumActor3() != null).forEach(fov -> props.add(fov.getFrustumActor3()));
//			historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov.getFrustumActor4() != null).forEach(fov -> props.add(fov.getFrustumActor4()));
//			historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov.getFrustumActor5() != null).forEach(fov -> props.add(fov.getFrustumActor5()));
//			historySpacecraftFovMap.get(runs.getCurrentRun()).stream().filter(fov -> fov.getFrustumActor6() != null).forEach(fov -> props.add(fov.getFrustumActor6()));
		}
		if (!historyFootprintMap.isEmpty() && historyFootprintMap.get(runs.getCurrentRun()) != null)
		{
			historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint != null).filter(fov -> fov.getFootprintActor() != null).forEach(footprint -> props.add(footprint.getFootprintActor()));
			historyFootprintMap.get(runs.getCurrentRun()).stream().filter(fprint -> fprint != null).filter(fov -> fov.getFootprintActor() != null).forEach(footprint -> props.add(footprint.getFootprintBoundaryActor()));
		}

		props.addAll(spacecraft.getActor());
		props.addAll(scDirectionMarker.getActor());
		props.add(spacecraftLabelActor);
		props.addAll(earthDirectionMarker.getActor());
		props.addAll(sunDirectionMarker.getActor());
		props.add(timeBarActor);
		props.add(statusBarTextActor);
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
		if (Double.compare(currentTimeFraction, timeFraction) == 0) return;
		this.currentTimeFraction = timeFraction;
//		System.out.println("StateHistoryRendererManager: setTimeFraction: setting time fraction state is " + state + " and actor " + spacecraft.getActor());
		// StateHistory state = getCurrentRun();
//		Logger.getAnonymousLogger().log(Level.INFO, "Updating FOVs for state " + state.getStateHistoryName());
		updateFovs(state);
//		Logger.getAnonymousLogger().log(Level.INFO, "Updated FOVs");
		if (state != null && spacecraft.getActor() != null )
		{
//			Logger.getAnonymousLogger().log(Level.INFO, "setting sc pos");
			positionCalculator.updateSpacecraftPosition(state, timeFraction, spacecraft, scDirectionMarker,
														spacecraftLabelActor);
//			if (sunDirectionMarker.isVisible() == true)
			{
//				Logger.getAnonymousLogger().log(Level.INFO, "setting earth pos");
				positionCalculator.updateEarthPosition(state, timeFraction, earthDirectionMarker);
			}
//			if (sunDirectionMarker.isVisible() == true)
			{
//				Logger.getAnonymousLogger().log(Level.INFO, "setting sun pos");
				positionCalculator.updateSunPosition(state, timeFraction, sunDirectionMarker);
			}
//			Logger.getAnonymousLogger().log(Level.INFO, "Updating FOV Position");
			if (historySpacecraftFovMap.get(runs.getCurrentRun()) != null)
				positionCalculator.updateFOVLocations(state, historySpacecraftFovMap.get(runs.getCurrentRun()));

//			Logger.getAnonymousLogger().log(Level.INFO, "Updating Footprint location");

			if (historyFootprintMap.get(runs.getCurrentRun()) != null)
				positionCalculator.updateFootprintLocations(state, historyFootprintMap.get(runs.getCurrentRun()));

			updateLookDirection(lookDirection);
			if ((renderer.getLighting() == LightingType.FIXEDLIGHT && runs.getCurrentRun() != null) == false) return;
			renderer.setFixedLightDirection(runs.getCurrentRun().getSunPosition());
			propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
//			Logger.getAnonymousLogger().log(Level.INFO, "Position changed called");
		}
	}

	/**
	 * Updates the look direction based on the selected option in user interface
	 *
	 * @param runs			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 * @param model			The model that contains information about the view options
	 */
	public void updateLookDirection(RendererLookDirection lookDirection)
	{
		this.lookDirection = lookDirection;
		StateHistoryCollection runs = getRuns();
		if (runs.getCurrentRun() == null) return; // can't do any view things if we don't have a trajectory / time history
		Renderer renderer = getRenderer();
		double[] upVector = { 0, 0, 1 };

		Vector3D targOrig = new Vector3D(renderer.getCameraFocalPoint());
		Vector3D targAxis = new Vector3D(positionCalculator.updateLookDirection(lookDirection, scalingFactor));
		renderer.setCameraFocalPoint(new double[]{ 0, 0, 0 });
		double[] lookFromDirection;
		if (lookDirection == RendererLookDirection.FREE_VIEW)
		{
			lookFromDirection = renderer.getCamera().getPosition().toArray();
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(),
					renderer.getCamera().getUpUnit().toArray(), renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(false, Vector3D.ZERO, targOrig);
		}
		else
		{
			lookFromDirection = positionCalculator.updateLookDirection(lookDirection, scalingFactor);
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(), upVector,
					renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(true, targAxis, targOrig);
		}
		renderer.getRenderWindowPanel().resetCameraClippingRange();
	}


//	public double[] updateLookDirection(RendererLookDirection lookDirection)
//	{
//		return positionCalculator.updateLookDirection(lookDirection, scalingFactor);
//	}

//	/**
//	 * @return
//	 */
//	public double[] getCurrentLookFromDirection()
//	{
//		return positionCalculator.getCurrentLookFromDirection();
//	}
//
//	public static FeatureAttr getFeatureAttrFor(StateHistory history, FeatureType aFeatureType)
//	{
//		vtkDoubleArray dataArray = new vtkDoubleArray();
//		for (int i=0; i<history.getTrajectory().getNumPoints(); i++)
//		{
//			FeatureAttr attr = getFeatureAttrFor(history, aFeatureType, i);
//			dataArray.InsertNextValue(attr.getValAt(0));
//		}
//		return new VtkFeatureAttr(dataArray);
//	}

	public static FeatureAttr getFeatureAttrFor(Trajectory trajectory, FeatureType aFeatureType)
	{
		vtkDoubleArray dataArray = new vtkDoubleArray();
		int start = (int)(trajectory.getMinDisplayFraction()*trajectory.getNumPoints());
		int end = (int)(trajectory.getMaxDisplayFraction()*trajectory.getNumPoints());
		for (int i=start; i<end; i++)
		{
			FeatureAttr attr = getFeatureAttrFor(trajectory, aFeatureType, i);
			dataArray.InsertNextValue(attr.getValAt(0));
		}
		return new VtkFeatureAttr(dataArray);
	}

	public static FeatureAttr getFeatureAttrFor(StateHistory history, FeatureType aFeatureType, int i)
	{
		return getFeatureAttrFor(history.getTrajectory(), aFeatureType, i);
	}

	public static FeatureAttr getFeatureAttrFor(Trajectory trajectory, FeatureType aFeatureType, int i)
	{
		int minValueToColor = (int)(trajectory.getMinDisplayFraction()*trajectory.getNumPoints());
		int maxValueToColor = (int)(trajectory.getMaxDisplayFraction()*trajectory.getNumPoints());
		Range<Integer> coloredRange = Range.closed(minValueToColor, maxValueToColor);
		if (!coloredRange.contains(i)) return null;
		double startTime = trajectory.getStartTime();
		double nextTime = startTime + i*trajectory.getTimeStep();
		String currentInstrument = trajectory.getPointingProvider().getCurrentInstFrameName();
		InstrumentPointing instrumentPointing = trajectory.getPointingProvider().provide(nextTime);
		vtkDoubleArray dataArray = new vtkDoubleArray();
		if (aFeatureType == StateHistoryFeatureType.Time)
		{
			dataArray.InsertNextValue(nextTime);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.Distance)
		{
			double distance = instrumentPointing.getScPosition().getLength();
			dataArray.InsertNextValue(distance);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.Range)
		{
			double range =
					StateHistoryPositionCalculator.getSpacecraftRange(trajectory.getHistory(), currentInstrument, nextTime);
			dataArray.InsertNextValue(range);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.SubSCIncidence)
		{
			double incidence =
					StateHistoryPositionCalculator.getIncidenceAngle(trajectory.getHistory(), currentInstrument, nextTime);
			dataArray.InsertNextValue(incidence);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.SubSCEmission)
		{
			double emission =
					StateHistoryPositionCalculator.getEmissionAngle(trajectory.getHistory(), currentInstrument, nextTime);
			dataArray.InsertNextValue(emission);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.SubSCPhase)
		{
			double phase =
					StateHistoryPositionCalculator.getPhaseAngle(trajectory.getHistory(), currentInstrument, nextTime);
			dataArray.InsertNextValue(phase);
			return new VtkFeatureAttr(dataArray);
		}
		else return null;
	}

	/**
	 *
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<StateHistory> getAllItems()
	{
		return ImmutableList.copyOf(runs.getSimRuns());
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return runs.getSimRuns().size();
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<StateHistory> history)
	{
		for (StateHistory hist : getAllItems())
		{
//			hist.setVisible(history.contains(hist));
			setVisibility(hist, history.contains(hist));
		}
	}


//	private Map<StateHistory, VtkStateHistoryPainter<StateHistory>> vPainterM;
	private Map<StateHistory, StateHistoryRenderProperties> propM;
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
		int tmpIdx = -1;
		int numItems = stateHistoryToRendererMap.size(); //runs.getNumItems();
		StateHistory aItem = getSelectedItems().asList().get(0);
//		for (StateHistory aItem : stateHistoryToRendererMap.keySet())
//		{
			tmpIdx++;

			// Skip to next if no RenderProp
			StateHistoryRenderProperties tmpProp = propM.get(aItem);
			if (tmpProp == null)
				return;

			// Skip to next if custom
			if (tmpProp.isCustomCP == true)
				return;

			tmpProp.srcCP = aSrcGCP.getColorProviderFor(aItem, tmpIdx, numItems);
//			System.out.println("StateHistoryRendererManager: installGroupColorProviders: color provider " + tmpProp.srcCP.getBaseColor());
			refreshColoring(aItem);

//		}
		//TODO fix this?
//		runs.notify(this, ItemEventType.ItemsMutated);
//		updateVtkVars(stateHistoryToRendererMap.keySet());
	}

	public ColorProvider getColorProviderForStateHistory(StateHistory history)
	{
		if (propM.get(history) == null) return new ConstColorProvider(history.getTrajectory().getColor());
		return propM.get(history).srcCP;
	}

	public String getPlateColoringForInstrument(String fov)
	{
		if (runs.getCurrentRun() instanceof StandardStateHistory) return null;
		return ((SpiceStateHistory)runs.getCurrentRun()).getPlateColoringForInstrument(fov);
	}

	public void setPlateColoringForInstrument(String instrument, String plateColoring)
	{
		if (runs.getCurrentRun() instanceof StandardStateHistory) return;
		((SpiceStateHistory)runs.getCurrentRun()).setPlateColoringForInstrument(plateColoring, instrument);

		historyFootprintMap.get(runs.getCurrentRun()).forEach(item -> {
//			if (name.equals("")) item.setPlateColoringName(null);
			if (item.getInstrumentName().equals(instrument)) item.setPlateColoringName(plateColoring);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
//		updateVisibilities();
	}

	public int getNumMappedTrajectories()
	{
		return (int)(stateHistoryToRendererMap.keySet().stream().filter(state -> state.isMapped()).count());
	}

//	public void installGroupColorProviders(GroupColorProvider aSrcGCP/*, StateHistoryCollection runs*/)
//	{
//		int tmpIdx = -1;
//		int numItems = stateHistoryToRendererMap.size(); //runs.getNumItems();
//		for (StateHistory aItem : stateHistoryToRendererMap.keySet())
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
////			System.out.println("StateHistoryRendererManager: installGroupColorProviders: color provider " + tmpProp.srcCP.getBaseColor());
//			refreshColoring(aItem);
//
//		}
//		//TODO fix this?
////		runs.notify(this, ItemEventType.ItemsMutated);
////		updateVtkVars(stateHistoryToRendererMap.keySet());
//	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	public ItemProcessor<DisplayableItem> getDisplayItemsProcessor()
	{
		return new ItemProcessor<DisplayableItem>()
		{

			@Override
			public void addListener(ItemEventListener aListener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void delListener(ItemEventListener aListener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public ImmutableList<DisplayableItem> getAllItems()
			{
				return ImmutableList.copyOf(getDisplayableItems());
			}

			@Override
			public int getNumItems()
			{
				return getDisplayableItems().size();
			}
		};
	}

	/**
	 * @return the syncImages
	 */
	public boolean isSyncImages()
	{
		return syncImages;
	}

	/**
	 * @param syncImages the syncImages to set
	 */
	public void setSyncImages(boolean syncImages)
	{
		this.syncImages = syncImages;
	}

	/**
	 * @return the syncSpectra
	 */
	public boolean isSyncSpectra()
	{
		return syncSpectra;
	}

	/**
	 * @param syncSpectra the syncSpectra to set
	 */
	public void setSyncSpectra(boolean syncSpectra)
	{
		this.syncSpectra = syncSpectra;
	}

	/**
	 * @return the syncLidar
	 */
	public boolean isSyncLidar()
	{
		return syncLidar;
	}

	/**
	 * @param syncLidar the syncLidar to set
	 */
	public void setSyncLidar(boolean syncLidar)
	{
		this.syncLidar = syncLidar;
	}



	/**
	 * Notification method that the lidar data associated with aFileSpec has been
	 * loaded. The provided VtkLidarDataPainter will contain the loaded state.
	 */
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

	/**
	 * Helper method to load the lidar data into a VtkLidarDataPainter.
	 * <P>
	 * The actual loading of the lidar data may happen asynchronously.
	 */
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