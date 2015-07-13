package edu.jhuapl.near.gui.joglrendering;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkObject;
import vtk.vtkRenderWindow;
import vtk.rendering.vtkAbstractComponent;
import vtk.rendering.vtkInteractorForwarder;

/**
 * Provide JOGL based rendering component for VTK
 *
 * @author Sebastien Jourdain - sebastien.jourdain@kitware.com
 */
public class vtksbmtAbstractJoglComponent<T extends java.awt.Component> extends vtkAbstractComponent<T> {

  protected T uiComponent;
  protected boolean isWindowCreated;
  protected GLEventListener glEventListener;
  protected vtkGenericOpenGLRenderWindow glRenderWindow;


  public vtksbmtAbstractJoglComponent(vtkRenderWindow renderWindowToUse, T glContainer) {
    super(renderWindowToUse);
    this.isWindowCreated = false;
    this.uiComponent = glContainer;
    this.glRenderWindow = (vtkGenericOpenGLRenderWindow) renderWindowToUse;
    this.glRenderWindow.SetIsDirect(1);
    this.glRenderWindow.SetSupportsOpenGL(1);
    this.glRenderWindow.SetIsCurrent(true);

    // Create the JOGL Event Listener
    this.glEventListener = new GLEventListener() {
      public void init(GLAutoDrawable drawable) {
        vtksbmtAbstractJoglComponent.this.isWindowCreated = true;

        // Make sure the JOGL Context is current
        GLContext ctx = drawable.getContext();
        if (!ctx.isCurrent()) {
          ctx.makeCurrent();
        }

        // Init VTK OpenGL RenderWindow
        vtksbmtAbstractJoglComponent.this.glRenderWindow.SetMapped(1);
        vtksbmtAbstractJoglComponent.this.glRenderWindow.SetPosition(0, 0);
        vtksbmtAbstractJoglComponent.this.setSize(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        vtksbmtAbstractJoglComponent.this.glRenderWindow.OpenGLInit();
      }

      public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        vtksbmtAbstractJoglComponent.this.setSize(width, height);
      }

      public void display(GLAutoDrawable drawable) {
        vtksbmtAbstractJoglComponent.this.inRenderCall = true;
        vtksbmtAbstractJoglComponent.this.glRenderWindow.Render();
        vtksbmtAbstractJoglComponent.this.inRenderCall = false;
      }

      public void dispose(GLAutoDrawable drawable) {
        vtksbmtAbstractJoglComponent.this.Delete();
        vtkObject.JAVA_OBJECT_MANAGER.gc(false);
      }
    };

    // Bind the interactor forwarder
    vtkInteractorForwarder forwarder = this.getInteractorForwarder();
    this.uiComponent.addMouseListener(forwarder);
    this.uiComponent.addMouseMotionListener(forwarder);
    this.uiComponent.addMouseWheelListener(forwarder);
    this.uiComponent.addKeyListener(forwarder);

    // Make sure when VTK internaly request a Render, the Render get
    // properly triggered
    renderWindowToUse.AddObserver("WindowFrameEvent", this, "Render");
    renderWindowToUse.GetInteractor().AddObserver("RenderEvent", this, "Render");
    renderWindowToUse.GetInteractor().SetEnableRender(false);
  }

  public T getComponent() {
    return this.uiComponent;
  }

  /**
   * Render the internal component
   */
  public void Render() {
    // Make sure we can render
    if (!inRenderCall) {
      this.uiComponent.repaint();
    }
  }

  /**
   * @return true if the graphical component has been properly set and
   * operation can be performed on it.
   */
  public boolean isWindowSet() {
    return this.isWindowCreated;
  }
}
