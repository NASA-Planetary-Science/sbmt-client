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

	}

	public PlannedImage(Double time, String instrumentName)
	{
		this.time = time;
		this.instrument = Instrument.valueFor(instrumentName);
	}

	public String getInstrumentName()
	{
		return instrument.name();
	}

	/**
	 * @return the color
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/**
	 * @return the instrument
	 */
	public Instrument getInstrument()
	{
		return instrument;
	}

	/**
	 * @param instrument the instrument to set
	 */
	public void setInstrument(Instrument instrument)
	{
		this.instrument = instrument;
	}

	/**
	 * @return the time
	 */
	public Double getTime()
	{
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Double time)
	{
		this.time = time;
	}

	/**
	 * @return the isShowing
	 */
	public boolean isShowing()
	{
		return isShowing;
	}

	/**
	 * @param isShowing the isShowing to set
	 */
	public void setShowing(boolean isShowing)
	{
		this.isShowing = isShowing;
	}

	/**
	 * @return the isFrustumShowing
	 */
	public boolean isFrustumShowing()
	{
		return isFrustumShowing;
	}

	/**
	 * @param isFrustumShowing the isFrustumShowing to set
	 */
	public void setFrustumShowing(boolean isFrustumShowing)
	{
		this.isFrustumShowing = isFrustumShowing;
	}
}
