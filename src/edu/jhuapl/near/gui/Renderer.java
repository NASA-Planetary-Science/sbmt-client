package edu.jhuapl.near.gui;

import java.util.*;
import javax.swing.*;
import java.beans.*;
import java.awt.*;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.Properties;
import vtk.*;

public class Renderer extends JPanel implements 
			PropertyChangeListener
{
    private vtkRenderWindowPanelWithMouseWheel renWin;
    private ModelManager modelManager;
    private vtkInteractorStyleTrackballCamera defaultInteractorStyle;
    private vtkInteractorStyleRubberBand3D rubberBandInteractorStyle;
    
    public Renderer(ModelManager modelManager) 
    {
    	setLayout(new BorderLayout());
    	
        renWin = new vtkRenderWindowPanelWithMouseWheel();

        this.modelManager = modelManager;

        modelManager.addPropertyChangeListener(this);
        
        defaultInteractorStyle = new vtkInteractorStyleTrackballCamera();
        rubberBandInteractorStyle = new vtkInteractorStyleRubberBand3D();

        renWin.setInteractorStyle(defaultInteractorStyle);
        
        renWin.GetRenderer().AutomaticLightCreationOff();
        vtkLightKit lightKit = new vtkLightKit();
        //lightKit.SetKeyToBackRatio(5);
        //lightKit.SetKeyToHeadRatio(5);
        lightKit.AddLightsToRenderer(renWin.GetRenderer());

        add(renWin, BorderLayout.CENTER);

        setProps(modelManager.getProps());
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
