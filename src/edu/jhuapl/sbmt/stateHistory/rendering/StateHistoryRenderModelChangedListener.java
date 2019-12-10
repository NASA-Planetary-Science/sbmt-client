package edu.jhuapl.sbmt.stateHistory.rendering;

public interface StateHistoryRenderModelChangedListener
{
	public void timeChanged(Double t);

	public void showSpacecraftChanged(boolean showSpacecraft);

	public void showSpacecraftDirectionChanged(boolean showSpacecraftDirection);

	public void showSunChanged(boolean showSun);

	public void showEarthChanged(boolean showEarth);

	public void showScalarBarChanged(boolean showScalarBar);

	public void showTrajectoryChanged(boolean showTrajectory);

	public void trajectoryColorChanged(double[] color);

	public void trajectoryThicknessChanged(Double thickness);

	public void distanceTextChanged(String distanceText);

	public void showLightingChanged(boolean showLighting);

	public void showTimeBarChanged(boolean showTimeBar);

}
