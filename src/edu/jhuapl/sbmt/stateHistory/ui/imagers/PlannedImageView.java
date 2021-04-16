package edu.jhuapl.sbmt.stateHistory.ui.imagers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;

public class PlannedImageView extends JPanel
{
	PlannedImageTableView table;

	public PlannedImageView(PlannedImageCollection collection)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedImageTableView(collection);
		table.setup();
		add(table);
	}

	/**
	 * @return the table
	 */
	public PlannedImageTableView getTable()
	{
		return table;
	}
}
