package edu.jhuapl.sbmt.client;

import java.util.Map;

import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.Instrument;

public interface ISmallBodyViewConfig
{

	public String serverPath(String fileName);

	public String serverPath(String fileName, Instrument instrument);

	boolean isAccessible();

	static boolean isBeta()
	{
		return false;
	}

	public Map<String, String> getSpectraSearchDataSourceMap();

	public Instrument getLidarInstrument();

	public boolean hasHypertreeLidarSearch();

	public SpectraHierarchicalSearchSpecification getHierarchicalSpectraSearchSpecification();

}