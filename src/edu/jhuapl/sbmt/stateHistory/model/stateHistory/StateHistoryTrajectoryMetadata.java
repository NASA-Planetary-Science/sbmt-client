package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.awt.Color;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

public class StateHistoryTrajectoryMetadata implements IStateHistoryTrajectoryMetadata
{
	/**
	 *
	 */
	private Trajectory trajectory;

	public StateHistoryTrajectoryMetadata()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public Trajectory getTrajectory()
	{
		return trajectory;
	}

	@Override
	public void setTrajectory(Trajectory traj)
	{
		this.trajectory = traj;
	}

	@Override
	public void setTrajectoryColor(Color color)
	{
		this.trajectory.setColor(color);
	}

}
