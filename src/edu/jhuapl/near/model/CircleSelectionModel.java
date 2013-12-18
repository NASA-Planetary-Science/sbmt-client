package edu.jhuapl.near.model;


/**
 * Model of circle structures drawn on a body.
 */
public class CircleSelectionModel extends AbstractEllipsePolygonModel
{
    public CircleSelectionModel(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.CIRCLE_MODE, "Selection", ModelNames.CIRCLE_SELECTION);
        //setInteriorOpacity(1.0);
        //int[] color = {255, 0, 255};
        //setDefaultColor(color);
    }
}