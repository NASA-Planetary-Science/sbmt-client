package edu.jhuapl.sbmt.stateHistory.model;

import java.util.function.BiFunction;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

public enum StateHistoryColoringFunctions
{
	PER_TABLE("Per Table", null),
	DISTANCE("Distance", (traj, time) -> {

    	int index = traj.getTime().lastIndexOf(time);
    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
    	return distance;
    })/*,
	TIME("Time", (traj, time) -> {

    	int index = traj.getTime().lastIndexOf(time);
    	return (double)index;
    }),
	SUB_SC_EMISSION("Sub SC Emission", (traj, time) -> {

    	int index = traj.getTime().lastIndexOf(time);
    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
    	return distance;
    }),
	SUB_SC_INCIDENCE("Sub SC Incidence", (traj, time) -> {

    	int index = traj.getTime().lastIndexOf(time);
    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
    	return distance;
    }),
	SUB_SC_PHASE("Sub SC Phase", (traj, time) -> {

    	int index = traj.getTime().lastIndexOf(time);
    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
    	return distance;
    }),*//*,
	SUB_SC_RANGE("Range", (traj, time) -> {

    	int index = traj.getTime().lastIndexOf(time);
    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
    	return distance;
    })*/;

	private String name;
	BiFunction<Trajectory, Double, Double> coloringFunction;

	private StateHistoryColoringFunctions(String name, BiFunction<Trajectory, Double, Double> coloringFunction)
	{
		this.name = name;
		this.coloringFunction = coloringFunction;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public BiFunction<Trajectory, Double, Double> getColoringFunction()
	{
		return coloringFunction;
	}
}
