package edu.jhuapl.near.dbgen;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import vtk.*;

import edu.jhuapl.near.*;
import edu.jhuapl.near.database.SqlManager;
import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

import nom.tam.fits.FitsException;

public class DatabaseGeneratorSql 
{
	static SqlManager db = null;
	static PreparedStatement msiInsert = null;
	static PreparedStatement nisInsert = null;
	
    static void createMSITables()
    {
    	System.out.println("creating msi");
        try {

            //make an empty table
            //
            // by declaring the id column IDENTITY, the db will automatically
            // generate unique values for new rows- useful for row keys
            db.update(
            		"create cached table msiimages(" +
            		"id int, " +
            		"year smallint, " +
            		"day smallint, " +
            		"starttime timestamp, " +
            		"stoptime timestamp, " +
            		"filter tinyint, " +
            		"iofcif tinyint," +
            		"target_center_distance double," +
            		"horizontal_pixel_scale double" +
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

            //make an empty table
            //
            // by declaring the id column IDENTITY, the db will automatically
            // generate unique values for new rows- useful for row keys
            db.update(
            		"create cached table nisspectra(" +
            		"id int, " +
            		"year smallint, " +
            		"day smallint, " +
            		"starttime timestamp, " +
            		"duration double, " +
            		"polygon_type_flag tinyint)"
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
    
    static void populateMSITables(ArrayList<String> msiFiles) throws IOException, SQLException
    {
    	for (String filename : msiFiles)
    	{
    		int iof_or_cif = -1;
    		String dayOfYearStr = "";
    		String yearStr = "";

    		//String fullpath = image.getFullPath();
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

    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		System.out.println("iof_or_cif: " + iof_or_cif);

    		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
    		HashMap<String, String> properties = NearImage.parseLblFile(lblFilename);

            if (msiInsert == null)
            {
            	msiInsert = db.preparedStatement(                                                                                    
            		"insert into msiimages values (?, ?, ?, ?, ?, ?, ?, ?, ?)");                                                                   
            }
            
            msiInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            msiInsert.setInt(2, Integer.parseInt(yearStr));
            msiInsert.setInt(3, Integer.parseInt(dayOfYearStr));
            msiInsert.setTimestamp(4, Timestamp.valueOf(properties.get(NearImage.START_TIME)));
            msiInsert.setTimestamp(5, Timestamp.valueOf(properties.get(NearImage.STOP_TIME)));
            msiInsert.setInt(6, Integer.parseInt(origFile.getName().substring(12, 13)));
            msiInsert.setInt(7, iof_or_cif);
            msiInsert.setDouble(8, Double.parseDouble(properties.get(NearImage.TARGET_CENTER_DISTANCE)));
            msiInsert.setDouble(9, Double.parseDouble(properties.get(NearImage.HORIZONTAL_PIXEL_SCALE)));
            
            msiInsert.executeUpdate();
    	}
    }
    
    static void populateNISTables(ArrayList<String> nisFiles) throws SQLException
    {
    	for (String filename : nisFiles)
    	{
    		int iof_or_cif = -1;
    		String dayOfYearStr = "";
    		String yearStr = "";

    		//String fullpath = image.getFullPath();
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

    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		System.out.println("iof_or_cif: " + iof_or_cif);

    		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
    		HashMap<String, String> properties = null;//NearImage.parseLblFile(lblFilename);

    		if (nisInsert == null)
    		{
    			nisInsert = db.preparedStatement(                                                                                    
    					"insert into nisspectra values (?, ?, ?, ?, ?, ?, ?, ?, ?)");                                                                   
    		}

    		nisInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
    		nisInsert.setInt(2, Integer.parseInt(yearStr));
    		nisInsert.setInt(3, Integer.parseInt(dayOfYearStr));
    		nisInsert.setTimestamp(4, Timestamp.valueOf(properties.get(NearImage.START_TIME)));
    		nisInsert.setTimestamp(5, Timestamp.valueOf(properties.get(NearImage.STOP_TIME)));
    		nisInsert.setInt(6, Integer.parseInt(origFile.getName().substring(12, 13)));
    		nisInsert.setInt(7, iof_or_cif);
    		nisInsert.setDouble(8, Double.parseDouble(properties.get(NearImage.TARGET_CENTER_DISTANCE)));
    		nisInsert.setDouble(9, Double.parseDouble(properties.get(NearImage.HORIZONTAL_PIXEL_SCALE)));

    		nisInsert.executeUpdate();
    	}
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		String msiFileList="/media/KANGURU2.0/near/data/filelist.txt";
		//String nisFiles="/media/KANGURU2.0/near/data/filelist.txt";
		//String nlrFiles="/media/KANGURU2.0/near/data/filelist.txt";

		//ArrayList<String> msiFiles = FileUtil.getFileAsStringList(msiFileList);
		//ArrayList<String> nisFiles = FileUtil.getFileAsStringList(msiFileList);
		
        try {
            db = new SqlManager("db/1/near");
        } catch (Exception ex1) {
            ex1.printStackTrace();
            return;
        }

		createMSITables();
		//populateMSITables(msiFiles);
		
		createNISTables();
		//populateNISTables(nisFiles);
		
		
        // at end of program
        try {
			db.shutdown();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
