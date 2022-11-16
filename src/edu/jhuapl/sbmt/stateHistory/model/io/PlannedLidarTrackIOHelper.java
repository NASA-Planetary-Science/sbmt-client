package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.ThreadService;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackVtkCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

public class PlannedLidarTrackIOHelper
{

	public static void main(String[] args) throws Exception
	{
		ThreadService.initialize(100);
//    	ShapeModelBody body = ShapeModelBody.APOPHIS;
//        ShapeModelType type = ShapeModelType.provide("Apophis");
//    	boolean aplVersion = true;
//        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
//        String rootURL = safeUrlPaths.getUrl("https://sbmt.jhuapl.edu/sbmt/prod/");
//    	Configuration.setAPLVersion(aplVersion);
//        Configuration.setRootURL(rootURL);
//
//        SbmtMultiMissionTool.configureMission();
//
//         // authentication
//        Configuration.authenticate();
//        NativeLibraryLoader.loadVtkLibraries();
//         // initialize view config
//        SmallBodyViewConfig.initialize();
//    	SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(body, type);
//    	SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
//		StateHistoryPositionCalculator posCalc = new StateHistoryPositionCalculator(smallBodyModel);
////		SpiceInfo.initializeSerializationProxy();
////		SpiceStateHistory.initializeSerializationProxy();
//		StateHistoryKey key = new StateHistoryKey(1);
//		StateHistory stateHistory = StateHistoryModelIOHelper.loadStateHistoryFromFile(new File("/Users/steelrj1/Desktop/Segment_1.spicestate"), null, key);
//		stateHistory.getLocationProvider().reloadPointingProvider();
//		PlannedLidarTrackCollection trackCollection = new PlannedLidarTrackCollection("/Users/steelrj1/Downloads/ApophisSimKernels/OLAScanReport1629828638774-1.csv", stateHistory);
//		trackCollection.setListener(new PlannedLidarTrackCollectionListener()
//		{
//
//			@Override
//			public void trackAdded(LidarTrack track, PlannedLidarTrack plannedTrack)
//			{
////				lidarTrackCollection.addTrack(track, plannedTrack);
//			}
//		});
//		PlannedLidarTrackIOHelper.loadPlannedLidarTracksFromFileWithName("/Users/steelrj1/Downloads/ApophisSimKernels/OLAScanReport1629828638774-1.csv", stateHistory.getMetadata(), trackCollection, new ProgressStatusListener()
//		{
//
//			@Override
//			public void setProgressStatus(String status, int progress)
//			{
//				System.out.println(
//						"PlannedLidarTrackIOHelper.main(...).new ProgressStatusListener() {...}: setProgressStatus: status " + status + " progress " + progress);
//			}
//		},
//		() -> {
//			SwingUtilities.invokeLater(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					System.out.println("PlannedLidarTrackIOHelper.main(...).new Runnable() {...}: run: Ready.");
//				}
//			});
//		});
//		Logger.getAnonymousLogger().log(Level.INFO, "DONE");

	}

	/**
	 * Reads in planned images from a list.  Assumes the following format
	 * TimeInET,InstrumentName
	 *
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void loadPlannedLidarTracksFromFileWithName(String filename, IStateHistoryMetadata metadata, PlannedLidarTrackCollection collection, ProgressStatusListener listener, Runnable completion) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		if (FilenameUtils.getExtension(filename).equals("txt"))
		{
			while ((line = reader.readLine()) != null)
			{
				//Skip the headers
				if (line.startsWith("#")) continue;
				//Now we've reached entries - build the
				String[] parts = line.split(",");
				String instrumentName = parts[2];
				double etStart = TimeUtil.str2et(parts[0]);
				double etStop = TimeUtil.str2et(parts[1]);
				PlannedLidarTrack plannedLidarTrack = new PlannedLidarTrack(etStart, etStop, instrumentName.toUpperCase());
				plannedLidarTrack.setStateHistoryMetadata(metadata);
				collection.addLidarTrackToList(plannedLidarTrack, listener);
			}
		}
		else if (FilenameUtils.getExtension(filename).equals("csv"))
		{
			reader.readLine(); //skip the header
			while ((line = reader.readLine()) != null)
			{
				//Skip the headers
				if (line.startsWith("#")) continue;
				//Now we've reached entries - build the
				String[] parts = line.split(",");
				String instrumentName = "HELT";
				double etStart = TimeUtil.str2et(parts[0]);
				double etStop = TimeUtil.str2et(parts[1]);
				PlannedLidarTrack plannedLidarTrack = new PlannedLidarTrack(etStart, etStop, instrumentName.toUpperCase());
				plannedLidarTrack.setStateHistoryMetadata(metadata);
//				try
//				{
//					addToCollection(plannedLidarTrack, collection, listener);
//				} catch (InterruptedException | ExecutionException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				Thread thread = new Thread(new Runnable()
//				{
//
//					@Override
//					public void run()
//					{
						collection.addLidarTrackToList(plannedLidarTrack, listener);
//					}
//				});
//				thread.start();

			}
		}
		reader.close();
		completion.run();
	}

//	private static List<Void> addToCollection(PlannedLidarTrack track, PlannedLidarTrackCollection collection, ProgressStatusListener listener) throws InterruptedException, ExecutionException
//	{
//		ThreadService.initialize(100);
//		List<Void> outputs = new ArrayList<Void>();
//		final List<Future<Void>> resultList;
//		List<Callable<Void>> taskList = new ArrayList<>();
//		double time = track.getStartTime();
//
//		Callable<Void> task = new AddToCollectionTask(track, collection, listener);
//		taskList.add(task);
//
//		resultList = ThreadService.submitAll(taskList);
//		for (int i = 0; i < resultList.size(); i++)
//		{
//			int index = i;
//			SwingUtilities.invokeLater(() -> {
//				listener.setProgressStatus("Track: " + (index+1) + " of " + resultList.size(), (index*100/resultList.size()));
//			});
//			Future<Void> future = resultList.get(i);
//			Void lidarPoint = future.get();
//			if (lidarPoint != null)
//				outputs.add(lidarPoint);
//		}
//		return outputs;
//	}
//
//	public class AddToCollectionTask implements Callable<Void>
//	{
//
//		PlannedLidarTrack track;
//		ProgressStatusListener listener;
//		PlannedLidarTrackCollection collection;
//
//		public AddToCollectionTask(PlannedLidarTrack track, PlannedLidarTrackCollection collection, ProgressStatusListener listener)
//		{
//			this.track = track;
//			this.listener = listener;
//			this.collection = collection;
//		}
//
//		@Override
//		public Void call() throws Exception
//		{
//     		collection.addLidarTrackToList(track, listener);
//     		return null;
//		}
//	}

	/**
	 * Saves current planned image collection list to file
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void savePlannedLidarTracksToFileWithName(String filename, PlannedLidarTrackVtkCollection collection) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		writer.write("## Planned Lidar Track list, updated on " + dtf.format(now));
		writer.newLine();
		for (PlannedLidarTrack image : collection.getAllItems())
		{
			writer.write(image.getTime() + "," + image.getInstrumentName());
			writer.newLine();
		}

		writer.close();
	}
}