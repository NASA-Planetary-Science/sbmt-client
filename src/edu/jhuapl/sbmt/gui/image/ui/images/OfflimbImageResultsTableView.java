package edu.jhuapl.sbmt.gui.image.ui.images;

import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

public class OfflimbImageResultsTableView extends ImageResultsTableView
{

    /**
     * @wbp.parser.constructor
     */
    public OfflimbImageResultsTableView(ImagingInstrument instrument, ImageCollection imageCollection, ImagePopupMenu imagePopupMenu)
    {
        super(instrument, imageCollection, imagePopupMenu);
        init();
        resultList = new OfflimbImageResultsTable();
    }

    public int getOffLimbIndex()
    {
        return ((OfflimbImageResultsTable)resultList).getOffLimbIndex();
    }
}
