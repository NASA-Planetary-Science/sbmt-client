package edu.jhuapl.near.gui.eros;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.ErosQuery;
import edu.jhuapl.near.query.QueryBase;

public class MSISearchPanel extends AbstractImageSearchPanel
{

    public MSISearchPanel(
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
        return new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return ErosQuery.getInstance();
    }

    @Override
    protected String[] getFilterNames()
    {
        return new String[]{
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return new String[]{"iofdbl", "cifdbl"};
    }

    @Override
    protected double getDefaultMaxSpacecraftDistance()
    {
        return 100.0;
    }

    @Override
    protected double getDefaultMaxResolution()
    {
        return 50.0;
    }

    @Override
    protected ImageSource[] getImageSources()
    {
        return new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS};
    }
}
