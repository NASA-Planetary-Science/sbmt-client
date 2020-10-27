package edu.jhuapl.sbmt.stateHistory.ui.spectrometers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;

import lombok.Getter;

public class PlannedSpectrumView extends JPanel
{
	@Getter
	PlannedSpectrumTableView table;

	public PlannedSpectrumView(PlannedSpectrumCollection collection, SmallBodyViewConfig config)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		table = new PlannedSpectrumTableView(collection);
		table.setup();
		add(table);
	}
}
