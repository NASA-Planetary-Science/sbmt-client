package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Mapmaker;
import edu.jhuapl.near.util.MathUtil;

public class MapmakerSwingWorker extends FileDownloadSwingWorker
{
    boolean regionSpecifiedWithLatLonScale = false;
    private String name;
    private double[] centerPoint;
    private double radius;
    private File outputFolder;
    private File mapletFile;
    private int halfSize;
    private double pixelScale;
    private double latitude;
    private double longitude;

    public MapmakerSwingWorker(Component c, String title, String filename)
    {
        super(c, title, filename);
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public void setCenterPoint(double[] centerPoint)
    {
        this.centerPoint = centerPoint;
    }


    public void setRadius(double radius)
    {
        this.radius = radius;
    }

    public void setHalfSize(int halfSize)
    {
        this.halfSize = halfSize;
    }


    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }

    public void setPixelScale(double pixelScale)
    {
        this.pixelScale = pixelScale;
    }


    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }


    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }


    public File getMapletFile()
    {
        return mapletFile;
    }


    public void setRegionSpecifiedWithLatLonScale(
            boolean regionSpecifiedWithLatLonScale)
    {
        this.regionSpecifiedWithLatLonScale = regionSpecifiedWithLatLonScale;
    }


    @Override
    protected Void doInBackground()
    {
        super.doInBackground();

        if (isCancelled())
        {
            return null;
        }

        setLabelText("<html>Running Mapmaker<br> </html>");
        setIndeterminate(true);
        setCancelButtonEnabled(false);
        setProgress(1);

        Process mapmakerProcess = null;

        try
        {
            File file = FileCache.getFileFromServer(this.getFileDownloaded());
            String mapmakerRootDir = file.getParent() + File.separator + "mapmaker";

            Mapmaker mapmaker = new Mapmaker(mapmakerRootDir);
            mapmaker.setName(name);
            if (regionSpecifiedWithLatLonScale)
            {
                mapmaker.setLatitude(latitude);
                mapmaker.setLongitude(longitude);
                mapmaker.setPixelSize(pixelScale);
            }
            else
            {
                LatLon ll = MathUtil.reclat(centerPoint).toDegrees();
                mapmaker.setLatitude(ll.lat);
                mapmaker.setLongitude(ll.lon);
                mapmaker.setPixelSize(1000.0 * 1.5 * radius / (double)halfSize);
            }
            mapmaker.setHalfSize(halfSize);
            mapmaker.setOutputFolder(outputFolder);

            mapmakerProcess = mapmaker.runMapmaker();

            while (true)
            {
                if (isCancelled())
                    break;

                try
                {
                    mapmakerProcess.exitValue();
                    break;
                }
                catch (IllegalThreadStateException e)
                {
                    //e.printStackTrace();
                    // do nothing. We'll get here if the process is still running
                }

                Thread.sleep(333);
            }

            mapmaker.convertCubeToFitsAndSaveInOutputFolder(false);
            mapletFile = mapmaker.getMapletFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            //e.printStackTrace();
        }

        if (mapmakerProcess != null && isCancelled())
        {
            mapmakerProcess.destroy();
        }

        setProgress(100);

        return null;
    }
}
