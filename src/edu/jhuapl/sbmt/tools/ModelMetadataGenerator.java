package edu.jhuapl.sbmt.tools;

import edu.jhuapl.sbmt.config.SmallBodyViewConfigMetadataIO;
import edu.jhuapl.sbmt.core.client.Mission;

/**
 * Wrapper for running {@link SmallBodyViewConfigMetadataIO} from a command line tool.
 *
 * @author James Peachey
 *
 */
public class ModelMetadataGenerator
{
    private static final String MissionPropertyName = "edu.jhuapl.sbmt.mission";

    public static void main(String[] args)
    {
        try
        {
            if (System.getProperty(MissionPropertyName) == null)
            {
                System.setProperty(MissionPropertyName, Mission.TEST_APL_INTERNAL.getHashedName());
            }

            SmallBodyViewConfigMetadataIO.main(args);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
