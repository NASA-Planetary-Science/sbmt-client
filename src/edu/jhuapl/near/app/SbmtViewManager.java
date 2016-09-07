package edu.jhuapl.near.app;

import java.awt.Frame;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;

public class SbmtViewManager extends ViewManager
{
    public SbmtViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
        for (ViewConfig config: SmallBodyViewConfig.getBuiltInConfigs())
        {
            addBuiltInView(new SbmtView(statusBar, (SmallBodyViewConfig)config));
        }
    }

    public View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        SmallBodyViewConfig config = new SmallBodyViewConfig();
        config.customName = name;
        config.customTemporary = temporary;
        config.author = ShapeModelAuthor.CUSTOM;
        return new SbmtView(statusBar, config);
    }

}
