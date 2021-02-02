/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * @author steelrj1
 *
 */
public class StateHistoryDisplayInstrumentFrustumPanel extends JPanel
{
	/**
	 *
	 */
	private JCheckBox showFrustum;

	/**
	 *
	 */
	public StateHistoryDisplayInstrumentFrustumPanel()
	{
		configureShowFrustumControls();
	}

	/**
	 *
	 */
	private void configureShowFrustumControls()
	{
		// Show lighting panel
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		showFrustum = new JCheckBox("Instrument Frustrum");
		showFrustum.setEnabled(false);
		add(showFrustum);
		add(Box.createHorizontalGlue());
	}

	/**
	 * @return
	 */
	public JCheckBox getShowFrustum()
	{
		return showFrustum;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showFrustum.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}