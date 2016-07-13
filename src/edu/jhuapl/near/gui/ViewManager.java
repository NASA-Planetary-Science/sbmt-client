package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

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


        for (SmallBodyConfig config: SmallBodyConfig.builtInSmallBodyConfigs)
        {
            builtInViews.add(new View(statusBar, config));
        }

        for (View view : builtInViews)
            add(view, view.getUniqueName());

        loadCustomViews(tempCustomShapeModelPath);

        for (View view : customViews)
            add(view, view.getUniqueName());

        if (tempCustomShapeModelPath == null)
        {
            int idxToShow=0;
            for (int i=0; i<builtInViews.size(); i++)
                if (builtInViews.get(i).getSmallBodyConfig().getUniqueName().equals(getDefaultBodyToLoad()))
                    idxToShow=i;
            setCurrentView(builtInViews.get(idxToShow));
        }
        else
        {
            int idxToShow=0;
            for (int i=0; i<customViews.size(); i++)
                if (customViews.get(i).getSmallBodyConfig().getUniqueName().equals(getDefaultBodyToLoad()))
                    idxToShow=i;
            setCurrentView(customViews.get(idxToShow));
        }
    }

    public static void setDefaultBodyToLoad(String uniqueName)
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

    public static String getDefaultBodyToLoad()
    {
        try
        {
            if (!defaultModelFile.toFile().exists())
                return SmallBodyConfig.builtInSmallBodyConfigs.get(0).getUniqueName();
            //
            Scanner scanner=new Scanner(ViewManager.defaultModelFile.toFile());
            if (scanner.hasNextLine())
                defaultModelName=scanner.nextLine();
            else
                defaultModelName=null;
            scanner.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return defaultModelName;
    }

    public static void resetDefaultBodyToLoad()
    {
        if (defaultModelFile.toFile().exists())
            defaultModelFile.toFile().delete();
    }

    private void loadCustomViews(String newCustomShapeModelPath)
    {
        if (newCustomShapeModelPath != null)
        {
            customViews.add(View.createTemporaryCustomView(statusBar, newCustomShapeModelPath));
        }

        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            Arrays.sort(dirs);
            for (File dir : dirs)
            {
                if (new File(dir, "model.vtk").isFile())
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
