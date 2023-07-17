package edu.jhuapl.sbmt.spectrum.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.IFeatureConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;

public class SpectrumInstrumentConfig implements IFeatureConfig
{
	public SpectrumInstrumentMetadataIO hierarchicalSpectraSearchSpecification;
	public String spectrumMetadataFile;
	public boolean hasHierarchicalSpectraSearch = false;
	public Date spectrumSearchDefaultStartDate;
    public Date spectrumSearchDefaultEndDate;
    public double spectrumSearchDefaultMaxSpacecraftDistance;
    public double spectrumSearchDefaultMaxResolution;

	public boolean hasHypertreeBasedSpectraSearch = false;
	public Map<String, String> spectraSearchDataSourceMap = Maps.newHashMap();
	
	public List<BasicSpectrumInstrument> spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
	
    public boolean hasSpectralData = false;
    
    private BodyViewConfig config;
    
    public SpectrumInstrumentConfig(BodyViewConfig config)
	{
		this.config = config;
	}
	
	public void setConfig(BodyViewConfig config)
	{
		this.config = config;
	}

	
    public Map<String, String> getSpectraSearchDataSourceMap()
	{
		return spectraSearchDataSourceMap;
	}
    
//	public SpectraHierarchicalSearchSpecification getHierarchicalSpectraSearchSpecification();
//
//	public Map<String, String> getSpectraSearchDataSourceMap();
//	
//	public boolean hasHypertreeBasedSpectraSearch();
//
//	public boolean hasHierarchicalSpectraSearch();
    
    public SpectrumInstrumentMetadataIO  getHierarchicalSpectraSearchSpecification()
    {
        return hierarchicalSpectraSearchSpecification;
    }
    
//	@Override
	public boolean hasHypertreeBasedSpectraSearch()
	{
		return hasHypertreeBasedSpectraSearch;
	}

//	@Override
	public boolean hasHierarchicalSpectraSearch()
	{
		return hasHierarchicalSpectraSearch;
	}
    
    
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
    	SpectrumInstrumentConfig c = (SpectrumInstrumentConfig)super.clone();
        c.hasSpectralData = this.hasSpectralData;

    	return c;
    }
    
    @Override
    public int hashCode()
    {
    	final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (hasHierarchicalSpectraSearch ? 1231 : 1237);
		result = prime * result + (hasHypertreeBasedSpectraSearch ? 1231 : 1237);
		result = prime * result + ((spectraSearchDataSourceMap == null) ? 0 : spectraSearchDataSourceMap.hashCode());
		result = prime * result + ((spectralInstruments == null) ? 0 : spectralInstruments.hashCode());
		result = prime * result + ((spectrumMetadataFile == null) ? 0 : spectrumMetadataFile.hashCode());
		result = prime * result + (hasSpectralData ? 1231 : 1237);

		return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
    	if (this == obj)
			return true;
		if (!super.equals(obj))
		{
			return false;
		}

		SpectrumInstrumentConfig other = (SpectrumInstrumentConfig) obj;
		if (hasHierarchicalSpectraSearch != other.hasHierarchicalSpectraSearch)
		{
			return false;
		}
		
		if (hasHypertreeBasedSpectraSearch != other.hasHypertreeBasedSpectraSearch)
		{
			return false;
		}
		if (hasSpectralData != other.hasSpectralData)
		{
			return false;
		}
		if (spectraSearchDataSourceMap == null)
		{
			if (other.spectraSearchDataSourceMap != null)
				return false;
		} else if (!spectraSearchDataSourceMap.equals(other.spectraSearchDataSourceMap))
		{
//			System.err.println("BodyViewConfig: equals: spectra search data source map don't match");
			return false;
		}
		if (spectralInstruments == null)
		{
			if (other.spectralInstruments != null)
				return false;
		} else if (!spectralInstruments.equals(other.spectralInstruments))
			return false;
		if (spectrumMetadataFile == null)
		{
			if (other.spectrumMetadataFile != null)
				return false;
		} else if (!spectrumMetadataFile.equals(other.spectrumMetadataFile))
		{
//			System.err.println("BodyViewConfig: equals: spectrum metadata files don't match");
			return false;
		}
		
		return true;
    }
}
