package edu.jhuapl.sbmt.gui.dtm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk2.event.BasicEventSource;
import edu.jhuapl.saavtk2.util.LatLon;
import edu.jhuapl.saavtk2.util.MathUtil;
import edu.jhuapl.sbmt.model.dem.DEMKey;
import edu.jhuapl.sbmt.util.MapmakerNativeWrapper;


public class MapmakerDEMCreator extends BasicEventSource implements DEMCreator
{

    Path exePathOnServer;
    Path demOutputBasePath;

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
        return FileCache.getFileInfoFromServer(getExecutablePathOnServer().toString()).isNeedToDownload();
    }

    @Override
    public DEMKey create(String name, double latDeg, double lonDeg,
            double pixScaleMeters, int pixHalfSize)
    {
        MapmakerNativeWrapper wrapper=createWrapperInstance();
        wrapper.setName(name);
        wrapper.setLatitude(latDeg);
        wrapper.setLongitude(lonDeg);
        wrapper.setPixelSize(pixScaleMeters);
        wrapper.setHalfSize(pixHalfSize);
        wrapper.setOutputFolder(getDEMOutputBasePath().toFile());
        processAndWait(wrapper);
        DEMKey key=postProcessAndCreate(wrapper);
        fire(new DEMCreatedEvent(this, key));
        return key;
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


    @Override
    public DEMKey create(String name, double[] center, double radius,
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
        processAndWait(wrapper);
        DEMKey key=postProcessAndCreate(wrapper);
        fire(new DEMCreatedEvent(this, key));
        return key;
    }


}
