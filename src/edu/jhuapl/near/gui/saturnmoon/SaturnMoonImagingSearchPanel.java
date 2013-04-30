package edu.jhuapl.near.gui.saturnmoon;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.SaturnMoonQuery;
import edu.jhuapl.near.query.QueryBase;

public class SaturnMoonImagingSearchPanel extends AbstractImageSearchPanel
{
    private QueryBase query;

    public SaturnMoonImagingSearchPanel(
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        super(modelManager, infoPanelManager, pickManager, renderer);

        String smallBodyModelName = modelManager.getSmallBodyModel().getModelName().toLowerCase();

        if (smallBodyModelName.startsWith("dione"))
            query = new SaturnMoonQuery("/GASKELL/DIONE/IMAGING");
        else if (smallBodyModelName.startsWith("phoebe"))
            query = new SaturnMoonQuery("/GASKELL/PHOEBE/IMAGING");
        else if (smallBodyModelName.startsWith("mimas"))
            query = new SaturnMoonQuery("/GASKELL/MIMAS/IMAGING");
    }

    @Override
    protected Date getDefaultStartDate()
    {
        return new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
    }

    @Override
    protected QueryBase getQuery()
    {
        return query;
    }

    @Override
    protected String[] getFilterNames()
    {
        return new String[]{};
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return new String[]{};
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
