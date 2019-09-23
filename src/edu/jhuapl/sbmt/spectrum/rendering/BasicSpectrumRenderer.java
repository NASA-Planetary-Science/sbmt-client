package edu.jhuapl.sbmt.spectrum.rendering;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkFeatureEdges;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTriangle;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics.Sample;

/**
 * Base renderer for spectra
 * @author steelrj1
 *
 */
public class BasicSpectrumRenderer<S extends BasicSpectrum> extends AbstractModel implements IBasicSpectrumRenderer<S>
{
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected boolean footprintGenerated = false;

	protected vtkActor selectionActor = new vtkActor();
    protected vtkPolyData selectionPolyData = new vtkPolyData();
    protected vtkPolyData footprint;
    protected vtkPolyData shiftedFootprint;
    protected vtkActor footprintActor;
    protected vtkActor frustumActor;
    protected List<vtkProp> footprintActors = new ArrayList<vtkProp>();
    protected vtkPolyData outlinePolyData = new vtkPolyData();
    boolean isOutlineShowing;

    protected vtkActor toSunVectorActor = new vtkActor();
    protected vtkPolyData toSunVectorPolyData = new vtkPolyData();
    protected boolean isToSunVectorShowing = false;

    protected boolean headless = false;

    protected boolean isSelected;
    protected double footprintHeight;

    protected vtkActor outlineActor = new vtkActor();
    protected ISmallBodyModel smallBodyModel;
    protected S spectrum;
    public static final String faceAreaFractionArrayName="faceAreaFraction";

	protected double[] spacecraftPosition;
	protected double[] frustum1;
	protected double[] frustum2;
	protected double[] frustum3;
	protected double[] frustum4;
    protected boolean showFrustum = false;


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
            createSelectionActor();
            createOutlineActor();
            createToSunVectorActor();
        }
	}

	@Override
	public void generateFootprint()
    {
        if (!spectrum.getLatLons().isEmpty())
        {
            vtkPolyData tmp = smallBodyModel.computeFrustumIntersection(
                    spacecraftPosition, frustum1, frustum2, frustum3, frustum4);
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
            double[] color = getChannelColor();
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
        double[] color = getChannelColor();
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

	@Override
    public double[] getChannelColor()
    {
        if (spectrum.getColoringStyle() == SpectrumColoringStyle.EMISSION_ANGLE)
        {
            //This calculation is using the average emission angle over the spectrum, which doesn't exacty match the emission angle of the
            //boresight - no good way to calculate this data at the moment.  Olivier said this is fine.  Need to present a way to either have this option or the old one via RGB for coloring
//        	AdvancedSpectrumRenderer renderer = new AdvancedSpectrumRenderer(this, smallBodyModel, false);
            List<Sample> sampleEmergenceAngle = SpectrumStatistics.sampleEmergenceAngle(this, new Vector3D(spacecraftPosition));
            Colormap colormap = Colormaps.getNewInstanceOfBuiltInColormap("OREX Scalar Ramp");
            colormap.setRangeMin(0.0);  //was 5.4
            colormap.setRangeMax(90.00); //was 81.7

            Color color2 = colormap.getColor(SpectrumStatistics.getWeightedMean(sampleEmergenceAngle));
            double[] color = new double[3];
            color[0] = color2.getRed()/255.0;
            color[1] = color2.getGreen()/255.0;
            color[2] = color2.getBlue()/255.0;
            return color;
        }
        else
        {
            //TODO: What do we do for L3 data here?  It has less XAxis points than the L2 data, so is the coloring scheme different?
            double[] color = new double[3];
            BasicSpectrumInstrument instrument = spectrum.getInstrument();
            int[] channelsToColorBy = spectrum.getChannelsToColorBy();
            double[] channelsColoringMinValue = spectrum.getChannelsColoringMinValue();
            double[] channelsColoringMaxValue = spectrum.getChannelsColoringMaxValue();

            for (int i=0; i<3; ++i)
            {
                double val = 0.0;
                if (spectrum.getChannelsToColorBy()[i] < instrument.getBandCenters().length)
                {
                    val = spectrum.getSpectrum()[channelsToColorBy[i]];
                }
                else if (channelsToColorBy[i] < instrument.getBandCenters().length + instrument.getSpectrumMath().getDerivedParameters().length)
                    val = spectrum.evaluateDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length);
                else
                    val = instrument.getSpectrumMath().evaluateUserDefinedDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length-instrument.getSpectrumMath().getDerivedParameters().length, spectrum.getSpectrum());

                if (val < 0.0)
                    val = 0.0;
                else if (val > 1.0)
                    val = 1.0;

                double slope = 1.0 / (channelsColoringMaxValue[i] - channelsColoringMinValue[i]);
                color[i] = slope * (val - channelsColoringMinValue[i]);
            }
            return color;
        }
    }
}
