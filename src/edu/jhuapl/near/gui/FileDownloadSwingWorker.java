package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;
import java.text.DecimalFormat;

import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileCache.FileInfo;
import edu.jhuapl.near.util.FileUtil;


public class FileDownloadSwingWorker extends ProgressBarSwingWorker
{
    private String filename;
    private DecimalFormat df = new DecimalFormat("0.00");
    private boolean useAPLServer;

    public FileDownloadSwingWorker(Component c, String title, String filename, boolean useAPLServer)
    {
        super(c, title);
        this.filename = filename;
        this.useAPLServer = useAPLServer;
        setLabelText("<html>Downloading file<br>0% completed</html>");
    }

    public static void downloadFile(Component c, String title, String filename, boolean useAPLServer)
    {
        FileDownloadSwingWorker worker = new FileDownloadSwingWorker(c, title, filename, useAPLServer);
        worker.executeDialog();
    }

    public boolean getIfNeedToDownload()
    {
        return FileCache.getFileInfoFromServer(filename, useAPLServer).needToDownload;
    }

    @Override
    protected Void doInBackground()
    {
        final FileInfo fileInfo = FileCache.getFileInfoFromServer(filename, useAPLServer);
        final boolean needToDownload = fileInfo.needToDownload;

        String zipfile = fileInfo.file.getAbsolutePath();
        File zipRootFolder = new File(zipfile.substring(0, zipfile.length()-4));
        final boolean needToUnzip = filename.endsWith(".zip") &&
                                    (needToDownload || !zipRootFolder.exists());

        if (!needToDownload && !needToUnzip)
            return null;

        if (needToDownload)
            FileCache.resetDownloadProgess();
        if (needToUnzip)
            FileUtil.resetUnzipProgress();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                File file = FileCache.getFileFromServer(filename, useAPLServer);

                if (file != null && needToUnzip)
                    FileUtil.unzipFile(file);
            }
        };
        Thread downloadThread = new Thread(runner);
        downloadThread.start();

        try
        {
            String name = new File(filename).getName();
            while (downloadThread.isAlive() && !isCancelled())
            {
                long downloadProgress = FileCache.getDownloadProgess();
                double percentDownloaded = 100.0 * (double)downloadProgress / (double)fileInfo.length;
                double downloadedSoFarInMB = (double)downloadProgress / 1048576.0;
                double totalSizeInMB = (double)fileInfo.length / 1048576.0;

                double unzipProgress = FileUtil.getUnzipProgress();
                if (downloadProgress < fileInfo.length && needToDownload)
                {
                    setLabelText("<html>Downloading " + name + "<br>" +
                            df.format(percentDownloaded) + "% completed " +
                             "  (" + df.format(downloadedSoFarInMB) + " of " +
                             df.format(totalSizeInMB) + " MB)</html>");

                    // Call firePropertyChange rather than setProgess since the latter will
                    // only cause a property change if the percent downloaded (cast to an int)
                    // changes, whereas firePropertyChange always forces a property change.
                    firePropertyChange("progress", null, Math.min((int)percentDownloaded, 99));
                }
                else if (unzipProgress < 100.0 && needToUnzip)
                {
                    setLabelText("<html>Unzipping " + name + "<br>" +
                            df.format(unzipProgress) + "% completed</html>");

                    // See comment in previous if block
                    firePropertyChange("progress", null, Math.min((int)unzipProgress, 99));
                }

                Thread.sleep(333);
            }
        }
        catch (InterruptedException ignore)
        {
            //ignore.printStackTrace();
        }

        if (isCancelled())
        {
            if (needToDownload)
                FileCache.abortDownload();
            if (needToUnzip)
                FileUtil.abortUnzip();
        }

        return null;
    }

    protected String getFileDownloaded()
    {
        return filename;
    }
}
