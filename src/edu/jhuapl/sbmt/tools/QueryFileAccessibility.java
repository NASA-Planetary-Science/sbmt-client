package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.DownloadableFileManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;
import edu.jhuapl.sbmt.client.BasicConfigInfo;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main class that performs accessibility checks for supplied lists of URL
 * segments descending from a top-level SBMT root URL. The output is one line
 * per URL containing the original input URL segment, the
 * {@link UrlStatus} as a string, the content length (long) and last-modified time (long).
 *
 * @author peachjm1
 *
 */
public class QueryFileAccessibility implements Callable<Integer>
{
    private final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    @Parameters(index = "0", description = "The root URL from which all local file paths are determined")
    private String rootURLString;

    @Parameters(index = "1", description = "SBMT user name")
    private String userName;

    @Parameters(index = "2", description = "SBMT password")
    private String password;

    @Option(names = { "-f", "-file", "--file" }, description = "Get URLs to check from supplied file instead of from stdin (overrides -test option).")
    private String inputFile;

    @Option(names = { "-t", "-test", "--test" }, description = "Run in test mode; ignore stdin and check all SBMT model config files (ignored if -file option is also used)")
    private boolean testMode = false;

    @Option(names = { "-d", "-debug", "--debug" }, description = "Enable/disable debugging")
    private boolean debug = false;

    @Override
    public Integer call() throws Exception
    {
        Debug.setEnabled(debug);

        return queryFiles();
    }

    protected int queryFiles() throws IOException
    {
        FileCache.setSilenceInfoMessages(true);

        // Get a unique location for the file cache and point Configuration to it.
        UUID uniqueCacheDir = UUID.randomUUID();
        File cacheDir = SAFE_URL_PATHS.get(System.getProperty("user.home"), uniqueCacheDir.toString()).toFile();
        Configuration.setCacheDir(cacheDir.toString());

        // Initialize the tool/mission.
        SbmtMultiMissionTool.configureMission();

        // Find/set up root URL and point Configuration to it.
        Configuration.setRootURL(SAFE_URL_PATHS.getUrl(rootURLString));
        Configuration.setupPasswordAuthentication(userName, password.toCharArray());

        int result;
        try
        {
            if (!cacheDir.mkdirs())
            {
                throw new IOException("Unable to create directory " + cacheDir);
            }

            if (inputFile != null)
            {
                Debug.err().println("Reading URL segments from file " + inputFile);
                try (InputStream stream = new FileInputStream(inputFile))
                {
                    result = queryFilesFromStream(stream);
                }
            }
            else if (testMode)
            {
                Debug.err().println("Checking all model config files");
                result = queryAllModels();
            }
            else
            {
                Debug.err().println("Checking URL segments supplied on stdin");
                result = queryFilesFromStream(System.in);
            }
        }
        finally
        {
            if (!debug)
            {
                FileUtils.deleteDirectory(cacheDir);
            }
        }

        return result;
    }

    protected int queryAllModels() throws IOException
    {
        DownloadableFileManager fileManager = FileCache.instance();
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        SmallBodyViewConfig.initialize();

        for (BasicConfigInfo info : SmallBodyViewConfig.getConfigIdentifiers())
        {
            if (info.isEnabled())
            {
                String urlString = info.getConfigURL();
                builder.add(urlString);
                fileManager.getState(urlString);
            }
        }

        return query(fileManager, builder.build());
    }

    protected int queryFilesFromStream(InputStream stream) throws IOException
    {
        DownloadableFileManager fileManager = FileCache.instance();
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
        {
            while (reader.ready())
            {
                String urlString = reader.readLine();
                builder.add(urlString);
                fileManager.getState(urlString);
            }
        }

        return query(fileManager, builder.build());
    }

    protected int query(DownloadableFileManager fileManager, Iterable<String> urlStrings)
    {
        // Now actually query the status of all the files.
        fileManager.queryAll(true);

        for (String urlString : urlStrings)
        {
            UrlState state = fileManager.getState(urlString).getUrlState();
            System.out.println(urlString + "," + state.getStatus() + "," + state.getContentLength() + "," + state.getLastModified());
        }

        return 0;

    }

    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");

        int exitCode = new CommandLine(new QueryFileAccessibility()).execute(args);

        System.exit(exitCode);
    }

}
