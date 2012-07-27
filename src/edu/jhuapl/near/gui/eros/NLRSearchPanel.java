package edu.jhuapl.near.gui.eros;

import java.util.Date;

import org.joda.time.DateTime;

import edu.jhuapl.near.gui.LidarSearchPanel;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;

public class NLRSearchPanel extends LidarSearchPanel
{
    public NLRSearchPanel(ModelManager modelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        super(modelManager, pickManager, renderer);
    }

    @Override
    protected Date getDefaultStartDate()
    {
        return new DateTime(2000, 2, 28, 0, 0, 0, 0).toDate();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new DateTime(2001, 2, 13, 0, 0, 0, 0).toDate();
    }
}
