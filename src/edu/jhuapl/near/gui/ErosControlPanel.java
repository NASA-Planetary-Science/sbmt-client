package edu.jhuapl.near.gui;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

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
    private ButtonGroup buttonGroup;
    
    static private String elevStr = "Elevation";
    static private String gravAccStr = "Gravitational Acceleration";
    static private String gravPotStr = "Gravitational Potential";
    static private String slopeStr = "Slope";

    public ErosControlPanel(ModelManager modelManager) 
    {
		super(new BorderLayout());
		this.modelManager = modelManager;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

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
		
		buttonGroup = new ButtonGroup();
        buttonGroup.add(elevationButton);
        buttonGroup.add(gravitationalAccelerationButton);
        buttonGroup.add(gravitationalPotentialButton);
        buttonGroup.add(slopeButton);
        buttonGroup.setSelected(elevationButton.getModel(), true);
        
    	panel.add(modelCheckBox, "wrap");
    	panel.add(showColoringCheckBox, "wrap");
    	panel.add(elevationButton, "wrap, gapleft 25");
    	panel.add(gravitationalAccelerationButton, "wrap, gapleft 25");
    	panel.add(gravitationalPotentialButton, "wrap, gapleft 25");
    	panel.add(slopeButton, "wrap, gapleft 25");
    	
    	add(panel, BorderLayout.CENTER);
	}

	public void itemStateChanged(ItemEvent e) 
	{
		ErosModel erosModel = (ErosModel)modelManager.getModel(ModelManager.EROS);

		if (e.getItemSelectable() == this.modelCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
				erosModel.setShowEros(false);
			else
				erosModel.setShowEros(true);
		}
		else if (e.getItemSelectable() == this.showColoringCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				elevationButton.setEnabled(false);
				gravitationalAccelerationButton.setEnabled(false);
				gravitationalPotentialButton.setEnabled(false);
				slopeButton.setEnabled(false);
				try 
				{
					erosModel.setColorBy(ErosModel.ColoringType.NONE);
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			else
			{
				elevationButton.setEnabled(true);
				gravitationalAccelerationButton.setEnabled(true);
				gravitationalPotentialButton.setEnabled(true);
				slopeButton.setEnabled(true);
				setColoring();
			}
		}
		else
		{
			if (this.showColoringCheckBox.isSelected())
				setColoring();
		}
	}

	private void setColoring()
	{
		ErosModel erosModel = (ErosModel)modelManager.getModel(ModelManager.EROS);

		try 
		{
			if (buttonGroup.getSelection() == this.elevationButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.ELEVATION);
			}
			else if (buttonGroup.getSelection() == this.gravitationalAccelerationButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.GRAVITATIONAL_ACCELERATION);
			}
			else if (buttonGroup.getSelection() == this.gravitationalPotentialButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.GRAVITATIONAL_POTENTIAL);	
			}
			else if (buttonGroup.getSelection() == this.slopeButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.SLOPE);	
			}
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
	}
}
