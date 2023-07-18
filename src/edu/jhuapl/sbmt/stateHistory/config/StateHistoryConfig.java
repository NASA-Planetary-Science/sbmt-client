package edu.jhuapl.sbmt.stateHistory.config;

import java.util.Date;

import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.IFeatureConfig;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;

public class StateHistoryConfig implements IFeatureConfig
{
	public boolean hasStateHistory; // for bodies with state history tabs
	public SpiceInfo spiceInfo;
	public Date stateHistoryStartDate, stateHistoryEndDate;
	public String timeHistoryFile;
	
	private BodyViewConfig config;
    
    public StateHistoryConfig(BodyViewConfig config)
	{
		this.config = config;
	}
	
	public void setConfig(BodyViewConfig config)
	{
		this.config = config;
	}
	
	public boolean isHasStateHistory() 
	{
		return hasStateHistory;
	}
	
	public SpiceInfo getSpiceInfo() 
	{
		return spiceInfo;
	}
	
//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		StateHistoryConfig  c = (StateHistoryConfig ) super.clone();
//		
//		
//		
//		
//		return c;
//	}

	public Date getStateHistoryStartDate()
	{
		return stateHistoryStartDate;
	}

	public Date getStateHistoryEndDate()
	{
		return stateHistoryEndDate;
	}

	public String getTimeHistoryFile()
	{
		return timeHistoryFile;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasStateHistory ? 1231 : 1237);
		result = prime * result + ((spiceInfo == null) ? 0 : spiceInfo.hashCode());
		result = prime * result + ((stateHistoryEndDate == null) ? 0 : stateHistoryEndDate.hashCode());
		result = prime * result + ((stateHistoryStartDate == null) ? 0 : stateHistoryStartDate.hashCode());
		result = prime * result + ((timeHistoryFile == null) ? 0 : timeHistoryFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateHistoryConfig other = (StateHistoryConfig) obj;
		if (hasStateHistory != other.hasStateHistory)
			return false;
		if (spiceInfo == null)
		{
			if (other.spiceInfo != null)
				return false;
		} else if (!spiceInfo.equals(other.spiceInfo))
			return false;
		if (stateHistoryEndDate == null)
		{
			if (other.stateHistoryEndDate != null)
				return false;
		} else if (!stateHistoryEndDate.equals(other.stateHistoryEndDate))
			return false;
		if (stateHistoryStartDate == null)
		{
			if (other.stateHistoryStartDate != null)
				return false;
		} else if (!stateHistoryStartDate.equals(other.stateHistoryStartDate))
			return false;
		if (timeHistoryFile == null)
		{
			if (other.timeHistoryFile != null)
				return false;
		} else if (!timeHistoryFile.equals(other.timeHistoryFile))
			return false;
		return true;
	}
	
	
	
	
}
