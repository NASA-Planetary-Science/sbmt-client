package edu.jhuapl.near.popupmenus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import edu.jhuapl.near.gui.ColorChooser;
import edu.jhuapl.near.model.StructureModel;

abstract public class StructuresPopupMenu extends PopupMenu
{
	private ChangeColorAction changeColorAction;
	private int cellIdLastClicked = -1;

	protected void addMenuItems(StructureModel model)
	{
		changeColorAction = new ChangeColorAction(model);
		JMenuItem mi = new JMenuItem(changeColorAction);
		mi.setText("Change Color...");
		this.add(mi);
	}

	protected ChangeColorAction getChangeColorAction()
	{
		return changeColorAction;
	}

	protected class DeleteAction extends AbstractAction
	{
    	private StructureModel structureModel;

    	public DeleteAction(StructureModel mod)
		{
			this.structureModel = mod;
		}

		public void actionPerformed(ActionEvent e)
		{
			structureModel.removeStructure(cellIdLastClicked);
		}
	}

    protected static class ChangeColorAction extends AbstractAction
    {
    	private Component invoker;
    	private StructureModel structureModel;
    	private int structureIndex;

    	public ChangeColorAction(StructureModel structureModel)
        {
			this.structureModel = structureModel;
        }

        public void setInvoker(Component invoker)
		{
			this.invoker = invoker;
		}

		public void setStructureIndex(int idx)
		{
			this.structureIndex = idx;
		}

		public void actionPerformed(ActionEvent actionEvent)
        {
        	Color color = ColorChooser.showColorChooser(invoker);

        	if (color == null)
        		return;

        	int[] c = new int[4];
        	c[0] = color.getRed();
        	c[1] = color.getGreen();
        	c[2] = color.getBlue();
        	c[3] = color.getAlpha();

        	structureModel.setStructureColor(structureIndex, c);
        }
    }

}
