package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PolygonModel;

public class PolygonsPopupMenu extends StructuresPopupMenu
{
    public PolygonsPopupMenu(ModelManager modelManager)
    {
        super((PolygonModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES), false, true);
    }
}
