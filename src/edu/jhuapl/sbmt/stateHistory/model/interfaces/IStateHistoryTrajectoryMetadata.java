package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.awt.Color;

public interface IStateHistoryTrajectoryMetadata
{
    /**
     * Returns the trajectory object for this state history
     * @return
     */
    public Trajectory getTrajectory();

    /**
     * Sets the trajectory for this StateHistory to <pre>traj</pre>
     * @param traj
     */
    public void setTrajectory(Trajectory traj);


	public void setTrajectoryColor(Color color);
}
