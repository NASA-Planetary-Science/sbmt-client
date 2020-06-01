package edu.jhuapl.sbmt.stateHistory.ui.imagers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;

public class PlannedImageView extends JPanel
{
	PlannedImageTableView table;

	public PlannedImageView(PlannedImageCollection collection, SmallBodyViewConfig config)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedImageTableView(collection);
		table.setup();
		add(table);
	}

	public PlannedImageTableView getTable()
	{
		return table;
	}
}
