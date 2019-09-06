package edu.jhuapl.sbmt.gui.lidar.color;

/**
 * Interface that defines the methods to allow configuration of ColorProviders
 * (used to render lidar data).
 *
 * @author lopeznr1
 */
public interface LidarColorConfigPanel
{
	/**
	 * Notifies the panel that it is active.
	 */
	public abstract void activate();

	/**
	 * Returns the GroupColorProvider that should be used to color lidar data
	 * points associated with the source (spacecraft).
	 */
	public abstract GroupColorProvider getSourceGroupColorProvider();

	/**
	 * Returns the GroupColorProvider that should be used to color lidar data
	 * points associated with the target.
	 */
	public abstract GroupColorProvider getTargetGroupColorProvider();

}
