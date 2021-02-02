package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
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

	public void setStateHistoryCollection(StateHistoryRendererManager rendererManager)
	{
		DisplayOptionsTableView tableView = new DisplayOptionsTableView(rendererManager);
		tableView.setup();
		add(tableView);
//		rendererManager.getDisplayItemsProcessor().addListener(new ItemEventListener()
//		{
//
//			@Override
//			public void handleItemEvent(Object aSource, ItemEventType aEventType)
//			{
//				System.out.println(
//						"StateHistoryDisplayItemsPanel.setStateHistoryCollection(...).new ItemEventListener() {...}: handleItemEvent: selecting display item");
//			}
//		});
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
	}
}
