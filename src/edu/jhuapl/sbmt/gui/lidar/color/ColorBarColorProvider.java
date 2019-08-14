package edu.jhuapl.sbmt.gui.lidar.color;

import java.awt.Color;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.sbmt.model.lidar.feature.FeatureType;

/**
 * ColorProvider where the returned color will be a function of the specified
 * value and the reference Colormap.
 *
 * @author lopeznr1
 */
public class ColorBarColorProvider implements ColorProvider
{
	// Attributes
	private final Colormap refColorMap;
	private final FeatureType refFeatureType;
	private final double refMinVal;
	private final double refMaxVal;

	/**
	 * Standard Constructor
	 *
	 * @param aColor The color that will be used as the baseline color. All
	 * returned
	 */
	public ColorBarColorProvider(Colormap aColormap, FeatureType aFeatureType, double aMinVal, double aMaxVal)
	{
		refColorMap = aColormap;
		refFeatureType = aFeatureType;
		refMinVal = aMinVal;
		refMaxVal = aMaxVal;

		refColorMap.setRangeMin(0.0);
		refColorMap.setRangeMax(1.0);
	}

	@Override
	public Color getBaseColor()
	{
		// Color bars have no base line color
		return null;
	}

	@Override
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal)
	{
		// Determine if we should just use the NaN color
		boolean isNaNColor = false;
		isNaNColor |= Double.isNaN(refMinVal) == true;
		isNaNColor |= Double.isNaN(refMaxVal) == true;
		isNaNColor |= refMinVal == refMaxVal;
		if (isNaNColor == true)
			return refColorMap.getNanColor();

		// Rescale aTargVal to the range: [0.0, 1.0]
		// Note this may not work to well if the scale is log based
		double tmpVal = (aTargVal - refMinVal) / (refMaxVal - refMinVal);
		return refColorMap.getColor(tmpVal);
	}

	@Override
	public FeatureType getFeatureType()
	{
		return refFeatureType;
	}

}
