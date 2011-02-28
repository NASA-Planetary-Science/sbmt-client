package edu.jhuapl.near;

import edu.jhuapl.near.util.Configuration;


/**
 * This class contains the "main" function called at the start of the program
 * for the APL internal version. It sets up some APL version specific configuration
 * options and then calls the public (non-APL) version's main function.
 *
 * @author kahneg1
 *
 */
public class SmallBodyMappingToolAPL
{
    public static void main(String[] args)
    {
        Configuration.setAPLVersion(true);

        // The following ensures that the APL version can always access the
        // password-protected files on the server even if run outside the lab.
        String username = "asteroid";
        String password = "crater";
        Configuration.setupPasswordAuthentication(username, password);

        // Call the public version's main function
        SmallBodyMappingTool.main(args);
    }
}
