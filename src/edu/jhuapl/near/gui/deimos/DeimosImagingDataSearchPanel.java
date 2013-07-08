package edu.jhuapl.near.gui.deimos;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.DeimosQuery;
import edu.jhuapl.near.query.QueryBase;

public class DeimosImagingDataSearchPanel extends AbstractImageSearchPanel
{

    public DeimosImagingDataSearchPanel(
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        super(modelManager, infoPanelManager, pickManager, renderer);

    }

    @Override
    protected Date getDefaultStartDate()
    {
        return new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(1978, 9, 19, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return DeimosQuery.getInstance();
    }

    @Override
    protected String[] getFilterNames()
    {
        return new String[]{
                "VIS, Blue",
                "VIS, Minus Blue",
                "VIS, Violet",
                "VIS, Clear",
                "VIS, Green",
                "VIS, Red",
        };
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return new String[]{"Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
    }

    @Override
    protected double getDefaultMaxSpacecraftDistance()
    {
        return 9000.0;
    }

    @Override
    protected double getDefaultMaxResolution()
    {
        return 300.0;
    }

    @Override
    protected ImageSource[] getImageSources()
    {
        return new ImageSource[]{ImageSource.PDS};
    }
}
