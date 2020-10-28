/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.model;

/**
 * @author steelrj1
 *
 */
public enum StateHistorySourceType
{
	PREGEN("Pregenerated Pointing"),
	SPICE("SPICE Pointing");

	private String name;

	private StateHistorySourceType(String name)
	{
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
}
