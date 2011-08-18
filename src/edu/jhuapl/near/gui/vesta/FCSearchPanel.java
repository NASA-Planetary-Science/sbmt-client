package edu.jhuapl.near.gui.vesta;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.jhuapl.near.gui.AbstractImageSearchPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.query.QueryBase;
import edu.jhuapl.near.query.VestaQuery;

public class FCSearchPanel extends AbstractImageSearchPanel
{

    public FCSearchPanel(
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
        return new GregorianCalendar(2011, 6, 1, 0, 0, 0).getTime();
    }

    @Override
    protected Date getDefaultEndDate()
    {
        return new GregorianCalendar(2012, 7, 1, 0, 0, 0).getTime();
    }

    @Override
    protected String getImageCollectionModelName()
    {
        return ModelNames.FC_IMAGES;
    }

    @Override
    protected String getImageBoundaryCollectionModelName()
    {
        return ModelNames.FC_BOUNDARY;
    }

    @Override
    protected String getColorImageCollectionModelName()
    {
        return ModelNames.FC_COLOR_IMAGES;
    }

    @Override
    protected QueryBase getQuery()
    {
        return VestaQuery.getInstance();
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
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        };
    }

    @Override
    protected boolean hasUserDefinedCheckBoxes()
    {
        return true;
    }

    @Override
    protected String[] getUserDefinedCheckBoxesNames()
    {
        return new String[]{"FC1", "FC2"};
    }

    @Override
    protected boolean showSourceComboBox()
    {
        return false;
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
}
