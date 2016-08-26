package edu.jhuapl.near.gui;

import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import edu.jhuapl.near.model.SmallBodyConfig;

public class SbmtViewMenu extends ViewMenu
{
    private ButtonGroup group;

    public SbmtViewMenu(ViewManager rootPanel, RecentlyViewed viewed)
    {
        super(rootPanel, viewed);
    }

    protected void initialize()
    {
        for (int i=0; i < getRootPanel().getNumberOfBuiltInViews(); ++i)
        {
            View view = getRootPanel().getBuiltInView(i);
            JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(view));
            mi.setText(view.getDisplayName());
            if (i==0)
                mi.setSelected(true);

            SmallBodyConfig smallBodyConfig = view.getSmallBodyConfig();

            addMenuItem(mi, smallBodyConfig);
        }

        super.initialize();

    }

    private void addMenuItem(JMenuItem mi, SmallBodyConfig smallBodyConfig)
    {
        ArrayList<String> tree = new ArrayList<String>();
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
