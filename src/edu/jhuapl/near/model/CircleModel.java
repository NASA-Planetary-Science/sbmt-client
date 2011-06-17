package edu.jhuapl.near.model;


/**
 * Model of circle structures drawn on a body.
 *
 * @author
 *
 */

public class CircleModel extends AbstractEllipsePolygonModel
{
    public CircleModel(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.CIRCLE_MODE, "circle", ModelNames.CIRCLE_STRUCTURES);
        setInteriorOpacity(0.0);
        int[] color = {255, 0, 255};
        setDefaultColor(color);
    }
}