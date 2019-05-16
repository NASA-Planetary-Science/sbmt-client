package edu.jhuapl.sbmt.gui.dtm.ui.creation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk2.event.BasicEventSource;
import edu.jhuapl.saavtk2.task.BasicTask;
import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.saavtk2.task.TaskFinishedEvent;
import edu.jhuapl.saavtk2.task.TaskProgressEvent;
import edu.jhuapl.saavtk2.task.TaskStartedEvent;
import edu.jhuapl.sbmt.model.dem.DEMKey;
import edu.jhuapl.sbmt.util.MapmakerNativeWrapper;


public class MapmakerDEMCreator extends BasicEventSource implements DEMCreator
{

    Path exePathOnServer;
    Path demOutputBasePath;

    static protected class DEMCreationTask extends BasicTask
    {

        MapmakerNativeWrapper wrapper;

        public DEMCreationTask(MapmakerNativeWrapper wrapper)
        {
            this.wrapper=wrapper;
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
            DEMKey key=postProcessAndCreate(wrapper);
            fire(new DEMCreatedEvent(this, key));
            fire(new TaskFinishedEvent(this));
        }

        protected void processAndWait(MapmakerNativeWrapper wrapper)
        {
            Process mapmakerProcess;
            try
            {
                mapmakerProcess = wrapper.runMapmaker();
                while (true)
                {
                    // if (isCancelled())
                    // break;

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
            }
            catch (IOException | InterruptedException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }

        protected DEMKey postProcessAndCreate(MapmakerNativeWrapper wrapper)
        {
            wrapper.convertCubeToFitsAndSaveInOutputFolder(false);
            return new DEMKey(wrapper.getMapletFile().getAbsolutePath(),FilenameUtils.getBaseName(wrapper.getMapletFile().toString()));
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
    	System.out.println("MapmakerDEMCreator: needToDownloadExecutable: looking for " + getExecutablePathOnServer().toString());
        return FileCache.isDownloadNeeded(getExecutablePathOnServer().toString());
    }

    @Override
    public Task getCreationTask(String name, double latDeg, double lonDeg,
            double pixScaleMeters, int pixHalfSize)
    {
        final MapmakerNativeWrapper wrapper=createWrapperInstance();
        wrapper.setName(name);
        wrapper.setLatitude(latDeg);
        wrapper.setLongitude(lonDeg);
        wrapper.setPixelSize(pixScaleMeters);
        wrapper.setHalfSize(pixHalfSize);
        wrapper.setOutputFolder(getDEMOutputBasePath().toFile());
        return new DEMCreationTask(wrapper);
    }


    @Override
    public Task getCreationTask(String name, double[] center, double radius,
            int pixHalfSize)
    {

        MapmakerNativeWrapper wrapper=createWrapperInstance();
        wrapper.setName(name);
        LatLon ll = MathUtil.reclat(center).toDegrees();
        wrapper.setLatitude(ll.lat);
        wrapper.setLongitude(ll.lon);
        wrapper.setPixelSize(1000.0 * 1.5 * radius / (double) pixHalfSize);
        wrapper.setHalfSize(pixHalfSize);
        wrapper.setOutputFolder(getDEMOutputBasePath().toFile());
        return new DEMCreationTask(wrapper);
    }

    protected MapmakerNativeWrapper createWrapperInstance()
    {
        File file = FileCache.getFileFromServer(getExecutablePathOnServer().toString());
        String mapmakerRootDir = file.toPath().resolve("mapmaker").toString();
        try
        {
            return new MapmakerNativeWrapper(mapmakerRootDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }




}
