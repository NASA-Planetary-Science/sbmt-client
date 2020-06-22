package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems;

import java.awt.Color;

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

public class StateHistoryDisplayItemShowSunPanel extends AbstractStateHistoryDisplayItemPanel
{

	/**
	*
	*/
	private JSlider sunSlider;

	/**
	*
	*/
	private JCheckBox showSunPointer;

	/**
	*
	*/
	private JButton resizeSunPointerButton;

	/**
	 *
	 */
	private Boolean sunResizeShown = false;

	/**
	 *
	 */
	private JButton sunPointerColorButton;

	/**
	 *
	 */
	private JLabel sunPointerColorLabel;

	/**
	 *
	 */
	private Color sunPointerColor = Color.YELLOW;

	/**
	*
	*/
	private JLabel sunText;

	public StateHistoryDisplayItemShowSunPanel()
	{
		configureShowSunControls();
	}

	/**
	 *
	 */
	private void configureShowSunControls()
	{
		// Show Sun Pointer Panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel sunPanel1 = new JPanel();
		add(sunPanel1);
		sunPanel1.setLayout(new BoxLayout(sunPanel1, BoxLayout.X_AXIS));

		JPanel sunPanel2 = new JPanel();
		add(sunPanel2);
		sunPanel2.setLayout(new BoxLayout(sunPanel2, BoxLayout.X_AXIS));

		showSunPointer = new JCheckBox("Sun Pointer");
		showSunPointer.setEnabled(false);
		sunPanel1.add(showSunPointer);

//		Component horizontalStrut_2 = Box.createHorizontalStrut(30);
//		sunPanel1.add(horizontalStrut_2);
		sunPanel1.add(Box.createHorizontalGlue());

		resizeSunPointerButton = new JButton(questionIcon);
		resizeSunPointerButton.addActionListener(e ->
		{
			updateGui();
			sunPanel2.setVisible(!sunResizeShown);
			sunResizeShown = !sunResizeShown;
			resizeSunPointerButton.setText("");
			if (sunResizeShown)
			{
				resizeSunPointerButton.setText("Done");
				resizeSunPointerButton.setIcon(null);
			} else
				resizeSunPointerButton.setIcon(questionIcon);
		});
		sunPanel1.add(resizeSunPointerButton);
//		sunPanel1.add(Box.createHorizontalGlue());

		sunText = new JLabel("Resize:");
		sunText.setEnabled(false);
		sunPanel2.add(sunText);

		sunSlider = new JSlider();
		sunSlider.setEnabled(false);
		sunPanel2.add(sunSlider);
		sunPanel2.setVisible(false);

		sunPointerColorLabel = new JLabel("Color:");
		sunPanel2.add(sunPointerColorLabel);

		sunPointerColorButton = GuiUtil.formButton(this, "");
		sunPanel2.add(sunPointerColorButton);
		// Icon sunIcon = new ColorIcon(sunPointerColor, Color.BLACK, iconW,
		// iconH);
		// sunPointerColorButton.setIcon(sunIcon);
	}

	/**
	 * Helper method that will update the UI to reflect the user selected
	 * colors.
	 */
	public void updateGui()
	{
		Icon sunIcon = new ColorIcon(sunPointerColor, Color.BLACK, iconW, 10);
		sunPointerColorButton.setIcon(sunIcon);
	}

	/**
	 * @return
	 */
	public JSlider getSunSlider()
	{
		return sunSlider;
	}

	/**
	 * @return
	 */
	public JLabel getSunText()
	{
		return sunText;
	}

	/**
	 * @return
	 */
	public Color getSunPointerColor()
	{
		return sunPointerColor;
	}

	/**
	 * @param sunPointerColor
	 */
	public void setSunPointerColor(Color sunPointerColor)
	{
		this.sunPointerColor = new Color(sunPointerColor.getRGB());
	}

	/**
	 * @return
	 */
	public JButton getSunPointerColorButton()
	{
		return sunPointerColorButton;
	}


	/**
	 * @return
	 */
	public JCheckBox getShowSunPointer()
	{
		return showSunPointer;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showSunPointer.setEnabled(enabled);
    	sunText.setEnabled(enabled);
    	resizeSunPointerButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}

}
