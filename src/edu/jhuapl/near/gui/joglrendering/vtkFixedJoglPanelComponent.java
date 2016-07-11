package edu.jhuapl.near.gui.joglrendering;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;

import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkRenderWindow;

public class vtkFixedJoglPanelComponent extends vtkFixedAbstractJoglComponent<GLJPanel>
{
	  public vtkFixedJoglPanelComponent() {
	    this(new vtkGenericOpenGLRenderWindow());
	  }

	  public vtkFixedJoglPanelComponent(vtkRenderWindow renderWindow) {
	    this(renderWindow, new GLCapabilities(GLProfile.getDefault()));
	  }

	  public vtkFixedJoglPanelComponent(vtkRenderWindow renderWindow, GLCapabilities capabilities) {
	    super(renderWindow, new GLJPanel(capabilities));
	    this.getComponent().addGLEventListener(this.glEventListener);
	  }


}
