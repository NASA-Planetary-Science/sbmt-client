package edu.jhuapl.near;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class StatusBar extends JPanel 
{
	private JLabel label;
	
	public StatusBar()
	{
		setLayout(new BorderLayout());
		label = new JLabel(" ");
    	add(label, BorderLayout.CENTER);

    	setBorder(new BevelBorder(BevelBorder.LOWERED));
	}

	public void setText(String text)
	{
		if (text.length() == 0)
			text = " ";
		label.setText(text);
	}
}
