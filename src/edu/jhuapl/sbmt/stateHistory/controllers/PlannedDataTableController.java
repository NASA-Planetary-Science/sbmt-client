package edu.jhuapl.sbmt.stateHistory.controllers;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedDataTableController<P extends JPanel, D extends PlannedInstrumentData> implements IPlannedDataController<P>
{
	protected P view;
	protected BasePlannedDataCollection<D> collection;
	protected boolean allMapped = true;

	public PlannedDataTableController() { }

	protected void refreshView()
	{
		updateButtonState();
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				view.repaint();
	            view.validate();
			}
		});
	}

	protected void updateButtonState()
	{
		ImmutableSet<D> selectedItems = collection.getSelectedItems();

		for (D history : selectedItems)
		{
			if (history.isShowing() == false) allMapped = false;
		}
	}

	/**
	 * Returns the view for this controller
	 * @return the view
	 */
	public P getView()
	{
		return view;
	}
}