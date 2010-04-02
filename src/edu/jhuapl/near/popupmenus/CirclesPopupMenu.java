package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.ModelManager;

public class CirclesPopupMenu extends PopupMenu
{
	private int cellIdLastClicked = -1;
	private CircleModel model = null;
	
	public CirclesPopupMenu(ModelManager modelManager)
	{
		this.model = (CircleModel)modelManager.getModel(ModelManager.CIRCLE_STRUCTURES);
		
		JMenuItem mi; 
		mi = new JMenuItem(new DeleteAction());
		mi.setText("Delete");
		this.add(mi);
	}

	private class DeleteAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			int idx = model.getPolygonIdFromBoundaryCellId(cellIdLastClicked);
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
