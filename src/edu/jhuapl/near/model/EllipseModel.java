package edu.jhuapl.near.model;

public class EllipseModel extends AbstractEllipsePolygonModel
{
    public EllipseModel(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.ELLIPSE_MODE, "ellipse", ModelNames.ELLIPSE_STRUCTURES);
        setInteriorOpacity(0.0);
        int[] color = {255, 0, 255};
        setDefaultColor(color);
    }
}
