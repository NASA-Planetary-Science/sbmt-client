package edu.jhuapl.near.gui;

import java.util.*;
import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import edu.jhuapl.near.gui.pick.PickEvent;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.util.Properties;
import vtk.*;

public class ErosRenderer extends JPanel implements 
//			MouseListener, 
//			MouseMotionListener, 
			MouseWheelListener,
			PropertyChangeListener
{
    //private StatusBar statusBar;
    private vtkRenderWindowPanel renWin;
    private ModelManager modelManager;
    
    //private LineamentPopupMenu popupMenu;
    
//	protected final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
//    public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
//    { this.pcs.addPropertyChangeListener( propertyName, listener ); }
//    public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
//    { this.pcs.removePropertyChangeListener( propertyName, listener ); }
    
    
    public ErosRenderer(ModelManager modelManager) 
    {
    	setLayout(new BorderLayout());
    	
    	//this.statusBar = statusBar;
    	
        renWin = new vtkRenderWindowPanel();

        this.modelManager = modelManager;
        
        modelManager.addPropertyChangeListener(this);
        //popupMenu = new LineamentPopupMenu(model);
        
        
        vtkInteractorStyleTrackballCamera style =
            new vtkInteractorStyleTrackballCamera();
        renWin.setInteractorStyle(style);
        
        add(renWin, BorderLayout.CENTER);

        //renWin.addMouseListener(this);
        //renWin.addMouseMotionListener(this);
        renWin.addMouseWheelListener(this);

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

    	render();
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
    
	public void mousePressed(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseClicked(MouseEvent e)
	{
		
	}

	public void mouseEntered(MouseEvent e)
	{
		
	}

	public void mouseExited(MouseEvent e)
	{
		
	}

	public void mouseDragged(MouseEvent e)
	{
		
	}

	public void mouseMoved(MouseEvent e)
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
			return;

		this.pickActors(e);
		
		/*
		LineamentModel.Lineament lin = pickLineament(e);

		if (lin != null)
			statusBar.setLeftText("Lineament " + lin.id + " mapped on MSI image " + lin.name + " contains " + lin.x.size() + " vertices");

		LatLon ll = pickEros(e);
		if (ll != null)
			statusBar.setRightText("Lineament " + lin.id + " mapped on MSI image " + lin.name + " contains " + lin.x.size() + " vertices");
		*/
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		int ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1
				: 0;
		int shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1
				: 0;

		renWin.getIren().SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed,
			        shiftPressed, '0', 0, "0");

		renWin.lock();
		if (e.getWheelRotation() > 0)
			renWin.getIren().MouseWheelBackwardEvent();
		else
			renWin.getIren().MouseWheelForwardEvent();
		renWin.unlock();
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
    		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
    			return;
    		
    		/*
    		LineamentModel.Lineament lin = pickLineament(e);
    		
    		if (lin != null)
            	popupMenu.show(e.getComponent(), e.getX(), e.getY(), lin);
            */
        }
    }

	private void pickActors(MouseEvent e)
	{
		vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.002);
		int pickSucceeded = cellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		System.out.println("dfds");
		if (pickSucceeded != 0)
		{
			System.out.println("XXXXXXXXXx");
			PickEvent pickEvent = new PickEvent(cellPicker, e);
			this.firePropertyChange(Properties.PICK_OCCURED, null, pickEvent);
			//this.firePropertyChange(Properties.PICK_OCCURED, null, null);
		}
	}
	
	/*
	private LineamentModel.Lineament pickLineament(MouseEvent e)
	{
		vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.002);
		int pickSucceeded = cellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded != 0 && cellPicker.GetActor() == this.lineamentActor)
			return this.lineamentModel.getLineament(cellPicker.GetCellId());
		else
			return null;
	}

	private LatLon pickEros(MouseEvent e)
	{
		vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.002);
		int pickSucceeded = cellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded != 0 // && cellPicker.GetActor() == this.erosActor
			)
		{
			vtkPoints pts = cellPicker.GetPickedPositions();
			System.out.println(pts.GetNumberOfPoints());
			for (int i=0;i<cellPicker.GetActors().GetNumberOfItems();++i)
				System.out.println("--"+(cellPicker.GetActors().GetItemAsObject(i)==this.erosActor));
			return null;
		}
		else
		{
			System.out.println(0);
			return null;
		}
	}
	*/
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
    
//    public ModelManager getModelManager()
//    {
//    	return modelManager;
//    }
    
    public void render()
    {
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
			return;
		renWin.Render();
    }
}
