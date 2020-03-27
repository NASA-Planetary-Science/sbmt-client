package edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls.displayItems;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import edu.jhuapl.saavtk.util.ColorIcon;

import glum.gui.GuiUtil;

public class StateHistoryDisplayItemShowSpacecraftPointerPanel extends AbstractStateHistoryDisplayItemPanel
{
	/**
	*
	*/
	private JCheckBox showSpacecraftMarker;

	/**
	*
	*/
	private JSlider spacecraftSlider;

	/**
	 *
	 */
	private JButton scPointerColorButton;

	/**
	 *
	 */
	private Color scPointerColor = Color.GREEN;


	/**
	 *
	 */
	private JLabel scPointerColorLabel;

	/**
	*
	*/
	private JButton resizeSpacecraftPointerButton;

	/**
	*
	*/
	private JLabel spacecraftText;

	/**
	 *
	 */
	private Boolean scResizeShown = false;

	public StateHistoryDisplayItemShowSpacecraftPointerPanel()
	{
		configureShowSCPointerControls();
	}

	/**
	 *
	 */
	private void configureShowSCPointerControls()
	{
		// Show S/C pointer panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel scPanel1 = new JPanel();
		add(scPanel1);
		scPanel1.setLayout(new BoxLayout(scPanel1, BoxLayout.X_AXIS));

		JPanel scPanel2 = new JPanel();
		add(scPanel2);
		scPanel2.setLayout(new BoxLayout(scPanel2, BoxLayout.X_AXIS));

		showSpacecraftMarker = new JCheckBox("Show S/C Pointer");
		showSpacecraftMarker.setEnabled(false);
		scPanel1.add(showSpacecraftMarker);

		Component horizontalStrut_3 = Box.createHorizontalStrut(30);
		scPanel1.add(horizontalStrut_3);

		resizeSpacecraftPointerButton = new JButton(questionIcon);

		resizeSpacecraftPointerButton.addActionListener(e ->
		{
			updateGui();
			scPanel2.setVisible(!scResizeShown);
			scResizeShown = !scResizeShown;
			resizeSpacecraftPointerButton.setText("");
			if (scResizeShown)
			{
				resizeSpacecraftPointerButton.setText("Done");
				resizeSpacecraftPointerButton.setIcon(null);
			} else
				resizeSpacecraftPointerButton.setIcon(questionIcon);

		});
		scPanel1.add(resizeSpacecraftPointerButton);
		scPanel1.add(Box.createHorizontalGlue());

		spacecraftText = new JLabel("Resize:");
		spacecraftText.setEnabled(false);
		scPanel2.add(spacecraftText);

		spacecraftSlider = new JSlider();
		spacecraftSlider.setEnabled(false);
		scPanel2.add(spacecraftSlider);
		scPanel2.setVisible(false);
		scPanel2.add(Box.createHorizontalGlue());

		scPointerColorLabel = new JLabel("Color:");
		scPanel2.add(scPointerColorLabel);

		scPointerColorButton = GuiUtil.formButton(this, "");
		scPointerColorButton.repaint();
		scPanel2.add(scPointerColorButton);
		scPanel2.add(Box.createHorizontalGlue());
	}

	/**
	 * Helper method that will update the UI to reflect the user selected
	 * colors.
	 */
	public void updateGui()
	{
		if (scPointerColorLabel == null)
			return;
		int iconH = (int) (scPointerColorLabel.getHeight() * 0.80);
		iconH = 10;

		Icon scIcon = new ColorIcon(scPointerColor, Color.BLACK, iconW, iconH);
		scPointerColorButton.setIcon(scIcon);
	}

	/**
	 * @return
	 */
	public Color getScPointerColor()
	{
		return scPointerColor;
	}

	/**
	 * @param scPointerColor
	 */
	public void setScPointerColor(Color scPointerColor)
	{
		this.scPointerColor = new Color(scPointerColor.getRGB());
	}

	/**
	 * @return
	 */
	public JButton getScPointerColorButton()
	{
		return scPointerColorButton;
	}

	/**
	 * @return
	 */
	public JSlider getSpacecraftSlider()
	{
		return spacecraftSlider;
	}

	/**
	 * @return
	 */
	public JCheckBox getShowSpacecraftMarker()
	{
		return showSpacecraftMarker;
	}

	/**
	 * @return
	 */
	public JLabel getSpacecraftText()
	{
		return spacecraftText;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showSpacecraftMarker.setEnabled(enabled);
		spacecraftText.setEnabled(enabled);
		resizeSpacecraftPointerButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}

}
