package edu.jhuapl.near.pick;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.*;

import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.model.*;

public class CircleSelectionPicker extends Picker
{
    private ModelManager modelManager;
	//private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    private CircleModel circleModel;
    
    private vtkCellPicker erosPicker;

    private int vertexIdBeingEdited = -1;
	
    public CircleSelectionPicker(
			ErosRenderer erosRenderer, 
			ModelManager modelManager
			) 
	{
    	//this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;
		this.circleModel = (CircleModel)modelManager.getModel(ModelManager.CIRCLE_SELECTION);
		
		erosPicker = new vtkCellPicker();
		erosPicker.SetTolerance(0.002);
		erosPicker.PickFromListOn();
		erosPicker.InitializePickList();
		erosModel = (ErosModel)modelManager.getModel(ModelManager.EROS);
		ArrayList<vtkProp> actors = erosModel.getProps();
		erosPicker.GetPickList().RemoveAllItems();
		for (vtkProp act : actors)
		{
			erosPicker.AddPickList(act);
		}
	}

    public void mousePressed(MouseEvent e) 
    {
    	//if (e.getButton() != MouseEvent.BUTTON1)
    	//	return;

    	vertexIdBeingEdited = -1;

    	circleModel.removeAllStructures();
    	
    	renWin.lock();
    	int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
    	renWin.unlock();

    	if (pickSucceeded == 1)
    	{
    		vtkActor pickedActor = erosPicker.GetActor();
    		Model model = modelManager.getModel(pickedActor);

    		if (model == erosModel)
    		{
    			double[] pos = erosPicker.GetPickPosition();
    			if (e.getClickCount() == 1)
    			{
    				circleModel.addNewStructure(pos);
    				vertexIdBeingEdited = circleModel.getNumberOfStructures()-1;
    			}
    		}
    	}		
	}
	
	public void mouseReleased(MouseEvent e) 
	{
		vertexIdBeingEdited = -1;
	}
	
	public void mouseDragged(MouseEvent e) 
	{
		//if (e.getButton() != MouseEvent.BUTTON1)
		//	return;

		
		if (vertexIdBeingEdited >= 0)
		{
			renWin.lock();
			int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();
			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = erosPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model == erosModel)
				{
					double[] lastDragPosition = erosPicker.GetPickPosition();

					circleModel.changeRadiusOfCircle(vertexIdBeingEdited, lastDragPosition);
				}
			}
		}
	}
}
