package edu.jhuapl.sbmt.gui.lidar.v2;

import java.util.Date;

import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

public class LidarSearchModel
{
	// Ref vars
	private final LidarSearchDataCollection refLidarModel;
	private final BodyViewConfig refSmallBodyConfig;

	// State vars
	private Date startDate = null;
	private Date endDate = null;

	public LidarSearchModel(BodyViewConfig aSmallBodyConfig, LidarSearchDataCollection aModel)
	{
		refSmallBodyConfig = aSmallBodyConfig;
		refLidarModel = aModel;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public void setStartDate(Date aStartDate)
	{
		startDate = aStartDate;
	}

	public void setEndDate(Date aEndDate)
	{
		endDate = aEndDate;
	}

	public BodyViewConfig getSmallBodyConfig()
	{
		return refSmallBodyConfig;
	}

	public LidarSearchDataCollection getLidarModel()
	{
		return refLidarModel;
	}
}
