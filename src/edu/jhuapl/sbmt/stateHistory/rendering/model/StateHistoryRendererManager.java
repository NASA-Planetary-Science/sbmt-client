package edu.jhuapl.sbmt.stateHistory.rendering.model;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import vtk.vtkDoubleArray;
import vtk.vtkProp;
import vtk.vtkScalarBarActor;

import edu.jhuapl.saavtk.camera.CameraUtil;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.provider.SimpleColorProvider;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.feature.VtkFeatureAttr;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.view.light.LightingType;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFrustum;
import edu.jhuapl.sbmt.pointing.InstrumentPointing;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.liveColoring.LiveColorableManager;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.DisplayableItem;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActorFactory;
import edu.jhuapl.sbmt.stateHistory.rendering.text.SpacecraftLabel;
import edu.jhuapl.sbmt.stateHistory.rendering.text.StatusBarTextActor;
import edu.jhuapl.sbmt.stateHistory.rendering.text.TimeBarTextActor;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryFeatureType;

import crucible.core.mechanics.providers.lockable.LockableEphemerisLinkEvaluationException;
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

	private Map<StateHistory, StateHistoryRenderProperties> propM;

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

	private StateHistoryCollection historyCollection;

	private Renderer renderer;

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

	public StateHistoryCollection getHistoryCollection()
	{
		return historyCollection;
	}

	public StateHistoryRendererManager(SmallBodyModel smallBodyModel, StateHistoryCollection historyCollection, Renderer renderer)
	{
		this.smallBodyModel = smallBodyModel;
		this.historyCollection = historyCollection;
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

		addListener((aSource, aEventType) ->
		{
			try {
				if (aEventType != ItemEventType.ItemsSelected) return;
				for (StateHistory history : getAllItems())
				{
					if (history.getTrajectoryMetadata().getTrajectory() == null) continue;
					history.getTrajectoryMetadata().getTrajectory().setFaded(!getSelectedItems().contains(history));
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

	/**
	 * @param distanceTextFont
	 */
	public void setSunTextFont(Font distanceTextFont)
	{
		sunDirectionMarker.setStringFont(distanceTextFont);
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

	/**
	 * @param distanceTextFont
	 */
	public void setEarthTextFont(Font distanceTextFont)
	{
		earthDirectionMarker.setStringFont(distanceTextFont);
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
	 * @param distanceTextFont
	 */
	public void setSpacecraftLabelTextFont(Font distanceTextFont)
	{
		spacecraft.setStringFont(distanceTextFont);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spacecraft);
	}

	/**
	 * @param distanceTextFont
	 */
	public void setSpacecraftTextFont(Font distanceTextFont)
	{
		scDirectionMarker.setStringFont(distanceTextFont);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, scDirectionMarker);
	}

	/**
	 * @param distanceTextFont
	 */
	public Font getSpacecraftTextFont()
	{
		return scDirectionMarker.getStringFont();
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
		run.getMetadata().setMapped(true);
		historyCollection.addRun(run);
		updateFovs(run);
		IStateHistoryTrajectoryMetadata trajectoryMetadata = run.getTrajectoryMetadata();
		// Get the trajectory actor the state history segment
		TrajectoryActor trajectoryActor = new TrajectoryActor(trajectoryMetadata.getTrajectory());
		stateHistoryToRendererMap.put(run, trajectoryActor);
		setVisibility(run, true);

		trajectoryActor.setMinMaxFraction(trajectoryMetadata.getTrajectory().getMinDisplayFraction(), trajectoryMetadata.getTrajectory().getMaxDisplayFraction());
		trajectoryActor.VisibilityOn();
		trajectoryActor.GetMapper().Update();

		boolean instrumentPointingAvailable = trajectoryMetadata.getTrajectory().isHasInstrumentPointingInfo();
		if (historyFootprintMap.get(historyCollection.getCurrentRun()) != null)
			historyFootprintMap.get(run).stream().filter(fprint -> fprint != null).filter(footprint -> footprint.getFootprintActor() != null).forEach(footprint -> {
				if (instrumentPointingAvailable) footprint.setFootprintColor();
			});
		SwingUtilities.invokeLater(() -> {
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajectoryActor);
			notifyListeners(this, ItemEventType.ItemsChanged);
			notifyListeners(this, ItemEventType.ItemsSelected);
		});

		return trajectoryActor;
	}

	public TrajectoryActor updateRun(StateHistory run)
	{
		IStateHistoryTrajectoryMetadata trajectoryMetadata = run.getTrajectoryMetadata();
		// Get the trajectory actor the state history segment
		TrajectoryActor trajectoryActor = new TrajectoryActor(trajectoryMetadata.getTrajectory());
		stateHistoryToRendererMap.put(run, trajectoryActor);
		setVisibility(run, true);
		trajectoryActor.setMinMaxFraction(trajectoryMetadata.getTrajectory().getMinDisplayFraction(), trajectoryMetadata.getTrajectory().getMaxDisplayFraction());
		trajectoryActor.VisibilityOn();
		trajectoryActor.GetMapper().Update();
		boolean instrumentPointingAvailable = trajectoryMetadata.getTrajectory().isHasInstrumentPointingInfo();
		if (historyFootprintMap.get(historyCollection.getCurrentRun()) != null)
			historyFootprintMap.get(run).stream().filter(fprint -> fprint != null).filter(footprint -> footprint.getFootprintActor() != null).forEach(footprint -> {
				if (instrumentPointingAvailable) footprint.setFootprintColor();
			});
		SwingUtilities.invokeLater(() -> {
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, trajectoryActor);
			notifyListeners(this, ItemEventType.ItemsChanged);
			notifyListeners(this, ItemEventType.ItemsSelected);
		});

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
		run.getMetadata().setMapped(false);
		run.getMetadata().setVisible(false);
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
		stateHistory.getMetadata().setVisible(visibility);
		TrajectoryActor renderer = stateHistoryToRendererMap.get(stateHistory);
		int isVisible = (visibility == true) ? 1 : 0;
		renderer.SetVisibility(isVisible);
		SwingUtilities.invokeLater(() -> {
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
		});
	}

	/**
	 * @param segment
	 * @param color
	 */
	public void setTrajectoryColor(StateHistory segment, Color color)
	{
		segment.getTrajectoryMetadata().getTrajectory().setColor(color);
		TrajectoryActor renderer = stateHistoryToRendererMap.get(segment);
		renderer.setColoringFunction(null, null);
		renderer.setTrajectoryColor(color);
		refreshColoring(segment);
	}

	public void refreshColoring()
	{
		refreshColoring(historyCollection.getCurrentRun());
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
		renderer.setColoringProvider(tmpProp.activeCP);
		SwingUtilities.invokeLater(() -> {
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, renderer);
		});
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
		if (run.getLocationProvider().getPointingProvider() == null) return;
		int numberOfInstruments = run.getLocationProvider().getPointingProvider().getInstrumentNames().length;
		if (numberOfInstruments == 0) return;
		Color color = PlannedDataActorFactory.getColorForInstrument(instName);
		PerspectiveImageFrustum fov = instrumentNameToFovMap.get(instName);
		if (fov == null)
		{
			fov = new PerspectiveImageFrustum(1, 0, 0, true, diagonalLength);
			instrumentNameToFovMap.put(instName, fov);
			fov.getFrustumActor().VisibilityOff();
			fov.setColor(color);
			fov.setInstrumentName(instName);
		}

		ArrayList<PerspectiveImageFrustum> frusta = historySpacecraftFovMap.get(run);
		if (frusta == null)
		{
			frusta = new ArrayList<PerspectiveImageFrustum>();
		}
		frusta.add(fov);
		historySpacecraftFovMap.put(run, frusta);
		positionCalculator.updateFOVLocations(run, historySpacecraftFovMap.get(historyCollection.getCurrentRun()));
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, fov);
	}

	public void makeFootprint(StateHistory run, String instName)
	{
		if (run.getLocationProvider().getPointingProvider() == null) return;
		int numberOfInstruments = run.getLocationProvider().getPointingProvider().getInstrumentNames().length;
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

		}
		ArrayList<PerspectiveImageFootprint> footprints = historyFootprintMap.get(run);
		if (footprints == null) footprints = new ArrayList<PerspectiveImageFootprint>();
		if (!footprints.contains(fprint)) footprints.add(fprint);
		historyFootprintMap.put(run, footprints);
		positionCalculator.updateFootprintLocations(run, historyFootprintMap.get(historyCollection.getCurrentRun()));
		LiveColorableManager.updateFootprint(fprint);
//		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, run);
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

	public boolean getInstrumentFrustumVisibility(String name)
	{
		if (historySpacecraftFovMap.get(historyCollection.getCurrentRun()) == null || historySpacecraftFovMap.get(historyCollection.getCurrentRun()).isEmpty()) return false;

		List<PerspectiveImageFrustum> fovs = historySpacecraftFovMap.get(historyCollection.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fovs.size() == 0) return false;

		return fovs.get(0).isShowFrustum();
//		return fovs.get(0).getFrustumActor().GetVisibility() == 1 ? true : false;
	}

	public void setInstrumentFrustumVisibility(String name, boolean isVisible)
	{
		if (historySpacecraftFovMap.get(historyCollection.getCurrentRun()) == null || historySpacecraftFovMap.get(historyCollection.getCurrentRun()).isEmpty()) return;
		List<PerspectiveImageFrustum> fovs = historySpacecraftFovMap.get(historyCollection.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fovs.size() == 0) return;
		fovs.get(0).setShowFrustum(isVisible);
//		fovs.get(0).getFrustumActor().SetVisibility(isVisible? 1: 0);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, historySpacecraftFovMap.get(historyCollection.getCurrentRun()));
	}

	public Color getInstrumentFrustumColor(String name)
	{
		if (historySpacecraftFovMap.get(historyCollection.getCurrentRun()) == null || historySpacecraftFovMap.get(historyCollection.getCurrentRun()).isEmpty()) return Color.white;
		List<PerspectiveImageFrustum> fovs = historySpacecraftFovMap.get(historyCollection.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fovs.size() == 0) return Color.white;
		return fovs.get(0).getColor();
	}

	public void setInstrumentFrustumColor(String name, Color color)
	{
		historySpacecraftFovMap.get(historyCollection.getCurrentRun()).stream().filter(item -> item.getInstrumentName().equals(name)).forEach(item -> {
			item.setColor(color);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
	}


	//***************************************
	//Footprint related
	//***************************************
	public void setFootprintPlateColoring(String name)
	{
		historyFootprintMap.get(historyCollection.getCurrentRun()).forEach(item -> {
			if (name.equals("")) item.setPlateColoringName(null);
			else item.setPlateColoringName(name);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		updateVisibilities();
	}

	public void setInstrumentFootprintColor(String name, Color color)
	{
		if (historyFootprintMap.get(historyCollection.getCurrentRun()) == null || historyFootprintMap.get(historyCollection.getCurrentRun()).isEmpty()) return;
		historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).forEach(item -> {
			item.setBoundaryColor(color);
			item.setColor(color);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
	}

	public boolean getInstrumentFootprintVisibility(String name)
	{
		if (historyFootprintMap.get(historyCollection.getCurrentRun()) == null || historyFootprintMap.get(historyCollection.getCurrentRun()).isEmpty()) return false;
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fp.size() == 0) return false;
		return fp.get(0).isShowFootprint();
//		return fp.get(0).isVisible();
	}

	public void setInstrumentFootprintVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
//		fp.get(0).setVisible(isVisible);
		LiveColorableManager.updateFootprint(fp.get(0));
		fp.get(0).setShowFootprint(isVisible);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, fp.get(0));
	}

	public boolean getInstrumentFootprintBorderVisibility(String name)
	{
		if (historyFootprintMap.get(historyCollection.getCurrentRun()) == null || historyFootprintMap.get(historyCollection.getCurrentRun()).isEmpty()) return false;
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
		if (fp.size() == 0) return false;
		return fp.get(0).getFootprintBoundaryActor().GetVisibility() == 1 ? true : false;
	}

	public void setInstrumentFootprintBorderVisibility(String name, boolean isVisible)
	{
		List<PerspectiveImageFootprint> fp = historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint.getInstrumentName().equals(name)).collect(Collectors.toList());
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
		historySpacecraftFovMap.get(historyCollection.getCurrentRun()).stream().filter(item -> item.getFrustumActor() != null).forEach(item -> {
			item.getFrustumActor().SetVisibility(selectedFOVs.contains(item.getInstrumentName()) ? 1 : 0);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint != null).forEach(item -> {
			item.setBoundaryVisible(selectedFOVs.contains(item.getInstrumentName()));
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
		historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint != null).forEach(item -> {
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
		if (historyCollection.getCurrentRun() == null) return;
		if (timeBarActor == null) return;
		double time = historyCollection.getCurrentRun().getMetadata().getCurrentTime();
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
		if (!historySpacecraftFovMap.isEmpty() && (historySpacecraftFovMap.get(historyCollection.getCurrentRun()) != null))
		{
			historySpacecraftFovMap.get(historyCollection.getCurrentRun()).stream().filter(fov -> fov != null && fov.getFrustumActor() != null).forEach(fov -> props.add(fov.getFrustumActor()));
		}
		if (!historyFootprintMap.isEmpty() && historyFootprintMap.get(historyCollection.getCurrentRun()) != null)
		{
			historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint != null).filter(fov -> fov.getFootprintActor() != null).forEach(footprint -> props.add(footprint.getFootprintActor()));
			historyFootprintMap.get(historyCollection.getCurrentRun()).stream().filter(fprint -> fprint != null).filter(fov -> fov.getFootprintActor() != null).forEach(footprint -> props.add(footprint.getFootprintBoundaryActor()));
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
	}

	/**
	*
	*/
	public void setTimeFraction(Double timeFraction, StateHistory state) throws LockableEphemerisLinkEvaluationException
	{
		updateFovs(state);
		if (state != null && spacecraft.getActor() != null )
		{
			positionCalculator.updateSpacecraftPosition(state, timeFraction, spacecraft, scDirectionMarker,
														spacecraftLabelActor);
			positionCalculator.updateEarthPosition(state, timeFraction, earthDirectionMarker);
			positionCalculator.updateSunPosition(state, timeFraction, sunDirectionMarker);
			if (historySpacecraftFovMap.get(historyCollection.getCurrentRun()) != null)
				positionCalculator.updateFOVLocations(state, historySpacecraftFovMap.get(historyCollection.getCurrentRun()));

			if (historyFootprintMap.get(historyCollection.getCurrentRun()) != null)
				positionCalculator.updateFootprintLocations(state, historyFootprintMap.get(historyCollection.getCurrentRun()));

			updateLookDirection(lookDirection);
			if ((renderer.getLightCfg().getType() == LightingType.FIXEDLIGHT && historyCollection.getCurrentRun() != null) == false)
			{
				SwingUtilities.invokeLater(() -> {
					propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
				});
				return;
			}
			renderer.setLightCfgToFixedLightAtDirection(new Vector3D(historyCollection.getCurrentRun().getLocationProvider().getSunPosition()));
			SwingUtilities.invokeLater(() -> {
				propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
			});

		}
//		Logger.getAnonymousLogger().log(Level.INFO, "!!!!!!!!!!!!!!!!!Set time fraction");
	}

	/**
	 * Updates the look direction based on the selected option in user interface
	 *
	 * @param historyCollection			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 * @param model			The model that contains information about the view options
	 */
	public void updateLookDirection(RendererLookDirection lookDirection)
	{
		this.lookDirection = lookDirection;
		StateHistoryCollection runs = getHistoryCollection();
		if (runs.getCurrentRun() == null) return; // can't do any view things if we don't have a trajectory / time history
		Renderer renderer = getRenderer();
		double[] upVector = { 0, 0, 1 };

		Vector3D targOrig = new Vector3D(renderer.getCameraFocalPoint());
		Vector3D targAxis = new Vector3D(positionCalculator.updateLookDirection(lookDirection, scalingFactor));
		renderer.setCameraFocalPoint(new double[]{ 0, 0, 0 });
		double[] lookFromDirection;
		if (lookDirection == RendererLookDirection.FREE_VIEW)
		{
			double previousDistance = CameraUtil.calcDistance(renderer.getCamera());
			lookFromDirection = renderer.getCamera().getPosition().toArray();
			MathUtil.vscl(previousDistance/new Vector3D(lookFromDirection).getNorm(), lookFromDirection, lookFromDirection);
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(),
					renderer.getCamera().getUpUnit().toArray(), renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(false, Vector3D.ZERO, targOrig);
		}
		else
		{
			double previousDistance = CameraUtil.calcDistance(renderer.getCamera());
			lookFromDirection = positionCalculator.updateLookDirection(lookDirection, scalingFactor);
			MathUtil.vscl(previousDistance/new Vector3D(lookFromDirection).getNorm(), lookFromDirection, lookFromDirection);
			renderer.setCameraOrientation(lookFromDirection, renderer.getCameraFocalPoint(), upVector,
					renderer.getCameraViewAngle());
			((RenderPanel) renderer.getRenderWindowPanel()).setZoomOnly(true, targAxis, targOrig);

		}
		renderer.getRenderWindowPanel().resetCameraClippingRange();
	}

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
		return getFeatureAttrFor(history.getTrajectoryMetadata().getTrajectory(), aFeatureType, i);
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
					StateHistoryPositionCalculator.getSpacecraftRange(trajectory.getHistory(), currentInstrument, null, nextTime);
			dataArray.InsertNextValue(range);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.SubSCIncidence)
		{
			double incidence =
					StateHistoryPositionCalculator.getIncidenceAngle(trajectory.getHistory(), currentInstrument, null, nextTime);
			dataArray.InsertNextValue(incidence);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.SubSCEmission)
		{
			double emission =
					StateHistoryPositionCalculator.getEmissionAngle(trajectory.getHistory(), currentInstrument, null, nextTime);
			dataArray.InsertNextValue(emission);
			return new VtkFeatureAttr(dataArray);
		}
		else if (aFeatureType == StateHistoryFeatureType.SubSCPhase)
		{
			double phase =
					StateHistoryPositionCalculator.getPhaseAngle(trajectory.getHistory(), currentInstrument, null, nextTime);
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
		return ImmutableList.copyOf(historyCollection.getSimRuns());
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return historyCollection.getSimRuns().size();
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<StateHistory> history)
	{
		for (StateHistory hist : getAllItems())
		{
			if (hist.getMetadata().isMapped() == false) continue;
			setVisibility(hist, history.contains(hist));
		}
	}

	@Override
	public void setAllItems(Collection<StateHistory> aItemC)
	{
		// Clear relevant state vars
		propM = new HashMap<>();
		// Setup the initial props for all the items
		int tmpIdx = 0;
		int numItems = aItemC.size();
		for (StateHistory aItem : aItemC)
		{
			ColorProvider tmpSrcCP = sourceGCP.getColorProviderFor(aItem, tmpIdx, numItems);

			StateHistoryRenderProperties tmpProp = new StateHistoryRenderProperties();
			tmpProp.isVisible = false;
			tmpIdx++;

			propM.put(aItem, tmpProp);
		}
		notifyListeners(this, ItemEventType.ItemsChanged);
		notifyListeners(this, ItemEventType.ItemsSelected);

	}

	public void installCustomColorProvider(Collection<StateHistory> aItemC, ColorProvider colorProvider)
	{
		for (StateHistory aItem : aItemC)
		{
			StateHistoryRenderProperties tmpProp = propM.get(aItem);
			tmpProp.lastActive = tmpProp.activeCP;
			tmpProp.customCP = colorProvider;
			tmpProp.activeCP = colorProvider;
			propM.put(aItem, tmpProp);
			refreshColoring(aItem);
		}
	}

	public void installGroupColorProviders(GroupColorProvider aSrcGCP/*, StateHistoryCollection runs*/)
	{
		int tmpIdx = -1;
		for (StateHistory aItem : getAllItems())
		{
			tmpIdx++;

			// Skip to next if no RenderProp
			StateHistoryRenderProperties tmpProp = propM.get(aItem);
			if (tmpProp == null)
				return;

			// Skip to next if custom
			if (tmpProp.isCustomCP == true)
				return;

			ColorProvider provider = aSrcGCP.getColorProviderFor(aItem, tmpIdx, getNumItems());
			if (provider instanceof SimpleColorProvider)
			{
				tmpProp.simpleCP = provider;

				if (tmpProp.customCP == null)
				{
					tmpProp.activeCP = tmpProp.simpleCP;
				}
				else
				{
					tmpProp.activeCP = tmpProp.customCP;
				}

			}
			else
			{
				tmpProp.featureCP =  provider;
				tmpProp.activeCP = tmpProp.featureCP;
//				tmpProp.lastActive = tmpProp.featureCP;
			}
			refreshColoring(aItem);

		}
	}

	public ColorProvider getColorProviderForStateHistory(StateHistory history)
	{
		return propM.get(history).activeCP;
	}

	public boolean hasCustomColor(StateHistory history)
	{
		return propM.get(history).customCP != null;
	}

	public void clearCustomColor(Collection<StateHistory> history)
	{
		for (StateHistory hist : history)
		{
			propM.get(hist).customCP = null;
			propM.get(hist).activeCP = propM.get(hist).lastActive;
			refreshColoring(hist);
		}
	}

	public String getPlateColoringForInstrument(String fov)
	{
		if (historyCollection.getCurrentRun() instanceof StandardStateHistory) return null;
		return ((SpiceStateHistory)historyCollection.getCurrentRun()).getPlateColoringForInstrument(fov);
	}

	public void setPlateColoringForInstrument(String instrument, String plateColoring)
	{
		if (historyCollection.getCurrentRun() instanceof StandardStateHistory) return;
		((SpiceStateHistory)historyCollection.getCurrentRun()).setPlateColoringForInstrument(plateColoring, instrument);

		historyFootprintMap.get(historyCollection.getCurrentRun()).forEach(item -> {
//			if (name.equals("")) item.setPlateColoringName(null);
			if (item.getInstrumentName().equals(instrument)) item.setPlateColoringName(plateColoring);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, item);
		});
//		updateVisibilities();
	}

	public int getNumMappedTrajectories()
	{
		return (int)(stateHistoryToRendererMap.keySet().stream().filter(state -> state.getMetadata().isMapped()).count());
	}

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
}