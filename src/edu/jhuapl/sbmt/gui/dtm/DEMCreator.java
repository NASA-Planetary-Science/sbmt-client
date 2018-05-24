package edu.jhuapl.sbmt.gui.dtm;

import java.nio.file.Path;

import edu.jhuapl.saavtk2.task.Task;

public interface DEMCreator
{
    public String getExecutableDisplayName();
    public Path getExecutablePathOnServer();
    public boolean needToDownloadExecutable();
    public Path getDEMOutputBasePath();
    public Task getCreationTask(String name, double latDeg, double lonDeg, double pixScaleMeters, int pixHalfSize);
    public Task getCreationTask(String name, double[] center, double radius, int pixHalfSize);
}
