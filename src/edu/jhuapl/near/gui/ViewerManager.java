package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.near.gui.custom.CustomViewer;
import edu.jhuapl.near.gui.deimos.DeimosViewer;
import edu.jhuapl.near.gui.eros.ErosViewer;
import edu.jhuapl.near.gui.itokawa.ItokawaViewer;
import edu.jhuapl.near.gui.vesta.VestaViewer;
import edu.jhuapl.near.gui.vesta_old.VestaOldViewer;
import edu.jhuapl.near.util.Configuration;

public class ViewerManager extends JPanel
{
    private ArrayList<Viewer> builtInViewers = new ArrayList<Viewer>();
    private ArrayList<Viewer> customViewers = new ArrayList<Viewer>();
    private Viewer currentViewer;
    private final StatusBar statusBar;

    public ViewerManager(StatusBar statusBar)
    {
        super(new CardLayout());
        setBorder(BorderFactory.createEmptyBorder());
        this.statusBar = statusBar;

        builtInViewers.add(new ErosViewer(statusBar));
        builtInViewers.add(new DeimosViewer(statusBar));
        builtInViewers.add(new ItokawaViewer(statusBar));
        builtInViewers.add(new VestaOldViewer(statusBar));
        builtInViewers.add(new VestaViewer(statusBar));

        currentViewer = builtInViewers.get(0);

        for (Viewer viewer : builtInViewers)
            add(viewer, viewer.getName());

        loadCustomViewers();

        for (Viewer viewer : customViewers)
            add(viewer, viewer.getName());
    }

    private void loadCustomViewers()
    {
        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            Arrays.sort(dirs);
            for (File dir : dirs)
            {
                if (dir.isDirectory())
                {
                    customViewers.add(new CustomViewer(statusBar, dir.getName()));
                }
            }
        }
    }

    public Viewer getCurrentViewer()
    {
        return currentViewer;
    }

    public void setCurrentViewer(Viewer viewer)
    {
        // defer initialization of Viewer until we show it.
        viewer.initialize();

        CardLayout cardLayout = (CardLayout)(getLayout());
        cardLayout.show(this, viewer.getName());

        currentViewer = viewer;
    }

    public Viewer getBuiltInViewer(int i)
    {
        return builtInViewers.get(i);
    }

    public int getNumberOfBuiltInViewers()
    {
        return builtInViewers.size();
    }

    public Viewer getCustomViewer(int i)
    {
        return customViewers.get(i);
    }

    public int getNumberOfCustomViewers()
    {
        return customViewers.size();
    }

    public Viewer addCustomViewer(String name)
    {
        Viewer viewer = new CustomViewer(statusBar, name);
        customViewers.add(viewer);
        add(viewer, name);
        return viewer;
    }

    public Viewer removeCustomViewer(String name)
    {
        for (Viewer viewer : customViewers)
        {
            if (viewer.getName().equals(name))
            {
                customViewers.remove(viewer);
                remove(viewer);
                return viewer;
            }
        }

        return null;
    }
}
