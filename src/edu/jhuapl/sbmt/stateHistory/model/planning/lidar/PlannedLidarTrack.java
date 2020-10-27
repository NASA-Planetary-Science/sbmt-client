package edu.jhuapl.sbmt.stateHistory.model.planning.lidar;

import java.awt.Color;

import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PlannedLidarTrack implements PlannedInstrumentData
{
	@Getter @Setter
	private Color color = Color.red;

	@Getter @Setter
	private Instrument instrument;

	@Getter @Setter
	private Double startTime;

	@Getter @Setter
	private Double stopTime;

	@Getter @Setter
	private boolean isShowing = false;

	@Getter @Setter
	private boolean isFrustumShowing = false;

	public PlannedLidarTrack(Double startTime, Double stopTime, String instrumentName)
	{
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.instrument = Instrument.valueFor(instrumentName);
	}

	public Double getTime()
	{
		return startTime;
	}

	public String getInstrumentName()
	{
		return instrument.name();
	}

}