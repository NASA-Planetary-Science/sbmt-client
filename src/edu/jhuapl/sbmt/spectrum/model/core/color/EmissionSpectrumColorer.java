package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumColorer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics.Sample;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class EmissionSpectrumColorer<S extends BasicSpectrum> implements ISpectrumColorer<S>
{
    private Colormap currentColormap;
    private List<SpectrumColoringChangedListener> colorChangedListeners;

	public EmissionSpectrumColorer()
	{
		colorChangedListeners = new ArrayList<SpectrumColoringChangedListener>();
		currentColormap = Colormaps.getNewInstanceOfBuiltInColormap("OREX Scalar Ramp");
	}

	@Override
	public double[] getColorForSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		List<Sample> sampleEmergenceAngle = SpectrumStatistics.sampleEmergenceAngle(spectrumRenderer, new Vector3D(spectrumRenderer.getSpacecraftPosition()));
//        Colormap colormap = Colormaps.getNewInstanceOfBuiltInColormap("OREX Scalar Ramp");
		currentColormap.setRangeMin(0.0);  //was 5.4
		currentColormap.setRangeMax(90.00); //was 81.7

        Color color2 = currentColormap.getColor(SpectrumStatistics.getWeightedMean(sampleEmergenceAngle));
        double[] color = new double[3];
        color[0] = color2.getRed()/255.0;
        color[1] = color2.getGreen()/255.0;
        color[2] = color2.getBlue()/255.0;
        return color;
	}

	public void setCurrentColormap(Colormap currentColormap)
	{
		this.currentColormap = currentColormap;
		fireColoringChanged();
	}

	public void addSpectrumColoringChangedListener(SpectrumColoringChangedListener sccl)
	{
		colorChangedListeners.add(sccl);
	}

	private void fireColoringChanged()
	{
		for (SpectrumColoringChangedListener sccl : colorChangedListeners) sccl.coloringChanged();
	}

}
