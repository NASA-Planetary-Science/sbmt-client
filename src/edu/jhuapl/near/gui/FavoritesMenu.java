package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class FavoritesMenu extends JMenu
{
    FavoritesFile favoritesFile;
    ViewManager manager;

    public FavoritesMenu(FavoritesFile file, ViewManager manager)
    {
        super("\u2661");    // cute unicode heart
        favoritesFile=file;
        this.manager=manager;
        rebuild();
    }

    private void rebuild()
    {
        removeAll();
        //
        JMenuItem add=new JMenuItem();
        JMenuItem rem=new JMenuItem();
        add.setAction(new AddFavoriteAction("Add to favorites"));
        rem.setAction(new RemoveFavoriteAction("Remove from favorites"));
        add(add);
        add(rem);
        add(new JSeparator());
        //
        List<String> stringsOnFile=favoritesFile.getAllFavorites();
        for (String viewName : stringsOnFile)
            add(new FavoritesMenuItem(viewName, manager));
        //

    }

    private class FavoritesMenuItem extends JMenuItem
    {
        public FavoritesMenuItem(String viewName, ViewManager manager)
        {
            super(viewName);
            setAction(new ShowFavoriteAction(manager, viewName));
        }
    }

    private class ShowFavoriteAction extends AbstractAction
    {
        ViewManager manager;
        String viewName;

        public ShowFavoriteAction(ViewManager manager, String viewName)
        {
            super(viewName);
            this.manager=manager;
            this.viewName=viewName;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            for (int i=0; i<manager.getNumberOfBuiltInViews(); i++)
                if (manager.getBuiltInView(i).getUniqueName().equals(unfilterViewName(viewName)))
                    manager.setCurrentView(manager.getBuiltInView(i));
            for (int i=0; i<manager.getNumberOfCustomViews(); i++)
                if (manager.getCustomView(i).getUniqueName().equals(unfilterViewName(viewName)))
                    manager.setCurrentView(manager.getCustomView(i));
        }

    }

    private class AddFavoriteAction extends AbstractAction
    {
        public AddFavoriteAction(String string)
        {
            super(string);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            favoritesFile.addFavorite(filterViewName(manager.getCurrentView().getUniqueName()));
            rebuild();
        }
    }

    private class RemoveFavoriteAction extends AbstractAction
    {
        public RemoveFavoriteAction(String string)
        {
            super(string);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            favoritesFile.removeFavorite(filterViewName(manager.getCurrentView().getUniqueName()));
            rebuild();
        }
    }

    private String filterViewName(String str)
    {
        return str.replace(' ','|');
    }

    private String unfilterViewName(String str)
    {
        return str.replace('|',' ');
    }

}
