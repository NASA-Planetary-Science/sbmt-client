package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;
import java.util.function.BiFunction;

import com.google.common.collect.Range;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyLine;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.sbmt.lidar.feature.FeatureAttr;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryColoringFunctions;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.ui.color.ColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.color.GroupColorProvider;
import edu.jhuapl.sbmt.stateHistory.ui.color.StateHistoryFeatureType;

/**
 * vtkActor that represents a state history trajectory
 * @author steelrj1
 *
 */
public class TrajectoryActor extends vtkActor
{
    /**
     *
     */
    private Trajectory trajectory;

    /**
     *
     */
    private vtkPolyData trajectoryPolylines;

    /**
     *
     */
    private vtkPolyDataMapper trajectoryMapper = new vtkPolyDataMapper();

    /**
     *
     */
    private double[] trajectoryColor = {0, 255, 255, 255};

    /**
     *
     */
    private double trajectoryLineThickness = 1;

    /**
     *
     */
    private double minFraction = 0.0;

    /**
     *
     */
    private double maxFraction = 1.0;

    /**
     *
     */
    private vtkUnsignedCharArray colors;

    /**
     *
     */
    private int size;

    /**
     *
     */
    private vtkPolyData trajectoryPolyline;

    /**
     *
     */
    private BiFunction<Trajectory, Double, Double> coloringFunction;

    /**
     *
     */
    private Colormap colormap;

    private GroupColorProvider gcp;

    private FeatureAttr coloringAttribute;


	/**
	 * @param id
	 */
	public TrajectoryActor(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor
	 * @param trajectory	The <pre>Trajectory</pre> for this <pre>TrajectoryActor</pre>
	 */
	public TrajectoryActor(Trajectory trajectory)
	{
		this.trajectory = trajectory;
		this.trajectoryColor = trajectory.getTrajectoryColor();
		createTrajectoryPolyData();
		SetMapper(trajectoryMapper);
		SetVisibility(1);
	}

	/**
	 *	Creates the polydata for this actor.
	 */
	private void createTrajectoryPolyData()
	{
		int cellId = 0;
        vtkPoints points = new vtkPoints();
        vtkCellArray polylines = new vtkCellArray();
        colors = new vtkUnsignedCharArray();
        vtkPolyLine polyline = new vtkPolyLine();
        vtkCellArray edges = new vtkCellArray();

        colors.SetNumberOfComponents(4);

        Trajectory traj =  trajectory;
        traj.setCellId(cellId);

        size = traj.getX().size();
        for (int i=(int)(minFraction*size);i<maxFraction*size;i++)
        {
        	Double x = traj.getX().get(i);
            Double y = traj.getY().get(i);
            Double z = traj.getZ().get(i);

            points.InsertNextPoint(x, y, z);
            polyline.GetPointIds().InsertNextId(i);
        }

        polylines.InsertNextCell(polyline);
        colors.InsertNextTuple4(trajectoryColor[0], trajectoryColor[1], trajectoryColor[2], 0.0);	//last one is alpha
        for (int i=(int)(minFraction*size);i<maxFraction*size;i++)
        {
        	vtkLine edge = new vtkLine();
        	edge.GetPointIds().SetId(0, i);
        	edge.GetPointIds().SetId(1, (i+1));
        	edges.InsertNextCell(edge);
        	Color colorAtIndex = getColorAtIndex(i);
        	colors.InsertNextTuple4(colorAtIndex.getRed(), colorAtIndex.getGreen(), colorAtIndex.getBlue(), colorAtIndex.getAlpha());
        }

        trajectoryPolyline = new vtkPolyData();
        trajectoryPolyline.SetPoints(points);
        trajectoryPolyline.SetLines(edges);
        trajectoryPolyline.SetVerts(polylines);

        trajectoryPolyline.GetCellData().SetScalars(colors);

        trajectoryPolylines = trajectoryPolyline;
        trajectoryMapper.SetInputData(trajectoryPolyline);
        trajectoryMapper.Modified();
        GetProperty().SetLineWidth(trajectoryLineThickness);

		trajectoryMapper.SetInputData(trajectoryPolylines);
		trajectoryMapper.Update();
	}

	/**
	 * Based on the current <pre>minFraction</pre> and <pre>maxFraction</pre> values, updates the shown
	 * shown segments of the trajectory, and updates the mapper as appropriate.
	 */
	private void updateShownSegments()
	{
		int minValueToColor = (int)(minFraction*size);
		int maxValueToColor = (int)(maxFraction*size);
		Range<Integer> coloredRange = Range.closed(minValueToColor, maxValueToColor);

		vtkCellArray edges = new vtkCellArray();
		for (int i=0; i<size-1; i++)
        {
			if (!coloredRange.contains(i)) continue;
			vtkLine edge = new vtkLine();
        	edge.GetPointIds().SetId(0, i);
        	edge.GetPointIds().SetId(1, (i+1));
        	edges.InsertNextCell(edge);
        	Color colorAtIndex = getColorAtIndex(i);
        	colors.SetTuple4(i+1, colorAtIndex.getRed(), colorAtIndex.getGreen(), colorAtIndex.getBlue(), colorAtIndex.getAlpha());
        }
		trajectoryPolyline.SetLines(edges);
		trajectoryMapper.Modified();
		trajectoryMapper.Update();
	}

	/**
	 * Sets the coloring function (time based) and associated colormap.  The colormap is expected
	 * to have its min and max values already set.
	 * @param coloringFunction
	 * @param colormap
	 */
	/**
	 * @param coloringFunction
	 * @param colormap
	 */
	public void setColoringFunction(BiFunction<Trajectory, Double, Double> coloringFunction, Colormap colormap)
	{
		this.coloringFunction = coloringFunction;
		this.colormap = colormap;
	}

	public void setColoringProvider(GroupColorProvider gcp)
	{
		this.gcp = gcp;
	}

	/**
	 * For the given trajectory index (which in turn provides a time), returns the color of the trajectory at this time
	 * @param index	the index into the arraylist of times that make up this trajectory
	 * @return	the <pre>Color</pre> of the trajectory at this index
	 */
	private Color getColorAtIndex(int index)
	{
		if (gcp == null)
		{
			if (coloringFunction == null) return new Color((int)trajectoryColor[0], (int)trajectoryColor[1], (int)trajectoryColor[2], (int)trajectoryColor[3]);
//			System.out.println("TrajectoryActor: getColorAtIndex: using old method");
			double time = trajectory.getTime().get(index);
			double valueAtTime = coloringFunction.apply(trajectory, time);
			Color color = colormap.getColor(valueAtTime);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)trajectoryColor[3]);
			return color;
		}
		else
		{
//			System.out.println("TrajectoryActor: getColorAtIndex: using color provider");
			ColorProvider colorProvider = gcp.getColorProviderFor(trajectory, index, trajectory.getTime().size());
			StateHistoryFeatureType featureType = colorProvider.getFeatureType();
			if (featureType == StateHistoryFeatureType.Time)
			{
				double time = trajectory.getTime().get(index);
				return colorProvider.getColor(6.2e8, 6.3e8, time);
			}
			else if (featureType == StateHistoryFeatureType.Distance)
			{
				double time = trajectory.getTime().get(index);
				double valueAtTime = StateHistoryColoringFunctions.DISTANCE.getColoringFunction().apply(trajectory, time);
				if (valueAtTime < 0.859 || valueAtTime > 9.66) System.out.println("TrajectoryActor: getColorAtIndex: out of bounds");
				return colorProvider.getColor(0.859, 9.66, valueAtTime);
			}
			else
				return colorProvider.getColor(0.0, 1.0, 0.7);

//			return color;
		}
	}

	/**
	 * Sets the thickness of the trajectory line
	 * @param value	the thickness of the trajectory
	 */
	public void setTrajectoryLineThickness(double value)
    {
        this.trajectoryLineThickness = value;
        // recreate poly data with new thickness
        GetProperty().SetLineWidth(trajectoryLineThickness);
        createTrajectoryPolyData();
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null , null);
    }

    /**
     * Returns the trajectory line thickness
     * @return the trajectory line thickness
     */
    public double getTrajectoryLineThickness()
    {
        return trajectoryLineThickness;
    }

    /**
     * Returns the trajectory color
     * @return the trajectory color
     */
    public double[] getTrajectoryColor()
    {
        return trajectoryColor;
    }

    /**
     * Sets the trajectory color
     * @param color the trajectory color as a double array with values from 0.0 to 1.0
     */
    public void setTrajectoryColor(double[] color)
    {
        this.trajectoryColor = color;
        // recreate poly data with new color
        createTrajectoryPolyData();
        updateShownSegments();
    }

    /**
     * Sets the trajectory name
     * @param name the name of the trajectory
     */
    public void setTrajectoryName(String name)
    {
        trajectory.setName(name);
    }

    /**
     * Returns the name of the trajectory
     * @return the name of the trajectory
     */
    public String getTrajectoryName()
	{
		return trajectory.getName();
	}

	/**
	 * Toggles the visibility of the trajectory based on the value of <pre>show</pre>
	 * @param show
	 */
	public void showTrajectory(boolean show)
    {
    	if (show == true) { VisibilityOn(); } else { VisibilityOff(); }
		Modified();
    }

	/**
	 * Sets the minFraction and maxFraction values, and updates the shownSegments
	 * @param min
	 * @param max
	 */
	public void setMinMaxFraction(double min, double max)
	{
		this.minFraction = min;
		this.maxFraction = max;
		updateShownSegments();
	}
}