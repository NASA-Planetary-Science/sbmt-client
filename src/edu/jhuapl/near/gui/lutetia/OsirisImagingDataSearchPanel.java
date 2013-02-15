package edu.jhuapl.near.gui.lutetia;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.LutetiaQuery;
import edu.jhuapl.near.query.QueryBase;

public class OsirisImagingDataSearchPanel extends AbstractImageSearchPanel
{

    public OsirisImagingDataSearchPanel(
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
        return new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return LutetiaQuery.getInstance();
    }

    @Override
    protected int getNumberOfFilters()
    {
        return 8;
    }

    @Override
    protected String[] getFilterNames()
    {
        return new String[]{
                "Filter 1",
                "Filter 2",
                "Filter 3",
                "Filter 4",
                "Filter 5",
                "Filter 6",
                "Filter 7",
                "Filter 8"
        };
    }

    @Override
    protected boolean hasUserDefinedCheckBoxes()
    {
        return false;
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return new String[]{"", ""};
    }

    @Override
    protected double getDefaultMaxSpacecraftDistance()
    {
        return 40000.0;
    }

    @Override
    protected double getDefaultMaxResolution()
    {
        return 4000.0;
    }

    @Override
    protected ImageSource[] getImageSources()
    {
        return new ImageSource[]{ImageSource.GASKELL};
    }
}
