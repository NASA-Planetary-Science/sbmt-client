package edu.jhuapl.near.dbgen;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import edu.jhuapl.near.database.*;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.*;

import nom.tam.fits.FitsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DatabaseGeneratorSql 
{
	static SqlManager db = null;
	static PreparedStatement msiInsert = null;
	static PreparedStatement nisInsert = null;
	static ErosModel erosModel;
	
    static void createMSITables()
    {
    	System.out.println("creating msi");
        try {

            //make a table
            db.update(
            		"create cached table msiimages(" +
            		"id int PRIMARY KEY, " +
            		"year smallint, " +
            		"day smallint, " +
            		//"starttime timestamp, " +
            		//"stoptime timestamp, " +
            		"starttime bigint, " +
            		"stoptime bigint, " +
            		"filter tinyint, " +
            		"iofcif tinyint," +
            		"target_center_distance double," +
            		"horizontal_pixel_scale double," +
            		"has_limb boolean" +
            		")"
                );
        } catch (SQLException ex2) {

            //ignore
            ex2.printStackTrace();  // second time we run program
            //  should throw execption since table
            // already there
            //
            // this will have no effect on the db
        }
    }
    
    static void createNISTables()
    {
        try {

            //make a table
            db.update(
            		"create cached table nisspectra(" +
            		"id int PRIMARY KEY, " +
            		"year smallint, " +
            		"day smallint, " +
            		//"time timestamp, " +
            		"midtime bigint, " +
            		"range double, " +
            		"polygon_type_flag smallint)"
                );
        } catch (SQLException ex2) {

            //ignore
        	ex2.printStackTrace();  // second time we run program
            //  should throw execption since table
            // already there
            //
            // this will have no effect on the db
        }
    }
    
    static void populateMSITables(ArrayList<String> msiFiles) throws IOException, SQLException, FitsException
    {
    	int count = 1;
    	
    	for (String filename : msiFiles)
    	{
			boolean filesExist = checkIfAllFilesExist(filename);
			if (filesExist == false)
				continue;

			System.out.println("starting msi " + count++);
			
    		byte iof_or_cif = -1;
    		String dayOfYearStr = "";
    		String yearStr = "";

    		File origFile = new File(filename);
    		File f = origFile;

    		f = f.getParentFile();
    		if (f.getName().equals("iofdbl"))
    			iof_or_cif = 0;
    		else if (f.getName().equals("cifdbl"))
    			iof_or_cif = 1;

    		f = f.getParentFile();
    		dayOfYearStr = f.getName();

    		f = f.getParentFile();
    		yearStr = f.getName();

    		NearImage image = new NearImage(origFile);
    		//HashMap<String, String> properties = image.getProperties();

    		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
    		HashMap<String, String> properties = NearImage.parseLblFile(lblFilename);
    		
            if (msiInsert == null)
            {
            	msiInsert = db.preparedStatement(                                                                                    
            		"insert into msiimages values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");                                                                   
            }

            DateTime startTime = new DateTime(properties.get(NearImage.START_TIME), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(properties.get(NearImage.STOP_TIME), DateTimeZone.UTC);
    		// Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());
            
            
    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		System.out.println("iof_or_cif: " + iof_or_cif);
    		System.out.println("starttime: " + startTime);
    		System.out.println("stoptime: " + stopTime);
    		System.out.println("filter: " + Integer.parseInt(origFile.getName().substring(12, 13)));
    		System.out.println("iof_or_cif: " + iof_or_cif);
    		System.out.println("TARGET_CENTER_DISTANCE: " + properties.get(NearImage.TARGET_CENTER_DISTANCE));
    		System.out.println("HORIZONTAL_PIXEL_SCALE: " + properties.get(NearImage.HORIZONTAL_PIXEL_SCALE));
    		System.out.println("hasLimb: " + image.containsLimb());
    		System.out.println(" ");

            msiInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            msiInsert.setShort(2, Short.parseShort(yearStr));
            msiInsert.setShort(3, Short.parseShort(dayOfYearStr));
            //msiInsert.setTimestamp(4, Timestamp.valueOf(startTime));
            //msiInsert.setTimestamp(5, Timestamp.valueOf(stopTime));
            msiInsert.setLong(4, startTime.getMillis());
            msiInsert.setLong(5, stopTime.getMillis());
            msiInsert.setByte(6, Byte.parseByte(origFile.getName().substring(12, 13)));
            msiInsert.setByte(7, iof_or_cif);
            msiInsert.setDouble(8, Double.parseDouble(properties.get(NearImage.TARGET_CENTER_DISTANCE)));
            msiInsert.setDouble(9, Double.parseDouble(properties.get(NearImage.HORIZONTAL_PIXEL_SCALE)));
            msiInsert.setBoolean(10, image.containsLimb());
            
            msiInsert.executeUpdate();
    	}
    }
    
    static void populateNISTables(ArrayList<String> nisFiles) throws SQLException, IOException
    {
    	for (String filename : nisFiles)
    	{
    		String dayOfYearStr = "";
    		String yearStr = "";

    		File origFile = new File(filename);
    		File f = origFile;

    		f = f.getParentFile();
    		dayOfYearStr = f.getName();

    		f = f.getParentFile();
    		yearStr = f.getName();


    		NISSpectrum nisSpectrum = new NISSpectrum(origFile, erosModel);
    		
    		if (nisInsert == null)
    		{
    			nisInsert = db.preparedStatement(                                                                                    
    					"insert into nisspectra values (?, ?, ?, ?, ?, ?)");                                                                   
    		}

    		DateTime midtime = new DateTime(nisSpectrum.getDateTime().toString(), DateTimeZone.UTC);
    		// Replace the "T" with a space
    		//time = time.substring(0, 10) + " " + time.substring(11, time.length());
    		
    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		System.out.println("midtime: " + midtime);
    		System.out.println("range: " + nisSpectrum.getRange());
    		System.out.println("polygon type: " + nisSpectrum.getPolygonTypeFlag());
    		System.out.println(" ");
    		

    		nisInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
    		nisInsert.setShort(2, Short.parseShort(yearStr));
    		nisInsert.setShort(3, Short.parseShort(dayOfYearStr));
    		nisInsert.setLong(4, midtime.getMillis());
    		nisInsert.setDouble(5, nisSpectrum.getRange());
    		nisInsert.setShort(6, nisSpectrum.getPolygonTypeFlag());

    		nisInsert.executeUpdate();
    	}
    }

	static boolean checkIfAllFilesExist(String line)
	{
		File file = new File(line);
		if (!file.exists())
			return false;

		String name = line.substring(0, line.length()-4) + ".LBL";
		file = new File(name);
		if (!file.exists())
			return false;
		
		name = line.substring(0, line.length()-4) + "_DDR.LBL";
		file = new File(name);
		if (!file.exists())
			return false;

		name = line.substring(0, line.length()-4) + "_DDR.IMG.gz";
		file = new File(name);
		if (!file.exists())
			return false;
		
		name = line.substring(0, line.length()-4) + "_BOUNDARY.VTK";
		file = new File(name);
		if (!file.exists())
			return false;

		return true;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		erosModel = new ErosModel();
		
		String msiFileList=args[1];
		String nisFileList=args[2];

		
		ArrayList<String> msiFiles = null;
		ArrayList<String> nisFiles = null;
		try {
			msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
			nisFiles = FileUtil.getFileLinesAsStringList(nisFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
        try 
        {
            db = new SqlManager(args[0]);
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            return;
        }

		
		createMSITables();
		createNISTables();

		
		try 
		{
			populateMSITables(msiFiles);
			populateNISTables(nisFiles);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
        try 
        {
			db.shutdown();
		}
        catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
