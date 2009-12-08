package edu.jhuapl.near.dbgen;

import java.io.*;
import java.util.*;

import vtk.*;

import edu.jhuapl.near.*;
import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.NativeLibraryLoader;

import nom.tam.fits.FitsException;

public class MSIDatabaseGenerator 
{
	double cellSize = 10.0;
	Cell[][] cells;
	
	/*
	static class ImageInfo
	{
		String name; // E.g. /2000/012/iofdbl/M0123063737F4_2P_IOF_DBL.LBL
		//double[] scPos;
		//double[] scDirection;
		vtkPolyData boundary = null;
		int year; // 2000 or 2001
		int day; // day of year
		String start_time; // time image was taken
		String stop_time; // time image was taken
		int filter; // 1 through 7
		int iof_cif; // 0 for iofdbl, 1 for cifdbl
		
		public String toString()
		{
			String s = name + " ";
			s += year + " ";
			s += day + " ";
			s += start_time + " ";
			s += stop_time + " ";
			s += filter + " ";
			s += iof_cif + " ";
//			s += boundary.size() + " ";
//			
//			for(int i=0; i<boundary.size(); ++i)
//			{
//				LatLon ll = boundary.get(i);
//				s += ll.lat + " " + ll.lon + " ";
//			}

			return s;
		}
	}
	 */
	
	static class Cell
	{
		//ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
		
		// Images that completely cover this cell
		ArrayList<String> imagesFull = new ArrayList<String>();

		// Images that are do not completely cover this cell whether or
		// not they are completely contained in this cell or span multiple cells.
		ArrayList<String> imagesPartial = new ArrayList<String>();

		int cellId;
		int row;
		int col;
		double latMin;
		double lonMin;
		double latMax;
		double lonMax;
		
		/*
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
		*/
	}

	
	Cell getCellThatIntersectsPoint(double lat, double lon)
	{
		 int row = (int)Math.floor((lat + 90.0) / cellSize);
		 int col = (int)Math.floor((lon + 180.0) / cellSize);
		 return cells[row][col];
	}
	
	ArrayList<Cell> getCellsThatIntersectImagePartially(NearImage image)
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

	ArrayList<Cell> getCellsThatIntersectImageFully(
			NearImage image, 
			ArrayList<Cell> partialCells)
	{
		HashSet<Cell> cellsThatIntersect = new HashSet<Cell>();

		//double[] lon = new doubl
		
		Iterator<Cell> it = cellsThatIntersect.iterator(); 
		while(it.hasNext()) 
		{
			Cell cell = it.next();
			
			int size = (int)(this.cellSize/1.0);
			for (int i=0; i<size; ++i)
				for (int j=0; j<size; ++j)
				{
					double plat;
					double plon;
					//PointInPolygon.pointInPolygonGeo(plon, plat, lon, lat)
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
	
	void addImageToDataStructure(NearImage image) throws IOException
	{
		//ArrayList<Cell> cellsThatIntersect = this.getCellsThatIntersectImage(image);
		//for (Cell c : cellsThatIntersect)
		{
			//c.imageInfos.add(info);
		}
	}
	
    /*
    ImageInfo getImageInfo(NearImage image) throws IOException
    {
    	ImageInfo info = new ImageInfo();
		
    	String name = (new File(image.getFullPath())).getName();
    	info.name = name;
    	
    	if (name.contains("F1"))
    		info.filter = 1;
    	else if (name.contains("F2"))
    		info.filter = 2;
    	else if (name.contains("F3"))
    		info.filter = 3;
    	else if (name.contains("F4"))
    		info.filter = 4;
    	else if (name.contains("F5"))
    		info.filter = 5;
    	else if (name.contains("F6"))
    		info.filter = 6;
    	else if (name.contains("F7"))
    		info.filter = 7;
		
    	if (name.contains("CIF"))
    		info.iof_cif = 1;
    	else
    		info.iof_cif = 0;
    	
    	info.boundary = image.getImageBorder();
    	
    	//info.year = Integer.parseInt(path.substring(1, 5));
    	//info.day = Integer.parseInt(path.substring(6, 9));
    	
    	StringPair startStop = image.getImageStartStopTime();
    	info.start_time = startStop.s1;
    	info.stop_time = startStop.s2;
    	
    	return info;
    }
     */
    /*
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
    */
    
    void writeDatabaseXml(String rootDir) throws IOException
    {
		int rows = (int)(180.0/cellSize);
		int cols = (int)(360.0/cellSize);
    	for (int i=0; i<rows; ++i)
    	{
    		String currentDir = rootDir+"/"+i;
    		(new File(currentDir)).mkdirs();
    		for (int j=0; j<cols; ++j)
    		{
    			Cell cell = cells[i][j];
    			//ArrayList<ImageInfo> infos = cell.imageInfos;
    			
    			// Create a new file
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

		String line = "";
        try 
        {
			while ((line = in.readLine()) != null)
			{
				NearImage image = new NearImage(line);
				
				addImageToDataStructure(image);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FitsException e) {
			e.printStackTrace();
		}

		// Now write out the database
		try 
		{
			this.writeDatabaseXml((new File(msiFiles)).getParent() + "/msiSpatialDb.xml");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}


	}
    
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		String msiFiles="/media/KANGURU2.0/near/data/filelist.txt";// = args[0];

    	new MSIDatabaseGenerator(msiFiles);
	}

}
