package edu.jhuapl.sbmt.gui.lidar;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.EditMode;
import edu.jhuapl.saavtk.pick.PickUtilEx;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

/**
 * Class which provides the functionality that enables a user to translate a
 * lidar track to a new location. All points associated with the lidar track
 * will be translated an equal amount.
 * <P>
 * The translation action is triggered when the user selects on a specific point
 * on the lidar track and then proceeds to drag that point to a new location.
 */
public class LidarShiftPicker extends Picker
{
	// Reference vars
	private ModelManager refModelManager;
	private PolyhedralModel refSmallBodyModel;
	private LidarSearchDataCollection refLidarModel;
	private vtkJoglPanelComponent refRenWin;

	// VTK vars
	private vtkCellPicker smallBodyPicker;
	private vtkCellPicker pointPicker;

	// State vars
	private EditMode currEditMode;
	private Vector3D origPt;
	private int trackIdx = -1;

	public LidarShiftPicker(Renderer renderer, ModelManager modelManager, LidarSearchDataCollection lidarModel)
	{
		refModelManager = modelManager;
		refSmallBodyModel = (PolyhedralModel) modelManager.getPolyhedralModel();
		refLidarModel = lidarModel;
		refRenWin = renderer.getRenderWindowPanel();

		smallBodyPicker = PickUtilEx.formSmallBodyPicker(refSmallBodyModel);
		pointPicker = PickUtilEx.formStructurePicker((vtkActor) lidarModel.getProps().get(0));

		currEditMode = EditMode.CLICKABLE;
		origPt = null;
		trackIdx = -1;
	}

	@Override
	public int getCursorType()
	{
		if (currEditMode == EditMode.DRAGGABLE)
			return Cursor.HAND_CURSOR;

		return Cursor.CROSSHAIR_CURSOR;
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (origPt != null)
			return true;

		return false;
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// Assume nothing will be picked
		origPt = null;
		trackIdx = -1;

		// Bail if we are not ready to do a drag operation
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if the button-1 or button-3 are not selected
		if (aEvent.getButton() != MouseEvent.BUTTON1 && aEvent.getButton() != MouseEvent.BUTTON3)
			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		// Retrieve the selected track
		pickSucceeded = doPick(aEvent, pointPicker, refRenWin);
		if (pickSucceeded != 1 || pointPicker.GetActor() != refLidarModel.getProps().get(0))
			return;

		int cellId = pointPicker.GetCellId();
		trackIdx = refLidarModel.getTrackIdFromCellId(cellId);
		if (trackIdx >= 0)
			origPt = refLidarModel.getLidarPointFromCellId(cellId).getTargetPosition();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// Reset the state vars
		origPt = null;
		trackIdx = -1;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not in the proper edit mode
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if do not have a valid origPt
		if (origPt == null)
			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model != refSmallBodyModel)
			return;

		// Perform the translation
		double[] currPt = smallBodyPicker.GetPickPosition();
		int[] idArr = {trackIdx};
		Vector3D tmpVect = new Vector3D(currPt[0] - origPt.getX(), currPt[1] - origPt.getY(), currPt[2] - origPt.getZ());
		refLidarModel.setTranslation(idArr, tmpVect);
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		int pickSucceeded = doPick(aEvent, pointPicker, refRenWin);

		if (pickSucceeded == 1 && pointPicker.GetActor() == refLidarModel.getProps().get(0))
			currEditMode = EditMode.DRAGGABLE;
		else
			currEditMode = EditMode.CLICKABLE;

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

}
