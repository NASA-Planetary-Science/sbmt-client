package edu.jhuapl.near.util;

import java.io.File;

public class Configuration
{
    static private String rootURL = "http://near.jhuapl.edu/software/apl";
    static private String appDir = null;
    static private String cacheDir = null;
    static private String cacheVersion = "2";
    static private String mapMaperDir = null;

    // Flag indicating if this version of the tool is APL in-house only ("private")
    static private boolean APLVersion = false;

    static
    {
        // If the user sets the sbmt.root.url property then use that
        // as the root URL. Otherwise use the default.
        String rootURLProperty = System.getProperty("sbmt.root.url");
        if (rootURLProperty != null)
            rootURL = rootURLProperty;
    }

    static public void setupPasswordAuthentication(final String username, final String password)
    {
        try
        {
            java.net.Authenticator.setDefault(new java.net.Authenticator()
            {
                protected java.net.PasswordAuthentication getPasswordAuthentication()
                {
                    return new java.net.PasswordAuthentication(username, password.toCharArray());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * @return Return the location where all application specific files should be stored. This is within
     * the .neartool folder located in the users home directory.
     */
    static public String getApplicationDataDir()
    {
        if (appDir == null)
        {
            appDir = System.getProperty("user.home") + File.separator + ".neartool";

            // if the directory does not exist, create it
            File dir = new File(appDir);
            if (!dir.exists())
            {
                dir.mkdir();
            }
        }

        return appDir;
    }

    /**
     * The cache folder is where files downloaded from the server are placed. The
     * URL of server is returned by getDataRootURL()
     * @return
     */
    static public String getCacheDir()
    {
        if (cacheDir == null)
        {
            cacheDir = Configuration.getApplicationDataDir() + File.separator +
            "cache" + File.separator + cacheVersion;
        }

        return cacheDir;
    }

    /**
     * @return Return the url of the server where data is downloaded from.
     */
    static public String getDataRootURL()
    {
        return rootURL + "/data";
    }

    /**
     * @return Return the url of the server where data is downloaded from.
     * for use by the APL in-house version
     */
    static public String getDataRootURLAPL()
    {
        return rootURL + "/data-apl";
    }

    static public String getQueryRootURL()
    {
        return rootURL + "/query";
    }

    static public String getHelpRootURL()
    {
        return rootURL + "/help/";
    }

    static public String getFeedbackRootURL()
    {
        return rootURL + "/feedback/";
    }

    static public String getImportedShapeModelsDir()
    {
        return getApplicationDataDir() + File.separator + "models";
    }

    static public String getMapmakerDir()
    {
        return mapMaperDir;
    }

    static public void setMapmakerDir(String folder)
    {
        mapMaperDir = folder;
    }

    static public boolean isMac()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("mac");
    }

    static public boolean isLinux()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    static public boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    static public void setAPLVersion(boolean b)
    {
        APLVersion = b;
    }

    static public boolean isAPLVersion()
    {
        return APLVersion;
    }

    static public String getCustomDataFolderForBuiltInViews()
    {
        return getApplicationDataDir() + File.separator + "custom-data";
    }

    static public String getTempFolder()
    {
        String tmpDir = getApplicationDataDir() + File.separator + "tmp";
        File dir = new File(tmpDir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return tmpDir;
    }

}
