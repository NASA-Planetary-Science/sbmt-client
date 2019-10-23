package edu.jhuapl.sbmt.spectrum.model.core.color;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumColorer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumColoringChangedListener;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class GreyscaleSpectrumColorer<S extends BasicSpectrum> implements ISpectrumColorer<S>
{
	private int greyScaleIndex;
	private Double greyMinVal = 0.0;
    private Double greyMaxVal;
    private List<SpectrumColoringChangedListener> colorChangedListeners;
    private int[] channels;
    private double[] mins;
    private double[] maxs;

	public GreyscaleSpectrumColorer()
	{
		colorChangedListeners = new ArrayList<SpectrumColoringChangedListener>();
	}

	@Override
	public double[] getColorForSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getGreyScaleIndex()
	{
		return greyScaleIndex;
	}

	public void setGreyScaleIndex(int greyScaleIndex)
	{
		this.greyScaleIndex = greyScaleIndex;
	}

	public Double getGreyMinVal()
	{
		return greyMinVal;
	}

	public void setGreyMinVal(Double greyMinVal)
	{
		this.greyMinVal = greyMinVal;
	}

	public Double getGreyMaxVal()
	{
		return greyMaxVal;
	}

	public void setGreyMaxVal(Double greyMaxVal)
	{
		this.greyMaxVal = greyMaxVal;
	}

	public int[] getChannels()
	{
		return channels;
	}

	public void setChannels(int[] channels)
	{
		this.greyScaleIndex = channels[0];
		this.channels = channels;
	}

	public double[] getMins()
	{
		return mins;
	}

	public void setMins(double[] mins)
	{
		this.mins = mins;
	}

	public double[] getMaxs()
	{
		return maxs;
	}

	public void setMaxs(double[] maxs)
	{
		this.greyMaxVal = maxs[0];
		this.maxs = maxs;
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
		this.channels = new int[]{greyScaleIndex, greyScaleIndex, greyScaleIndex};
    	this.mins = new double[]{greyMinVal, greyMinVal, greyMinVal};
    	this.maxs = new double[]{greyMaxVal, greyMaxVal, greyMaxVal};
		fireColoringChanged();
	}

}
