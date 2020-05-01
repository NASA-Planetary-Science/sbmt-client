package edu.jhuapl.sbmt.stateHistory.rendering.model;

import java.awt.Color;

import edu.jhuapl.sbmt.stateHistory.ui.color.ColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.color.ConstColorProvider;



/**
 * Object that contains the mutable renderable properties associated with a
 * state history.
 * <P>
 * Since this class is NOT to be exposed outside of this package there are no
 * publicly accessible methods and all access is package private.
 *
 * @author steelrj1
 */
class StateHistoryRenderProperties
{
	// Associated properties
	/** Defines if the lidar data should be visible. */
	boolean isVisible;
	/** Defines whether the installed ColorProvider is a custom ColorProvider */
	boolean isCustomCP;
	/** Defines the ColorProvider associated with a source points. */
	ColorProvider srcCP;

	/**
	 * Standard Constructor
	 */
	StateHistoryRenderProperties()
	{
		isVisible = true;
		isCustomCP = false;
		srcCP = new ConstColorProvider(Color.GREEN);
	}
}
