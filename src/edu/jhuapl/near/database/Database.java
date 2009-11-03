package edu.jhuapl.near.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.joda.time.LocalDateTime;


/**
 * This class represents a database storing information about all the
 * data. It also provides functions for querying the database.
 * 
 * @author kahneg1
 * 
 */
public class Database 
{
	private static Database ref = null;
	
	String version;

	ArrayList<ImageInfo> images = new ArrayList<ImageInfo>();
	
	static class ImageInfo
	{
		int name;
		short year;
		short dayOfYear;
		String type; // either iofdbl or cifdbl
		int filter;
		LocalDateTime startTime;
		LocalDateTime stopTime;
		float scDistance; // Distance of spacecraft in km from center of Eros at time image was taken.
		float resolution;
		
		String getPath()
		{
			String str = "/";
			str += year + "/";
			
			if (dayOfYear < 10)
				str += "00";
			else if (dayOfYear < 100)
				str += "0";
	
			str += dayOfYear + "/";
			
			str += type + "/";
			
			str += "M0" + name + "F" + filter + "_2P_";
			
			if (type.equals("iofdbl"))
				str += "IOF_DBL.FIT";
			else
				str += "CIF_DBL.FIT";
			
			return str;
		}
	}
	
	public static Database getInstance()                                                                                                 
    {                                                                                                                                          
        if (ref == null)                                                                                                                       
            ref = new Database();                                                                                                        
        return ref;                                                                                                                            
    }                                                                                                                                          
                                                                                                                                               
    public Object clone()                                                                                                                      
        throws CloneNotSupportedException                                                                                                      
    {                                                                                                                                          
        throw new CloneNotSupportedException();                                                                                                
    }                                                                                                                                          
                                                            
	private Database() {
		// Create a directory in the user's home space for storing
		// the cells of the database that get downloaded.
		
		try 
		{
			this.loadMSIDatabase();
		} 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadMSIDatabase() throws NumberFormatException, IOException
	{
		InputStream is = getClass().getResourceAsStream("/edu/jhuapl/near/data/MsiTemporalDb.txt");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isr);

		String line;
        while ((line = in.readLine()) != null)
        {
            String [] tokens = line.split("\t");
            
            if (tokens.length < 5)
            {
                System.out.println(tokens.length);
                for (int i=0;i<tokens.length;++i)
                	System.out.println(tokens[i]);
                continue;
            }

            ImageInfo info = new ImageInfo();
            
            info.name = Integer.parseInt(tokens[0]);
            info.year = Short.parseShort(tokens[1]);
            info.dayOfYear = Short.parseShort(tokens[2]);
            info.type = tokens[3];
            info.filter = Byte.parseByte(tokens[4]);
            info.startTime = new LocalDateTime(tokens[5]);
            info.stopTime = new LocalDateTime(tokens[6]);
            info.scDistance = Float.parseFloat(tokens[7]);
            info.resolution = Float.parseFloat(tokens[8]);
            
            images.add(info);
        }
        
        in.close();
	}
	
	/**
	 * Run a query which searches for msi images between the specified dates.
	 * Returns a list of URL's of the fit files that match.
	 * 
	 * @param startDate
	 * @param endDate
	 */
	public ArrayList<String> runQuery(
			LocalDateTime startDate, 
			LocalDateTime stopDate,
			ArrayList<Integer> filters,
			boolean iofdbl,
			boolean cifdbl,
			double startDistance,
			double stopDistance,
			double startResolution,
			double stopResolution,
			String searchString) 
	{
		ArrayList<String> matchedImages = new ArrayList<String>();
		
		if (searchString != null)
		{
			for (ImageInfo info: images)
			{
				if (info.name == Integer.parseInt(searchString))
				{
					matchedImages.add(info.getPath());
					break;
				}
			}
			return matchedImages;
		}
		
		if (filters.isEmpty() || (iofdbl == false && cifdbl == false))
			return matchedImages;
		
		double minScDistance = Math.min(startDistance, stopDistance);
		double maxScDistance = Math.max(startDistance, stopDistance);
		double minResolution = Math.min(startResolution, stopResolution) / 1000.0;
		double maxResolution = Math.max(startResolution, stopResolution) / 1000.0;

		for (ImageInfo info: images)
		{
			if ((iofdbl && info.type.equals("iofdbl")) ||
					(cifdbl && info.type.equals("cifdbl")))
			{
				if (filters.contains(info.filter) &&
						containsDate(startDate, stopDate, info) &&
						info.scDistance >= minScDistance && info.scDistance <= maxScDistance &&
						info.resolution >= minResolution && info.resolution <= maxResolution)
				{
					matchedImages.add(info.getPath());
				}
			}
		}
		
		return matchedImages;
	}

	/**
	 * Determines if query time interval intersects with the image time interval
	 * @param date date to test
	 * @return true if date matches query, false otherwise.
	 */
    private boolean containsDate(
    		LocalDateTime startDate, 
    		LocalDateTime stopDate, 
    		ImageInfo image)
    {
    	if (image.startTime.compareTo(stopDate) <= 0 &&
    		image.stopTime.compareTo(startDate) >= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
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
    */
}
