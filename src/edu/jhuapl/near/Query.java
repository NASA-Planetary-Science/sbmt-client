package edu.jhuapl.near;

import java.io.*;
import java.util.*;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.*;
import edu.jhuapl.geoviewer.cache.gui.DatabaseSelector;
import edu.jhuapl.geoviewer.cache.model.Cache;
import edu.jhuapl.geoviewer.cache.model.CacheDataModel;
import edu.jhuapl.geoviewer.cache.model.IED;
import edu.jhuapl.geoviewer.cache.pick.PickManager;
import edu.jhuapl.geoviewer.common.gui.InformationWindow;
import edu.jhuapl.geoviewer.common.query.Query;
import edu.jhuapl.geoviewer.common.util.PointInPolygon;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements a query on the cache/ied databases. When run is called,
 * a list of caches and ieds are generated which match the query parameters
 * passed in the constructor of the class. To get the caches and ieds, call
 * getIEDs or getCaches. In addition, this class provides several exporting
 * functions which generate CSV files of various summaries.
 * 
 * @author kahneg1
 * 
 */
 
public class Query
{
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double north = 90.0;
    private double south = -90.0;
    private double east = 180.0;
    private double west = -180.0;
    private Position center;
    private double radius;
//    private double radius2; // used only for queries searching for ieds near caches and vice versa
//    private Duration timeInterval;
    private String shapeType;
    private String queryType;
    private FieldFilter fieldFilter;
    // 	If the query region is a shapefile polygon, store the longitudes and latitudes here. 	
    private double [] polyLon; 
    private double [] polyLat;
    
	public static final String SEARCH_IEDS_AND_CACHES = "Search IEDs and Caches";
	public static final String SEARCH_IEDS_ONLY = "Search IEDs only";
	public static final String SEARCH_CACHES_ONLY = "Search Caches only";
	public static final String SEARCH_CACHES_NEAR_IED = "Search Caches Near Specific IED";
	public static final String SEARCH_IEDS_NEAR_CACHE = "Search IEDs Near Specific Cache";

	public static final String CIRCLE = "Circle";
	public static final String RECTANGLE = "Rectangle";
	public static final String SHAPEFILE_POLYGON = "Shapefile";

	public static final String QUERY = "query";
	public static final String QUERY_TYPE = "query-type";
	public static final String SHAPE_TYPE = "shape-type";
	
	public static final String START_DATE = "start-date";
	public static final String END_DATE = "end-date";
	
	public static final String CACHE_FIELD_FILTER = "cache-field-filter";

	public static final String POSITIONS = "positions";
	public static final String CENTER = "center";
	public static final String RADIUS = "radius";
	
    private ArrayList<IED> ieds = new ArrayList<IED>();
    private ArrayList<Cache> caches = new ArrayList<Cache>();

    ArrayList<Position> positions;

	/**
	 * Constructor
	 * 
	 * @param shapeType
	 *            either CIRCLE or RECTANGLE
	 * @param positions
	 *            if shapeType is RECTANGLE, contains the corners of the
	 *            rectangle, otherwise ignored
	 * @param center
	 *            if shapeType is CIRCLE, contains the center of the circle.
	 * @param radius
	 *            if shapeType is CIRCLE, contains the radius of the circle.
	 * @param radius2
	 *            not used
	 * @param startDate
	 *            the start date of the query
	 * @param endDate
	 *            the end date of the query
	 * @param timeInterval
	 *            not used
	 * @param queryType
	 *            the type of query valid options are SEARCH_IEDS_AND_CACHES,
	 *            SEARCH_IEDS_ONLY, SEARCH_CACHES_ONLY, SEARCH_CACHES_NEAR_IED,
	 *            SEARCH_IEDS_NEAR_CACHE
	 * @param fieldFilter
	 *            a field filter for restricting the matches of the query to
	 *            various values
	 */
    public Query(String shapeType,
    				  ArrayList<Position> positions,
    				  Position center,
    				  double radius,
    				  double radius2,
    				  LocalDateTime startDate, 
    				  LocalDateTime endDate,
    				  Duration timeInterval,
    				  String queryType,
    				  FieldFilter fieldFilter)
    {
        this.startDate = startDate;
        this.endDate = endDate;
        this.shapeType = shapeType;
        this.queryType = queryType;
        this.fieldFilter = fieldFilter;
        this.positions = positions;
//        this.radius2 = radius2;
//        this.timeInterval = timeInterval;
    	this.center = center;
    	this.radius = radius;

    	init();
    }

    /**
     * Default Constructor.
     */
    public Query()
    {
    	
    }

    /**
     * Initializes the query. Called by the constructor and fromXmlDomElement.
     */
    private void init()
    {
        if (shapeType.equals(RECTANGLE))
        {
        	if (positions != null && positions.size() == 4)
        	{
        		double x0 = positions.get(0).getLongitude().getDegrees();
        		double x1 = positions.get(1).getLongitude().getDegrees();
        		double x2 = positions.get(2).getLongitude().getDegrees();
        		double x3 = positions.get(3).getLongitude().getDegrees();
        		double y0 = positions.get(0).getLatitude().getDegrees();
        		double y1 = positions.get(1).getLatitude().getDegrees();
        		double y2 = positions.get(2).getLatitude().getDegrees();
        		double y3 = positions.get(3).getLatitude().getDegrees();

        		this.north = Math.max(Math.max(y0, y1), Math.max(y2, y3));
        		this.south = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        		this.east = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        		this.west = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        	}
        }
        else if (shapeType.equals(SHAPEFILE_POLYGON))
        {
        	this.polyLon = new double[positions.size()];
        	this.polyLat = new double[positions.size()];

        	final int n = this.polyLon.length;
    		
    		for (int i=0; i<n; i++) 
    		{
    			this.polyLon[i] = positions.get(i).getLongitude().getRadians();
    			this.polyLat[i] = positions.get(i).getLatitude().getRadians();
    		}
        }
    }
    
    /**
     * Starts running the query. The matching caches and/or ieds are stored in 
     * this.caches and this.ieds
     */
    public void run()
    {
    	CacheDataModel model = CacheDataModel.getInstance();
		this.ieds.clear();
		this.caches.clear();

    	if (this.queryType.equals(SEARCH_IEDS_AND_CACHES) || 
    		this.queryType.equals(SEARCH_IEDS_ONLY) ||
    		this.queryType.equals(SEARCH_IEDS_NEAR_CACHE))
    	{
    		if (model.getIEDs().size() > 0)
    		{
    			for (IED ied : model.getIEDs())
    			{
    				if (this.containsPosition(ied.getPosition()) &&
    						this.containsDate(ied.dateTime))
    				{
    					this.ieds.add(ied);
    				}
    			}
    		}
    		if (this.queryType.equals(SEARCH_IEDS_NEAR_CACHE) &&
    				model.getCaches().size() > 0 &&
    				this.center != null)
    		{
    			this.caches.add((Cache)PickManager.getInstance().getLastPickedEvent());
    		}
    	}

    	if (this.queryType.equals(SEARCH_IEDS_AND_CACHES) || 
        	this.queryType.equals(SEARCH_CACHES_ONLY) ||
        	this.queryType.equals(SEARCH_CACHES_NEAR_IED))
    	{
    		if (model.getCaches().size() > 0)
    		{
    			for (Cache cache : model.getCaches())
    			{
    				if (this.containsPosition(cache.getPosition()) &&
    						this.containsDate(cache.dateTime) &&
    						this.fieldFilter.filter(cache))
    				{
    					this.caches.add(cache);
    				}
    			}
    		}
    		if (this.queryType.equals(SEARCH_CACHES_NEAR_IED) &&
    				model.getIEDs().size() > 0 &&
    				this.center != null)
    		{
    			this.ieds.add((IED)PickManager.getInstance().getLastPickedEvent());
    		}
    	}
/*
    	if (this.queryType.equals(SEARCH_IEDS_NEAR_CACHE))
    	{
    		//System.out.println(model.getIEDs());
    		if (model.getCaches().size() > 0)
    		{
    			for (Cache cache : model.getCaches())
    			{
    				if (this.containsPosition(cache.getPosition()) &&
    						this.containsDate(cache.dateTime) &&
    						this.fieldFilter.filter(cache))
    				{
    					this.caches.add(cache);
    		    		if (model.getIEDs().size() > 0)
    		    		{
    		    			for (IED ied : model.getIEDs())
    		    			{
    		    				if (this.containsPosition(ied.getPosition()) &&
    		    						this.containsDate(ied.dateTime))
    		    				{
    		    					this.ieds.add(ied);
    		    				}
    		    			}
    		    		}
    				}
    			}
    		}
    	}
*/
    	InformationWindow.getInstance().appendText(this.ieds.size() + " IEDs matched query.");
    	InformationWindow.getInstance().appendText(this.caches.size() + " caches matched query.\n");
    }


    public ArrayList<IED> getIEDs()
    {
        return this.ieds;
    }

    public ArrayList<Cache> getCaches()
    {
        return this.caches;
    }

    
    /**
	 * @return the startDate
	 */
	public LocalDateTime getStartDate() {
		return startDate;
	}

	/**
	 * @return the endDate
	 */
	public LocalDateTime getEndDate() {
		return endDate;
	}

	/**
	 * @return the center
	 */
	public Position getCenter() {
		return center;
	}

	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @return the shapeType
	 */
	public String getShapeType() {
		return shapeType;
	}

	/**
	 * @return the queryType
	 */
	public String getQueryType() {
		return queryType;
	}

	/**
	 * @return the positions
	 */
	public ArrayList<Position> getPositions() {
		return positions;
	}

	/**
	 * Determines if pos matches this query.
	 * 
	 * @param pos
	 *            the position to test
	 * @return true if pos matches this query, false otherwise.
	 */
	private boolean containsPosition(Position pos)
    {
        if (shapeType.equals(RECTANGLE))
        {
            double x = pos.getLongitude().getDegrees();
            double y = pos.getLatitude().getDegrees();

        	if ( x > east ||
         		 x < west ||
        		 y > north ||
        		 y < south )
        	{
        		return false;
        	}
        	else
        	{
        		return true;
        	}
        }
        else if (shapeType.equals(CIRCLE))
        {
        	if (center == null)
        		return true;
        	
            double d = LatLon.greatCircleDistance(pos.getLatLon(), center.getLatLon()).getRadians();
        	if ( d > radius )
           	{
           		return false;
           	}
           	else
           	{
           		return true;
           	}
        }
        else
        {
        	if (this.polyLon.length == 0)
        		return true;
        	
        	double plon = pos.getLongitude().getRadians();
            double plat = pos.getLatitude().getRadians();
        	return PointInPolygon.pointInPolygonGeo(plon, plat, this.polyLon, this.polyLat);
        }
    }

	/**
	 * Determines if date matches this query
	 * @param date date to test
	 * @return true if date matches query, false otherwise.
	 */
    private boolean containsDate(LocalDateTime date)
    {
    	if (date.compareTo(startDate) >= 0 &&
    		date.compareTo(endDate) <= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
    public boolean saveQuery(String filename)
    {
    	return true;
    }
    
    private Duration formDuration(LocalDateTime t1, LocalDateTime t2)
    {
		return new Duration(
				t1.getChronology().
					getDateTimeMillis(t1.getYear(), 
									  t1.getMonthOfYear(),
									  t1.getDayOfMonth(),
									  t1.getMillisOfDay()), 
				t2.getChronology().
					getDateTimeMillis(t2.getYear(), 
									  t2.getMonthOfYear(),
									  t2.getDayOfMonth(),
									  t2.getMillisOfDay())); 
    }
    
    private LocalDateTime parseDateTime(String str)
    {
		String [] vals = str.split(" ");
		int year = Integer.parseInt(vals[0]);
		int month = Integer.parseInt(vals[1]);
		int day = Integer.parseInt(vals[2]);
		int hour = Integer.parseInt(vals[3]);
		int min = Integer.parseInt(vals[4]);
		return new LocalDateTime(year, month, day, hour, min);
    }
   
 
    static private class Interval
    {
    	public double min;
    	public double max;
    }
    
    private static final GregorianCalendar cal = new GregorianCalendar();
    private static final double millisInRegularYear = 365.0 * 24.0 * 60.0 * 60.0 * 1000.0;
    private static final double millisInLeapYear =    366.0 * 24.0 * 60.0 * 60.0 * 1000.0;
    private static final double millisInDay =                 24.0 * 60.0 * 60.0 * 1000.0;
    private double convertDateTimeToDouble(LocalDateTime dt)
    {
    	int year = dt.getYear();
    	
    	double doy = dt.getDayOfYear();
    	double mod = dt.getMillisOfDay();
    	double millisInYear = millisInRegularYear;
    	if (cal.isLeapYear(year))
    		millisInYear = millisInLeapYear;
    	
    	return (double)year + (doy * millisInDay + mod ) / millisInYear;
    }
    
    private Interval getMinMaxField(String database, String field)
    {
    	Interval interval = new Interval();
    	interval.min = Double.MAX_VALUE;
    	interval.max = Double.MIN_VALUE;
    	if (database.equals(DatabaseSelector.CACHE_DATABASE))
    	{
    		for (Cache cache : this.caches)
    		{
    			if (field.equals(CacheDataModel.LAT))
    			{
    				double lat = cache.getPosition().getLatitude().getDegrees();
    				if (lat < interval.min)
    					interval.min = lat;
    				if (lat > interval.max)
    					interval.max = lat;
    			}
    			else if (field.equals(CacheDataModel.LON))
    			{
    				double lon = cache.getPosition().getLongitude().getDegrees();
    				if (lon < interval.min)
    					interval.min = lon;
    				if (lon > interval.max)
    					interval.max = lon;
    			}
    			else if (field.equals(CacheDataModel.DATE))
    			{
    				double dt = this.convertDateTimeToDouble(cache.dateTime);
    				if (dt < interval.min)
    					interval.min = dt;
    				if (dt > interval.max)
    					interval.max = dt;
    			}
    		}
    	}
    	return interval;
    }
    
}