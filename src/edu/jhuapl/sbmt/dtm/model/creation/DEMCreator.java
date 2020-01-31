package edu.jhuapl.sbmt.dtm.model.creation;

import java.nio.file.Path;

import edu.jhuapl.saavtk2.task.Task;
import edu.jhuapl.sbmt.dtm.model.DEMKey;

public interface DEMCreator
{
    public String getExecutableDisplayName();
    public Path getExecutablePathOnServer();
    public boolean needToDownloadExecutable();
    public Path getDEMOutputBasePath();
    public DEMKey getDEMKey();
    public Task getCreationTask(String name, double latDeg, double lonDeg, double pixScaleMeters, int pixHalfSize);
    public Task getCreationTask(String name, double[] center, double radius, int pixHalfSize);
    public void setCompletionBlock(Runnable completionBlock);
}
