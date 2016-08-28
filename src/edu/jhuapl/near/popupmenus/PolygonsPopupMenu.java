package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PolygonModel;

public class PolygonsPopupMenu extends StructuresPopupMenu
{
    public PolygonsPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((PolygonModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES), modelManager.getPolyhedralModel(), renderer, false, true, true);
    }
}
