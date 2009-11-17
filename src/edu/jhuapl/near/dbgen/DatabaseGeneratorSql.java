package edu.jhuapl.near.dbgen;

import java.io.*;
import java.sql.SQLException;
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
            		"filter smallint, " +
            		"iofcif smallint)"
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
    
    static void populateMSITables(ArrayList<String> msiFiles)
    {
    }
    
    static void populateNISTables(ArrayList<String> nisFiles)
    {
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
