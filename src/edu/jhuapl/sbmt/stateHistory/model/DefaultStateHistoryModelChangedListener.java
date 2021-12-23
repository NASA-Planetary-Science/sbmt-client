package edu.jhuapl.sbmt.stateHistory.model;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryModelChangedListener;

/**
 * @author steelrj1
 *
 */
public class DefaultStateHistoryModelChangedListener implements StateHistoryModelChangedListener
{

	/**
	 *
	 */
	public DefaultStateHistoryModelChangedListener()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void trajectoryColorChanged(double[] color)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void trajectoryThicknessChanged(Double thickness)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void distanceTextChanged(String distanceText)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void historySegmentCreated(StateHistory historySegment)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void historySegmentRemoved(StateHistory historySegment)
	{
		// TODO Auto-generated method stub

	}
}
