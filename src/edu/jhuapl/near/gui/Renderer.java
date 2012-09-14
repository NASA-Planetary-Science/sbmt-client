package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkCaptionActor2D;
import vtk.vtkInteractorStyle;
import vtk.vtkInteractorStyleImage;
import vtk.vtkInteractorStyleJoystickCamera;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkLight;
import vtk.vtkLightKit;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkProperty;
import vtk.vtkRenderWindowPanel;
import vtk.vtkRenderer;
import vtk.vtkTextProperty;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Preferences;
import edu.jhuapl.near.util.Properties;

public class Renderer extends JPanel implements
            PropertyChangeListener
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
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Y,
        NEGATIVE_Y,
        POSITIVE_Z,
        NEGATIVE_Z
    }

    private vtkEnhancedRenderWindowPanel renWin;
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

    public Renderer(final ModelManager modelManager)
    {
        setLayout(new BorderLayout());

        renWin = new vtkEnhancedRenderWindowPanel();

        this.modelManager = modelManager;

        modelManager.addPropertyChangeListener(this);

        trackballCameraInteractorStyle = new vtkInteractorStyleTrackballCamera();
        joystickCameraInteractorStyle = new vtkInteractorStyleJoystickCamera();

        defaultInteractorStyle = trackballCameraInteractorStyle;

        InteractorStyleType interactorStyleType = InteractorStyleType.valueOf(
                Preferences.getInstance().get(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.TRACKBALL_CAMERA.toString()));
        setDefaultInteractorStyleType(interactorStyleType);

        setMouseWheelMotionFactor(Preferences.getInstance().getAsDouble(Preferences.MOUSE_WHEEL_MOTION_FACTOR, 1.0));

        headlight = renWin.GetRenderer().MakeLight();
        headlight.SetLightTypeToHeadlight();
        headlight.SetConeAngle(180.0);

        fixedLight = renWin.GetRenderer().MakeLight();
        fixedLight.SetLightTypeToSceneLight();
        fixedLight.PositionalOn();
        fixedLight.SetConeAngle(180.0);
        LatLon defaultPosition = new LatLon(
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LATITUDE, 90.0),
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LONGITUDE, 0.0),
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_DISTANCE, 1.0e8));
        setFixedLightPosition(defaultPosition);
        setLightIntensity(Preferences.getInstance().getAsDouble(Preferences.LIGHT_INTENSITY, 1.0));


        renWin.GetRenderer().AutomaticLightCreationOff();
        lightKit = new vtkLightKit();
        lightKit.SetKeyToFillRatio(1.0);
        lightKit.SetKeyToHeadRatio(20.0);

        LightingType lightingType = LightingType.valueOf(
                Preferences.getInstance().get(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString()));
        setLighting(lightingType);

        add(renWin, BorderLayout.CENTER);

        axes = new vtkAxesActor();

        // Set the z axis to yellow since the blue default is
        // hard to see on a black background.
        vtkProperty property = axes.GetZAxisShaftProperty();
        property.SetColor(1.0, 1.0, 0.0);
        property = axes.GetZAxisTipProperty();
        property.SetColor(1.0, 1.0, 0.0);

        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        //caption.GetTextActor().SetTextScaleModeToNone();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        //textProperty.SetFontSize(14);
        textProperty.BoldOff();
        textProperty.ItalicOff();

        caption = axes.GetYAxisCaptionActor2D();
        //caption.GetTextActor().SetTextScaleModeToNone();
        textProperty = caption.GetCaptionTextProperty();
        //textProperty.SetFontSize(14);
        textProperty.BoldOff();
        textProperty.ItalicOff();

        caption = axes.GetZAxisCaptionActor2D();
        //caption.GetTextActor().SetTextScaleModeToNone();
        textProperty = caption.GetCaptionTextProperty();
        //textProperty.SetFontSize(14);
        textProperty.BoldOff();
        textProperty.ItalicOff();

        orientationWidget = new vtkOrientationMarkerWidget();
        orientationWidget.SetOrientationMarker(axes);
        orientationWidget.SetInteractor(renWin.getIren());
        orientationWidget.SetTolerance(10);

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

    public void setProps(ArrayList<vtkProp> props)
    {
        // Go through the props and if an prop is already in the renderer,
        // do nothing. If not, add it. If an prop not listed is
        // in the renderer, remove it from the renderer.

        // First remove the props not in the specified list that are currently rendered.
        vtkPropCollection propCollection = renWin.GetRenderer().GetViewProps();
        int size = propCollection.GetNumberOfItems();
        HashSet<vtkProp> renderedProps = new HashSet<vtkProp>();
        for (int i=0; i<size; ++i)
            renderedProps.add((vtkProp)propCollection.GetItemAsObject(i));
        renderedProps.removeAll(props);
        if (!renderedProps.isEmpty())
        {
            renWin.lock();
            for (vtkProp prop : renderedProps)
                renWin.GetRenderer().RemoveViewProp(prop);
            renWin.unlock();
        }

        // Next add the new props.
        for (vtkProp prop : props)
        {
            if (renWin.GetRenderer().HasViewProp(prop) == 0)
                renWin.GetRenderer().AddViewProp(prop);
        }

        // If we are in 2D mode, then remove all props of models that
        // do not support 2D mode.
        if (modelManager.is2DMode())
        {
            propCollection = renWin.GetRenderer().GetViewProps();
            size = propCollection.GetNumberOfItems();
            for (int i=size-1; i>=0; --i)
            {
                vtkProp prop = (vtkProp)propCollection.GetItemAsObject(i);
                Model model = modelManager.getModel(prop);
                if (model != null && !model.supports2DMode())
                {
                    renWin.lock();
                    renWin.GetRenderer().RemoveViewProp(prop);
                    renWin.unlock();
                }
            }
        }

        if (renWin.GetRenderWindow().GetNeverRendered() > 0)
            return;
        renWin.Render();
    }

    /*
    public void addActor(vtkActor actor)
    {
        if (renWin.GetRenderer().HasViewProp(actor) == 0)
        {
            renWin.GetRenderer().AddActor(actor);
            renWin.Render();
        }
    }

    private void addActors(ArrayList<vtkActor> actors)
    {
        boolean actorWasAdded = false;
        for (vtkActor act : actors)
        {
            if (renWin.GetRenderer().HasViewProp(act) == 0)
            {
                renWin.GetRenderer().AddActor(act);
                actorWasAdded = true;
            }
        }

        if (actorWasAdded)
            render();
    }

    public void removeActor(vtkActor actor)
    {
        if (renWin.GetRenderer().HasViewProp(actor) > 0)
        {
            renWin.GetRenderer().RemoveActor(actor);
            renWin.Render();
        }
    }

    public void removeActors(ArrayList<vtkActor> actors)
    {
        boolean actorWasRemoved = false;
        for (vtkActor act : actors)
        {
            if (renWin.GetRenderer().HasViewProp(act) > 0)
            {
                renWin.GetRenderer().RemoveActor(act);
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
        File file = ImageFileChooser.showSaveDialog(this, "Export to Image");
        renWin.saveToFile(file);
    }

    public void save6ViewsToFile()
    {
        File file = ImageFileChooser.showSaveDialog(this, "Export to Image", false);
        String path = file.getAbsolutePath();
        String base = path.substring(0, path.lastIndexOf('.'));
        String ext = path.substring(path.lastIndexOf('.'));

        File[] files = {
                file = new File(base + "+x" + ext),
                file = new File(base + "-x" + ext),
                file = new File(base + "+y" + ext),
                file = new File(base + "-y" + ext),
                file = new File(base + "+z" + ext),
                file = new File(base + "-z" + ext)
        };

        // Check if one of the files already exist and if so, prompt user.
        for (File f : files)
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
                    break;
            }
        }

        AxisType[] axes = {
                AxisType.POSITIVE_X, AxisType.NEGATIVE_X,
                AxisType.POSITIVE_Y, AxisType.NEGATIVE_Y,
                AxisType.POSITIVE_Z, AxisType.NEGATIVE_Z
        };

        for (int i=0; i<axes.length; ++i)
        {
            setCameraOrientationInDirectionOfAxis(axes[i], true);
            renWin.saveToFile(files[i]);
        }
    }

    public void setCameraOrientationInDirectionOfAxis(AxisType axisType, boolean preserveCurrentDistance)
    {
        vtkRenderer ren = renWin.GetRenderer();
        if (ren.VisibleActorCount() == 0) return;

        double[] bounds = modelManager.getSmallBodyModel().getBoundingBox().getBounds();
        double xSize = Math.abs(bounds[1] - bounds[0]);
        double ySize = Math.abs(bounds[3] - bounds[2]);
        double zSize = Math.abs(bounds[5] - bounds[4]);
        double maxSize = Math.max(Math.max(xSize, ySize), zSize);

        double cameraDistance = getCameraDistance();

        renWin.lock();
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

        renWin.unlock();

        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public void setCameraOrientation(
            double[] position,
            double[] focalPoint,
            double[] upVector,
            double viewAngle)
    {
        renWin.lock();
        vtkCamera cam = renWin.GetRenderer().GetActiveCamera();
        cam.SetPosition(position);
        cam.SetFocalPoint(focalPoint);
        cam.SetViewUp(upVector);
        cam.SetViewAngle(viewAngle);
        renWin.unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public void setCameraViewAngle(
            double viewAngle)
    {
        renWin.lock();
        vtkCamera cam = renWin.GetRenderer().GetActiveCamera();
        cam.SetViewAngle(viewAngle);
        renWin.unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public double getCameraViewAngle()
    {
        return renWin.GetRenderer().GetActiveCamera().GetViewAngle();
    }

    public void resetToDefaultCameraViewAngle()
    {
        setCameraViewAngle(30.0);
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
        vtkCamera cam = renWin.GetRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();

        MathUtil.unorm(pos, pos);

        pos[0] *= distance;
        pos[1] *= distance;
        pos[2] *= distance;

        renWin.lock();
        cam.SetPosition(pos);
        renWin.unlock();
        renWin.resetCameraClippingRange();
        renWin.Render();
    }

    public double getCameraDistance()
    {
        vtkCamera cam = renWin.GetRenderer().GetActiveCamera();

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
        vtkCamera cam = renWin.GetRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];

        double[] up = cam.GetViewUp();
        cx[0] = up[0];
        cx[1] = up[1];
        cx[2] = up[2];

        double[] fp = cam.GetFocalPoint();
        cz[0] = fp[0] - position[0];
        cz[1] = fp[1] - position[1];
        cz[2] = fp[2] - position[2];
        MathUtil.vhat(cz, cz);

        MathUtil.vcrss(cz, cx, cy);

        viewAngle[0] = cam.GetViewAngle();
    }


    public vtkRenderWindowPanel getRenderWindowPanel()
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
        if (renWin.getIren().GetInteractorStyle() != null)
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
            renWin.GetRenderer().RemoveAllLights();
            if (type == LightingType.LIGHT_KIT)
            {
                lightKit.AddLightsToRenderer(renWin.GetRenderer());
            }
            else if (type == LightingType.HEADLIGHT)
            {
                renWin.GetRenderer().AddLight(headlight);
            }
            else
            {
                renWin.GetRenderer().AddLight(fixedLight);
            }
            currentLighting = type;
            if (renWin.GetRenderWindow().GetNeverRendered() == 0)
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
            if (renWin.GetRenderWindow().GetNeverRendered() == 0)
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
        if (renWin.GetRenderWindow().GetNeverRendered() == 0)
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
        if (renWin.GetRenderWindow().GetNeverRendered() == 0)
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
            renWin.lock();
            orientationWidget.SetEnabled(show ? 1 : 0);
            if (show)
                orientationWidget.SetInteractive(interactiveAxes ? 1 : 0);
            renWin.unlock();
            if (renWin.GetRenderWindow().GetNeverRendered() == 0)
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
            renWin.lock();
            orientationWidget.SetInteractive(interactive ? 1 : 0);
            renWin.unlock();
            if (renWin.GetRenderWindow().GetNeverRendered() == 0)
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

    public void set2DMode(boolean enable)
    {
        modelManager.set2DMode(enable);

        if (enable)
        {
            vtkCamera cam = renWin.GetRenderer().GetActiveCamera();
            cam.ParallelProjectionOn();
            setCameraOrientationInDirectionOfAxis(AxisType.NEGATIVE_X, false);
            renWin.lock();
            cam.SetViewUp(0.0, 1.0, 0.0);
            renWin.unlock();
            vtkInteractorStyleImage style = new vtkInteractorStyleImage();
            renWin.setInteractorStyle(style);
        }
        else
        {
            renWin.GetRenderer().GetActiveCamera().ParallelProjectionOff();
            renWin.resetCamera();
            setInteractorStyleToDefault();
        }

        renWin.Render();
    }
}
