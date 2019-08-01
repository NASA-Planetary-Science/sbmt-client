package edu.jhuapl.sbmt.spectrum.model.rendering;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkDoubleArray;
import vtk.vtkIdTypeArray;
import vtk.vtkPointLocator;
import vtk.vtkPolyData;
import vtk.vtkTriangle;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

public class AdvancedSpectrumRenderer extends BasicSpectrumRenderer
{

	public AdvancedSpectrumRenderer(BasicSpectrum spectrum, ISmallBodyModel smallBodyModel, boolean headless)
	{
		super(spectrum, smallBodyModel, headless);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateFootprint()
	{
		if (!footprintGenerated)
		{
			spectrum.readPointingFromInfoFile();
			spectrum.readSpectrumFromFile();

			vtkPolyData tmp = smallBodyModel.computeFrustumIntersection(spacecraftPosition, frustum1, frustum2,
					frustum3, frustum4);

			if (tmp == null)
				return;

			Vector3D f1 = new Vector3D(frustum1);
			Vector3D f2 = new Vector3D(frustum2);
			Vector3D f3 = new Vector3D(frustum3);
			Vector3D f4 = new Vector3D(frustum4);
			Vector3D lookUnit = new Vector3D(1, f1, 1, f2, 1, f3, 1, f4);

			double[] angles = new double[]
			{ 22.5, 45, 67.5 };
			for (int i = 0; i < angles.length; i++)

			{
				vtkPolyData tmp2 = new vtkPolyData();

				Rotation rot = new Rotation(lookUnit, Math.toRadians(angles[i]));
				Vector3D g1 = rot.applyTo(f1);
				Vector3D g2 = rot.applyTo(f2);
				Vector3D g3 = rot.applyTo(f3);
				Vector3D g4 = rot.applyTo(f4);

				vtksbCellLocator tree = new vtksbCellLocator();
				tree.SetDataSet(tmp);
				tree.SetTolerance(1e-12);
				tree.BuildLocator();

				vtkPointLocator ploc = new vtkPointLocator();
				ploc.SetDataSet(tmp);
				ploc.SetTolerance(1e-12);
				ploc.BuildLocator();

				tmp2 = PolyDataUtil.computeFrustumIntersection(tmp, tree, ploc, spacecraftPosition, g1.toArray(),
						g2.toArray(), g3.toArray(), g4.toArray());
				if (tmp2 == null)
				{
					try
					{
						throw new Exception("Frustum intersection is null - this needs to be handled better");
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					continue;
				}
				tmp.DeepCopy(tmp2);
			}

			if (tmp == null)
				return;

			vtkDoubleArray faceAreaFraction = new vtkDoubleArray();
			faceAreaFraction.SetName(faceAreaFractionArrayName);
			for (int c = 0; c < tmp.GetNumberOfCells(); c++)
			{
				vtkIdTypeArray originalIds = (vtkIdTypeArray) tmp.GetCellData()
						.GetArray(GenericPolyhedralModel.cellIdsArrayName);
				int originalId = originalIds.GetValue(c);
				vtkTriangle tri = (vtkTriangle) smallBodyModel.getSmallBodyPolyData().GetCell(originalId); // tri
																											// on
																											// original
																											// body
																											// model
				vtkTriangle ftri = (vtkTriangle) tmp.GetCell(c); // tri on
																	// footprint
				faceAreaFraction.InsertNextValue(ftri.ComputeArea() / tri.ComputeArea());
			}
			tmp.GetCellData().AddArray(faceAreaFraction);

			// Need to clear out scalar data since if coloring data is being
			// shown,
			// then the color might mix-in with the image.
			tmp.GetCellData().SetScalars(null);
			tmp.GetPointData().SetScalars(null);

			footprint = new vtkPolyData();
			footprint.DeepCopy(tmp);

			shiftedFootprint = new vtkPolyData();
			shiftedFootprint.DeepCopy(tmp);
			PolyDataUtil.shiftPolyDataInMeanNormalDirection(shiftedFootprint, footprintHeight);

			createSelectionPolyData();
			createSelectionActor();
			createToSunVectorPolyData();
			createToSunVectorActor();
			createOutlinePolyData();
			createOutlineActor();

		}
	}
}
