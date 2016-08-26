package edu.jhuapl.near.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenu;

public class ViewMenu extends JMenu implements PropertyChangeListener
{
    public ViewManager getRootPanel()
    {
        return rootPanel;
    }

    public void setRootPanel(ViewManager rootPanel)
    {
        this.rootPanel = rootPanel;
    }

    public RecentlyViewed getViewed()
    {
        return viewed;
    }

    public void setViewed(RecentlyViewed viewed)
    {
        this.viewed = viewed;
    }

    private ViewManager rootPanel;
    private RecentlyViewed viewed;

    public ViewMenu(ViewManager rootPanel, RecentlyViewed viewed)
    {
        super("View");

        this.rootPanel = rootPanel;
        this.viewed=viewed;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
    }
}
