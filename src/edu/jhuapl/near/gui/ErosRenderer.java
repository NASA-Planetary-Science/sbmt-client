package edu.jhuapl.near.gui;

import java.util.*;
import javax.swing.*;
import java.beans.*;
import java.awt.*;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.Properties;
import vtk.*;

public class ErosRenderer extends JPanel implements 
			PropertyChangeListener
{
    private vtkRenderWindowPanel renWin;
    private ModelManager modelManager;
    
    
    public ErosRenderer(ModelManager modelManager) 
    {
    	setLayout(new BorderLayout());
    	
        renWin = new vtkRenderWindowPanelWithMouseWheel();

        this.modelManager = modelManager;

        modelManager.addPropertyChangeListener(this);
        
        vtkInteractorStyleTrackballCamera style =
        	new vtkInteractorStyleTrackballCamera();
        renWin.setInteractorStyle(style);
        
        add(renWin, BorderLayout.CENTER);

        setActors(modelManager.getActors());
    }

    public void setActors(ArrayList<vtkActor> actors)
    {
    	// Go through the actors and if an actor is already in the renderer,
    	// do nothing. If not, add it. If an actor not listed is
    	// in the renderer, remove it from the renderer.
    	
    	// First remove the actors not in the specified list that are currently rendered.
    	vtkActorCollection actorCollection = renWin.GetRenderer().GetActors();
    	int size = actorCollection.GetNumberOfItems();
    	HashSet<vtkActor> renderedActors = new HashSet<vtkActor>();
    	for (int i=0; i<size; ++i)
    		renderedActors.add((vtkActor)actorCollection.GetItemAsObject(i));
    	renderedActors.removeAll(actors);
    	for (vtkActor act : renderedActors)
			renWin.GetRenderer().RemoveActor(act);

    	// Next add the new actors.
    	for (vtkActor act : actors)
    	{
    		if (renWin.GetRenderer().HasViewProp(act) == 0)
    			renWin.GetRenderer().AddActor(act);
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
    
    public vtkRenderWindowPanel getRenderWindowPanel()
    {
    	return renWin;
    }
    
    public void propertyChange(PropertyChangeEvent e)
    {
    	if (e.getPropertyName().equals(Properties.MODEL_CHANGED))
    	{
    		this.setActors(modelManager.getActors());
    	}
    	else
    	{
    		renWin.Render();
    	}
    }
}
