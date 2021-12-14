package edu.jhuapl.sbmt.stateHistory.ui.state.color;

import java.awt.event.ActionListener;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.gui.ColorBarPanel;
import edu.jhuapl.saavtk.color.gui.EditGroupColorPanel;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.provider.ColorBarColorProvider;
import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.feature.FeatureAttr;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Custom {@link ColorBarPanel} that adds support for state history color configuration.
 *
 * Originally made for Lidar by lopeznr1, customized for State History
 *
 * @author steelrj1
 */
public class StateHistoryColorBarPanel extends ColorBarPanel
		implements ItemEventListener, EditGroupColorPanel
{
	// Ref vars
	private final Renderer refRenderer;

	// State vars
	private ColorBarPainter colorBar;

	private StateHistoryRendererManager rendererManager;

	/**
	 * Standard Constructor
	 */
	public StateHistoryColorBarPanel(ActionListener aListener, StateHistoryRendererManager rendererManager,
			ColorBarPainter colorBarPainter)
	{
		super(colorBarPainter, true);
		this.rendererManager = rendererManager;
		refRenderer = rendererManager.getRenderer();

		colorBar = colorBarPainter;

		addFeatureType(StateHistoryFeatureType.Distance, "S/C Distance to Center (km)");
		addFeatureType(StateHistoryFeatureType.Time, "Time (Sec)");

		setFeatureType(StateHistoryFeatureType.Distance);
		// Auto register the provided ActionListener
		addActionListener(aListener);

		// Register for events of interest
		addActionListener((aEvent) -> updateColorBar());
//		rendererManager.addListener(this);

		if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
	}

	@Override
	public void activate(boolean aIsActive)
	{
		if (rendererManager.getHistoryCollection().getCurrentRun() != null && rendererManager.getHistoryCollection().getCurrentRun().getMetadata().getType() == StateHistorySourceType.SPICE)
		{
			addFeatureType(StateHistoryFeatureType.Range, "S/C Range to Surface (km)");
			addFeatureType(StateHistoryFeatureType.SubSCIncidence, "S/C Incidence Angle (deg)");
			addFeatureType(StateHistoryFeatureType.SubSCEmission, "S/C Emission Angle (deg)");
			addFeatureType(StateHistoryFeatureType.SubSCPhase, "S/C Phase Angle (deg)");
		}

		// Ensure our default range is in sync
		updateDefaultRange();

		// Force install the ColorMapAttr with the default (reset) range
		ColorMapAttr tmpCMA = getColorMapAttr();

		double minVal = Double.NaN;
		double maxVal = Double.NaN;
		FeatureType tmpFT = getFeatureType();
		Range<Double> tmpRange = getResetRange(tmpFT);
		if (tmpRange != null)
		{
			minVal = tmpRange.lowerEndpoint();
			maxVal = tmpRange.upperEndpoint();
		}

		tmpCMA = new ColorMapAttr(tmpCMA.getColorTable(), minVal, maxVal, tmpCMA.getNumLevels(), tmpCMA.getIsLogScale());
		setColorMapAttr(tmpCMA);

		// Update the color bar
		updateColorBar();

		// Update the renderer to reflect the ColorBarPainter
		if (aIsActive == true)
			refRenderer.addVtkPropProvider(colorBar);
		else
			refRenderer.delVtkPropProvider(colorBar);
	}

	@Override
	public GroupColorProvider getGroupColorProvider()
	{
		ColorProvider tmpCP = new ColorBarColorProvider(getColorMapAttr(), getFeatureType());
		return new ConstGroupColorProvider(tmpCP);
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		// Update our default range
		if (aEventType == ItemEventType.ItemsChanged || aEventType == ItemEventType.ItemsMutated)
		{
			updateDefaultRange();
			FeatureType tmpFT = getFeatureType();
			double minVal = Double.NaN;
			double maxVal = Double.NaN;
			Range<Double> tmpRange = getResetRange(tmpFT);
			if (tmpRange != null)
			{
				minVal = tmpRange.lowerEndpoint();
				maxVal = tmpRange.upperEndpoint();
			}
			ColorMapAttr tmpCMA = new ColorMapAttr(getColorMapAttr().getColorTable(), minVal, maxVal, getColorMapAttr().getNumLevels(), getColorMapAttr().getIsLogScale());
			colorBar.setColorMapAttr(tmpCMA);
			setColorMapAttr(tmpCMA);
		}
	}

	/**
	 * Helper method to calculate the range of values for the specified
	 * {@link FeatureType}.
	 */
	private Range<Double> calcRangeForFeature(FeatureType aFeatureType)
	{
		Range<Double> fullRange = null;
		for (StateHistory aItem : rendererManager.getAllItems())
		{
			if (!aItem.getMetadata().isVisible())
				continue;

			fullRange = updateRange(aItem, aFeatureType, fullRange);
		}
		return fullRange;
	}

	/**
	 * Helper method that updates the default range for all of the lidar
	 * {@link FeatureType}s.
	 */
	private void updateDefaultRange()
	{
		// Bail if we are not visible. Maintenance of default range
		// synchronization is relevant only when the panel is visible.
//		if (isShowing() == false)
//			return;
		for (FeatureType aFeatureType : StateHistoryFeatureType.FullSet)
		{
			Range<Double> tmpRange = calcRangeForFeature(aFeatureType);
			setResetRange(aFeatureType, tmpRange);
		}
	}

	/**
	 * Helper method that keeps the {@link ColorBarPainter} synchronized with the
	 * gui.
	 */
	private void updateColorBar()
	{
		ColorMapAttr tmpCMA = getColorMapAttr();
		colorBar.setColorMapAttr(tmpCMA);

		FeatureType tmpFT = getFeatureType();
		colorBar.setTitle(tmpFT.getName());
	}

	/**
	 * Helper method that will update the fullRangeZ state var to include the
	 * specified lidar data.
	 */
	private Range<Double> updateRange(StateHistory aItem, FeatureType aFeatureType, Range<Double> aFullRange)
	{
		// Bail if there are no values associated with the feature
		FeatureAttr tmpFA = StateHistoryRendererManager.getFeatureAttrFor(aItem.getTrajectoryMetadata().getTrajectory(), aFeatureType);
		if (tmpFA == null || tmpFA.getNumVals() == 0)
			return aFullRange;

		Range<Double> tmpRangeZ = Range.closed(tmpFA.getMinVal(), tmpFA.getMaxVal());

		// Grow fullRangeZ to include the specified lidar data
		if (aFullRange == null)
			aFullRange = tmpRangeZ;
		aFullRange = aFullRange.span(tmpRangeZ);
		return aFullRange;
	}

}
