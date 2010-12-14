package edu.jhuapl.near.gui;

import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import edu.jhuapl.near.model.Model;

public abstract class ModelInfoWindow extends JFrame implements PropertyChangeListener
{
	public ModelInfoWindow()
	{
		ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
		setIconImage(erosIcon.getImage());
	}

	public abstract Model getModel();

	/**
	 * Get the collection model which directly manages the model returned by getModel.
	 * E.g. for MSI Images this would MSIImageCollection
	 *
	 * @return
	 */
	public abstract Model getCollectionModel();
}
