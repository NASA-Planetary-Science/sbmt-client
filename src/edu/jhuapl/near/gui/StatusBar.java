package edu.jhuapl.near.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class StatusBar extends JPanel 
{
	private JLabel leftLabel;
	private JLabel rightLabel;
	
	public StatusBar()
	{
		setLayout(new BorderLayout());
		leftLabel = new JLabel(" ", SwingConstants.LEFT);
    	add(leftLabel, BorderLayout.CENTER);
		rightLabel = new JLabel(" ", SwingConstants.RIGHT);
    	add(rightLabel, BorderLayout.EAST);

    	setBorder(new BevelBorder(BevelBorder.LOWERED));
	}

	public void setLeftText(String text)
	{
		if (text.length() == 0)
			text = " ";
		leftLabel.setText(text);
	}

	public void setRightText(String text)
	{
		if (text.length() == 0)
			text = " ";
		rightLabel.setText(text);
	}
}
