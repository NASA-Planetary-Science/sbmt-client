package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.Map.Entry;
import java.util.Set;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;

import crucible.core.math.vectorspace.UnwritableVectorIJK;

public interface IStateHistoryLocationProvider
{
    //Heavenly body position getters

    /**
     * Returns the spacecraft position in the body fixed frame
     * @return
     */
    public double[] getSpacecraftPosition();

    /**
     * @param time
     * @return
     */
    public double[] getSpacecraftPositionAtTime(double time);

    /**
     * @param instrumentName
     * @return
     */
    public double[] getInstrumentLookDirection(String instrumentName);

    /**
     * @param instrumentName
     * @param time
     * @return
     */
    public double[] getInstrumentLookDirectionAtTime(String instrumentName, double time);

    /**
     * @param instrumentName
     * @param index
     * @return
     */
    public UnwritableVectorIJK getFrustum(String instrumentName, int index);

    /**
     * @param instrumentName
     * @param index
     * @param time
     * @return
     */
    public UnwritableVectorIJK getFrustumAtTime(String instrumentName, int index, double time);

    /**
     * Returns the sun position in the body fixed frame
     * @return
     */
    public double[] getSunPosition();

    /**
     * Returns the earth position in the body fixed frame
     * @return
     */
    public double[] getEarthPosition();

    /**
     * @param flybyState
     */
    public void addState(State flybyState);

    /**
     * @param time
     * @param flybyState
     */
    public void addStateAtTime(Double time, State flybyState);

    /**
     * @param time
     * @return
     */
    public Entry<Double, State> getStateBeforeOrAtTime(Double time);

    /**
     * @param time
     * @return
     */
    public Entry<Double, State> getStateAtOrAfter(Double time);

    /**
     * @param time
     * @return
     */
    public State getStateAtTime(Double time);

    /**
     * @return
     */
    public State getCurrentState();

    /**
     * @return
     */
    public Set<Double> getAllTimes();

    /**
	 * @return
	 */
	public IPointingProvider getPointingProvider();

	/**
	 * @param pointingProvider
	 */
	public void setPointingProvider(IPointingProvider pointingProvider);

	public void reloadPointingProvider() throws StateHistoryIOException;

	/**
	 * @param sourceFile
	 */
	public void setSourceFile(String sourceFile);

	/**
	 * @return
	 */
	public String getSourceFile();

	public boolean validate();
}
