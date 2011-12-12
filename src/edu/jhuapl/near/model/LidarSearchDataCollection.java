package edu.jhuapl.near.model;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

import edu.jhuapl.near.server.SqlManager;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Properties;

public abstract class LidarSearchDataCollection extends Model
{
    private vtkPolyData polydata;
    private vtkPolyData selectedPointPolydata;
    private ArrayList<LidarPoint> originalPoints = new ArrayList<LidarPoint>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkPolyDataMapper pointsMapper;
    private vtkPolyDataMapper selectedPointMapper;
    private vtkActor actor;
    private vtkActor selectedPointActor;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private double radialOffset = 0.0;

    private DateTime startDate;
    private DateTime stopDate;
    private BoundingBox boundingBox;

    private SmallBodyModel smallBodyModel;

    private int selectedPoint = -1;

    private ArrayList<Track> tracks = new ArrayList<Track>();
    private long timeSeparationBetweenTracks = 10000; // In milliseconds
    private int minTrackLength = 1;
    private int[] defaultColor = {0, 0, 255, 255};
    //private final int[] highlightColor = {255, 0, 0, 255};
    private SqlManager db;
    private ArrayList<Integer> displayedPointToOriginalPointMap = new ArrayList<Integer>();

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
        //public boolean highlighted = false;
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

        polydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray vert = new vtkCellArray();
        polydata.SetPoints( points );
        polydata.SetVerts( vert );
        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
        colors.SetNumberOfComponents(4);
        polydata.GetCellData().SetScalars(colors);
        vtkUnsignedCharArray hidden = new vtkUnsignedCharArray();
        hidden.SetNumberOfComponents(1);
        polydata.GetPointData().SetScalars(hidden);

        selectedPointPolydata = new vtkPolyData();
        points = new vtkPoints();
        vert = new vtkCellArray();
        selectedPointPolydata.SetPoints( points );
        selectedPointPolydata.SetVerts( vert );
    }

    abstract public double getOffsetScale();
    abstract public String getDatabasePath();

    public void setLidarData(
            DateTime startDate,
            DateTime stopDate,
            BoundingBox bb,
            double[] selectionRegionCenter,
            double selectionRegionRadius,
            double maskValue,
            boolean reset,
            long timeSeparationBetweenTracks,
            int minTrackLength) throws IOException, ParseException
    {
        runQuery(
                startDate,
                stopDate,
                bb,
                selectionRegionCenter,
                selectionRegionRadius,
                timeSeparationBetweenTracks,
                minTrackLength);

        if (pointsMapper == null)
        {
            pointsMapper = new vtkPolyDataMapper();
            pointsMapper.SetScalarModeToUseCellData();
            pointsMapper.SetInput(polydata);

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


    private void runQuery(
            DateTime startDate,
            DateTime stopDate,
            BoundingBox bb,
            double[] selectionRegionCenter,
            double selectionRegionRadius,
            long timeSeparationBetweenTracks,
            int minTrackLength) throws IOException, ParseException
    {
        if (startDate.equals(this.startDate) &&
                stopDate.equals(this.stopDate) &&
                bb.equals(this.boundingBox) &&
                timeSeparationBetweenTracks == this.timeSeparationBetweenTracks &&
                minTrackLength == this.minTrackLength)
        {
            return;
        }

        // Make clones since otherwise the previous if statement might
        // evaluate to true even if something changed.
        this.startDate = new DateTime(startDate);
        this.stopDate = new DateTime(stopDate);
        this.boundingBox = (BoundingBox)bb.clone();
        this.timeSeparationBetweenTracks = timeSeparationBetweenTracks;
        this.minTrackLength = minTrackLength;


        long start = startDate.getMillis();
        long stop = stopDate.getMillis();

        originalPoints.clear();

        try
        {
            if (db == null)
            {
                String dbPath = getDatabasePath();
                File file = FileCache.getFileFromServer(dbPath);

                if (file == null)
                    throw new IOException(dbPath + " could not be loaded");

                String path = file.getAbsolutePath();
                path = path.substring(0, path.length()-6);

                db = new SqlManager("org.h2.Driver", "jdbc:h2:" + path + ";ACCESS_MODE_DATA=r");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String statement =
            "SELECT UTC, xtarget, ytarget, ztarget, xsc, ysc, zsc, potential FROM lidar WHERE" +
            " UTC >= " + start + " AND UTC <= " + stop +
            " AND xclosest >= " + bb.xmin + " AND xclosest <= " + bb.xmax +
            " AND yclosest >= " + bb.ymin + " AND yclosest <= " + bb.ymax +
            " AND zclosest >= " + bb.zmin + " AND zclosest <= " + bb.zmax +
            " ORDER BY UTC";

        try
        {
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery(statement);

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

            while(rs.next())
            {
                long time = rs.getLong(1);
                double[] target = new double[3];
                target[0] = rs.getFloat(2);
                target[1] = rs.getFloat(3);
                target[2] = rs.getFloat(4);
                double[] scpos = new double[3];
                scpos[0] = rs.getFloat(5);
                scpos[1] = rs.getFloat(6);
                scpos[2] = rs.getFloat(7);
                float potential = rs.getFloat(8);

                double dist2 = 0.0;
                if (selectionRegionCenter != null)
                    dist2 = line.DistanceToLine(target, selectionRegionCenter, point2);
                if (dist2 <= radius2)
                {
                    originalPoints.add(new LidarPoint(target, scpos, time, potential));
                }
            }

            st.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }


        computeTracks();
        removeTracksThatAreTooSmall();

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
    }

    public void hideAllTracks()
    {
        for (Track track : tracks)
        {
            track.hidden = true;
        }

        updateTrackPolydata();
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

    private void updateTrackPolydata()
    {
        // Place the points into polydata
        vtkPoints points = polydata.GetPoints();
        vtkCellArray vert = polydata.GetVerts();
        vtkUnsignedCharArray colors = (vtkUnsignedCharArray)polydata.GetCellData().GetScalars();
        vtkUnsignedCharArray hidden = (vtkUnsignedCharArray)polydata.GetPointData().GetScalars();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        points.SetNumberOfPoints(0);
        colors.SetNumberOfTuples(0);
        hidden.SetNumberOfTuples(0);
        vert.Initialize();
        displayedPointToOriginalPointMap.clear();

        int numTracks = getNumberOfTrack();
        for (int j=0; j<numTracks; ++j)
        {
            Track track = getTrack(j);
            int startId = track.startId;
            int stopId = track.stopId;
            for (int i=startId; i<=stopId; ++i)
            {
                if (track.hidden)
                {
                    hidden.InsertNextTuple1(1);
                }
                else
                {
                    points.InsertNextPoint(originalPoints.get(i).target);
                    idList.SetId(0, i);
                    vert.InsertNextCell(idList);

                    hidden.InsertNextTuple1(0);

                    colors.InsertNextTuple4(track.color[0], track.color[1], track.color[2], track.color[3]);

                    displayedPointToOriginalPointMap.add(i);
                }
            }
        }

        polydata.GetCellData().GetScalars().Modified();
        polydata.GetPointData().GetScalars().Modified();
        polydata.Modified();

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
        polydata.GetPoints().SetNumberOfPoints(0);
        originalPoints.clear();
        tracks.clear();

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

    public void setRadialOffset(double offset)
    {
        if (offset == radialOffset)
            return;

        radialOffset = offset;

        vtkPoints points = polydata.GetPoints();

        int numberOfPoints = points.GetNumberOfPoints();

        for (int i=0;i<numberOfPoints;++i)
        {
            double[] pt = originalPoints.get(i).target;
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
     * Returns the potential plotted as a function of distance for the masked points
     * @param potential
     * @param distance
     */
    public void getPotentialVsDistance(int trackId,
            ArrayList<Double> potential,
            ArrayList<Double> distance)
    {
        potential.clear();
        distance.clear();

        Track track = tracks.get(trackId);

        if (originalPoints.size() == 0 || track.startId < 0 || track.stopId < 0)
            return;

        double[] fittedLinePoint = new double[3];
        double[] fittedLineDirection = new double[3];
        fitLineToTrack(trackId, fittedLinePoint, fittedLineDirection);

        for (int i=track.startId; i<=track.stopId; ++i)
        {
            double[] point = originalPoints.get(i).target;
            double dist = distanceOfClosestPointOnLineToStartOfLine(point, trackId, fittedLinePoint, fittedLineDirection);
            potential.add(originalPoints.get(i).potential);
            distance.add(dist);
        }
    }

    /**
     * Returns the potential plotted as a function of time for the masked points
     * @param potential
     * @param time
     */
    public void getPotentialVsTime(int trackId,
            ArrayList<Double> potential,
            ArrayList<Long> time)
    {
        potential.clear();
        time.clear();

        Track track = tracks.get(trackId);

        if (originalPoints.size() == 0 || track.startId < 0 || track.stopId < 0)
            return;

        for (int i=track.startId; i<=track.stopId; ++i)
        {
            potential.add(originalPoints.get(i).potential);
            time.add(originalPoints.get(i).time);
        }
    }

    public void selectPoint(int ptId)
    {
        if (ptId >= 0)
            ptId = displayedPointToOriginalPointMap.get(ptId);

        selectedPoint = ptId;

        vtkPoints points = selectedPointPolydata.GetPoints();
        vtkCellArray vert = selectedPointPolydata.GetVerts();

        points.SetNumberOfPoints(0);
        vert.Initialize();

        if (ptId >= 0)
        {
            points.SetNumberOfPoints(1);

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);

            points.SetPoint(0, polydata.GetPoints().GetPoint(ptId));
            idList.SetId(0, 0);
            vert.InsertNextCell(idList);
        }

        selectedPointPolydata.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
}
