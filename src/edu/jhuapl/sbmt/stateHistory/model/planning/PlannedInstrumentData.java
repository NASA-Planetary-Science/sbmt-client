package edu.jhuapl.sbmt.stateHistory.model.planning;

import edu.jhuapl.sbmt.core.config.Instrument;

public interface PlannedInstrumentData
{
	/**
	 * @return
	 */
	public boolean isShowing();

	/**
	 * @param isShowing
	 */
	public void setShowing(boolean isShowing);

	/**
	 * @return
	 */
	public boolean isFrustumShowing();

	/**
	 * @param isFrustumShowing
	 */
	public void setFrustumShowing(boolean isFrustumShowing);

	/**
	 * @return
	 */
	public Double getTime();

	/**
	 * @return
	 */
	public String getInstrumentName();

	/**
	 * @return
	 */
	public Instrument getInstrument();

}
