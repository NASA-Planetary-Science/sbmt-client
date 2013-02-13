package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

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

        for (Viewer.ViewerConfig config: Viewer.builtInViewerConfigs)
        {
            // Vesta, RQ36, and Lutetia are currently restricted
            if (Configuration.isAPLVersion() ||
                    (!config.name.equals("Vesta") &&
                     !config.name.equals("RQ36") &&
                     !config.name.equals("Lutetia")))
            {
                builtInViewers.add(new Viewer(statusBar, config));
            }
        }


        currentViewer = builtInViewers.get(0);
        currentViewer.initialize();

        for (Viewer viewer : builtInViewers)
            add(viewer, viewer.getUniqueName());

        loadCustomViewers();

        for (Viewer viewer : customViewers)
            add(viewer, viewer.getUniqueName());
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
                    customViewers.add(Viewer.createCustomViewer(statusBar, dir.getName()));
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
        CardLayout cardLayout = (CardLayout)(getLayout());
        cardLayout.show(this, viewer.getUniqueName());

        // defer initialization of Viewer until we show it.
        repaint();
        validate();
        viewer.initialize();

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
        Viewer viewer = Viewer.createCustomViewer(statusBar, name);
        customViewers.add(viewer);
        add(viewer, name);
        return viewer;
    }

    public Viewer removeCustomViewer(String name)
    {
        for (Viewer viewer : customViewers)
        {
            if (viewer.getUniqueName().equals(name))
            {
                customViewers.remove(viewer);
                remove(viewer);
                return viewer;
            }
        }

        return null;
    }

    public Viewer getCustomViewer(String name)
    {
        for (Viewer viewer : customViewers)
        {
            if (viewer.getUniqueName().equals(name))
            {
                return viewer;
            }
        }

        return null;
    }

    public ArrayList<Viewer> getAllViewers()
    {
        ArrayList<Viewer> allViewers = new ArrayList<Viewer>();
        allViewers.addAll(builtInViewers);
        allViewers.addAll(customViewers);
        return allViewers;
    }
}
