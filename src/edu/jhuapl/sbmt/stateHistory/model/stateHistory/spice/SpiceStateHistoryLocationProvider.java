package edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Set;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.State;
import edu.jhuapl.sbmt.pointing.modules.SpiceReaderPublisher;
import edu.jhuapl.sbmt.pointing.scState.SpiceState;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.SpiceKernelNotFoundException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

import crucible.core.math.vectorspace.UnwritableVectorIJK;
import crucible.mantle.spice.adapters.AdapterInstantiationException;
import crucible.mantle.spice.kernel.KernelInstantiationException;

public class SpiceStateHistoryLocationProvider implements IStateHistoryLocationProvider
{
    /**
    *
    */
	public static final double epsilon = 0.0000001;

	/**
	 *
	 */
	private String sourceFile;

	private IPointingProvider pointingProvider;

	private SpiceState state;

	private SpiceInfo spiceInfo;

	private StateHistory stateHistory;

	public SpiceStateHistoryLocationProvider(StateHistory history)
	{
		this.stateHistory = history;
	}

	@Override
	public double[] getSpacecraftPosition()
	{
		return state.getSpacecraftPosition();
	}

	@Override
	public double[] getSpacecraftPositionAtTime(double time)
	{
		state.setEphemerisTime(time);
		return state.getSpacecraftPosition();
	}

	@Override
	public double[] getInstrumentLookDirection(String instrumentName)
	{
		return state.getInstrumentLookDirection(instrumentName);
	}

	@Override
	public double[] getInstrumentLookDirectionAtTime(String instrumentName, double time)
	{
		state.setEphemerisTime(time);
		return state.getInstrumentLookDirection(instrumentName);
	}

	@Override
	public UnwritableVectorIJK getFrustum(String instrumentName, int index)
	{
		return state.getFrustum(instrumentName, index);
	}

	@Override
	public UnwritableVectorIJK getFrustumAtTime(String instrumentName, int index, double time)
	{
		state.setEphemerisTime(time);
		return state.getFrustum(instrumentName, index);
	}

	@Override
	public double[] getSunPosition()
	{
		return state.getSunPosition();
	}

	@Override
	public double[] getEarthPosition()
	{
		return state.getEarthPosition();
	}

	@Override
	public void addState(State flybyState)
	{
		this.state = (SpiceState)flybyState;
	}

	@Override
	public void addStateAtTime(Double time, State flybyState)
	{
		this.state = (SpiceState)flybyState;
	}

	@Override
	public Entry<Double, State> getStateBeforeOrAtTime(Double time)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<Double, State> getStateAtOrAfter(Double time)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State getStateAtTime(Double time)
	{
		return state;
	}

	@Override
	public State getCurrentState()
	{
		return state;
	}

	@Override
	public Set<Double> getAllTimes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPointingProvider getPointingProvider()
	{
		if (pointingProvider == null)
			try
			{
				buildPointingProvider();
			}
			catch (StateHistoryIOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return pointingProvider;
	}

	@Override
	public void setPointingProvider(IPointingProvider pointingProvider)
	{
		this.pointingProvider = pointingProvider;
	}

	@Override
	public void reloadPointingProvider() throws StateHistoryIOException
	{
		buildPointingProvider();
	}

	/**
	 * @return the spiceInfo
	 */
	public SpiceInfo getSpiceInfo()
	{
		return spiceInfo;
	}

	/**
	 * @param spiceInfo the spiceInfo to set
	 */
	public void setSpiceInfo(SpiceInfo spiceInfo)
	{
		this.spiceInfo = spiceInfo;

	}

	@Override
	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
	}

	@Override
	public String getSourceFile()
	{
		return sourceFile;
	}

	public boolean validate()
	{
		if (new File(sourceFile).exists() == false) return false;
		else if (spiceInfo == null) return false;
		else return true;
	}

	private void updateTrajectoryAndStateWithPointing(IPointingProvider pointingProvider)
	{
		Trajectory trajectory = new StandardTrajectory(stateHistory);
		trajectory.setPointingProvider(pointingProvider);
		trajectory.setStartTime(stateHistory.getMetadata().getStartTime());
		trajectory.setStopTime(stateHistory.getMetadata().getEndTime());

		trajectory.setNumPoints(Math.abs((int)(stateHistory.getMetadata().getEndTime() - stateHistory.getMetadata().getStartTime())/60));

		State state = new SpiceState((SpicePointingProvider)pointingProvider);
		((SpiceState)state).setEphemerisTime(stateHistory.getMetadata().getStartTime());
		// add to history
		addState(state);
		stateHistory.getTrajectoryMetadata().setTrajectory(trajectory);
		stateHistory.getMetadata().setType(StateHistorySourceType.SPICE);
	}

	private void buildPointingProvider() throws StateHistoryIOException
	{
		Path mkPath = Paths.get(sourceFile);
		if (!mkPath.toFile().exists())
		{
			throw new StateHistoryIOException(new SpiceKernelNotFoundException("Cannot find metakernel at specified location."));
		}
		try
		{
    		SpiceReaderPublisher pointingPublisher = new SpiceReaderPublisher(mkPath.toString(), spiceInfo);
    		pointingProvider = pointingPublisher.getOutputs().get(0);

            updateTrajectoryAndStateWithPointing(pointingProvider);
		}
		catch (FileNotFoundException fnfe)
		{
			throw new StateHistoryIOException("Cannot find metakernel at specified location.", new SpiceKernelNotFoundException("Cannot find metakernel at specified location.", fnfe));
		}
		catch (AdapterInstantiationException aie)
		{
			aie.printStackTrace();
		}
		catch (KernelInstantiationException kie)
		{
			throw new StateHistoryIOException("Kernels are invalid; cannot instantiate Kernel environment", kie);
		}
		catch (IOException ioe)
		{
			throw new StateHistoryIOException("Error reading in SPICE related files; please check kernels", ioe);
		}
	}
}