package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JPanel;

import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkCaptionActor2D;
import vtk.vtkInteractorStyleRubberBand3D;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkLight;
import vtk.vtkLightKit;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkProperty;
import vtk.vtkRenderWindowPanel;
import vtk.vtkTextProperty;

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
    private vtkEnhancedRenderWindowPanel renWin;
    private ModelManager modelManager;
    private vtkInteractorStyleTrackballCamera defaultInteractorStyle;
    private vtkInteractorStyleRubberBand3D rubberBandInteractorStyle;
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

        defaultInteractorStyle = new vtkInteractorStyleTrackballCamera();
        rubberBandInteractorStyle = new vtkInteractorStyleRubberBand3D();

        renWin.setInteractorStyle(defaultInteractorStyle);

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
        renWin.saveToFile();
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

    public void setInteractorToRubberBand()
    {
        renWin.setInteractorStyle(rubberBandInteractorStyle);
    }

    public void setInteractorToDefault()
    {
        renWin.setInteractorStyle(defaultInteractorStyle);
    }

    public void setInteractorToNone()
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
}
