package edu.jhuapl.near.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import edu.jhuapl.near.model.*;

public class ErosControlPanel extends JPanel implements ItemListener 
{
    private JCheckBox modelCheckBox;
    private ModelManager modelManager;
    
    public ErosControlPanel(ModelManager modelManager) 
    {
		super(new BorderLayout());
		this.modelManager = modelManager;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		modelCheckBox = new JCheckBox();
    	modelCheckBox.setText("Show Eros");
    	modelCheckBox.setSelected(true);
    	modelCheckBox.addItemListener(this);

    	panel.add(modelCheckBox);

    	add(panel, BorderLayout.CENTER);
	}

	public void itemStateChanged(ItemEvent e) 
	{
		if (e.getItemSelectable() == this.modelCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
				((ErosModel)modelManager.getModel(ModelManager.EROS)).setShowEros(false);
			else
				((ErosModel)modelManager.getModel(ModelManager.EROS)).setShowEros(true);
		}
	
	}

}
