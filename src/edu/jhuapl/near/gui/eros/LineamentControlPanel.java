package edu.jhuapl.near.gui.eros;

import javax.swing.*;

import edu.jhuapl.near.gui.RadialOffsetChanger;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.model.eros.LineamentModel;

import java.awt.event.*;

public class LineamentControlPanel extends JPanel implements ItemListener
{
    private JCheckBox lineamentCheckBox;
    private LineamentModel lineamentModel;
    
    public LineamentControlPanel(ErosModelManager modelManager) 
    {
		lineamentModel = (LineamentModel)modelManager.getModel(ModelNames.LINEAMENT);
        RadialOffsetChanger radialChanger = new RadialOffsetChanger(lineamentModel, "Radial Offset");

        lineamentCheckBox = new JCheckBox();
        lineamentCheckBox.setText("Show Lineaments");
        lineamentCheckBox.setSelected(false);
        lineamentCheckBox.addItemListener(this);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane,
        		BoxLayout.PAGE_AXIS));
        
        pane.add(lineamentCheckBox);
        pane.add(Box.createVerticalStrut(5));
        pane.add(radialChanger);

   
        add(pane);
	}

	public void itemStateChanged(ItemEvent e) 
	{
		if (e.getItemSelectable() == this.lineamentCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
				lineamentModel.setShowLineaments(false);
			else
				lineamentModel.setShowLineaments(true);
		}	
	}
}
