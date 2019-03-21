package edu.jhuapl.sbmt.dtm.service;

import edu.jhuapl.saavtk.model.PointInRegionChecker;
import edu.jhuapl.sbmt.dtm.model.DEM;

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
