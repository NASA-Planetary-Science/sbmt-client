package edu.jhuapl.near.model;

public class PointInDEMChecker extends PointInRegionChecker
{
    private DEMModel dem;
    private double minDistanceToBoundary;

    public PointInDEMChecker(DEMModel dem, double minDistanceToBoundary)
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
