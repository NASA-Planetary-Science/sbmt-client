package edu.jhuapl.near.model;


/**
 * Model of line structures drawn on a body.
 *
 * @author
 *
 */
public class PointModel extends RegularPolygonModel
{
	public PointModel(SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel, 4, false, "point", ModelNames.POINT_STRUCTURES);
		setInteriorOpacity(1.0);
		int[] color = {255, 0, 255};
		setDefaultColor(color);
	}
}
