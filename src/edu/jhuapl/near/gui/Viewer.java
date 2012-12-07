package edu.jhuapl.near.gui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickManager;


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

    public abstract Renderer getRenderer();
    public abstract ModelManager getModelManager();
    public abstract PickManager getPickManager();

    /**
     * Return a unique name for this viewer. No other viewer may have this
     * name. Note that only applies within built-in viewers or custom viewers
     * but a custom viewer can share the name of a built-in one or vice versa.
     * By default simply return the submenu concatenated with the display
     * name if the submenu is not null or just the display name if the submenu
     * is null.
     * @return
     */
    public String getUniqueName()
    {
        return SmallBodyModel.getUniqueName(getDisplayName(), getSubmenu());
    }

    /**
     * Return the display name for this viewer (the name to be shown in the menu).
     * This name need not be unique among all viewers.
     * @return
     */
    public abstract String getDisplayName();

    /**
     * Return the submenu in which this viewer should be placed.
     * If null (the default), do not place in a submenu but in the
     * top menu.
     *
     * @return
     */
    public String getSubmenu()
    {
        return null;
    }
}
