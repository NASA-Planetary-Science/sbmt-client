package edu.jhuapl.sbmt.stateHistory.rendering;

import java.awt.Color;
import java.util.function.Function;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyLine;
import vtk.vtkUnsignedCharArray;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

public class TrajectoryActor extends vtkActor
{
    private Trajectory trajectory;
    private vtkPolyData trajectoryPolylines;
    private vtkPolyDataMapper trajectoryMapper = new vtkPolyDataMapper();
    private double[] trajectoryColor = {0, 255, 255, 255};
    private double trajectoryLineThickness = 1;
    private String trajectoryName = ""; // default name and description fields
    private double minFraction = 0.0;
    private double maxFraction = 1.0;
    private vtkUnsignedCharArray colors;
    private int size;
    private vtkPolyData trajectoryPolyline;
    private Function<Double, Double> coloringFunction;
    private Colormap colormap;

	public TrajectoryActor(Trajectory trajectory)
	{
		this.trajectory = trajectory;
		this.trajectoryColor = trajectory.getTrajectoryColor();

		createTrajectoryPolyData2();

		SetMapper(trajectoryMapper);

		SetVisibility(1);

	}

	private void createTrajectoryPolyData2()
	{
		int cellId = 0;
        vtkIdList idList = new vtkIdList();
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

//            colors.InsertNextTuple4(trajectoryColor[0], trajectoryColor[1], trajectoryColor[2], 150.0);	//last one is alpha

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

	private void updateShownSegments()
	{
		int minValueToColor = (int)(minFraction*size);
		int maxValueToColor = (int)(maxFraction*size);
		Range<Integer> coloredRange = Ranges.closed(minValueToColor, maxValueToColor);
		double alphaColor = 150.0;

		vtkCellArray edges = new vtkCellArray();
		for (int i=0; i<size; i++)
        {
			if (!coloredRange.contains(i)) continue;
			vtkLine edge = new vtkLine();
        	edge.GetPointIds().SetId(0, i);
        	edge.GetPointIds().SetId(1, (i+1));
        	edges.InsertNextCell(edge);
        	Color colorAtIndex = getColorAtIndex(i);
        	colors.SetTuple4(i, colorAtIndex.getRed(), colorAtIndex.getGreen(), colorAtIndex.getBlue(), colorAtIndex.getAlpha());
//			alphaColor = 150.0;
//			if (!coloredRange.contains(i)) alphaColor = 0.0;
//			colors.SetTuple4(i, trajectoryColor[0], trajectoryColor[1], trajectoryColor[2], alphaColor);
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
	public void setColoringFunction(Function<Double, Double> coloringFunction, Colormap colormap)
	{
		this.coloringFunction = coloringFunction;
		this.colormap = colormap;
	}

	private Color getColorAtIndex(int index)
	{
		if (coloringFunction == null) return new Color((int)trajectoryColor[0], (int)trajectoryColor[1], (int)trajectoryColor[2], (int)trajectoryColor[3]);
		double time = trajectory.getTime().get(index);
		double valueAtTime = coloringFunction.apply(time);
		return colormap.getColor(valueAtTime);
	}

	private void createTrajectoryPolyData()
	{
		createTrajectoryPolyData2();
//		trajectoryPolylines = new vtkPolyData();
//
//		int cellId = 0;
//        vtkIdList idList = new vtkIdList();
//        vtkPoints points = new vtkPoints();
//        vtkCellArray lines = new vtkCellArray();
//        vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
//        colors.SetNumberOfComponents(4);
//
//        Trajectory traj =  trajectory;
//        traj.setCellId(cellId);
//
//        int size = traj.getX().size();
//        idList.SetNumberOfIds(size);
//        System.out.println("TrajectoryActor: createTrajectoryPolyData: going from " + (int)(minFraction*size) + " to " + maxFraction*size);
//        for (int i=(int)(minFraction*size);i<maxFraction*size;++i)
//        {
//            Double x = traj.getX().get(i);
//            Double y = traj.getY().get(i);
//            Double z = traj.getZ().get(i);
//
//            points.InsertNextPoint(x, y, z);
//            idList.SetId(i, i);
//        }
//        System.out.println("TrajectoryActor: createTrajectoryPolyData: done with for loop");
//        lines.InsertNextCell(idList);
//        colors.InsertNextTuple4(trajectoryColor[0], trajectoryColor[1], trajectoryColor[2], trajectoryColor[3]);	//last one is alpha
//
//        vtkPolyData trajectoryPolyline = new vtkPolyData();
//        trajectoryPolyline.SetPoints(points);
//        trajectoryPolyline.SetLines(lines);
//        trajectoryPolyline.GetCellData().SetScalars(colors);
//
//        trajectoryPolylines = trajectoryPolyline;
//        System.out.println("TrajectoryActor: createTrajectoryPolyData: setting mapper");
//        trajectoryMapper.SetInputData(trajectoryPolyline);
//        trajectoryMapper.Modified();
//        GetProperty().SetLineWidth(trajectoryLineThickness);
//
//		trajectoryMapper.SetInputData(trajectoryPolylines);
//		System.out.println("TrajectoryActor: createTrajectoryPolyData: updating");
//		trajectoryMapper.Update();

	}

	public TrajectoryActor(long id)
	{
		super(id);
		// TODO Auto-generated constructor stub
	}

	public void setTrajectoryLineThickness(double value)
    {
        this.trajectoryLineThickness = value;
        // recreate poly data with new thickness
        GetProperty().SetLineWidth(trajectoryLineThickness);
        createTrajectoryPolyData();
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null , null);
    }

    public double getTrajectoryLineThickness()
    {
        return trajectoryLineThickness;
    }

    public double[] getTrajectoryColor()
    {
        return trajectoryColor;
    }

    public void setTrajectoryColor(double[] color)
    {
        this.trajectoryColor = color;
        // recreate poly data with new color
        createTrajectoryPolyData();
//        if (showing) {
//            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//        }
    }

    public void setTrajectoryName(String name)
    {
        trajectoryName = name;
    }

    public String getTrajectoryName()
	{
		return trajectoryName;
	}

	public void showTrajectory(boolean show)
    {
    	if (show == true) { VisibilityOn(); } else { VisibilityOff(); }
		Modified();
    }

	public void setMinMaxFraction(double min, double max)
	{
		this.minFraction = min;
		this.maxFraction = max;
		updateShownSegments();
//		createTrajectoryPolyData();
	}

}
