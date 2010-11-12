package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;

import edu.jhuapl.near.util.FileCacheNew;
import edu.jhuapl.near.util.FileUtil;

public class FileDownloadSwingWorker extends ProgressBarSwingWorker
{
	private String filename;

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

	@Override
	protected Void doInBackground()
	{
		FileCacheNew.FileInfo fi = FileCacheNew.getFileInfoFromServer(filename);
		if (fi.needToDownload == false)
			return null;

		// Delete the temporary file if it exists for some reason (e.g. the program crashed during
		// a previous download)
		File tempFile = new File(fi.file.getAbsolutePath() + FileCacheNew.getTemporarySuffix());
		tempFile.delete();
		
		FileCacheNew.resetDownloadProgess();
		FileUtil.resetUnzipProgress();

		Runnable runner = new Runnable()
		{
			public void run()
			{
				File file = FileCacheNew.getFileFromServer(filename);
				FileUtil.unzipFile(file, file.getParent());
			}
		};
		Thread downloadThread = new Thread(runner);
		downloadThread.start();

        setProgress(0);
        try
        {
            while (downloadThread.isAlive() && !isCancelled())
            {
                int downloadProgress = (int)Math.floor(FileCacheNew.getDownloadProgess());
                int unzipProgress = (int)Math.floor(FileUtil.getUnzipProgress());
                if (downloadProgress < 100)
                {
                	setLabelText("<html>Downloading Mapmaker<br>Completed " + downloadProgress + "%</html>");
                	setProgress(Math.min(downloadProgress, 99));
                }
                else if (unzipProgress < 100)
                {
                	setLabelText("<html>Unzipping Mapmaker<br>Completed " + unzipProgress + "%</html>");
                	setProgress(Math.min(unzipProgress, 99));
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
        	FileCacheNew.abortDownload();
        	FileUtil.abortUnzipping();
        }
        
        return null;
	}
}
