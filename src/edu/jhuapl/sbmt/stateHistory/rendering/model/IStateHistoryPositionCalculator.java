package edu.jhuapl.sbmt.stateHistory.rendering.model;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.rendering.SpacecraftBody;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.EarthDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SpacecraftDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.directionMarkers.SunDirectionMarker;
import edu.jhuapl.sbmt.stateHistory.rendering.text.SpacecraftLabel;

public interface IStateHistoryPositionCalculator
{
	/**
	 * @param history
	 * @param time
	 */
	public void updateSunPosition(StateHistory history, double time, SunDirectionMarker sunDirectionMarker);

	/**
	 * @param history
	 * @param time
	 */
	public void updateEarthPosition(StateHistory history, double time, EarthDirectionMarker earthDirectionMarker);

	/**
	 * @param history
	 * @param time
	 */
	public void updateSpacecraftPosition(StateHistory history, double time, SpacecraftBody spacecraft, SpacecraftDirectionMarker scDirectionMarker,
			SpacecraftLabel spacecraftLabelActor);

	/**
	 * @param lookDirection
	 * @param scalingFactor
	 * @return
	 */
	public double[] updateLookDirection(RendererLookDirection lookDirection, double scalingFactor);

	/**
	 * @return
	 */
	public double[] getCurrentLookFromDirection();
}
