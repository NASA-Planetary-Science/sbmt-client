package edu.jhuapl.near.dbgen;

import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.NativeLibraryLoader;

import java.io.*;
import java.util.*;
import java.util.Properties;

import java.sql.*;


public class MSIDatabaseGeneratorSql 
{
	//private String framework = "embedded";                                                                                       
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";                                                              
    private String protocol = "jdbc:derby:";

	Connection conn = null;
    ResultSet rs = null;
	ArrayList<Statement> statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements                                        

	void addImageToDatabase(NearImage image) throws IOException, SQLException
	{
		int iof_or_cif = -1;
		String dayOfYearStr = "";
		String yearStr = "";
		
		String fullpath = image.getFullPath();
        File origFile = new File(fullpath);
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
        
    	StringPair startStopTimes = image.getImageStartStopTime();

        PreparedStatement psInsert = null;                                                                                       
    	psInsert = conn.prepareStatement(                                                                                    
    			"insert into msiimages values (?, ?, ?, ?, ?, ?, ?)");                                                                   
    	statements.add(psInsert);                                                                                            

    	psInsert.setInt(1, 123421956);                                                                                            
    	psInsert.setInt(2, 2000);                                                                                
    	psInsert.setInt(3, 43);                                                                                
    	psInsert.setTimestamp(4, Timestamp.valueOf(startStopTimes.s1));                                                                                
    	psInsert.setTimestamp(5, Timestamp.valueOf(startStopTimes.s2));                                                                                
    	psInsert.setInt(6, 4);                                                                                
    	psInsert.setInt(7, 0);                                                                                
    	psInsert.executeUpdate();                                                                                            

	}

    void createDatabase()
    {
    	loadDriver();
    	
        //PreparedStatement psInsert = null;                                                                                       
        //PreparedStatement psUpdate = null;                                                                                       
        Statement s = null;                                                                                                      
        
        try                                                                                                                      
        {                                                                                                                        
            Properties props = new Properties();                                                                                 
            props.put("user", "user1");                                                                                          
            props.put("password", "user1");                                                                                      
            
            String dbName = "nearDatabase";
            
            conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);
            
            s = conn.createStatement();                                                                                          
            statements.add(s);                                                                                                   
                                                                                                                                 
            // We create a table...                                                                                              
            s.execute("create table msiimages(" +
            		"id int, " +
            		"yearr smallint, " +
            		"day smallint, " +
            		"starttime timestamp, " +
            		"stoptime timestamp, " +
            		"filter smallint, " +
            		"iofcif smallint)"
            		);                                                       
            
            //psInsert = conn.prepareStatement(                                                                                    
            //"insert into msiimages values (?, ?, ?, ?, ?, ?, ?)");                                                                   
            //statements.add(psInsert);                                                                                            

            //psInsert.setInt(1, 123421956);                                                                                            
            //psInsert.setInt(2, 2000);                                                                                
            //psInsert.setInt(3, 43);                                                                                
            //psInsert.setTimestamp(4, Timestamp.valueOf("2000-22-03 11:34:23"));                                                                                
            //psInsert.setTimestamp(5, Timestamp.valueOf("2000-12-03 11:34:23"));                                                                                
            //psInsert.setInt(6, 4);                                                                                
            //psInsert.setInt(7, 0);                                                                                
            //psInsert.executeUpdate();                                                                                            
            //System.out.println("Inserted 1956 Webster");                                                                         

            //psInsert.setInt(1, 1910);                                                                                            
            //psInsert.setString(2, "Union St.");                                                                                  
            //psInsert.executeUpdate();                                                                                            
            //System.out.println("Inserted 1910 Union");   
            
            //psUpdate = conn.prepareStatement(                                                                                    
            //"update location set num=?, addr=? where num=?");                                                        
            //statements.add(psUpdate);                                                                                            

            //psUpdate.setInt(1, 180);                                                                                             
            //psUpdate.setString(2, "Grand Ave.");                                                                                 
            //psUpdate.setInt(3, 1956);                                                                                            
            //psUpdate.executeUpdate();                                                                                            
            //System.out.println("Updated 1956 Webster to 180 Grand");                                                             

            //psUpdate.setInt(1, 300);                                                                                             
            //psUpdate.setString(2, "Lakeshore Ave.");                                                                             
            //psUpdate.setInt(3, 180);                                                                                             
            //psUpdate.executeUpdate();                                                                                            
            //System.out.println("Updated 180 Grand to 300 Lakeshore");            
        }
        catch (Exception e)
        {
			e.printStackTrace();
        }
        
    }
    
    void shutdownDatabase()
    {
        try
        {
            // the shutdown=true attribute shuts down Derby
            DriverManager.getConnection("jdbc:derby:;shutdown=true");

            // To shut down a specific database only, but keep the
            // engine running (for example for connecting to other
            // databases), specify a database in the connection URL:
            //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
        }
        catch (SQLException se)
        {
            if (( (se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState()) ))) {
                // we got the expected exception
                System.out.println("Derby shut down normally");
                // Note that for single database shutdown, the expected
                // SQL state is "08006", and the error code is 45000.
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)
                System.err.println("Derby did not shut down normally");
                printSQLException(se);
            }
        }
    	

        // release all open resources to avoid unnecessary memory usage

        // ResultSet
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }

        // Statements and PreparedStatements
        int i = 0;
        while (!statements.isEmpty()) {
            // PreparedStatement extend Statement
            Statement st = (Statement)statements.remove(i);
            try {
                if (st != null) {
                    st.close();
                    st = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }

        //Connection
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }
    
    private void loadDriver() 
    {
        try {                                                                                                                    
            Class.forName(driver).newInstance();                                                                                 
            System.out.println("Loaded the appropriate driver");                                                                 
        } catch (ClassNotFoundException cnfe) {                                                                                  
            System.err.println("\nUnable to load the JDBC driver " + driver);                                                    
            System.err.println("Please check your CLASSPATH.");                                                                  
            cnfe.printStackTrace(System.err);                                                                                    
        } catch (InstantiationException ie) {                                                                                    
            System.err.println(                                                                                                  
                        "\nUnable to instantiate the JDBC driver " + driver);                                                    
            ie.printStackTrace(System.err);                                                                                      
        } catch (IllegalAccessException iae) {                                                                                   
            System.err.println(                                                                                                  
                        "\nNot allowed to access the JDBC driver " + driver);                                                    
            iae.printStackTrace(System.err);                                                                                     
        }                                                                                   
    }
    
    public static void printSQLException(SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }

	MSIDatabaseGeneratorSql(String msiFiles)
	{
		// Read in a list of files which we need to process
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(msiFiles);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		int numberOfFailures = 0;
		int count = 1;
		ArrayList<String> imageFailures = new ArrayList<String>();
		
		this.createDatabase();
		
		String line = "";
        try 
        {
			while ((line = in.readLine()) != null)
			{
				System.out.println("\n");
				System.out.println("Processing image " + count++);
				System.out.println(line);
				
				NearImage image = new NearImage(line);
				
				addImageToDatabase(image);
			}
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
			++numberOfFailures;
			if (line == null) line = "";
			imageFailures.add(line);
		} 

		// Now write out the database
		try 
		{
			this.shutdownDatabase();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		System.out.println("\n\n\n");
        System.out.println("Warning: " + numberOfFailures + " images could not be processed");
        System.out.println("They are:");
        for (String s: imageFailures)
        {
        	System.out.println(s);
        }
	}
	
	/**
	 * The purpose of this program is to generate an xml file containing a list
	 * of which images were taken per day in the mission, the start and stop time
	 * and the paths to the images.
	 * @param args name of file which contains a list of the full paths of
	 * all images which need to be indexed, one path per line.
	 */
	public static void main(String[] args) 
	{
		//System.setProperty("derby.system.home", "/tmp");

		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		String msiFiles = args[0];

    	new MSIDatabaseGeneratorSql(msiFiles);
	}

}
