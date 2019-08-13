package edu.jhuapl.sbmt.gui.lidar.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.model.lidar.LidarManager;

import glum.gui.GuiExeUtil;
import glum.gui.component.GComboBox;
import glum.gui.panel.CardPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to allow a user to configure the LidarColorProvider used for
 * coloring lidar data.
 *
 * @author lopeznr1
 */
public class ColorConfigPanel<G1> extends JPanel implements ActionListener
{
	// Ref vars
	private final ActionListener refListener;

	// GUI vars
	private LidarColorBarPanel<G1> colorMapPanel;
	private CardPanel<LidarColorConfigPanel> colorPanel;
	private GComboBox<ColorMode> colorModeBox;

	/**
	 * Standard Constructor
	 */
	public ColorConfigPanel(ActionListener aListener, LidarManager<G1> aManager)
	{
		refListener = aListener;

		setLayout(new MigLayout("", "0[][]0", "0[][]0"));

		JLabel tmpL = new JLabel("Colorize:");
		colorModeBox = new GComboBox<>(this, ColorMode.values());
		add(tmpL);
		add(colorModeBox, "growx,wrap 2");

		colorMapPanel = new LidarColorBarPanel<>(this, aManager);
		colorPanel = new CardPanel<>();
		colorPanel.addCard(ColorMode.AutoHue, new AutoColorPanel(this));
		colorPanel.addCard(ColorMode.ColorMap, colorMapPanel);
		colorPanel.addCard(ColorMode.Randomize, new RandomizePanel(this, 0));
		colorPanel.addCard(ColorMode.Simple, new SimplePanel(this, Color.GREEN, Color.BLUE));

		add(colorPanel, "growx,growy,span");

		// Custom initialization code
		Runnable tmpRunnable = () -> {
			colorPanel.getActiveCard().activate();
		};
		GuiExeUtil.executeOnceWhenShowing(this, tmpRunnable);
	}

	/**
	 * Returns the GroupColorProvider that should be used to color data points
	 * associated with the lidar source (spacecraft).
	 */
	public GroupColorProvider getSourceGroupColorProvider()
	{
		return (GroupColorProvider) colorPanel.getActiveCard().getSourceGroupColorProvider();
	}

	/**
	 * Returns the GroupColorProvider that should be used to color data points
	 * associated with the lidar target (ground).
	 */
	public GroupColorProvider getTargetGroupColorProvider()
	{
		return (GroupColorProvider) colorPanel.getActiveCard().getTargetGroupColorProvider();
	}

	/**
	 * Sets the ColorProviderMode which will be active
	 */
	public void setActiveMode(ColorMode aMode)
	{
		colorModeBox.setChosenItem(aMode);
		colorPanel.switchToCard(aMode);
		colorPanel.getActiveCard().activate();
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
	 * Helper method tho properly update the colorPanel.
	 */
	private void doUpdateColorPanel()
	{
		ColorMode tmpCM = colorModeBox.getChosenItem();

		colorPanel.switchToCard(tmpCM);
		colorPanel.getActiveCard().activate();
	}

}
