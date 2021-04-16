package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFrame;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.stateHistory.controllers.imagers.PlannedImageTableController;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;

import glum.item.ItemEventType;

public class PlannedImageScheduleCollection extends SaavtkItemManager<PlannedImageCollection> implements PropertyChangeListener
{
	protected List<PlannedImageCollection> plannedData = new ArrayList<PlannedImageCollection>();
	protected List<vtkProp> footprintActors = new ArrayList<vtkProp>();

	protected List<PlannedDataActor> plannedDataActors = new ArrayList<PlannedDataActor>();
	protected Hashtable<PlannedImageCollection, JFrame> fullScheduleFrames = new Hashtable<PlannedImageCollection, JFrame>();

	public PlannedImageScheduleCollection()
	{
	}

	@Override
	public List<vtkProp> getProps()
	{
		return footprintActors;
	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (!Properties.MODEL_CHANGED.equals(evt.getPropertyName())) return;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void addCollection(PlannedImageCollection data)
	{
		plannedData.add(data);
		setAllItems(plannedData);
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<PlannedImageCollection> getAllItems()
	{
		return ImmutableList.copyOf(plannedData);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return plannedData.size();
	}

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
