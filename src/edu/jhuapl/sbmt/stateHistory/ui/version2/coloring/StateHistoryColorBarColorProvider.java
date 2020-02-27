package edu.jhuapl.sbmt.stateHistory.ui.version2.coloring;

import java.awt.Color;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.sbmt.gui.lidar.color.ColorMapAttr;

public class StateHistoryColorBarColorProvider implements StateHistoryColorProvider
{
	// Attributes
	private final ColorMapAttr refColorMapAttr;
	private final StateHistoryColoringFeatureType refFeatureType;

	// State vars
	private final Colormap colorMap;

	/**
	 * Standard Constructor
	 *
	 * @param aColor The color that will be used as the baseline color. All
	 * returned
	 */
	public StateHistoryColorBarColorProvider(ColorMapAttr aColorMapAttr, StateHistoryColoringFeatureType aFeatureType)
	{
		refColorMapAttr = aColorMapAttr;
		refFeatureType = aFeatureType;

		colorMap = Colormaps.getNewInstanceOfBuiltInColormap(refColorMapAttr.getName());
		colorMap.setNumberOfLevels(aColorMapAttr.getNumLevels());
		colorMap.setLogScale(aColorMapAttr.getIsLogScale());
		colorMap.setRangeMin(0.0);
		colorMap.setRangeMax(1.0);
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
		double minVal = refColorMapAttr.getMinVal();
		double maxVal = refColorMapAttr.getMaxVal();

		// Determine if we should just use the NaN color
		boolean isNaNColor = false;
		isNaNColor |= Double.isNaN(minVal) == true;
		isNaNColor |= Double.isNaN(maxVal) == true;
		isNaNColor |= minVal == maxVal;
		if (isNaNColor == true)
			return colorMap.getNanColor();

		// Rescale aTargVal to the range: [0.0, 1.0]
		// Note this may not work to well if the scale is log based
		double tmpVal = (aTargVal - minVal) / (maxVal - minVal);
		return colorMap.getColor(tmpVal);
	}

	@Override
	public StateHistoryColoringFeatureType getFeatureType()
	{
		return refFeatureType;
	}

}
