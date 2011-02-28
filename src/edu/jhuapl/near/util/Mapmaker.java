package edu.jhuapl.near.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;


public class Mapmaker
{
    private ProcessBuilder processBuilder;
    private String mapmakerRootDir;
    private String name;
    private double latitude;
    private double longitude;
    private int halfSize = 513;
    private double pixelSize;
    private File outputFolder;
    private File cubeFile;
    private File lblFile;

    public Mapmaker(String mapmakerRootDir) throws IOException
    {
        this.mapmakerRootDir = mapmakerRootDir;

        String execDir = mapmakerRootDir + File.separator + "EXECUTABLES";

        ArrayList<String> processCommand = new ArrayList<String>();

        processBuilder = new ProcessBuilder(processCommand);

        processBuilder.directory(new File(mapmakerRootDir));

        Map<String, String> env = processBuilder.environment();

        String processName = null;
        if (Configuration.isLinux())
        {
            if (System.getProperty("sun.arch.data.model").equals("64"))
                processName = execDir + File.separator + "MAPMAKERO.linux64";
            else
                processName = execDir + File.separator + "MAPMAKERO.linux32";

            env.put("LD_LIBRARY_PATH", execDir);
        }
        else if (Configuration.isMac())
        {
            processName = execDir + File.separator + "MAPMAKERO.macosx";

            env.put("DYLD_LIBRARY_PATH", execDir);
        }
        else
        {
            processName = execDir + File.separator + "MAPMAKERO.win32.exe";
            //throw new IOException("Operating system not supported");
        }

        new File(processName).setExecutable(true);
        processCommand.add(processName);
    }

    public Process runMapmaker() throws IOException, InterruptedException
    {
        Process process = processBuilder.start();
        OutputStream stdin = process.getOutputStream();

        String arguments = name + "\n" + halfSize + " " + pixelSize + "\nL\n" + latitude + "," + longitude + "\nn\nn\nn\nn\nn\nn\n";
        stdin.write(arguments.getBytes());
        stdin.flush();
        stdin.close();

        return process;
    }

    public void copyGeneratedFilesToOutputFolder() throws IOException
    {
        // Copy output files to output folder
        File origCubeFile = new File(mapmakerRootDir + File.separator + "OUTPUT" + File.separator + name + ".cub");
        File origLblFile = new File(mapmakerRootDir + File.separator + "OUTPUT" + File.separator + name + ".lbl");

        cubeFile = new File(outputFolder + File.separator + name + ".cub");
        lblFile = new File(outputFolder + File.separator + name + ".lbl");

        FileUtil.copyFile(origCubeFile, cubeFile);
        FileUtil.copyFile(origLblFile, lblFile);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    /**
     * set the latitude in radians
     * @param latitude
     */
    public void setLatitude(double latitude)
    {
        this.latitude = latitude * 180.0 / Math.PI;
    }

    public double getLongitude()
    {
        return longitude;
    }

    /**
     * set the longitude in radians and as West Longitude (not east as is shown in the status bar)
     * @param longitude
     */
    public void setLongitude(double longitude)
    {
        this.longitude = longitude * 180.0 / Math.PI;
        this.longitude = 360.0 - this.longitude;
        if (this.longitude < 0.0)
            this.longitude += 360.0;
    }

    public int getHalfSize()
    {
        return halfSize;
    }

    public void setHalfSize(int halfSize)
    {
        this.halfSize = halfSize;
    }

    public double getPixelSize()
    {
        return pixelSize;
    }

    public void setPixelSize(double pixelSize)
    {
        this.pixelSize = pixelSize;
    }

    public File getCubeFile()
    {
        return cubeFile;
    }

    public File getLabelFile()
    {
        return lblFile;
    }

    public File getOutputFolder()
    {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder)
    {
        this.outputFolder = outputFolder;
    }

}
