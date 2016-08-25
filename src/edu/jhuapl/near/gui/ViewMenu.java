package edu.jhuapl.near.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenu;

public class ViewMenu extends JMenu implements PropertyChangeListener
{
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
