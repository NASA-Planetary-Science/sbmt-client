package edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers;

import java.awt.Color;

/**
 * @author steelrj1
 *
 */
public class SpacecraftDirectionMarker extends BaseDirectionMarker
{
	/**
	 * @param markerRadius
	 * @param markerHeight
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 */
	public SpacecraftDirectionMarker(double markerRadius, double markerHeight,
			double centerX, double centerY, double centerZ)
	{
		super(markerRadius, markerHeight, centerX, centerY, centerZ, "Spacecraft Direction");
		specularColorValue = 0.1;
		markerColor = new double[] { Color.GREEN.getRed()/255.0, Color.GREEN.getGreen()/255.0, Color.GREEN.getBlue()/255.0 };
	}

	/**
	 * @param id
	 */
	public SpacecraftDirectionMarker(long id)
	{
		super(id);
	}
}
