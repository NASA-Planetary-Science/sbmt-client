/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.model.scState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import edu.jhuapl.sbmt.pointing.spice.SpiceInstrumentPointing;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.util.TimeUtil;

import crucible.core.math.vectorspace.UnwritableVectorIJK;
import crucible.core.mechanics.EphemerisID;
import crucible.core.mechanics.FrameID;
import crucible.core.mechanics.utilities.SimpleEphemerisID;

/**
 * @author steelrj1
 *
 */
public class SpiceState implements State
{
	private SpiceInstrumentPointing pointing;

    /**
     * State time in UTC
     */
    private String utc;

    /**
     * State time in ephemeris time
     */
    private double ephemerisTime;

    private EphemerisID bodyId;

    private SpicePointingProvider pointingProvider;

	/**
	 *
	 */
	public SpiceState(SpicePointingProvider pointingProvider, FrameID defaultInstFrame, EphemerisID bodyId, double et)
	{
		this.pointingProvider = pointingProvider;
		this.pointing = pointingProvider.provide(defaultInstFrame, bodyId, et);
		this.ephemerisTime = et;
		this.bodyId = bodyId;
		this.utc = TimeUtil.et2str(et);
	}

	@Override
	public double getEphemerisTime()
	{
		return ephemerisTime;
	}

	@Override
	public String getUtc()
	{
		return utc;
	}

	public double[] getInstrumentLookDirection(FrameID instrumentFrameID)
	{
		SpiceInstrumentPointing pointing = pointingProvider.provide(instrumentFrameID, bodyId, ephemerisTime);
		UnwritableVectorIJK boresight = pointing.getBoresight().createNegated();
//		System.out.println("SpiceState: getInstrumentLookDirection: returning boresight " + boresight + " for " + instrumentFrameID + " wrt " + bodyId);
		return new double[] {
				boresight.getI(), boresight.getJ(), boresight.getK()
		};
	}

	public UnwritableVectorIJK getFrustum(FrameID instrumentFrameID, int index)
	{
		SpiceInstrumentPointing pointing = pointingProvider.provide(instrumentFrameID, bodyId, ephemerisTime);
		return pointing.getFrustum().get(index);
	}

	@Override
	public double[] getSpacecraftPosition()
	{
		return new double[] { pointing.getSpacecraftPos().getI(),
							  pointing.getSpacecraftPos().getJ(),
							  pointing.getSpacecraftPos().getK()
		};
	}

	@Override
	public double[] getSpacecraftVelocity()
	{
//		return null;
		return new double[] { 0.0, 0.0, 0.0 };
//		return new double[] { pointing.getSpacecraftPos().getI(),
//							  pointing.getSpacecraftPos().getJ(),
//							  pointing.getSpacecraftPos().getK()
//		};
	}

	@Override
	public double[] getEarthPosition()
	{
//		return new double[] { 0.0, 0.0, 0.0 };
		EphemerisID earth = new SimpleEphemerisID("EARTH");
		return new double[] { pointing.getPos(earth).getI(),
	  			  pointing.getPos(earth).getJ(),
	  			  pointing.getPos(earth).getK()

		};
	}

	@Override
	public double[] getSunPosition()
	{
		EphemerisID sun = new SimpleEphemerisID("SUN");
		return new double[] { pointing.getPos(sun).getI(),
	  			  pointing.getPos(sun).getJ(),
	  			  pointing.getPos(sun).getK()

		};
	}

	@Override
	public double[] getSpacecraftXAxis()
	{
		return new double[] { 1.0, 0.0, 0.0 };
		// TODO Auto-generated method stub
//		return null;
	}

	@Override
	public double[] getSpacecraftYAxis()
	{
		return new double[] { 0.0, 1.0, 0.0 };
		// TODO Auto-generated method stub
//		return null;
	}

	@Override
	public double[] getSpacecraftZAxis()
	{
		return new double[] { 0.0, 0.0, 1.0 };
		// TODO Auto-generated method stub
//		return null;
	}

	/**
     * Writes the state object out to a comma separated value line in the file (appending to it)
     * @param path
     */
    public void writeToCSV(String path)
    {
        try
        {
            File file = new File(path);
            FileWriter in = new FileWriter(file, true);

            in.append(utc);
            in.append(',');
            in.append(Double.toString(getSunPosition()[0]));
            in.append(',');
            in.append(Double.toString(getSunPosition()[1]));
            in.append(',');
            in.append(Double.toString(getSunPosition()[2]));
            in.append(',');
            in.append(Double.toString(getEarthPosition()[0]));
            in.append(',');
            in.append(Double.toString(getEarthPosition()[1]));
            in.append(',');
            in.append(Double.toString(getEarthPosition()[2]));
            in.append(',');
            in.append(Double.toString(getSpacecraftPosition()[0]));
            in.append(',');
            in.append(Double.toString(getSpacecraftPosition()[1]));
            in.append(',');
            in.append(Double.toString(getSpacecraftPosition()[2]));
            in.append(',');
            in.append(Double.toString(getSpacecraftVelocity()[0]));
            in.append(',');
            in.append(Double.toString(getSpacecraftVelocity()[1]));
            in.append(',');
            in.append(Double.toString(getSpacecraftVelocity()[2]));
            in.append('\n');

            in.flush();
            in.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

	@Override
	public String toString()
	{
		return "SPICE State:  SC Position: " + getSpacecraftPosition()[0] + "," + getSpacecraftPosition()[1] + "," + getSpacecraftPosition()[2];
	}

}
