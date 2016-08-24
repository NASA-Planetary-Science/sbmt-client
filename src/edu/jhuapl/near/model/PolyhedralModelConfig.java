package edu.jhuapl.near.model;

import java.util.Date;



/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public abstract class PolyhedralModelConfig extends Config
{
    public boolean hasLidarData = false;
    public Date lidarSearchDefaultStartDate;
    public Date lidarSearchDefaultEndDate;

    public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1

}
