package edu.jhuapl.sbmt.stateHistory.ui.version2.coloring;

public interface StateHistoryColorConfigPanel
{
	/**
	 * Notifies the panel of its active state.
	 */
	public abstract void activate(boolean aIsActive);

	/**
	 * Returns the GroupColorProvider that should be used to color state history data
	 */
	public abstract StateHistoryGroupColorProvider getStateHistoryGroupColorProvider();
}
