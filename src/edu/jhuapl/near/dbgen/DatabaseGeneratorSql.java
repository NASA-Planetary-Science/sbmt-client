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

import vtk.vtkPolyData;

public class DatabaseGeneratorSql 
{
	private static class ErosCells
	{
		public BoundingBox erosBB;
		ArrayList<BoundingBox> allCells = new ArrayList<BoundingBox>();
		double cellSize = 1.0;
		int numCellsX;
		int numCellsY;
		int numCellsZ;
		
		public ErosCells(ErosModel eros)
		{
			erosBB = eros.computeBoundingBox();
			numCellsX = (int)(Math.ceil(erosBB.xmax - erosBB.xmin) / cellSize);
			numCellsY = (int)(Math.ceil(erosBB.ymax - erosBB.ymin) / cellSize);
			numCellsZ = (int)(Math.ceil(erosBB.zmax - erosBB.zmin) / cellSize);
			
			for (int k=0; k<numCellsZ; ++k)
			{
				double zmin = k * cellSize;
				double zmax = (k+1) * cellSize;
				for (int j=0; j<numCellsY; ++j)
				{
					double ymin = j * cellSize;
					double ymax = (j+1) * cellSize;
					for (int i=0; i<numCellsX; ++i)
					{
						double xmin = i * cellSize;
						double xmax = (i+1) * cellSize;
						BoundingBox bb = new BoundingBox();
						bb.xmin = xmin;
						bb.xmax = xmax;
						bb.ymin = ymin;
						bb.ymax = ymax;
						bb.zmin = zmin;
						bb.zmax = zmax;
						allCells.add(bb);
					}
				}
			}
		}
		
		public BoundingBox getCellBoundingBox(int cellId)
		{
			return allCells.get(cellId);
		}
		
		public TreeSet<Integer> getIntersectingCells(NISSpectrum spectrum)
		{
			TreeSet<Integer> cellIds = new TreeSet<Integer>();

			// Iterate through each cell and check if it intersects
			// with the bounding box of any of the polygons of the footprint
			vtkPolyData footprint = spectrum.getFootprint();
			BoundingBox spectrumBB = new BoundingBox(footprint.GetBounds());
			double[] bounds = new double[6];
			
			int numberCells = numCellsX * numCellsY * numCellsZ;
			for (int i=0; i<numberCells; ++i)
			{
				// Before checking each polygon individually, first see if the
				// footprint as a whole intersects the cell
				BoundingBox cellBB = getCellBoundingBox(i);
				if (cellBB.intersects(spectrumBB))
				{
					int numberPolygons = footprint.GetNumberOfCells();
					for (int j=0; j<numberPolygons; ++j)
					{
						footprint.GetCellBounds(j, bounds);
						BoundingBox polyBB = new BoundingBox(bounds);
						if (cellBB.intersects(polyBB))
						{
							cellIds.add(i);
							break;
						}
					}
				}
			}
			
			return cellIds;
		}

		/*
		public int getCellId(double[] pt)
		{
			double x = pt[0];
			double y = pt[1];
			double z = pt[2];
			
			return (int)Math.floor((x - erosBB.xmin) / cellSize) +
			(int)Math.floor((y - erosBB.ymin) / cellSize)*numCellsX +
			(int)Math.floor((z - erosBB.zmin) / cellSize)*numCellsX*numCellsY; 
		}
		
		public TreeSet<Integer> getIntersectingCells(NISSpectrum spectrum)
		{
			TreeSet<Integer> cellIds = new TreeSet<Integer>();
			
			vtkPoints points = spectrum.getFootprint().GetPoints();
			int numberPoints = points.GetNumberOfPoints();
			for (int i=0; i<numberPoints; ++i)
			{
				double[] pt = points.GetPoint(i);
				cellIds.add(getCellId(pt));
			}
			
			return cellIds;
		}
		*/
		/*
		public TreeSet<Integer> getIntersectingCells(MSIImage image)
		{
			TreeSet<Integer> cellIds = new TreeSet<Integer>();
			
			for (int i=0; i<MSIImage.IMAGE_HEIGHT; ++i)
				for (int j=0; j<MSIImage.IMAGE_WIDTH; ++j)
				{
					double x = image.getX(i, j);
					double y = image.getY(i, j);
					double z = image.getZ(i, j);
					double[] pt = {x, y, z};
					cellIds.add(getCellId(pt));
				}
			
			return cellIds;
		}
		*/
	}
	
	static SqlManager db = null;
	static PreparedStatement msiInsert = null;
	static PreparedStatement nisInsert = null;
	static PreparedStatement nisInsert2 = null;
	static ErosModel erosModel;
	static ErosCells erosCells;
	
    private static void createMSITables()
    {
    	System.out.println("creating msi");
        try {

            //make a table
        	try
        	{
        		db.dropTable("msiimages");
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        	
            db.update(
            		"create table msiimages(" +
            		"id int PRIMARY KEY, " +
            		"year smallint, " +
            		"day smallint, " +
            		"starttime bigint, " +
            		"stoptime bigint, " +
            		"filter tinyint, " +
            		"iofcif tinyint," +
            		"target_center_distance double," +
            		"horizontal_pixel_scale double," +
            		"has_limb boolean," +
            		"minincidence double," +
            		"maxincidence double," +
            		"minemission double," +
            		"maxemission double," +
            		"minphase double," +
            		"maxphase double" +
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
    
    private static void createNISTables()
    {
        try {

            //make a table
        	try
        	{
            	db.dropTable("nisspectra");
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}

        	db.update(
            		"create table nisspectra(" +
            		"id int PRIMARY KEY, " +
            		"year smallint, " +
            		"day smallint, " +
            		"midtime bigint, " +
            		"minincidence double," +
            		"maxincidence double," +
            		"minemission double," +
            		"maxemission double," +
            		"minphase double," +
            		"maxphase double," +
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
    
    private static void createNISTables2()
    {
        try {

            //make a table
        	try
        	{
            	db.dropTable("niscells");
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}

        	db.update(
            		"create table niscells(" +
            		"id int PRIMARY KEY, " +
            		"nisspectrumid int, " +
            		"cellid int)"
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
    
    private static void populateMSITables(ArrayList<String> msiFiles) throws IOException, SQLException, FitsException
    {
    	int count = 0;
    	
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

    		MSIImage image = new MSIImage(origFile);
    		//HashMap<String, String> properties = image.getProperties();

    		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
    		HashMap<String, String> properties = MSIImage.parseLblFile(lblFilename);
    		
            if (msiInsert == null)
            {
            	msiInsert = db.preparedStatement(                                                                                    
            		"insert into msiimages values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");                                                                   
            }

            DateTime startTime = new DateTime(properties.get(MSIImage.START_TIME), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(properties.get(MSIImage.STOP_TIME), DateTimeZone.UTC);
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
    		System.out.println("TARGET_CENTER_DISTANCE: " + properties.get(MSIImage.TARGET_CENTER_DISTANCE));
    		System.out.println("HORIZONTAL_PIXEL_SCALE: " + properties.get(MSIImage.HORIZONTAL_PIXEL_SCALE));
    		System.out.println("hasLimb: " + image.containsLimb());
    		System.out.println("minIncidence: " + image.getMinIncidence());
    		System.out.println("maxIncidence: " + image.getMaxIncidence());
    		System.out.println("minEmission: " + image.getMinEmission());
    		System.out.println("maxEmission: " + image.getMaxEmission());
    		System.out.println("minPhase: " + image.getMinPhase());
    		System.out.println("maxPhase: " + image.getMaxPhase());
    		System.out.println(" ");

            msiInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            msiInsert.setShort(2, Short.parseShort(yearStr));
            msiInsert.setShort(3, Short.parseShort(dayOfYearStr));
            msiInsert.setLong(4, startTime.getMillis());
            msiInsert.setLong(5, stopTime.getMillis());
            msiInsert.setByte(6, Byte.parseByte(origFile.getName().substring(12, 13)));
            msiInsert.setByte(7, iof_or_cif);
            msiInsert.setDouble(8, Double.parseDouble(properties.get(MSIImage.TARGET_CENTER_DISTANCE)));
            msiInsert.setDouble(9, Double.parseDouble(properties.get(MSIImage.HORIZONTAL_PIXEL_SCALE)));
            msiInsert.setBoolean(10, image.containsLimb());
    		msiInsert.setDouble(11, image.getMinIncidence());
    		msiInsert.setDouble(12, image.getMaxIncidence());
    		msiInsert.setDouble(13, image.getMinEmission());
    		msiInsert.setDouble(14, image.getMaxEmission());
    		msiInsert.setDouble(15, image.getMinPhase());
    		msiInsert.setDouble(16, image.getMaxPhase());
            
            msiInsert.executeUpdate();
    	}
    }
    
    private static void populateNISTables(ArrayList<String> nisFiles) throws SQLException, IOException
    {
    	int count = 0;
    	for (String filename : nisFiles)
    	{
			System.out.println("starting nis " + count++);
			
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
    					"insert into nisspectra values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");                                                                   
    		}

    		DateTime midtime = new DateTime(nisSpectrum.getDateTime().toString(), DateTimeZone.UTC);
    		// Replace the "T" with a space
    		//time = time.substring(0, 10) + " " + time.substring(11, time.length());
    		
    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		System.out.println("midtime: " + midtime);
    		System.out.println("minIncidence: " + nisSpectrum.getMinIncidence());
    		System.out.println("maxIncidence: " + nisSpectrum.getMaxIncidence());
    		System.out.println("minEmission: " + nisSpectrum.getMinEmission());
    		System.out.println("maxEmission: " + nisSpectrum.getMaxEmission());
    		System.out.println("minPhase: " + nisSpectrum.getMinPhase());
    		System.out.println("maxPhase: " + nisSpectrum.getMaxPhase());
    		System.out.println("range: " + nisSpectrum.getRange());
    		System.out.println("polygon type: " + nisSpectrum.getPolygonTypeFlag());
    		System.out.println(" ");
    		

    		nisInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
    		nisInsert.setShort(2, Short.parseShort(yearStr));
    		nisInsert.setShort(3, Short.parseShort(dayOfYearStr));
    		nisInsert.setLong(4, midtime.getMillis());
    		nisInsert.setDouble(5, nisSpectrum.getMinIncidence());
    		nisInsert.setDouble(6, nisSpectrum.getMaxIncidence());
    		nisInsert.setDouble(7, nisSpectrum.getMinEmission());
    		nisInsert.setDouble(8, nisSpectrum.getMaxEmission());
    		nisInsert.setDouble(9, nisSpectrum.getMinPhase());
    		nisInsert.setDouble(10, nisSpectrum.getMaxPhase());
    		nisInsert.setDouble(11, nisSpectrum.getRange());
    		nisInsert.setShort(12, nisSpectrum.getPolygonTypeFlag());

    		nisInsert.executeUpdate();
    	}
    }

    private static void populateNISTables2(ArrayList<String> nisFiles) throws SQLException, IOException
    {
    	int count = 0;
    	for (String filename : nisFiles)
    	{
			System.out.println("starting nis " + count);
			
//    		String dayOfYearStr = "";
//    		String yearStr = "";

    		File origFile = new File(filename);
//    		File f = origFile;

//    		f = f.getParentFile();
//    		dayOfYearStr = f.getName();

//    		f = f.getParentFile();
//    		yearStr = f.getName();


    		NISSpectrum nisSpectrum = new NISSpectrum(origFile, erosModel);
    		
    		if (nisInsert2 == null)
    		{
    			nisInsert2 = db.preparedStatement(                                                                                    
    					"insert into nisspectra values (?, ?, ?)");
    		}

    		TreeSet<Integer> cellIds = erosCells.getIntersectingCells(nisSpectrum);
    		for (Integer i : cellIds)
    		{
        		System.out.println("id: " + count);
        		System.out.println("nis id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
        		System.out.println("cellId: " + i);
        		
    			nisInsert.setInt(1, count);
        		nisInsert.setInt(2, Integer.parseInt(origFile.getName().substring(2, 11)));
        		nisInsert.setInt(3, i);

    			nisInsert.executeUpdate();

    			++count;
    		}
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

		
        //createMSITables();
		//createNISTables();
		createNISTables2();

		
		try 
		{
			//populateMSITables(msiFiles);
			//populateNISTables(nisFiles);
			populateNISTables2(nisFiles);
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
