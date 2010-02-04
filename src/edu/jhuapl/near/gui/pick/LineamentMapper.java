package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.*;

import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.Properties;

public class LineamentMapper extends Picker
{
    private ModelManager modelManager;
	//private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    private LineModel lineModel;
    
    private vtkCellPicker erosPicker;
    private vtkCellPicker lineModelPicker;

    private boolean currentlyDrawing = false;
    private int vertexIdBeingEdited = -1;
    private int lineIdBeingEdited = -1;
    
    public LineamentMapper(
			ErosRenderer erosRenderer, 
			ModelManager modelManager
			) 
	{
		//this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;
		this.lineModel = ((StructureModel)modelManager.getModel(ModelManager.STRUCTURES)).getLineModel();
		
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

		lineModelPicker = new vtkCellPicker();
		lineModelPicker.SetTolerance(0.002);
		lineModelPicker.PickFromListOn();
		lineModelPicker.InitializePickList();
		actors = lineModel.getProps();
		lineModelPicker.GetPickList().RemoveAllItems();
		for (vtkProp act : actors)
		{
			lineModelPicker.AddPickList(act);
		}

	}
	
	public void mousePressed(MouseEvent e) 
	{
		// If we pressed a point on Eros, begin drawing a new line.
		// If we pressed a vertex of an existing lineament, begin dragging that vertex.
		// If we double clicked, end drawing the line

		if (e.getButton() != MouseEvent.BUTTON1)
			return;
			
		int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());

		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = erosPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);

			if (model == erosModel)
			{
				double[] pos = erosPicker.GetPickPosition();
				if (e.getClickCount() == 1)
				{
					if (currentlyDrawing)
					{
						lineModel.addVertexToLine(lineIdBeingEdited, pos);
						++vertexIdBeingEdited;
					}
					else
					{
						lineIdBeingEdited = lineModel.getNumberOfLines();
						lineModel.addNewLine(pos);
						currentlyDrawing = true;
						vertexIdBeingEdited = 1;
					}
				}
				else if (e.getClickCount() > 1)
				{
					lineModel.addVertexToLine(lineIdBeingEdited, pos);
					stopEditing();
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

	/*
	public void mouseMoved(MouseEvent e) 
	{
		if (currentlyDrawing)
		{
			int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());

			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = erosPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model == erosModel)
				{
					double[] pos = erosPicker.GetPickPosition();
					lineModel.updateLineVertex(lineIdBeingEdited, vertexIdBeingEdited, pos);
				}
			}		
		}
	}
	*/
	
	public void stopEditing()
	{
		currentlyDrawing = false;
		lineIdBeingEdited = -1;
		vertexIdBeingEdited = -1;

		//this.pcs.firePropertyChange(Properties.FINISHED_DRAWING_LINE, null, null);
	}
}
