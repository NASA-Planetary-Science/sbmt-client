package edu.jhuapl.near.gui.dem;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.jhuapl.near.util.Bigmap;
import edu.jhuapl.saavtk.gui.FileDownloadSwingWorker;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

public class BigmapSwingWorker extends FileDownloadSwingWorker
{
    boolean regionSpecifiedWithLatLonScale = false;
    private String name;
    private double[] centerPoint;
    private double radius;
    private File tempFolder;
    private File outputFolder;
    private File mapletFile;
    private int halfSize;
    private double pixelScale;
    private double latitude;
    private double longitude;
    private boolean grotesque;
    private PolyhedralModel smallBodyModel;

    public BigmapSwingWorker(Component c, String title, String filename)
    {
        super(c, title, filename);
    }


    public void setName(String name)
    {
        // Note: Bigmap requires exactly 6 char long names, create 6 char long pad
        String pad = "______";

        // Take only the first 6 chars, no spaces allowed
        this.name = (name + pad).substring(0, 6).replace(" ",  "_");
    }

    public void setGrotesque(boolean grotesque)
    {
        this.grotesque = grotesque;
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

    public void setTempFolder(File tempFolder)
    {
        this.tempFolder = tempFolder;
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

    public void setSmallBodyModel(PolyhedralModel smallBodyModel)
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
        setCancelButtonEnabled(true);
        setProgress(1);

        Process bigmapProcess = null;
        BufferedReader bigmapOutputReader = null;
        BufferedReader bigmapErrorReader = null;

        try
        {
            File file = FileCache.getFileFromServer(this.getFileDownloaded());
            String bigmapRootDir = file.getParent() + File.separator + "bigmap";

            Bigmap bigmap = new Bigmap(bigmapRootDir, grotesque);
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
            bigmap.setTempFolder(tempFolder);
            bigmap.setOutputFolder(outputFolder);
            bigmap.setSmallBodyModel(smallBodyModel);

            // Create the bigmap process
            bigmapProcess = bigmap.runBigmap();

            // Get the output and error streams, this must be done because bigmap is very
            // verbose and it will fill up the buffers of java.lang.Process quickly,
            // which causes the process to block
            bigmapOutputReader = new BufferedReader(new InputStreamReader(bigmapProcess.getInputStream()));
            bigmapErrorReader = new BufferedReader(new InputStreamReader(bigmapProcess.getErrorStream()));
            String line;

            // Wait for bigmaps to finish
            while (true)
            {
                if (isCancelled())
                    break;

                try
                {
                    // Read java.lang.Process output and error streams to prevent it from blocking
                    // output results to stdout and stderr
                    while((line = bigmapOutputReader.readLine()) != null)
                    {
                        System.out.println(line);
                    }
                    while((line = bigmapErrorReader.readLine()) != null)
                    {
                        System.err.println(line);
                    }

                    // Calling exitValue() throws IllegalThreadStateException if
                    // process is still alive.  This is how we check if process
                    // has finished or not.
                    bigmapProcess.exitValue();
                    break;
                }
                catch (IllegalThreadStateException e)
                {
                    //e.printStackTrace();
                    // do nothing. We'll get here if the process is still running
                }

                Thread.sleep(333);
            }

            // Begin post processing
            setLabelText("<html>Postprocessing Results<br> </html>");
            bigmap.convertMapletToFitsAndSaveInOutputFolder(false);
            mapletFile = bigmap.getMapletFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Close the streams
            if(bigmapOutputReader != null)
            {
                try
                {
                    bigmapOutputReader.close();
                }
                catch(IOException e)
                {
                    // Do nothing
                }
            }
            if(bigmapErrorReader != null)
            {
                try
                {
                    bigmapErrorReader.close();
                }
                catch(IOException e)
                {
                    // Do nothing
                }
            }
        }

        if (bigmapProcess != null && isCancelled())
        {
            bigmapProcess.destroy();
        }

        setProgress(100);

        return null;
    }
}
