package edu.jhuapl.near.lidar.test;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class LidarPoint implements Comparable<LidarPoint>
{
    Vector3D scpos;
    Vector3D tgpos;
    Double time;
    Double intensity;

    public LidarPoint(Vector3D scpos, Vector3D tgpos, double time, double intensity)
    {
        this.scpos=scpos;
        this.tgpos=tgpos;
        this.time=time;
        this.intensity=intensity;
    }

    public LidarPoint(double[] tgpos, double[] scpos, double time, double intensity)
    {
        this.scpos=new Vector3D(scpos);
        this.tgpos=new Vector3D(tgpos);
        this.time=time;
        this.intensity=new Double(intensity);
    }

    public LidarPoint(double[] tgpos, double[] scpos, double time)
    {
        this.scpos=new Vector3D(scpos);
        this.tgpos=new Vector3D(tgpos);
        this.time=time;
        this.intensity=new Double(0);
    }

    public Vector3D getTargetPosition()
    {
        return tgpos;
    }

    public Vector3D getSourcePosition()
    {
        return scpos;
    }

    public double[] getTargetPositionAsArray()
    {
        return new double[]{tgpos.getX(),tgpos.getY(),tgpos.getZ()};
    }

    public double[] getSourcePositionAsArray()
    {
        return new double[]{scpos.getX(),scpos.getY(),scpos.getZ()};
    }

    public Double getIntensityReceived()
    {
        return intensity;
    }

    public Double getTime()
    {
        return time;
    }

    @Override
    public int compareTo(LidarPoint o)
    {
        return time.compareTo(o.getTime());
    }
}
