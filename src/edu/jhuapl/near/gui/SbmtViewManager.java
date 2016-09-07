package edu.jhuapl.near.gui;

import java.awt.Frame;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;

public class SbmtViewManager extends ViewManager
{
    public SbmtViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
        for (ViewConfig config: SmallBodyConfig.getBuiltInConfigs())
        {
            addBuiltInView(new SbmtView(statusBar, (SmallBodyConfig)config));
        }
    }

    public View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        SmallBodyConfig config = new SmallBodyConfig();
        config.customName = name;
        config.customTemporary = temporary;
        config.author = ShapeModelAuthor.CUSTOM;
        return new SbmtView(statusBar, config);
    }

}
