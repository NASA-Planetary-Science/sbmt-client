package edu.jhuapl.sbmt.stateHistory.ui.spectrometers.schedule;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumScheduleCollection;

public class PlannedSpectrumScheduleView extends JPanel
{
	PlannedSpectrumScheduleTableView table;

	public PlannedSpectrumScheduleView(PlannedSpectrumScheduleCollection collection)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedSpectrumScheduleTableView(collection);
		table.setup();
		add(table);
	}

	/**
	 * @return the table
	 */
	public PlannedSpectrumScheduleTableView getTable()
	{
		return table;
	}
}
