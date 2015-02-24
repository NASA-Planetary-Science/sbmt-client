package edu.jhuapl.near.tools;

import java.io.File;
import java.util.ArrayList;

import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileUtil;


/**
 * This class contains the "main" function called at the start of the program
 * for the APL internal version. It sets up some APL version specific configuration
 * options and then calls the public (non-APL) version's main function.
 */
public class SmallBodyMappingToolAPL
{
    public static void main(String[] args)
    {
        Configuration.setAPLVersion(true);

        String username = null;
        String password = null;

        try
        {
            // First try to see if there's a password.txt file in ~/.neartool, otherwise
            // try the current directory.
            String[] passwordFilesToTry = {
                    Configuration.getApplicationDataDir() + File.separator + "password.txt",
                    "password.txt"
            };

            for (String passwordFile : passwordFilesToTry)
            {
                if (new File(passwordFile).exists())
                {
                    ArrayList<String> credentials = FileUtil.getFileLinesAsStringList(passwordFile);
                    if (credentials.size() >= 2)
                    {
                        String user = credentials.get(0);
                        String pass = credentials.get(1);

                        if (user != null && user.trim().length() > 0 && !user.trim().toLowerCase().contains("replace-with-") &&
                            pass != null && pass.trim().length() > 0)
                        {
                            username = user.trim();
                            password = pass.trim();
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        }

        if (username != null && password != null)
        {
            Configuration.setupPasswordAuthentication(username, password);
            System.out.println("Password file found.  Username: " + username + "  Password: " + password);
        }
        else
        {
            System.out.println("Warning: no correctly formatted password file found. "
                    + "Continuing without password. Certain functionality may not work.");
        }

        // Call the public version's main function
        SmallBodyMappingTool.main(args);
    }
}
