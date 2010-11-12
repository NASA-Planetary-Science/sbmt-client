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
				if (file != null)
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
                double downloadProgress = FileCacheNew.getDownloadProgess();
                double unzipProgress = FileUtil.getUnzipProgress();
                if (downloadProgress < 100.0)
                {
                	setLabelText("<html>Downloading Mapmaker<br>Completed " +
                			decimalFormatter.format(downloadProgress) + "%</html>");
                	setProgress(Math.min((int)downloadProgress, 99));
                }
                else if (unzipProgress < 100.0)
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
        	FileCacheNew.abortDownload();
        	FileUtil.abortUnzipping();
        }
        
        return null;
	}
}
