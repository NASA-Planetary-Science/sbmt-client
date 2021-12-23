package edu.jhuapl.sbmt.stateHistory.ui.state.displayItems;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import edu.jhuapl.saavtk.util.ColorIcon;

import glum.gui.GuiUtil;

public class StateHistoryDisplayItemShowSpacecraftPanel extends AbstractStateHistoryDisplayItemPanel
{
	/**
	 *
	 */
	private JButton spacecraftColorButton;

	/**
	 *
	 */
	private Boolean scLabelShown = false;

	/**
	 *
	 */
	private Color spacecraftColor;

	/**
	*
	*/
	private JComboBox<String> distanceOptions;

	/**
	*
	*/
	private JCheckBox showSpacecraft;

	/**
	*
	*/
	private JButton scLabelButton;

	/**
	*
	*/
	private JSlider scSizeSlider;

	/**
	 *
	 */
	protected JCheckBox labelCheckBox;

	/**
	*
	*/
	protected JButton labelFontButton;

	/**
	 *
	 */
	protected Font labelFont;


	public StateHistoryDisplayItemShowSpacecraftPanel()
	{
		spacecraftColor = new Color(1.0f, 0.7f, 0.4f, 1.0f);
		labelFont = getFont();
		configureShowSpacecraftControls();
	}

	/**
	 *
	 */
	private void configureShowSpacecraftControls()
	{
		// Show spacecraft panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel scPanel1 = new JPanel();
		add(scPanel1);
		scPanel1.setLayout(new BoxLayout(scPanel1, BoxLayout.X_AXIS));

		JPanel scPanel2 = new JPanel();
		add(scPanel2);
		scPanel2.setLayout(new BoxLayout(scPanel2, BoxLayout.X_AXIS));

		JPanel scPanel3 = new JPanel();
		add(scPanel3);
		scPanel3.setLayout(new BoxLayout(scPanel3, BoxLayout.X_AXIS));

		JPanel scPanel4 = new JPanel();
		add(scPanel4);
		scPanel4.setLayout(new BoxLayout(scPanel4, BoxLayout.X_AXIS));

		showSpacecraft = new JCheckBox("Spacecraft");
		showSpacecraft.setEnabled(false);
		scPanel1.add(showSpacecraft);

//		Component horizontalStrut = Box.createHorizontalStrut(25);
//		scPanel1.add(horizontalStrut);
		scPanel1.add(Box.createHorizontalGlue());

		scLabelButton = new JButton(questionIcon);
		scLabelButton.addActionListener(e ->
		{
			updateGui();
			scPanel2.setVisible(!scLabelShown);
			scPanel3.setVisible(!scLabelShown);
			scPanel4.setVisible(!scLabelShown);
			scLabelShown = !scLabelShown;
			scLabelButton.setText("");
			if (scLabelShown)
			{
				scLabelButton.setText("Done");
				scLabelButton.setIcon(null);
			} else
				scLabelButton.setIcon(questionIcon);
		});
		scPanel1.add(scLabelButton);
//		scPanel1.add(Box.createHorizontalGlue());

		labelCheckBox = new JCheckBox("Label:");
		labelCheckBox.setEnabled(false);
		scPanel2.add(labelCheckBox);

		distanceOptions = new JComboBox<String>();
		distanceOptions.setEnabled(false);
		distanceOptions.setPreferredSize(new Dimension(200, 30));
		distanceOptions.setMaximumSize(new Dimension(200, 30));
		scPanel2.add(distanceOptions);

		labelFontButton = new JButton(questionIcon);
		labelFontButton.setEnabled(false);

		scPanel2.add(labelFontButton);

		scPanel2.add(Box.createHorizontalGlue());
		scPanel2.setVisible(false);

		JLabel scResizeText = new JLabel("Resize:");
		scPanel3.add(scResizeText);

		scSizeSlider = new JSlider(0, 100);
		scSizeSlider.setEnabled(false);
		scPanel3.add(scSizeSlider);

		scPanel3.add(Box.createHorizontalGlue());
		scPanel3.setVisible(false);

		JLabel spacecraftColorLabel = new JLabel("Color:");
		scPanel4.add(spacecraftColorLabel);

		spacecraftColorButton = GuiUtil.formButton(this, "");
		scPanel4.add(spacecraftColorButton);

		scPanel4.add(Box.createHorizontalGlue());
		scPanel4.setVisible(false);
	}

	/**
	 * Helper method that will update the UI to reflect the user selected
	 * colors.
	 */
	public void updateGui()
	{
		int iconH = 10;

		Icon spacecraftIcon = new ColorIcon(spacecraftColor, Color.BLACK, iconW, iconH);
		spacecraftColorButton.setIcon(spacecraftIcon);

	}

	/**
	 * @return
	 */
	public JComboBox<String> getDistanceOptions()
	{
		return distanceOptions;
	}

	/**
	 * @return
	 */
	public JCheckBox getShowSpacecraft()
	{
		return showSpacecraft;
	}

	/**
	 * @return
	 */
	public JSlider getScSizeSlider()
	{
		return scSizeSlider;
	}

	/**
	 * @return
	 */
	public Color getSpacecraftColor()
	{
		return spacecraftColor;
	}

	/**
	 * @param spacecraftColor
	 */
	public void setSpacecraftColor(Color spacecraftColor)
	{
		this.spacecraftColor = new Color(spacecraftColor.getRGB());
	}

	/**
	 * @return
	 */
	public JButton getSpacecraftColorButton()
	{
		return spacecraftColorButton;
	}

	/**
	 * @return
	 */
	public JButton getLabelFontButton()
	{
		return labelFontButton;
	}

	/**
	 * @return
	 */
	public Font getLabelFont()
	{
		return labelFont;
	}

	/**
	 * @param labelFont
	 */
	public void setLabelFont(Font labelFont)
	{
		this.labelFont = labelFont;
	}

	/**
	 * @return
	 */
	public JCheckBox getLabelCheckBox()
	{
		return labelCheckBox;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		labelCheckBox.setEnabled(enabled);
		showSpacecraft.setEnabled(enabled);
		scLabelButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}

}
