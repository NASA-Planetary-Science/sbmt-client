package edu.jhuapl.sbmt.client;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;

public class ApplicationConfiguration
{
    protected enum ApplicationKey {
        HAYABUSA2,
        OSIRIS_REX,
        ;
    }

    public static ApplicationConfiguration of(String appId)
    {
        return new ApplicationConfiguration(appId);
    }

    protected ApplicationConfiguration(String appId)
    {
        SmallBodyViewConfig.initialize();
        if (appId != null)
        {
            ApplicationKey key = ApplicationKey.valueOf(appId);
            disableAll();
            enableBodies(key);
        }
    }

    protected void disableAll()
    {
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            each.enable(false);
        }
    }

    protected void enableBodies(ApplicationKey key)
    {
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            if (each instanceof SmallBodyViewConfig)
            {
                SmallBodyViewConfig config = (SmallBodyViewConfig) each;
                setBodyEnableState(key, config);
            }
        }
        // TODO switch logo for this app.
    }

    protected void setBodyEnableState(ApplicationKey key, SmallBodyViewConfig config)
    {
        switch (key)
        {
        case HAYABUSA2:
            if (
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelAuthor.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case OSIRIS_REX:
            if (
                    ShapeModelBody.RQ36.equals(config.body) ||
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelAuthor.OREX.equals(config.author)
               )
            {
                config.enable(true);
            }
            break;
            default:
                throw new AssertionError();
        }
    }
}
