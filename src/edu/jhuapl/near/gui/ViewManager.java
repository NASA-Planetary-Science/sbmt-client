package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.util.Configuration;

public class ViewManager extends JPanel
{
    private ArrayList<View> builtInViews = new ArrayList<View>();
    private ArrayList<View> customViews = new ArrayList<View>();
    private View currentView;
    private final StatusBar statusBar;
    private final Frame frame;

    /**
     * The top level frame is required so that the title can be updated
     * when the view changes.
     *
     * @param statusBar
     * @param frame
     */
    public ViewManager(StatusBar statusBar, Frame frame)
    {
        super(new CardLayout());
        setBorder(BorderFactory.createEmptyBorder());
        this.statusBar = statusBar;
        this.frame = frame;

        for (SmallBodyConfig config: SmallBodyConfig.builtInSmallBodyConfigs)
        {
            builtInViews.add(new View(statusBar, config));
        }


        currentView = builtInViews.get(0);
        currentView.initialize();

        frame.setTitle(currentView.getSmallBodyConfig().getPathRepresentation());

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

        frame.setTitle(currentView.getSmallBodyConfig().getPathRepresentation());
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
        add(view, view.getUniqueName());
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
