package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;
import java.text.DecimalFormat;

import edu.jhuapl.near.util.FileCacheNew;
import edu.jhuapl.near.util.FileUtil;


public class FileDownloadSwingWorker extends ProgressBarSwingWorker
{
	private String filename;
    private DecimalFormat decimalFormatter = new DecimalFormat("0.000");

	public FileDownloadSwingWorker(Component c, String title, String filename)
	{
		super(c, title);
		this.filename = filename;
    	setLabelText("<html>Downloading file<br>Completed 0%</html>");
	}

	public boolean getIfNeedToDownload()
	{
		return FileCacheNew.getFileInfoFromServer(filename).needToDownload;
	}

	public boolean getIfNeedToUnzip()
	{
		String zipfile = FileCacheNew.getFileInfoFromServer(filename).file.getAbsolutePath();
		File zipRootFolder = new File(zipfile.substring(0, zipfile.length()-4));

		return !zipRootFolder.exists() && zipfile.endsWith(".zip");
	}

	@Override
	protected Void doInBackground()
	{
		final boolean needToDownload = getIfNeedToDownload();
		final boolean needToUnzip = needToDownload || getIfNeedToUnzip();
		if (!needToDownload && !needToUnzip)
			return null;

    	if (needToDownload)
    		FileCacheNew.resetDownloadProgess();
    	if (needToUnzip)
    		FileUtil.resetUnzipProgress();

		Runnable runner = new Runnable()
		{
			public void run()
			{
				File file = FileCacheNew.getFileFromServer(filename);

				if (file != null && needToUnzip)
					FileUtil.unzipFile(file);
			}
		};
		Thread downloadThread = new Thread(runner);
		downloadThread.start();

        try
        {
            while (downloadThread.isAlive() && !isCancelled())
            {
                double downloadProgress = FileCacheNew.getDownloadProgess();
                double unzipProgress = FileUtil.getUnzipProgress();
                if (downloadProgress < 100.0 && needToDownload)
                {
                	setLabelText("<html>Downloading Mapmaker<br>Completed " +
                			decimalFormatter.format(downloadProgress) + "%</html>");
                	setProgress(Math.min((int)downloadProgress, 99));
                }
                else if (unzipProgress < 100.0 && needToUnzip)
                {
                	setLabelText("<html>Unzipping Mapmaker<br>Completed " +
                			decimalFormatter.format(unzipProgress) + "%</html>");
                	setProgress(Math.min((int)unzipProgress, 99));
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
        		FileCacheNew.abortDownload();
        	if (needToUnzip)
        		FileUtil.abortUnzip();
        }

        return null;
	}
}
