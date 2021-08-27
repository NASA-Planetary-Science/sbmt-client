package edu.jhuapl.sbmt.stateHistory.ui.state.displayItems;

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

public class StateHistoryDisplayItemShowEarthPanel extends AbstractStateHistoryDisplayItemPanel
{
	/**
	*
	*/
	private JSlider earthSlider;

	/**
	*
	*/
	private JCheckBox showEarthPointer;

	/**
	*
	*/
	private JLabel earthText;

	/**
	 *
	 */
	private Boolean earthResizeShown = false;

	/**
	 *
	 */
	private JButton earthPointerColorButton;

	/**
	 *
	 */
	private Color earthPointerColor;

	/**
	 *
	 */
	private JLabel earthPointerColorLabel;

	/**
	*
	*/
	private JButton resizeEarthPointerButton;

	public StateHistoryDisplayItemShowEarthPanel()
	{
		earthPointerColor = Color.BLUE;
		configureShowEarthControls();
	}

	/**
	 *
	 */
	private void configureShowEarthControls()
	{
		// Show Earth Pointer Panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel earthPanel1 = new JPanel();
		add(earthPanel1);
		earthPanel1.setLayout(new BoxLayout(earthPanel1, BoxLayout.X_AXIS));

		JPanel earthPanel2 = new JPanel();
		add(earthPanel2);
		earthPanel2.setLayout(new BoxLayout(earthPanel2, BoxLayout.X_AXIS));

		showEarthPointer = new JCheckBox("Earth Pointer");
		showEarthPointer.setEnabled(false);
		earthPanel1.add(showEarthPointer);

//		Component horizontalStrut_5 = Box.createHorizontalStrut(25);
//		earthPanel1.add(horizontalStrut_5);
		earthPanel1.add(Box.createHorizontalGlue());

		resizeEarthPointerButton = new JButton(questionIcon);
		resizeEarthPointerButton.addActionListener(e ->
		{
			updateGui();
			earthPanel2.setVisible(!earthResizeShown);
			earthResizeShown = !earthResizeShown;
			resizeEarthPointerButton.setText("");
			if (earthResizeShown)
			{
				resizeEarthPointerButton.setText("Done");
				resizeEarthPointerButton.setIcon(null);
			} else
				resizeEarthPointerButton.setIcon(questionIcon);
		});
		earthPanel1.add(resizeEarthPointerButton);
//		earthPanel1.add(Box.createHorizontalGlue());

		earthText = new JLabel("Resize:");
		earthText.setEnabled(false);
		earthPanel2.add(earthText);

		earthSlider = new JSlider();
		earthSlider.setEnabled(false);
		earthPanel2.add(earthSlider);
		earthPanel2.setVisible(false);

		earthPointerColorLabel = new JLabel("Color:");
		earthPanel2.add(earthPointerColorLabel);

		earthPointerColorButton = GuiUtil.formButton(this, "");
		earthPanel2.add(earthPointerColorButton);
	}

	/**
	 * Helper method that will update the UI to reflect the user selected
	 * colors.
	 */
	public void updateGui()
	{

		Icon earthIcon = new ColorIcon(earthPointerColor, Color.BLACK, iconW, 10);
		earthPointerColorButton.setIcon(earthIcon);
	}

	/**
	 * @return
	 */
	public JSlider getEarthSlider()
	{
		return earthSlider;
	}

	/**
	 * @return
	 */
	public JCheckBox getShowEarthPointer()
	{
		return showEarthPointer;
	}

	/**
	 * @return
	 */
	public JLabel getEarthText()
	{
		return earthText;
	}

	/**
	 * @return
	 */
	public Color getEarthPointerColor()
	{
		return earthPointerColor;
	}

	/**
	 * @param earthPointerColor
	 */
	public void setEarthPointerColor(Color earthPointerColor)
	{
		this.earthPointerColor = new Color(earthPointerColor.getRGB());
	}

	/**
	 * @return
	 */
	public JButton getEarthPointerColorButton()
	{
		return earthPointerColorButton;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		showEarthPointer.setEnabled(enabled);
    	earthText.setEnabled(enabled);
    	resizeEarthPointerButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}

}
