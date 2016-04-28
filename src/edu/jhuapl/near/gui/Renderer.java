package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import vtk.vtkAxesActor;
import vtk.vtkBMPWriter;
import vtk.vtkCamera;
import vtk.vtkCaptionActor2D;
import vtk.vtkInteractorStyle;
import vtk.vtkInteractorStyleImage;
import vtk.vtkInteractorStyleJoystickCamera;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkJPEGWriter;
import vtk.vtkLight;
import vtk.vtkLightKit;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkPNGWriter;
import vtk.vtkPNMWriter;
import vtk.vtkPostScriptWriter;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkProperty;
import vtk.vtkRenderer;
import vtk.vtkTIFFWriter;
import vtk.vtkTextProperty;
import vtk.vtkWindowToImageFilter;

import edu.jhuapl.near.gui.joglrendering.StereoMirror;
import edu.jhuapl.near.gui.joglrendering.vtksbmtJoglCanvasComponent;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Preferences;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.SbmtLODActor;

public class Renderer extends JPanel implements
            PropertyChangeListener, ActionListener
{
    public enum LightingType
    {
        NONE,
        HEADLIGHT,
        LIGHT_KIT,
        FIXEDLIGHT
    }

    public enum InteractorStyleType
    {
        TRACKBALL_CAMERA,
        JOYSTICK_CAMERA
    }

    public enum AxisType
    {
        NONE,
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Y,
        NEGATIVE_Y,
        POSITIVE_Z,
        NEGATIVE_Z
    }

    public enum ProjectionType
    {
        PERSPECTIVE {
            @Override
            public String toString() {
                return "Perspective";
            }
        },
        ORTHOGRAPHIC {
            @Override
            public String toString() {
                return "Orthographic";
            }
        }
    }

    protected vtksbmtJoglCanvasComponent renWin;
    private ModelManager modelManager;
    private vtkInteractorStyleTrackballCamera trackballCameraInteractorStyle;
    private vtkInteractorStyleJoystickCamera joystickCameraInteractorStyle;
    private vtkInteractorStyle defaultInteractorStyle;
    private vtkAxesActor axes;
    private vtkOrientationMarkerWidget orientationWidget;
    private vtkLightKit lightKit;
    private vtkLight headlight;
    private vtkLight fixedLight;
    private LightingType currentLighting = LightingType.NONE;
    // We need a separate flag for this since we should modify interaction if
    // axes are enabled
    private boolean interactiveAxes = true;
    private double axesSize; // needed because java wrappers do not expose vtkOrientationMarkerWidget.GetViewport() function.
    public static boolean enableLODs = true; // This is temporary to show off the LOD feature, very soon we will replace this with an actual menu

    private JFrame stereoFrame;
    private StereoMirror stereoMirror;
    private boolean stereoOn=false;

    void localKeypressHandler()
    {
        char ch=renWin.getRenderWindowInteractor().GetKeyCode();
        if (ch=='S')
        {
            stereoOn=!stereoOn;
            if (stereoOn)
                generateStereoFrame();
            else
                destroyStereoFrame();
        }
        if (stereoOn && stereoFrame!=null)
        {
            System.out.println(ch);
            if (ch=='<')
                stereoMirror.decreaseEyeSeparation();
            else if (ch=='>')
                stereoMirror.increaseEyeSeparation();
        }
    }

    void generateStereoFrame()
    {
        final Dimension preferredSize=renWin.getComponent().getPreferredSize();
        stereoMirror=new StereoMirror(renWin,1e5);
        stereoMirror.getRenderWindow().StereoCapableWindowOn();
        stereoMirror.getRenderWindow().SetStereoTypeToSplitViewportHorizontal();
        stereoMirror.getRenderWindow().StereoRenderOn();
        //
        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                stereoFrame=new JFrame();
                stereoFrame.setSize(preferredSize);
                stereoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                stereoFrame.getContentPane().add(stereoMirror.getComponent());
                stereoFrame.setVisible(true);
                setProps(modelManager.getProps(), stereoMirror, stereoMirror.getRenderer());
            }
        });

    }

    void destroyStereoFrame()
    {
        stereoFrame.dispose();
    }

    public Renderer(final ModelManager modelManager)
    {
        setLayout(new BorderLayout());

        renWin = new vtksbmtJoglCanvasComponent();
        renWin.getRenderWindowInteractor().AddObserver("KeyPressEvent", this, "localKeypressHandler");

        this.modelManager = modelManager;

        modelManager.addPropertyChangeListener(this);

        trackballCameraInteractorStyle = new vtkInteractorStyleTrackballCamera();
        joystickCameraInteractorStyle = new vtkInteractorStyleJoystickCamera();

        defaultInteractorStyle = trackballCameraInteractorStyle;

        InteractorStyleType interactorStyleType = InteractorStyleType.valueOf(
                Preferences.getInstance().get(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.TRACKBALL_CAMERA.toString()));
        setDefaultInteractorStyleType(interactorStyleType);

        setMouseWheelMotionFactor(Preferences.getInstance().getAsDouble(Preferences.MOUSE_WHEEL_MOTION_FACTOR, 1.0));

        setBackgroundColor(Preferences.getInstance().getAsIntArray(Preferences.BACKGROUND_COLOR, new int[]{0, 0, 0}));

        headlight = renWin.getRenderer().MakeLight();
        headlight.SetLightTypeToHeadlight();
        headlight.SetConeAngle(180.0);

        fixedLight = renWin.getRenderer().MakeLight();
        fixedLight.SetLightTypeToSceneLight();
        fixedLight.PositionalOn();
        fixedLight.SetConeAngle(180.0);
        LatLon defaultPosition = new LatLon(
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LATITUDE, 90.0),
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LONGITUDE, 0.0),
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_DISTANCE, 1.0e8));
        setFixedLightPosition(defaultPosition);
        setLightIntensity(Preferences.getInstance().getAsDouble(Preferences.LIGHT_INTENSITY, 1.0));

        renWin.getRenderer().AutomaticLightCreationOff();
        lightKit = new vtkLightKit();
        lightKit.SetKeyToFillRatio(1.0);
        lightKit.SetKeyToHeadRatio(20.0);

        LightingType lightingType = LightingType.valueOf(
                Preferences.getInstance().get(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString()));
        setLighting(lightingType);

        add(renWin.getComponent(), BorderLayout.CENTER);

        axes = new vtkAxesActor();

        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        caption.GetTextActor().SetTextScaleModeToNone();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        textProperty.BoldOff();
        textProperty.ItalicOff();

        caption = axes.GetYAxisCaptionActor2D();
        caption.GetTextActor().SetTextScaleModeToNone();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.BoldOff();
        textProperty.ItalicOff();

        caption = axes.GetZAxisCaptionActor2D();
        caption.GetTextActor().SetTextScaleModeToNone();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.BoldOff();
        textProperty.ItalicOff();

        setXAxisColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_XAXIS_COLOR, new int[]{255, 0, 0}));
        setYAxisColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_YAXIS_COLOR, new int[]{0, 255, 0}));
        setZAxisColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_ZAXIS_COLOR, new int[]{255, 255, 0}));
        setAxesLabelFontSize((int)Preferences.getInstance().getAsLong(Preferences.AXES_FONT_SIZE, 12L));
        setAxesLabelFontColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_FONT_COLOR, new int[]{255, 255, 255}));
        setAxesLineWidth(Preferences.getInstance().getAsDouble(Preferences.AXES_LINE_WIDTH, 1.0));
        setAxesConeLength(Preferences.getInstance().getAsDouble(Preferences.AXES_CONE_LENGTH, 0.2));
        setAxesConeRadius(Preferences.getInstance().getAsDouble(Preferences.AXES_CONE_RADIUS, 0.4));

        orientationWidget = new vtkOrientationMarkerWidget();
        orientationWidget.SetOrientationMarker(axes);
        orientationWidget.SetInteractor(renWin.getRenderWindowInteractor());
        orientationWidget.SetTolerance(10);
        setAxesSize(Preferences.getInstance().getAsDouble(Preferences.AXES_SIZE, 0.2));

        // Setup observers for start/stop interaction events
        renWin.getRenderWindowInteractor().AddObserver("StartInteractionEvent", this, "onStartInteraction");
        renWin.getRenderWindowInteractor().AddObserver("EndInteractionEvent", this, "onEndInteraction");


        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setShowOrientationAxes(Preferences.getInstance().getAsBoolean(Preferences.SHOW_AXES, true));
                setOrientationAxesInteractive(Preferences.getInstance().getAsBoolean(Preferences.INTERACTIVE_AXES, true));
                setProps(modelManager.getProps());
                renWin.resetCamera();
                renWin.Render();
            }
        });
    }

    public void setProps(List<vtkProp> props)
    {
        setProps(props,renWin,renWin.getRenderer());
        if (stereoOn)
        {
            setProps(props,stereoMirror,stereoMirror.getRenderer());
            //setProps(props,stereoMirror,stereoMirror.getKludgeRenderer());
        }
    }


    public void setProps(List<vtkProp> props, vtksbmtJoglCanvasComponent renderWindow, vtkRenderer whichRenderer)
    {
        // Go through the props and if an prop is already in the renderer,
        // do nothing. If not, add it. If an prop not listed is
        // in the renderer, remove it from the renderer.

        // First remove the props not in the specified list that are currently rendered.
        vtkPropCollection propCollection = renderWindow.getRenderer().GetViewProps();
        int size = propCollection.GetNumberOfItems();
        HashSet<vtkProp> renderedProps = new HashSet<vtkProp>();
        for (int i=0; i<size; ++i)
            renderedProps.add((vtkProp)propCollection.GetItemAsObject(i));
        renderedProps.removeAll(props);
        if (!renderedProps.isEmpty())
        {
            renderWindow.getVTKLock().lock();
            for (vtkProp prop : renderedProps)
                whichRenderer.RemoveViewProp(prop);
            renderWindow.getVTKLock().unlock();
        }

        // Next add the new props.
        for (vtkProp prop : props)
        {
            if (whichRenderer.HasViewProp(prop) == 0)
                whichRenderer.AddViewProp(prop);
        }

        // If we are in 2D mode, then remove all props of models that
        // do not support 2D mode.
        if (modelManager.is2DMode())
        {
            propCollection = whichRenderer.GetViewProps();
            size = propCollection.GetNumberOfItems();
            for (int i=size-1; i>=0; --i)
            {
                vtkProp prop = (vtkProp)propCollection.GetItemAsObject(i);
                Model model = modelManager.getModel(prop);
                if (model != null && !model.supports2DMode())
                {
                    renderWindow.getVTKLock().lock();
                    whichRenderer.RemoveViewProp(prop);
                    renderWindow.getVTKLock().unlock();
                }
            }
        }
        //

        if (renderWindow.getRenderWindow().GetNeverRendered() > 0)
            return;
        renderWindow.Render();
    }

    public void onStartInteraction()
    {
        // LOD switching control for SbmtLODActor
        if(enableLODs && modelManager != null)
        {
            List<vtkProp> props = modelManager.getProps();
            for(vtkProp prop : props)
            {
                if(prop instanceof SbmtLODActor)
                {
                    ((SbmtLODActor)prop).selectMapper(Integer.MIN_VALUE);
                }
            }
        }
    }

    public void onEndInteraction()
    {
        // LOD switching control for SbmtLODActor
        if(enableLODs && modelManager != null)
        {
            List<vtkProp> props = modelManager.getProps();
            for(vtkProp prop : props)
            {
                if(prop instanceof SbmtLODActor)
                {
                    ((SbmtLODActor)prop).selectMapper(Integer.MAX_VALUE);
                }
            }
        }
    }

    /*
    public void addActor(vtkActor actor)
    {
        if (renWin.getRenderer().HasViewProp(actor) == 0)
        {
            renWin.getRenderer().AddActor(actor);
            renWin.Render();
        }
    }

    private void addActors(ArrayList<vtkActor> actors)
    {
        boolean actorWasAdded = false;
        for (vtkActor act : actors)
        {
            if (renWin.getRenderer().HasViewProp(act) == 0)
            {
                renWin.getRenderer().AddActor(act);
                actorWasAdded = true;
            }
        }

        if (actorWasAdded)
            render();
    }

    public void removeActor(vtkActor actor)
    {
        if (renWin.getRenderer().HasViewProp(actor) > 0)
        {
            renWin.getRenderer().RemoveActor(actor);
            renWin.Render();
        }
    }

    public void removeActors(ArrayList<vtkActor> actors)
    {
        boolean actorWasRemoved = false;
        for (vtkActor act : actors)
        {
            if (renWin.getRenderer().HasViewProp(act) > 0)
            {
                renWin.getRenderer().RemoveActor(act);
                actorWasRemoved = true;
            }
        }

        if (actorWasRemoved)
            renWin.Render();
    }

    public void updateAllActors()
    {
        renWin.Render();
    }
    */

    public void saveToFile()
    {
        File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "image.png", "png");
        saveToFile(file, renWin);
    }

    private BlockingQueue<CameraFrame> cameraFrameQueue;

    private File[] sixFiles = new File[6];

    AxisType[] sixAxes = {
            AxisType.POSITIVE_X, AxisType.NEGATIVE_X,
            AxisType.POSITIVE_Y, AxisType.NEGATIVE_Y,
            AxisType.POSITIVE_Z, AxisType.NEGATIVE_Z
    };


    public void save6ViewsToFile()
    {
        File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "", "png");
        String path = file.getAbsolutePath();
        String base = path.substring(0, path.lastIndexOf('.'));
        String ext = path.substring(path.lastIndexOf('.'));

        sixFiles[0] = new File(base + "+x" + ext);
        sixFiles[1] = new File(base + "-x" + ext);
        sixFiles[2] = new File(base + "+y" + ext);
        sixFiles[3] = new File(base + "-y" + ext);
        sixFiles[4] = new File(base + "+z" + ext);
        sixFiles[5] = new File(base + "-z" + ext);

        sixAxes[0] = AxisType.POSITIVE_X;
        sixAxes[1] = AxisType.NEGATIVE_X;
        sixAxes[2] = AxisType.POSITIVE_Y;
        sixAxes[3] = AxisType.NEGATIVE_Y;
        sixAxes[4] = AxisType.POSITIVE_Z;
        sixAxes[5] = AxisType.NEGATIVE_Z;

        // Check if one of the files already exist and if so, prompt user.
        for (File f : sixFiles)
        {
            if (f.exists())
            {
                int response = JOptionPane.showConfirmDialog (JOptionPane.getFrameForComponent(this),
                  "Overwrite file(s)?","Confirm Overwrite",
                   JOptionPane.OK_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE);

                if (response == JOptionPane.CANCEL_OPTION)
                    return;
                else
                {
                    break;
                }
            }
        }

        cameraFrameQueue = new LinkedBlockingQueue<CameraFrame>();

        for (int i=0; i<6; i++)
        {
            File f = sixFiles[i];
            AxisType at = sixAxes[i];
            CameraFrame frame = createCameraFrameInDirectionOfAxis(at, true, f, 1000);
            cameraFrameQueue.add(frame);
        }

        // start off the timer
        this.actionPerformed(null);

    }

    public void setCameraOrientationInDirectionOfAxis(AxisType axisType, boolean preserveCurrentDistance)
    {
        vtkRenderer ren = renWin.getRenderer();
        if (ren.VisibleActorCount() == 0) return;

        renWin.getVTKLock().lock();

        double[] bounds = modelManager.getSmallBodyModel().getBoundingBox().getBounds();
        double xSize = Math.abs(bounds[1] - bounds[0]);
        double ySize = Math.abs(bounds[3] - bounds[2]);
        double zSize = Math.abs(bounds[5] - bounds[4]);
        double maxSize = Math.max(Math.max(xSize, ySize), zSize);

        double cameraDistance = getCameraDistance();

        vtkCamera cam = ren.GetActiveCamera();
        cam.SetFocalPoint(0.0, 0.0, 0.0);

        if (axisType == AxisType.NEGATIVE_X)
        {
            double xpos = xSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            cam.SetPosition(xpos, 0.0, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.POSITIVE_X)
        {
            double xpos = -xSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            cam.SetPosition(xpos, 0.0, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.NEGATIVE_Y)
        {
            double ypos = ySize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            cam.SetPosition(0.0, ypos, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.POSITIVE_Y)
        {
            double ypos = -ySize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            cam.SetPosition(0.0, ypos, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.NEGATIVE_Z)
        {
            double zpos = zSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            cam.SetPosition(0.0, 0.0, zpos);
            cam.SetViewUp(0.0, 1.0, 0.0);
        }
        else if (axisType == AxisType.POSITIVE_Z)
        {
            double zpos = -zSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            cam.SetPosition(0.0, 0.0, zpos);
            cam.SetViewUp(0.0, 1.0, 0.0);
        }

        if (preserveCurrentDistance)
        {
            double[] pos = cam.GetPosition();

            MathUtil.unorm(pos, pos);

            pos[0] *= cameraDistance;
            pos[1] *= cameraDistance;
            pos[2] *= cameraDistance;

            cam.SetPosition(pos);
        }

        renWin.getVTKLock().unlock();

        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public CameraFrame createCameraFrameInDirectionOfAxis(AxisType axisType, boolean preserveCurrentDistance, File file, int delayMilliseconds)
    {
        CameraFrame result = new CameraFrame();
        result.file = file;
        result.delay = delayMilliseconds;

        double[] bounds = modelManager.getSmallBodyModel().getBoundingBox().getBounds();
        double xSize = Math.abs(bounds[1] - bounds[0]);
        double ySize = Math.abs(bounds[3] - bounds[2]);
        double zSize = Math.abs(bounds[5] - bounds[4]);
        double maxSize = Math.max(Math.max(xSize, ySize), zSize);

        double cameraDistance = getCameraDistance();

        result.focalPoint = new double[] {0.0, 0.0, 0.0};

        if (axisType == AxisType.NEGATIVE_X)
        {
            double xpos = xSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            result.position = new double[] {xpos, 0.0, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.POSITIVE_X)
        {
            double xpos = -xSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            result.position = new double[] {xpos, 0.0, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.NEGATIVE_Y)
        {
            double ypos = ySize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            result.position = new double[] {0.0, ypos, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.POSITIVE_Y)
        {
            double ypos = -ySize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            result.position = new double[] {0.0, ypos, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.NEGATIVE_Z)
        {
            double zpos = zSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            result.position = new double[] {0.0, 0.0, zpos};
            result.upDirection = new double[] {0.0, 1.0, 0.0};
        }
        else if (axisType == AxisType.POSITIVE_Z)
        {
            double zpos = -zSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            result.position = new double[] {0.0, 0.0, zpos};
            result.upDirection = new double[] {0.0, 1.0, 0.0};
        }

        if (preserveCurrentDistance)
        {
            double[] poshat = new double[3];

            MathUtil.unorm(result.position, poshat);

            result.position[0] = poshat[0] * cameraDistance;
            result.position[1] = poshat[1] * cameraDistance;
            result.position[2] = poshat[2] * cameraDistance;
        }

        return result;
    }

    public void setCameraFrame(CameraFrame frame)
    {
        vtkRenderer ren = renWin.getRenderer();
        if (ren.VisibleActorCount() == 0)
            return;

        renWin.getVTKLock().lock();

        vtkCamera cam = ren.GetActiveCamera();
        cam.SetFocalPoint(frame.focalPoint[0], frame.focalPoint[1], frame.focalPoint[2]);
        cam.SetPosition(frame.position[0], frame.position[1], frame.position[2]);
        cam.SetViewUp(frame.upDirection[0], frame.upDirection[1], frame.upDirection[2]);

        renWin.getVTKLock().unlock();

        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public void setCameraOrientation(
            double[] position,
            double[] focalPoint,
            double[] upVector,
            double viewAngle)
    {
        renWin.getVTKLock().lock();
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        cam.SetPosition(position);
        cam.SetFocalPoint(focalPoint);
        cam.SetViewUp(upVector);
        cam.SetViewAngle(viewAngle);
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public void setCameraViewAngle(
            double viewAngle)
    {
        renWin.getVTKLock().lock();
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        cam.SetViewAngle(viewAngle);
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public double getCameraViewAngle()
    {
        return renWin.getRenderer().GetActiveCamera().GetViewAngle();
    }

    public void resetToDefaultCameraViewAngle()
    {
        setCameraViewAngle(30.0);
    }

    public void setProjectionType(ProjectionType projectionType)
    {
        renWin.getVTKLock().lock();
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        if (projectionType == ProjectionType.ORTHOGRAPHIC)
            cam.ParallelProjectionOn();
        else
            cam.ParallelProjectionOff();
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public ProjectionType getProjectionType()
    {
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        if (cam.GetParallelProjection() != 0)
            return ProjectionType.ORTHOGRAPHIC;
        else
            return ProjectionType.PERSPECTIVE;
    }

    /**
     * Change the distance to the asteroid by simply scaling the unit vector
     * the points from the center of the asteroid in the direction of the
     * asteroid.
     *
     * @param distance
     */
    public void setCameraDistance(double distance)
    {
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();

        MathUtil.unorm(pos, pos);

        pos[0] *= distance;
        pos[1] *= distance;
        pos[2] *= distance;

        renWin.getVTKLock().lock();
        cam.SetPosition(pos);
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public double getCameraDistance()
    {
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();

        return MathUtil.vnorm(pos);
    }

    /**
     * Note viewAngle is a 1-element array which is returned to caller
     * @param position
     * @param cx
     * @param cy
     * @param cz
     * @param viewAngle
     */
    public void getCameraOrientation(
            double[] position,
            double[] cx,
            double[] cy,
            double[] cz,
            double[] viewAngle)
    {
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];

        double[] up = cam.GetViewUp();
        cx[0] = up[0];
        cx[1] = up[1];
        cx[2] = up[2];
        MathUtil.vhat(cx, cx);

        double[] fp = cam.GetFocalPoint();
        cz[0] = fp[0] - position[0];
        cz[1] = fp[1] - position[1];
        cz[2] = fp[2] - position[2];
        MathUtil.vhat(cz, cz);

        MathUtil.vcrss(cz, cx, cy);
        MathUtil.vhat(cy, cy);

        viewAngle[0] = cam.GetViewAngle();
    }

    // Gets the current lat/lon (degrees) position of the camera
    public LatLon getCameraLatLon()
    {
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        return MathUtil.reclat(cam.GetPosition()).toDegrees();
    }

    // Sets the lat/lon (degrees) position of the camera
    public void setCameraLatLon(LatLon latLon)
    {
        // Get active camera and current distance from origin
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        double distance = getCameraDistance();

        // Convert desired Lat/Lon to unit vector and scale to maintain same distance
        double[] pos = MathUtil.latrec(latLon.toRadians());
        MathUtil.unorm(pos, pos);
        pos[0] *= distance;
        pos[1] *= distance;
        pos[2] *= distance;

        // Set the new camera position
        renWin.getVTKLock().lock();
        cam.SetPosition(pos);
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    // Set camera's focal point
    public void setCameraFocalPoint(double[] focalPoint)
    {
        // Obtain lock
        renWin.getVTKLock().lock();
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        cam.SetFocalPoint(focalPoint);
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    // Sets the camera roll with roll as defined by vtkCamera
    public void setCameraRoll(double angle)
    {
        renWin.getVTKLock().lock();
        vtkCamera cam = renWin.getRenderer().GetActiveCamera();
        cam.SetRoll(angle);
        renWin.getVTKLock().unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    // Gets the camera roll with roll as defined by vtkCamera
    public double getCameraRoll()
    {
        return renWin.getRenderer().GetActiveCamera().GetRoll();
    }

    public vtksbmtJoglCanvasComponent getRenderWindowPanel()
    {
        return renWin;
    }

    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(Properties.MODEL_CHANGED))
        {
            this.setProps(modelManager.getProps());
        }
        else
        {
            renWin.Render();
        }
    }

    public void setDefaultInteractorStyleType(InteractorStyleType interactorStyleType)
    {
        if (interactorStyleType == InteractorStyleType.JOYSTICK_CAMERA)
            defaultInteractorStyle = joystickCameraInteractorStyle;
        else
            defaultInteractorStyle = trackballCameraInteractorStyle;

        // Change the interactor now unless it is currently null.
        if (renWin.getRenderWindowInteractor().GetInteractorStyle() != null)
            setInteractorStyleToDefault();
    }

    public InteractorStyleType getDefaultInteractorStyleType()
    {
        if (defaultInteractorStyle == joystickCameraInteractorStyle)
            return InteractorStyleType.JOYSTICK_CAMERA;
        else
            return InteractorStyleType.TRACKBALL_CAMERA;
    }

    public void setInteractorStyleToDefault()
    {
        renWin.setInteractorStyle(defaultInteractorStyle);
    }

    public void setInteractorStyleToNone()
    {
        renWin.setInteractorStyle(null);
    }

    public void setLighting(LightingType type)
    {
        if (type != currentLighting)
        {
            renWin.getRenderer().RemoveAllLights();
            if (type == LightingType.LIGHT_KIT)
            {
                lightKit.AddLightsToRenderer(renWin.getRenderer());
            }
            else if (type == LightingType.HEADLIGHT)
            {
                renWin.getRenderer().AddLight(headlight);
            }
            else
            {
                renWin.getRenderer().AddLight(fixedLight);
            }
            currentLighting = type;
            if (renWin.getRenderWindow().GetNeverRendered() == 0)
                renWin.Render();
        }
    }

    public LightingType getLighting()
    {
        return currentLighting;
    }

    public void setLightIntensity(double percentage)
    {
        if (percentage != getLightIntensity())
        {
            headlight.SetIntensity(percentage);
            fixedLight.SetIntensity(percentage);
            if (renWin.getRenderWindow().GetNeverRendered() == 0)
                renWin.Render();
        }
    }

    /**
     * Get the absolute position of the light in lat/lon/rad where
     * lat and lon are in degress.
     * @return
     */
    public LatLon getFixedLightPosition()
    {
        double[] position = fixedLight.GetPosition();
        return MathUtil.reclat(position).toDegrees();
    }

    /**
     * Set the absolute position of the light in lat/lon/rad.
     * Lat and lon must be in degrees.
     * @param latLon
     */
    public void setFixedLightPosition(LatLon latLon)
    {
        fixedLight.SetPosition(MathUtil.latrec(latLon.toRadians()));
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    /**
     * Rather than setting the absolute position of the light as in the previous
     * function, set the direction of the light source in the body frame. We still need a
     * distance for the light, so simply use a large multiple of the
     * shape model bounding box diagonal.
     * @param dir
     */
    public void setFixedLightDirection(double[] dir)
    {
        dir = dir.clone();
        MathUtil.vhat(dir, dir);
        double bbd = modelManager.getSmallBodyModel().getBoundingBoxDiagonalLength();
        dir[0] *= (1.0e5 * bbd);
        dir[1] *= (1.0e5 * bbd);
        dir[2] *= (1.0e5 * bbd);
        fixedLight.SetPosition(dir);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public double getLightIntensity()
    {
        return headlight.GetIntensity();
    }

    public void setShowOrientationAxes(boolean show)
    {
        if (getShowOrientationAxes() != show)
        {
            renWin.getVTKLock().lock();
            orientationWidget.SetEnabled(show ? 1 : 0);
            if (show)
                orientationWidget.SetInteractive(interactiveAxes ? 1 : 0);
            renWin.getVTKLock().unlock();
            if (renWin.getRenderWindow().GetNeverRendered() == 0)
                renWin.Render();
        }
    }

    public boolean getShowOrientationAxes()
    {
        int value = orientationWidget.GetEnabled();
        return value == 1 ? true : false;
    }

    public void setOrientationAxesInteractive(boolean interactive)
    {
        if (getOrientationAxesInteractive() != interactive &&
            getShowOrientationAxes())
        {
            renWin.getVTKLock().lock();
            orientationWidget.SetInteractive(interactive ? 1 : 0);
            renWin.getVTKLock().unlock();
            if (renWin.getRenderWindow().GetNeverRendered() == 0)
                renWin.Render();
        }
        interactiveAxes = interactive;
    }

    public boolean getOrientationAxesInteractive()
    {
        return interactiveAxes;
    }

    public void setMouseWheelMotionFactor(double factor)
    {
        trackballCameraInteractorStyle.SetMouseWheelMotionFactor(factor);
        joystickCameraInteractorStyle.SetMouseWheelMotionFactor(factor);
    }

    public double getMouseWheelMotionFactor()
    {
        return trackballCameraInteractorStyle.GetMouseWheelMotionFactor();
    }

    public int[] getBackgroundColor()
    {
        double[] bg = renWin.getRenderer().GetBackground();
        return new int[]{(int)(255.0*bg[0]), (int)(255.0*bg[1]), (int)(255.0*bg[2])};
    }

    public void setBackgroundColor(int[] color)
    {
        renWin.getRenderer().SetBackground(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public void set2DMode(boolean enable)
    {
        modelManager.set2DMode(enable);

        if (enable)
        {
            vtkCamera cam = renWin.getRenderer().GetActiveCamera();
            cam.ParallelProjectionOn();
            setCameraOrientationInDirectionOfAxis(AxisType.NEGATIVE_X, false);
            renWin.getVTKLock().lock();
            cam.SetViewUp(0.0, 1.0, 0.0);
            renWin.getVTKLock().unlock();
            vtkInteractorStyleImage style = new vtkInteractorStyleImage();
            renWin.setInteractorStyle(style);
        }
        else
        {
            renWin.getRenderer().GetActiveCamera().ParallelProjectionOff();
            renWin.resetCamera();
            setInteractorStyleToDefault();
        }

        renWin.Render();
    }

    public void setAxesSize(double size)
    {
        this.axesSize = size;
        orientationWidget.SetViewport(0.0, 0.0, size, size);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public double getAxesSize()
    {
        return axesSize;
    }

    public void setAxesLineWidth(double width)
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        property.SetLineWidth(width);
        property = axes.GetYAxisShaftProperty();
        property.SetLineWidth(width);
        property = axes.GetZAxisShaftProperty();
        property.SetLineWidth(width);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public double getAxesLineWidth()
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        return property.GetLineWidth();
    }

    public void setAxesLabelFontSize(int size)
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        textProperty.SetFontSize(size);
        caption = axes.GetYAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetFontSize(size);
        caption = axes.GetZAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetFontSize(size);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public int getAxesLabelFontSize()
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        return textProperty.GetFontSize();
    }

    public void setAxesLabelFontColor(int[] color)
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        textProperty.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        caption = axes.GetYAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        caption = axes.GetZAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public int[] getAxesLabelFontColor()
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        double[] c = textProperty.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setXAxisColor(int[] color)
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetXAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public int[] getXAxisColor()
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        double[] c = property.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setYAxisColor(int[] color)
    {
        vtkProperty property = axes.GetYAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetYAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public int[] getYAxisColor()
    {
        vtkProperty property = axes.GetYAxisShaftProperty();
        double[] c = property.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setZAxisColor(int[] color)
    {
        vtkProperty property = axes.GetZAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetZAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public int[] getZAxisColor()
    {
        vtkProperty property = axes.GetZAxisShaftProperty();
        double[] c = property.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setAxesConeLength(double size)
    {
        if (size > 1.0) size = 1.0;
        if (size < 0.0) size = 0.0;
        axes.SetNormalizedTipLength(size, size, size);
        // Change the shaft length also to fill in any space.
        axes.SetNormalizedShaftLength(1.0-size, 1.0-size, 1.0-size);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public double getAxesConeLength()
    {
        return axes.GetNormalizedTipLength()[0];
    }

    public void setAxesConeRadius(double size)
    {
        axes.SetConeRadius(size);
        if (renWin.getRenderWindow().GetNeverRendered() == 0)
            renWin.Render();
    }

    public double getAxesConeRadius()
    {
        return axes.GetConeRadius();
    }

    public static void saveToFile(File file, vtksbmtJoglCanvasComponent renWin)
    {
        if (file != null)
        {
            try
            {
                // The following line is needed due to some weird threading
                // issue with JOGL when saving out the pixel buffer. Note release
                // needs to be called at the end.
                renWin.getComponent().getContext().makeCurrent();

                renWin.getVTKLock().lock();
                vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
                windowToImage.SetInput(renWin.getRenderWindow());

                String filename = file.getAbsolutePath();
                if (filename.toLowerCase().endsWith("bmp"))
                {
                    vtkBMPWriter writer = new vtkBMPWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("jpg") ||
                        filename.toLowerCase().endsWith("jpeg"))
                {
                    vtkJPEGWriter writer = new vtkJPEGWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("png"))
                {
                    vtkPNGWriter writer = new vtkPNGWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("pnm"))
                {
                    vtkPNMWriter writer = new vtkPNMWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("ps"))
                {
                    vtkPostScriptWriter writer = new vtkPostScriptWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("tif") ||
                        filename.toLowerCase().endsWith("tiff"))
                {
                    vtkTIFFWriter writer = new vtkTIFFWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.SetCompressionToNoCompression();
                    writer.Write();
                }
                renWin.getVTKLock().unlock();
            }
            finally
            {
                renWin.getComponent().getContext().release();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        CameraFrame frame = cameraFrameQueue.peek();
        if (frame != null)
        {
            if (frame.staged && frame.file != null)
            {
                saveToFile(frame.file, renWin);
                cameraFrameQueue.remove();
            }
            else
            {
                setCameraFrame(frame);
                frame.staged = true;
            }

            Timer timer = new Timer(frame.delay, this);
            timer.setRepeats(false);
            timer.start();
        }

    }


}

class CameraFrame
{
    public boolean staged;
    public boolean saved;
    public int delay;
    public double[] position;
    public double[] upDirection;
    public double[] focalPoint;
    public File file;
}
