package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedLidarTrack implements PlannedInstrumentData
{
	private Color color = Color.red;
	private Instrument instrument;
	private Double startTime;
	private Double stopTime;
	private boolean isShowing = false;
	private boolean isFrustumShowing = false;

	public PlannedLidarTrack()
	{
		// TODO Auto-generated constructor stub
	}

	public PlannedLidarTrack(Double startTime, Double stopTime, String instrumentName)
	{
		this.startTime = startTime;
		this.stopTime = stopTime;
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
		return startTime;
	}

	public void setStartTime(Double time)
	{
		this.startTime = time;
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

	/**
	 * @return the startTime
	 */
	public Double getStartTime()
	{
		return startTime;
	}

	/**
	 * @return the stopTime
	 */
	public Double getStopTime()
	{
		return stopTime;
	}
}