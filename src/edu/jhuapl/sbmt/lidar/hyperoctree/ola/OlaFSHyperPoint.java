package edu.jhuapl.sbmt.lidar.hyperoctree.ola;

import java.io.DataInputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.FSLidarHyperPoint;

public class OlaFSHyperPoint extends FSLidarHyperPoint
{

    public OlaFSHyperPoint()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public OlaFSHyperPoint(DataInputStream stream) throws IOException
    {
        super(stream);
        // TODO Auto-generated constructor stub
    }

    public OlaFSHyperPoint(double tgx, double tgy, double tgz, double time,
            double scx, double scy, double scz, double intensity, int fileNum)
    {
        super(tgx, tgy, tgz, time, scx, scy, scz, intensity, fileNum);
        // TODO Auto-generated constructor stub
    }

}
