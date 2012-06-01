package edu.jhuapl.near.gui.gaspra;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.GaspraEverythingQuery;
import edu.jhuapl.near.query.QueryBase;

public class SSIGaspraSearchPanel extends AbstractImageSearchPanel
{

    public SSIGaspraSearchPanel(
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
        return new GregorianCalendar(1991, 9, 29, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(1991, 9, 30, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return GaspraEverythingQuery.getInstance();
    }

    @Override
    protected int getNumberOfFilters()
    {
        return 0;
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
    protected boolean hasUserDefinedCheckBoxes()
    {
        return false;
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return null;
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
        return new ImageSource[]{ImageSource.CORRECTED};
    }
}
