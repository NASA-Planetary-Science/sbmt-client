package edu.jhuapl.sbmt.stateHistory.ui.state.color;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.gui.EditGroupColorPanel;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.provider.SimpleColorProvider;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.gui.GuiExeUtil;
import glum.gui.component.GComboBox;
import glum.gui.panel.CardPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Interface that defines the methods to allow configuration of ColorProviders
 * (used to render state history data).
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public class StateHistoryColorConfigPanel extends JPanel implements ActionListener
{
	// Ref vars
	private final ActionListener refListener;

	// GUI vars
	private StateHistoryColorBarPanel colorMapPanel;
	private CardPanel<EditGroupColorPanel> colorPanel;
	private GComboBox<ColorMode> colorModeBox;

	/**
	 * Standard Constructor
	 */
	public StateHistoryColorConfigPanel(ActionListener aListener, StateHistoryRendererManager rendererManager)
	{
		refListener = aListener;

		setLayout(new MigLayout("", "0[][]0", "0[][]0"));

		JLabel tmpL = new JLabel("Colorize:");
		colorModeBox = new GComboBox<>(this, ColorMode.values());
		add(tmpL);
		add(colorModeBox, "growx,wrap 2");

		ColorBarPainter tmpCBP = new ColorBarPainter(rendererManager.getRenderer());
		colorMapPanel = new StateHistoryColorBarPanel(this, rendererManager, tmpCBP);
		colorPanel = new CardPanel<>();
		colorPanel.addCard(ColorMode.ColorMap, colorMapPanel);
		colorPanel.addCard(ColorMode.Simple, new SimplePanel(this, new Color(0.0f, 1.0f, 1.0f)));

		if (rendererManager.getRuns().getCurrentRun() != null)
		{
			ColorProvider colorProvider = rendererManager.getColorProviderForStateHistory(rendererManager.getRuns().getCurrentRun());
			if (colorProvider instanceof SimpleColorProvider)
				setActiveMode(ColorMode.Simple);
			else
			{
				setActiveMode(ColorMode.ColorMap);
				colorMapPanel.setFeatureType(colorProvider.getFeatureType());
			}
		}

		add(colorPanel, "growx,growy,span");
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
		EditGroupColorPanel tmpPanel = colorPanel.getActiveCard();
		if (tmpPanel instanceof SimplePanel)
			return ((SimplePanel) tmpPanel).getGroupColorProvider();
		return tmpPanel.getGroupColorProvider();
	}

	public FeatureType getFeatureType()
	{
		EditGroupColorPanel tmpPanel = colorPanel.getActiveCard();
		if (tmpPanel instanceof SimplePanel)
			return null;
		return ((StateHistoryColorBarPanel)tmpPanel).getFeatureType();
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