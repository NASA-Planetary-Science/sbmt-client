package edu.jhuapl.near.gui;

import javax.swing.*;
import java.awt.event.*;

public class LineamentControlPanel extends JPanel implements ItemListener
{
    private JCheckBox lineamentCheckBox;
    private ImageGLWidget viewer;

    public LineamentControlPanel(ImageGLWidget viewer) 
    {
		this.viewer = viewer;

        LineamentRadialOffsetChanger radialChanger = new LineamentRadialOffsetChanger(viewer.getLineamentModel());

        lineamentCheckBox = new JCheckBox();
        lineamentCheckBox.setText("Show Lineaments");
        lineamentCheckBox.setSelected(true);
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
				viewer.showLineaments(false);
			else
				viewer.showLineaments(true);
		}	
	}
}
