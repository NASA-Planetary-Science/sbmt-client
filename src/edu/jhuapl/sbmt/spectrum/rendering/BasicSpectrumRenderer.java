package edu.jhuapl.sbmt.spectrum.rendering;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkDataArray;
import vtk.vtkDoubleArray;
import vtk.vtkFeatureEdges;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataNormals;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTriangle;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;

/**
 * Base renderer for spectra
 * @author steelrj1
 *
 */
public class BasicSpectrumRenderer<S extends BasicSpectrum> extends AbstractModel implements IBasicSpectrumRenderer<S>
{
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected boolean footprintGenerated = false;

	protected vtkActor selectionActor;
    protected vtkPolyData selectionPolyData = new vtkPolyData();
    protected vtkPolyData footprint;
    protected vtkPolyData shiftedFootprint;
    protected vtkActor footprintActor;
    protected vtkActor frustumActor;
    protected List<vtkProp> footprintActors = new ArrayList<vtkProp>();
    protected vtkPolyData outlinePolyData = new vtkPolyData();
    boolean isOutlineShowing;

    protected vtkActor toSunVectorActor;
    protected vtkPolyData toSunVectorPolyData = new vtkPolyData();
    protected boolean isToSunVectorShowing = false;

    protected boolean headless = false;

    protected boolean isSelected;
    protected double footprintHeight;

    protected vtkActor outlineActor;
    protected ISmallBodyModel smallBodyModel;
    protected S spectrum;
    public static final String faceAreaFractionArrayName="faceAreaFraction";

	protected double[] spacecraftPosition;
	protected double[] frustum1;
	protected double[] frustum2;
	protected double[] frustum3;
	protected double[] frustum4;
    protected boolean showFrustum = false;
    protected double[] color;

    private boolean normalsGenerated = false;
    private vtkPolyDataNormals normalsFilter;
    private double minIncidence = Double.MAX_VALUE;
    private double maxIncidence = -Double.MAX_VALUE;
    private double minEmission = Double.MAX_VALUE;
    private double maxEmission = -Double.MAX_VALUE;
    private double minPhase = Double.MAX_VALUE;
    private double maxPhase = -Double.MAX_VALUE;


	public BasicSpectrumRenderer(S spectrum, ISmallBodyModel smallBodyModel, boolean headless)
	{
		this.spectrum = spectrum;
		spacecraftPosition = spectrum.getSpacecraftPosition();
        frustum1 = spectrum.getFrustum1();
        frustum2 = spectrum.getFrustum2();
        frustum3 = spectrum.getFrustum3();
        frustum4 = spectrum.getFrustum4();

        this.smallBodyModel = smallBodyModel;
		footprintHeight = smallBodyModel.getMinShiftAmount();
        this.headless = headless;
        if (headless == false)
        {
        	selectionActor = new vtkActor();
            createSelectionActor();
            outlineActor = new vtkActor();
            createOutlineActor();
            toSunVectorActor = new vtkActor();
            createToSunVectorActor();
        }
	}

	@Override
	public void generateFootprint()
    {
        if (!spectrum.getLatLons().isEmpty())
        {
        	spectrum.readPointingFromInfoFile();
			try
			{
				spectrum.readSpectrumFromFile();
			}
			catch (SpectrumIOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			spacecraftPosition = spectrum.getSpacecraftPosition();
	        frustum1 = spectrum.getFrustum1();
	        frustum2 = spectrum.getFrustum2();
	        frustum3 = spectrum.getFrustum3();
	        frustum4 = spectrum.getFrustum4();

            vtkPolyData tmp = smallBodyModel.computeFrustumIntersection(
            		spectrum.getFrustumOrigin(),
            		spectrum.getFrustumCorner(0), spectrum.getFrustumCorner(1),
                    spectrum.getFrustumCorner(2), spectrum.getFrustumCorner(3));
            if (tmp == null)
				return;
            footprint = new vtkPolyData();
			shiftedFootprint = new vtkPolyData();
            vtkDoubleArray faceAreaFraction = new vtkDoubleArray();
            faceAreaFraction.SetName(faceAreaFractionArrayName);
            Frustum frustum = new Frustum(spectrum.getFrustumOrigin(),
            		spectrum.getFrustumCorner(0), spectrum.getFrustumCorner(1),
                    spectrum.getFrustumCorner(2), spectrum.getFrustumCorner(3));
            for (int c = 0; c < tmp.GetNumberOfCells(); c++)
            {
                vtkIdTypeArray originalIds = (vtkIdTypeArray) tmp.GetCellData()
                        .GetArray(GenericPolyhedralModel.cellIdsArrayName);
                int originalId = originalIds.GetValue(c);
                vtkTriangle tri = (vtkTriangle) smallBodyModel
                        .getSmallBodyPolyData().GetCell(originalId); // tri on
                                                                     // original
                                                                     // body
                                                                     // model
                vtkTriangle ftri = (vtkTriangle) tmp.GetCell(c); // tri on
                                                                 // footprint
                faceAreaFraction.InsertNextValue(
                        ftri.ComputeArea() / tri.ComputeArea());
            }
            tmp.GetCellData().AddArray(faceAreaFraction);

            if (tmp != null)
            {
                // Need to clear out scalar data since if coloring data is being
                // shown,
                // then the color might mix-in with the image.
                tmp.GetCellData().SetScalars(null);
                tmp.GetPointData().SetScalars(null);

                footprint.DeepCopy(tmp);

                shiftedFootprint.DeepCopy(tmp);
                PolyDataUtil.shiftPolyDataInMeanNormalDirection(
                        shiftedFootprint, footprintHeight);

                createSelectionPolyData();
                createSelectionActor();
                createToSunVectorPolyData();
                createToSunVectorActor();
                createOutlinePolyData();
                createOutlineActor();
            }
        }
    }

	@Override
	public List<vtkProp> getProps()
    {
        if (footprintActor == null && !spectrum.getLatLons().isEmpty())
        {
            generateFootprint();

            vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
            footprintMapper.SetInputData(shiftedFootprint);
            // footprintMapper.SetResolveCoincidentTopologyToPolygonOffset();
            // footprintMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-.002,
            // -2.0);
            footprintMapper.Update();

            footprintActor = new vtkActor();
            footprintActor.SetMapper(footprintMapper);
            vtkProperty footprintProperty = footprintActor.GetProperty();
            if (color != null)
            	footprintProperty.SetColor(color[0], color[1], color[2]);
            footprintProperty.SetLineWidth(2.0);
            footprintProperty.LightingOff();

            footprintActors.add(footprintActor);

            /*
             * // Compute the bounding edges of this surface vtkFeatureEdges
             * edgeExtracter = new vtkFeatureEdges();
             * edgeExtracter.SetInput(shiftedFootprint);
             * edgeExtracter.BoundaryEdgesOn(); edgeExtracter.FeatureEdgesOff();
             * edgeExtracter.NonManifoldEdgesOff();
             * edgeExtracter.ManifoldEdgesOff(); edgeExtracter.Update();
             *
             * vtkPolyDataMapper edgeMapper = new vtkPolyDataMapper();
             * edgeMapper.SetInputConnection(edgeExtracter.GetOutputPort());
             * edgeMapper.ScalarVisibilityOff();
             * //edgeMapper.SetResolveCoincidentTopologyToPolygonOffset();
             * //edgeMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(
             * -.004, -4.0); edgeMapper.Update();
             *
             * vtkActor edgeActor = new vtkActor();
             * edgeActor.SetMapper(edgeMapper);
             * edgeActor.GetProperty().SetColor(0.0, 0.39, 0.0);
             * edgeActor.GetProperty().SetLineWidth(2.0);
             * edgeActor.GetProperty().LightingOff();
             * footprintActors.add(edgeActor);
             */
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
            frustumProperty.SetLineWidth(2.0);
            frustumActor.VisibilityOff();

            footprintActors.add(frustumActor);
        }

        footprintActors.add(selectionActor);
        footprintActors.add(toSunVectorActor);
        footprintActors.add(outlineActor);
        return footprintActors;
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            // System.out.println("updating spectral image");
            generateFootprint();
            setUnselected();

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

	@Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
	public void shiftFootprintToHeight(double h)
    {
        vtkPolyData tmp = smallBodyModel.computeFrustumIntersection(
        		spectrum.getSpacecraftPosition(), spectrum.getFrustum1(), spectrum.getFrustum2(), spectrum.getFrustum3(), spectrum.getFrustum4());
        shiftedFootprint.DeepCopy(tmp);
        PolyDataUtil.shiftPolyDataInMeanNormalDirection(shiftedFootprint, h);
        createSelectionPolyData();
        createOutlinePolyData();
        //
        if (isSelected)
            selectionActor.VisibilityOn();
        //
        ((vtkPolyDataMapper) footprintActor.GetMapper())
                .SetInputData(shiftedFootprint);
        footprintActor.GetMapper().Update();
        ((vtkPolyDataMapper) selectionActor.GetMapper())
                .SetInputData(selectionPolyData);
        selectionActor.GetMapper().Update();
        ((vtkPolyDataMapper) outlineActor.GetMapper())
                .SetInputData(selectionPolyData);
        outlineActor.GetMapper().Update();

        //
        footprintHeight = h;
    }

    protected void createSelectionPolyData()
    {
        vtkFeatureEdges edgeFilter = new vtkFeatureEdges();
        edgeFilter.SetInputData(getShiftedFootprint());
        edgeFilter.BoundaryEdgesOn();
        edgeFilter.FeatureEdgesOff();
        edgeFilter.ManifoldEdgesOff();
        edgeFilter.NonManifoldEdgesOff();
        edgeFilter.Update();
        selectionPolyData.DeepCopy(edgeFilter.GetOutput());
    }

    protected void createSelectionActor()
    {
        if (headless == false)
        {
        	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        	mapper.SetInputData(selectionPolyData);
        	mapper.Update();
        	selectionActor.SetMapper(mapper);
        	selectionActor.VisibilityOff();
        	selectionActor.GetProperty().EdgeVisibilityOn();
        	selectionActor.GetProperty().SetEdgeColor(0.5, 1, 0.5);
        	selectionActor.GetProperty().SetLineWidth(5);
    	}
    }

    protected void createOutlinePolyData()
    {
        vtkFeatureEdges edgeFilter = new vtkFeatureEdges();
        edgeFilter.SetInputData(getShiftedFootprint());
        edgeFilter.BoundaryEdgesOn();
        edgeFilter.FeatureEdgesOff();
        edgeFilter.ManifoldEdgesOff();
        edgeFilter.NonManifoldEdgesOff();
        edgeFilter.Update();
        outlinePolyData.DeepCopy(edgeFilter.GetOutput());
    }

    protected void createOutlineActor()
    {
        if (headless == false)
        {
        	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        	mapper.SetInputData(outlinePolyData);
        	mapper.Update();
        	outlineActor.SetMapper(mapper);
        	outlineActor.VisibilityOff();
        	outlineActor.GetProperty().EdgeVisibilityOn();
        	outlineActor.GetProperty().SetEdgeColor(0.4, 0.4, 1);
        	outlineActor.GetProperty().SetLineWidth(2);
   	 	}
    }

    protected void createToSunVectorPolyData()
    {
    	vtkPoints points = new vtkPoints();
        vtkCellArray cells = new vtkCellArray();
        Vector3D footprintCenter = new Vector3D(
                getUnshiftedFootprint().GetCenter());
        int id1 = points.InsertNextPoint(footprintCenter.toArray());
        int id2 = points.InsertNextPoint(footprintCenter
                .add(new Vector3D(spectrum.getToSunUnitVector()).scalarMultiply(spectrum.getToSunVectorLength()))
                .toArray());
        vtkLine line = new vtkLine();
        line.GetPointIds().SetId(0, id1);
        line.GetPointIds().SetId(1, id2);
        cells.InsertNextCell(line);
        toSunVectorPolyData.SetPoints(points);
        toSunVectorPolyData.SetLines(cells);
    }

    protected void createToSunVectorActor()
    {
        if (headless == false)
        {
        	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        	mapper.SetInputData(toSunVectorPolyData);
        	mapper.Update();
        	toSunVectorActor.SetMapper(mapper);
        	toSunVectorActor.VisibilityOff();
        	toSunVectorActor.GetProperty().SetColor(1, 1, 0.5);
    	}
    }

    /**
     * The shifted footprint is the original footprint shifted slightly in the
     * normal direction so that it will be rendered correctly and not obscured
     * by the asteroid.
     *
     * @return
     */
    @Override
	public vtkPolyData getShiftedFootprint()
    {
        return shiftedFootprint;
    }

    /**
     * The original footprint whose cells exactly overlap the original asteroid.
     * If rendered as is, it would interfere with the asteroid.
     *
     * @return
     */
    @Override
	public vtkPolyData getUnshiftedFootprint()
    {
        return footprint;
    }

    @Override
	public void Delete()
    {
        footprint.Delete();
        shiftedFootprint.Delete();
    }

    @Override
	public void setSelected()
    {
//        isSelected = true;
//        selectionActor.VisibilityOn();
//        selectionActor.Modified();
    }

    @Override
	public void setUnselected()
    {
        isSelected = false;
        selectionActor.VisibilityOff();
        selectionActor.Modified();
    }

    @Override
	public void setShowFrustum(boolean show)
    {
    	this.showFrustum = show;

        if (show)
        {
            frustumActor.VisibilityOn();
        }
        else
        {
            frustumActor.VisibilityOff();
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, this);
    }

    @Override
    public void setVisible(boolean b)
    {
        // TODO Auto-generated method stub
        if (b)
        {
            footprintActor.VisibilityOn();
        }
        else
        {
            footprintActor.VisibilityOff();
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, this);
    }

    @Override
    public boolean isVisible()
    {
    	if (footprintActor == null) return false;
        return footprintActor.GetVisibility() == 0 ? false : true;
    }

    @Override
	public void updateChannelColoring()
    {
    	if (footprintActor == null) return;
        vtkProperty footprintProperty = footprintActor.GetProperty();
        footprintProperty.SetColor(color[0], color[1], color[2]);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, this);
    }

    @Override
	public void setShowToSunVector(boolean b)
    {
        isToSunVectorShowing = b;
        if (isToSunVectorShowing)
            toSunVectorActor.VisibilityOn();
        else
            toSunVectorActor.VisibilityOff();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
	public void setShowOutline(boolean b)
    {
        isOutlineShowing = b;
        if (isOutlineShowing)
            outlineActor.VisibilityOn();
        else
            outlineActor.VisibilityOff();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
	public vtkPolyData getSelectionPolyData()
    {
        return selectionPolyData;
    }

    @Override
	public double getMinFootprintHeight()
    {
        return smallBodyModel.getMinShiftAmount();
    }

    @Override
	public boolean isSelected()
    {
        return isSelected;
    }

    @Override
	public boolean isToSunVectorShowing()
    {
        return isToSunVectorShowing;
    }

    @Override
	public boolean isOutlineShowing()
    {
        return isOutlineShowing;
    }

	public S getSpectrum()
	{
		return spectrum;
	}

    public boolean isFrustumShowing()
    {
        return showFrustum;
    }

	public vtkActor getOutlineActor()
	{
		return outlineActor;
	}

	public double[] getSpacecraftPosition()
	{
		return spacecraftPosition;
	}

	public double[] getColor()
	{
		return color;
	}

	public void setColor(double[] color)
	{
		this.color = color;
	}

	void computeCellNormals()
    {
        if (normalsGenerated == false)
        {
        	normalsFilter = new vtkPolyDataNormals();
            normalsFilter.SetInputData(footprint);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(0);
            // normalsFilter.AutoOrientNormalsOn();
            // normalsFilter.ConsistencyOn();
            normalsFilter.SplittingOff();
            normalsFilter.Update();

            if (footprint != null)
            {
                vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
                footprint.DeepCopy(normalsFilterOutput);
                normalsGenerated = true;
            }
        }
    }

    // Computes the incidence, emission, and phase at a point on the footprint with
    // a given normal.
    // (I.e. the normal of the plate which the point is lying on).
    // The output is a 3-vector with the first component equal to the incidence,
    // the second component equal to the emission and the third component equal to
    // the phase.
    double[] computeIlluminationAnglesAtPoint(double[] pt, double[] normal)
    {
        double[] scvec = {
        		spacecraftPosition[0] - pt[0],
        		spacecraftPosition[1] - pt[1],
        		spacecraftPosition[2] - pt[2]};

        double[] sunVectorAdjusted = spectrum.getToSunUnitVector();
        double incidence = MathUtil.vsep(normal, sunVectorAdjusted) * 180.0 / Math.PI;
        double emission = MathUtil.vsep(normal, scvec) * 180.0 / Math.PI;
        double phase = MathUtil.vsep(sunVectorAdjusted, scvec) * 180.0 / Math.PI;

        double[] angles = { incidence, emission, phase };

        return angles;
    }

	void computeIlluminationAngles()
    {
        if (footprintGenerated == false)
            generateFootprint();

        computeCellNormals();

        int numberOfCells = footprint.GetNumberOfCells();

        vtkPoints points = footprint.GetPoints();
        vtkCellData footprintCellData = footprint.GetCellData();
        vtkDataArray normals = footprintCellData.GetNormals();

        this.minEmission = Double.MAX_VALUE;
        this.maxEmission = -Double.MAX_VALUE;
        this.minIncidence = Double.MAX_VALUE;
        this.maxIncidence = -Double.MAX_VALUE;
        this.minPhase = Double.MAX_VALUE;
        this.maxPhase = -Double.MAX_VALUE;

        for (int i = 0; i < numberOfCells; ++i)
        {
            vtkCell cell = footprint.GetCell(i);
            double[] pt0 = points.GetPoint(cell.GetPointId(0));
            double[] pt1 = points.GetPoint(cell.GetPointId(1));
            double[] pt2 = points.GetPoint(cell.GetPointId(2));
            double[] centroid = {
                    (pt0[0] + pt1[0] + pt2[0]) / 3.0,
                    (pt0[1] + pt1[1] + pt2[1]) / 3.0,
                    (pt0[2] + pt1[2] + pt2[2]) / 3.0
            };
            double[] normal = normals.GetTuple3(i);

            double[] angles = computeIlluminationAnglesAtPoint(centroid, normal);
            double incidence = angles[0];
            double emission = angles[1];
            double phase = angles[2];

            if (incidence < minIncidence)
                minIncidence = incidence;
            if (incidence > maxIncidence)
                maxIncidence = incidence;
            if (emission < minEmission)
                minEmission = emission;
            if (emission > maxEmission)
                maxEmission = emission;
            if (phase < minPhase)
                minPhase = phase;
            if (phase > maxPhase)
                maxPhase = phase;
            cell.Delete();
        }

        points.Delete();
        footprintCellData.Delete();
        if (normals != null)
            normals.Delete();
    }

	public double getMinIncidence()
	{
		return minIncidence;
	}

	public double getMaxIncidence()
	{
		return maxIncidence;
	}

	public double getMinEmission()
	{
		return minEmission;
	}

	public double getMaxEmission()
	{
		return maxEmission;
	}

	public double getMinPhase()
	{
		return minPhase;
	}

	public double getMaxPhase()
	{
		return maxPhase;
	}
}
