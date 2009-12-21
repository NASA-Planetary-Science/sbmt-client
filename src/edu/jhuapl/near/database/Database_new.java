package edu.jhuapl.near.database;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.joda.time.DateTime;

import edu.jhuapl.near.util.Configuration;


/**
 * This class represents a database storing information about all the
 * data. It also provides functions for querying the database.
 * 
 * @author kahneg1
 * 
 */
public class Database_new 
{
	public enum Datatype {MSI, NIS, NLR};
	
	private static Database_new ref = null;
	
	String version;

	private ArrayList<ArrayList<String>> doQuery(String url)
	{
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		
		try 
		{
			URL u = new URL(Configuration.getQueryRootURL() + "/" + url);
			
			InputStream is = u.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader in = new BufferedReader(isr);

			String line;
			
			while ((line = in.readLine()) != null)
			{
				String[] tokens = line.trim().split("\\s+");
				ArrayList<String> words = new ArrayList<String>();
				for (String word : tokens)
					words.add(word);
				results.add(words);
			}

			in.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		return results;
	}
	
	private String constructUrlArguments(HashMap<String, String> args)
	{
		String str = "";
		
		boolean firstKey = true;
		for (String key : args.keySet())
		{
			if (firstKey == true)
				str += "?";
			else
				str += "&";
			
			str += key + "=" + args.get(key);
			
			firstKey = false;
		}
		
		return str;
	}
	
	private String getMsiPath(ArrayList<String> result)
	{
		int id = Integer.parseInt(result.get(0));
		int year = Integer.parseInt(result.get(1));
		int dayOfYear = Integer.parseInt(result.get(2));
		int filter = Integer.parseInt(result.get(3));
		int type = Integer.parseInt(result.get(4));
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

	private String getNisPath(ArrayList<String> result)
	{
		int id = Integer.parseInt(result.get(0));
		int year = Integer.parseInt(result.get(1));
		int dayOfYear = Integer.parseInt(result.get(2));

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

	public static Database_new getInstance()                                                                                                 
    {                                                                                                                                          
        if (ref == null)                                                                                                                       
            ref = new Database_new();                                                                                                        
        return ref;                                                                                                                            
    }                                                                                                                                          
                                                                                                                                               
    public Object clone()                                                                                                                      
        throws CloneNotSupportedException                                                                                                      
    {                                                                                                                                          
        throw new CloneNotSupportedException();                                                                                                
    }                                                                                                                                          
                                                            
	private Database_new() 
	{
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
			String searchString,
			ArrayList<Integer> polygonTypes,
			double fromIncidence,
			double toIncidence,
			double fromEmission,
			double toEmission,
			double fromPhase,
			double toPhase) 
	{
		System.gc();
		
		
		ArrayList<String> matchedImages = new ArrayList<String>();
		ArrayList<ArrayList<String>> results = null;

		double minIncidence = Math.min(fromIncidence, toIncidence);
		double maxIncidence = Math.max(fromIncidence, toIncidence);
		double minEmission = Math.min(fromEmission, toEmission);
		double maxEmission = Math.max(fromEmission, toEmission);
		double minPhase = Math.min(fromPhase, toPhase);
		double maxPhase = Math.max(fromPhase, toPhase);

		switch (datatype)
		{
		case MSI:
			if (searchString != null)
			{
				try
				{
					int id = Integer.parseInt(searchString);

					HashMap<String, String> args = new HashMap<String, String>();
					args.put("id", String.valueOf(id));
					
					results = doQuery("searchmsi_id.php" + constructUrlArguments(args));
				}
				catch (NumberFormatException e)
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

//				String query = "SELECT id, year, day, filter, iofcif FROM msiimages ";
//				query += "WHERE starttime <= " + stopDate.getMillis();
//				query += " AND stoptime >= " + startDate.getMillis();
//				query += " AND target_center_distance >= " + minScDistance;
//				query += " AND target_center_distance <= " + maxScDistance;
//				query += " AND horizontal_pixel_scale >= " + minResolution;
//				query += " AND horizontal_pixel_scale <= " + maxResolution;
//				if (iofdbl == false)
//					query += " AND iofcif = 1";
//				else if (cifdbl == false)
//					query += " AND iofcif = 0";
//				query += " AND ( ";
//				for (int i=0; i<filters.size(); ++i)
//				{
//					query += " filter = " + filters.get(i);
//					if (i < filters.size()-1)
//						query += " OR ";
//				}
//				query += " ) ";
//
//				query += " AND minincidence <= " + maxIncidence;
//				query += " AND maxincidence >= " + minIncidence;
//				query += " AND minemission <= " + maxEmission;
//				query += " AND maxemission >= " + minEmission;
//				query += " AND minphase <= " + maxPhase;
//				query += " AND maxphase >= " + minPhase;
//
//				System.out.println(query);

				HashMap<String, String> args = new HashMap<String, String>();
				args.put("minResolution", String.valueOf(minResolution));
				args.put("maxResolution", String.valueOf(maxResolution));
				args.put("minScDistance", String.valueOf(minScDistance));
				args.put("maxScDistance", String.valueOf(maxScDistance));
				args.put("startDate", String.valueOf(startDate.getMillis()));
				args.put("stopDate", String.valueOf(stopDate.getMillis()));
				args.put("minIncidence", String.valueOf(minIncidence));
				args.put("maxIncidence", String.valueOf(maxIncidence));
				args.put("minEmission", String.valueOf(minEmission));
				args.put("maxEmission", String.valueOf(maxEmission));
				args.put("minPhase", String.valueOf(minPhase));
				args.put("maxPhase", String.valueOf(maxPhase));
				args.put("iofdbl", iofdbl==true ? "1" : "0");
				args.put("cifdbl", cifdbl==true ? "1" : "0");
				for (int i=1; i<=7; ++i)
				{
					if (filters.contains(i))
						args.put("filterType"+i, "1");
					else
						args.put("filterType"+i, "0");
				}

				results = doQuery("searchmsi.php" + constructUrlArguments(args));
				
				for (ArrayList<String> res : results)
				{
					String path = this.getMsiPath(res);
					
					matchedImages.add(path);
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			return matchedImages;

		case NIS:
			try
			{
				double minScDistance = Math.min(startDistance, stopDistance);
				double maxScDistance = Math.max(startDistance, stopDistance);
				
//				String query = "SELECT id, year, day FROM nisspectra ";
//				query += "WHERE midtime >= " + startDate.getMillis();
//				query += " AND midtime <= " + stopDate.getMillis();
//				query += " AND range >= " + minScDistance;
//				query += " AND range <= " + maxScDistance;
//				if (!polygonTypes.isEmpty())
//				{
//					query += " AND ( ";
//					int count = 0;
//					for (Integer i : polygonTypes)
//					{
//						if (count++ > 0)
//							query += " OR ";
//						query += " polygon_type_flag = " + i;
//					}
//					query += " ) ";
//				}
//				query += " AND minincidence <= " + maxIncidence;
//				query += " AND maxincidence >= " + minIncidence;
//				query += " AND minemission <= " + maxEmission;
//				query += " AND maxemission >= " + minEmission;
//				query += " AND minphase <= " + maxPhase;
//				query += " AND maxphase >= " + minPhase;
//
//				System.out.println(query);
				
				HashMap<String, String> args = new HashMap<String, String>();
				args.put("startDate", String.valueOf(startDate.getMillis()));
				args.put("stopDate", String.valueOf(stopDate.getMillis()));
				args.put("minScDistance", String.valueOf(minScDistance));
				args.put("maxScDistance", String.valueOf(maxScDistance));
				args.put("minIncidence", String.valueOf(minIncidence));
				args.put("maxIncidence", String.valueOf(maxIncidence));
				args.put("minEmission", String.valueOf(minEmission));
				args.put("maxEmission", String.valueOf(maxEmission));
				args.put("minPhase", String.valueOf(minPhase));
				args.put("maxPhase", String.valueOf(maxPhase));
				for (int i=0; i<4; ++i)
				{
					if (polygonTypes.contains(i))
						args.put("polygonType"+i, "1");
					else
						args.put("polygonType"+i, "0");
				}

				results = doQuery("searchnis.php" + constructUrlArguments(args));
				
				for (ArrayList<String> res : results)
				{
					String path = this.getNisPath(res);
					
					matchedImages.add(path);
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}

			break;
		}
		
		System.out.println("finished NIS query");
		
		return matchedImages;
	}


}
