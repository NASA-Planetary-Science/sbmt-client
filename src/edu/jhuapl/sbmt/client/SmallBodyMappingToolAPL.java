package edu.jhuapl.sbmt.client;

import java.io.File;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.SafePaths;


/**
 * This class contains the "main" function called at the start of the program
 * for the APL internal version. It sets up some APL version specific configuration
 * options and then calls the public (non-APL) version's main function.
 */
public class SmallBodyMappingToolAPL
{
    public static void main(String[] args)
    {
        String opSysName = System.getProperty("os.name").toLowerCase();
        if (opSysName.contains("mac"))
        {
            // to set the name of the app in the Mac App menu:
            System.setProperty("apple.awt.application.name", "Small Body Mapping Tool");
            //to show the menu bar at the top of the screen:
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // to show a more mac-like file dialog box
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
        }

        if (SbmtMultiMissionTool.getOption(args, "--debug") != null)
        {
            Debug.setEnabled(true);
        }

        Configuration.setAPLVersion(true);

        if (Configuration.getAppName() == null)
        {
            SbmtMultiMissionTool.configureMission();
        }

        try
        {
            // First try to see if there's a password.txt file in ~/.neartool. Then try the folder
            // containing the runsbmt script.
            String jarLocation = SmallBodyMappingToolAPL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String parent = new File(jarLocation).getParentFile().getParent();
            ImmutableList<Path> passwordFilesToTry = ImmutableList.of(
                    SafePaths.get(Configuration.getApplicationDataDir(), "password.txt"),
                    SafePaths.get(parent, "password.txt")
            );

            Configuration.setupPasswordAuthentication("http://sbmt.jhuapl.edu/internal/restricted", "DO_NOT_DELETE.TXT", passwordFilesToTry);
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
        }

        // Call the public version's main function
        SbmtMultiMissionTool.main(args);
    }
}
