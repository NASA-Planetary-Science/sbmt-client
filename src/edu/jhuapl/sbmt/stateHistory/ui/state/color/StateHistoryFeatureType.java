package edu.jhuapl.sbmt.stateHistory.ui.state.color;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.feature.FeatureType;

/**
 * @author steelrj1
 *
 */
public class StateHistoryFeatureType
{
	// Constants
	public static final FeatureType Time = new FeatureType("Time (sec)", null, 1.0);

//	public static final FeatureType Radius = new FeatureType("Radius", null, 1.0);

	public static final FeatureType Distance = new FeatureType("Spacecraft Distance (km)", null, 1.0);

	public static final FeatureType Range = new FeatureType("Spacecraft Range to Surface (km)", null, 1.0);

	public static final FeatureType SubSCEmission = new FeatureType("Sub-Spacecraft emission angle (deg)", null, 1.0);

	public static final FeatureType SubSCIncidence = new FeatureType("Sub-Spacecraft incidence angle (deg)", null, 1.0);

	public static final FeatureType SubSCPhase = new FeatureType("Sub-Spacecraft phase angle (deg)", null, 1.0);

	/** Provides access to all of the available lidar {@link FeatureType}s. */
	public static final ImmutableSet<FeatureType> FullSet = ImmutableSet.of(Time, Distance, Range, SubSCEmission, SubSCIncidence, SubSCPhase);
}
