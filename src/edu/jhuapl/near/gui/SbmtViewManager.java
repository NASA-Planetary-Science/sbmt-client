package edu.jhuapl.near.gui;

import java.awt.Frame;

import edu.jhuapl.near.model.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig;

public class SbmtViewManager extends ViewManager
{
    public SbmtViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    protected void addBuiltInViews(StatusBar statusBar)
    {
        for (SmallBodyConfig config: SmallBodyConfig.builtInSmallBodyConfigs)
        {
            addBuiltInView(new SbmtView(statusBar, config));
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
