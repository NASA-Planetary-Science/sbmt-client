package edu.jhuapl.sbmt.client;

import java.util.Date;
import java.util.Map;

import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification;

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
	public String getShapeModelName();

	public ShapeModelType getAuthor();

	public String getRootDirOnServer();

    public boolean isCustomTemporary();

}