package edu.jhuapl.near.gui.joglrendering;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import vtk.vtkCamera;
import vtk.vtkProp;

public class vtksbmtStereoSBSCanvas implements vtksbmtCanvas
{
    JPanel panel=new JPanel(new GridLayout(1, 2));
    vtkFixedJoglPanelComponent lft=new vtkFixedJoglPanelComponent();
    vtkFixedJoglPanelComponent rgt=new vtkFixedJoglPanelComponent();

    public vtksbmtStereoSBSCanvas(boolean freeView)
    {
        lft.getRenderWindowInteractor().RemoveObservers("CharEvent");  // remove default keyboard events
        rgt.getRenderWindowInteractor().RemoveObservers("CharEvent");
        if (freeView)
        {
            panel.add(rgt.getComponent());
            panel.add(lft.getComponent());
        }
        else
        {
            panel.add(lft.getComponent());
            panel.add(rgt.getComponent());
        }
        lft.getRenderWindow().StereoCapableWindowOn();
        rgt.getRenderWindow().StereoCapableWindowOn();
        lft.getRenderWindow().StereoRenderOn();
        rgt.getRenderWindow().StereoRenderOn();
        lft.getRenderWindow().SetStereoTypeToLeft();
        rgt.getRenderWindow().SetStereoTypeToRight();
        lft.getActiveCamera().AddObserver("ModifiedEvent", this, "lftCameraModified");
        rgt.getActiveCamera().AddObserver("ModifiedEvent", this, "rgtCameraModified");
    }

    private void lftCameraModified()
    {
        syncCameras(lft.getActiveCamera(), rgt.getActiveCamera());
        rgt.Render();
    }

    private void rgtCameraModified()
    {
        syncCameras(rgt.getActiveCamera(), lft.getActiveCamera());
        lft.Render();
    }

    private void syncCameras(vtkCamera sourceCam, vtkCamera targetCam)
    {
        if (sourceCam==null || targetCam==null)
            return;
        targetCam.SetPosition(sourceCam.GetPosition());
        targetCam.SetFocalPoint(sourceCam.GetFocalPoint());
        targetCam.SetViewUp(sourceCam.GetViewUp());
        targetCam.SetViewAngle(sourceCam.GetViewAngle());
        targetCam.SetClippingRange(sourceCam.GetClippingRange());
    }

    @Override
    public Component getSwingComponent()
    {
        return panel;
    }

    @Override
    public void addProp(vtkProp prop)
    {
        lft.getRenderer().AddActor(prop);
        rgt.getRenderer().AddActor(prop);
        lft.resetCamera();
        rgt.resetCamera();
    }

    @Override
    public void removeProp(vtkProp prop)
    {
        lft.getRenderer().RemoveActor(prop);
        rgt.getRenderer().RemoveActor(prop);
    }
}
