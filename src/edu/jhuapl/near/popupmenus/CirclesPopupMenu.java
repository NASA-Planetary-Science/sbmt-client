package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

public class CirclesPopupMenu extends StructuresPopupMenu
{
    public CirclesPopupMenu(ModelManager modelManager)
    {
        super((CircleModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), true, true);
    }
}
