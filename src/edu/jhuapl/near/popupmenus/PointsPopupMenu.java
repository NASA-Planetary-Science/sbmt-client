package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PointModel;

public class PointsPopupMenu extends PopupMenu
{
	private int cellIdLastClicked = -1;
	private PointModel model;
	
	public PointsPopupMenu(ModelManager modelManager)
	{
		this.model = (PointModel)modelManager.getModel(ModelManager.POINT_STRUCTURES);
		
		JMenuItem mi;
		mi = new JMenuItem(new DeleteAction());
		mi.setText("Delete");
		this.add(mi);
	}

	private class DeleteAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			int idx = model.getPolygonIdFromInteriorCellId(cellIdLastClicked);
			model.removeStructure(idx);
		}
	}

	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
			double[] pickedPosition)
	{
		this.cellIdLastClicked = pickedCellId;
		show(e.getComponent(), e.getX(), e.getY());
	}
}
