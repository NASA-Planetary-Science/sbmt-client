package edu.jhuapl.near.dbgen;

import java.io.*;
import java.util.*;

import edu.jhuapl.near.*;

import nom.tam.fits.FitsException;

public class MSIDatabaseGenerator 
{
	double cellSize = 10.0;
	Cell[][] cells;
	
	static class StringPair
	{
		String s1;
		String s2;
	}

	static class Pixel
	{
		int row;
		int col;
	}
	
	static class LatLon
	{
		double lat;
		double lon;
		LatLon(double lat, double lon)
		{
			this.lat = lat;
			this.lon = lon;
		}
	}
	
	static class ImageInfo
	{
		String path; // E.g. /2000/012/iofdbl/M0123063737F4_2P_IOF_DBL.LBL
		//double[] scPos;
		//double[] scDirection;
		ArrayList<LatLon> boundary = new ArrayList<LatLon>();
		int year; // 2000 or 2001
		int day; // day of year
		String start_time; // time image was taken
		String stop_time; // time image was taken
		int filter; // 1 through 7
		int iof_cif; // 0 for iofdbl, 1 for cifdbl
		
		public String toString()
		{
			String s = path + " ";
			s += year + " ";
			s += day + " ";
			s += start_time + " ";
			s += stop_time + " ";
			s += filter + " ";
			s += iof_cif + " ";
			s += boundary.size() + " ";
			
			for(int i=0; i<boundary.size(); ++i)
			{
				LatLon ll = boundary.get(i);
				s += ll.lat + " " + ll.lon + " ";
			}

			return s;
		}
	}

	static class Cell
	{
		ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
		int cellId;
		int row;
		int col;
		double latMin;
		double lonMin;
		double latMax;
		double lonMax;
		
		public String toString()
		{
			String s = 
				"cellId: " + cellId +
				"  row: " + row +
				"  col: " + col +
				"  latMin: " + latMin +
				"  latMax: " + latMax +
				"  lonMin: " + lonMin +
				"  lonMax: " + lonMax;
			return s;
		}
	}


	ArrayList<LatLon> getImageBorder(NearImage image)
	{
		ArrayList<LatLon> pixels = new ArrayList<LatLon>();
		
		// Start out at the top left and move to the right and shoot out a ray
		// downward till we hit a valid pixel
		
		int farthestDown = 0;
		int farthestLeft = NearImage.IMAGE_WIDTH-1;
		int farthestUp = NearImage.IMAGE_HEIGHT-1;
		//int farthestRight = 0;
		
		// Top
		for (int i=0; i<NearImage.IMAGE_WIDTH; ++i)
		{
			for (int j=0; j<NearImage.IMAGE_HEIGHT; ++j)
			{
				float lat = image.getLatitude(j, i);
				float lon = image.getLongitude(j, i);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					farthestDown = j+1;
					break;
				}
			}
		}
		// If nothing was found, we can return
		if (pixels.size() == 0)
			return pixels;
		
		// Right
		for (int i=farthestDown; i<NearImage.IMAGE_HEIGHT; ++i)
		{
			for (int j=NearImage.IMAGE_WIDTH-1; j>=0; --j)
			{
				float lat = image.getLatitude(i, j);
				float lon = image.getLongitude(i, j);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					farthestLeft = j-1;
					break;
				}
			}
		}
		// Bottom
		for (int i=farthestLeft; i>=0; --i)
		{
			for (int j=NearImage.IMAGE_HEIGHT-1; j>=0; --j)
			{
				float lat = image.getLatitude(j, i);
				float lon = image.getLongitude(j, i);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					farthestUp = j-1;
					break;
				}
			}
		}
		// Left
		for (int i=farthestUp; i>=0; --i)
		{
			for (int j=0; j<NearImage.IMAGE_WIDTH; ++j)
			{
				float lat = image.getLatitude(i, j);
				float lon = image.getLongitude(i, j);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					//farthestRight = j+1;
					break;
				}
			}
		}
		
		return pixels;
	}
	
	Cell getCellThatIntersectsPoint(double lat, double lon)
	{
		 int row = (int)Math.floor((lat + 90.0) / cellSize);
		 int col = (int)Math.floor((lon + 180.0) / cellSize);
		 return cells[row][col];
	}
	
	ArrayList<Cell> getCellsThatIntersectImage(NearImage image)
	{
		HashSet<Cell> cellsThatIntersect = new HashSet<Cell>();
		
		for (int i=0; i<NearImage.IMAGE_WIDTH; ++i)
			for (int j=0; j<NearImage.IMAGE_HEIGHT; ++j)
			{
				float lat = image.getLatitude(j, i);
				float lon = image.getLongitude(j, i);
				if (isValidPixel(lat, lon))
				{
					Cell cell = this.getCellThatIntersectsPoint(lat, lon);
					cellsThatIntersect.add(cell);
				}
			}
		return new ArrayList<Cell>(cellsThatIntersect);
	}
	
    private boolean isValidPixel(float x, float y)
    {
    	if (x <= NearImage.PDS_NA || y <= NearImage.PDS_NA)
    		return false;
    	else
    		return true;
    }

    StringPair getImageStartStopTime(NearImage image) throws IOException
    {
    	// Parse through the lbl file till we find the strings "START_TIME"                                                                 
    	// and "STOP_TIME"
    	String START_TIME = "START_TIME";
    	String STOP_TIME = "STOP_TIME";
    	
    	StringPair startStop = new StringPair();
    	String filename = image.getFullPath();
    	
    	String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";

		FileInputStream fs = null;
		try {
			fs = new FileInputStream(lblFilename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		String str;
		while ((str = in.readLine()) != null)
		{
			boolean breakedOut = false;
			
		    StringTokenizer st = new StringTokenizer(str);
		    while (st.hasMoreTokens()) 
		    {
		    	String token = st.nextToken();
		    	if (START_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		startStop.s1 = st.nextToken();
		    	}
		    	if (STOP_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		startStop.s2 = st.nextToken();
		    		breakedOut = true;
		    		break;
		    	}
		    }

		    if (breakedOut)
		    	break;
		}
		
    	
    	return startStop;
    }
    
    ImageInfo getImageInfo(NearImage image, String path) throws IOException
    {
    	ImageInfo info = new ImageInfo();
		
    	info.path = path;
    	
    	if (path.contains("F1"))
    		info.filter = 1;
    	else if (path.contains("F2"))
    		info.filter = 2;
    	else if (path.contains("F3"))
    		info.filter = 3;
    	else if (path.contains("F4"))
    		info.filter = 4;
    	else if (path.contains("F5"))
    		info.filter = 5;
    	else if (path.contains("F6"))
    		info.filter = 6;
    	else if (path.contains("F7"))
    		info.filter = 7;
		
    	if (path.contains("CIF"))
    		info.iof_cif = 1;
    	else
    		info.iof_cif = 0;
    	
    	info.boundary = this.getImageBorder(image);
    	
    	info.year = Integer.parseInt(path.substring(1, 5));
    	info.day = Integer.parseInt(path.substring(6, 9));
    	
    	StringPair startStop = this.getImageStartStopTime(image);
    	info.start_time = startStop.s1;
    	info.stop_time = startStop.s2;
    	
    	return info;
    }

    void writeDatabase(String rootDir) throws IOException
    {
    	(new File(rootDir)).mkdirs();
		int rows = (int)(180.0/cellSize);
		int cols = (int)(360.0/cellSize);
    	for (int i=0; i<rows; ++i)
    	{
    		String currentDir = rootDir+"/"+i;
    		(new File(currentDir)).mkdirs();
    		for (int j=0; j<cols; ++j)
    		{
    			Cell cell = cells[i][j];
    			ArrayList<ImageInfo> infos = cell.imageInfos;
    			
    			// Create a new file
    			String filename = currentDir+"/"+j+".txt";
    			BufferedWriter out = new BufferedWriter(new FileWriter(filename));

				if (infos.size() > 0)
					System.out.println(filename);

    			for (int k=0; k<infos.size(); ++k)
    			{
    				out.write(infos.get(k).toString());
    				out.write("\n");
    			}
				out.close();
    		}
    	}
    }
    
    public MSIDatabaseGenerator(String msiFiles) 
    {
		
		// Create the cell grid
		
		int rows = (int)(180.0/cellSize);
		int cols = (int)(360.0/cellSize);
		int cellId = 0;

		cells = new Cell[rows][cols];

		for (int i=0; i<rows; ++i)
		{
			for (int j=0; j<cols; ++j)
			{
				Cell cell = new Cell();
				cell.row = i;
				cell.col = j;
				cell.cellId = cellId;
				
				cell.lonMin = cellSize * j - 180.0;
				cell.lonMax = cellSize * (j+1) - 180.0;
				cell.latMin = cellSize * (rows-i-1) - 90.0;
				cell.latMax = cellSize * (rows-i) - 90.0;
				
				System.out.println(cell);
				++cellId;
				cells[i][j] = cell;
			}
		}
		
		
		// Read in a list of files which we need to process
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(msiFiles);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		
		String imageBaseDir = "/media/KANGURU2.0/near/data/test";
		String line;
        try 
        {
			while ((line = in.readLine()) != null)
			{
				NearImage image = new NearImage(imageBaseDir+line);
				
				ImageInfo info = getImageInfo(image, line);
				
				ArrayList<Cell> cellsThatIntersect = this.getCellsThatIntersectImage(image);
				for (Cell c : cellsThatIntersect)
				{
					c.imageInfos.add(info);
				}
			}
			
			// Now write out the database
			this.writeDatabase("/home/eli/tmp/msidb");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FitsException e) {
			e.printStackTrace();
		}
	
	}
    
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibraries();

		String msiFiles="/media/KANGURU2.0/near/data/filelist.txt";// = args[0];

    	new MSIDatabaseGenerator(msiFiles);
	}

}
