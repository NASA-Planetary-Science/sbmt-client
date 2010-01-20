package edu.jhuapl.near.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import edu.jhuapl.near.model.*;

public class ErosControlPanel extends JPanel implements ItemListener 
{
    private JCheckBox modelCheckBox;
    private ModelManager modelManager;
    private JCheckBox showColoringCheckBox;
    private JRadioButton elevationButton;
    private JRadioButton gravitationalAccelerationButton;
    private JRadioButton gravitationalPotentialButton;
    private JRadioButton slopeButton;

    static private String elevStr = "Elevation";
    static private String gravAccStr = "Gravitational Acceleration";
    static private String gravPotStr = "Gravitational Potential";
    static private String slopeStr = "Slope";

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

		showColoringCheckBox = new JCheckBox();
		showColoringCheckBox.setText("Color Eros by");
		showColoringCheckBox.setSelected(false);
		showColoringCheckBox.addItemListener(this);

		elevationButton = new JRadioButton(elevStr);
		elevationButton.setActionCommand(elevStr);
		elevationButton.addItemListener(this);
		elevationButton.setEnabled(false);
		
		gravitationalAccelerationButton = new JRadioButton(gravAccStr);
		gravitationalAccelerationButton.setActionCommand(gravAccStr);
		gravitationalAccelerationButton.addItemListener(this);
		gravitationalAccelerationButton.setEnabled(false);
		
		gravitationalPotentialButton = new JRadioButton(gravPotStr);
		gravitationalPotentialButton.setActionCommand(gravPotStr);
		gravitationalPotentialButton.addItemListener(this);
		gravitationalPotentialButton.setEnabled(false);
		
		slopeButton = new JRadioButton(slopeStr);
		slopeButton.setActionCommand(slopeStr);
		slopeButton.addItemListener(this);
		slopeButton.setEnabled(false);
		
		ButtonGroup group = new ButtonGroup();
        group.add(elevationButton);
        group.add(gravitationalAccelerationButton);
        group.add(gravitationalPotentialButton);
        group.add(slopeButton);
        group.setSelected(elevationButton.getModel(), true);
        
    	panel.add(modelCheckBox);
    	panel.add(Box.createVerticalStrut(10));
    	panel.add(showColoringCheckBox);
    	panel.add(elevationButton);
    	panel.add(gravitationalAccelerationButton);
    	panel.add(gravitationalPotentialButton);
    	panel.add(slopeButton);
    	
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
		else if (e.getItemSelectable() == this.showColoringCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				elevationButton.setEnabled(false);
				gravitationalAccelerationButton.setEnabled(false);
				gravitationalPotentialButton.setEnabled(false);
				slopeButton.setEnabled(false);
			}
			else
			{
				elevationButton.setEnabled(true);
				gravitationalAccelerationButton.setEnabled(true);
				gravitationalPotentialButton.setEnabled(true);
				slopeButton.setEnabled(true);
			}
		}
		else if (e.getItemSelectable() == this.elevationButton)
		{
			if (this.showColoringCheckBox.isSelected())
			{
				
			}
		}
		else if (e.getItemSelectable() == this.gravitationalAccelerationButton)
		{
			if (this.showColoringCheckBox.isSelected())
			{
				
			}
		}
		else if (e.getItemSelectable() == this.gravitationalPotentialButton)
		{
			if (this.showColoringCheckBox.isSelected())
			{
				
			}
		}
		else if (e.getItemSelectable() == this.slopeButton)
		{
			if (this.showColoringCheckBox.isSelected())
			{
				
			}
		}
	
	}

}
