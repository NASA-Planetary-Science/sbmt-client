package edu.jhuapl.near.model;

public class PointInDEMChecker extends PointInRegionChecker
{
    private DEM dem;
    private double minDistanceToBoundary;

    public PointInDEMChecker(DEM dem, double minDistanceToBoundary)
    {
        this.dem = dem;
        this.minDistanceToBoundary = minDistanceToBoundary;
    }

    @Override
    public boolean checkPointIsInRegion(double[] point)
    {
        return dem.isPointWithinDEM(point, minDistanceToBoundary);
    }
}
