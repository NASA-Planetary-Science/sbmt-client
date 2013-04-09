package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.util.Configuration;

public class ViewManager extends JPanel
{
    private ArrayList<View> builtInViews = new ArrayList<View>();
    private ArrayList<View> customViews = new ArrayList<View>();
    private View currentView;
    private final StatusBar statusBar;

    public ViewManager(StatusBar statusBar)
    {
        super(new CardLayout());
        setBorder(BorderFactory.createEmptyBorder());
        this.statusBar = statusBar;

        for (ModelFactory.ModelConfig config: ModelFactory.builtInModelConfigs)
        {
            // Vesta, RQ36, and Lutetia are currently restricted
            if (Configuration.isAPLVersion() ||
                    (!config.name.equals("Vesta") &&
                     !config.name.equals("RQ36") &&
                     !config.name.equals("Lutetia")))
            {
                builtInViews.add(new View(statusBar, config));
            }
        }


        currentView = builtInViews.get(0);
        currentView.initialize();

        for (View view : builtInViews)
            add(view, view.getUniqueName());

        loadCustomViews();

        for (View view : customViews)
            add(view, view.getUniqueName());
    }

    private void loadCustomViews()
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
                    customViews.add(View.createCustomView(statusBar, dir.getName()));
                }
            }
        }
    }

    public View getCurrentView()
    {
        return currentView;
    }

    public void setCurrentView(View view)
    {
        CardLayout cardLayout = (CardLayout)(getLayout());
        cardLayout.show(this, view.getUniqueName());

        // defer initialization of View until we show it.
        repaint();
        validate();
        view.initialize();

        currentView = view;
    }

    public View getBuiltInView(int i)
    {
        return builtInViews.get(i);
    }

    public int getNumberOfBuiltInViews()
    {
        return builtInViews.size();
    }

    public View getCustomView(int i)
    {
        return customViews.get(i);
    }

    public int getNumberOfCustomViews()
    {
        return customViews.size();
    }

    public View addCustomView(String name)
    {
        View view = View.createCustomView(statusBar, name);
        customViews.add(view);
        add(view, name);
        return view;
    }

    public View removeCustomView(String name)
    {
        for (View view : customViews)
        {
            if (view.getUniqueName().equals(name))
            {
                customViews.remove(view);
                remove(view);
                return view;
            }
        }

        return null;
    }

    public View getCustomView(String name)
    {
        for (View view : customViews)
        {
            if (view.getUniqueName().equals(name))
            {
                return view;
            }
        }

        return null;
    }

    public ArrayList<View> getAllViews()
    {
        ArrayList<View> allViews = new ArrayList<View>();
        allViews.addAll(builtInViews);
        allViews.addAll(customViews);
        return allViews;
    }
}
