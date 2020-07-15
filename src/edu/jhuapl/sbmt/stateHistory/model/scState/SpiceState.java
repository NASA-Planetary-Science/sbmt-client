/**
 *
 */
package edu.jhuapl.sbmt.stateHistory.model.scState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import edu.jhuapl.sbmt.pointing.Pointing;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.util.TimeUtil;

/**
 * @author steelrj1
 *
 */
public class SpiceState implements State
{
	private Pointing pointing;

    /**
     * State time in UTC
     */
    private String utc;

    /**
     * State time in ephemeris time
     */
    private double ephemerisTime;

	/**
	 *
	 */
	public SpiceState(Pointing pointing, double et)
	{
		this.pointing = pointing;
		this.ephemerisTime = et;
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
		return new double[] { 0.0, 0.0, 0.0 };
//		return new double[] { pointing.getSunPos().getI(),
//	  			  pointing.getSunPos().getJ(),
//	  			  pointing.getSunPos().getK()
//
//		};
	}

	@Override
	public double[] getSunPosition()
	{
		return new double[] { pointing.getSunPos().getI(),
				  			  pointing.getSunPos().getJ(),
				  			  pointing.getSunPos().getK()
		};
	}

	@Override
	public double[] getSpacecraftXAxis()
	{
		return new double[] { 0.0, 0.0, 0.0 };
		// TODO Auto-generated method stub
//		return null;
	}

	@Override
	public double[] getSpacecraftYAxis()
	{
		return new double[] { 0.0, 0.0, 0.0 };
		// TODO Auto-generated method stub
//		return null;
	}

	@Override
	public double[] getSpacecraftZAxis()
	{
		return new double[] { 0.0, 0.0, 0.0 };
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
