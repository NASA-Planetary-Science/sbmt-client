package edu.jhuapl.near.model.eros;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.util.*;

import vtk.*;

public class NLRDataCollection2 extends Model 
{
	private vtkPolyData polydata;
	private ArrayList<NLRPoint> originalPoints = new ArrayList<NLRPoint>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkGeometryFilter geometryFilter;
    private vtkPolyDataMapper pointsMapper;
    private vtkActor actor;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-DDD'T'HH:mm:ss.SSS", Locale.US);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MMM-d HH:mm:ss.SSS", Locale.US);

	private double radialOffset = 0.0;
	
	private GregorianCalendar startDate;
	private GregorianCalendar stopDate;
	private TreeSet<Integer> cubeList;

	private HashMap<String, String> nlrDoyToPathMap = null;
	private ErosModel erosModel;
	private SmallBodyCubes smallBodyCubes;

	private int firstPointShown = -1;
	private int lastPointShown = -1;

	private boolean needToResetMask = true;
	
	public enum NLRMaskType
	{
		NONE,
		BY_NUMBER,
		BY_TIME,
		BY_DISTANCE
	}
	
	public static class NLRPoint implements Comparable<NLRPoint>
	{
		public double[] point;
		public Long time;
		public double potential;
		
		public NLRPoint(double[] point, long time, double potential)
		{
			this.point = point;
			this.time = time;
			this.potential = potential;
		}

		public int compareTo(NLRPoint o)
		{
			return time.compareTo(o.time);
		}
	}
	
	public NLRDataCollection2(ErosModel erosModel)
	{
		this.erosModel = erosModel;
		
		createDoyToPathMap();

		polydata = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray vert = new vtkCellArray();
		polydata.SetPoints( points );
		polydata.SetVerts( vert );

		geometryFilter = new vtkGeometryFilter();
		geometryFilter.SetInput(polydata);
		geometryFilter.PointClippingOn();
		geometryFilter.CellClippingOff();
		geometryFilter.ExtentClippingOff();
		geometryFilter.MergingOff();
	}

	public void setNlrData(
			GregorianCalendar startDate,
			GregorianCalendar stopDate,
			TreeSet<Integer> cubeList,
			NLRMaskType maskType,
			double maskValue,
			boolean reset) throws IOException, ParseException
	{
		loadNlrData(startDate, stopDate, cubeList);
		
		applyMask(maskType, maskValue, reset);
		
		geometryFilter.SetPointMinimum(firstPointShown);
		geometryFilter.SetPointMaximum(lastPointShown);
		
		if (pointsMapper == null)
		{
			pointsMapper = new vtkPolyDataMapper();
			pointsMapper.SetInput(geometryFilter.GetOutput());
		}

		if (actor == null)
		{
			actor = new vtkActor();
			actor.SetMapper(pointsMapper);
			actor.GetProperty().SetColor(0.0, 0.0, 1.0);
			actor.GetProperty().SetPointSize(2.0);
        
			actors.add(actor);
		}
		
		setRadialOffset(radialOffset);

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	private ArrayList<IdPair> getValidIntersectingDays(GregorianCalendar startDate, GregorianCalendar stopDate)
	{
		ArrayList<IdPair> validDays = new ArrayList<IdPair>();

		int yearStart = startDate.get(Calendar.YEAR);
		int doyStart = startDate.get(Calendar.DAY_OF_YEAR);
		int yearStop = stopDate.get(Calendar.YEAR);
		int doyStop = stopDate.get(Calendar.DAY_OF_YEAR);

		// Check for valid values. NLR data was only aquired from day 59 in year 2000
		// to day 43 in year 2001
		if (startDate.compareTo(stopDate) > 0 ||
				stopDate.compareTo(new GregorianCalendar(2000, 2, 28, 0, 0, 0)) < 0 ||
				startDate.compareTo(new GregorianCalendar(2001, 2, 13, 0, 0, 0)) >= 0)
		{
			return validDays;
		}
		
		if (yearStart < 2000)
		{
			yearStart = 2000;
			doyStart = 59;
		}
		if (doyStart < 59 && yearStart == 2000)
			doyStart = 59;
		
		if (yearStop > 2001)
		{
			yearStop = 2001;
			doyStop = 43;
		}
		if (doyStop > 43 && yearStop == 2001)
			doyStop = 43;
		
		int doy = doyStart;
		int year = yearStart;
		
		while(true)
		{
			validDays.add(new IdPair(doy, year));
			
			if (doy == doyStop && year == yearStop)
				break;
			
			++doy;
			
			if (year == 2000 && doy > 366)
			{
				doy = 1;
				year = 2001;
			}
		}
		
		return validDays;
	}
	
	@SuppressWarnings("unchecked")
	public void loadNlrData(
			GregorianCalendar startDate,
			GregorianCalendar stopDate,
			TreeSet<Integer> cubeList) throws IOException, ParseException
	{
		if (startDate.equals(this.startDate) &&
				stopDate.equals(this.stopDate) &&
				cubeList.equals(this.cubeList))
		{
			return;
		}
		
		// Make clones since otherwise the previous if statement might
		// evaluate to true even if something changed.
		this.startDate = (GregorianCalendar)startDate.clone();
		this.stopDate = (GregorianCalendar)stopDate.clone();
		this.cubeList = (TreeSet<Integer>)cubeList.clone();
		
		long start = startDate.getTimeInMillis();
		long stop = stopDate.getTimeInMillis();

		// First calculate the days the given dates span
		ArrayList<IdPair> validDays = getValidIntersectingDays(startDate, stopDate);

		vtkPoints points = polydata.GetPoints();
		vtkCellArray vert = polydata.GetVerts();

		points.SetNumberOfPoints(0);
		vert.SetNumberOfCells(0);
		originalPoints.clear();
		
		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(1);

		// If several times the number of days spanned is more than the number of cubes,
		// then download days, otherwise download cubes. The reason for the "several times"
		// is that the day files are much larger than the cube files.
		if (cubeList == null ||
				cubeList.size() == 0 ||
				4*validDays.size() < cubeList.size())
		{
			int id = 0;
			for (IdPair day : validDays)
			{
				String path = nlrDoyToPathMap.get(day.toString());

				File file = FileCache.getFileFromServer(path);
				if (file == null)
					throw new IOException(path + " could not be loaded");
				File filecubes = FileCache.getFileFromServer(path.substring(0, path.length()-2) + "cubeids.gz");
				if (filecubes == null)
					throw new IOException(path + "cubeids could not be loaded");

				ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
				ArrayList<String> linescubes = FileUtil.getFileLinesAsStringList(filecubes.getAbsolutePath());

				for (int i=2; i<lines.size(); ++i)
				{
					String[] vals = lines.get(i).trim().split("\\s+");

					long time = sdf.parse(vals[4]).getTime();
					if (time < start || time > stop)
						continue;

					if (cubeList != null &&
							cubeList.size() > 0 &&
							!cubeList.contains(Integer.valueOf(linescubes.get(i))))
					{
						continue;
					}
					
					// don't include outliers
					if (Integer.valueOf(linescubes.get(i)) == -1)
					    continue;
					
					double[] point = {
							Double.parseDouble(vals[14])/1000.0,
							Double.parseDouble(vals[15])/1000.0,
							Double.parseDouble(vals[16])/1000.0};
					points.InsertNextPoint(point);
					idList.SetId(0, id++);
					vert.InsertNextCell(idList);

					double potential = Double.parseDouble(vals[18]);

					originalPoints.add(new NLRPoint(point, time, potential));
				}
			}
		}
		else
		{
			for (Integer cubeid : cubeList)
			{
				String filename = "/NLR/per_cube/" + cubeid + ".nlr";
				File file = FileCache.getFileFromServer(filename);

				if (file == null)
					continue;

				ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());

				for (int i=0; i<lines.size(); ++i)
				{
					String[] vals = lines.get(i).trim().split("\\s+");

					long time = sdf.parse(vals[4]).getTime();
					if (time < start || time > stop)
						continue;

					double[] point = {
							Double.parseDouble(vals[14])/1000.0,
							Double.parseDouble(vals[15])/1000.0,
							Double.parseDouble(vals[16])/1000.0};

					double potential = Double.parseDouble(vals[18]);

					originalPoints.add(new NLRPoint(point, time, potential));
				}
			}
			
			// Now sort all the points in time order and place them into polydata

			points.SetNumberOfPoints(originalPoints.size());
			
			Collections.sort(originalPoints);
			int numPoints = originalPoints.size();
			for (int i=0; i<numPoints; ++i)
			{
				points.SetPoint(i, originalPoints.get(i).point);
				idList.SetId(0, i);
				vert.InsertNextCell(idList);
			}
		}
	}
	
	private void applyMask(NLRMaskType maskType, double maskValue, boolean reset)
	{
        int totalNumberPoints = originalPoints.size();

        if (reset || needToResetMask)
        {
            // If we are resetting the mask, make sure maskValue is positive
            if (maskValue < 0)
                maskValue = -maskValue;
        }
        
        if (maskType == NLRMaskType.NONE)
        {
            firstPointShown = 0;
            lastPointShown = totalNumberPoints - 1;
            return;
        }

        if ( reset == false && needToResetMask == false &&
                ( (maskValue >= 0 && lastPointShown  >= totalNumberPoints-1) ||
                  (maskValue <  0 && firstPointShown <= 0) ) )
		{
			return;
		}

        if (maskType == NLRMaskType.BY_NUMBER)
		{
			if (maskValue >= 0)
			{
			    if (reset || needToResetMask)
			    {
			        firstPointShown = 0;
			        lastPointShown = (int)maskValue - 1;
			    }
			    else
			    {
			        firstPointShown = lastPointShown + 1;
			        lastPointShown = lastPointShown + (int)maskValue;
			    }
				if (lastPointShown > totalNumberPoints-1)
					lastPointShown = totalNumberPoints-1;
			}
			else
			{
				lastPointShown = firstPointShown - 1;
				firstPointShown = firstPointShown + (int)maskValue;
				if (firstPointShown < 0)
					firstPointShown = 0;
			}
		}
		else if (maskType == NLRMaskType.BY_TIME)
		{
			// Find the last point ahead (or behind) the current shown points which
			// is within the value specified by maskValue of the last (first) point shown

			long milliseconds = 1000 * (long)maskValue;
			long initialTime = -1;
			int pointer = 0;
			if (maskValue >= 0)
			{
                if (reset || needToResetMask)
                    firstPointShown = 0;
                else
                    firstPointShown = lastPointShown;
			    initialTime = originalPoints.get(firstPointShown).time;
			    pointer = firstPointShown;
			}
			else
			{
                lastPointShown = firstPointShown;
                initialTime = originalPoints.get(lastPointShown).time;
                pointer = lastPointShown;
			}			

			while(true)
			{
				if (milliseconds > 0)
					++pointer;
				else
					--pointer;
			
				if (pointer < 0)
				{
				    pointer = 0;
					break;
				}
				else if (pointer >= totalNumberPoints)
				{
                    pointer = totalNumberPoints-1;
                    break;
				}				    
				
				long nextTime = originalPoints.get(pointer).time;
				
				if (Math.abs(nextTime - initialTime) > Math.abs(milliseconds))
					break;
			}

            if (milliseconds > 0)
                lastPointShown = pointer;
            else
                firstPointShown = pointer;
		}
		else if (maskType == NLRMaskType.BY_DISTANCE)
		{
			double[] initialPoint = null;
            int pointer = 0;
            if (maskValue >= 0)
            {
                if (reset || needToResetMask)
                    firstPointShown = 0;
                else
                    firstPointShown = lastPointShown;
                initialPoint = originalPoints.get(firstPointShown).point;
                pointer = firstPointShown;
            }
            else
            {
                lastPointShown = firstPointShown;
                initialPoint = originalPoints.get(lastPointShown).point;
                pointer = lastPointShown;
            }           

			double[] prevPoint = initialPoint;
			double currentDist = 0.0;
			while(true)
			{
				if (maskValue > 0)
					++pointer;
				else
					--pointer;
			
                if (pointer < 0)
                {
                    pointer = 0;
                    break;
                }
                else if (pointer >= totalNumberPoints)
                {
                    pointer = totalNumberPoints-1;
                    break;
                }                   
		
				double[] nextPoint = originalPoints.get(pointer).point;
				currentDist += GeometryUtil.distanceBetween(nextPoint, prevPoint);
				
				if (currentDist > Math.abs(maskValue))
					break;
				
				prevPoint = nextPoint;
			}

            if (maskValue > 0)
                lastPointShown = pointer;
            else
                firstPointShown = pointer;
		}
        
        needToResetMask = false;
	}

	public void removeAllNlrData()
	{
        needToResetMask = true;

        geometryFilter.SetPointMinimum(Integer.MAX_VALUE);
        geometryFilter.SetPointMaximum(Integer.MAX_VALUE);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public ArrayList<vtkProp> getProps() 
	{
		return actors;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	cellId = geometryFilter.GetPointMinimum() + cellId;
    	Date date = new Date(originalPoints.get(cellId).time);
    	return "NLR acquired at " + sdf2.format(date) 
    		+ ", Potential: " + originalPoints.get(cellId).potential + " J/kg";
    }

    // Create a mapping from dayOfYear/Year pair to path to nlr data
    private void createDoyToPathMap()
    {
    	nlrDoyToPathMap = new HashMap<String, String>();
    	
    	ArrayList<String> allpaths = getAllNlrPaths();
    	for (String path : allpaths)
    	{
    		String name = (new File(path)).getName();
    		int year = 2000 + Integer.parseInt(name.substring(2, 3));
    		String doyStr = name.substring(3, 6);
    		if (doyStr.startsWith("0"))
    			doyStr = doyStr.substring(1);
    		if (doyStr.startsWith("0"))
    			doyStr = doyStr.substring(1);
    		int doy = Integer.parseInt(doyStr);
    		
    		nlrDoyToPathMap.put((new IdPair(doy, year)).toString(), path);
    	}
    }
    
    public ArrayList<String> getAllNlrPaths()
    {
    	ArrayList<String> paths = new ArrayList<String>();
    	
		InputStream is = getClass().getResourceAsStream("/edu/jhuapl/near/data/NlrFiles.txt");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isr);

		String line;
        try 
        {
			while ((line = in.readLine()) != null)
			{
				paths.add(line);
			}
		}
        catch (IOException e) {
			e.printStackTrace();
		}
        
        return paths;
    }
    
	public void setRadialOffset(double offset)
	{
		if (offset == radialOffset)
			return;
		
		radialOffset = offset;

        vtkPoints points = polydata.GetPoints();
        
        int numberOfPoints = points.GetNumberOfPoints();
        
        for (int i=0;i<numberOfPoints;++i)
        {
        	double[] pt = originalPoints.get(i).point;
        	LatLon lla = GeometryUtil.reclat(pt);
        	lla.rad += offset;
        	pt = GeometryUtil.latrec(lla);
        	points.SetPoint(i, pt);
		}		

        polydata.Modified();

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		if (smallBodyCubes == null)
		{
			smallBodyCubes = new SmallBodyCubes(
					erosModel.getLowResSmallBodyPolyData(), 1.0, 1.0, true);
		}
		
		return smallBodyCubes.getIntersectingCubes(polydata);
	}

	public int getNumberOfPoints()
	{
		return originalPoints.size();
	}
	
	public int[] getMaskedPointRange()
	{
        int[] range = {firstPointShown, lastPointShown};
		return range;
	}

    public long getTimeOfPoint(int i)
    {
        return originalPoints.get(i).time;
    }
	
    public double getLengthOfMaskedPoints()
    {
        double length = 0.0;
        
        for (int i=firstPointShown+1; i<=lastPointShown; ++i)
        {
            double[] prevPoint = originalPoints.get(i-1).point;
            double[] currentPoint = originalPoints.get(i).point;
            double dist = GeometryUtil.distanceBetween(prevPoint, currentPoint);
            length += dist;
        }
        
        return length;
    }
    
    /**
     * Returns the potential plotted as a function of distance for the masked points
     * @param potential
     * @param distance
     */
    public void getPotentialVsDistance(ArrayList<Double> potential, ArrayList<Double> distance)
    {
    	potential.clear();
    	distance.clear();
    	
    	if (originalPoints.size() == 0)
    		return;
    	
        double length = 0.0;
        
        potential.add(originalPoints.get(firstPointShown).potential);
        distance.add(0.0);
        
        for (int i=firstPointShown+1; i<=lastPointShown; ++i)
        {
            double[] prevPoint = originalPoints.get(i-1).point;
            double[] currentPoint = originalPoints.get(i).point;
            double dist = GeometryUtil.distanceBetween(prevPoint, currentPoint);
            length += dist;

            potential.add(originalPoints.get(i).potential);
            distance.add(length);
        }
    }

    /**
     * Returns the potential plotted as a function of time for the masked points
     * @param potential
     * @param time
     */
    public void getPotentialVsTime(ArrayList<Double> potential, ArrayList<Long> time)
    {
    	potential.clear();
    	time.clear();
    	
    	if (originalPoints.size() == 0)
    		return;
    	
        for (int i=firstPointShown; i<=lastPointShown; ++i)
        {
            potential.add(originalPoints.get(i).potential);
            time.add(originalPoints.get(i).time);
        }
    }
}
