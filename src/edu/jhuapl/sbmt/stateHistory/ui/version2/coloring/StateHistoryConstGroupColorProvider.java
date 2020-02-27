package edu.jhuapl.sbmt.stateHistory.ui.version2.coloring;

/**
 * Immutable class that provides a GroupColorProvider which always returns the
 * same ColorProvider for all items.
 *
 * @author lopeznr1
 */
public class StateHistoryConstGroupColorProvider implements StateHistoryGroupColorProvider
{
	// Attributes
	private final StateHistoryColorProvider refColorProvider;

	/**
	 * Standard Constructor
	 */
	public StateHistoryConstGroupColorProvider(StateHistoryColorProvider aColorProvider)
	{
		refColorProvider = aColorProvider;
	}

	@Override
	public StateHistoryColorProvider getColorProviderFor(Object aItem, int aIdx, int aMaxCnt)
	{
		return refColorProvider;
	}

}
