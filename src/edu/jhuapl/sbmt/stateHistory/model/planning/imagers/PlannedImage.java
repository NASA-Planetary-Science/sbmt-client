package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedImage implements PlannedInstrumentData
{
	private Color color = Color.red;
	private Instrument instrument;
	private Double time;
	private boolean isShowing = false;
	private boolean isFrustumShowing = false;

	public PlannedImage()
	{
		// TODO Auto-generated constructor stub
	}

	public PlannedImage(Double time, String instrumentName)
	{
		this.time = time;
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
