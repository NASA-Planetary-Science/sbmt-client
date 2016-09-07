package edu.jhuapl.saavtk.example;

import java.util.ArrayList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;

public class ExampleConfig extends ViewConfig
{
    static public ExampleConfig getExampleConfig(ShapeModelBody name, ShapeModelAuthor author)
    {
        return (ExampleConfig)getConfig(name, author, null);
    }

    static public ExampleConfig getExampleConfig(ShapeModelBody name, ShapeModelAuthor author, String version)
    {
        return (ExampleConfig)getConfig(name, author, version);
    }

    public static void initialize()
    {
        ArrayList<ViewConfig> configArray = getBuiltInConfigs();

        ExampleConfig config = new ExampleConfig();
        config.customName = "data/brain.obj";
        config.customTemporary = true;
        config.author = ShapeModelAuthor.CUSTOM;
        configArray.add(config);

        config = new ExampleConfig();
        config.customName = "data/left-lung.obj";
        config.customTemporary = true;
        config.author = ShapeModelAuthor.CUSTOM;
        configArray.add(config);
    }

    public ExampleConfig clone() // throws CloneNotSupportedException
    {
        ExampleConfig c = (ExampleConfig)super.clone();

        return c;
    }

}
