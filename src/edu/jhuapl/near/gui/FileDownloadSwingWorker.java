package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;

import edu.jhuapl.near.util.FileCacheNew;

public class FileDownloadSwingWorker extends ProgressMonitorSwingWorker
{
	private String filename;

	public FileDownloadSwingWorker(Component c, String filename)
	{
		super(c);
		this.filename = filename;
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
		
		Runnable runner = new Runnable()
		{
			public void run()
			{
				FileCacheNew.getFileFromServer(filename);
			}
		};
		Thread downloadThread = new Thread(runner);
		downloadThread.start();

		long fileLength = fi.length;
		int progress = 0;
        setProgress(0);
        try
        {
            while (downloadThread.isAlive() && !isCancelled())
            {
                //Sleep for one second.
                Thread.sleep(1000);
                long amountSoFar = FileCacheNew.getAmountOfFileDownloadedSoFar(filename);
                long percentage = Math.round(100.0 * ((double)amountSoFar / (double)fileLength));
                if (percentage > progress)
                {
                	progress = (int) percentage;
                	setProgress(Math.min(progress, 99));
                }
            }
        }
        catch (InterruptedException ignore)
        {
        	//ignore.printStackTrace();
        }

        if (isCancelled())
        {
        	FileCacheNew.abortDownload();
        }
        else
        {
        	setProgress(100);
        }
        
        return null;
	}
}
