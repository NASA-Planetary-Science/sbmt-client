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
		setInteriorOpacity(1.0);
		double[] boundaryColor = {1.0, 0.0, 1.0};
		setBoundaryColor(boundaryColor);
		double[] interiorColor = {0.0, 0.0, 0.0};
		setInteriorColor(interiorColor);
	}
}