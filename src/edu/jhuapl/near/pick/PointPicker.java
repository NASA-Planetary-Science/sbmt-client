package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.*;

import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.model.*;

public class PointPicker extends Picker
{
    private ModelManager modelManager;
	//private ErosRenderer erosRenderer;
    private vtkRenderWindowPanel renWin;
    private ErosModel erosModel;
    private PointModel pointModel;
    
    private vtkCellPicker erosPicker;
    private vtkCellPicker pointPicker;

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
	
    public PointPicker(
			ErosRenderer erosRenderer, 
			ModelManager modelManager
			) 
	{
    	//this.erosRenderer = erosRenderer;
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;
		this.pointModel = (PointModel)modelManager.getModel(ModelManager.POINT_STRUCTURES);
		
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

		pointPicker = new vtkCellPicker();
		pointPicker.SetTolerance(0.002);
		pointPicker.PickFromListOn();
		pointPicker.InitializePickList();
		pointPicker.GetPickList().RemoveAllItems();
		pointPicker.AddPickList(pointModel.getPointActor());
	}
	
	public void mousePressed(MouseEvent e) 
	{
		// If we pressed a vertex of an existing point, begin dragging that vertex.
		// If we pressed a point on Eros, begin drawing a new point.


		vertexIdBeingEdited = -1;
		lastDragPosition = null;
		
		if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
		{
			if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
				return;

			renWin.lock();
			int pickSucceeded = pointPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();
			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = pointPicker.GetActor();

				if (pickedActor == pointModel.getPointActor())
				{
					if (e.getButton() == MouseEvent.BUTTON1)
					{
						int cellId = pointPicker.GetCellId();
						int pointId = pointModel.getPointIdFromCellId(cellId);
						this.vertexIdBeingEdited = pointId;
					}
					else
					{
						vertexIdBeingEdited = -1;
					}
				}
			}
		}
		else if (this.currentEditMode == EditMode.VERTEX_ADD)
		{
			if (e.getButton() != MouseEvent.BUTTON1)
				return;

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
						pointModel.addNewStructure(pos);
					}
				}
			}		
		}
	}
	
	public void mouseReleased(MouseEvent e) 
	{
//		if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
//				vertexIdBeingEdited >= 0 &&
//				lastDragPosition != null)
//		{
//			pointModel.updateSelectedLineVertex(vertexIdBeingEdited, lastDragPosition);
//		}
//
//		vertexIdBeingEdited = -1;
	}
	
	public void mouseDragged(MouseEvent e) 
	{
		//if (e.getButton() != MouseEvent.BUTTON1)
		//	return;

		
		if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
			vertexIdBeingEdited >= 0)
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
					lastDragPosition = erosPicker.GetPickPosition();

					pointModel.movePoint(vertexIdBeingEdited, lastDragPosition);
				}
			}
		}
	}

	
	public void mouseMoved(MouseEvent e) 
	{
		renWin.lock();
		int pickSucceeded = pointPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
		renWin.unlock();
		if (pickSucceeded == 1 &&
				pointPicker.GetActor() == pointModel.getPointActor())
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
