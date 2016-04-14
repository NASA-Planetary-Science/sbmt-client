package edu.jhuapl.near.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Bigmap;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;

public class BigmapSwingWorker extends FileDownloadSwingWorker
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
    private SmallBodyModel smallBodyModel;

    public BigmapSwingWorker(Component c, String title, String filename)
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

    public void setSmallBodyModel(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
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

        setLabelText("<html>Running Bigmap<br> </html>");
        setIndeterminate(true);
        setCancelButtonEnabled(false);
        setProgress(1);

        Process bigmapProcess = null;

        try
        {
            File file = FileCache.getFileFromServer(this.getFileDownloaded());
            String bigmapRootDir = file.getParent() + File.separator + "bigmap";

            Bigmap bigmap = new Bigmap(bigmapRootDir);
            bigmap.setName(name);
            if (regionSpecifiedWithLatLonScale)
            {
                bigmap.setLatitude(latitude);
                bigmap.setLongitude(longitude);
                bigmap.setPixelSize(pixelScale);
            }
            else
            {
                LatLon ll = MathUtil.reclat(centerPoint).toDegrees();
                bigmap.setLatitude(ll.lat);
                bigmap.setLongitude(ll.lon);
                bigmap.setPixelSize(1000.0 * 1.5 * radius / (double)halfSize);
            }
            bigmap.setHalfSize(halfSize);
            bigmap.setOutputFolder(outputFolder);
            bigmap.setSmallBodyModel(smallBodyModel);

            System.out.println("twupy1: Commenting out call to bigmaps for testing purposes");
            /*bigmapProcess = bigmap.runBigmap();

            while (true)
            {
                if (isCancelled())
                    break;

                try
                {
                    bigmapProcess.exitValue();
                    break;
                }
                catch (IllegalThreadStateException e)
                {
                    //e.printStackTrace();
                    // do nothing. We'll get here if the process is still running
                }

                Thread.sleep(333);
            }*/

            bigmap.convertMapletToFitsAndSaveInOutputFolder(false);
            mapletFile = bigmap.getMapletFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        /*catch (InterruptedException e)
        {
            //e.printStackTrace();
        }*/

        if (bigmapProcess != null && isCancelled())
        {
            bigmapProcess.destroy();
        }

        setProgress(100);

        return null;
    }
}
