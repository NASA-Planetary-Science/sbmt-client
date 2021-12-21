package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.LidarTrack;
import edu.jhuapl.sbmt.lidar.util.LidarTrackUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.util.ThreadService;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.item.IncrIdGenerator;

public class PlannedLidarTrackCollection
{
//	private List<LidarTrack> currentTracks;
//	private List<PlannedLidarTrack> plannedTracks;
	private double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE;
	private String filename;
	private StateHistory stateHistory;
	private PlannedLidarTrackCollectionListener listener;

	public PlannedLidarTrackCollection(String filename, StateHistory stateHistory)
	{
//		currentTracks = new ArrayList<LidarTrack>();
//		plannedTracks = new ArrayList<PlannedLidarTrack>();
		this.filename = filename;
		this.stateHistory = stateHistory;
	}

	public void setListener(PlannedLidarTrackCollectionListener listener)
	{
		this.listener = listener;
	}

//	public List<LidarTrack> getCurrentTracks()
//	{
//		return currentTracks;
//	}
//
//	public List<PlannedLidarTrack> getCurrentPlannedTracks()
//	{
//		return plannedTracks;
//	}

	/**
	 * @param run
	 */
	public void addLidarTrackToList(PlannedLidarTrack track, ProgressStatusListener listener)
	{
		IncrIdGenerator idGenerator = new IncrIdGenerator(0);
		minTime = Math.min(minTime, track.getStartTime());
		maxTime = Math.max(maxTime, track.getStopTime());
		double trackDuration = track.getStopTime() - track.getStartTime();
		int numSteps = (int)((track.getStopTime() - track.getStartTime())*100.0/200.0);
		double timeDelta = trackDuration/(double)numSteps;
		double time = track.getStartTime();
//		Logger.getAnonymousLogger().log(Level.INFO, "Making points");
		int numPoints = (int)((track.getStopTime() - time)/timeDelta);
		List<LidarPoint> lidarPoints = new ArrayList<LidarPoint>(numPoints);
		int i=0;
//		SwingUtilities.invokeLater(() -> {
//			listener.setProgressStatus("Track: " + plannedData.size(), 0);
//		});
		try
		{
			Logger.getAnonymousLogger().log(Level.INFO, "generating points");
			lidarPoints = generatePoints(stateHistory, track, timeDelta, listener);
		}
		catch (InterruptedException | ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		while (time <= track.getStopTime())
//		{
//			LidarPoint point = StateHistoryPositionCalculator.updateLidarFootprintPointing(stateHistory, time, track.getInstrumentName());
//			if (point != null)
//			{
//				int index=i;
//				Logger.getAnonymousLogger().log(Level.INFO, "Making points index " + i + " for " + TimeUtil.et2str(track.getStartTime()));
////				SwingUtilities.invokeLater(() -> {
////					listener.setProgressStatus("Track: " + plannedData.size() + ": point " + (index+1) + " of " + numPoints, (index*100/numPoints));
////				});
//				lidarPoints.add(point);
//				i++;
//			}
//			time += timeDelta;
//		}
		SwingUtilities.invokeLater(() -> {
			listener.setProgressStatus("Ready", 0);
		});
		Set<String> tmpSourceS = new LinkedHashSet<>();
		tmpSourceS.add("Planned track " + track.getStartTime() + " " + track.getStopTime());
		LidarTrack composedTrack = LidarTrackUtil.formTrack(idGenerator, lidarPoints, tmpSourceS, 1);
//		currentTracks.add(composedTrack);
//		plannedTracks.add(track);
		this.listener.trackAdded(composedTrack, track);
		Logger.getAnonymousLogger().log(Level.INFO, "Added track");
	}

	private List<LidarPoint> generatePoints(StateHistory stateHistorySource, PlannedLidarTrack track, double timeDelta, ProgressStatusListener listener) throws InterruptedException, ExecutionException
	{
//		ThreadService.initialize(100);
		List<LidarPoint> outputs = new ArrayList<LidarPoint>();
		final List<Future<LidarPoint>> resultList;
		List<Callable<LidarPoint>> taskList = new ArrayList<>();
		double time = track.getStartTime();
		SwingUtilities.invokeLater(() -> {
			listener.setProgressStatus("Track: processing start " + TimeUtil.et2str(track.getStartTime()), 0);
		});
//		SwingUtilities.invokeLater(() -> {
//			listener.setProgressStatus("Track: " + plannedData.size() + ": point " + (index+1) + " of " + resultList.size(), (index*100/resultList.size()));
//		});
//		System.out.println("PlannedLidarTrackCollection: generatePoints: track stop time is " + track.getStopTime() + " time is " + time + " time delta " + timeDelta);
		while (time <= track.getStopTime())
		{
			Callable<LidarPoint> task = new LidarPointTask(stateHistorySource, time, track.getInstrumentName());
			taskList.add(task);
			time += timeDelta;
		}
//		System.out.println("PlannedLidarTrackCollection: generatePoints: getting result list, task list size " + taskList.size());
		resultList = ThreadService.submitAll(taskList);
		Logger.getAnonymousLogger().log(Level.INFO, "Submitted all tasks");

//		System.out.println("PlannedLidarTrackCollection: generatePoints: got result list");
		for (int i = 0; i < resultList.size(); i++)
		{
//			System.out.println("PlannedLidarTrackCollection: generatePoints: getting back results " + i);
			int index = i;
//			SwingUtilities.invokeLater(() -> {
//				listener.setProgressStatus("Track: " + plannedData.size() + ": point " + (index+1) + " of " + resultList.size(), (index*100/resultList.size()));
//			});
			Future<LidarPoint> future = resultList.get(i);
			LidarPoint lidarPoint = future.get();
//			System.out.println("PlannedLidarTrackCollection: generatePoints: returning " + lidarPoint);
			if (lidarPoint != null)
				outputs.add(lidarPoint);
		}
		Logger.getAnonymousLogger().log(Level.INFO, "Got outputs, size " + outputs.size());
//		System.out.println("PlannedLidarTrackCollection: generatePoints: returning outputs size " + outputs.size());
		return outputs;
	}

	private class LidarPointTask implements Callable<LidarPoint>
	{

		private StateHistory stateHistorySource;
		private double time;
		private String instrumentName;

		public LidarPointTask(StateHistory stateHistorySource, double time, String instrumentName)
		{
			this.stateHistorySource = stateHistorySource;
			this.time = time;
			this.instrumentName = instrumentName;
		}

		@Override
		public LidarPoint call() throws Exception
		{
			LidarPoint point = StateHistoryPositionCalculator.updateLidarFootprintPointing(stateHistorySource, time, instrumentName);
//			System.out.println("PlannedLidarTrackCollection.LidarPointTask: call: returning " + point);
			return point;
		}
	}
}
