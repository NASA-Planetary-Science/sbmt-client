package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.feature.FeatureAttr;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryCollectionChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.TrajectoryActor;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.GroupColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryFeatureType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import glum.item.ItemEventType;

/**
 * Item manager that governs the available state histories for display in the
 * table, and once selected, in the renderer
 *
 * @author steelrj1
 *
 */
public class StateHistoryCollection extends SaavtkItemManager<StateHistory> implements PropertyChangeListener, MetadataManager
{
	/**
	 *
	 */
	private ArrayList<StateHistoryCollectionChangedListener> changeListeners = new ArrayList<StateHistoryCollectionChangedListener>();

	/**
	 *
	 */
	private ArrayList<StateHistoryKey> keys = new ArrayList<StateHistoryKey>();

	/**
	 *
	 */
	private List<StateHistory> simRuns = new ArrayList<StateHistory>();

	/**
	 *
	 */
	private StateHistory currentRun = null;

	/**
	 *
	 */
	private StateHistoryRendererManager renderManager;

	/**
	 *
	 */
	private String bodyName;

	private Vector<String> selectedFOVs;

	private Vector<String> availableFOVs;

	final Key<List<StateHistory>> stateHistoryKey = Key.of("stateHistoryCollection");

//	private ConstGroupColorProvider sourceGCP;

	/**
	 * @param smallBodyModel
	 */
	public StateHistoryCollection(SmallBodyModel smallBodyModel)
	{
		this.renderManager = new StateHistoryRendererManager(smallBodyModel, pcs);
		this.bodyName = smallBodyModel.getConfig().getShapeModelName();
		selectedFOVs = new Vector<String>();
		availableFOVs = new Vector<String>();
	}

	public void addStateHistoryCollectionChangedListener(StateHistoryCollectionChangedListener listener)
	{
		changeListeners.add(listener);
	}

	public void fireHistorySegmentUpdatedListeners(StateHistory history)
	{
		for (StateHistoryCollectionChangedListener listener : changeListeners)
		{
			listener.historySegmentUpdated(history);
		}
	}

	/**
	 * @param key
	 * @return
	 */
	private boolean containsKey(StateHistoryKey key)
	{
		for (StateHistory run : simRuns)
		{
			if (run.getKey().equals(key))
				return true;
		}

		return false;
	}

	/**
	 * @param key
	 * @return
	 */
	private StateHistory getRunFromKey(StateHistoryKey key)
	{
		for (StateHistory run : simRuns)
		{
			if (run.getKey().equals(key))
				return run;
		}

		return null;
	}

	/**
	 * @return
	 */
	public StateHistory getCurrentRun()
	{
		return currentRun;
	}

	/**
	 * @param run
	 */
	public void setCurrentRun(StateHistory run)
	{
		currentRun = run;
	}

	/**
	 * @param run
	 */
	public void addRunToList(StateHistory run)
	{
		simRuns.add(run);
		keys.add(run.getKey());
		setCurrentRun(run);
		setAllItems(simRuns);
	}

	/**
	 * @param run
	 */
	public void removeRunFromList(StateHistory run)
	{
		simRuns.remove(run);
		keys.remove(run.getKey());
		setCurrentRun(simRuns.get(0));
		setAllItems(simRuns);
	}

	public void clearPlannedScience()
	{
		renderManager.clearPlannedScience();
	}

	public void addPlannedScience(List<vtkProp> actors)
	{
		renderManager.addPlannedScienceActors(actors);
	}


//	@Override
//	public void setAllItems(Collection<StateHistory> aItemC)
//	{
//		renderManager.setAllItems(aItemC);
//		// Delegate
//		super.setAllItems(aItemC);
//	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	/**
	 * @param spacecraft
	 */
	public void setSpacecraft(SpacecraftBody spacecraft)
	{
		renderManager.setSpacecraft(spacecraft);
	}

	/**
	 * @param run
	 * @return
	 */
	public TrajectoryActor addRun(StateHistory run)
	{
		availableFOVs.clear();
		selectedFOVs.clear();
		Arrays.stream(run.getPointingProvider().getInstrumentNames()).forEach(inst -> availableFOVs.add(inst));
		System.out.println("StateHistoryCollection: addRun: number of available FOVs " + availableFOVs.size());
		this.currentRun = run;
		notifyListeners(this, ItemEventType.ItemsChanged);
		return renderManager.addRun(run);
	}

	/**
	 * @param key
	 */
	public void removeRun(StateHistoryKey key)
	{
		if (!containsKey(key))
		{
			System.out.println("StateHistoryCollection: removeRun: doesn't contain key " + key);
			return;
		}

		StateHistory run = getRunFromKey(key);
		this.currentRun = null;
		renderManager.removeRun(run);
	}

	/**
	 * @param keys
	 */
	public void removeRuns(StateHistoryKey[] keys)
	{
		for (StateHistoryKey key : keys)
		{
			removeRun(key);
		}
	}

	/**
	 * @param show
	 */
	public void setShowTrajectories(boolean show)
	{
		// TODO fix
		// for (StateHistory run : simRuns)
		// run.setShowSpacecraft(show);
	}

	/**
	 * @param segment
	 * @return
	 */
	public boolean isStateHistoryMapped(StateHistory segment)
	{
		return renderManager.isStateHistoryMapped(segment);
	}

	/**
	 * @param segment
	 * @param color
	 */
	public void setTrajectoryColor(StateHistory segment, Color color)
	{
		renderManager.setTrajectoryColor(segment, color);
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
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		// TODO fix
		// if (currentRun != null)
		// return currentRun.getClickStatusBarText(prop, cellId, pickPosition);
		// else
		return "No simulation run selected";
	}

	public void updateTimeBarValue()
	{
		if (getCurrentRun() == null) return;
		renderManager.updateTimeBarValue(getCurrentRun().getCurrentTime());
	}

	public void updateTimeBarValue(double time)
	{
		renderManager.updateTimeBarValue(time);
	}

	public void updateTimeBarLocation(int width, int height)
	{
		renderManager.updateTimeBarLocation(width, height);
	}

	public void updateStatusBarValue(String text)
	{
		renderManager.updateStatusBarValue(text);
	}

	public void updateStatusBarLocation(int width, int height)
	{
		renderManager.updateStatusBarLocation(width, height);
	}

	/**
	 * @param segment
	 * @return
	 */
	public TrajectoryActor getTrajectoryActorForStateHistory(StateHistory segment)
	{
		return renderManager.getTrajectoryActorForStateHistory(segment);
	}

	/**
	 *
	 */
	public Double getPeriod()
	{
		if (currentRun != null)
			return currentRun.getTimeWindow();
		else
			return 0.0;
	}

	/**
	 * @return
	 */
	public List<StateHistoryKey> getKeys()
	{
		return keys;
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<StateHistory> getAllItems()
	{
		return ImmutableList.copyOf(simRuns);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return simRuns.size();
	}

	public boolean getVisibility(StateHistory segment)
	{
		return renderManager.getVisibility(segment);
	}

	/**
	 * @param segment
	 * @param visibility
	 */
	public void setVisibility(StateHistory segment, boolean visibility)
	{
		renderManager.setVisibility(segment, visibility);
	}

	/**
	 * @param segment
	 */
	public void refreshColoring(StateHistory segment)
	{
		renderManager.refreshColoring(segment);
	}

	/**
	 *
	 */
	public void setTimeFraction(Double timeFraction)
	{
		renderManager.setTimeFraction(timeFraction, getCurrentRun());
		renderManager.updateTimeBarValue(getCurrentRun().getCurrentTime());
	}

	public void addSelectedFov(String fov)
	{
		selectedFOVs.add(fov);
		renderManager.updateFOVVisibility(getSelectedFOVs());
		renderManager.updateFootprintVisibility(getSelectedFOVs());
		renderManager.updateFootprintBoundaryVisibility(getSelectedFOVs());
	}

	public void removeSelectedFov(String fov)
	{
		selectedFOVs.remove(fov);
		renderManager.updateFOVVisibility(getSelectedFOVs());
		renderManager.updateFootprintVisibility(getSelectedFOVs());
		renderManager.updateFootprintBoundaryVisibility(getSelectedFOVs());
	}

	public Vector<String> getSelectedFOVs()
	{
		return selectedFOVs;
	}

	public void addAvailableFov(String fov)
	{
		availableFOVs.add(fov);
	}

	public Vector<String> getAvailableFOVs()
	{
		return availableFOVs;
	}

	public void setFootprintPlateColoring(String coloringName)
	{
		renderManager.setFootprintPlateColoring(coloringName);
	}

	/**
	 * @param visible
	 */
	public void setInstrumentFootprintVisibility(boolean visible)
	{
		renderManager.setInstrumentFootprintVisibility(visible);
	}

	/**
	 * @param visible
	 */
	public void setInstrumentFootprintBorderVisibility(boolean visible)
	{
		renderManager.setInstrumentFootprintBorderVisibility(visible);
	}

	/**
	 * @param visible
	 */
	public void setInstrumentFrustumVisibility(boolean visible)
	{
		renderManager.setInstrumentFrustumVisibility(visible);
	}

	/**
	 * @param color
	 */
	public void setSpacecraftColor(Color color)
	{
		renderManager.setSpacecraftColor(color);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftVisibility(boolean visible)
	{
		renderManager.setSpacecraftVisibility(visible);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftLabelVisibility(boolean visible)
	{
		renderManager.setSpacecraftLabelVisibility(visible);
	}

	/**
	 * @param visible
	 */
	public void setSpacecraftDirectionMarkerVisibility(boolean visible)
	{
		renderManager.setSpacecraftDirectionMarkerVisibility(visible);
	}

	/**
	 * @param radius
	 */
	public void setSpacecraftDirectionMarkerSize(int radius)
	{
		renderManager.setSpacecraftDirectionMarkerSize(radius);
	}

	/**
	 * @param visible
	 */
	public void setEarthDirectionMarkerVisibility(boolean visible)
	{
		renderManager.setEarthDirectionMarkerVisibility(visible);
	}

	/**
	 * @param scale
	 */
	public void setSpacecraftSize(double scale)
	{
		renderManager.setSpacecraftSize(scale);
	}

	public String getBodyName()
	{
		return bodyName;
	}

	/**
	 * @param radius
	 */
	public void setEarthDirectionMarkerSize(int radius)
	{
		renderManager.setEarthDirectionMarkerSize(radius);
	}

	/**
	 * @param visible
	 */
	public void setSunDirectionMarkerVisibility(boolean visible)
	{
		renderManager.setSunDirectionMarkerVisibility(visible);
	}

	/**
	 * @param radius
	 */
	public void setSunDirectionMarkerSize(int radius)
	{
		renderManager.setSunDirectionMarkerSize(radius);
	}

	/**
	 * @param distanceText
	 */
	public void setDistanceText(String distanceText)
	{
		renderManager.setDistanceText(distanceText);
	}

	/**
	 * @param distanceTextFont
	 */
	public void setDistanceTextFont(Font distanceTextFont)
	{
		renderManager.setDistanceTextFont(distanceTextFont);
	}

	/**
	 * @param isVisible
	 */
	public void setDistanceTextVisiblity(boolean isVisible)
	{
		renderManager.setDistanceTextVisiblity(isVisible);
	}

	/**
	 * @param color
	 */
	public void setEarthDirectionMarkerColor(Color color)
	{
		renderManager.setEarthDirectionMarkerColor(color);
	}

	/**
	 * @param color
	 */
	public void setSunDirectionMarkerColor(Color color)
	{
		renderManager.setSunDirectionMarkerColor(color);
	}

	/**
	 * @param color
	 */
	public void setScDirectionMarkerColor(Color color)
	{
		renderManager.setScDirectionMarkerColor(color);
	}

	/**
	 * @return
	 */
	public double[] getCurrentLookFromDirection()
	{
		return renderManager.getCurrentLookFromDirection();
	}

	/**
	 * @param run
	 * @param min
	 * @param max
	 */
	public void setTrajectoryMinMax(StateHistory run, double min, double max)
	{
		renderManager.setTrajectoryMinMax(run, min, max);
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<StateHistory> history)
	{
		for (StateHistory hist : getAllItems())
		{
			renderManager.setVisibility(hist, history.contains(hist));
		}
	}

	public ArrayList<vtkProp> getProps()
	{
		return renderManager.getProps();
	}

	/**
	 * @param lookDirection
	 * @param scalingFactor
	 * @return
	 */
	public double[] updateLookDirection(RendererLookDirection lookDirection)
	{
		return renderManager.updateLookDirection(lookDirection);
	}

	public FeatureAttr getFeatureAttrFor(StateHistory item, StateHistoryFeatureType aFeatureType)
	{
		return renderManager.getFeatureAttrFor(item, aFeatureType);
	}

	public void installGroupColorProviders(GroupColorProvider aSrcGCP)
	{
		renderManager.installGroupColorProviders(aSrcGCP);
	}

	/**
	 * Stores the model to metadata
	 */
	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(stateHistoryKey, simRuns);
    	return result;
	}

	/**
	 * Fetches the model from metadata
	 */
	@Override
	public void retrieve(Metadata source)
	{
		simRuns = source.get(stateHistoryKey);
	}
}
