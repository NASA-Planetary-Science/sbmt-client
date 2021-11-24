package edu.jhuapl.sbmt.image2.model;

public enum ImageOrigin {
	SERVER("Server"),
	LOCAL("Local"),
	COMPOSITE("Composite");

	private String name;

	private ImageOrigin(String name)
	{
		this.name = name;
	}

	public String getFullName()
	{
		return name;
	}
}