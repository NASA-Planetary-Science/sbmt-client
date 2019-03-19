package edu.jhuapl.sbmt.gui.dtm.ui.creation;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk2.task.BasicTask;
import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.saavtk2.task.TaskFinishedEvent;
import edu.jhuapl.saavtk2.task.TaskStartedEvent;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.dem.CustomDEMPanel.DEMInfo;
import edu.jhuapl.sbmt.gui.dem.MapmakerRemoteSwingWorker;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public class MapmakerRemoteDEMCreator implements DEMCreator
{

	 static protected class RemoteDEMCreationTask extends BasicTask
    {

        Runnable wrapper;

        public RemoteDEMCreationTask(Runnable wrapper)
        {
            this.wrapper=wrapper;
        }

        @Override
        public String getDisplayName()
        {
        	return "Remote DEM Creator";
        }

        @Override
        public void run()
        {
            fire(new TaskStartedEvent(this));
            processAndWait(wrapper);
//            DEMKey key = postProcessAndCreate(wrapper);
//            fire(new DEMCreatedEvent(this, key));
            fire(new TaskFinishedEvent(this));
        }

        protected void processAndWait(Runnable wrapper)
        {
        	wrapper.run();
//            Process mapmakerProcess;
//            try
//            {
//                mapmakerProcess = wrapper.runMapmaker();
//                while (true)
//                {
//                    // if (isCancelled())
//                    // break;
//
//                    fire(new TaskProgressEvent(RemoteDEMCreationTask.this, -1));
//
//                    try
//                    {
//                        mapmakerProcess.exitValue();
//                        break;
//                    }
//                    catch (IllegalThreadStateException e)
//                    {
//                        // e.printStackTrace();
//                        // do nothing. We'll get here if the process is still
//                        // running
//                    }
//
//                    Thread.sleep(333);
//                }
//            }
//            catch (IOException | InterruptedException e1)
//            {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }

        }

        protected DEMKey postProcessAndCreate(Runnable wrapper)
        {
        	return null;
//	            wrapper.convertCubeToFitsAndSaveInOutputFolder(false);
//	            return new DEMKey(wrapper.getMapletFile().getAbsolutePath(),FilenameUtils.getBaseName(wrapper.getMapletFile().toString()));
        }

    }



	SmallBodyViewConfig smallBodyConfig;
    Path demOutputBasePath;
    DEMInfo newDemInfo;
    private Runnable completionBlock;


	public MapmakerRemoteDEMCreator(Path demOutputBasePath, SmallBodyViewConfig smallBodyConfig)
	{
		this.smallBodyConfig = smallBodyConfig;
		this.demOutputBasePath = demOutputBasePath;
	}

	@Override
	public String getExecutableDisplayName()
	{
		return "Remote DEM Creator";
	}

	@Override
	public Path getExecutablePathOnServer()
	{
		return null;
	}

	@Override
	public boolean needToDownloadExecutable()
	{
		return false;
	}

	@Override
	public Path getDEMOutputBasePath()
	{
		 return demOutputBasePath;
	}

	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}

	@Override
	public Task getCreationTask(String demName, double latDeg, double lonDeg, double pixScaleMeters, int pixHalfSize)
	{
		return new RemoteDEMCreationTask(new Runnable()
		{

			@Override
			public void run()
			{
		        final MapmakerRemoteSwingWorker mapmakerWorker = new MapmakerRemoteSwingWorker(null, "Running Mapmaker", "");
		        mapmakerWorker.setRotationRate(smallBodyConfig.bodyRotationRate);
		        mapmakerWorker.setReferencePotential(smallBodyConfig.bodyReferencePotential);
		        mapmakerWorker.setDensity(smallBodyConfig.bodyDensity);
		        mapmakerWorker.setBodyLowestResModelName(smallBodyConfig.bodyLowestResModelName);

		        mapmakerWorker.setLatitude(latDeg);
		        mapmakerWorker.setLongitude(lonDeg);
		        mapmakerWorker.setPixelScale(pixScaleMeters);
		        mapmakerWorker.setRegionSpecifiedWithLatLonScale(true);

		        mapmakerWorker.setName(demName);
		        mapmakerWorker.setHalfSize(pixHalfSize);
		        mapmakerWorker.setCacheDir(getDEMOutputBasePath().toFile().getAbsolutePath());
		        File lowResPath = FileCache.getFileFromServer(smallBodyConfig.bodyLowestResModelName);
		        String path = lowResPath.getAbsolutePath() + "." + FilenameUtils.getExtension(smallBodyConfig.getShapeModelFileNames()[0]);
		        mapmakerWorker.setLowResModelPath(new File(path).getAbsolutePath());

		        mapmakerWorker.setDatadir(smallBodyConfig.rootDirOnServer + File.separator + "DATA");
		        mapmakerWorker.setMapoutdir(smallBodyConfig.rootDirOnServer + File.separator + "MAPFILES");
		        mapmakerWorker.executeDialog();

		        if (mapmakerWorker.isCancelled())
		            return;

		        newDemInfo = new DEMInfo();
		        newDemInfo.name = demName;
		        newDemInfo.demfilename = new File(getDEMOutputBasePath().toFile() + File.separator + demName + ".fits").getAbsolutePath();
		        System.out.println("MapmakerRemoteDEMCreator.getCreationTask(...).new Runnable() {...}: run: new dem info filename " + newDemInfo.demfilename);
		        completionBlock.run();
			}
		});
	}

	@Override
	public Task getCreationTask(String demName, double[] center, double radius, int pixHalfSize)
	{
		return new RemoteDEMCreationTask(new Runnable()
		{

			@Override
			public void run()
			{
				final MapmakerRemoteSwingWorker mapmakerWorker = new MapmakerRemoteSwingWorker(null, "Running Mapmaker", "");
		        mapmakerWorker.setRotationRate(smallBodyConfig.bodyRotationRate);
		        mapmakerWorker.setReferencePotential(smallBodyConfig.bodyReferencePotential);
		        mapmakerWorker.setDensity(smallBodyConfig.bodyDensity);
		        mapmakerWorker.setBodyLowestResModelName(smallBodyConfig.bodyLowestResModelName);

		        mapmakerWorker.setRegionSpecifiedWithLatLonScale(false);
		        mapmakerWorker.setCenterPoint(center);
		        mapmakerWorker.setRadius(radius);

		        mapmakerWorker.setName(demName);
		        mapmakerWorker.setHalfSize(pixHalfSize);
		        mapmakerWorker.setCacheDir(getDEMOutputBasePath().toFile().getAbsolutePath());
		        File lowResPath = FileCache.getFileFromServer(smallBodyConfig.bodyLowestResModelName);
		        String path = lowResPath.getAbsolutePath() + "." + FilenameUtils.getExtension(smallBodyConfig.getShapeModelFileNames()[0]);
		        mapmakerWorker.setLowResModelPath(new File(path).getAbsolutePath());

		        mapmakerWorker.setDatadir(smallBodyConfig.rootDirOnServer + File.separator + "DATA");
		        mapmakerWorker.setMapoutdir(smallBodyConfig.rootDirOnServer + File.separator + "MAPFILES");
		        mapmakerWorker.executeDialog();

		        if (mapmakerWorker.isCancelled())
		            return;

		        newDemInfo = new DEMInfo();
		        newDemInfo.name = demName;
		        newDemInfo.demfilename = new File(getDEMOutputBasePath().toFile() + File.separator + demName + ".fits").getAbsolutePath();
		        completionBlock.run();
			}
		});
	}

	@Override
	public DEMKey getDEMKey()
	{
		System.out.println("MapmakerRemoteDEMCreator: getDEMKey: new dem filename " + newDemInfo.demfilename);
		return new DEMKey(newDemInfo.demfilename, newDemInfo.name);
	}
}
