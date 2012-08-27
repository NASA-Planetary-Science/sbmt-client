package edu.jhuapl.near.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GravityProgram
{
    private ProcessBuilder processBuilder;
    private double density;
    private double rotationRate;
    private double refPotential;
    private String shapeModelFile;
    private String trackFile;


    public GravityProgram()
    {
        File file = FileCache.getFileFromServer("/GRAVITY/gravity.zip", true);
        FileUtil.unzipFile(file);

        String execDir = file.getParent() + File.separator + "gravity";

        ArrayList<String> processCommand = new ArrayList<String>();

        processBuilder = new ProcessBuilder(processCommand);

        processBuilder.directory(new File(execDir));

        Map<String, String> env = processBuilder.environment();

        String processName = null;
        if (Configuration.isLinux())
        {
            if (System.getProperty("sun.arch.data.model").equals("64"))
                processName = execDir + File.separator + "gravity.linux64";
            else
                processName = execDir + File.separator + "gravity.linux32";

            env.put("LD_LIBRARY_PATH", execDir);
        }
        else if (Configuration.isMac())
        {
            processName = execDir + File.separator + "gravity.macosx";

            env.put("DYLD_LIBRARY_PATH", execDir);
        }
        else
        {
            processName = execDir + File.separator + "gravity.win32.exe";
        }

        new File(processName).setExecutable(true);
        processCommand.add(processName);
    }

    public Process runGravity() throws IOException, InterruptedException
    {
        List<String> processCommand = processBuilder.command();
        processCommand.add("-d");
        processCommand.add(String.valueOf(density));
        processCommand.add("-r");
        processCommand.add(String.valueOf(rotationRate));
        processCommand.add("--ref-potential");
        processCommand.add(String.valueOf(refPotential));
        processCommand.add("--file");
        processCommand.add(trackFile);
        processCommand.add("--columns");
        processCommand.add("1,2,3");
        processCommand.add("--cheng");
        processCommand.add(shapeModelFile);

        Process process = processBuilder.start();

        return process;
    }

    public double getDensity()
    {
        return density;
    }

    public void setDensity(double density)
    {
        this.density = density;
    }

    public double getRotationRate()
    {
        return rotationRate;
    }

    public void setRotationRate(double rotationRate)
    {
        this.rotationRate = rotationRate;
    }

    public String getShapeModelFile()
    {
        return shapeModelFile;
    }

    public void setShapeModelFile(String shapeModelFile)
    {
        this.shapeModelFile = shapeModelFile;
    }

    public String getTrackFile()
    {
        return trackFile;
    }

    public void setTrackFile(String trackFile)
    {
        this.trackFile = trackFile;
    }

    public double getRefPotential()
    {
        return refPotential;
    }

    public void setRefPotential(double refPotential)
    {
        this.refPotential = refPotential;
    }

    public String getPotentialFile()
    {
        return processBuilder.directory() + File.separator +
                (new File(shapeModelFile)).getName() + "-potential.txt";
    }

    public String getAccelerationMagnitudeFile()
    {
        return processBuilder.directory() + File.separator +
                (new File(shapeModelFile)).getName() + "-acceleration-magnitude.txt";
    }

    public String getElevationFile()
    {
        return processBuilder.directory() + File.separator +
                (new File(shapeModelFile)).getName() + "-elevation.txt";
    }
}
