package edu.jhuapl.sbmt.stateHistory.model;

import edu.jhuapl.sbmt.model.image.Instrument;

import spice.basic.Matrix33;
import spice.basic.Vector3;

public interface IPositionOrientation
{
	public Vector3 getSpacecraftPosition(double time);

	public Matrix33 getSpacecraftOrientation(double time);

	public Vector3 getUpDirection(double time);

	public Vector3 getLookDirectionAtTimeForInstrument(double time, Instrument instrument);

	public double getInstrumentFOVWidth(Instrument instrument);

	public double getInstrumentFOVHeight(Instrument instrument);
}
