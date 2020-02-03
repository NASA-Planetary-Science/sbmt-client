package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.DownloadableFileManager;
import edu.jhuapl.saavtk.util.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.BasicConfigInfo;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class QueryModelAccessibility implements Callable<Integer>
{
    private final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    @Parameters(index = "0", description = "The root URL from which all local file paths are determined")
    private String rootURLString;

    @Parameters(index = "1", description = "SBMT user name")
    private String userName;

    @Parameters(index = "2", description = "SBMT password")
    private String password;

    @Option(names = { "-d", "-debug", "--debug" }, description = "Enable/disable debugging")
    private boolean debug = false;

    @Option(names = { "-debug-cache", "--debug-cache" }, description = "Enable/disable file cache debugging")
    private boolean debugCache = false;

    @Override
    public Integer call() throws Exception
    {
        Debug.setEnabled(debug);
        FileCache.enableDebug(debugCache);

        return getUserModelsFromConfigs();
    }

    protected Integer getUserModelsFromConfigs() throws IOException
    {
        FileCache.enableInfoMessages(false);

        // Get a unique location for the file cache and point Configuration to it.
        UUID uniqueCacheDir = UUID.randomUUID();
        File cacheDir = SAFE_URL_PATHS.get(System.getProperty("user.home"), uniqueCacheDir.toString()).toFile();
        Configuration.setCacheDir(cacheDir.toString());

        // Initialize the tool/mission.
        SbmtMultiMissionTool.configureMission();

        // Find/set up root URL and point Configuration to it.
        Configuration.setRootURL(SAFE_URL_PATHS.getUrl(rootURLString));
        Configuration.setupPasswordAuthentication(userName, password.toCharArray());

        try
        {
            if (!cacheDir.mkdirs())
            {
                throw new IOException("Unable to create directory " + cacheDir);
            }

            DownloadableFileManager fileManager = FileCache.instance();

            SmallBodyViewConfig.initialize();
            List<BasicConfigInfo> allConfigInfo = SmallBodyViewConfig.getConfigIdentifiers();
            for (BasicConfigInfo info : allConfigInfo)
            {
                if (info.isEnabled())
                {
                    fileManager.getState(info.getConfigURL());
                }
            }
            fileManager.queryAll(true);

            for (BasicConfigInfo info : allConfigInfo)
            {
                if (info.isEnabled())
                {
                    DownloadableFileState state = fileManager.getState(info.getConfigURL());
                    if (state.isAccessible()) {
                        System.out.println(info.getUniqueName());
                    }
                }
            }
        }
        finally
        {
            if (!debug)
            {
                FileUtils.deleteDirectory(cacheDir);
            }
        }
        return 0;

    }

    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");

        int exitCode = new CommandLine(new QueryModelAccessibility()).execute(args);

        System.exit(exitCode);
    }

}
