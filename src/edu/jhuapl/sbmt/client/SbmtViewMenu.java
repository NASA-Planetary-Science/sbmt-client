package edu.jhuapl.sbmt.client;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.ViewMenu;

public class SbmtViewMenu extends ViewMenu
{
    public SbmtViewMenu(SbmtViewManager viewManager, RecentlyViewed viewed)
    {
        // Note the base class constructor calls addMenuItem.
        super("Body", viewManager, viewed);
    }

    @Override
    protected void addMenuItem(JMenuItem mi, ViewConfig config)
    {
        // Note the base class constructor calls this method. For this reason
        // cannot move the following code block to the constructor and store
        // the SbmtViewManager as a field, as would be more natural.
        ViewManager manager = getRootPanel();
        SbmtViewManager viewManager = null;
        if (manager instanceof SbmtViewManager)
        {
            viewManager = (SbmtViewManager) manager;
        }
        else
        {
            throw new AssertionError();
        }

        // Set up a hierarchy like "Body" -> Asteroids -> Near Earth -> Eros -> Image-based -> Gaskell.
        // Encode this as a list of strings.
        SmallBodyViewConfig smallBodyConfig = (SmallBodyViewConfig)config;
        List<String> tree = new ArrayList<>();
        if (smallBodyConfig.type != null)
            tree.add(smallBodyConfig.type.toString());
        if (smallBodyConfig.population != null)
            tree.add(smallBodyConfig.population.toString());
        if (smallBodyConfig.body != null && smallBodyConfig.author != null)
            tree.add(smallBodyConfig.body.toString());
        if (smallBodyConfig.dataUsed != null && smallBodyConfig.author != null)
            tree.add(smallBodyConfig.dataUsed.toString());

        // Go through the list of strings and generate a hierarchical menu tree.
        JMenu parentMenu = this;
        for (String subMenu : tree)
        {
            JMenu childMenu = getChildMenu(parentMenu, subMenu);
            if (childMenu == null)
            {
                childMenu = new JMenu(subMenu);
                if (viewManager.isAddSeparator(config, subMenu))
                {
                    parentMenu.addSeparator();
                }
                parentMenu.add(childMenu);
            }
            parentMenu = childMenu;
        }
        parentMenu.add(mi);
    }
}
