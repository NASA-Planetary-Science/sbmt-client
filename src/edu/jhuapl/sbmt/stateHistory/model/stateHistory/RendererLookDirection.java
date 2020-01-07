package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

public enum RendererLookDirection
{
	SUN("Sun View"),
	EARTH("Earth View"),
	SPACECRAFT("Spacecraft View"),
	SPACECRAFT_THIRD("Third Person View"),
	FREE_VIEW("Free View");

	private String name;

	private RendererLookDirection(String name)
	{
		this.name = name;
	}

	public String toString() { return name; }
}
