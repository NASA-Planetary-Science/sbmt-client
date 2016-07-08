package edu.jhuapl.near.gui.joglrendering;

import java.awt.Component;

import vtk.vtkProp;

public interface vtksbmtCanvas
{
    public Component getSwingComponent();
    public void addProp(vtkProp prop);
    public void removeProp(vtkProp prop);
    public void suspendRendering();
    public void resumeRendering();
}
