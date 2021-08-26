package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.ui.state.displayItems;
//
//import javax.swing.BoxLayout;
//import javax.swing.JPanel;
//import javax.swing.border.TitledBorder;
//
//import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
//import edu.jhuapl.sbmt.stateHistory.ui.state.displayItems.table.DisplayOptionsTableView;
//
//public class StateHistoryDisplayItemsPanel extends JPanel
//{
//	public StateHistoryDisplayItemsPanel()
//	{
//		configureDisplayItemsPanel();
//	}
//
//	/**
//	 *
//	 */
//	private void configureDisplayItemsPanel()
//	{
//		setBorder(new TitledBorder(null, "Display Items", TitledBorder.LEADING, TitledBorder.TOP, null, null));
//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//	}
//
//	public void setStateHistoryCollection(StateHistoryRendererManager rendererManager)
//	{
//		DisplayOptionsTableView tableView = new DisplayOptionsTableView(rendererManager);
//		tableView.setup();
//		add(tableView);
//	}
//
//	@Override
//	public void setEnabled(boolean enabled)
//	{
//		super.setEnabled(enabled);
//	}
//}
