package edu.jhuapl.near;

import javax.swing.*;

import java.awt.*;
import java.awt.*;
import java.awt.event.*;

public class ErosPanel extends JPanel implements ItemListener 
{
    private JCheckBox modelCheckBox;
    private ImageGLWidget viewer;

    public ErosPanel(ImageGLWidget viewer) 
    {
		super(new BorderLayout());
		this.viewer = viewer;

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
				viewer.showModel(false);
			else
				viewer.showModel(true);
		}
	
	}

}
