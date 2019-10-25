package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumColorer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class RGBSpectrumColorer<S extends BasicSpectrum> implements ISpectrumColorer<S>
{
    private Double redMinVal = 0.0;
    private Double redMaxVal = 0.0;
    private Double greenMinVal = 0.0;
    private Double greenMaxVal = 0.0;
    private Double blueMinVal = 0.0;
    private Double blueMaxVal = 0.0;
    private int redIndex;
    private int greenIndex;
    private int blueIndex;
    private int[] channels;
    private double[] mins;
    private double[] maxs;
    protected boolean currentlyEditingUserDefinedFunction = false;
    private List<SpectrumColoringChangedListener> colorChangedListeners;

	public RGBSpectrumColorer()
	{
		colorChangedListeners = new ArrayList<SpectrumColoringChangedListener>();
	}

	@Override
	public double[] getColorForSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		//TODO: What do we do for L3 data here?  It has less XAxis points than the L2 data, so is the coloring scheme different?
        double[] color = new double[3];
        BasicSpectrumInstrument instrument = spectrumRenderer.getSpectrum().getInstrument();
        int[] channelsToColorBy = channels;
        double[] channelsColoringMinValue = mins;
        double[] channelsColoringMaxValue = maxs;
        for (int i=0; i<3; ++i)
        {
            double val = 0.0;
            if (channelsToColorBy[i] < instrument.getBandCenters().length)
            {
                val = spectrumRenderer.getSpectrum().getSpectrum()[channelsToColorBy[i]];
            }
            else if (channelsToColorBy[i] < instrument.getBandCenters().length + instrument.getSpectrumMath().getDerivedParameters().length)
            {
                val = spectrumRenderer.getSpectrum().evaluateDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length);
            }
            else
            {
                val = instrument.getSpectrumMath().evaluateUserDefinedDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length-instrument.getSpectrumMath().getDerivedParameters().length, spectrumRenderer.getSpectrum().getSpectrum());
            }
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

    public void addSpectrumColoringChangedListener(SpectrumColoringChangedListener sccl)
	{
		colorChangedListeners.add(sccl);
	}

	private void fireColoringChanged()
	{
		for (SpectrumColoringChangedListener sccl : colorChangedListeners) sccl.coloringChanged();
	}

	public void updateColoring()
	{
		this.channels = new int[]{redIndex, greenIndex, blueIndex};
    	this.mins = new double[]{redMinVal, greenMinVal, blueMinVal};
    	this.maxs = new double[]{redMaxVal, greenMaxVal, blueMaxVal};
		fireColoringChanged();
	}
}
