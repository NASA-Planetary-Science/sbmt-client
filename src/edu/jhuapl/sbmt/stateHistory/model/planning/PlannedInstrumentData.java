package edu.jhuapl.sbmt.stateHistory.model.planning;

import edu.jhuapl.sbmt.model.image.Instrument;

public interface PlannedInstrumentData
{
	public boolean isShowing();

	public void setShowing(boolean isShowing);

	public boolean isFrustumShowing();

	public void setFrustumShowing(boolean isFrustumShowing);

	public Double getTime();

	public String getInstrumentName();

	public Instrument getInstrument();

}
