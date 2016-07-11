package edu.jhuapl.near.gui.joglrendering;

import java.awt.Component;

import vtk.vtkProp;

public class vtksbmtBasicCanvas implements vtksbmtCanvas
{
    protected vtkFixedJoglPanelComponent component;

    public vtksbmtBasicCanvas()
    {
        component=new vtkFixedJoglPanelComponent();
        component.getRenderWindowInteractor().RemoveObservers("CharEvent");  // remove default keyboard events
    }

    public Component getSwingComponent()
    {
        return component.getComponent();
    }

    public void addProp(vtkProp prop)
    {
        component.getRenderer().AddActor(prop);
    }

    public void removeProp(vtkProp prop)
    {
        component.getRenderer().RemoveActor(prop);
    }

    @Override
    public void suspendRendering()
    {
        component.getRenderWindowInteractor().EnableRenderOff();
    }

    @Override
    public void resumeRendering()
    {
        component.getRenderWindowInteractor().EnableRenderOn();
    }


}
