package edu.jhuapl.near.model;


/**
 * Model of line structures drawn on Eros.
 * 
 * @author 
 *
 */
public class PointModel extends RegularPolygonModel 
{
	public PointModel(ErosModel erosModel)
	{
		super(erosModel, 4, false, "point");
		setInteriorOpacity(1.0);
		double[] color = {1.0, 0.0, 1.0};
		setInteriorColor(color);
	}
}
