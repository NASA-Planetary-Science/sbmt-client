package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import edu.jhuapl.sbmt.stateHistory.controllers.lidars.PlannedLidarTableController;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataScheduleCollection;

public class PlannedLidarTrackScheduleCollection extends BasePlannedDataScheduleCollection<PlannedLidarTrackVtkCollection>
{

	public PlannedLidarTrackScheduleCollection()
	{

	}

	public void showDetailedScheduleFor(PlannedLidarTrackVtkCollection collection)
	{
		JFrame frame = fullScheduleFrames.get(collection);
		if (frame == null)
		{
			frame = new JFrame("Schedule Details - " + collection.getFilename());
			frame.add(new PlannedLidarTableController(collection).getView());
			frame.setSize(600, 400);
			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					collection.setDisplayingDetails(false);
				}
			});
			fullScheduleFrames.put(collection, frame);
		}
		frame.setVisible(collection.isDisplayingDetails());
	}
}
