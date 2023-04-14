package edu.jhuapl.sbmt.common.client;

import java.util.Date;
import java.util.Map;

import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.config.Instrument;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;

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

	public String[] getShapeModelFileNames();

	public boolean hasColoringData();

	public boolean hasHypertreeBasedSpectraSearch();

	public boolean hasHierarchicalSpectraSearch();


	public Date getDefaultImageSearchStartDate();
	public Date getDefaultImageSearchEndDate();
	public String[] getImageSearchFilterNames();
	public String[] getImageSearchUserDefinedCheckBoxesNames();
	public boolean hasHierarchicalImageSearch();
	public HierarchicalSearchSpecification getHierarchicalImageSearchSpecification();
	public String getTimeHistoryFile();
	public SpiceInfo getSpiceInfo();
	public Date getStateHistoryStartDate();
	public Date getStateHistoryEndDate();
	public String getShapeModelName();

	public ShapeModelType getAuthor();

	public String getRootDirOnServer();

    public boolean isCustomTemporary();

    public String getUniqueName();

    public Mission[] getPresentInMissions();

	public Mission[] getDefaultForMissions();

	public ImagingInstrument[] getImagingInstruments();

}