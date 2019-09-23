package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.util.Vector;

import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;

/**
 * Model to capture the current coloring state for the spectra on screen.  This manages both the RGB coloring mode values as well as whether
 * the display is in that mode or 1 or more other modes defined by the enum <pre>spectrumColoringStyle</pre>.  Listeners are included to
 * broadcast changes out to interested parties via a <pre>SpectrumColoringChangedListener</pre>
 *
 * @author steelrj1
 *
 */
public class SpectrumColoringModel
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

	public SpectrumColoringModel()
	{
        this.colorChangedListeners = new Vector<SpectrumColoringChangedListener>();
	}

	public void updateColoring()
	{
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
}