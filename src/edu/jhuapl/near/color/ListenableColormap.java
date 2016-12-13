package edu.jhuapl.near.color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class ListenableColormap implements Colormap
{
	List<PropertyChangeListener> listeners=Lists.newArrayList();
		
	@Override
	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		listeners.add(l);		
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		listeners.remove(l);
	}
	
	protected void firePropertyChangeEvent()
	{
		for (PropertyChangeListener l : listeners)
			l.propertyChange(new PropertyChangeEvent(this, "colormap", null, null));
	}
	
}
