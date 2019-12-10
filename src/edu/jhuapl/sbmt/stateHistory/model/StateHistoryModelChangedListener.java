package edu.jhuapl.sbmt.stateHistory.model;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;

public interface StateHistoryModelChangedListener
{
	public void timeChanged(Double t);

	public void trajectoryColorChanged(double[] color);

	public void trajectoryThicknessChanged(Double thickness);

	public void distanceTextChanged(String distanceText);

	public void historySegmentCreated(StateHistory trajectory);

}
