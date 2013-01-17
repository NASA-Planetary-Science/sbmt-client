package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

public class CirclesPopupMenu extends StructuresPopupMenu
{
    public CirclesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((CircleModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), renderer, true, true);
    }
}
