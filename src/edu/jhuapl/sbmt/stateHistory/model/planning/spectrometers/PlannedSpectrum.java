package edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PlannedSpectrum implements PlannedInstrumentData
{
	@Getter @Setter
	private Color color = Color.green;

	@Getter @Setter
	private Instrument instrument;

	@Getter @Setter
	private Double startTime, stopTime;

	@Getter
	private Integer cadence;

	@Getter @Setter
	private boolean isShowing;

	@Getter @Setter
	private boolean isFrustumShowing;

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
}
