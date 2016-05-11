package edu.jhuapl.near.gui.joglrendering;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkRenderWindow;

public class vtksbmtJoglCanvas extends vtksbmtAbstractJoglCanvas<GLCanvas> {

  public vtksbmtJoglCanvas() {
    this(new vtkGenericOpenGLRenderWindow());
  }

  public vtksbmtJoglCanvas(vtkRenderWindow renderWindow) {
    this(renderWindow, new GLCapabilities(GLProfile.getDefault()));
  }

  public vtksbmtJoglCanvas(vtkRenderWindow renderWindow, GLCapabilities capabilities) {
    super(renderWindow, new GLCanvas(capabilities));
    this.getComponent().addGLEventListener(this.glEventListener);
  }
}
