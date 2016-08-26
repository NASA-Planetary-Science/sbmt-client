package edu.jhuapl.near.gui;

import java.awt.Frame;
import java.io.File;
import java.util.Arrays;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.util.Configuration;

public class ExampleViewManager extends ViewManager
{
    public ExampleViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
        for (SmallBodyConfig config: SmallBodyConfig.builtInSmallBodyConfigs)
        {
            addBuiltInView(new ExampleView(statusBar, config));
        }
    }

    protected void setupViews()
    {
        super.setupViews();

        if (getTempCustomShapeModelPath() == null)
        {
            int idxToShow=0;
            for (int i=0; i<getBuiltInViews().size(); i++)
                if (getBuiltInViews().get(i).getSmallBodyConfig().getUniqueName().equals(getDefaultBodyToLoad()))
                    idxToShow=i;
            setCurrentView(getBuiltInViews().get(idxToShow));
        }
        else
        {
            int idxToShow=0;
            for (int i=0; i<getCustomViews().size(); i++)
                if (getCustomViews().get(i).getSmallBodyConfig().getUniqueName().equals(getDefaultBodyToLoad()))
                    idxToShow=i;
            setCurrentView(getCustomViews().get(idxToShow));
        }
    }

    protected void loadCustomViews(String newCustomShapeModelPath, StatusBar statusBar)
    {
        super.loadCustomViews(newCustomShapeModelPath, statusBar);

        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            Arrays.sort(dirs);
            for (File dir : dirs)
            {
                if (new File(dir, "model.vtk").isFile())
                {
                    addCustomView(View.createCustomView(statusBar, dir.getName()));
                }
            }
        }

    }

}
