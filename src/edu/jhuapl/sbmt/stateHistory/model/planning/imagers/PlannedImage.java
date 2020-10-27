package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PlannedImage implements PlannedInstrumentData
{
	@Getter @Setter
	private Color color = Color.red;

	@Getter @Setter
	private Instrument instrument;

	@Getter @Setter
	private Double time;

	@Getter @Setter
	private boolean isShowing = false;

	@Getter @Setter
	private boolean isFrustumShowing = false;

	public PlannedImage(Double time, String instrumentName)
	{
		this.time = time;
		this.instrument = Instrument.valueFor(instrumentName);
	}

	public String getInstrumentName()
	{
		return instrument.name();
	}
}
