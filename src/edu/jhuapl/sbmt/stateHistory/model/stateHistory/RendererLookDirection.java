package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

public enum RendererLookDirection
{
	/**
	 *
	 */
	FREE_VIEW("Free View"),
	/**
	 *
	 */
	SUN("Sun View"),
	/**
	 *
	 */
	EARTH("Earth View"),
	/**
	 *
	 */
	SPACECRAFT("Spacecraft View"),
	/**
	 *
	 */
	SPACECRAFT_THIRD("Third Person View");


	/**
	 *
	 */
	private String name;

	/**
	 * @param name
	 */
	private RendererLookDirection(String name)
	{
		this.name = name;
	}

	/**
	 *
	 */
	public String toString() { return name; }
}
