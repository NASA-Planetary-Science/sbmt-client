package edu.jhuapl.sbmt.core.config;

import java.util.List;
import java.util.Map;

import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.core.client.Mission;

public interface ISmallBodyViewConfig
{

	public String serverPath(String fileName);

	public String serverPath(String fileName, Instrument instrument);

	boolean isAccessible();

	static boolean isBeta()
	{
		return false;
	}

	public String[] getShapeModelFileNames();

	public boolean hasColoringData();

	public String getTimeHistoryFile();

	public String getShapeModelName();

	public ShapeModelType getAuthor();

	public String getRootDirOnServer();

    public boolean isCustomTemporary();

    public String getUniqueName();

    public Mission[] getPresentInMissions();

	public Mission[] getDefaultForMissions();

	public Map<Class<?>, List<IFeatureConfig>> getFeatureConfigs();
}