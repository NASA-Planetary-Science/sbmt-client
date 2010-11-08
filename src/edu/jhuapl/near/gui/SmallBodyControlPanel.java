package edu.jhuapl.near.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import edu.jhuapl.near.model.*;

public class SmallBodyControlPanel extends JPanel implements ItemListener, ChangeListener 
{
    private JCheckBox modelCheckBox;
    private ModelManager modelManager;
    private JCheckBox showColoringCheckBox;
    private ArrayList<JRadioButton> coloringButtons = new ArrayList<JRadioButton>();
    private JComboBox customColorRedComboBox;
    private JComboBox customColorGreenComboBox;
    private JComboBox customColorBlueComboBox;
    private JLabel customColorRedLabel;
    private JLabel customColorGreenLabel;
    private JLabel customColorBlueLabel;
    private JRadioButton flatShadingButton;
    private JRadioButton smoothShadingButton;
    private JRadioButton lowResModelButton;
    private JRadioButton medResModelButton;
    private JRadioButton highResModelButton;
    private JRadioButton veryHighResModelButton;
    private ButtonGroup coloringButtonGroup;
    private ButtonGroup shadingButtonGroup;
    private ButtonGroup resolutionButtonGroup;
    private JCheckBox gridCheckBox;
    private JCheckBox imageMapCheckBox;
    private JLabel opacityLabel;
	private JSpinner imageMapOpacitySpinner;


    public SmallBodyControlPanel(ModelManager modelManager, String bodyName) 
    {
		super(new BorderLayout());
		this.modelManager = modelManager;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

		modelCheckBox = new JCheckBox();
    	modelCheckBox.setText("Show " + bodyName);
    	modelCheckBox.setSelected(true);
    	modelCheckBox.addItemListener(this);

        JLabel resolutionLabel = new JLabel("Resolution");
        
        lowResModelButton = new JRadioButton(SmallBodyModel.LowResModelStr);
        lowResModelButton.setActionCommand(SmallBodyModel.LowResModelStr);
        lowResModelButton.addItemListener(this);
        lowResModelButton.setEnabled(true);
        lowResModelButton.setToolTipText(
        		"<html>Click here to show a low resolution model of " + bodyName + " <br />" +
        		"containing 49152 plates or triangles</html>");
		
        medResModelButton = new JRadioButton(SmallBodyModel.MedResModelStr);
        medResModelButton.setActionCommand(SmallBodyModel.MedResModelStr);
        medResModelButton.addItemListener(this);
        medResModelButton.setEnabled(true);
        medResModelButton.setToolTipText(
        		"<html>Click here to show a medium resolution model of " + bodyName + " <br />" +
        		"containing 196608 plates or triangles</html>");
		
        highResModelButton = new JRadioButton(SmallBodyModel.HighResModelStr);
        highResModelButton.setActionCommand(SmallBodyModel.HighResModelStr);
        highResModelButton.addItemListener(this);
        highResModelButton.setEnabled(true);
        highResModelButton.setToolTipText(
        		"<html>Click here to show a high resolution model of " + bodyName + " <br />" +
        		"containing 786432 plates or triangles</html>");

        veryHighResModelButton = new JRadioButton(SmallBodyModel.VeryHighResModelStr);
        veryHighResModelButton.setActionCommand(SmallBodyModel.VeryHighResModelStr);
        veryHighResModelButton.addItemListener(this);
        veryHighResModelButton.setEnabled(true);
        veryHighResModelButton.setToolTipText(
        		"<html>Click here to show a very high resolution model of " + bodyName + " <br />" +
        		"containing 3145728 plates or triangles <br />" +
        		"Warning: A high-end graphics card and several gigabytes of RAM are required for best performance.</html>");
		
		resolutionButtonGroup = new ButtonGroup();
		resolutionButtonGroup.add(lowResModelButton);
		resolutionButtonGroup.add(medResModelButton);
		resolutionButtonGroup.add(highResModelButton);
		resolutionButtonGroup.add(veryHighResModelButton);
		resolutionButtonGroup.setSelected(lowResModelButton.getModel(), true);

		showColoringCheckBox = new JCheckBox();
		showColoringCheckBox.setText("Color " + bodyName + " by");
		showColoringCheckBox.setSelected(false);
		showColoringCheckBox.addItemListener(this);

		SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();
		for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
		{
			JRadioButton button = new JRadioButton(smallBodyModel.getColoringName(i));
			button.setActionCommand(smallBodyModel.getColoringName(i));
			button.addItemListener(this);
			button.setEnabled(false);
			coloringButtons.add(button);
		}
		
		if (smallBodyModel.isFalseColoringSupported())
		{
			final String customColor = "Custom";
			JRadioButton button = new JRadioButton(customColor);
			button.setActionCommand(customColor);
			button.addItemListener(this);
			button.setEnabled(false);
			coloringButtons.add(button);

	        customColorRedLabel = new JLabel("Red: ");
	        customColorGreenLabel = new JLabel("Green: ");
	        customColorBlueLabel = new JLabel("Blue: ");

	        Object[] coloringOptions = new Object[smallBodyModel.getNumberOfColors()];
	    	for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
	    		coloringOptions[i] = smallBodyModel.getColoringName(i);
	        
	    	customColorRedComboBox = new JComboBox(coloringOptions);
	        customColorRedComboBox.setMaximumSize(new Dimension(175, 23));
	        customColorRedComboBox.addItemListener(this);
	    	customColorGreenComboBox = new JComboBox(coloringOptions);
	        customColorGreenComboBox.setMaximumSize(new Dimension(175, 23));
	        customColorGreenComboBox.addItemListener(this);
	    	customColorBlueComboBox = new JComboBox(coloringOptions);
	        customColorBlueComboBox.setMaximumSize(new Dimension(175, 23));
	        customColorBlueComboBox.addItemListener(this);

    		customColorRedComboBox.setEnabled(false);
    	    customColorGreenComboBox.setEnabled(false);
    	    customColorBlueComboBox.setEnabled(false);
    	    customColorRedLabel.setEnabled(false);
    	    customColorGreenLabel.setEnabled(false);
    	    customColorBlueLabel.setEnabled(false);
		}
		
		coloringButtonGroup = new ButtonGroup();
		for (int i=0; i<coloringButtons.size(); ++i)
	        coloringButtonGroup.add(coloringButtons.get(i));
		if (coloringButtons.size() > 0)
			coloringButtonGroup.setSelected(coloringButtons.get(0).getModel(), true);

        JLabel shadingLabel = new JLabel("Shading");
        
        flatShadingButton = new JRadioButton(SmallBodyModel.FlatShadingStr);
		flatShadingButton.setActionCommand(SmallBodyModel.FlatShadingStr);
		flatShadingButton.addItemListener(this);
		flatShadingButton.setEnabled(true);
		
		smoothShadingButton = new JRadioButton(SmallBodyModel.SmoothShadingStr);
		smoothShadingButton.setActionCommand(SmallBodyModel.SmoothShadingStr);
		smoothShadingButton.addItemListener(this);
		smoothShadingButton.setEnabled(true);
		
		shadingButtonGroup = new ButtonGroup();
		shadingButtonGroup.add(flatShadingButton);
		shadingButtonGroup.add(smoothShadingButton);
		shadingButtonGroup.setSelected(smoothShadingButton.getModel(), true);

		gridCheckBox = new JCheckBox();
    	gridCheckBox.setText("Show Coordinate Grid");
    	gridCheckBox.setSelected(false);
    	gridCheckBox.addItemListener(this);

    	imageMapCheckBox = new JCheckBox();
    	imageMapCheckBox.setText("Show Image Map");
    	imageMapCheckBox.setSelected(false);
    	imageMapCheckBox.addItemListener(this);

    	opacityLabel = new JLabel("Opacity");
    	imageMapOpacitySpinner = new JSpinner(new SpinnerNumberModel(0.50, 0.0, 1.0, 0.1));
    	imageMapOpacitySpinner.setEditor(new JSpinner.NumberEditor(imageMapOpacitySpinner, "0.00"));
		imageMapOpacitySpinner.setPreferredSize(new Dimension(80, 21));
		imageMapOpacitySpinner.addChangeListener(this);
		opacityLabel.setEnabled(false);
		imageMapOpacitySpinner.setEnabled(false);

    	panel.add(modelCheckBox, "wrap");
    	if (modelManager.getSmallBodyModel().getNumberResolutionLevels() > 1)
    	{
    		panel.add(resolutionLabel, "wrap");
    		panel.add(lowResModelButton, "wrap, gapleft 25");
    		panel.add(medResModelButton, "wrap, gapleft 25");
    		panel.add(highResModelButton, "wrap, gapleft 25");
    		panel.add(veryHighResModelButton, "wrap, gapleft 25");
    	}
    	if (modelManager.getSmallBodyModel().isColoringDataAvailable())
    	{
    		panel.add(showColoringCheckBox, "wrap");
    		for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
        		panel.add(coloringButtons.get(i), "wrap, gapleft 25");
    		if (smallBodyModel.isFalseColoringSupported())
    		{
    			panel.add(coloringButtons.get(coloringButtons.size()-1), "wrap, gapleft 25");
    			
    			panel.add(customColorRedLabel, "gapleft 50, split 2, align right");
    			panel.add(customColorRedComboBox, "wrap");
    			panel.add(customColorGreenLabel, "gapleft 50, split 2, align right");
    			panel.add(customColorGreenComboBox, "wrap");
    			panel.add(customColorBlueLabel, "gapleft 50, split 2, align right");
    			panel.add(customColorBlueComboBox, "wrap");
    		}
    	}
    	if (modelManager.getSmallBodyModel().isImageMapAvailable())
    	{
    		panel.add(imageMapCheckBox, "wrap");
    		panel.add(opacityLabel, "gapleft 25, split 2");
    		panel.add(imageMapOpacitySpinner, "wrap");
    	}
    	panel.add(gridCheckBox, "wrap");
    	panel.add(shadingLabel, "wrap");
    	panel.add(flatShadingButton, "wrap, gapleft 25");
    	panel.add(smoothShadingButton, "wrap, gapleft 25");
    	
    	add(panel, BorderLayout.CENTER);
	}

	public void itemStateChanged(ItemEvent e) 
	{
		SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

		if (e.getItemSelectable() == this.modelCheckBox)
		{
			// In the following we ensure that the graticule is shown
			// only if the shape model is shown
			Graticule graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				smallBodyModel.setShowSmallBody(true);
				if (gridCheckBox.isSelected())
					graticule.setShowGraticule(true);
			}
			else
			{
				smallBodyModel.setShowSmallBody(false);
				if (gridCheckBox.isSelected())
					graticule.setShowGraticule(false);
			}
		}
		else if (e.getItemSelectable() == this.gridCheckBox)
		{
			Graticule graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
			if (e.getStateChange() == ItemEvent.SELECTED)
				graticule.setShowGraticule(true);
			else
				graticule.setShowGraticule(false);
		}
		else if (e.getItemSelectable() == this.imageMapCheckBox)
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				smallBodyModel.setShowImageMap(true);
				if (this.showColoringCheckBox.isSelected())
				{
					opacityLabel.setEnabled(true);
					imageMapOpacitySpinner.setEnabled(true);
				}
			}
			else
			{
				smallBodyModel.setShowImageMap(false);
				opacityLabel.setEnabled(false);
				imageMapOpacitySpinner.setEnabled(false);
			}
		}
		else if (e.getItemSelectable() == this.flatShadingButton)
		{
			if (this.flatShadingButton.isSelected())
				smallBodyModel.setShadingToFlat();
		}
		else if (e.getItemSelectable() == this.smoothShadingButton)
		{
			if (this.smoothShadingButton.isSelected())
				smallBodyModel.setShadingToSmooth();
		}
		else if (e.getItemSelectable() == this.lowResModelButton)
		{
			if (this.lowResModelButton.isSelected())
				try {
					smallBodyModel.setModelResolution(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		else if (e.getItemSelectable() == this.medResModelButton)
		{
			if (this.medResModelButton.isSelected())
				try {
					smallBodyModel.setModelResolution(1);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		else if (e.getItemSelectable() == this.highResModelButton)
		{
			if (this.highResModelButton.isSelected())
				try {
					smallBodyModel.setModelResolution(2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		else if (e.getItemSelectable() == this.veryHighResModelButton)
		{
			if (this.veryHighResModelButton.isSelected())
				try {
					smallBodyModel.setModelResolution(3);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		else if (e.getItemSelectable() == this.showColoringCheckBox)
		{
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
	    		for (int i=0; i<coloringButtons.size(); ++i)
	    			coloringButtons.get(i).setEnabled(false);
	    		if (smallBodyModel.isFalseColoringSupported())
	    		{
	    			customColorRedComboBox.setEnabled(false);
	    			customColorGreenComboBox.setEnabled(false);
	    			customColorBlueComboBox.setEnabled(false);
	    			customColorRedLabel.setEnabled(false);
	    			customColorGreenLabel.setEnabled(false);
	    			customColorBlueLabel.setEnabled(false);
	    		}
	    		
				try 
				{
					smallBodyModel.setColoringIndex(-1);
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
				
				opacityLabel.setEnabled(false);
				imageMapOpacitySpinner.setEnabled(false);
			}
			else
			{
	    		for (int i=0; i<coloringButtons.size(); ++i)
	    			coloringButtons.get(i).setEnabled(true);
	    		if (smallBodyModel.isFalseColoringSupported())
	    		{
	    			customColorRedComboBox.setEnabled(true);
	    			customColorGreenComboBox.setEnabled(true);
	    			customColorBlueComboBox.setEnabled(true);
	    			customColorRedLabel.setEnabled(true);
	    			customColorGreenLabel.setEnabled(true);
	    			customColorBlueLabel.setEnabled(true);
	    		}

				setColoring();

				if (imageMapCheckBox.isSelected())
				{
					opacityLabel.setEnabled(true);
					imageMapOpacitySpinner.setEnabled(true);
				}
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
		SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

		try 
		{
			if (this.showColoringCheckBox.isSelected())
			{
				for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
				{
					if (coloringButtonGroup.getSelection() == this.coloringButtons.get(i).getModel())
					{
						smallBodyModel.setColoringIndex(i);
						return;
					}
				}
				
				// If the false coloring option is selected (which is the last radio button
				// and is not included in the previous for loop)
				if (coloringButtonGroup.getSelection() ==
					this.coloringButtons.get(smallBodyModel.getNumberOfColors()).getModel())
				{
					smallBodyModel.setFalseColoring(
							customColorRedComboBox.getSelectedIndex(),
							customColorGreenComboBox.getSelectedIndex(),
							customColorBlueComboBox.getSelectedIndex());
				}
			}

		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
	}

	public void stateChanged(ChangeEvent e)
	{
		SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();

		double val = (Double)imageMapOpacitySpinner.getValue();

		smallBodyModel.setImageMapOpacity(val);
	}
}
