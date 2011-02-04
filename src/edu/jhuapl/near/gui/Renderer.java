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
import vtk.vtkLightKit;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkProperty;
import vtk.vtkRenderWindowPanel;
import vtk.vtkTextProperty;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.Properties;

public class Renderer extends JPanel implements
            PropertyChangeListener
{
    private vtkEnhancedRenderWindowPanel renWin;
    private ModelManager modelManager;
    private vtkInteractorStyleTrackballCamera defaultInteractorStyle;
    private vtkInteractorStyleRubberBand3D rubberBandInteractorStyle;
    private vtkAxesActor axes;
    private vtkOrientationMarkerWidget orientationWidget;

    public Renderer(final ModelManager modelManager)
    {
        setLayout(new BorderLayout());

        renWin = new vtkEnhancedRenderWindowPanel();

        this.modelManager = modelManager;

        modelManager.addPropertyChangeListener(this);

        defaultInteractorStyle = new vtkInteractorStyleTrackballCamera();
        rubberBandInteractorStyle = new vtkInteractorStyleRubberBand3D();

        renWin.setInteractorStyle(defaultInteractorStyle);

        renWin.GetRenderer().AutomaticLightCreationOff();
        vtkLightKit lightKit = new vtkLightKit();
        lightKit.SetKeyToFillRatio(1.0);
        lightKit.SetKeyToHeadRatio(20.0);
        lightKit.AddLightsToRenderer(renWin.GetRenderer());

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
        orientationWidget.SetEnabled(1);
        orientationWidget.InteractiveOn();

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
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
            double[] upVector)
    {
        renWin.lock();
        vtkCamera cam = renWin.GetRenderer().GetActiveCamera();
        cam.SetPosition(position);
        cam.SetFocalPoint(focalPoint);
        cam.SetViewUp(upVector);
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
}
