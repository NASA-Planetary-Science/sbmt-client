package edu.jhuapl.sbmt.app;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.ViewMenu;

public class SbmtViewMenu extends ViewMenu
{
    private ButtonGroup group;

    public SbmtViewMenu(ViewManager rootPanel, RecentlyViewed viewed)
    {
        super(rootPanel, viewed);
    }

    protected void addMenuItem(JMenuItem mi, ViewConfig config)
    {
        SmallBodyViewConfig smallBodyConfig = (SmallBodyViewConfig)config;
        List<String> tree = new ArrayList<String>();
        if (smallBodyConfig.type != null)
            tree.add(smallBodyConfig.type.toString());
        if (smallBodyConfig.population != null)
            tree.add(smallBodyConfig.population.toString());
        if (smallBodyConfig.body != null && smallBodyConfig.author != null)
            tree.add(smallBodyConfig.body.toString());
        if (smallBodyConfig.dataUsed != null && smallBodyConfig.author != null)
            tree.add(smallBodyConfig.dataUsed.toString());

        JMenu parentMenu = this;
        for (String subMenu : tree)
        {
            JMenu childMenu = getChildMenu(parentMenu, subMenu);
            if (childMenu == null)
            {
                childMenu = new JMenu(subMenu);
                parentMenu.add(childMenu);
            }
            parentMenu = childMenu;
        }

        parentMenu.add(mi);
    }
}
