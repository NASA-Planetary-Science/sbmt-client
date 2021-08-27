package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.util.LidarTrackUtil;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;

import glum.item.IncrIdGenerator;

public class PlannedLidarTrackCollection extends BasePlannedDataCollection<PlannedLidarTrack>
{

	private LidarTrackManager trackManager;

	private List<LidarTrack> currentTracks;

	private double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE;


	public PlannedLidarTrackCollection(String filename, ModelManager modelManager, SmallBodyModel smallBodyModel, Renderer renderer)
	{
		super(smallBodyModel);
		currentTracks = new ArrayList<LidarTrack>();
		this.filename = filename;
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
			time = (double)evt.getNewValue();
			updateFootprints();

		}
	}

	@Override
	public void updateFootprints()
	{
		if (time == 0) time = minTime;
		double currentWindowDuration = maxTime - minTime;
		double currentOffsetFromMin = time - minTime;
		trackManager.setPercentageShown(0, currentOffsetFromMin/currentWindowDuration);
		this.pcs.firePropertyChange("PLANNED_LIDAR_CHANGED", null, null);
	}

	/**
	 * @param run
	 */
	public void addLidarTrackToList(PlannedLidarTrack track, ProgressStatusListener listener)
	{
		if (stateHistorySource == null) return;

		IncrIdGenerator idGenerator = new IncrIdGenerator(0);
		plannedData.add(track);
		minTime = Math.min(minTime, track.getStartTime());
		maxTime = Math.max(maxTime, track.getStopTime());
		double trackDuration = track.getStopTime() - track.getStartTime();
		int numSteps = (int)((track.getStopTime() - track.getStartTime())*100.0/1.0);
		double timeDelta = trackDuration/(double)numSteps;
		double time = track.getStartTime();
		List<LidarPoint> lidarPoints = new ArrayList<LidarPoint>();
		Logger.getAnonymousLogger().log(Level.INFO, "Making points");
		int numPoints = (int)((track.getStopTime() - time)/timeDelta);
		int i=0;
		listener.setProgressStatus("Track: " + plannedData.size(), 0);
		while (time <= track.getStopTime())
		{
			LidarPoint point = StateHistoryPositionCalculator.updateLidarFootprintPointing(stateHistorySource, time, smallBodyModel, track.getInstrumentName());
			if (point != null)
			{
				listener.setProgressStatus("Track: " + plannedData.size() + ": point " + (i+1) + " of " + numPoints, (i*100/numPoints));
				lidarPoints.add(point);
				i++;
			}
			time += timeDelta;
		}
		listener.setProgressStatus("Ready", 0);

		Set<String> tmpSourceS = new LinkedHashSet<>();
		tmpSourceS.add("Planned track " + track.getStartTime() + " " + track.getStopTime());
		LidarTrack composedTrack = LidarTrackUtil.formTrack(idGenerator, lidarPoints, tmpSourceS, 1);
		currentTracks.add(composedTrack);
		trackManager.setAllItems(currentTracks);
		setVisibility(track, true);
		setAllItems(plannedData);
		this.pcs.firePropertyChange("PLANNED_LIDAR_CHANGED", null, null);
	}

	private void setVisibility(PlannedLidarTrack track, boolean visibility)
	{
		int index = plannedData.indexOf(track);
		LidarTrack lidarTrack = currentTracks.get(index);
		List<LidarTrack> trackList = new ArrayList<LidarTrack>();
		trackList.add(lidarTrack);
		trackManager.setIsVisible(trackList, visibility);
	}

	public void setPercentageShown(double percShown)
	{
		trackManager.setPercentageShown(0, percShown);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

}