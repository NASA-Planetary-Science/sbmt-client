package edu.jhuapl.near.gui;

import javax.swing.*;
import javax.swing.event.*;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.pick.PickManager;

public class PointsMappingControlPanel extends
		AbstractStructureMappingControlPanel implements ChangeListener
{
	//private JSlider slider;
	private JSpinner spinner;
	private PointModel pointModel;
	private double sizeScale = 0.01;
	
	public PointsMappingControlPanel(
			ModelManager modelManager,
			PickManager pickManager)
	{
		super(modelManager,
				(StructureModel)modelManager.getModel(ModelManager.POINT_STRUCTURES),
				pickManager,
				PickManager.PickMode.POINT_DRAW,
				false);
		
		pointModel = (PointModel)modelManager.getModel(ModelManager.POINT_STRUCTURES);

		double radius = pointModel.getCurrentRadius();

		JPanel panel = new JPanel();

		JLabel radiusLabel = new JLabel("Radius");
		panel.add(radiusLabel);
		
		SpinnerModel model = new SpinnerNumberModel(radius, //initial value
                0.01,   //min
                2.0,    //max
                0.01);  //step
		
		spinner = new JSpinner(model);
		spinner.addChangeListener(this);
        radiusLabel.setLabelFor(spinner);
        panel.add(spinner);

		JLabel kmLabel = new JLabel("km");
		panel.add(kmLabel);

		add(panel, "span");
	}

	public void stateChanged(ChangeEvent e) 
	{
		Number val = (Number)spinner.getValue();
		System.out.println(val);
		pointModel.setCurrentRadius(val.doubleValue());
	}
}
