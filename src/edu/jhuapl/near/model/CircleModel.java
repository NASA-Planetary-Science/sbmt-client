package edu.jhuapl.near.model;


/**
 * Model of circle structures drawn on Eros.
 * 
 * @author 
 *
 */

public class CircleModel extends RegularPolygonModel 
{
	public CircleModel(ErosModel erosModel)
	{
		super(erosModel, 20, true, "circle");
		setInteriorOpacity(0.0);
		int[] color = {255, 0, 255};
		setDefaultColor(color);
	}
}