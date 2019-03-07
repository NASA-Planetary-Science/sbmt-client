package edu.jhuapl.sbmt.client;

public interface ISmallBodyModel
{

	SmallBodyViewConfig getSmallBodyConfig();

	String serverPath(String fileName);

}