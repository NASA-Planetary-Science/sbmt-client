package edu.jhuapl.sbmt.client;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileCache.NoInternetAccessException;
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
//            // to show a more mac-like file dialog box
//            System.setProperty("apple.awt.fileDialogForDirectories", "true");
        }

        if (SbmtMultiMissionTool.getOption(args, "--debug") != null)
        {
            Debug.setEnabled(true);
        }

        Configuration.setAPLVersion(true);

        SbmtMultiMissionTool.configureMission();

        URL dataRootUrl = Configuration.getDataRootURL();
        try
        {
        	// Just try to hit the server itself first.
            FileCache.getFileInfoFromServer(dataRootUrl.toString());

            // Set up two locations to check for passwords: in the installed location or in the user's home directory.
            String jarLocation = SmallBodyMappingToolAPL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String parent = new File(jarLocation).getParentFile().getParent();
            ImmutableList<Path> passwordFilesToTry = ImmutableList.of(
                    SafePaths.get(Configuration.getApplicationDataDir(), "password.txt"),
                    SafePaths.get(parent, "password.txt")
                    );

            Configuration.setupPasswordAuthentication(dataRootUrl, "DO_NOT_DELETE.TXT", passwordFilesToTry);
        }
        catch (NoInternetAccessException e)
        {
            e.printStackTrace();
            FileCache.setOfflineMode(true, Configuration.getCacheDir());
            JOptionPane.showMessageDialog(null, "Unable to find server " + dataRootUrl + ". Starting in offline mode. See console log for more information.", "No internet access", JOptionPane.INFORMATION_MESSAGE);
        }

        // Call the public version's main function
        SbmtMultiMissionTool.main(args);
    }
}
