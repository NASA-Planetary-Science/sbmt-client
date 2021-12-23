package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

public class PlannedSpectrum implements PlannedInstrumentData
{
	private Color color = Color.green;

	private Instrument instrument;

	private Double startTime, stopTime;

	private Integer cadence;

	private boolean isShowing;

	private boolean isFrustumShowing;

	private IStateHistoryMetadata stateHistoryMetadata;

	public PlannedSpectrum()
	{

	}

	public PlannedSpectrum(Double etStart, Double etEnd, Integer cadence, String instrumentName)
	{
		this.startTime = etStart;
		this.stopTime = etEnd;
		this.cadence = cadence;
		this.instrument = Instrument.valueFor(instrumentName);
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
	 * @return the startTime
	 */
	public Double getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Double startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public Double getStopTime()
	{
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(Double stopTime)
	{
		this.stopTime = stopTime;
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

	/**
	 * @return the cadence
	 */
	public Integer getCadence()
	{
		return cadence;
	}

	/**
	 * @return the stateHistoryMetadata
	 */
	public IStateHistoryMetadata getStateHistoryMetadata()
	{
		return stateHistoryMetadata;
	}

	/**
	 * @param stateHistoryMetadata the stateHistoryMetadata to set
	 */
	public void setStateHistoryMetadata(IStateHistoryMetadata stateHistoryMetadata)
	{
		this.stateHistoryMetadata = stateHistoryMetadata;
	}
}
