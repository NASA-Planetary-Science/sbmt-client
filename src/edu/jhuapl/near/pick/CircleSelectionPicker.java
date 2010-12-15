package edu.jhuapl.near.pick;

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
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class CircleSelectionPicker extends Picker
{
    private ModelManager modelManager;
    private vtkRenderWindowPanel renWin;
    private SmallBodyModel smallBodyModel;
    private RegularPolygonModel circleModel;

    private vtkCellPicker smallBodyPicker;

    private int vertexIdBeingEdited = -1;

    public CircleSelectionPicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.circleModel = (RegularPolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);

        smallBodyPicker = new vtkCellPicker();
        smallBodyPicker.SetTolerance(0.002);
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
    }

    public void mousePressed(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;

        vertexIdBeingEdited = -1;

        circleModel.removeAllStructures();

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
        //    return;


        if (vertexIdBeingEdited >= 0)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] lastDragPosition = smallBodyPicker.GetPickPosition();

                    circleModel.changeRadiusOfPolygon(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }
}
