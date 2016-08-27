package edu.jhuapl.near.gui;

import java.awt.Frame;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;

public class ExampleViewManager extends ViewManager
{
    public ExampleViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    public View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        SmallBodyConfig config = new SmallBodyConfig();
        config.customName = name;
        config.customTemporary = temporary;
        config.author = ShapeModelAuthor.CUSTOM;
        return new ExampleView(statusBar, config);
    }
}
