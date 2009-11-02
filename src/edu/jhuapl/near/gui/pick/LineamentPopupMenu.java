package edu.jhuapl.near.gui.pick;

import javax.swing.*;

import edu.jhuapl.near.gui.ColorChooser;
import edu.jhuapl.near.model.LineamentModel;
import edu.jhuapl.near.model.LineamentModel.Lineament;

import java.awt.*;
import java.awt.event.*;

public class LineamentPopupMenu extends JPopupMenu 
{
	private enum ColoringType
	{
		ONE_LINEAMENT,
		LINEAMENTS_PER_IMAGE,
		ALL_LINEAMENTS,
	}

	private LineamentModel model;
	private LineamentModel.Lineament lineament;
	private Component invoker;
	
	public LineamentPopupMenu(LineamentModel model)
	{        
		this.model = model;

		JMenuItem mi; 
		mi = new JMenuItem(new ChangeLineamentColorAction(ColoringType.ONE_LINEAMENT));
		mi.setText("Change color of this lineament only");
		this.add(mi);
		mi = new JMenuItem(new ChangeLineamentColorAction(ColoringType.LINEAMENTS_PER_IMAGE));
		mi.setText("Change color of lineaments on this MSI image");
		this.add(mi);
		mi = new JMenuItem(new ChangeLineamentColorAction(ColoringType.ALL_LINEAMENTS));
		mi.setText("Change color of all lineaments");
		this.add(mi);
		
	}
	
	public void show(Component invoker, int x, int y, LineamentModel.Lineament lin)
	{
		this.invoker = invoker;
		lineament = lin;
		super.show(invoker, x, y);
	}
	
    private class ChangeLineamentColorAction extends AbstractAction
    {
    	ColoringType coloringType;
    	
    	public ChangeLineamentColorAction(ColoringType type)
        {
    		this.coloringType = type;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	Color color = ColorChooser.showColorChooser(invoker);
        	int[] c = new int[4];
        	c[0] = color.getRed();
        	c[1] = color.getGreen();
        	c[2] = color.getBlue();
        	c[3] = color.getAlpha();
        	
        	switch(coloringType)
        	{
        	case ONE_LINEAMENT:
        		model.setLineamentColor(lineament.cellId, c);
        		break;
        	case LINEAMENTS_PER_IMAGE:
        		model.setMSIImageLineamentsColor(lineament.cellId, c);
        		break;
        	case ALL_LINEAMENTS:
        		model.setsAllLineamentsColor(c);
        		break;
        	}
        }
    }

}
