package edu.jhuapl.near.gui.phobos;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.PhobosQuery;
import edu.jhuapl.near.query.QueryBase;

public class PhobosImagingDataSearchPanel extends AbstractImageSearchPanel
{

    public PhobosImagingDataSearchPanel(
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
        return new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return PhobosQuery.getInstance();
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
                "Phobos 2 VSK, Channel 1",
                "Phobos 2 VSK, Channel 2",
                "Phobos 2 VSK, Channel 3",
                "Viking Orbiter VIS, Clear",
                "Viking Orbiter VIS, Green",
                "Viking Orbiter VIS, Minus Blue",
                "Viking Orbiter VIS, Red",
                "Viking Orbiter VIS, Violet",
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
        return new String[]{"Viking", "Phobos 2 VSK"};
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
