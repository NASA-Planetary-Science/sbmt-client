package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

public class EllipsesPopupMenu extends StructuresPopupMenu
{
    public EllipsesPopupMenu(ModelManager modelManager)
    {
        super((EllipseModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES), true, true);
    }
}
