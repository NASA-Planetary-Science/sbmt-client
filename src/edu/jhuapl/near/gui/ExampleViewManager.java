package edu.jhuapl.near.gui;

import java.awt.Frame;

import edu.jhuapl.near.model.PolyhedralModelConfig;
import edu.jhuapl.near.model.ShapeModelAuthor;

public class ExampleViewManager extends ViewManager
{
    public ExampleViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
    {
        super(statusBar, frame, tempCustomShapeModelPath);
    }

    public View createCustomView(StatusBar statusBar, String name, boolean temporary)
    {
        PolyhedralModelConfig config = new PolyhedralModelConfig();
        config.customName = name;
        config.customTemporary = temporary;
        config.author = ShapeModelAuthor.CUSTOM;
        return new ExampleView(statusBar, config);
    }
}
