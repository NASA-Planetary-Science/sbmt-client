package edu.jhuapl.near.util;

public interface BackplanesFile
{
    public void write(float[] data, String outputFile, int imageWidth, int imageHeight, int nBackplanes) throws Exception;
}
