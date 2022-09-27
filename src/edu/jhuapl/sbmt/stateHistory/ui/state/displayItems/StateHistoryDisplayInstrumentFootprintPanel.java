/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.ui.state.displayItems;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * @author steelrj1
 *
 */
public class StateHistoryDisplayInstrumentFootprintPanel extends JPanel
{
	/**
	 *
	 */
	private JCheckBox showFootprint;

	/**
	 *
	 */
	public StateHistoryDisplayInstrumentFootprintPanel()
	{
		configureShowFootprintControls();
	}

	/**
	 *
	 */
	private void configureShowFootprintControls()
	{
		// Show lighting panel
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		showFootprint = new JCheckBox("Instrument Footprint");
		showFootprint.setEnabled(false);
		add(showFootprint);
		add(Box.createHorizontalGlue());
	}

	/**
	 * @return
	 */
	public JCheckBox getShowFootprint()
	{
		return showFootprint;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showFootprint.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
