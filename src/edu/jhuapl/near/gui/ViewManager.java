package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import edu.jhuapl.near.util.Configuration;

public class ViewManager extends JPanel
{
    private ArrayList<View> builtInViews = new ArrayList<View>();
    private ArrayList<View> customViews = new ArrayList<View>();
    private View currentView;
    private final StatusBar statusBar;
    private final Frame frame;
    private String tempCustomShapeModelPath;

    private static String defaultModelName=null;
    private final static Path defaultModelFile=Paths.get(Configuration.getApplicationDataDir()+File.separator+"defaultModelToLoad");



    /**
     * The top level frame is required so that the title can be updated
     * when the view changes.
     *
     * @param statusBar
     * @param frame
     * @param tempCustomShapeModelPath path to shape model. May be null.
     * If non-null, the main window will create a temporary custom view of the shape model
     * which will be shown first. This temporary view is not saved into the custom application
     * folder and will not be available unless explicitely imported.
     */
    public ViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(new CardLayout());
        setBorder(BorderFactory.createEmptyBorder());
        this.statusBar = statusBar;
        this.frame = frame;
        this.tempCustomShapeModelPath = tempCustomShapeModelPath;

        setupViews();
    }

    protected void addBuiltInView(View view)
    {
        builtInViews.add(view);
    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
    }

    protected void setupViews()
    {
        addBuiltInViews(statusBar);

        for (View view : getBuiltInViews())
            add(view, view.getUniqueName());

        loadCustomViews(getTempCustomShapeModelPath(), statusBar);

        for (View view : getCustomViews())
            add(view, view.getUniqueName());
    }

    public void setDefaultBodyToLoad(String uniqueName)
    {
        try
        {
            defaultModelName=uniqueName;
            if (defaultModelFile.toFile().exists())
                defaultModelFile.toFile().delete();
            defaultModelFile.toFile().createNewFile();
            FileWriter writer=new FileWriter(defaultModelFile.toFile());
            writer.write(defaultModelName);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getDefaultBodyToLoad()
    {
        return defaultModelName;
    }

    public void resetDefaultBodyToLoad()
    {
        if (defaultModelFile.toFile().exists())
            defaultModelFile.toFile().delete();
    }

    protected void loadCustomViews(String newCustomShapeModelPath, StatusBar statusBar)
    {
        if (newCustomShapeModelPath != null)
        {
            addCustomView(View.createTemporaryCustomView(statusBar, newCustomShapeModelPath));
        }
    }

    public ArrayList<View> getBuiltInViews()
    {
        return builtInViews;
    }

    public void setBuiltInViews(ArrayList<View> builtInViews)
    {
        this.builtInViews = builtInViews;
    }

    public ArrayList<View> getCustomViews()
    {
        return customViews;
    }

    public void setCustomViews(ArrayList<View> customViews)
    {
        this.customViews = customViews;
    }

    public String getTempCustomShapeModelPath()
    {
        return tempCustomShapeModelPath;
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

    protected void addCustomView(View view)
    {
        customViews.add(view);
    }

    public View addCustomView(String name)
    {
        View view = View.createCustomView(statusBar, name);
        addCustomView(view);
        add(view, view.getUniqueName());
        return view;
    }

    public View removeCustomView(String name)
    {
        for (View view : customViews)
        {
            if (view.getSmallBodyConfig().getShapeModelName().equals(name))
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
