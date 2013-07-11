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
        return new GregorianCalendar(1989, 2, 26, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return PhobosQuery.getInstance();
    }

    @Override
    protected String[] getFilterNames()
    {
        return new String[]{
                "VSK, Channel 1",
                "VSK, Channel 2",
                "VSK, Channel 3",
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
        return new String[]{"Phobos 2", "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
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
        return new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS, ImageSource.CORRECTED};
    }
}
