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
    private JRadioButton flatShadingButton;
    private JRadioButton smoothShadingButton;
    private ButtonGroup coloringButtonGroup;
    private ButtonGroup shadingButtonGroup;
    

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

		elevationButton = new JRadioButton(ErosModel.ElevStr);
		elevationButton.setActionCommand(ErosModel.ElevStr);
		elevationButton.addItemListener(this);
		elevationButton.setEnabled(false);
		
		gravitationalAccelerationButton = new JRadioButton(ErosModel.GravAccStr);
		gravitationalAccelerationButton.setActionCommand(ErosModel.GravAccStr);
		gravitationalAccelerationButton.addItemListener(this);
		gravitationalAccelerationButton.setEnabled(false);
		
		gravitationalPotentialButton = new JRadioButton(ErosModel.GravPotStr);
		gravitationalPotentialButton.setActionCommand(ErosModel.GravPotStr);
		gravitationalPotentialButton.addItemListener(this);
		gravitationalPotentialButton.setEnabled(false);
		
		slopeButton = new JRadioButton(ErosModel.SlopeStr);
		slopeButton.setActionCommand(ErosModel.SlopeStr);
		slopeButton.addItemListener(this);
		slopeButton.setEnabled(false);
		
		coloringButtonGroup = new ButtonGroup();
        coloringButtonGroup.add(elevationButton);
        coloringButtonGroup.add(gravitationalAccelerationButton);
        coloringButtonGroup.add(gravitationalPotentialButton);
        coloringButtonGroup.add(slopeButton);
        coloringButtonGroup.setSelected(elevationButton.getModel(), true);

        JLabel shadingLabel = new JLabel("Shading");
        
        flatShadingButton = new JRadioButton(ErosModel.FlatShadingStr);
		flatShadingButton.setActionCommand(ErosModel.FlatShadingStr);
		flatShadingButton.addItemListener(this);
		flatShadingButton.setEnabled(true);
		
		smoothShadingButton = new JRadioButton(ErosModel.SmoothShadingStr);
		smoothShadingButton.setActionCommand(ErosModel.SmoothShadingStr);
		smoothShadingButton.addItemListener(this);
		smoothShadingButton.setEnabled(true);
		
		shadingButtonGroup = new ButtonGroup();
		shadingButtonGroup.add(flatShadingButton);
		shadingButtonGroup.add(smoothShadingButton);
		shadingButtonGroup.setSelected(smoothShadingButton.getModel(), true);

    	panel.add(modelCheckBox, "wrap");
    	panel.add(showColoringCheckBox, "wrap");
    	panel.add(elevationButton, "wrap, gapleft 25");
    	panel.add(gravitationalAccelerationButton, "wrap, gapleft 25");
    	panel.add(gravitationalPotentialButton, "wrap, gapleft 25");
    	panel.add(slopeButton, "wrap, gapleft 25");
    	panel.add(shadingLabel, "wrap");
    	panel.add(flatShadingButton, "wrap, gapleft 25");
    	panel.add(smoothShadingButton, "wrap, gapleft 25");
    	
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
		else if (e.getItemSelectable() == this.flatShadingButton)
		{
			if (this.flatShadingButton.isSelected())
				erosModel.setShadingToFlat();
		}
		else if (e.getItemSelectable() == this.smoothShadingButton)
		{
			if (this.smoothShadingButton.isSelected())
				erosModel.setShadingToSmooth();
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
			if (coloringButtonGroup.getSelection() == this.elevationButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.ELEVATION);
			}
			else if (coloringButtonGroup.getSelection() == this.gravitationalAccelerationButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.GRAVITATIONAL_ACCELERATION);
			}
			else if (coloringButtonGroup.getSelection() == this.gravitationalPotentialButton.getModel())
			{
				if (this.showColoringCheckBox.isSelected())
					erosModel.setColorBy(ErosModel.ColoringType.GRAVITATIONAL_POTENTIAL);	
			}
			else if (coloringButtonGroup.getSelection() == this.slopeButton.getModel())
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
