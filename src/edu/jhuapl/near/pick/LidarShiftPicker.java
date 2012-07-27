package edu.jhuapl.near.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

/**
 * This class is used to allow the user to translate a lidar track to a new location.
 * This works as follows. All the tracks are translated such that the currently selected
 * point is translated to the point on the asteroid the user clicked on.
 *
 * @author kahneg1
 *
 */
public class LidarShiftPicker extends Picker
{
    private ModelManager modelManager;
    private vtkRenderWindowPanel renWin;
    private SmallBodyModel smallBodyModel;
    private LidarSearchDataCollection lidarModel;
    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker pointPicker;

    public LidarShiftPicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.lidarModel = (LidarSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_SEARCH);

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
        pointPicker.AddPickList(lidarModel.getProps().get(0));
    }

    public void mousePressed(MouseEvent e)
    {
        mouseDragged(e);
    }

    public void mouseDragged(MouseEvent e)
    {
        double[] selectedPoint = lidarModel.getSelectedPoint();
        if (selectedPoint != null)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] pickPosition = smallBodyPicker.GetPickPosition();
                    double[] shift = {
                            pickPosition[0]-selectedPoint[0],
                            pickPosition[1]-selectedPoint[1],
                            pickPosition[2]-selectedPoint[2]};

                    lidarModel.setTranslation(shift);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, pointPicker, renWin);
        if (pickSucceeded == 1 &&
                pointPicker.GetActor() == lidarModel.getProps().get(0))
        {
            if (renWin.getCursor().getType() != Cursor.HAND_CURSOR)
                renWin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        else
        {
            if (renWin.getCursor().getType() != getDefaultCursor())
                renWin.setCursor(new Cursor(getDefaultCursor()));
        }
    }

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }
}
