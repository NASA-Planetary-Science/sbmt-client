package edu.jhuapl.sbmt.gui.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbar;
import edu.jhuapl.saavtk2.image.CylindricalImage;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.util.LatLon;
import edu.jhuapl.saavtk2.util.MathUtil;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class ComplicatedCylindricalImageCubeExporter extends JDialog implements ChangeListener, ActionListener
{
    RenderToolbar toolbar=new RenderToolbar();
    RenderPanel renderObject=new RenderPanel(toolbar);

    vtkPolyData footprintPolyData;
    vtkPolyDataMapper footprintMapper;
    vtkActor footprintActor;
    List<PerspectiveImage> pimages;

    CylindricalProjection projection;
    vtkPolyData projectionPolyData;
    vtkPolyDataMapper projectionMapper;
    vtkActor projectionActor;

    JPanel sliderPanel = new JPanel();
    JSlider midLatSlider = new JSlider(-90, 90);
    JSlider midLonSlider = new JSlider(0, 360);
    JSlider deltaLatSlider = new JSlider(0, 180);
    JSlider deltaLonSlider = new JSlider(0, 360);

    JLabel midLatLabel=new JLabel("--", JLabel.CENTER);
    JLabel midLonLabel=new JLabel("--", JLabel.CENTER);
    JLabel deltaLatLabel=new JLabel("--", JLabel.CENTER);
    JLabel deltaLonLabel=new JLabel("--", JLabel.CENTER);

    // initial conditions
    double maxNorm;
    double maxDeltaLatDeg;
    double maxDeltaLonDeg;
    LatLon llMean;

    JButton viewFromFrustumButton=new JButton("Spacecraft View");
    JButton viewFromCylindricalButton=new JButton("Cylindrical View");
    JButton resetProjectionButton=new JButton("Reset Projection");

    JSlider bandSlider;
    vtkTexture imageTexture;

    public ComplicatedCylindricalImageCubeExporter(vtkPolyData footprintPolyData, List<PerspectiveImage> pimages)
    {
        bandSlider=new JSlider(0,pimages.size()-1,0);

        JPanel renderPanel=new JPanel(new BorderLayout());
        renderPanel.add(renderObject.getComponent());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(renderPanel, BorderLayout.CENTER);

        this.pimages=pimages;
        this.footprintPolyData=footprintPolyData;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        renderObject.setSize(800, 600);
        setVisible(true);

        footprintMapper=new vtkPolyDataMapper();
        footprintMapper.SetInputData(footprintPolyData);
        footprintMapper.Update();
        footprintActor=new vtkActor();
        footprintActor.SetMapper(footprintMapper);
        renderObject.getRenderer().AddActor(footprintActor);

        CylindricalProjection projection=new CylindricalProjection(-10, 30, 45, 90);
        projectionPolyData=CylindricalImage.createProjectionGeometry(footprintPolyData.GetLength()+new Vector3D(renderObject.getActiveCamera().GetPosition()).getNorm(), projection);
        projectionMapper=new vtkPolyDataMapper();
        projectionMapper.SetInputData(projectionPolyData);
        projectionMapper.Update();
        projectionActor=new vtkActor();
        projectionActor.SetMapper(projectionMapper);
        renderObject.getRenderer().AddActor(projectionActor);

        renderObject.resetCamera();

        for (int i=0; i<pimages.size(); i++)
        {
            List<vtkProp> props=pimages.get(i).getProps();
            for (int j=0; j<props.size(); j++)
                renderObject.getRenderer().AddActor(props.get(j));
            pimages.get(i).setShowFrustum(i==0);
        }

        JPanel midLatPanel = new JPanel(new GridLayout(2, 1));
        JPanel midLonPanel = new JPanel(new GridLayout(2, 1));
        JPanel deltaLatPanel = new JPanel(new GridLayout(2, 1));
        JPanel deltaLonPanel = new JPanel(new GridLayout(2, 1));

        JPanel midLatLabelPanel=new JPanel();
        JPanel midLonLabelPanel=new JPanel();
        JPanel deltaLatLabelPanel=new JPanel();
        JPanel deltaLonLabelPanel=new JPanel();

        midLatLabelPanel.add(new JLabel("Lat Center", JLabel.CENTER));
        midLatLabelPanel.add(midLatLabel);
        deltaLatLabelPanel.add(new JLabel("Lat Spread", JLabel.CENTER));
        deltaLatLabelPanel.add(deltaLatLabel);

        midLonLabelPanel.add(new JLabel("Lon Center", JLabel.CENTER));
        midLonLabelPanel.add(midLonLabel);
        deltaLonLabelPanel.add(new JLabel("Lon Spread", JLabel.CENTER));
        deltaLonLabelPanel.add(deltaLonLabel);

        midLatPanel.add(midLatSlider);
        deltaLatPanel.add(deltaLatSlider);
        midLonPanel.add(midLonSlider);
        deltaLonPanel.add(deltaLonSlider);

        midLatPanel.add(midLatLabelPanel);
        midLonPanel.add(midLonLabelPanel);
        deltaLatPanel.add(deltaLatLabelPanel);
        deltaLonPanel.add(deltaLonLabelPanel);

        midLatPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        midLonPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        deltaLatPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        deltaLonPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        midLatSlider.setPaintLabels(true);
        midLonSlider.setPaintLabels(true);
        deltaLatSlider.setPaintLabels(true);
        deltaLonSlider.setPaintLabels(true);
        midLatSlider.setPaintTicks(true);
        midLonSlider.setPaintTicks(true);
        deltaLatSlider.setPaintTicks(true);
        deltaLonSlider.setPaintTicks(true);
        midLatSlider.setMajorTickSpacing(30);
        midLonSlider.setMajorTickSpacing(90);
        deltaLatSlider.setMajorTickSpacing(30);
        deltaLonSlider.setMajorTickSpacing(90);

        JPanel buttonPanel=new JPanel();
        buttonPanel.add(viewFromFrustumButton);
        buttonPanel.add(viewFromCylindricalButton);
        JPanel buttonPanel2=new JPanel();
        buttonPanel2.add(resetProjectionButton);
        buttonPanel2.add(bandSlider);

        JPanel southPanel = new JPanel(new GridLayout(2, 3));

        southPanel.add(midLatPanel);
        southPanel.add(deltaLatPanel);
        southPanel.add(buttonPanel);

        southPanel.add(midLonPanel);
        southPanel.add(deltaLonPanel);
        southPanel.add(buttonPanel2);

        renderPanel.add(southPanel, BorderLayout.SOUTH);

        midLatSlider.addChangeListener(this);
        midLonSlider.addChangeListener(this);
        deltaLatSlider.addChangeListener(this);
        deltaLonSlider.addChangeListener(this);

        viewFromCylindricalButton.addActionListener(this);
        viewFromFrustumButton.addActionListener(this);
        resetProjectionButton.addActionListener(this);
        bandSlider.addChangeListener(this);

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

        midLatSlider.setValue((int)llMean.lat);
        midLonSlider.setValue((int)llMean.lon);
        deltaLatSlider.setValue((int)maxDeltaLatDeg);
        deltaLonSlider.setValue((int)maxDeltaLatDeg);

    }

    protected void regenerateProjectionGeometry() {
        double minLat = Math.max(-90, midLatSlider.getValue() - deltaLatSlider.getValue() / 2.);
        double maxLat = Math.min(90, midLatSlider.getValue() + deltaLatSlider.getValue() / 2.);
        double minLon = midLonSlider.getValue() - deltaLonSlider.getValue() / 2.;
        double maxLon = midLonSlider.getValue() + deltaLonSlider.getValue() / 2.;

        projection = new CylindricalProjection(minLat, maxLat, minLon, maxLon);
        projectionPolyData = CylindricalImage.createProjectionGeometry(1.5*maxNorm, projection);
        projectionMapper.SetInputData(projectionPolyData);
        projectionMapper.Update();
        projectionActor.GetProperty().SetRepresentationToWireframe();
        projectionActor.GetProperty().SetEdgeColor(0.5, 0, 0.75);

        String fmt = "%.0f";
        String degSymbol = String.valueOf(Character.toChars(0x00B0));

        midLatLabel.setText(String.format(fmt,(double)midLatSlider.getValue())+degSymbol);
        midLonLabel.setText(String.format(fmt,(double)midLonSlider.getValue())+degSymbol);
        deltaLatLabel.setText(String.format(fmt,(double)deltaLatSlider.getValue())+degSymbol);
        deltaLonLabel.setText(String.format(fmt,(double)deltaLonSlider.getValue())+degSymbol);

    }

    public void showBand(int band)
    {
        imageTexture = new vtkTexture();
        imageTexture.InterpolateOn();
        imageTexture.RepeatOff();
        imageTexture.EdgeClampOn();
        imageTexture.SetInputData(pimages.get(0).getRawImage());
        footprintActor.SetTexture(imageTexture);

        pimages.get(0).setCurrentSlice(band);
        pimages.get(0).setDisplayedImageRange(null);
        pimages.get(0).loadFootprint();
        footprintPolyData=pimages.get(0).getShiftedFootprint();
        footprintMapper.SetInputData(footprintPolyData);
        footprintMapper.Update();

        renderObject.Render();


/*        vtkXMLImageDataWriter writer=new vtkXMLImageDataWriter();
        writer.SetInputData(pimages.get(band).getRawImage());
        writer.SetFileName("/Users/zimmemi1/Desktop/test.vti");
        writer.SetDataModeToBinary();
        writer.Write();*/
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == midLatSlider || e.getSource() == midLonSlider || e.getSource() == deltaLatSlider
                || e.getSource() == deltaLonSlider) {
            regenerateProjectionGeometry();
            renderObject.Render();
        }
        else if (e.getSource()==bandSlider)
        {
            showBand(bandSlider.getValue());
        }

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource()==viewFromFrustumButton)
        {
            double[] pos=new double[3];
            double[] up=new double[3];
            double[] look=new double[3];
            pimages.get(0).getCameraOrientation(pos, look, up);
            renderObject.getActiveCamera().SetPosition(pos);
            renderObject.getActiveCamera().SetFocalPoint(look);
            renderObject.getActiveCamera().SetViewUp(up);
            renderObject.Render();
        }
        else if (e.getSource()==viewFromCylindricalButton)
        {
            double[] pos=projection.getMidPointUnit().scalarMultiply(maxNorm).toArray();    // look from far point on boresight vector
            double[] look=projection.getRayOrigin().toArray();                              // back toward origin of cylindrical projection
            double[] up=projection.getUpperRightUnit().subtract(projection.getLowerRightUnit()).toArray();  // with specified up direction
            renderObject.getActiveCamera().SetPosition(pos);
            renderObject.getActiveCamera().SetFocalPoint(look);
            renderObject.getActiveCamera().SetViewUp(up);
            renderObject.Render();

        }
        else if (e.getSource()==resetProjectionButton)
        {
            midLatSlider.setValue((int)llMean.lat);
            midLonSlider.setValue((int)llMean.lon);
            deltaLatSlider.setValue((int)maxDeltaLatDeg);
            deltaLonSlider.setValue((int)maxDeltaLatDeg);
        }
    }
}
