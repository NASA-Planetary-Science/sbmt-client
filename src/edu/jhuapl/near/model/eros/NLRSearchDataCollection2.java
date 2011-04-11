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

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkGeometryFilter;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.IdPair;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.SmallBodyCubes;

public class NLRSearchDataCollection2 extends Model
{
    private vtkPolyData polydata;
    private vtkPolyData selectedPointPolydata;
    private ArrayList<NLRPoint> originalPoints = new ArrayList<NLRPoint>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkGeometryFilter geometryFilter;
    private vtkPolyDataMapper pointsMapper;
    private vtkPolyDataMapper selectedPointMapper;
    private vtkActor actor;
    private vtkActor selectedPointActor;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-DDD'T'HH:mm:ss.SSS", Locale.US);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MMM-d HH:mm:ss.SSS", Locale.US);

    private double radialOffset = 0.0;

    private GregorianCalendar startDate;
    private GregorianCalendar stopDate;
    private TreeSet<Integer> cubeList;

    private HashMap<String, String> nlrDoyToPathMap = null;
    private SmallBodyModel erosModel;
    private SmallBodyCubes smallBodyCubes;

    private int firstPointShown = -1;
    private int lastPointShown = -1;

    private boolean needToResetMask = true;

    private int selectedPoint = -1;

    private ArrayList<Track> tracks = new ArrayList<Track>();
    private vtkUnsignedCharArray colors;
    private int currentTrack = -1;
    private static long TRACK_SEPARATION = 10000;
    private final int[] defaultColor = {0, 0, 255, 255};
    private final int[] highlightColor = {255, 0, 255, 255};

    public enum NLRMaskType
    {
        NONE,
//        BY_NUMBER,
//        BY_TIME,
//        BY_DISTANCE
    }

    private static class NLRPoint implements Comparable<NLRPoint>
    {
        public double[] point;
        public Long time;
        public double potential;
        public short[] color;

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

    public static class Track
    {
        public int startId = -1;
        public int stopId = -1;

        public int getNumberOfPoints()
        {
            return stopId - startId + 1;
        }

        public boolean containsId(int id)
        {
            return startId >= 0 && stopId >=0 && id >= startId && id <= stopId;
        }
    }

    public NLRSearchDataCollection2(SmallBodyModel erosModel)
    {
        super(ModelNames.NLR_DATA_SEARCH);

        this.erosModel = erosModel;

        createDoyToPathMap();

        polydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray vert = new vtkCellArray();
        polydata.SetPoints( points );
        polydata.SetVerts( vert );
        colors = new vtkUnsignedCharArray();
        colors.SetNumberOfComponents(4);
        polydata.GetCellData().SetScalars(colors);

        selectedPointPolydata = new vtkPolyData();
        points = new vtkPoints();
        vert = new vtkCellArray();
        selectedPointPolydata.SetPoints( points );
        selectedPointPolydata.SetVerts( vert );

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
            pointsMapper.SetScalarModeToUseCellData();

            selectedPointMapper = new vtkPolyDataMapper();
            selectedPointMapper.SetInput(selectedPointPolydata);
        }

        if (actor == null)
        {
            actor = new vtkActor();
            actor.SetMapper(pointsMapper);
            //actor.GetProperty().SetColor(0.0, 0.0, 1.0);
            actor.GetProperty().SetPointSize(2.0);

            actors.add(actor);

            selectedPointActor = new vtkActor();
            selectedPointActor.SetMapper(selectedPointMapper);
            selectedPointActor.GetProperty().SetColor(0.1, 0.1, 1.0);
            selectedPointActor.GetProperty().SetPointSize(7.0);

            actors.add(selectedPointActor);
        }

        setRadialOffset(radialOffset);

        selectPoint(-1);

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



    private void loadNlrData(
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
        colors.SetNumberOfTuples(0);
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

                    // Don't include noise
                    if (vals[7].equals("1"))
                        continue;

                    double[] point = {
                            Double.parseDouble(vals[14])/1000.0,
                            Double.parseDouble(vals[15])/1000.0,
                            Double.parseDouble(vals[16])/1000.0};
                    points.InsertNextPoint(point);
                    idList.SetId(0, id++);
                    vert.InsertNextCell(idList);
                    colors.InsertNextTuple4(defaultColor[0], defaultColor[1], defaultColor[2], defaultColor[3]);

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

                    // Don't include noise
                    if (vals[7].equals("1"))
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
                colors.InsertNextTuple4(defaultColor[0], defaultColor[1], defaultColor[2], defaultColor[3]);
            }
        }

        computeTracks();
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
    }

    public Track getTrack(int trackId)
    {
        return tracks.get(trackId);
    }

    public int getTrackIdFromPointId(int pointId)
    {
        for (int i=0; i<tracks.size(); ++i)
        {
            if (getTrack(i).containsId(pointId))
                return i;
        }

        return -1;
    }

    public int getNumberOfTrack()
    {
        return tracks.size();
    }

    private void computeTracks()
    {
        tracks.clear();

        int size = originalPoints.size();
        if (size == 0)
            return;

        long prevTime = originalPoints.get(0).time;
        Track track = new Track();
        track.startId = 0;
        tracks.add(track);

        for (int i=1; i<size; ++i)
        {
            long currentTime = originalPoints.get(i).time;
            if (currentTime - prevTime >= TRACK_SEPARATION)
            {
                track.stopId = i-1;

                track = new Track();
                track.startId = i;

                tracks.add(track);
            }

            prevTime = currentTime;
        }

        track.stopId = size-1;
    }

    public void removeTrack(int trackId)
    {

    }

    public void saveTrack(int trackId)
    {

    }

    public void highlightTrack(int trackId)
    {
        setTrackColor(trackId, highlightColor, defaultColor);
    }

    public void hideTrack(int trackId)
    {
        setTrackColor(trackId, new int[]{0, 0, 0, 0}, defaultColor);
    }

    public void hideOtherTracksExcept(int trackId)
    {
        setTrackColor(trackId, defaultColor, new int[]{0, 0, 0, 0});
    }

    private void setTrackColor(int trackId, int[] trackColor, int[] otherTracksColor)
    {
        currentTrack = trackId;

        int numPoints = originalPoints.size();
        Track track = getTrack(trackId);
        for (int i=0; i<numPoints; ++i)
        {
            if (track.containsId(i))
                colors.SetTuple4(i, trackColor[0], trackColor[1], trackColor[2], trackColor[3]);
            else
                colors.SetTuple4(i, otherTracksColor[0], otherTracksColor[1], otherTracksColor[2], otherTracksColor[3]);
        }

        polydata.GetCellData().SetScalars(colors);
        polydata.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllNlrData()
    {
        needToResetMask = true;

        firstPointShown = -1;
        lastPointShown = -1;
        geometryFilter.SetPointMinimum(Integer.MAX_VALUE);
        geometryFilter.SetPointMaximum(Integer.MAX_VALUE);

        selectPoint(-1);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public ArrayList<vtkProp> getProps()
    {
        return actors;
    }

    /**
     *  Returns whether or not <tt>prop</tt> is the prop used for the actual data points
     *  as opposed the selection prop which is used for showing only the selected point.
     * @param prop
     * @return
     */
    public boolean isDataPointsProp(vtkProp prop)
    {
        return prop == actor;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
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

    private ArrayList<String> getAllNlrPaths()
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
            LatLon lla = MathUtil.reclat(pt);
            lla.rad += offset;
            pt = MathUtil.latrec(lla);
            points.SetPoint(i, pt);
        }

        polydata.Modified();

        // Force an update on the selected point
        selectPoint(selectedPoint);

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
            double dist = MathUtil.distanceBetween(prevPoint, currentPoint);
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

        if (originalPoints.size() == 0 || firstPointShown < 0 || lastPointShown < 0)
            return;

        double length = 0.0;

        potential.add(originalPoints.get(firstPointShown).potential);
        distance.add(0.0);

        for (int i=firstPointShown+1; i<=lastPointShown; ++i)
        {
            double[] prevPoint = originalPoints.get(i-1).point;
            double[] currentPoint = originalPoints.get(i).point;
            double dist = MathUtil.distanceBetween(prevPoint, currentPoint);
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

        if (originalPoints.size() == 0 || firstPointShown < 0 || lastPointShown < 0)
            return;

        for (int i=firstPointShown; i<=lastPointShown; ++i)
        {
            potential.add(originalPoints.get(i).potential);
            time.add(originalPoints.get(i).time);
        }
    }

    public void selectPoint(int ptId)
    {
//        if (ptId == selectedPoint)
//            return;

        selectedPoint = ptId;

        vtkPoints points = selectedPointPolydata.GetPoints();
        vtkCellArray vert = selectedPointPolydata.GetVerts();

        points.SetNumberOfPoints(0);
        vert.SetNumberOfCells(0);

        if (ptId >= 0)
        {
            points.SetNumberOfPoints(1);

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);

            ptId = geometryFilter.GetPointMinimum() + ptId;

            points.SetPoint(0, polydata.GetPoints().GetPoint(ptId));
            idList.SetId(0, 0);
            vert.InsertNextCell(idList);
        }

        selectedPointPolydata.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void saveNlrData(File outFile) throws IOException, ParseException
    {
        if (firstPointShown < 0 || lastPointShown < 0)
            return;
    }

}
