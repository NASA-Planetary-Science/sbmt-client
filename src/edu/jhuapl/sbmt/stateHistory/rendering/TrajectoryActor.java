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

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.sbmt.pointing.InstrumentPointing;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.ui.state.color.StateHistoryFeatureType;

import crucible.core.math.vectorspace.UnwritableVectorIJK;

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
    private double trajectoryLineThickness = 2;

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

    /**
     *
     */
    private ColorProvider gcp;


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
		this.trajectoryColor = trajectory.getColor();
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

        size = traj.getNumPoints();
		double stepSize = ((trajectory.getStopTime() - trajectory.getStartTime())/(double)(trajectory.getNumPoints()));
        for (int i=(int)(minFraction*size);i<maxFraction*size;i++)
        {
    		double time = trajectory.getStartTime() + stepSize*i;
    		InstrumentPointing pointing = trajectory.getPointingProvider().provide(time);
    		if (pointing == null) continue;
    		UnwritableVectorIJK scPosition = pointing.getScPosition();
    		double x = scPosition.getI();
    		double y = scPosition.getJ();
    		double z = scPosition.getK();
//        	Double x = traj.getX().get(i);
//            Double y = traj.getY().get(i);
//            Double z = traj.getZ().get(i);

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

	public void setColoringProvider(ColorProvider gcp)
	{
		this.gcp = gcp;
		updateShownSegments();
	}

	/**
	 * For the given trajectory index (which in turn provides a time), returns the color of the trajectory at this time
	 * @param index	the index into the arraylist of times that make up this trajectory
	 * @return	the <pre>Color</pre> of the trajectory at this index
	 */
	private Color getColorAtIndex(int index)
	{
		double time = trajectory.getStartTime() + index*trajectory.getTimeStep();
		if (gcp == null)
		{
			if (coloringFunction == null) return new Color((int)trajectoryColor[0], (int)trajectoryColor[1], (int)trajectoryColor[2], (int)trajectoryColor[3]);
			double valueAtTime = coloringFunction.apply(trajectory, time);
			Color color = colormap.getColor(valueAtTime);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)trajectoryColor[3]);
			return color;
		}
		else
		{
			FeatureType featureType = gcp.getFeatureType();
			if (featureType == StateHistoryFeatureType.Time)
			{
				return gcp.getColor(trajectory.getStartTime(), trajectory.getStopTime(), time);
			}
			else if (featureType == StateHistoryFeatureType.Distance)
			{
				double valueAtTime = trajectory.getPointingProvider().provide(time).getScPosition().getLength();
//				double valueAtTime = StateHistoryColoringFunctions.DISTANCE.getColoringFunction().apply(trajectory, time);
				return gcp.getColor(0.859, 9.66, valueAtTime);
			}
			else
				return gcp.getColor(0.0, 1.0, 0.7);
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

	/**
	 * @return the trajectoryColor
	 */
	public double[] getTrajectoryColor()
	{
		return trajectoryColor;
	}

	/**
	 * @return the trajectoryLineThickness
	 */
	public double getTrajectoryLineThickness()
	{
		return trajectoryLineThickness;
	}
}