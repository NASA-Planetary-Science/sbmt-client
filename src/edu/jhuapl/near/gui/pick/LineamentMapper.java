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
	//private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    private LineModel lineModel;
    
    private vtkCellPicker erosPicker;
    private vtkCellPicker lineModelPicker;

    private boolean currentlyDrawing = false;
    private int vertexIdBeingEdited = 0;
    private int lineIdBeingEdited = 0;
    
    public LineamentMapper(
			ErosRenderer erosRenderer, 
			ModelManager modelManager
			) 
	{
		//this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;
		this.lineModel = (LineModel)modelManager.getModel(ModelManager.LINE_STRUCTURES);
		
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

		lineModelPicker = new vtkCellPicker();
		lineModelPicker.SetTolerance(0.002);
		lineModelPicker.PickFromListOn();
		lineModelPicker.InitializePickList();
		actors = lineModel.getActors();
		lineModelPicker.GetPickList().RemoveAllItems();
		for (vtkActor act : actors)
		{
			lineModelPicker.AddPickList(act);
		}

	}
	
	public void mousePressed(MouseEvent e) 
	{
		// If we pressed a point on Eros, begin drawing a new line.
		// If we pressed a vertex of an existing lineament, begin dragging that vertex.
		
		System.err.println("A111111");
		int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		System.err.println("B2222222");
		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = erosPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);

			if (model == erosModel)
			{
				double[] pos = erosPicker.GetPickPosition();
				if (currentlyDrawing)
				{
					lineModel.addLineamentVertex(lineIdBeingEdited, pos);
					++vertexIdBeingEdited;
				}
				else
				{
					lineIdBeingEdited = lineModel.getNumberOfLineaments();
					lineModel.addNewLineament(pos, pos);
					currentlyDrawing = true;
					vertexIdBeingEdited = 1;
				}
			}
		}		

	}
	
	public void mouseReleased(MouseEvent e) 
	{
	}
	
	public void mouseDragged(MouseEvent e) 
	{
	}

	public void mouseMoved(MouseEvent e) 
	{
		if (currentlyDrawing)
		{
			System.err.println("C3333333");
			int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
			System.err.println("D4444444");
			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = erosPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model == erosModel)
				{
					double[] pos = erosPicker.GetPickPosition();
					lineModel.updateLineamentVertex(lineIdBeingEdited, vertexIdBeingEdited, pos);
				}
			}		
		}
	}
	
	public void stopEditing()
	{
		currentlyDrawing = false;
	}
}
