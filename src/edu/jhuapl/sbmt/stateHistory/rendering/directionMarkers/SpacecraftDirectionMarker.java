package edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers;

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
		super(markerRadius, markerHeight, centerX, centerY, centerZ);
		specularColorValue = 0.1;
	}

	/**
	 * @param id
	 */
	public SpacecraftDirectionMarker(long id)
	{
		super(id);
	}
}
