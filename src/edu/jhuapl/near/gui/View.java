package edu.jhuapl.near.gui;

import java.awt.LayoutManager;

import javax.swing.JPanel;


/**
 * A view is a container which contains a control panel and renderer
 * as well as a collection of managers. A view is unique to a specific
 * body. 
 * @author kahneg1
 *
 */
public abstract class View extends JPanel
{
	/**
	 * By default a view should be created empty. Only when the user
	 * requests to show a particular View, should the View's contents
	 * be created in order to reduce memory and startup time. Therefore,
	 * this function should be called prior to first time the View is
	 * shown in order to cause it 
	 */
	
	public View(LayoutManager mgr)
	{
		super(mgr);
	}
	
	public void initialize()
	{
	}
	
	public abstract String getName();
	
    public abstract Renderer getRenderer();
}
