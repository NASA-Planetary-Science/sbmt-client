package edu.jhuapl.sbmt.stateHistory.ui.state.color;

/**
 * Interface that defines the methods to allow configuration of ColorProviders
 * (used to render lidar data).
 *
 * Originally made for Lidar by lopeznr1
 *
 * @author steelrj1
 */
public interface StateHistoryColorConfigPanel
{
	/**
	 * Notifies the panel of its active state.
	 */
	public abstract void activate(boolean aIsActive);

	/**
	 * Returns the GroupColorProvider that should be used to color lidar data
	 * points associated with the source (spacecraft).
	 */
	public abstract GroupColorProvider getSourceGroupColorProvider();
}
