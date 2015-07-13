package edu.jhuapl.near.gui.joglrendering;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkRenderWindow;

public class vtksbmtJoglCanvasComponent extends vtksbmtAbstractJoglComponent<GLCanvas> {

  public vtksbmtJoglCanvasComponent() {
    this(new vtkGenericOpenGLRenderWindow());
  }

  public vtksbmtJoglCanvasComponent(vtkRenderWindow renderWindow) {
    this(renderWindow, new GLCapabilities(GLProfile.getDefault()));
  }

  public vtksbmtJoglCanvasComponent(vtkRenderWindow renderWindow, GLCapabilities capabilities) {
    super(renderWindow, new GLCanvas(capabilities));
    this.getComponent().addGLEventListener(this.glEventListener);
  }
}
