package edu.jhuapl.near.gui;

import java.awt.LayoutManager;

import javax.swing.JPanel;


/**
 * A viewer is a container which contains a control panel and renderer
 * as well as a collection of managers. A viewer is unique to a specific
 * body.
 * @author kahneg1
 *
 */
public abstract class Viewer extends JPanel
{
    /**
     * By default a viewer should be created empty. Only when the user
     * requests to show a particular Viewer, should the Viewer's contents
     * be created in order to reduce memory and startup time. Therefore,
     * this function should be called prior to first time the Viewer is
     * shown in order to cause it
     */

    public Viewer(LayoutManager mgr)
    {
        super(mgr);
    }

    public void initialize()
    {
    }

    public abstract String getName();

    public abstract Renderer getRenderer();
}
