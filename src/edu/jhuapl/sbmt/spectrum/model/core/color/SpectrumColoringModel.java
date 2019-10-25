package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.util.HashMap;
import java.util.Vector;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumColorer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
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
    private SpectrumColoringStyle spectrumColoringStyle = SpectrumColoringStyle.RGB;
    private ISpectrumColorer<S> currentColorer;
    private HashMap<SpectrumColoringStyle, ISpectrumColorer<S>> colorers;
    private double[] rgbMaxvals;
    private int[] rgbIndices;
    private GreyscaleSpectrumColorer<S> greyScaleColorer;
    private RGBSpectrumColorer<S> rgbColorer;
    private EmissionSpectrumColorer<S> emissionColorer;
    private SpectrumColoringChangedListener colorerListener;

	public SpectrumColoringModel()
	{
        this.colorChangedListeners = new Vector<SpectrumColoringChangedListener>();
        this.colorers = new HashMap<SpectrumColoringStyle, ISpectrumColorer<S>>();
        colorerListener = new SpectrumColoringChangedListener()
		{

			@Override
			public void coloringChanged()
			{
				fireColoringChanged();
			}
		};

		greyScaleColorer = new GreyscaleSpectrumColorer<S>();
		greyScaleColorer.addSpectrumColoringChangedListener(colorerListener);

		rgbColorer = new RGBSpectrumColorer<S>();
		rgbColorer.addSpectrumColoringChangedListener(colorerListener);

		emissionColorer = new EmissionSpectrumColorer<S>();
		emissionColorer.addSpectrumColoringChangedListener(colorerListener);

		colorers.put(SpectrumColoringStyle.GREYSCALE, greyScaleColorer);
		colorers.put(SpectrumColoringStyle.RGB, rgbColorer);
		colorers.put(SpectrumColoringStyle.EMISSION_ANGLE, emissionColorer);

		this.currentColorer = rgbColorer;

	}

	public void updateColoring()
	{
//        if (isCurrentlyEditingUserDefinedFunction())
//            return;
		// If we are currently editing user defined functions
        // (i.e. the dialog is open), do not update the coloring
        // since we may be in an inconsistent state.

//        if (isGreyScaleSelected())
//        {
//        	this.channels = new int[]{redIndex, redIndex, redIndex};
//        	this.mins = new double[]{redMinVal, redMinVal, redMinVal};
//        	this.maxs = new double[]{redMaxVal, redMaxVal, redMaxVal};
//        }
//        else
//        {
//        	this.channels = new int[]{redIndex, greenIndex, blueIndex};
//        	this.mins = new double[]{redMinVal, greenMinVal, blueMinVal};
//        	this.maxs = new double[]{redMaxVal, greenMaxVal, blueMaxVal};
//        }
        fireColoringChanged();
	}

	public double[] getSpectrumColoringForCurrentStyle(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		return currentColorer.getColorForSpectrum(spectrumRenderer);
	}

    public SpectrumColoringStyle getSpectrumColoringStyle()
    {
        return spectrumColoringStyle;
    }

    public void setSpectrumColoringStyle(SpectrumColoringStyle spectrumColoringStyle)
    {
        this.spectrumColoringStyle = spectrumColoringStyle;
        currentColorer = colorers.get(spectrumColoringStyle);
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

	public ISpectrumColorer<S> getCurrentColorer()
	{
		return currentColorer;
	}

	public void setCurrentColorer(ISpectrumColorer<S> currentColorer)
	{
		this.currentColorer = currentColorer;
		fireColoringChanged();
	}

	public void setRgbMaxvals(double[] rgbMaxvals)
	{
		this.rgbMaxvals = rgbMaxvals;
		rgbColorer.setRedMaxVal(rgbMaxvals[0]);
		rgbColorer.setGreenMaxVal(rgbMaxvals[1]);
		rgbColorer.setBlueMaxVal(rgbMaxvals[2]);
		rgbColorer.updateColoring();
		greyScaleColorer.setMaxs(rgbMaxvals);
		greyScaleColorer.updateColoring();
		updateColoring();
	}

	public void setRgbIndices(int[] rgbIndices)
	{
		this.rgbIndices = rgbIndices;
		rgbColorer.setRedIndex(rgbIndices[0]);
		rgbColorer.setGreenIndex(rgbIndices[1]);
		rgbColorer.setBlueIndex(rgbIndices[2]);
		rgbColorer.updateColoring();
		greyScaleColorer.setChannels(rgbIndices);
		greyScaleColorer.updateColoring();
		updateColoring();
	}

	public GreyscaleSpectrumColorer<S> getGreyScaleColorer()
	{
		return greyScaleColorer;
	}

	public RGBSpectrumColorer<S> getRgbColorer()
	{
		return rgbColorer;
	}

	public EmissionSpectrumColorer<S> getEmissionColorer()
	{
		return emissionColorer;
	}
}