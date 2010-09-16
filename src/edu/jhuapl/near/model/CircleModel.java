package edu.jhuapl.near.model;


/**
 * Model of circle structures drawn on a body.
 * 
 * @author 
 *
 */

public class CircleModel extends RegularPolygonModel 
{
	public CircleModel(SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel, 20, true, "circle", ModelNames.CIRCLE_STRUCTURES);
		setInteriorOpacity(0.0);
		int[] color = {255, 0, 255};
		setDefaultColor(color);
	}
}