package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import vtk.vtkCellArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.IPositionOrientation;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedImageActor extends PlannedInstrumentDataActor
{
	public PlannedImageActor(PlannedInstrumentData data, SmallBodyModel smallBodyModel, IPositionOrientation positionOrientationManager)
	{
		super(data, smallBodyModel, positionOrientationManager);
	}

	void calculateFrustum(double time)
	{
		calculatePosOrientationAtTime(time);

		if (frustumActor == null)
			return;
		// System.out.println("recalculateFrustum()");
		frustumPolyData = new vtkPolyData();

		vtkPoints points = new vtkPoints();
		vtkCellArray lines = new vtkCellArray();

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(2);

		double maxFrustumRayLength = MathUtil.vnorm(spacecraftPosition)
				+ model.getBoundingBoxDiagonalLength();
		double[] origin = spacecraftPosition;
		double[] UL =
		{ origin[0] + frustum1[0] * maxFrustumRayLength,
				origin[1] + frustum1[1] * maxFrustumRayLength,
				origin[2] + frustum1[2] * maxFrustumRayLength };
		double[] UR =
		{ origin[0] + frustum2[0] * maxFrustumRayLength,
				origin[1] + frustum2[1] * maxFrustumRayLength,
				origin[2] + frustum2[2] * maxFrustumRayLength };
		double[] LL =
		{ origin[0] + frustum3[0] * maxFrustumRayLength,
				origin[1] + frustum3[1] * maxFrustumRayLength,
				origin[2] + frustum3[2] * maxFrustumRayLength };
		double[] LR =
		{ origin[0] + frustum4[0] * maxFrustumRayLength,
				origin[1] + frustum4[1] * maxFrustumRayLength,
				origin[2] + frustum4[2] * maxFrustumRayLength };

		double minFrustumRayLength = MathUtil.vnorm(spacecraftPosition)
				- model.getBoundingBoxDiagonalLength();
		maxFrustumDepth = maxFrustumRayLength; // a reasonable
												// approximation
												// for a max
												// bound on the
												// frustum depth
		minFrustumDepth = minFrustumRayLength; // a reasonable
												// approximation
												// for a min
												// bound on the
												// frustum depth

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

		frustumPolyData.SetPoints(points);
		frustumPolyData.SetLines(lines);

		vtkPolyDataMapper frusMapper = new vtkPolyDataMapper();
		frusMapper.SetInputData(frustumPolyData);

		frustumActor.SetMapper(frusMapper);
	}

	private void calculateBoundary(double time)
	{
		calculatePosOrientationAtTime(time);

		// Using the frustum, go around the boundary of the frustum and
		// intersect with
		// the asteroid.
	    vtkPolyData emptyPolyData = new vtkPolyData();

		boundaryPolydata.DeepCopy(emptyPolyData);
		vtkPoints points = boundaryPolydata.GetPoints();
		vtkCellArray verts = boundaryPolydata.GetVerts();

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(1);

		vtksbCellLocator cellLocator = model.getCellLocator();

		vtkGenericCell cell = new vtkGenericCell();

		// Note it doesn't matter what image size we use, even
		// if it's not the same size as the original image. Just
		// needs to large enough so enough points get drawn.
		final int IMAGE_WIDTH = 475;
		final int IMAGE_HEIGHT = 475;

		int count = 0;

		double[] corner1 =
		{ spacecraftPosition[0] + frustum1[0], spacecraftPosition[1] + frustum1[1],
				spacecraftPosition[2] + frustum1[2] };
		double[] corner2 =
		{ spacecraftPosition[0] + frustum2[0], spacecraftPosition[1] + frustum2[1],
				spacecraftPosition[2] + frustum2[2] };
		double[] corner3 =
		{ spacecraftPosition[0] + frustum3[0], spacecraftPosition[1] + frustum3[1],
				spacecraftPosition[2] + frustum3[2] };
		double[] vec12 =
		{ corner2[0] - corner1[0], corner2[1] - corner1[1], corner2[2] - corner1[2] };
		double[] vec13 =
		{ corner3[0] - corner1[0], corner3[1] - corner1[1], corner3[2] - corner1[2] };

		// double horizScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1,
		// frustum3) / 2.0 ) / IMAGE_HEIGHT;
		// double vertScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1,
		// frustum2) / 2.0 ) / IMAGE_WIDTH;

		double scdist = MathUtil.vnorm(spacecraftPosition);

		for (int i = 0; i < IMAGE_HEIGHT; ++i)
		{
			// Compute the vector on the left of the row.
			double fracHeight = ((double) i / (double) (IMAGE_HEIGHT - 1));
			double[] left =
			{ corner1[0] + fracHeight * vec13[0], corner1[1] + fracHeight * vec13[1],
					corner1[2] + fracHeight * vec13[2] };

			for (int j = 0; j < IMAGE_WIDTH; ++j)
			{
				if (j == 1 && i > 0 && i < IMAGE_HEIGHT - 1)
				{
					j = IMAGE_WIDTH - 2;
					continue;
				}

				double fracWidth = ((double) j / (double) (IMAGE_WIDTH - 1));
				double[] vec =
				{ left[0] + fracWidth * vec12[0], left[1] + fracWidth * vec12[1], left[2] + fracWidth * vec12[2] };
				vec[0] -= spacecraftPosition[0];
				vec[1] -= spacecraftPosition[1];
				vec[2] -= spacecraftPosition[2];
				MathUtil.unorm(vec, vec);

				double[] lookPt =
				{ spacecraftPosition[0] + 2.0 * scdist * vec[0], spacecraftPosition[1] + 2.0 * scdist * vec[1],
						spacecraftPosition[2] + 2.0 * scdist * vec[2] };

				double tol = 1e-6;
				double[] t = new double[1];
				double[] x = new double[3];
				double[] pcoords = new double[3];
				int[] subId = new int[1];
				int[] cellId = new int[1];
				int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId,
						cellId, cell);

				if (result > 0)
				{
					double[] closestPoint = x;

					// double horizPixelScale = closestDist * horizScaleFactor;
					// double vertPixelScale = closestDist * vertScaleFactor;

					points.InsertNextPoint(closestPoint);
					idList.SetId(0, count);
					verts.InsertNextCell(idList);

					++count;
				}
			}
		}

		PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(boundaryPolydata, model.getSmallBodyPolyData(),
				model.getCellNormals(), model.getCellLocator(),
				3.0 * model.getMinShiftAmount());

		boundaryPolydata.Modified();
		boundaryMapper.SetInputData(boundaryPolydata);

		boundaryActor.SetMapper(boundaryMapper);
		boundaryActor.GetProperty().SetColor(1.0, 0.0, 0.0);
		boundaryActor.GetProperty().SetPointSize(1.0);

		model.shiftPolyLineInNormalDirection(boundaryPolydata, offset);
		//TODO move this to a calling function
//        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

}
