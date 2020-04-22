package edu.jhuapl.sbmt.stateHistory.ui.color;

import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * {@link StateHistoryColorConfigPanel} that provides the AutoHue color panel. There
 * are no configuration options.
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public class AutoColorPanel extends JPanel implements StateHistoryColorConfigPanel
{
	/**
	 * Standard Constructor
	 */
	public AutoColorPanel(ActionListener aListener)
	{
		// Setup the GUI
		setLayout(new MigLayout("", "0[]0", ""));

		JLabel tmpL = new JLabel("There are no configuration options.", JLabel.CENTER);
		add(tmpL, "growx,pushx");
	}

	@Override
	public void activate(boolean aIsActive)
	{
		; // Nothing to do
	}

	@Override
	public GroupColorProvider getSourceGroupColorProvider()
	{
		return ColorWheelGroupColorProvider.Instance;
	}
}
