package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedSpectrum implements PlannedInstrumentData
{
	private Color color = Color.green;
	private Instrument instrument;
	private Double time;
	private boolean isShowing;
	private boolean isFrustumShowing;
	private String instrumentName;

	public PlannedSpectrum()
	{
		// TODO Auto-generated constructor stub
	}

	public PlannedSpectrum(Double et, String instrumentName)
	{
		this.time = et;
		this.instrument = Instrument.valueFor(instrumentName);
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public Instrument getInstrument()
	{
		return instrument;
	}

	public void setInstrument(Instrument instrument)
	{
		this.instrument = instrument;
	}

	public Double getTime()
	{
		return time;
	}

	public void setTime(Double time)
	{
		this.time = time;
	}

	public boolean isShowing()
	{
		return isShowing;
	}

	public void setShowing(boolean isShowing)
	{
		this.isShowing = isShowing;
	}

	public boolean isFrustumShowing()
	{
		return isFrustumShowing;
	}

	public void setFrustumShowing(boolean isFrustumShowing)
	{
		this.isFrustumShowing = isFrustumShowing;
	}

	public String getInstrumentName()
	{
		return instrument.name();
	}
}
