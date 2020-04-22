package edu.jhuapl.sbmt.stateHistory.ui.color;

import java.awt.Color;

/**
 * ColorProvider where there is no variation based on intensity. All values will
 * be mapped to the same color.
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public class ConstColorProvider implements ColorProvider
{
	// Attributes
	private final Color baseColor;

	/**
	 * Standard Constructor
	 *
	 * @param aColor The color will be utilized as the constant color.
	 */
	public ConstColorProvider(Color aColor)
	{
		baseColor = aColor;
	}

	@Override
	public Color getBaseColor()
	{
		return baseColor;
	}

	@Override
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal)
	{
		return baseColor;
	}

	@Override
	public StateHistoryFeatureType getFeatureType()
	{
		return null;
	}

}
