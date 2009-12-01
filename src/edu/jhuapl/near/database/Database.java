package edu.jhuapl.near.database;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

import org.joda.time.DateTime;

import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.ConvertResourceToFile;


/**
 * This class represents a database storing information about all the
 * data. It also provides functions for querying the database.
 * 
 * @author kahneg1
 * 
 */
public class Database 
{
	public enum Datatype {MSI, NIS, NLR};
	
	private static Database ref = null;
	
	String version;
	SqlManager db = null;

	private String getMsiPath(ArrayList<Object> result)
	{
		int id = (Integer)result.get(0);
		int year = (Integer)result.get(1);
		int dayOfYear = (Integer)result.get(2);
		int filter = (Integer)result.get(5);
		int type = (Integer)result.get(6);
		String typeStr;
		if (type == 0)
			typeStr = "iofdbl";
		else
			typeStr = "cifdbl";
		
		return this.getMsiPath(id, year, dayOfYear, typeStr, filter);
	}

	private String getMsiPath(int name, int year, int dayOfYear, String type, int filter)
	{
		String str = "/MSI/";
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

	private String getNisPath(ArrayList<Object> result)
	{
		int id = (Integer)result.get(0);
		int year = (Integer)result.get(1);
		int dayOfYear = (Integer)result.get(2);

		return this.getNisPath(id, year, dayOfYear);
	}

	private String getNisPath(int name, int year, int dayOfYear)
	{
		String str = "/NIS/";
		str += year + "/";
		
		if (dayOfYear < 10)
			str += "00";
		else if (dayOfYear < 100)
			str += "0";

		str += dayOfYear + "/";
		
		str += "N0" + name + ".NIS";
		
		return str;
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
                                                            
	private Database() 
	{
		// First save to database files stored in the jar to the user's home directory
		String[] databaseresources = {
				"/edu/jhuapl/near/data/neardb/near.backup",
				"/edu/jhuapl/near/data/neardb/near.data",
				"/edu/jhuapl/near/data/neardb/near.properties",
				"/edu/jhuapl/near/data/neardb/near.script"
		};
		
		for (String resourcename : databaseresources)
		{
			ConvertResourceToFile.convertResourceToRealFile(
					this, 
					resourcename,
					Configuration.getDatabaseDir());
		}

        try 
        {
            db = new SqlManager(Configuration.getDatabaseDir() + File.separator + Configuration.getDatabaseName());
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            return;
        }

	}

	
	/**
	 * Run a query which searches for msi images between the specified dates.
	 * Returns a list of URL's of the fit files that match.
	 * 
	 * @param startDate
	 * @param endDate
	 */
	public ArrayList<String> runQuery(
			Datatype datatype,
			DateTime startDate, 
			DateTime stopDate,
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
		ArrayList<ArrayList<Object>> results = null;
		
		switch (datatype)
		{
		case MSI:
			if (searchString != null)
			{
				try
				{
					int id = Integer.parseInt(searchString);

					results = db.query("SELECT * FROM msiimages WHERE id = " + id);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				if (results != null && results.size() > 0)
				{
					String path = this.getMsiPath(results.get(0));
	
					matchedImages.add(path);
				}
				return matchedImages;
			}

			if (filters.isEmpty() || (iofdbl == false && cifdbl == false))
				return matchedImages;
			
			try
			{
				double minScDistance = Math.min(startDistance, stopDistance);
				double maxScDistance = Math.max(startDistance, stopDistance);
				double minResolution = Math.min(startResolution, stopResolution) / 1000.0;
				double maxResolution = Math.max(startResolution, stopResolution) / 1000.0;

				String query = "SELECT * FROM msiimages ";
				query += "WHERE starttime <= " + stopDate.getMillis();
				query += " AND stoptime >= " + startDate.getMillis();
				query += " AND target_center_distance >= " + minScDistance ;
				query += " AND target_center_distance <= " + maxScDistance;
				query += " AND horizontal_pixel_scale >= " + minResolution;
				query += " AND horizontal_pixel_scale <= " + maxResolution;
				if (iofdbl == false)
					query += " AND iofcif = 1";
				else if (cifdbl == false)
					query += " AND iofcif = 0";
				query += " AND ( ";
				for (int i=0; i<filters.size(); ++i)
				{
					query += " filter = " + filters.get(i);
					if (i < filters.size()-1)
						query += " OR ";
				}
				query += " ) ";
				
				results = db.query(query);
				
				for (ArrayList<Object> res : results)
				{
					String path = this.getMsiPath(res);
					
					matchedImages.add(path);
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			

//			for (ImageInfo info: images)
//			{
//				if ((iofdbl && info.type.equals("iofdbl")) ||
//						(cifdbl && info.type.equals("cifdbl")))
//				{
//					if (filters.contains(info.filter) &&
//							containsDate(startDate, stopDate, info) &&
//							info.scDistance >= minScDistance && info.scDistance <= maxScDistance &&
//							info.resolution >= minResolution && info.resolution <= maxResolution)
//					{
//						matchedImages.add(info.getPath());
//					}
//				}
//			}

			return matchedImages;

		case NIS:
			try
			{
				double minScDistance = Math.min(startDistance, stopDistance);
				double maxScDistance = Math.max(startDistance, stopDistance);

				String query = "SELECT * FROM nisspectra ";
				query += "WHERE midtime >= " + startDate.getMillis();
				query += " AND midtime <= " + stopDate.getMillis();
				//query += " AND range >= " + minScDistance ;
				//query += " AND range <= " + maxScDistance;
				query += " AND polygon_type_flag >= " + -999.0;
				query += " AND polygon_type_flag <= " + -999.0;
				
				results = db.query(query);
				
				for (ArrayList<Object> res : results)
				{
					String path = this.getNisPath(res);
					
					matchedImages.add(path);
				}
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}

			break;
		}
		
		return matchedImages;
	}


}
