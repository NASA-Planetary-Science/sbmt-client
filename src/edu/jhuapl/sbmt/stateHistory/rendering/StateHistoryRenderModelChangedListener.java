package edu.jhuapl.sbmt.stateHistory.rendering;

/**
 * @author steelrj1
 *
 */
public interface StateHistoryRenderModelChangedListener
{
	/**
	 * @param t
	 */
	public void timeChanged(Double t);

	/**
	 * @param showSpacecraft
	 */
	public void showSpacecraftChanged(boolean showSpacecraft);

	/**
	 * @param showSpacecraftDirection
	 */
	public void showSpacecraftDirectionChanged(boolean showSpacecraftDirection);

	/**
	 * @param showSun
	 */
	public void showSunChanged(boolean showSun);

	/**
	 * @param showEarth
	 */
	public void showEarthChanged(boolean showEarth);

	/**
	 * @param showScalarBar
	 */
	public void showScalarBarChanged(boolean showScalarBar);

	/**
	 * @param showTrajectory
	 */
	public void showTrajectoryChanged(boolean showTrajectory);

	/**
	 * @param color
	 */
	public void trajectoryColorChanged(double[] color);

	/**
	 * @param thickness
	 */
	public void trajectoryThicknessChanged(Double thickness);

	/**
	 * @param distanceText
	 */
	public void distanceTextChanged(String distanceText);

	/**
	 * @param showLighting
	 */
	public void showLightingChanged(boolean showLighting);

	/**
	 * @param showTimeBar
	 */
	public void showTimeBarChanged(boolean showTimeBar);

}
