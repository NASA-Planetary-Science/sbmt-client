package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * @author steelrj1
 *
 */
public class StateHistoryDisplayItemShowLightingPanel extends JPanel
{

	/**
	 *
	 */
	private JCheckBox showLighting;

	/**
	 *
	 */
	public StateHistoryDisplayItemShowLightingPanel()
	{
		configureShowLightingControls();
	}

	/**
	 *
	 */
	private void configureShowLightingControls()
	{
		// Show lighting panel
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		showLighting = new JCheckBox("Lighting");
//		showLighting.setEnabled(false);
		add(showLighting);
		add(Box.createHorizontalGlue());


	}

	/**
	 * @return
	 */
	public JCheckBox getShowLighting()
	{
		return showLighting;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showLighting.setEnabled(enabled);
		super.setEnabled(enabled);
	}

}
