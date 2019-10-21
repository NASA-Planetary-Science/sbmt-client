package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatistics.Sample;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

/**
 * Model to capture the current coloring state for the spectra on screen.  This manages both the RGB coloring mode values as well as whether
 * the display is in that mode or 1 or more other modes defined by the enum <pre>spectrumColoringStyle</pre>.  Listeners are included to
 * broadcast changes out to interested parties via a <pre>SpectrumColoringChangedListener</pre>
 *
 * @author steelrj1
 *
 */
public class SpectrumColoringModel<S extends BasicSpectrum>
{
    private Vector<SpectrumColoringChangedListener> colorChangedListeners;
    private Double redMinVal = 0.0;
    private Double redMaxVal;
    private Double greenMinVal = 0.0;
    private Double greenMaxVal;
    private Double blueMinVal = 0.0;
    private Double blueMaxVal;
    private boolean greyScaleSelected;
    private int redIndex;
    private int greenIndex;
    private int blueIndex;
    private SpectrumColoringStyle spectrumColoringStyle = SpectrumColoringStyle.RGB;
    private int[] channels;
    private double[] mins;
    private double[] maxs;
    private Colormap currentColormap;
    protected boolean currentlyEditingUserDefinedFunction = false;


	public SpectrumColoringModel()
	{
        this.colorChangedListeners = new Vector<SpectrumColoringChangedListener>();
	}

	public void updateColoring()
	{
        if (isCurrentlyEditingUserDefinedFunction())
            return;
		// If we are currently editing user defined functions
        // (i.e. the dialog is open), do not update the coloring
        // since we may be in an inconsistent state.

        if (isGreyScaleSelected())
        {
        	this.channels = new int[]{redIndex, redIndex, redIndex};
        	this.mins = new double[]{redMinVal, redMinVal, redMinVal};
        	this.maxs = new double[]{redMaxVal, redMaxVal, redMaxVal};
        }
        else
        {
        	this.channels = new int[]{redIndex, greenIndex, blueIndex};
        	this.mins = new double[]{redMinVal, greenMinVal, blueMinVal};
        	this.maxs = new double[]{redMaxVal, greenMaxVal, blueMaxVal};
        }
        fireColoringChanged();
	}

	public double[] getSpectrumColoringForCurrentStyle(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		if (spectrumColoringStyle == SpectrumColoringStyle.EMISSION_ANGLE)
			return getEmissionAngleColorForSpectrum(spectrumRenderer);
		else
			return getRGBColorforSpectrum(spectrumRenderer);
	}

	private double[] getEmissionAngleColorForSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		  List<Sample> sampleEmergenceAngle = SpectrumStatistics.sampleEmergenceAngle(spectrumRenderer, new Vector3D(spectrumRenderer.getSpacecraftPosition()));
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

	private double[] getRGBColorforSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		//TODO: What do we do for L3 data here?  It has less XAxis points than the L2 data, so is the coloring scheme different?
        double[] color = new double[3];
        BasicSpectrumInstrument instrument = spectrumRenderer.getSpectrum().getInstrument();
        int[] channelsToColorBy = spectrumRenderer.getSpectrum().getChannelsToColorBy();
        double[] channelsColoringMinValue = spectrumRenderer.getSpectrum().getChannelsColoringMinValue();
        double[] channelsColoringMaxValue = spectrumRenderer.getSpectrum().getChannelsColoringMaxValue();

        for (int i=0; i<3; ++i)
        {
            double val = 0.0;
            if (spectrumRenderer.getSpectrum().getChannelsToColorBy()[i] < instrument.getBandCenters().length)
            {
                val = spectrumRenderer.getSpectrum().getSpectrum()[channelsToColorBy[i]];
            }
            else if (channelsToColorBy[i] < instrument.getBandCenters().length + instrument.getSpectrumMath().getDerivedParameters().length)
                val = spectrumRenderer.getSpectrum().evaluateDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length);
            else
                val = instrument.getSpectrumMath().evaluateUserDefinedDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length-instrument.getSpectrumMath().getDerivedParameters().length, spectrumRenderer.getSpectrum().getSpectrum());

            if (val < 0.0)
                val = 0.0;
            else if (val > 1.0)
                val = 1.0;

            double slope = 1.0 / (channelsColoringMaxValue[i] - channelsColoringMinValue[i]);
            color[i] = slope * (val - channelsColoringMinValue[i]);
        }
        return color;
	}


	public Double getRedMinVal()
    {
        return redMinVal;
    }

    public void setRedMinVal(Double redMinVal)
    {
        this.redMinVal = redMinVal;
    }

    public Double getRedMaxVal()
    {
        return redMaxVal;
    }

    public void setRedMaxVal(Double redMaxVal)
    {
        this.redMaxVal = redMaxVal;
    }

    public Double getGreenMinVal()
    {
        return greenMinVal;
    }

    public void setGreenMinVal(Double greenMinVal)
    {
        this.greenMinVal = greenMinVal;
    }

    public Double getGreenMaxVal()
    {
        return greenMaxVal;
    }

    public void setGreenMaxVal(Double greenMaxVal)
    {
        this.greenMaxVal = greenMaxVal;
    }

    public Double getBlueMinVal()
    {
        return blueMinVal;
    }

    public void setBlueMinVal(Double blueMinVal)
    {
        this.blueMinVal = blueMinVal;
    }

    public Double getBlueMaxVal()
    {
        return blueMaxVal;
    }

    public void setBlueMaxVal(Double blueMaxVal)
    {
        this.blueMaxVal = blueMaxVal;
    }

    public boolean isGreyScaleSelected()
    {
        return greyScaleSelected;
    }

    public void setGreyScaleSelected(boolean greyScaleSelected)
    {
        this.greyScaleSelected = greyScaleSelected;
    }

    public int getRedIndex()
    {
        return redIndex;
    }

    public void setRedIndex(int redIndex)
    {
        this.redIndex = redIndex;
    }

    public int getGreenIndex()
    {
        return greenIndex;
    }

    public void setGreenIndex(int greenIndex)
    {
        this.greenIndex = greenIndex;
    }

    public int getBlueIndex()
    {
        return blueIndex;
    }

    public void setBlueIndex(int blueIndex)
    {
        this.blueIndex = blueIndex;
    }

    public SpectrumColoringStyle getSpectrumColoringStyle()
    {
        return spectrumColoringStyle;
    }

    public void setSpectrumColoringStyle(SpectrumColoringStyle spectrumColoringStyle)
    {
        this.spectrumColoringStyle = spectrumColoringStyle;
        fireColoringChanged();
    }

    public void fireColoringChanged()
    {
        for (SpectrumColoringChangedListener listener : colorChangedListeners)
        {
            listener.coloringChanged();
        }
    }

    public void addColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.add(listener);
    }

    public void removeColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.remove(listener);
    }

    public void removeAllColoringChangedListeners()
    {
        colorChangedListeners.removeAllElements();
    }

	public Vector<SpectrumColoringChangedListener> getColorChangedListeners()
	{
		return colorChangedListeners;
	}

	public int[] getChannels()
	{
		return channels;
	}

	public double[] getMins()
	{
		return mins;
	}

	public double[] getMaxs()
	{
		return maxs;
	}

	public void setCurrentColormap(Colormap currentColormap)
	{
		this.currentColormap = currentColormap;
		fireColoringChanged();
	}

    /**
     * Returns state describing whether the user defined color function is being edited
     * @return
     */
    public boolean isCurrentlyEditingUserDefinedFunction()
    {
        return currentlyEditingUserDefinedFunction;
    }

    /**
     * Updates the state describing whether the user defined color function is being edited
     * @param currentlyEditingUserDefinedFunction
     */
    public void setCurrentlyEditingUserDefinedFunction(
            boolean currentlyEditingUserDefinedFunction)
    {
        this.currentlyEditingUserDefinedFunction = currentlyEditingUserDefinedFunction;
    }
}