package edu.jhuapl.near.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class FileCache
{
    // Stores files already downloaded in this process
    private static ConcurrentHashMap<String, Object> downloadedFiles =
        new ConcurrentHashMap<String, Object>();

    private static volatile boolean abortDownload = false;
    private static volatile double downloadProgress = 0.0;

    public static class FileInfo
    {
        public File file = null;
        public boolean needToDownload = false;
        public long length = -1;
    }

    /**
     * This function retrieves the specifed file from the server and places it
     * in the cache. It first checks the cache to see if the file is already
     * there. The cache mirrors the file hierarchy on the server.
     *
     * The rules for determining whether or not we download the file from the
     * server or use the file already in the cache are as follows:
     *
     * - If the file does not exist in the cache, download it. - If the file
     * does exist, and was already downloaded by this very process (files
     * already downloaded are stored in the downloadedFiles hash set), then
     * return the cached file without comparing last modified times. - If the
     * file does exist, and has not been previously downloaded by this process,
     * compare the last modified time of the cached file to the remote file on
     * server. If the remote file is newer, download it, otherwise return the
     * cached file. - If there was a failure connecting to the server simply
     * return the file if it exists in the cache. - If the file could not be
     * retrieved for any reason, null is returned.
     *
     * @param path
     * @return
     */
    static private FileInfo getFileInfoFromServer(String path, boolean doDownloadIfNeeded)
    {
        FileInfo fi = new FileInfo();

        String unzippedPath = path;
        if (unzippedPath.toLowerCase().endsWith(".gz"))
            unzippedPath = unzippedPath.substring(0, unzippedPath.length() - 3);

        File file = new File(Configuration.getCacheDir() + File.separator
                + unzippedPath);

        fi.file = file;

        // If we've already downloaded the file previously in this process,
        // simply return without making any network connections.
        boolean exists = file.exists();
        if (exists && downloadedFiles.containsKey(path))
        {
            fi.length = file.length();
            return fi;
        }

        // Open a connection the file on the server
        try
        {
            URL u = new URL(Configuration.getDataRootURL() + path);
            URLConnection conn = u.openConnection();

            long urlLastModified = conn.getLastModified();

            if (exists && file.lastModified() >= urlLastModified)
            {
                fi.length = file.length();
                return fi;
            }
            else
            {
                if (doDownloadIfNeeded)
                {
                    file = addToCache(path, conn.getInputStream(), urlLastModified, conn.getContentLength());
                    if (file != null) // file can be null if the user aborted the download
                    {
                        downloadedFiles.put(path, "");
                        fi.length = file.length();
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    fi.needToDownload = true;
                    fi.length = conn.getContentLength();
                }

                return fi;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // If something happens that we reach here, simply return the file if it
        // exists.
        if (exists)
        {
            fi.length = file.length();
            return fi;
        }
        else
        {
            return null;
        }
    }

    static public FileInfo getFileInfoFromServer(String path)
    {
        return getFileInfoFromServer(path, false);
    }

    static public File getFileFromServer(String path)
    {
        FileInfo fi = getFileInfoFromServer(path, true);
        if (fi != null)
            return fi.file;
        else
            return null;
    }


    /**
     * When adding to the cache, gzipped files are always uncompressed and saved
     * without the ".gz" extension.
     *
     * @throws IOException
     */
    static private File addToCache(String path, InputStream is, long urlLastModified, long contentLength) throws IOException
    {
        if (path.toLowerCase().endsWith(".gz"))
            is = new GZIPInputStream(is);

        if (path.toLowerCase().endsWith(".gz"))
            path = path.substring(0, path.length()-3);

        // While we are downloading the file, the file should be named on disk
        // with a ".part_sbmt" suffix so that if the user forcibly kills the program
        // during a download, the file will not be used when the program is restarted.
        // After the download is successful, rename the file to the correct name.
        String realFilename = Configuration.getCacheDir() + File.separator + path;
        File file = new File(realFilename + getTemporarySuffix());

        file.getParentFile().mkdirs();

        FileOutputStream os = new FileOutputStream(file);

        abortDownload = false;
        boolean downloadAborted = false;
        downloadProgress = 0.0;

        int amountDownloadedSoFar = 0;

        final int bufferSize = 2048;
        byte[] buff = new byte[bufferSize];
        int len;
        while((len = is.read(buff)) > 0)
        {
            amountDownloadedSoFar += len;
            downloadProgress = 100.0 * (double)amountDownloadedSoFar / (double)contentLength;

            if (abortDownload)
            {
                downloadAborted = true;
                break;
            }

            os.write(buff, 0, len);
        }

        os.close();
        is.close();

        downloadProgress = 100.0;

        if (downloadAborted)
        {
            file.delete();
            return null;
        }

        // Change the modified time of the file to that of the server.
        if (urlLastModified > 0)
            file.setLastModified(urlLastModified);

        // Okay, now rename the file to the real name.
        File realFile = new File(realFilename);
        realFile.delete();
        file.renameTo(realFile);

        // Change the modified time again just in case the process of
        // renaming the file caused the modified time to change.
        // (On Linux, changing the filename, does not change the modified
        // time so this is not necessary, but I'm not sure about other platforms)
        if (urlLastModified > 0)
            realFile.setLastModified(urlLastModified);

        return realFile;
    }

    static public String getTemporarySuffix()
    {
        return FileUtil.getTemporarySuffix();
    }

    static public void abortDownload()
    {
        abortDownload = true;
    }

    static public double getDownloadProgess()
    {
        return downloadProgress;
    }

    static public void resetDownloadProgess()
    {
        downloadProgress = 0.0;
    }
}
