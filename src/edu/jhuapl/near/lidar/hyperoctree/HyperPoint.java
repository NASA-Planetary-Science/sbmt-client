package edu.jhuapl.near.lidar.hyperoctree;

public interface HyperPoint extends Dimensioned
{
    public double getCoordinate(int i);
    public double[] get();
}
