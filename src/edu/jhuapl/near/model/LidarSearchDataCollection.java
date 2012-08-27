package edu.jhuapl.near.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.joda.time.DateTime;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.near.util.ColorUtil;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.GravityProgram;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;

public abstract class LidarSearchDataCollection extends Model
{
    private SmallBodyModel smallBodyModel;
    private vtkPolyData polydata;
    private vtkPolyData selectedPointPolydata;
    private ArrayList<LidarPoint> originalPoints = new ArrayList<LidarPoint>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper pointsMapper;
    private vtkPolyDataMapper selectedPointMapper;
    private vtkActor actor;
    private vtkActor selectedPointActor;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private vtkPolyData emptyPolyData; // an empty polydata for resetting

    private double radialOffset = 0.0;
    private double[] translation = {0.0, 0.0, 0.0};

    private String dataSource;
    private DateTime startDate;
    private DateTime stopDate;
    private TreeSet<Integer> cubeList;

    private int selectedPoint = -1;

    private ArrayList<Track> tracks = new ArrayList<Track>();
    private long timeSeparationBetweenTracks = 10000; // In milliseconds
    private int minTrackLength = 1;
    private int[] defaultColor = {0, 0, 255, 255};
    private ArrayList<Integer> displayedPointToOriginalPointMap = new ArrayList<Integer>();
    private boolean enableTrackErrorComputation = false;
    private double trackError;

    private class LidarPoint implements Comparable<LidarPoint>
    {
        public double[] target;
        public double[] scpos;
        public Long time;
        public double potential;

        public LidarPoint(double[] target, double[] scpos, long time, double potential)
        {
            this.target = target;
            this.scpos = scpos;
            this.time = time;
            this.potential = potential;
        }

        public int compareTo(LidarPoint o)
        {
            return time.compareTo(o.time);
        }
    }

    private class Track
    {
        public int startId = -1;
        public int stopId = -1;
        public boolean hidden = false;
        public int[] color = defaultColor.clone(); // blue by default

        public int getNumberOfPoints()
        {
            return stopId - startId + 1;
        }

        public boolean containsId(int id)
        {
            return startId >= 0 && stopId >=0 && id >= startId && id <= stopId;
        }
    }

    public LidarSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;

        // Initialize an empty polydata for resetting
        emptyPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray vert = new vtkCellArray();
        emptyPolyData.SetPoints( points );
        emptyPolyData.SetVerts( vert );
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        colors.SetNumberOfComponents(4);
        emptyPolyData.GetCellData().SetScalars(colors);

        polydata = new vtkPolyData();
        polydata.DeepCopy(emptyPolyData);

        selectedPointPolydata = new vtkPolyData();
        selectedPointPolydata.DeepCopy(emptyPolyData);

        pointsMapper = new vtkPolyDataMapper();
        pointsMapper.SetScalarModeToUseCellData();
        pointsMapper.SetInput(polydata);

        selectedPointMapper = new vtkPolyDataMapper();
        selectedPointMapper.SetInput(selectedPointPolydata);

        actor = new vtkActor();
        actor.SetMapper(pointsMapper);
        actor.GetProperty().SetPointSize(2.0);

        actors.add(actor);

        selectedPointActor = new vtkActor();
        selectedPointActor.SetMapper(selectedPointMapper);
        selectedPointActor.GetProperty().SetColor(0.1, 0.1, 1.0);
        selectedPointActor.GetProperty().SetPointSize(7.0);

        actors.add(selectedPointActor);
    }

    abstract public double getOffsetScale();
    abstract public Map<String, String> getLidarDataSourceMap();


    public void setLidarData(
            String dataSource,
            DateTime startDate,
            DateTime stopDate,
            TreeSet<Integer> cubeList,
            double[] selectionRegionCenter,
            double selectionRegionRadius,
            long timeSeparationBetweenTracks,
            int minTrackLength) throws IOException, ParseException
    {
        runQuery(
                dataSource,
                startDate,
                stopDate,
                cubeList,
                selectionRegionCenter,
                selectionRegionRadius,
                timeSeparationBetweenTracks,
                minTrackLength);

        selectPoint(-1);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }


    private void runQuery(
            String dataSource,
            DateTime startDate,
            DateTime stopDate,
            TreeSet<Integer> cubeList,
            double[] selectionRegionCenter,
            double selectionRegionRadius,
            long timeSeparationBetweenTracks,
            int minTrackLength) throws IOException, ParseException
    {
        if (dataSource.equals(this.dataSource) &&
                startDate.equals(this.startDate) &&
                stopDate.equals(this.stopDate) &&
                cubeList.equals(this.cubeList) &&
                timeSeparationBetweenTracks == this.timeSeparationBetweenTracks &&
                minTrackLength == this.minTrackLength)
        {
            return;
        }

        // Make clones since otherwise the previous if statement might
        // evaluate to true even if something changed.
        this.dataSource = new String(dataSource);
        this.startDate = new DateTime(startDate);
        this.stopDate = new DateTime(stopDate);
        this.cubeList = (TreeSet<Integer>)cubeList.clone();
        this.timeSeparationBetweenTracks = timeSeparationBetweenTracks;
        this.minTrackLength = minTrackLength;


        long start = startDate.getMillis();
        long stop = stopDate.getMillis();

        originalPoints.clear();

        double[] point2 = null;
        double radius2 = Double.MAX_VALUE;
        vtkLine line = new vtkLine();
        if (selectionRegionCenter != null)
        {
            double[] normal = smallBodyModel.getNormalAtPoint(selectionRegionCenter);
            point2 = new double[]{
                selectionRegionCenter[0] + normal[0],
                selectionRegionCenter[1] + normal[1],
                selectionRegionCenter[2] + normal[2],
            };
            radius2 = selectionRegionRadius * selectionRegionRadius;
        }

        int timeindex = 0;
        int xindex = 1;
        int yindex = 2;
        int zindex = 3;
        int scxindex = 4;
        int scyindex = 5;
        int sczindex = 6;
        int potentialIndex = 7;

        for (Integer cubeid : cubeList)
        {
            String filename = getLidarDataSourceMap().get(dataSource) + "/" + cubeid + ".lidarcube";
            File file = FileCache.getFileFromServer(filename);

            if (file == null)
                continue;

            InputStream fs = new FileInputStream(file.getAbsolutePath());
            if (file.getAbsolutePath().toLowerCase().endsWith(".gz"))
                fs = new GZIPInputStream(fs);
            InputStreamReader isr = new InputStreamReader(fs);
            BufferedReader in = new BufferedReader(isr);

            String lineRead;
            while ((lineRead = in.readLine()) != null)
            {
                String[] vals = lineRead.trim().split("\\s+");

                long time = new DateTime(vals[timeindex]).getMillis();
                if (time < start || time > stop)
                    continue;

                double[] scpos = new double[3];
                double[] target = new double[3];
                target[0] = Double.parseDouble(vals[xindex]);
                target[1] = Double.parseDouble(vals[yindex]);
                target[2] = Double.parseDouble(vals[zindex]);
                scpos[0] = Double.parseDouble(vals[scxindex]);
                scpos[1] = Double.parseDouble(vals[scyindex]);
                scpos[2] = Double.parseDouble(vals[sczindex]);

                double potential = Double.parseDouble(vals[potentialIndex]);

                double dist2 = 0.0;
                if (selectionRegionCenter != null)
                    dist2 = line.DistanceToLine(target, selectionRegionCenter, point2);
                if (dist2 <= radius2)
                {
                    originalPoints.add(new LidarPoint(target, scpos, time, potential));
                }
            }

            in.close();
        }

        // Sort points in time order
        Collections.sort(originalPoints);

        radialOffset = 0.0;
        translation[0] = translation[1] = translation[2] = 0.0;

        computeTracks();
        removeTracksThatAreTooSmall();

        assignInitialColorToTrack();

        updateTrackPolydata();
    }

    /**
     * Load a track from a file. This will replace all currently existing tracks
     * with a single track.
     * @param filename
     */
    public void loadTrackFromFile(File file) throws IOException
    {
        originalPoints.clear();

        int timeindex = 0;
        int xindex = 1;
        int yindex = 2;
        int zindex = 3;
        int scxindex = 4;
        int scyindex = 5;
        int sczindex = 6;
        int potentialIndex = 7;

        InputStream fs = new FileInputStream(file.getAbsolutePath());
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String lineRead;
        while ((lineRead = in.readLine()) != null)
        {
            String[] vals = lineRead.trim().split("\\s+");

            long time = new DateTime(vals[timeindex]).getMillis();

            double[] scpos = new double[3];
            double[] target = new double[3];
            target[0] = Double.parseDouble(vals[xindex]);
            target[1] = Double.parseDouble(vals[yindex]);
            target[2] = Double.parseDouble(vals[zindex]);
            scpos[0] = Double.parseDouble(vals[scxindex]);
            scpos[1] = Double.parseDouble(vals[scyindex]);
            scpos[2] = Double.parseDouble(vals[sczindex]);

            double potential = Double.parseDouble(vals[potentialIndex]);

            originalPoints.add(new LidarPoint(target, scpos, time, potential));
        }

        in.close();

        timeSeparationBetweenTracks = Long.MAX_VALUE;
        radialOffset = 0.0;
        translation[0] = translation[1] = translation[2] = 0.0;

        computeTracks();
        assignInitialColorToTrack();
        updateTrackPolydata();
    }

    /**
     * Return the track with the specified trackId
     *
     * @param trackId
     * @return
     */
    public Track getTrack(int trackId)
    {
        return tracks.get(trackId);
    }

    public int getTrackIdFromPointId(int pointId)
    {
        pointId = displayedPointToOriginalPointMap.get(pointId);
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
            if (currentTime - prevTime >= timeSeparationBetweenTracks)
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

    public void saveTrack(int trackId, File outfile) throws IOException
    {
        FileWriter fstream = new FileWriter(outfile);
        BufferedWriter out = new BufferedWriter(fstream);

        int startId = tracks.get(trackId).startId;
        int stopId = tracks.get(trackId).stopId;

        String newline = System.getProperty("line.separator");

        for (int i=startId; i<=stopId; ++i)
        {
            LidarPoint pt = originalPoints.get(i);

            Date date = new Date(pt.time);

            out.write(sdf.format(date).replace(' ', 'T') + " " +
                    pt.target[0] + " " +
                    pt.target[1] + " " +
                    pt.target[2] + " " +
                    pt.scpos[0] + " " +
                    pt.scpos[1] + " " +
                    pt.scpos[2] + " " +
                    pt.potential + newline);
        }

        out.close();
    }

    private void assignInitialColorToTrack()
    {
        Color[] colors = ColorUtil.generateColors(tracks.size());
        int[] color = new int[4];
        int i = 0;

        for (Track track : tracks)
        {
            color[0] = colors[i].getRed();
            color[1] = colors[i].getGreen();
            color[2] = colors[i].getBlue();
            color[3] = colors[i].getAlpha();

            track.color = color.clone();

            ++i;
        }
    }

    public void setTrackColor(int trackId, Color color)
    {
        Track track = tracks.get(trackId);
        track.color[0] = color.getRed();
        track.color[1] = color.getGreen();
        track.color[2] = color.getBlue();
        track.color[3] = color.getAlpha();
        updateTrackPolydata();
    }

    public int[] getTrackColor(int trackId)
    {
        return tracks.get(trackId).color.clone();
    }

    public void setColorAllTracks(Color color)
    {
        defaultColor[0] = color.getRed();
        defaultColor[1] = color.getGreen();
        defaultColor[2] = color.getBlue();
        defaultColor[3] = color.getAlpha();

        for (Track track : tracks)
        {
            track.color = defaultColor.clone();
        }

        updateTrackPolydata();
    }

    public void hideTrack(int trackId, boolean hide)
    {
        tracks.get(trackId).hidden = hide;
        updateTrackPolydata();
        updateSelectedPoint();
    }

    public void hideOtherTracksExcept(int trackId)
    {
        Track trackToHide = tracks.get(trackId);
        for (Track track : tracks)
        {
            if (track != trackToHide)
                track.hidden = true;
        }

        updateTrackPolydata();
        updateSelectedPoint();
    }

    public void hideAllTracks()
    {
        for (Track track : tracks)
        {
            track.hidden = true;
        }

        updateTrackPolydata();
        updateSelectedPoint();
    }

    public void showAllTracks()
    {
        for (Track track : tracks)
        {
            track.hidden = false;
        }

        updateTrackPolydata();
    }

    public boolean isTrackHidden(int trackId)
    {
        return tracks.get(trackId).hidden;
    }

    private int getDisplayPointIdFromOriginalPointId(int ptId)
    {
        return displayedPointToOriginalPointMap.indexOf(ptId);
    }

    private double[] transformPoint(double[] pt)
    {
        if (radialOffset != 0.0)
        {
            LatLon lla = MathUtil.reclat(pt);
            lla.rad += radialOffset;
            pt = MathUtil.latrec(lla);
        }

        return new double[]{pt[0]+translation[0], pt[1]+translation[1], pt[2]+translation[2]};
    }

    private void updateTrackPolydata()
    {
        // Place the points into polydata
        polydata.DeepCopy(emptyPolyData);
        vtkPoints points = polydata.GetPoints();
        vtkCellArray vert = polydata.GetVerts();
        vtkUnsignedCharArray colors = (vtkUnsignedCharArray)polydata.GetCellData().GetScalars();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        displayedPointToOriginalPointMap.clear();
        int count = 0;

        int numTracks = getNumberOfTrack();
        for (int j=0; j<numTracks; ++j)
        {
            Track track = getTrack(j);
            int startId = track.startId;
            int stopId = track.stopId;
            if (!track.hidden)
            {
                for (int i=startId; i<=stopId; ++i)
                {
                    double[] pt = originalPoints.get(i).target;
                    pt = transformPoint(pt);
                    points.InsertNextPoint(pt);

                    idList.SetId(0, count);
                    vert.InsertNextCell(idList);

                    colors.InsertNextTuple4(track.color[0], track.color[1], track.color[2], track.color[3]);
                    displayedPointToOriginalPointMap.add(i);
                    ++count;
                }
            }
        }
        polydata.GetCellData().GetScalars().Modified();
        polydata.Modified();

        if (enableTrackErrorComputation)
            computeTrackError();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private void removeTrack(int trackId)
    {
        Track track = tracks.get(trackId);
        int trackSize = track.getNumberOfPoints();

        for (int i=track.stopId; i>=track.startId; --i)
            originalPoints.remove(i);

        tracks.remove(trackId);

        // Go through all tracks that follow the deleted track and shift
        // all the start and stop ids down by the size of the deleted track
        int numberOfTracks = tracks.size();
        for (int i=trackId; i<numberOfTracks; ++i)
        {
            track = tracks.get(i);
            track.startId -= trackSize;
            track.stopId -= trackSize;
        }
    }

    private void removeTracksThatAreTooSmall()
    {
        for (int i=tracks.size()-1; i>=0; --i)
        {
            if (tracks.get(i).getNumberOfPoints() < minTrackLength)
                removeTrack(i);
        }
    }

    public void removeAllLidarData()
    {
        polydata.DeepCopy(emptyPolyData);
        originalPoints.clear();
        tracks.clear();

        this.dataSource = null;
        this.startDate = null;
        this.stopDate = null;
        this.cubeList = null;

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
        if (!originalPoints.isEmpty() && !tracks.isEmpty())
        {
            cellId = displayedPointToOriginalPointMap.get(cellId);
            Date date = new Date(originalPoints.get(cellId).time);
            return "Lidar point acquired at " + sdf.format(date)
                + ", Potential: " + originalPoints.get(cellId).potential + " J/kg";
        }

        return "";
    }

    public void setOffset(double offset)
    {
        if (offset == radialOffset)
            return;

        radialOffset = offset;

        updateTrackPolydata();
        updateSelectedPoint();
    }

    public void setTranslation(double[] translation)
    {
        if (this.translation[0] == translation[0] && this.translation[1] == translation[1] && this.translation[2] == translation[2])
            return;

        this.translation[0] = translation[0];
        this.translation[1] = translation[1];
        this.translation[2] = translation[2];

        updateTrackPolydata();
        updateSelectedPoint();
    }

    public double[] getTranslation()
    {
        return this.translation;
    }

    public void setPointSize(int size)
    {
        if (actor != null)
        {
            actor.GetProperty().SetPointSize(size);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public int getNumberOfPoints()
    {
        return originalPoints.size();
    }

    public long getTimeOfPoint(int i)
    {
        return originalPoints.get(i).time;
    }

    /** It is useful to fit a line to the track. The following function computes
     * the parameters of such a line, namely, a point on the line
     * and a vector pointing in the direction of the line.
     * Note that the returned fittedLinePoint is the point on the line closest to
     * the first point of the track.
     */
    private void fitLineToTrack(int trackId, double[] fittedLinePoint, double[] fittedLineDirection)
    {
        Track track = tracks.get(trackId);
        int startId = track.startId;
        int stopId = track.stopId;

        if (startId == stopId)
            return;

        try
        {
            long t0 = originalPoints.get(startId).time;

            double[] lineStartPoint = new double[3];
            for (int j=0; j<3; ++j)
            {
                PolynomialFitter fitter = new PolynomialFitter(1, new LevenbergMarquardtOptimizer());
                for (int i=startId; i<=stopId; ++i)
                {
                    LidarPoint lp = originalPoints.get(i);
                    fitter.addObservedPoint(1.0, (double)(lp.time-t0)/1000.0, lp.target[j]);
                }

                PolynomialFunction fitted = fitter.fit();
                fittedLineDirection[j] = fitted.getCoefficients()[1];
                lineStartPoint[j] = fitted.value(0.0);
            }
            MathUtil.vhat(fittedLineDirection, fittedLineDirection);

            // Set the fittedLinePoint to the point on the line closest to first track point
            // as this makes it easier to do distance computations along the line.
            double[] dist = new double[1];
            MathUtil.nplnpt(lineStartPoint, fittedLineDirection, originalPoints.get(startId).target, fittedLinePoint, dist);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private double distanceOfClosestPointOnLineToStartOfLine(double[] point, int trackId, double[] fittedLinePoint, double[] fittedLineDirection)
    {
        Track track = tracks.get(trackId);
        if (track.startId == track.stopId)
            return 0.0;

        double[] pnear = new double[3];
        double[] dist = new double[1];
        MathUtil.nplnpt(fittedLinePoint, fittedLineDirection, point, pnear, dist);

        return MathUtil.distanceBetween(pnear, fittedLinePoint);
    }

    /**
     * Run gravity program on specified track and return potential, acceleration,
     * and elevation as function of distance and time.
     * @param trackId
     * @throws InterruptedException
     */
    public void getGravityDataForTrack(
            int trackId,
            ArrayList<Double> potential,
            ArrayList<Double> acceleration,
            ArrayList<Double> elevation,
            ArrayList<Double> distance,
            ArrayList<Long> time) throws InterruptedException, IOException
    {
        Track track = tracks.get(trackId);

        if (originalPoints.size() == 0 || track.startId < 0 || track.stopId < 0)
            throw new IOException();

        // Run the gravity program
        GravityProgram gravityProgram = new GravityProgram();
        gravityProgram.setDensity(smallBodyModel.getDensity());
        gravityProgram.setRotationRate(smallBodyModel.getRotationRate());
        gravityProgram.setRefPotential(smallBodyModel.getReferencePotential());
        File file = FileCache.getFileFromServer(
                smallBodyModel.getServerPathToShapeModelFileInPlateFormat());
        gravityProgram.setShapeModelFile(file.getAbsolutePath());
        File trackFile = new File(Configuration.getTempFolder() + File.separator + "track.txt");
        saveTrack(trackId, trackFile);
        gravityProgram.setTrackFile(trackFile.getAbsolutePath());
        Process process = gravityProgram.runGravity();
        process.waitFor();


        potential.clear();
        acceleration.clear();
        elevation.clear();
        distance.clear();
        time.clear();

        String filename = gravityProgram.getPotentialFile();
        potential.addAll(FileUtil.getFileLinesAsDoubleList(filename));
        filename = gravityProgram.getAccelerationMagnitudeFile();
        acceleration.addAll(FileUtil.getFileLinesAsDoubleList(filename));
        filename = gravityProgram.getElevationFile();
        elevation.addAll(FileUtil.getFileLinesAsDoubleList(filename));


        double[] fittedLinePoint = new double[3];
        double[] fittedLineDirection = new double[3];
        fitLineToTrack(trackId, fittedLinePoint, fittedLineDirection);
        for (int i=track.startId; i<=track.stopId; ++i)
        {
            double[] point = originalPoints.get(i).target;
            point = transformPoint(point);
            double dist = distanceOfClosestPointOnLineToStartOfLine(point, trackId, fittedLinePoint, fittedLineDirection);
            distance.add(dist);
            time.add(originalPoints.get(i).time);
        }
    }


    /**
     * select a point
     * @param ptId point id which must be id of a displayed point, not an original point
     */
    public void selectPoint(int ptId)
    {
        if (ptId >= 0)
            selectedPoint = displayedPointToOriginalPointMap.get(ptId);
        else
            selectedPoint = -1;

        selectedPointPolydata.DeepCopy(emptyPolyData);
        vtkPoints points = selectedPointPolydata.GetPoints();
        vtkCellArray vert = selectedPointPolydata.GetVerts();
        vtkUnsignedCharArray colors = (vtkUnsignedCharArray)selectedPointPolydata.GetCellData().GetScalars();

        if (ptId >= 0)
        {
            points.InsertNextPoint(polydata.GetPoints().GetPoint(ptId));

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);
            idList.SetId(0, 0);
            vert.InsertNextCell(idList);

            colors.InsertNextTuple4(0, 0, 255, 255);
        }

        selectedPointPolydata.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void updateSelectedPoint()
    {
        int ptId = -1;
        if (selectedPoint >= 0)
            ptId = getDisplayPointIdFromOriginalPointId(selectedPoint);

        if (ptId < 0)
        {
            selectedPointPolydata.DeepCopy(emptyPolyData);
        }
        else
        {
            vtkPoints points = selectedPointPolydata.GetPoints();
            points.SetPoint(0, polydata.GetPoints().GetPoint(ptId));
        }

        selectedPointPolydata.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double[] getSelectedPoint()
    {
        if (selectedPoint >= 0)
            return originalPoints.get(selectedPoint).target.clone();

        return null;
    }

    public String getTrackTimeRange(int trackId)
    {
        Track track = tracks.get(trackId);

        if (originalPoints.size() == 0 || track.startId < 0 || track.stopId < 0)
            return "";

        long t0 = originalPoints.get(track.startId).time;
        long t1 = originalPoints.get(track.stopId).time;

        return sdf.format(new Date(t0)).replace(' ', 'T') + " - " +
            sdf.format(new Date(t1)).replace(' ', 'T');
    }

    public int getNumberOfPointsPerTrack(int trackId)
    {
        return tracks.get(trackId).getNumberOfPoints();
    }

    public void setEnableTrackErrorComputation(boolean enable)
    {
        enableTrackErrorComputation = enable;
        if (enable)
            computeTrackError();
    }

    private void computeTrackError()
    {
        trackError = 0.0;

        vtkPoints points = polydata.GetPoints();
        int numberOfPoints = points.GetNumberOfPoints();
        double[] pt = new double[3];
        for (int i=0; i<numberOfPoints; ++i)
        {
            points.GetPoint(i, pt);
            double[] closestPt = smallBodyModel.findClosestPoint(pt);
            trackError += MathUtil.distanceBetween(pt, closestPt);
        }

        if (numberOfPoints > 0)
            trackError /= (double)numberOfPoints;
    }

    public double getTrackError()
    {
        return trackError;
    }
}
