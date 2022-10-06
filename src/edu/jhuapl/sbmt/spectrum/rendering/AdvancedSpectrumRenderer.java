package edu.jhuapl.sbmt.spectrum.rendering;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTriangle;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.common.client.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;

/**
 * Renderer for spectra like OTES, OVIRS, etc that are circular
 * @author steelrj1
 *
 */
public class AdvancedSpectrumRenderer<S extends BasicSpectrum> extends BasicSpectrumRenderer<S>
{

	public AdvancedSpectrumRenderer(S spectrum, ISmallBodyModel smallBodyModel, boolean headless)
	{
		super(spectrum, smallBodyModel, headless);
		// TODO Auto-generated constructor stub
	}

	@Override
    public List<vtkProp> getProps()
    {
        if (footprintActor == null)
        {
            generateFootprint();

            vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
            footprintMapper.SetInputData(shiftedFootprint);
            footprintMapper.Update();

            footprintActor = new vtkActor();
            footprintActor.SetMapper(footprintMapper);
            vtkProperty footprintProperty = footprintActor.GetProperty();
//            double[] color = getChannelColor();
            if (color != null)
            	footprintProperty.SetColor(color[0], color[1], color[2]);
            footprintProperty.SetLineWidth(2.0f);
            footprintProperty.LightingOff();

            footprintActors.add(footprintActor);
        }

        if (frustumActor == null)
        {
            vtkPolyData frus = new vtkPolyData();

            vtkPoints points = new vtkPoints();
            vtkCellArray lines = new vtkCellArray();

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(2);

            double dx = MathUtil.vnorm(spacecraftPosition)
                    + smallBodyModel.getBoundingBoxDiagonalLength();
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
            frustumProperty.SetLineWidth(2.0f);
            frustumActor.VisibilityOff();

            footprintActors.add(frustumActor);
        }

        footprintActors.add(selectionActor);
        footprintActors.add(toSunVectorActor);
        footprintActors.add(outlineActor);

//        vtkOutlineFilter outlineFilter = new vtkOutlineFilter();
//        outlineFilter.SetInputData(shiftedFootprint);
//        vtkPolyDataMapper bbMapper = new vtkPolyDataMapper();
//        bbMapper.SetInputConnection(outlineFilter.GetOutputPort());
//        vtkActor bbActor = new vtkActor();
//        bbActor.SetMapper(bbMapper);
//        bbActor.GetProperty().SetColor(0.0, 0.0, 1.0);
//        footprintActors.add(bbActor);

        return footprintActors;
    }

	@Override
	public void generateFootprint()
	{
		if (!footprintGenerated)
		{
			spectrum.readPointingFromInfoFile();
			try
			{
				spectrum.readSpectrumFromFile();
			}
			catch (SpectrumIOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			frustum1 = spectrum.getFrustum1();
	        frustum2 = spectrum.getFrustum2();
	        frustum3 = spectrum.getFrustum3();
	        frustum4 = spectrum.getFrustum4();
	        spacecraftPosition = spectrum.getSpacecraftPosition();

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
				int originalId = (int)originalIds.GetValue(c);
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

			footprintGenerated = true;
			computeIlluminationAngles();

		}
	}
}
