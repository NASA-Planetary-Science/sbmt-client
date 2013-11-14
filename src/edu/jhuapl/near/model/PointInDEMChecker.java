package edu.jhuapl.near.model;

public class PointInDEMChecker extends PointInRegionChecker
{
    private DEMModel dem;

    public PointInDEMChecker(DEMModel dem)
    {
        this.dem = dem;
    }

    @Override
    public boolean checkPointIsInRegion(double[] point)
    {
        return dem.isPointWithinDEM(point);
    }
}
