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
    private String name;
    private double[] centerPoint;
    private double radius;
    private File outputFolder;
    private File cubeFile;
    private File lblFile;
    private int halfSize;

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


    public File getCubeFile()
    {
        return cubeFile;
    }

    public File getLabelFile()
    {
        return lblFile;
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
            LatLon ll = MathUtil.reclat(centerPoint);
            mapmaker.setLatitude(ll.lat);
            mapmaker.setLongitude(ll.lon);
            mapmaker.setHalfSize(halfSize);
            mapmaker.setPixelSize(1000.0 * 1.5 * radius / (double)halfSize);
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

            mapmaker.copyGeneratedFilesToOutputFolder();
            cubeFile = mapmaker.getCubeFile();
            lblFile = mapmaker.getLabelFile();
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
