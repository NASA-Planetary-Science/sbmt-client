package edu.jhuapl.sbmt.dtm.service.demCreators;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk2.task.BasicTask;
import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.saavtk2.task.TaskFinishedEvent;
import edu.jhuapl.saavtk2.task.TaskStartedEvent;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.deprecated.CustomDEMPanel.DEMInfo;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;
import edu.jhuapl.sbmt.dtm.service.mapmakers.MapmakerRemoteSwingWorker;

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
            fire(new TaskFinishedEvent(this));
        }

        protected void processAndWait(Runnable wrapper)
        {
        	wrapper.run();
        }

        protected DEMKey postProcessAndCreate(Runnable wrapper)
        {
        	return null;
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

		        mapmakerWorker.setDatadir(smallBodyConfig.rootDirOnServer +  "/DATA");
		        mapmakerWorker.setMapoutdir(smallBodyConfig.rootDirOnServer + "/MAPFILES");
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

		        mapmakerWorker.setDatadir(smallBodyConfig.rootDirOnServer +  "/DATA");
		        mapmakerWorker.setMapoutdir(smallBodyConfig.rootDirOnServer + "/MAPFILES");
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
		return new DEMKey(newDemInfo.demfilename, newDemInfo.name);
	}
}
