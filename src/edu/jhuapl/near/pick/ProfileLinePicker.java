package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.*;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.model.eros.ProfileLineModel;

public class ProfileLinePicker extends Picker
{
    private ModelManager modelManager;
    private vtkRenderWindowPanel renWin;
    private SmallBodyModel smallBodyModel;
    private ProfileLineModel lineModel;
    
    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker linePicker;
    private vtkCellPicker lineSelectionPicker;

    private int lineIdBeingEdited = -1;
    
    // vertex within line being edited
    private int vertexIdBeingEdited = -1;
    
    // There are 2 types of line editing possible: 
    //   1. Dragging an existing vertex to a new locations
    //   2. Extending a line by adding new vertices
	public enum EditMode
	{
		VERTEX_DRAG_OR_DELETE,
		VERTEX_ADD
	}

	private EditMode currentEditMode = EditMode.VERTEX_ADD;

	private double[] lastDragPosition;
	
    public ProfileLinePicker(
			Renderer renderer, 
			ModelManager modelManager
			) 
	{
		this.renWin = renderer.getRenderWindowPanel();
		this.modelManager = modelManager;
		this.lineModel = (ProfileLineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
		
		smallBodyPicker = new vtkCellPicker();
		smallBodyPicker.SetTolerance(0.002);
		smallBodyPicker.PickFromListOn();
		smallBodyPicker.InitializePickList();
		smallBodyModel = modelManager.getSmallBodyModel();
		ArrayList<vtkProp> actors = smallBodyModel.getProps();
		smallBodyPicker.GetPickList().RemoveAllItems();
		for (vtkProp act : actors)
		{
			smallBodyPicker.AddPickList(act);
		}
		smallBodyPicker.AddLocator(smallBodyModel.getCellLocator());

		linePicker = new vtkCellPicker();
		linePicker.SetTolerance(0.002);
		linePicker.PickFromListOn();
		linePicker.InitializePickList();
		linePicker.GetPickList().RemoveAllItems();
		linePicker.AddPickList(lineModel.getLineActor());
		
		lineSelectionPicker = new vtkCellPicker();
		lineSelectionPicker.SetTolerance(0.008);
		lineSelectionPicker.PickFromListOn();
		lineSelectionPicker.InitializePickList();
		lineSelectionPicker.GetPickList().RemoveAllItems();
		lineSelectionPicker.AddPickList(lineModel.getLineSelectionActor());
	}
	
	public void mousePressed(MouseEvent e) 
	{
		// If we pressed a vertex of an existing lineament, begin dragging that vertex.
		// If we pressed a point on the body, begin drawing a new line.


		lineIdBeingEdited = -1;
		vertexIdBeingEdited = -1;
		lastDragPosition = null;
		
		if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
		{
			if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
				return;

			renWin.lock();
			int pickSucceeded = lineSelectionPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();
			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = lineSelectionPicker.GetActor();

				if (pickedActor == lineModel.getLineSelectionActor())
				{
					if (e.getButton() == MouseEvent.BUTTON1)
					{
						int cellId = lineSelectionPicker.GetCellId();
						//this.vertexIdBeingEdited = lineModel.getVertexIdFromSelectionCellId(cellId);
						this.lineIdBeingEdited = lineModel.getLineIdFromSelectionCellId(cellId);
						lineModel.selectStructure(lineIdBeingEdited);
						//lineModel.selectCurrentLineVertex(vertexIdBeingEdited);

						this.vertexIdBeingEdited = lineSelectionPicker.GetCellId();
						lineModel.selectCurrentLineVertex(vertexIdBeingEdited);
					}
					else
					{
						vertexIdBeingEdited = -1;
						lineModel.selectStructure(-1);
					}
				}
			}
		}
		else if (this.currentEditMode == EditMode.VERTEX_ADD)
		{
			if (e.getButton() != MouseEvent.BUTTON1)
				return;

			renWin.lock();
			int pickSucceeded = smallBodyPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();

			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = smallBodyPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model == smallBodyModel)
				{
					double[] pos = smallBodyPicker.GetPickPosition();
					if (e.getClickCount() == 1)
					{
						System.err.println(lineModel.getNumberOfStructures());
						System.err.println(lineModel.getSelectedStructureIndex());
						lineModel.insertVertexIntoSelectedLine(pos);
					}
				}
			}		
		}
	}
	
	public void mouseReleased(MouseEvent e) 
	{
		if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
				vertexIdBeingEdited >= 0 &&
				lastDragPosition != null)
		{
			lineModel.updateSelectedLineVertex(vertexIdBeingEdited, lastDragPosition);
		}

		vertexIdBeingEdited = -1;
	}
	
	public void mouseDragged(MouseEvent e) 
	{
		//if (e.getButton() != MouseEvent.BUTTON1)
		//	return;

		
		if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
			vertexIdBeingEdited >= 0)
		{
			renWin.lock();
			int pickSucceeded = smallBodyPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();
			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = smallBodyPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model == smallBodyModel)
				{
					lastDragPosition = smallBodyPicker.GetPickPosition();

					lineModel.moveSelectionVertex(vertexIdBeingEdited, lastDragPosition);
				}
			}
		}
	}

	
	public void mouseMoved(MouseEvent e) 
	{
		renWin.lock();
		int pickSucceeded = lineSelectionPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
		renWin.unlock();
		if (pickSucceeded == 1 &&
			lineSelectionPicker.GetActor() == lineModel.getLineSelectionActor())
		{
			if (renWin.getCursor().getType() != Cursor.HAND_CURSOR)
				renWin.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			currentEditMode = EditMode.VERTEX_DRAG_OR_DELETE;
		}
		else
		{
			if (renWin.getCursor().getType() != Cursor.DEFAULT_CURSOR)
				renWin.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			
			currentEditMode = EditMode.VERTEX_ADD;
		}
	}
}
