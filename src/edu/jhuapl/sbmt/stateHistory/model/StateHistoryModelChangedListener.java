package edu.jhuapl.sbmt.stateHistory.model;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

public interface StateHistoryModelChangedListener
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

	public void trajectoryCreated(Trajectory trajectory);

	public void showLightingChanged(boolean showLighting);

	public void showTimeBarChanged(boolean showTimeBar);

}
