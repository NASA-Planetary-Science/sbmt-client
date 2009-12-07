package edu.jhuapl.near.gui;

import java.awt.Dimension;

import javax.swing.*;

public class SearchPanelUtil 
{
	public static JPanel createFromToPanel(
			JFormattedTextField fromField,
			JFormattedTextField toField,
			double fromValue,
			double toValue,
			String labelTextLeft,
			String labelTextMiddle,
			String labelTextRight
	)
	{
		JPanel distancePanel = new JPanel();
		distancePanel.setLayout(new BoxLayout(distancePanel,
				BoxLayout.LINE_AXIS));
		JLabel fromDistanceLabel = new JLabel(labelTextLeft + " ");
		fromField.setValue(fromValue);
		fromField.setMaximumSize(new Dimension(50, 23));
		JLabel toDistanceLabel = new JLabel(" " + labelTextMiddle + " ");
		toField.setValue(toValue);
		toField.setMaximumSize(new Dimension(50, 23));
		JLabel endDistanceLabel = new JLabel(" " + labelTextRight);

		distancePanel.add(fromDistanceLabel);
		distancePanel.add(fromField);
		distancePanel.add(toDistanceLabel);
		distancePanel.add(toField);
		distancePanel.add(endDistanceLabel);

		return distancePanel;
	}
}
