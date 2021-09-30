package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.UrlStatus;
import edu.jhuapl.sbmt.client.users.AccessGroup;
import edu.jhuapl.sbmt.client.users.AccessGroupCollection;
import edu.jhuapl.sbmt.client.users.User;
import edu.jhuapl.sbmt.client.users.UserCollection;
import edu.jhuapl.sbmt.client.users.UserSerialization;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.gson.Serializers;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Server-side file-system-only checker for file access.
 *
 * @author peachjm1
 *
 */
public class CheckUserAccess implements Callable<Integer>
{
    private static final SafeURLPaths SafePaths = SafeURLPaths.instance();

    private static final String UrlEncoding = "UTF-8";

    @Parameters(index = "0", description = "The root URL from which all local file paths are determined")
    private String rootURLString;

    @Parameters(index = "1", description = "SBMT user name")
    private String userName;

    @Option(names = { "-f", "-file", "--file" }, description = "Get URLs to check from supplied file instead of from stdin (overrides -test option).")
    private String inputFile;

    @Option(names = { "-t", "-test", "--test" }, description = "Run in test mode; ignore stdin and check all SBMT model config files (ignored if -file option is also used)")
    private boolean testMode = false;

    @Option(names = { "-d", "-debug", "--debug" }, description = "Enable/disable debugging")
    private boolean debug = false;

    @Option(names = { "-e", "-encode", "--encode" }, description = "Enable/disable URL decoding (true when used as back-end of web page, false for running from command line)")
    private boolean decodingEnabled = true;

    // This is changed after startup.
    private Path serverTopPath = SafePaths.get("/project/sbmt2");

    private Path accessControlFilesPath = SafePaths.get("/project/sbmt2/sbmt/data/accessControl");

    protected CheckUserAccess()
    {
        super();
    }

    @Override
    public Integer call() throws Exception
    {
        Debug.setEnabled(debug);

        UserSerialization.initializeSerializationProxies();

        rootURLString = SafePaths.getUrl(rootURLString);
        userName = decodeIfEnabled(userName);

        serverTopPath = SafePaths.get(serverTopPath.toString(), identifyDataArea());

        int result;
        if (inputFile != null)
        {
            Debug.of().err().println("Reading URL segments from file " + inputFile);
            try (InputStream stream = new FileInputStream(inputFile))
            {
                result = queryFilesFromStream(stream, false);
            }
        }
        else if (testMode)
        {
            Debug.of().err().println("Testing this utility");
            result = testQueryFiles();
        }
        else
        {
            Debug.of().err().println("Checking URL segments supplied on stdin");
            result = queryFilesFromStream(System.in, true);
        }

        return result;
    }

    protected String identifyDataArea()
    {
        ImmutableList<String> serverAreas = ImmutableList.of("prod", "test", "stage");
        for (String area : serverAreas)
        {
            if (rootURLString.matches(".*\\b" + area + "\\b.*"))
            {
                return area;
            }
        }

        return serverAreas.get(0);
    }

    protected int queryFilesFromStream(InputStream stream, boolean decodeIfNecessary) throws IOException
    {
        FixedMetadata groupsMetadata = Serializers.deserialize(accessControlFilesPath.resolve("accessGroupCollection.json").toFile(), "AccessGroupCollection");
        AccessGroupCollection groupCollection = fromMetadata(Key.of("AccessGroupCollection"), groupsMetadata);

        FixedMetadata userMetadata = Serializers.deserialize(accessControlFilesPath.resolve("userCollection.json").toFile(), "UserCollection");
        UserCollection userCollection = fromMetadata(Key.of("UserCollection"), userMetadata);

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
        {
            while (reader.ready())
            {
                String urlString = reader.readLine();
                if (decodeIfNecessary)
                {
                    urlString = decodeIfEnabled(urlString);
                }
                builder.add(urlString);
            }
        }

        query(builder.build(), groupCollection, userCollection);

        return 0;
    }

    protected void query(Iterable<String> urlStrings, AccessGroupCollection groupCollection, UserCollection userCollection)
    {
        Map<String, Set<String>> urlToGroupMap = groupCollection.getURLToGroupIdMap();

        for (String url : urlStrings)
        {
            writeAccessStatus(url, urlToGroupMap, userCollection);
        }
    }

    protected void writeAccessStatus(String urlString, Map<String, Set<String>> urlToGroupMap, UserCollection userCollection)
    {
        boolean authorized = isAuthorized(urlString, urlToGroupMap, userCollection);

        UrlStatus status = UrlStatus.UNKNOWN;
        long length = -1;
        long lastModified = 0;
        if (authorized)
        {
            File file = SafePaths.get(serverTopPath.toString(), urlString).toFile();
            status = file.exists() ? UrlStatus.ACCESSIBLE : UrlStatus.NOT_FOUND;
            length = file.length();
            lastModified = file.lastModified();
        }
        else
        {
            status = UrlStatus.NOT_AUTHORIZED;
        }

        System.out.println(urlString + "," + status + "," + length + "," + lastModified);
        System.out.flush();
    }

    protected boolean isAuthorized(String urlString, Map<String, Set<String>> urlToGroupMap, UserCollection userCollection)
    {
        String matchString = AccessGroup.cleanPath(urlString);

        Set<String> authorizedGroupIds = null;
        while (authorizedGroupIds == null && matchString.matches("^.*\\S.*$"))
        {
            authorizedGroupIds = urlToGroupMap.get(matchString);
            matchString = matchString.replaceFirst("/*[^/]+$", "");
        }

        boolean result = false;
        if (authorizedGroupIds == null)
        {
            result = true;
        }
        else
        {
            User user = userCollection.getUser(userName);

            if (user == null)
            {
                // Give public access to any user not explicitly in the
                // collection.
                user = User.ofPublic();
            }
            for (String groupId : authorizedGroupIds)
            {
                if (user.isInGroup(groupId))
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    protected String decodeIfEnabled(String string)
    {
        try
        {
            return decodingEnabled ? URLDecoder.decode(string, UrlEncoding) : string;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }

    protected int testQueryFiles()
    {
        accessControlFilesPath = Paths.get(System.getProperty("user.home")).resolve("UserAccessChecker");
        try
        {
            Files.createDirectories(accessControlFilesPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Whoops, abort");
        }

        serverTopPath = Paths.get(System.getProperty("user.home")).resolve(".sbmt/cache/2");
        userName = "h2.tir.coi";

        UserCollection userCollection;
        try
        {
            FixedMetadata metadata = Serializers.deserialize(accessControlFilesPath.resolve("userCollection.json").toFile(), "UserCollection");
            userCollection = fromMetadata(Key.of("UserCollection"), metadata);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            userCollection = UserCollection.of( //
                    ImmutableList.of( //
                            User.of("user1", ImmutableList.of("groupA", "groupB")), //
                            User.of("user0", ImmutableList.of("groupA")) //
                    ), ImmutableList.of("groupA", "groupB"));
        }

        AccessGroupCollection groupCollection;
        try
        {
            FixedMetadata metadata = Serializers.deserialize(accessControlFilesPath.resolve("accessGroupCollection.json").toFile(), "AccessGroupCollection");
            groupCollection = fromMetadata(Key.of("AccessGroupCollection"), metadata);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            groupCollection = groupsFromUsers(userCollection);
        }

        try
        {
            Serializers.serialize("UserCollection", toMetadata(UserCollection.class, userCollection), accessControlFilesPath.resolve("userCollectionValidation.json").toFile());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Serializers.serialize("AccessGroupCollection", toMetadata(AccessGroupCollection.class, groupCollection), accessControlFilesPath.resolve("accessGroupCollectionValidation.json").toFile());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ImmutableList<String> testURLs = ImmutableList.of( //
                "GASKELL/EROS/Gaskell_433_Eros_v7.8.json", //
                "ryugu/gaskell/Gaskell_162173_Ryugu_v7.8.json", //
                "ryugu/gaskell/onc/imagelist-sum.txt", //
                "ryugu/jaxa-spc-v20181014/tir/imagelist-info.txt" //
        );

        query(testURLs, groupCollection, userCollection);

        return 0;
    }

    public static void main(String[] args)
    {
        int exitCode = 1;
        try
        {
            System.setProperty("java.awt.headless", "true");

            // Start a self-destruct thread to ensure this doesn't linger
            // forever if the PHP script that calls this does not close the pipes
            // cleanly. Have observed zombie processes accumulating
            // over time on the running server. Note that this explicit
            // self-destruct is entirely for the benefit of the server and is
            // not at all related to tuning query/cache performance.
            Executors.newSingleThreadExecutor().execute(() -> {
                int selfDestructCode = 0;
                try
                {
                    // 28 s is ~5 times longer than this tool should take.
                    Thread.sleep(28000);
                    selfDestructCode = 1;
                }
                catch (Exception e)
                {

                }
                finally
                {
                    System.out.flush();
                    System.err.flush();
                    System.exit(selfDestructCode);
                }
            });

            exitCode = new CommandLine(new CheckUserAccess()).execute(args);
        }
        catch (Throwable t)
        {

        }
        finally
        {
            System.out.flush();
            System.err.flush();
            System.exit(exitCode);
        }
    }

    private static AccessGroupCollection groupsFromUsers(UserCollection userCollection)
    {
        LinkedHashSet<String> groupIds = new LinkedHashSet<>();
        for (User user : userCollection.getUsers())
        {
            groupIds.addAll(user.getGroupIds());
        }

        ImmutableList<String> directoryPlaceholder = ImmutableList.of("directory");
        ImmutableList.Builder<AccessGroup> builder = ImmutableList.builder();
        for (String groupId : groupIds)
        {
            builder.add(AccessGroup.of(groupId, directoryPlaceholder));
        }

        return AccessGroupCollection.of(builder.build());
    }

    private static <T> T fromMetadata(Key<T> proxyTypeKey, FixedMetadata metadata)
    {
        InstanceGetter instanceGetter = InstanceGetter.defaultInstanceGetter();

        return instanceGetter.providesGenericObjectFromMetadata(proxyTypeKey).provide(metadata);
    }

    private static <T> Metadata toMetadata(Class<T> type, T object)
    {
        InstanceGetter instanceGetter = InstanceGetter.defaultInstanceGetter();

        return instanceGetter.providesMetadataFromGenericObject(type).provide(object);
    }

}
