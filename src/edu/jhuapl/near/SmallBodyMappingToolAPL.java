package edu.jhuapl.near;

import java.util.ArrayList;

import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileUtil;


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

        // Use default credentials so that the APL version can always access the
        // password-protected files on the server even if run outside the lab.
        String username = "asteroid";
        String password = "crater";

        try
        {
            ArrayList<String> credentials = FileUtil.getFileLinesAsStringList("password.txt");
            if (credentials.size() >= 2)
            {
                String user = credentials.get(0);
                String pass = credentials.get(1);

                if (user != null && user.trim().length() > 0 && !user.trim().toLowerCase().contains("replace-with-") &&
                    pass != null && pass.trim().length() > 0)
                {
                    username = user.trim();
                    password = pass.trim();
                }
            }
        }
        catch (Exception e)
        {
        }

        Configuration.setupPasswordAuthentication(username, password);

        // Call the public version's main function
        SmallBodyMappingTool.main(args);
    }
}
