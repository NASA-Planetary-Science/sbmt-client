package edu.jhuapl.sbmt.gui.dtm;

import java.nio.file.Path;

import edu.jhuapl.saavtk2.event.EventSource;
import edu.jhuapl.sbmt.model.dem.DEMKey;

public interface DEMCreator extends EventSource
{
    public String getExecutableDisplayName();
    public Path getExecutablePathOnServer();
    public boolean needToDownloadExecutable();
    public Path getDEMOutputBasePath();
    public DEMKey create(String name, double latDeg, double lonDeg, double pixScaleMeters, int pixHalfSize);
    public DEMKey create(String name, double[] center, double radius, int pixHalfSize);
}
