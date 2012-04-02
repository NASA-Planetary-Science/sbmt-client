package edu.jhuapl.near.util;

import java.io.File;

/**
 * Singleton preferences class for managing all preferences
 *
 * @author kahneg1
 *
 */
public class Preferences extends MapUtil
{
    // List of all preferences keys
    public static final String LIGHTING_TYPE = "LightingType";
    public static final String HEADLIGHT_INTENSITY = "HeadlightIntensity";
    public static final String SHOW_AXES = "ShowAxes";
    public static final String INTERACTIVE_AXES = "InteractiveAxes";


    private static final String preferencesPath = Configuration.getApplicationDataDir() + File.separator + "preferences.txt";

    private static Preferences ref = null;

    public static Preferences getInstance()
    {
        if (ref == null)
            ref = new Preferences();
        return ref;
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private Preferences()
    {
        super(preferencesPath);
    }
}
