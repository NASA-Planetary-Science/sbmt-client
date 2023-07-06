package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;

public class PlannedLidarTrackVtkCollection extends BasePlannedDataCollection<PlannedLidarTrack>
{

	private LidarTrackManager trackManager;
	private double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE;
	private List<LidarTrack> currentTracks;
	private PlannedLidarTrackCollection currentTracksCollection;

	public PlannedLidarTrackVtkCollection(String filename, PlannedLidarTrackCollection currentTracksCollection, ModelManager modelManager, SmallBodyModel smallBodyModel, Renderer renderer, StatusNotifier statusNotifier)
	{
		super(smallBodyModel);
		this.currentTracks = new ArrayList<LidarTrack>();
//		this.currentTracks = currentTracksCollection.getCurrentTracks();
		this.currentTracksCollection = currentTracksCollection;
		this.filename = filename;
		trackManager = new LidarTrackManager(renderer, statusNotifier, smallBodyModel);
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

	public void addTrack(LidarTrack track, PlannedLidarTrack plannedTrack)
	{
		currentTracks.add(track);
		plannedData.add(plannedTrack);
		renderFootprints();
	}

	public void renderFootprints()
	{
//		plannedData = currentTracksCollection.getCurrentPlannedTracks();
		trackManager.setAllItems(currentTracks);
		setAllItems(plannedData);
		Logger.getAnonymousLogger().log(Level.INFO, "Number of tracks " + plannedData.size());
		for (PlannedLidarTrack track : plannedData)
		{
			setVisibility(track, true);
		}
		SwingUtilities.invokeLater(() -> {
			this.pcs.firePropertyChange("PLANNED_LIDAR_CHANGED", null, null);
		});
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