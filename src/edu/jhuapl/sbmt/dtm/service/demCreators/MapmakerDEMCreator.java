package edu.jhuapl.sbmt.dtm.service.demCreators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.FileDownloadSwingWorker;
import edu.jhuapl.saavtk.gui.ProgressBarSwingWorker;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk2.event.BasicEventSource;
import edu.jhuapl.saavtk2.task.BasicTask;
import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.saavtk2.task.TaskFinishedEvent;
import edu.jhuapl.saavtk2.task.TaskProgressEvent;
import edu.jhuapl.saavtk2.task.TaskStartedEvent;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;
import edu.jhuapl.sbmt.dtm.service.events.DEMCreatedEvent;
import edu.jhuapl.sbmt.util.MapmakerNativeWrapper;


public class MapmakerDEMCreator extends BasicEventSource implements DEMCreator
{
    Path exePathOnServer;
    Path demOutputBasePath;
    DEMKey demKey;
    MapmakerNativeWrapper wrapper = null;
    private Runnable completionBlock;
    DEMCreationTask task = null;

    static protected class DEMCreationTask extends BasicTask
    {

        MapmakerNativeWrapper wrapper;
        Runnable completionBlock;
        private DEMKey demKey;

        public DEMCreationTask(MapmakerNativeWrapper wrapper, DEMKey key)
        {
            this.wrapper=wrapper;
            this.demKey = key;
        }

        @Override
        public String getDisplayName()
        {
            return wrapper.getName();
        }

        @Override
        public void run()
        {
            fire(new TaskStartedEvent(this));
            processAndWait(wrapper);
            demKey = postProcessAndCreate(wrapper);
            fire(new DEMCreatedEvent(this, demKey));
            fire(new TaskFinishedEvent(this));
            completionBlock.run();
        }

        protected void processAndWait(MapmakerNativeWrapper wrapper)
        {
        	ProgressBarSwingWorker worker = new ProgressBarSwingWorker(null, "Mapmaker", "Running Mapmaker.....", true)
			{

				@Override
				protected Void doInBackground() throws Exception
				{
		            setProgress(1);

		            Process mapmakerProcess = wrapper.runMapmaker();
	                while (true)
	                {
	                    fire(new TaskProgressEvent(DEMCreationTask.this, -1));

	                    try
	                    {
	                        mapmakerProcess.exitValue();
	                        break;
	                    }
	                    catch (IllegalThreadStateException e)
	                    {
	                        // e.printStackTrace();
	                        // do nothing. We'll get here if the process is still
	                        // running
	                    }

	                    Thread.sleep(333);
	                }

	                setProgress(100);

		            return null;
				}
			};
			worker.executeDialog();
        }

        protected DEMKey postProcessAndCreate(MapmakerNativeWrapper wrapper)
        {
            wrapper.convertCubeToFitsAndSaveInOutputFolder(false);
            DEMKey demKey = new DEMKey(wrapper.getMapletFile().getAbsolutePath(),FilenameUtils.getBaseName(wrapper.getMapletFile().toString()));
            return demKey;
        }

		public void setCompletionBlock(Runnable completionBlock)
		{
			this.completionBlock = completionBlock;
		}

    }

    public MapmakerDEMCreator(Path exePathOnServer, Path demOutputBasePath)
    {
        this.demOutputBasePath = demOutputBasePath;
        this.exePathOnServer = exePathOnServer;
    }

    @Override
    public String getExecutableDisplayName()
    {
        return "MapMaker";
    }

    @Override
    public Path getExecutablePathOnServer()
    {
        return exePathOnServer;
    }

    @Override
    public Path getDEMOutputBasePath()
    {
        return demOutputBasePath;
    }

    @Override
    public boolean needToDownloadExecutable()
    {
        String unzippedDirectoryName = exePathOnServer.toString().replaceFirst("\\.[^\\.]*$", "");
        File unzippedDir = FileCache.instance().getFile(unzippedDirectoryName);

    	return !unzippedDir.isDirectory();
    }

    @Override
    public Task getCreationTask(String name, double latDeg, double lonDeg,
            double pixScaleMeters, int pixHalfSize)
    {
        createWrapperInstance();
        if (wrapper == null) return null;
        wrapper.setName(name);
        wrapper.setLatitude(latDeg);
        wrapper.setLongitude(lonDeg);
        wrapper.setPixelSize(pixScaleMeters);
        wrapper.setHalfSize(pixHalfSize);
        wrapper.setOutputFolder(getDEMOutputBasePath().toFile());
        task = new DEMCreationTask(wrapper, demKey);
        task.setCompletionBlock(completionBlock);
        return task;
    }


    @Override
    public Task getCreationTask(String name, double[] center, double radius,
            int pixHalfSize)
    {

        createWrapperInstance();
        if (wrapper == null) return null;
        wrapper.setName(name);
        LatLon ll = MathUtil.reclat(center).toDegrees();
        wrapper.setLatitude(ll.lat);
        wrapper.setLongitude(ll.lon);
        wrapper.setPixelSize(1000.0 * 1.5 * radius / (double) pixHalfSize);
        wrapper.setHalfSize(pixHalfSize);
        wrapper.setOutputFolder(getDEMOutputBasePath().toFile());
        task = new DEMCreationTask(wrapper, demKey);
        task.setCompletionBlock(completionBlock);
        return task;
    }

    protected void createWrapperInstance()
    {
    	if (!needToDownloadExecutable())
    	{
    		makeWrapper();
    	}
    	else
    	{
	    	int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(null), //
	    	        "Before " + getExecutableDisplayName() + //
	    	        " can be run for the first time, a very large file needs to be downloaded.\n" + //
	    	        "This may take several minutes. Would you like to continue?", //
	    	        "Confirm Download", JOptionPane.YES_NO_OPTION);
	        if (result != JOptionPane.NO_OPTION)
	        {
		    	FileDownloadSwingWorker downloadWorker = FileDownloadSwingWorker.of(getExecutablePathOnServer().toString(), null, "Download " + getExecutableDisplayName(), false, () -> {
		    	    makeWrapper();
		    	});

		    	downloadWorker.executeDialog();
	        }
    	}
    }

    private void makeWrapper()
    {
        File file = FileCache.instance().getFile(getExecutablePathOnServer().getParent().toString());
        String mapmakerRootDir = file.toPath().resolve("mapmaker").toString();
        try
        {
            wrapper = new MapmakerNativeWrapper(mapmakerRootDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            wrapper = null;
        }
    }

	@Override
	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}

	@Override
	public DEMKey getDEMKey()
	{
		return task.demKey;
	}
}
