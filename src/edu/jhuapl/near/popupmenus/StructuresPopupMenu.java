package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.StructureModel;

public class StructuresPopupMenu extends JPopupMenu
{
	private StructureModel model = null;
	private int cellIdLastClicked = -1;
	
	public StructuresPopupMenu()
	{
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
}
