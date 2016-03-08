package edu.jhuapl.near.lidar.test;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class OlaPoint
{
    Vector3D scpos;
    Vector3D tgpos;
    double time;
    double intensity;

    public OlaPoint(Vector3D scpos, Vector3D tgpos, double time, double intensity)
    {
        this.scpos=scpos;
        this.tgpos=tgpos;
        this.time=time;
        this.intensity=intensity;
    }
}
