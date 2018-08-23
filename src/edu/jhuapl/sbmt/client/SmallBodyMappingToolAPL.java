package edu.jhuapl.sbmt.client;

import edu.jhuapl.saavtk.util.Configuration;

/**
 * This class contains the "main" function called at the start of the program
 * for the APL internal version. It sets up some APL version specific
 * configuration options and then calls the public (non-APL) version's main
 * function.
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

		Configuration.setAPLVersion(true);

		SbmtMultiMissionTool.setEnableAuthentication(true);

		// Call the standard client main function
		SbmtMultiMissionTool.main(args);
	}
}
