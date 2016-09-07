package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.util.ArrayList;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;


/**
 * This class contains the "main" function called at the start of the program
 * for the APL internal version. It sets up some APL version specific configuration
 * options and then calls the public (non-APL) version's main function.
 */
public class SmallBodyMappingToolAPL
{
    public static void main(String[] args)
    {
        Configuration.setAppName("neartool");
        Configuration.setCacheVersion("2");
        Configuration.setAPLVersion(true);

        String username = null;
        String password = null;

        try
        {
            // First try to see if there's a password.txt file in ~/.neartool. Then try the folder
            // containing the runsbmt script.
            String jarLocation = SmallBodyMappingToolAPL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String parent = new File(jarLocation).getParentFile().getParent();
            String[] passwordFilesToTry = {
                    Configuration.getApplicationDataDir() + File.separator + "password.txt",
                    parent + File.separator + "password.txt"
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
