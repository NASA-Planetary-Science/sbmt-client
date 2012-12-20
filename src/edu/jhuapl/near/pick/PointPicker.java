package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class PointPicker extends Picker
{
    private ModelManager modelManager;
    private vtkRenderWindowPanel renWin;
    private SmallBodyModel smallBodyModel;
    private PointModel pointModel;

    private vtkCellPicker smallBodyPicker;
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
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.pointModel = (PointModel)modelManager.getModel(ModelNames.POINT_STRUCTURES);

        smallBodyPicker = new vtkCellPicker();
        smallBodyPicker.PickFromListOn();
        smallBodyPicker.InitializePickList();
        smallBodyModel = modelManager.getSmallBodyModel();
        ArrayList<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyPicker.AddPickList(act);
        }
        smallBodyPicker.AddLocator(smallBodyModel.getCellLocator());

        pointPicker = new vtkCellPicker();
        pointPicker.PickFromListOn();
        pointPicker.InitializePickList();
        vtkPropCollection pointPickList = pointPicker.GetPickList();
        pointPickList.RemoveAllItems();
        pointPicker.AddPickList(pointModel.getInteriorActor());
    }

    public void mousePressed(MouseEvent e)
    {
        // If we pressed a vertex of an existing point, begin dragging that vertex.
        // If we pressed a point on the body, begin drawing a new point.


        vertexIdBeingEdited = -1;
        lastDragPosition = null;

        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
        {
            if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
                return;

            int pickSucceeded = doPick(e, pointPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = pointPicker.GetActor();

                if (pickedActor == pointModel.getInteriorActor())
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        int cellId = pointPicker.GetCellId();
                        int pointId = pointModel.getPolygonIdFromInteriorCellId(cellId);
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

            int pickSucceeded = doPick(e, smallBodyPicker, renWin);

            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] pos = smallBodyPicker.GetPickPosition();
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
//        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
//                vertexIdBeingEdited >= 0 &&
//                lastDragPosition != null)
//        {
//            pointModel.updateSelectedLineVertex(vertexIdBeingEdited, lastDragPosition);
//        }
//
//        vertexIdBeingEdited = -1;
    }

    public void mouseDragged(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;


        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
            vertexIdBeingEdited >= 0)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    lastDragPosition = smallBodyPicker.GetPickPosition();

                    if (e.isControlDown() || e.isShiftDown())
                        pointModel.changeRadiusOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else
                        pointModel.movePolygon(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, pointPicker, renWin);
        if (pickSucceeded == 1 &&
                pointPicker.GetActor() == pointModel.getInteriorActor())
        {
            if (renWin.getCursor().getType() != Cursor.HAND_CURSOR)
                renWin.setCursor(new Cursor(Cursor.HAND_CURSOR));

            currentEditMode = EditMode.VERTEX_DRAG_OR_DELETE;
        }
        else
        {
            if (renWin.getCursor().getType() != getDefaultCursor())
                renWin.setCursor(new Cursor(getDefaultCursor()));

            currentEditMode = EditMode.VERTEX_ADD;
        }
    }

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
        {
            int[] highlightedStructures = pointModel.getHighlightedStructures();
            pointModel.removeStructures(highlightedStructures);
        }
    }
}
