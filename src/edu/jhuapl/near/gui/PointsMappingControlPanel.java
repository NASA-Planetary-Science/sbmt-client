package edu.jhuapl.near.gui;

import javax.swing.*;
import javax.swing.event.*;

import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;

public class PointsMappingControlPanel extends
		AbstractStructureMappingControlPanel implements ChangeListener
{
	private JSpinner spinner;
	private PointModel pointModel;
	
	public PointsMappingControlPanel(
			ModelManager modelManager,
			PickManager pickManager)
	{
		super(modelManager,
				(StructureModel)modelManager.getModel(ModelNames.POINT_STRUCTURES),
				pickManager,
				PickManager.PickMode.POINT_DRAW,
				false);
		
		pointModel = (PointModel)modelManager.getModel(ModelNames.POINT_STRUCTURES);

		double diameter = 2.0 * pointModel.getDefaultRadius();

		JPanel panel = new JPanel();

		JLabel radiusLabel = new JLabel("Diameter");
		panel.add(radiusLabel);
		
		double bbLength = modelManager.getSmallBodyModel().getBoundingBoxDiagonalLength();
		double min = bbLength / 4000.0;
		double max =bbLength / 10.0;
		double step = max / 40.0;

		SpinnerModel model = new SpinnerNumberModel(diameter, //initial value
                min,
                max,
                step);
		
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
		pointModel.setDefaultRadius(val.doubleValue()/2.0);
		pointModel.changeRadiusOfAllPolygons(val.doubleValue()/2.0);
	}
}
