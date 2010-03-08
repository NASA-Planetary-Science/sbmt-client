package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.StructureModel;

public class StructuresPopupMenu extends PopupMenu
{
	private StructureModel model = null;
	private int cellIdLastClicked = -1;
	private ModelManager modelManager;
	
	public StructuresPopupMenu(ModelManager modelManager)
	{
		this.modelManager = modelManager;
		
		JMenuItem mi; 
		mi = new JMenuItem(new EditAction());
		mi.setText("Edit");
		this.add(mi);
		mi = new JMenuItem(new DeleteAction());
		mi.setText("Delete");
		this.add(mi);
	}

	public void setModel(StructureModel model, int cellId)
	{
		this.model = model;
		this.cellIdLastClicked = cellId;
	}
	
	private class EditAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			//model
		}
	}

	private class DeleteAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
		}
	}

	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
			double[] pickedPosition)
	{
		setModel((StructureModel)modelManager.getModel(pickedProp),
				pickedCellId);
		show(e.getComponent(), e.getX(), e.getY());
	}
}
