package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedSpectrumActor extends PlannedInstrumentDataActor
{
	private PlannedInstrumentData data;
	private SmallBodyModel model;
//	private IPositionOrientation positionOrientationManager;

	public PlannedSpectrumActor(PlannedInstrumentData data, SmallBodyModel model/*, IPositionOrientation positionOrientationManager*/)
	{
		super(data, model/*, positionOrientationManager*/);
	}

	//TODO this should really be reworked to be based on the shape of the FOV - it's based on a square, where it may be circular, like with OTES
	void calculateFrustum(double time)
	{
		vtkPolyData frus = new vtkPolyData();

        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(2);

        double dx = MathUtil.vnorm(spacecraftPosition)
                + model.getBoundingBoxDiagonalLength();
        double[] origin = spacecraftPosition;
        double[] UL = { origin[0] + frustum1[0] * dx,
                origin[1] + frustum1[1] * dx,
                origin[2] + frustum1[2] * dx };
        double[] UR = { origin[0] + frustum2[0] * dx,
                origin[1] + frustum2[1] * dx,
                origin[2] + frustum2[2] * dx };
        double[] LL = { origin[0] + frustum3[0] * dx,
                origin[1] + frustum3[1] * dx,
                origin[2] + frustum3[2] * dx };
        double[] LR = { origin[0] + frustum4[0] * dx,
                origin[1] + frustum4[1] * dx,
                origin[2] + frustum4[2] * dx };

        points.InsertNextPoint(spacecraftPosition);
        points.InsertNextPoint(UL);
        points.InsertNextPoint(UR);
        points.InsertNextPoint(LL);
        points.InsertNextPoint(LR);

        idList.SetId(0, 0);
        idList.SetId(1, 1);
        lines.InsertNextCell(idList);
        idList.SetId(0, 0);
        idList.SetId(1, 2);
        lines.InsertNextCell(idList);
        idList.SetId(0, 0);
        idList.SetId(1, 3);
        lines.InsertNextCell(idList);
        idList.SetId(0, 0);
        idList.SetId(1, 4);
        lines.InsertNextCell(idList);

        frus.SetPoints(points);
        frus.SetLines(lines);

        vtkPolyDataMapper frusMapper = new vtkPolyDataMapper();
        frusMapper.SetInputData(frus);

        frustumActor = new vtkActor();
        frustumActor.SetMapper(frusMapper);
        vtkProperty frustumProperty = frustumActor.GetProperty();
        frustumProperty.SetColor(0.0, 1.0, 0.0);
        frustumProperty.SetLineWidth(2.0);
        frustumActor.VisibilityOff();
	}

	private void calculateBoundary(double time)
	{

	}

}
