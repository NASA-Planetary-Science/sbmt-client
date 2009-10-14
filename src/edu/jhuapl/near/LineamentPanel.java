package edu.jhuapl.near;

import javax.swing.*;
import java.awt.*;
import java.awt.*;
import java.awt.event.*;

public class LineamentPanel extends JPanel implements ItemListener
{
    private JCheckBox lineamentCheckBox;
    private ImageGLWidget viewer;

    public LineamentPanel(ImageGLWidget viewer) 
    {
		super(new BorderLayout());
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

   
        add(pane, BorderLayout.CENTER);
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
