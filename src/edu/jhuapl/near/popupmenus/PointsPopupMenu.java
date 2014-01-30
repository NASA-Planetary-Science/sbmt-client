package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;

public class PointsPopupMenu extends StructuresPopupMenu
{
    public PointsPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((PointModel)modelManager.getModel(ModelNames.POINT_STRUCTURES), renderer, true, false, false);
    }
}
