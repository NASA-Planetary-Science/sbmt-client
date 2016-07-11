package edu.jhuapl.near.lidar.test;

import java.nio.file.Path;

public interface LidarPointList
{
    public int getNumberOfPoints();
    public BasicLidarPoint getPoint(int i);
    public void clear();
    public void appendFromFile(Path inputFilePath);
}
