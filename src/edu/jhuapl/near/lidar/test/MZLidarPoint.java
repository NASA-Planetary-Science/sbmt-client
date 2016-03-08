package edu.jhuapl.near.lidar.test;

public class MZLidarPoint
{
    String timestamp;
    public double scx,scy,scz;
    public double tgx,tgy,tgz;

    public MZLidarPoint(String timestamp, double scx, double scy, double scz, double tgx, double tgy, double tgz)
    {
        this.timestamp=timestamp;
        this.scx=scx;
        this.scy=scy;
        this.scz=scz;
        this.tgx=tgx;
        this.tgy=tgy;
        this.tgz=tgz;
    }

}
