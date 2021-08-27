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
public class StateHistoryDisplayInstrumentFootprintBorderPanel extends JPanel
{
	/**
	 *
	 */
	private JCheckBox showFootprintBorder;

	/**
	 *
	 */
	public StateHistoryDisplayInstrumentFootprintBorderPanel()
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
		showFootprintBorder = new JCheckBox("Instrument Footprint Border");
		showFootprintBorder.setEnabled(false);
		add(showFootprintBorder);
		add(Box.createHorizontalGlue());
	}

	/**
	 * @return
	 */
	public JCheckBox getShowFootprintBorder()
	{
		return showFootprintBorder;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showFootprintBorder.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
