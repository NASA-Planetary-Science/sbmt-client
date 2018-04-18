package edu.jhuapl.sbmt.gui.image;

import java.awt.GridLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk2.util.LatLon;
import edu.jhuapl.saavtk2.util.MathUtil;
import edu.jhuapl.sbmt.model.image.ImageCube;

public class SimpleCylindricalImageCubeExporter extends JFileChooser
{
    ImageCube cube;

    JSpinner minLatSpinner=new JSpinner(new SpinnerNumberModel(0, -90, 90, 1));
    JSpinner maxLatSpinner=new JSpinner(new SpinnerNumberModel(0, -90, 90, 1));
    JSpinner minLonSpinner=new JSpinner(new SpinnerNumberModel(0, -360, 360, 1));
    JSpinner maxLonSpinner=new JSpinner(new SpinnerNumberModel(0, -360, 360, 1));
    JSpinner pixelsPerDegreeSpinner=new JSpinner(new SpinnerNumberModel(5,1,200,1));


    // initial conditions
    double maxNorm;
    double maxDeltaLatDeg;
    double maxDeltaLonDeg;
    LatLon llMean;

    public SimpleCylindricalImageCubeExporter(ImageCube cube)
    {
        this.cube=cube;


        JPanel spinnerPanel=new JPanel(new GridLayout(5,2));
        spinnerPanel.add(minLatSpinner);
        spinnerPanel.add(new JLabel("Min Lat"));
        spinnerPanel.add(maxLatSpinner);
        spinnerPanel.add(new JLabel("Max Lat"));
        spinnerPanel.add(minLonSpinner);
        spinnerPanel.add(new JLabel("Min Lon"));
        spinnerPanel.add(maxLonSpinner);
        spinnerPanel.add(new JLabel("Max Lon"));
        spinnerPanel.add(pixelsPerDegreeSpinner);
        spinnerPanel.add(new JLabel("Pixels/Degree"));
        this.add(spinnerPanel);

        vtkPolyData footprintPolyData=cube.getShiftedFootprint();

        maxNorm=0;
        Vector3D meanPosition=Vector3D.ZERO;
        for (int i=0; i<footprintPolyData.GetNumberOfPoints(); i++)
        {
            Vector3D pos=new Vector3D(footprintPolyData.GetPoint(i));
            meanPosition=meanPosition.add(pos);
            double norm=pos.getNorm();
            maxNorm=Math.max(norm, maxNorm);
        }
        meanPosition=meanPosition.scalarMultiply(footprintPolyData.GetNumberOfPoints());
        llMean=MathUtil.reclat(meanPosition.toArray());

        double maxDeltaLat=0;
        double maxDeltaLon=0;
        for (int i=0; i<footprintPolyData.GetNumberOfPoints(); i++)
        {
            Vector3D pos=new Vector3D(footprintPolyData.GetPoint(i));
            LatLon ll=MathUtil.reclat(pos.toArray());
            double dLat=Math.abs(ll.lat-llMean.lat);
            maxDeltaLat=Math.max(dLat,maxDeltaLat);
            double dLon=Math.abs(Vector3D.angle(new Vector3D(pos.getX(),pos.getY(),0), new Vector3D(meanPosition.getX(),meanPosition.getY(),0)));
            maxDeltaLon=Math.max(dLon, maxDeltaLon);
        }
        maxDeltaLatDeg=Math.toDegrees(maxDeltaLat)*2;
        maxDeltaLonDeg=Math.toDegrees(maxDeltaLon)*2;

        minLatSpinner.setValue(llMean.lat-maxDeltaLatDeg/2.);
        minLonSpinner.setValue(llMean.lon-maxDeltaLonDeg/2.);
        maxLatSpinner.setValue(llMean.lat+maxDeltaLatDeg/2.);
        maxLonSpinner.setValue(llMean.lon+maxDeltaLonDeg/2.);

    }

    public double getMaxLat()
    {
        return (Double)maxLatSpinner.getValue();
    }

    public double getMaxLon()
    {
        return (Double)maxLonSpinner.getValue();
    }

    public double getMinLat()
    {
        return (Double)minLatSpinner.getValue();
    }

    public double getMinLon()
    {
        return (Double)minLonSpinner.getValue();
    }

    public double getPixelsPerDegree()
    {
        return (int)pixelsPerDegreeSpinner.getValue();
    }

}
