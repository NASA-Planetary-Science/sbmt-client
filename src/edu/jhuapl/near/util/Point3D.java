package edu.jhuapl.near.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Point3D
{
    public double[] xyz = new double[3];

    public Point3D(double[] pt)
    {
        xyz[0] = pt[0];
        xyz[1] = pt[1];
        xyz[2] = pt[2];
    }

    public String toString()
    {
        return "(" + xyz[0] + "," + xyz[1] + "," + xyz[2] + ")";
    }

    public double distanceTo(Point3D pt)
    {
        return MathUtil.distanceBetween(xyz, pt.xyz);
    }

    static public double distanceBetween(Point3D pt1, Point3D pt2)
    {
        return MathUtil.distanceBetween(pt1.xyz, pt2.xyz);
    }

    static public void savePointArray(ArrayList<Point3D> pointArray, File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);
        for (Point3D p : pointArray)
        {
            out.write(p.xyz[0] + " " +
                    p.xyz[1] + " " +
                    p.xyz[2] + "\n");
        }
        out.close();
    }
}
