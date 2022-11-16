package edu.jhuapl.sbmt.stateHistory.deprecated;
//package edu.jhuapl.sbmt.stateHistory.model;
//
//import java.util.function.BiFunction;
//
//import edu.jhuapl.sbmt.pointing.InstrumentPointing;
//import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
//
//import crucible.core.math.vectorspace.UnwritableVectorIJK;
//
///**
// * @author steelrj1
// *
// */
//public enum StateHistoryColoringFunctions
//{
//	/**
//	 *
//	 */
//	PER_TABLE("Per Table", null),
//
//	/**
//	 *
//	 */
//	DISTANCE("Distance", (traj, time) -> {
//
////    	int index = traj.getTime().lastIndexOf(time);
//		InstrumentPointing pointing = traj.getPointingProvider().provide(time);
//		UnwritableVectorIJK scPosition = pointing.getScPosition();
//		return scPosition.getLength();
////    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
////    	return distance;
//    })/*,
//	TIME("Time", (traj, time) -> {
//
//    	int index = traj.getTime().lastIndexOf(time);
//    	return (double)index;
//    }),
//	SUB_SC_EMISSION("Sub SC Emission", (traj, time) -> {
//
//    	int index = traj.getTime().lastIndexOf(time);
//    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
//    	return distance;
//    }),
//	SUB_SC_INCIDENCE("Sub SC Incidence", (traj, time) -> {
//
//    	int index = traj.getTime().lastIndexOf(time);
//    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
//    	return distance;
//    }),
//	SUB_SC_PHASE("Sub SC Phase", (traj, time) -> {
//
//    	int index = traj.getTime().lastIndexOf(time);
//    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
//    	return distance;
//    }),*//*,
//	SUB_SC_RANGE("Range", (traj, time) -> {
//
//    	int index = traj.getTime().lastIndexOf(time);
//    	double distance = Math.sqrt(Math.pow(traj.getX().get(index), 2) + Math.pow(traj.getY().get(index), 2) + Math.pow(traj.getZ().get(index), 2));
//    	return distance;
//    })*//**
//     *
//     */
//    ;
//
//	private String name;
//	/**
//	 *
//	 */
//	BiFunction<Trajectory, Double, Double> coloringFunction;
//
//
//	/**
//	 * @param name
//	 * @param coloringFunction
//	 */
//	private StateHistoryColoringFunctions(String name, BiFunction<Trajectory, Double, Double> coloringFunction)
//	{
//		this.name = name;
//		this.coloringFunction = coloringFunction;
//	}
//
//	@Override
//	public String toString()
//	{
//		return name;
//	}
//
//	/**
//	 * @return
//	 */
//	public BiFunction<Trajectory, Double, Double> getColoringFunction()
//	{
//		return coloringFunction;
//	}
//}
