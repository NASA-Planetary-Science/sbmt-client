package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedSpectrum implements PlannedInstrumentData
{
	private Color color = Color.green;
	private Instrument instrument;
	private Double startTime, stopTime;
	private Integer cadence;
	private boolean isShowing;
	private boolean isFrustumShowing;

	public PlannedSpectrum()
	{
		// TODO Auto-generated constructor stub
	}

	public PlannedSpectrum(Double etStart, Double etEnd, Integer cadence, String instrumentName)
	{
		this.startTime = etStart;
		this.stopTime = etEnd;
		this.cadence = cadence;
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

	public Double getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Double time)
	{
		this.startTime = time;
	}

	public Double getStopTime()
	{
		return stopTime;
	}

	public void setStopTime(Double time)
	{
		this.stopTime = time;
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

	@Override
	public Double getTime()
	{
		return startTime;
	}

	/**
	 * @return the cadence
	 */
	public Integer getCadence()
	{
		return cadence;
	}
}
