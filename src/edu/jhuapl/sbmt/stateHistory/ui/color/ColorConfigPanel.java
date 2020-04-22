package edu.jhuapl.sbmt.stateHistory.ui.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

import glum.gui.GuiExeUtil;
import glum.gui.component.GComboBox;
import glum.gui.panel.CardPanel;

/**
 * Panel used to allow a user to configure the LidarColorProvider used for
 * coloring lidar data.
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public class ColorConfigPanel<G1> extends JPanel implements ActionListener
{
	// Ref vars
	private final ActionListener refListener;

	// GUI vars
	private StateHistoryColorBarPanel colorMapPanel;
	private CardPanel<StateHistoryColorConfigPanel> colorPanel;
	private GComboBox<ColorMode> colorModeBox;

	/**
	 * Standard Constructor
	 */
	public ColorConfigPanel(ActionListener aListener, StateHistoryCollection aManager, Renderer aRenderer)
	{
		refListener = aListener;

//		setLayout(new MigLayout("", "0[][]0", "0[][]0"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel tmpL = new JLabel("Colorize:");
		colorModeBox = new GComboBox<>(this, ColorMode.values());
		JPanel comboPanel = new JPanel();
		comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
		comboPanel.add(tmpL);
		comboPanel.add(colorModeBox, "growx,wrap 2");
		add(comboPanel);

		colorMapPanel = new StateHistoryColorBarPanel(this, aManager, aRenderer);
		colorPanel = new CardPanel<>();
		colorPanel.addCard(ColorMode.AutoHue, new AutoColorPanel(this));
		colorPanel.addCard(ColorMode.ColorMap, colorMapPanel);
//		colorPanel.addCard(ColorMode.Randomize, new RandomizePanel(this, 0));
		colorPanel.addCard(ColorMode.Simple, new SimplePanel(this, Color.CYAN, Color.BLUE));

//		add(colorPanel, "growx,growy,span");
		add(colorPanel);

		// Custom initialization code
		Runnable tmpRunnable = () -> {
			colorPanel.getActiveCard().activate(true);
		};
		GuiExeUtil.executeOnceWhenShowing(this, tmpRunnable);
	}

	/**
	 * Returns the GroupColorProvider that should be used to color data points
	 * associated with the lidar source (spacecraft).
	 */
	public GroupColorProvider getSourceGroupColorProvider()
	{
		return colorPanel.getActiveCard().getSourceGroupColorProvider();
	}

	/**
	 * Sets the ColorProviderMode which will be active
	 */
	public void setActiveMode(ColorMode aMode)
	{
		colorPanel.getActiveCard().activate(false);

		colorModeBox.setChosenItem(aMode);
		colorPanel.switchToCard(aMode);
		colorPanel.getActiveCard().activate(true);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == colorModeBox)
			doUpdateColorPanel();

		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

	/**
	 * Helper method to properly update the colorPanel.
	 */
	private void doUpdateColorPanel()
	{
		colorPanel.getActiveCard().activate(false);

		ColorMode tmpCM = colorModeBox.getChosenItem();
		colorPanel.switchToCard(tmpCM);
		colorPanel.getActiveCard().activate(true);
	}

}
