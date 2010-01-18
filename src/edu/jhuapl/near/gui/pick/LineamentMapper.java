package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.model.*;

public class LineamentMapper 
{
    private ModelManager modelManager;
	private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    private LineModel lineModel;
    private vtkCellPicker erosPicker;

    private boolean currentlyDrawing = false;
    private int vertexIdBeingEdited = 0;
    private int lineIdBeingEdited = 0;
    
    public LineamentMapper(
			ErosRenderer erosRenderer, 
			ModelManager modelManager
			) 
	{
		this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;

		erosPicker = new vtkCellPicker();
		erosPicker.SetTolerance(0.002);
		erosPicker.PickFromListOn();
		erosPicker.InitializePickList();
		erosModel = (ErosModel)modelManager.getModel(ModelManager.EROS);
		ArrayList<vtkActor> actors = erosModel.getActors();
		erosPicker.GetPickList().RemoveAllItems();
		for (vtkActor act : actors)
		{
			erosPicker.AddPickList(act);
		}

	}
	
	public void mousePressed(MouseEvent e) 
	{
		// If we pressed a point on Eros, begin drawing a new line.
		// If we pressed a vertex of an existing lineament, begin dragging that vertex.
		
		int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = erosPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);

			if (model == erosModel)
			{
				double[] pos = erosPicker.GetPickPosition();
				if (currentlyDrawing)
				{
					lineModel.updateLineamentVertex(lineIdBeingEdited, vertexIdBeingEdited, pos);
				}
				else
				{
					lineModel.addNewLineament(pos, pos);
					currentlyDrawing = true;
				}
			}
		}		

	}
	
//	public void mouseReleased(MouseEvent e) 
//	{
//	}
//	
//	public void mouseDragged(MouseEvent e) 
//	{
//	}

	public void mouseMoved(MouseEvent e) 
	{
		int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = erosPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);

			if (model == erosModel)
			{
				double[] pos = erosPicker.GetPickPosition();
				lineModel.addNewLineament(pos, pos);
			}
		}		

	}
	
	public void stopEditing()
	{
		currentlyDrawing = false;
	}
}
