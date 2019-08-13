package edu.jhuapl.sbmt.gui.lidar.color;

import java.awt.event.ActionListener;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import edu.jhuapl.sbmt.model.lidar.LidarManager;
import edu.jhuapl.sbmt.model.lidar.feature.FeatureAttr;
import edu.jhuapl.sbmt.model.lidar.feature.FeatureType;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Custom {@link ColorBarPanel} that adds support for lidar color configuration.
 *
 * @author lopeznr1
 */
public class LidarColorBarPanel<G1> extends ColorBarPanel<FeatureType>
		implements ItemEventListener, LidarColorConfigPanel
{
	// Ref vars
	private final LidarManager<G1> refManager;

	/**
	 * Standard Constructor
	 */
	public LidarColorBarPanel(ActionListener aListener, LidarManager<G1> aManager)
	{
		addFeatureType(FeatureType.Intensity, "Intensity");
		addFeatureType(FeatureType.Range, "Spacecraft Range");
		addFeatureType(FeatureType.Time, "Time");
		setFeatureType(FeatureType.Range);

		refManager = aManager;

		// Auto register the provided ActionListener
		addActionListener(aListener);

		// Register for events of interest
		refManager.addListener(this);
	}

	@Override
	public void activate()
	{
		// Ensure our default range is in sync
		updateDefaultRange();

		// Force an update to the color map
		updateColorMapArea();

		// Reset the current range to the defaults
		double tmpMin = getDefaultMinValue();
		double tmpMax = getDefaultMaxValue();
		setCurrentMinMax(tmpMin, tmpMax);
	}

	@Override
	public GroupColorProvider getSourceGroupColorProvider()
	{
		ColorProvider tmpCP = new ColorBarColorProvider(getColormap(), getFeatureType(), getCurrentMinValue(),
				getCurrentMaxValue());
		return new ConstGroupColorProvider(tmpCP);
	}

	@Override
	public GroupColorProvider getTargetGroupColorProvider()
	{
		ColorProvider tmpCP = new ColorBarColorProvider(getColormap(), getFeatureType(), getCurrentMinValue(),
				getCurrentMaxValue());
		return new ConstGroupColorProvider(tmpCP);
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		// Update our default range
		if (aEventType == ItemEventType.ItemsChanged || aEventType == ItemEventType.ItemsMutated)
			updateDefaultRange();
	}

	@Override
	protected void updateDefaultRange()
	{
		// Bail if we are not visible. Maintenance of default range
		// synchronization is relevant only when the panel is visible.
		if (isShowing() == false)
			return;

		FeatureType tmpFT = getFeatureType();
		Range<Double> fullRange = null;
		for (G1 aItem : refManager.getAllItems())
		{
			// Skip to next if the lidar object is not rendered
			if (refManager.getIsVisible(aItem) == false)
				continue;

			fullRange = updateRange(aItem, tmpFT, fullRange);
		}

		// Update our (internal) default range
		double minVal = Double.NaN;
		double maxVal = Double.NaN;
		if (fullRange != null)
		{
			minVal = fullRange.lowerEndpoint();
			maxVal = fullRange.upperEndpoint();
		}

		setDefaultRange(minVal, maxVal);
	}

	/**
	 * Helper method that will update the fullRangeZ state var to include the
	 * specified lidar data.
	 */
	private Range<Double> updateRange(G1 aItem, FeatureType aFeatureType, Range<Double> aFullRange)
	{
		// Bail if there are no values associated with the feature
		FeatureAttr tmpFA = refManager.getFeatureAttrFor(aItem, aFeatureType);
		if (tmpFA == null || tmpFA.getNumVals() == 0)
			return aFullRange;

		Range<Double> tmpRangeZ = Ranges.closed(tmpFA.getMinVal(), tmpFA.getMaxVal());

		// Grow fullRangeZ to include the specified lidar data
		if (aFullRange == null)
			aFullRange = tmpRangeZ;
		aFullRange = aFullRange.span(tmpRangeZ);
		return aFullRange;
	}

}
