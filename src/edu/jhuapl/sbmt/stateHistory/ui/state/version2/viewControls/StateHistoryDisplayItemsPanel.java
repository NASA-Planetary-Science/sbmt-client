package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.StateHistoryDisplayItemShowEarthPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.StateHistoryDisplayItemShowLightingPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.StateHistoryDisplayItemShowSpacecraftPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.StateHistoryDisplayItemShowSpacecraftPointerPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems.StateHistoryDisplayItemShowSunPanel;

public class StateHistoryDisplayItemsPanel extends JPanel
{

	StateHistoryDisplayItemShowSpacecraftPanel showSpacecraftPanel;
	StateHistoryDisplayItemShowEarthPanel showEarthPanel;
	StateHistoryDisplayItemShowSunPanel showSunPanel;
	StateHistoryDisplayItemShowLightingPanel showLightingPanel;
	StateHistoryDisplayItemShowSpacecraftPointerPanel showSpacecraftPointerPanel;

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

		add(showSpacecraftPanel = new StateHistoryDisplayItemShowSpacecraftPanel());
		add(Box.createVerticalGlue());
		add(showEarthPanel = new StateHistoryDisplayItemShowEarthPanel());
		add(Box.createVerticalGlue());
		add(showSunPanel = new StateHistoryDisplayItemShowSunPanel());
		add(Box.createVerticalGlue());
		add(showSpacecraftPointerPanel = new StateHistoryDisplayItemShowSpacecraftPointerPanel());
		add(Box.createVerticalGlue());
		add(showLightingPanel = new StateHistoryDisplayItemShowLightingPanel());

	}

	public StateHistoryDisplayItemShowSpacecraftPanel getShowSpacecraftPanel()
	{
		return showSpacecraftPanel;
	}

	public StateHistoryDisplayItemShowEarthPanel getShowEarthPanel()
	{
		return showEarthPanel;
	}

	public StateHistoryDisplayItemShowSunPanel getShowSunPanel()
	{
		return showSunPanel;
	}

	public StateHistoryDisplayItemShowLightingPanel getShowLightingPanel()
	{
		return showLightingPanel;
	}

	public StateHistoryDisplayItemShowSpacecraftPointerPanel getShowSpacecraftPointerPanel()
	{
		return showSpacecraftPointerPanel;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		//child panels here
		showSpacecraftPanel.setEnabled(enabled);
		showEarthPanel.setEnabled(enabled);
		showSunPanel.setEnabled(enabled);
		showLightingPanel.setEnabled(enabled);
		showSpacecraftPointerPanel.setEnabled(enabled);
		super.setEnabled(enabled);
	}

}
