package edu.jhuapl.near.gui.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.*;

import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.model.*;

public class LinePicker extends Picker
{
    private ModelManager modelManager;
	//private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    private LineModel lineModel;
    
    private vtkCellPicker erosPicker;
    private vtkCellPicker linePicker;
    private vtkCellPicker lineSelectionPicker;

    private int vertexIdBeingEdited = -1;
    //private int lineIdBeingEdited = -1;
    //private LineModel.Line lineBeingEdited;
    
    // There are 2 types of line editing possible: 
    //   1. Dragging an existing vertex to a new locations
    //   2. Extending a line by adding new vertices
	public enum EditMode
	{
		VERTEX_DRAG,
		VERTEX_ADD
	}

	private EditMode currentEditMode = EditMode.VERTEX_ADD;

	private double[] lastDragPosition;
	
    public LinePicker(
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

		linePicker = new vtkCellPicker();
		linePicker.SetTolerance(0.002);
		linePicker.PickFromListOn();
		linePicker.InitializePickList();
		linePicker.GetPickList().RemoveAllItems();
		linePicker.AddPickList(lineModel.getLineActor());

		lineSelectionPicker = new vtkCellPicker();
		lineSelectionPicker.SetTolerance(0.002);
		lineSelectionPicker.PickFromListOn();
		lineSelectionPicker.InitializePickList();
		lineSelectionPicker.GetPickList().RemoveAllItems();
		lineSelectionPicker.AddPickList(lineModel.getLineSelectionActor());
	}
	
	public void mousePressed(MouseEvent e) 
	{
		// If we pressed a vertex of an existing lineament, begin dragging that vertex.
		// If we pressed a point on Eros, begin drawing a new line.

		if (e.getButton() != MouseEvent.BUTTON1)
			return;

		vertexIdBeingEdited = -1;

		if (this.currentEditMode == EditMode.VERTEX_DRAG)
		{
			renWin.lock();
			int pickSucceeded = lineSelectionPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();
			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = lineSelectionPicker.GetActor();
				System.out.println("ps1");

				if (pickedActor == lineModel.getLineSelectionActor())
				{
					System.out.println("ps2");
					this.vertexIdBeingEdited = lineSelectionPicker.GetCellId();

					lineModel.selectCurrentLineVertex(vertexIdBeingEdited);
				}
			}
		}
		else if (this.currentEditMode == EditMode.VERTEX_ADD)
		{
			renWin.lock();
			int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
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
						lineModel.insertVertexIntoSelectedLine(pos);
					}
				}
			}		
		}
	}
	
	public void mouseReleased(MouseEvent e) 
	{
		System.out.println("mouse released");
		if (this.currentEditMode == EditMode.VERTEX_DRAG &&
				vertexIdBeingEdited >= 0)
		{
			lineModel.updateSelectedLineVertex(vertexIdBeingEdited, lastDragPosition);
		}

		vertexIdBeingEdited = -1;
	}
	
	public void mouseDragged(MouseEvent e) 
	{
		//if (e.getButton() != MouseEvent.BUTTON1)
		//	return;

		
		if (this.currentEditMode == EditMode.VERTEX_DRAG &&
			vertexIdBeingEdited >= 0)
		{
			renWin.lock();
			int pickSucceeded = erosPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();
			if (pickSucceeded == 1)
			{
				System.out.println("Dragged1");
				vtkActor pickedActor = erosPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model == erosModel)
				{
					lastDragPosition = erosPicker.GetPickPosition();

					lineModel.moveSelectionVertex(vertexIdBeingEdited, lastDragPosition);
				}
				System.out.println("Dragged2");
			}
		}
	}

	
	public void mouseMoved(MouseEvent e) 
	{
		renWin.lock();
		int pickSucceeded = lineSelectionPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		renWin.unlock();
		if (pickSucceeded == 1 &&
			lineSelectionPicker.GetActor() == lineModel.getLineSelectionActor())
		{
			if (renWin.getCursor().getType() != Cursor.HAND_CURSOR)
				renWin.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			currentEditMode = EditMode.VERTEX_DRAG;
		}
		else
		{
			if (renWin.getCursor().getType() != Cursor.DEFAULT_CURSOR)
				renWin.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			
			currentEditMode = EditMode.VERTEX_ADD;
		}
	}
	
	
//	public void stopEditing()
//	{
//		currentlyDrawing = false;
//		lineIdBeingEdited = -1;
//		vertexIdBeingEdited = -1;
//
//		//this.pcs.firePropertyChange(Properties.FINISHED_DRAWING_LINE, null, null);
//	}
	/*
	public void setEditMode(EditMode mode)
	{
		this.currentEditMode = mode;
		vertexIdBeingEdited = -1;
	}
	*/
}
