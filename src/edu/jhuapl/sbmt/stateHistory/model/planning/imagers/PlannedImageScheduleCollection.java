package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import edu.jhuapl.sbmt.stateHistory.controllers.imagers.PlannedImageTableController;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataScheduleCollection;

public class PlannedImageScheduleCollection extends BasePlannedDataScheduleCollection<PlannedImageCollection>
{

	public PlannedImageScheduleCollection()
	{
	}

	@Override
	public void showDetailedScheduleFor(PlannedImageCollection collection)
	{
		JFrame frame = fullScheduleFrames.get(collection);
		if (frame == null)
		{
			frame = new JFrame("Schedule Details - " + collection.getFilename());
			frame.add(new PlannedImageTableController(collection).getView());
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
