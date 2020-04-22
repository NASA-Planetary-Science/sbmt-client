package edu.jhuapl.sbmt.stateHistory.ui.color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import edu.jhuapl.saavtk.colormap.Colorbar;
import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.model.lidar.feature.FeatureAttr;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Custom {@link ColorBarPanel} that adds support for lidar color configuration.
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public class StateHistoryColorBarPanel extends ColorBarPanel<StateHistoryFeatureType>
		implements ItemEventListener, StateHistoryColorConfigPanel
{
	// Ref vars
	private final StateHistoryCollection refManager;
	private final Renderer refRenderer;

	// State vars
	private Colorbar colorBar;
	private boolean isActive;

	/**
	 * Standard Constructor
	 */
	public StateHistoryColorBarPanel(ActionListener aListener, StateHistoryCollection aManager, Renderer aRenderer)
	{
		refManager = aManager;
		refRenderer = aRenderer;

		colorBar = null;
		isActive = false;

//		addFeatureType(StateHistoryFeatureType.Radius, "Radius");
		addFeatureType(StateHistoryFeatureType.Distance, "Spacecraft Distance");
		addFeatureType(StateHistoryFeatureType.Time, "Time");
		setFeatureType(StateHistoryFeatureType.Distance);

		// Auto register the provided ActionListener
		addActionListener(aListener);

		// Register for events of interest
		refManager.addListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		updateColorBar();

		super.actionPerformed(aEvent);
	}

	@Override
	public void activate(boolean aIsActive)
	{
		isActive = aIsActive;

		// Ensure our default range is in sync
		updateDefaultRange();

		// Reset the current range to the defaults
		double tmpMin = getDefaultMinValue();
		double tmpMax = getDefaultMaxValue();
		setCurrentMinMax(tmpMin, tmpMax);

		// Force an update to the color map
		updateColorMapArea();

		updateColorBar();
	}

	@Override
	public GroupColorProvider getSourceGroupColorProvider()
	{
		ColorProvider tmpCP = new ColorBarColorProvider(getColorMapAttr(), getFeatureType());
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

		StateHistoryFeatureType tmpFT = getFeatureType();
		Range<Double> fullRange = null;
		for (StateHistory aItem : refManager.getAllItems())
		{
			// Skip to next if the lidar object is not rendered
			if (refManager.getVisibility(aItem) == false)
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
	 * Helper method that keeps the color bar synchronized with the gui.
	 * <P>
	 * A {@link Colorbar} will be shown whenever this panel has been activated.
	 */
	private void updateColorBar()
	{
		// Show the SAAVTK Colorbar whenever the ColorBarPanel has been selected
		boolean isVisible = isActive;
		if (isVisible == true)
		{
			// Lazy init
			if (colorBar == null)
				colorBar = new Colorbar(refRenderer);

			ColorMapAttr tmpCMA = getColorMapAttr();
			Colormap tmpColorMap = Colormaps.getNewInstanceOfBuiltInColormap(tmpCMA.getName());
			tmpColorMap.setLogScale(tmpCMA.getIsLogScale());
			tmpColorMap.setNumberOfLevels(tmpCMA.getNumLevels());
			tmpColorMap.setRangeMin(tmpCMA.getMinVal());
			tmpColorMap.setRangeMax(tmpCMA.getMaxVal());
			colorBar.setColormap(tmpColorMap);

			StateHistoryFeatureType tmpFT = getFeatureType();
			String title = "" + tmpFT;
			if (tmpFT == StateHistoryFeatureType.Distance)
				title = "Spacecraft " + title;

			colorBar.setTitle(title);

			// Ensure the color bar is showing
			if (refRenderer.getRenderWindowPanel().getRenderer().HasViewProp(colorBar.getActor()) == 0)
				refRenderer.getRenderWindowPanel().getRenderer().AddActor(colorBar.getActor());
		}

		if (colorBar != null)
			colorBar.setVisible(isVisible);
	}

	/**
	 * Helper method that will update the fullRangeZ state var to include the
	 * specified lidar data.
	 */
	private Range<Double> updateRange(StateHistory aItem, StateHistoryFeatureType aFeatureType, Range<Double> aFullRange)
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
