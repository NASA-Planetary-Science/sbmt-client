package edu.jhuapl.near.gui.eros;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import edu.jhuapl.near.model.Model;

public abstract class ModelInfoWindow extends JFrame
{
	public ModelInfoWindow()
	{
		ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
		setIconImage(erosIcon.getImage());
	}
	
	public abstract Model getModel();
}
