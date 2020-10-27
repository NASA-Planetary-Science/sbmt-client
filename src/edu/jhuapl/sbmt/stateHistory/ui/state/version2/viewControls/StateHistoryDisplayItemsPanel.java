package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.table.DisplayOptionsTableView;

public class StateHistoryDisplayItemsPanel extends JPanel
{
	public StateHistoryDisplayItemsPanel()
	{
		configureDisplayItemsPanel();
	}

	/**
	 *
	 */
	private void configureDisplayItemsPanel()
	{
		setBorder(new TitledBorder(null, "Display Items", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void setStateHistoryCollection(StateHistoryCollection runs)
	{
		DisplayOptionsTableView tableView = new DisplayOptionsTableView(runs);
		tableView.setup();
		add(tableView);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
	}
}
