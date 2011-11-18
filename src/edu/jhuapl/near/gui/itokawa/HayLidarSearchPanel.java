package edu.jhuapl.near.gui.itokawa;

import java.util.Date;

import org.joda.time.DateTime;

import edu.jhuapl.near.gui.LidarSearchPanel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.pick.PickManager;

public class HayLidarSearchPanel extends LidarSearchPanel
{

    public HayLidarSearchPanel(ModelManager modelManager,
            PickManager pickManager)
    {
        super(modelManager, pickManager);
    }

    @Override
    protected Date getDefaultStartDate()
    {
        return new DateTime(2005, 9, 1, 0, 0, 0, 0).toDate();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new DateTime(2005, 11, 30, 0, 0, 0, 0).toDate();
    }

    @Override
    protected String getLidarModelName()
    {
        return ModelNames.HAYLIDAR_SEARCH;
    }

}
