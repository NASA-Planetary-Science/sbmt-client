package edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.displayItems;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

abstract class AbstractStateHistoryDisplayItemPanel extends JPanel implements ActionListener
{
	/**
	 *
	 */
	protected int iconW;

	/**
	*
	*/
	protected Image questionImage = null;

	/**
	 *
	 */
	protected Icon questionIcon;

	public AbstractStateHistoryDisplayItemPanel()
	{
		iconW = 30;

		try
		{
			questionImage = ImageIO.read(getClass().getResource("/edu/jhuapl/sbmt/data/questionMark.png"));
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		questionIcon = new ImageIcon(questionImage);
	}

	/**
	 * @return
	 */
	public int getIconW()
	{
		return iconW;
	}



	/**
	 *
	 */
	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
//		// Process the event
//		Object source = aEvent.getSource();
//		if (source == scPointerColorButton)
//			doUpdateColor(source, scPointerColor);
//		else if (source == sunPointerColorButton)
//			doUpdateColor(source, sunPointerColor);
//		else
//			doUpdateColor(source, earthPointerColor);

//		// Notify our refListener
//		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

//	/**
//	 * Helper method that handles the action for srcColorB
//	 */
//	private void doUpdateColor(Object source, Color colorToSet)
//	{
//		System.out.println("StateHistoryViewControlsPanel: doUpdateColor: color to set " + Integer.toHexString(colorToSet.hashCode()));
//		// Prompt the user for a color
//		Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", colorToSet);
//		System.out.println("StateHistoryViewControlsPanel: doUpdateColor: tmp color " + Integer.toHexString(tmpColor.hashCode()));
//		if (tmpColor == null)
//			return;
//		colorToSet = new Color(tmpColor.getRGB());
//		if (source == earthPointerColorButton)
//			earthPointerColor = tmpColor;
//		else if (source == sunPointerColorButton)
//			sunPointerColor = tmpColor;
//		else
//			scPointerColor = tmpColor;
//		System.out.println("StateHistoryViewControlsPanel: doUpdateColor: color to set now " + colorToSet + " " + Integer.toHexString(colorToSet.hashCode()));
//		updateGui();
//	}

}
