package edu.jhuapl.near.dbgen;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.SmallBodyCubes;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This program goes through all the NLR data and creates an sql database
 * containing all the data. 
 * 
 * The database consists of the following columns
 * 
 * 1.  id
 * 2.  longitudeE 
 * 3.  latitudeN
 * 4.  Eros_rad
 * 5.  ET_J2000
 * 6.  UTC
 * 7.  Range
 * 8.  t
 * 9.  a
 * 10. SClon
 * 11. SClat
 * 12. SCrdst
 * 13. Emission
 * 14. Offnadir
 * 15. SCLCKCH
 * 16. Eros_x
 * 17. Eros_y
 * 18. Eros_z
 * 19. Omega
 * 20. U
 * 21. filename
 * 22. cube_id
 * 
 * divides all the data
 * up into cubes and saves each cube to a separate file.
 * @author kahneg1
 *
 */
public class NLRSqlDatabaseGenerator
{
	static private SqlManager db = null;

	static private void createTable()
	{
    	System.out.println("creating msi");
        try {

            //make a table
        	try
        	{
        		db.dropTable("nlr");
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        	}
        	
            db.update(
            		"create table nlr(" +
            		"UTC bigint PRIMARY KEY, " +
            		"longitudeE real, " +
            		"latitudeN real, " +
            		"Eros_rad real, " +
            		"ET_J2000 double, " +
            		"Range real," +
            		"t tinyint," +
            		"a tinyint," +
            		"SClon real," +
            		"SClat real," +
            		"SCrdst real," +
            		"Emission real," +
            		"Offnadir real," +
            		"SCLCKCH double," +
            		"Eros_x real," +
            		"Eros_y real," +
            		"Eros_z real," +
            		"Omega real," +
            		"U real," +
            		"cube_id int" +
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
	
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibraries();

		SmallBodyModel erosModel = ModelFactory.createErosBodyModel();

		String nlrFileList = args[0];
		
		ArrayList<String> nlrFiles = null;
		try {
			nlrFiles = FileUtil.getFileLinesAsStringList(nlrFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		SmallBodyCubes cubes = new SmallBodyCubes(erosModel.getSmallBodyPolyData(), 1.0, 1.0, true);
		
		double[] pt = new double[3];
		
		// If a point is farther than MAX_DIST from the asteroid, then set its cubeid to -1.
		final double MAX_DIST = 1.0;
		
		PreparedStatement msiInsert = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-DDD'T'HH:mm:ss.SSS", Locale.US);
		
		try
		{
			db = new SqlManager("org.h2.Driver", "jdbc:h2:~/tmp/test-h2/nlr");

			createTable();
			
	    	msiInsert = db.preparedStatement(
    		"insert into nlr values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

	    	int count = 0;
	    	short filecount = 0;
			for (String filename : nlrFiles)
			{
				
			    System.out.println("Begin processing file " + filename + " - " + count + " / " + nlrFiles.size());
			    
			    ArrayList<String> lines = FileUtil.getFileLinesAsStringList(filename);
			    
				for (int i=2; i<lines.size(); ++i)
				{
					String[] vals = lines.get(i).trim().split("\\s+");

	       			pt[0] = Double.parseDouble(vals[14])/1000.0;
        			pt[1] = Double.parseDouble(vals[15])/1000.0;
        			pt[2] = Double.parseDouble(vals[16])/1000.0;

        			double[] closestPt = erosModel.findClosestPoint(pt);
        			
        			double dist = MathUtil.distanceBetween(pt, closestPt);

        			int cubeid = -1;
        			
        			if (dist <= MAX_DIST)
        			{
        				cubeid = cubes.getCubeId(closestPt);
        			}
        			
                    msiInsert.setLong(1, sdf.parse(vals[4]).getTime());
                    msiInsert.setFloat(2, Float.parseFloat(vals[0]));
                    msiInsert.setFloat(3, Float.parseFloat(vals[1]));
                    msiInsert.setFloat(4, Float.parseFloat(vals[2]));
                    msiInsert.setDouble(5, Double.parseDouble(vals[3]));
                    msiInsert.setFloat(6, Float.parseFloat(vals[5]));
                    msiInsert.setByte(7, Byte.parseByte(vals[6]));
                    msiInsert.setShort(8, Short.parseShort(vals[7]));
                    msiInsert.setFloat(9, Float.parseFloat(vals[8]));
                    msiInsert.setFloat(10, Float.parseFloat(vals[9]));
                    msiInsert.setFloat(11, Float.parseFloat(vals[10]));
                    msiInsert.setFloat(12, Float.parseFloat(vals[11]));
                    msiInsert.setFloat(13, Float.parseFloat(vals[12]));
                    msiInsert.setDouble(14, Double.parseDouble(vals[13]));
                    msiInsert.setFloat(15, Float.parseFloat(vals[14]));
                    msiInsert.setFloat(16, Float.parseFloat(vals[15]));
                    msiInsert.setFloat(17, Float.parseFloat(vals[16]));
                    msiInsert.setFloat(18, Float.parseFloat(vals[17]));
                    msiInsert.setFloat(19, Float.parseFloat(vals[18]));
                    msiInsert.setInt(20, cubeid);

                    msiInsert.executeUpdate();

					++count;
				}
				
				++filecount;
			}
			
			db.shutdown();
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
	}
}
