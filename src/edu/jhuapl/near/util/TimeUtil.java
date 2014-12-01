package edu.jhuapl.near.util;

import java.io.File;

import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

/**
 * Contains static utility functions for converting from UTC and ephemeris time
 * and vice versa. Uses SPICE's Java library for doing this.
 */
public class TimeUtil
{
    static
    {
        System.loadLibrary("JNISpice");
        try
        {
            File lskFile = ConvertResourceToFile.convertResourceToRealFile(
                    TimeUtil.class,
                    "/edu/jhuapl/near/data/naif0010.tls",
                    Configuration.getApplicationDataDir());

            CSPICE.furnsh(lskFile.getAbsolutePath());
        }
        catch (SpiceErrorException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Convert UTC string to ephemeris time. If the string is not a valid UTC
     * string then -Double.MIN_VALUE is returned.
     *
     * @param str UTC string, e.g. 2000-04-06T01:44:28.165
     * @return ephemeris time as a double
     */
    public static double str2et(String str)
    {
        try
        {
            return CSPICE.str2et(str);
        }
        catch (SpiceErrorException e)
        {
            e.printStackTrace();
        }

        return -Double.MIN_VALUE;
    }

    /**
     * Convert ephemeris time to UTC string. If an error occurs during the
     * conversion, the empty string is returned.
     *
     * @param et ephemeris time
     * @return UTC string
     */
    public static String et2str(double et)
    {
        try
        {
            return CSPICE.et2utc(et, "ISOC", 6);
        }
        catch (SpiceErrorException e)
        {
            e.printStackTrace();
        }

        return "";
    }
}
