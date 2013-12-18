package edu.jhuapl.near.model;


/**
 * Model of line structures drawn on a body.
 */
public class PointModel extends AbstractEllipsePolygonModel
{
    public PointModel(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, 4, Mode.POINT_MODE, "point", ModelNames.POINT_STRUCTURES);
        setInteriorOpacity(1.0);
        int[] color = {255, 0, 255};
        setDefaultColor(color);
    }
}
