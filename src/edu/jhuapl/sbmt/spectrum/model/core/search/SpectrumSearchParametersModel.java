package edu.jhuapl.sbmt.spectrum.model.core.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Model for search parameters for spectra searches
 * @author steelrj1
 *
 */
public class SpectrumSearchParametersModel
{
	private double minDistanceQuery;
    private double maxDistanceQuery;
    private double minIncidenceQuery;
    private double maxIncidenceQuery;
    private double minEmissionQuery;
    private double maxEmissionQuery;
    private double minPhaseQuery;
    private double maxPhaseQuery;
    protected Date startDate = new GregorianCalendar(2000, 0, 11, 0, 0, 0).getTime();
    protected Date endDate = new GregorianCalendar(2000, 4, 14, 0, 0, 0).getTime();
    private List<Integer> polygonTypesChecked = new ArrayList<Integer>();
    private String searchByFilename;


	public SpectrumSearchParametersModel()
	{
		// TODO Auto-generated constructor stub
	}

	public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }


	public double getMinDistanceQuery()
    {
        return minDistanceQuery;
    }


    public void setMinDistanceQuery(double minDistanceQuery)
    {
        this.minDistanceQuery = minDistanceQuery;
    }


    public double getMaxDistanceQuery()
    {
        return maxDistanceQuery;
    }


    public void setMaxDistanceQuery(double maxDistanceQuery)
    {
        this.maxDistanceQuery = maxDistanceQuery;
    }


    public double getMinIncidenceQuery()
    {
        return minIncidenceQuery;
    }


    public void setMinIncidenceQuery(double minIncidenceQuery)
    {
        this.minIncidenceQuery = minIncidenceQuery;
    }


    public double getMaxIncidenceQuery()
    {
        return maxIncidenceQuery;
    }


    public void setMaxIncidenceQuery(double maxIncidenceQuery)
    {
        this.maxIncidenceQuery = maxIncidenceQuery;
    }


    public double getMinEmissionQuery()
    {
        return minEmissionQuery;
    }


    public void setMinEmissionQuery(double minEmissionQuery)
    {
        this.minEmissionQuery = minEmissionQuery;
    }


    public double getMaxEmissionQuery()
    {
        return maxEmissionQuery;
    }


    public void setMaxEmissionQuery(double maxEmissionQuery)
    {
        this.maxEmissionQuery = maxEmissionQuery;
    }


    public double getMinPhaseQuery()
    {
        return minPhaseQuery;
    }


    public void setMinPhaseQuery(double minPhaseQuery)
    {
        this.minPhaseQuery = minPhaseQuery;
    }


    public double getMaxPhaseQuery()
    {
        return maxPhaseQuery;
    }


    public void setMaxPhaseQuery(double maxPhaseQuery)
    {
        this.maxPhaseQuery = maxPhaseQuery;
    }

    public void addToPolygonsSelected(int index)
    {
        polygonTypesChecked.add(index);
    }

	public List<Integer> getPolygonTypesChecked()
	{
		return polygonTypesChecked;
	}

    public void setSearchByFilename(String searchByFilename)
    {
        this.searchByFilename = searchByFilename;
    }

	public String getSearchByFilename()
	{
		return searchByFilename;
	}
}
