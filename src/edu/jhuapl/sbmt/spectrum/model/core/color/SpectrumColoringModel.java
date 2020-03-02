package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.util.HashMap;
import java.util.Vector;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumColorer;
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

	/**
	 * Returnst he coloring for the provided renderer
	 * @param spectrumRenderer
	 * @return
	 */
	public double[] getSpectrumColoringForCurrentStyle(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		return currentColorer.getColorForSpectrum(spectrumRenderer);
	}

    /**
     * Returns the active coloring style
     * @return
     */
    public SpectrumColoringStyle getSpectrumColoringStyle()
    {
        return spectrumColoringStyle;
    }

    /**
     * Sets the spectrum coloring style, and fires the coloring changed listeners
     * @param spectrumColoringStyle
     */
    public void setSpectrumColoringStyle(SpectrumColoringStyle spectrumColoringStyle)
    {
        this.spectrumColoringStyle = spectrumColoringStyle;
        currentColorer = colorers.get(spectrumColoringStyle);
        fireColoringChanged();
    }

    /**
     * Fires the coloring changed listeners
     */
    private void fireColoringChanged()
    {
        for (SpectrumColoringChangedListener listener : colorChangedListeners)
        {
            listener.coloringChanged();
        }
    }

    /**
     * Adds a coloring changed listener to the list
     * @param listener
     */
    public void addColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.add(listener);
    }

    /**
     * Removed a coloring changed listener from the list
     * @param listener
     */
    public void removeColoringChangedListener(SpectrumColoringChangedListener listener)
    {
        colorChangedListeners.remove(listener);
    }

    /**
     * Removes all coloring changed listeners
     */
    public void removeAllColoringChangedListeners()
    {
        colorChangedListeners.removeAllElements();
    }

	/**
	 * Sets the RGB max values for colorers that need them
	 * @param rgbMaxvals
	 */
	public void setRgbMaxvals(double[] rgbMaxvals)
	{
		this.rgbMaxvals = rgbMaxvals;
		rgbColorer.setRedMaxVal(rgbMaxvals[0]);
		rgbColorer.setGreenMaxVal(rgbMaxvals[1]);
		rgbColorer.setBlueMaxVal(rgbMaxvals[2]);
		rgbColorer.updateColoring();
		greyScaleColorer.setMaxs(rgbMaxvals);
		greyScaleColorer.updateColoring();
		fireColoringChanged();
	}

	/**
	 * Sets the RGB indices for colorerers that need them
	 * @param rgbIndices
	 */
	public void setRgbIndices(int[] rgbIndices)
	{
		this.rgbIndices = rgbIndices;
		rgbColorer.setRedIndex(rgbIndices[0]);
		rgbColorer.setGreenIndex(rgbIndices[1]);
		rgbColorer.setBlueIndex(rgbIndices[2]);
		rgbColorer.updateColoring();
		greyScaleColorer.setChannels(rgbIndices);
		greyScaleColorer.updateColoring();
		fireColoringChanged();
	}

	/**
	 * Returns the greyscale colorer
	 * @return
	 */
	public GreyscaleSpectrumColorer<S> getGreyScaleColorer()
	{
		return greyScaleColorer;
	}

	/**
	 * Returns the RGB colorerer
	 * @return
	 */
	public RGBSpectrumColorer<S> getRgbColorer()
	{
		return rgbColorer;
	}

	/**
	 * Returnst he emission colorer
	 * @return
	 */
	public EmissionSpectrumColorer<S> getEmissionColorer()
	{
		return emissionColorer;
	}
}