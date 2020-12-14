package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.util.LidarTrackUtil;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedLidarTrackIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.item.IncrIdGenerator;

public class PlannedLidarTrackCollection extends BasePlannedDataCollection<PlannedLidarTrack>
//extends SaavtkItemManager<PlannedLidarTrack> implements PropertyChangeListener
{
	/**
	*
	*/
//	private List<PlannedLidarTrack> plannedLidarTracks = new ArrayList<PlannedLidarTrack>();
//	private List<vtkProp> footprintActors = new ArrayList<vtkProp>();

//	private List<PlannedDataActor> plannedDataActors = new ArrayList<PlannedDataActor>();

//	private PlannedInstrumentRendererManager renderManager;

//	private SmallBodyModel smallBodyModel;

//	private StateHistory stateHistorySource;

	private LidarTrackManager trackManager;

	private List<LidarTrack> currentTracks;

	private double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE;

	public PlannedLidarTrackCollection(ModelManager modelManager, SmallBodyModel smallBodyModel, Renderer renderer)
	{
		super(smallBodyModel);
//		this.smallBodyModel = smallBodyModel;
		currentTracks = new ArrayList<LidarTrack>();
//		renderManager = new PlannedInstrumentRendererManager(this.pcs);

		trackManager = new LidarTrackManager(modelManager, smallBodyModel);
		renderer.addVtkPropProvider(trackManager);

//		// Manually register for events of interest
//		aPickManager.getDefaultPicker().addListener(tmpTrackManager);
//		aPickManager.getDefaultPicker().addPropProvider(tmpTrackManager);
	}

	@Override
	public List<vtkProp> getProps()
	{
		return trackManager.getProps();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		if (PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName()))
		{
			double time = (double)evt.getNewValue();
			double currentWindowDuration = maxTime - minTime;
			double currentOffsetFromMin = time - minTime;
			trackManager.setPercentageShown(0, currentOffsetFromMin/currentWindowDuration);
			System.out.println("PlannedLidarTrackCollection: propertyChange: time tick: " + TimeUtil.et2str(time) + " and percentage " + (currentOffsetFromMin/currentWindowDuration));

//			if (stateHistorySource == null) return;
//			System.out.println("PlannedLidarTrackCollection: propertyChange: updating " + plannedDataActors.size());
//			for (PlannedDataActor actor : plannedDataActors)
//			{
//				System.out.println("PlannedLidarTrackCollection: propertyChange: actor time " + TimeUtil.et2str(actor.getTime()) + " and time " + TimeUtil.et2str(time) + " for actor " + actor.getClass().getName() + '@' + Integer.toHexString(actor.hashCode()));
////				if (actor.getTime() > time) break;
//				if (((PerspectiveImageFootprint)actor).isStaticFootprintSet() == false)
//				{
//					StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
//
////					StateHistoryPositionCalculator.updateLidarFootprintPointing(stateHistorySource, time, (PlannedLidarActor)actor, smallBodyModel);
//				}
//				System.out.println("PlannedLidarTrackCollection: propertyChange: time > actor time " + time + " actor time " + actor.getTime() + " " + (time > actor.getTime()));
//				actor.getFootprintBoundaryActor().SetVisibility(time > actor.getTime() ? 1 : 0);
//				actor.SetVisibility(1);
//			}
			this.pcs.firePropertyChange("PLANNED_LIDAR_CHANGED", null, null);

		}
	}

	/**
	 * @param run
	 */
	public void addLidarTrackToList(PlannedLidarTrack track)
	{
		if (stateHistorySource == null) return;

		IncrIdGenerator idGenerator = new IncrIdGenerator(0);
		plannedData.add(track);
		minTime = Math.min(minTime, track.getStartTime());
		maxTime = Math.max(maxTime, track.getStopTime());
		double trackDuration = track.getStopTime() - track.getStartTime();
		int numSteps = (int)((track.getStopTime() - track.getStartTime())*100.0/1.0);
		double timeDelta = trackDuration/(double)numSteps;
		System.out.println("PlannedLidarTrackCollection: addLidarTrackToList: number of steps " + numSteps);
		System.out.println("PlannedLidarTrackCollection: addLidarTrackToList: trackDuration " + trackDuration);
		double time = track.getStartTime();
		List<LidarPoint> lidarPoints = new ArrayList<LidarPoint>();
		Logger.getAnonymousLogger().log(Level.INFO, "Making points");
//		lidarTimes.parallelStream().forEach(timeStep -> {lidarPoints.add(StateHistoryPositionCalculator.updateLidarFootprintPointing(stateHistorySource, timeStep, smallBodyModel, track.getInstrumentName()));});
		while (time < track.getStopTime())
		{
			LidarPoint point = StateHistoryPositionCalculator.updateLidarFootprintPointing(stateHistorySource, time, smallBodyModel, track.getInstrumentName());
			if (point != null)
				lidarPoints.add(point);
			time += timeDelta;
		}
		Logger.getAnonymousLogger().log(Level.INFO, "Done making points, number of lidar points " + lidarPoints.size());

		Set<String> tmpSourceS = new LinkedHashSet<>();
		tmpSourceS.add("Planned track " + track.getStartTime() + " " + track.getStopTime());
		LidarTrack composedTrack = LidarTrackUtil.formTrack(idGenerator, lidarPoints, tmpSourceS, 1);
		currentTracks.add(composedTrack);
		trackManager.setAllItems(currentTracks);
		setVisibility(track, true);
		trackManager.setPercentageShown(0, 0);
		setAllItems(plannedData);
		this.pcs.firePropertyChange("PLANNED_LIDAR_CHANGED", null, null);
	}

	public void setVisibility(PlannedLidarTrack track, boolean visibility)
	{
		int index = plannedData.indexOf(track);
		LidarTrack lidarTrack = currentTracks.get(index);
		List<LidarTrack> trackList = new ArrayList<LidarTrack>();
		trackList.add(lidarTrack);
		trackManager.setIsVisible(trackList, visibility);
//
//		renderManager.setVisibility(track, visibility);
	}

	public void loadPlannedLidarTracksFromFileWithName(String filename) throws IOException
	{
		PlannedLidarTrackIOHelper.loadPlannedLidarTracksFromFileWithName(filename, this);
	}

	public void savePlannedLidarTracksToFileWithName(String filename) throws IOException
	{
		PlannedLidarTrackIOHelper.savePlannedLidarTracksToFileWithName(filename, this);
	}
}