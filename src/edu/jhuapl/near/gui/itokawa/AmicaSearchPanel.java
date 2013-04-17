package edu.jhuapl.near.gui.itokawa;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.ItokawaQuery;
import edu.jhuapl.near.query.QueryBase;

/**
 *
 * @author kahneg1
 */
public class AmicaSearchPanel extends AbstractImageSearchPanel
{

    public AmicaSearchPanel(
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
        return new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return ItokawaQuery.getInstance();
    }

    @Override
    protected String[] getFilterNames()
    {
        return new String[]{
                "Filter ul (381 nm)",
                "Filter b (429 nm)",
                "Filter v (553 nm)",
                "Filter w (700 nm)",
                "Filter x (861 nm)",
                "Filter p (960 nm)",
                "Filter zs (1008 nm)"
        };
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return new String[]{};
    }

    @Override
    protected double getDefaultMaxSpacecraftDistance()
    {
        return 26.0;
    }

    @Override
    protected double getDefaultMaxResolution()
    {
        return 3.0;
    }

    @Override
    protected ImageSource[] getImageSources()
    {
        return new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS, ImageSource.CORRECTED};
    }
}
