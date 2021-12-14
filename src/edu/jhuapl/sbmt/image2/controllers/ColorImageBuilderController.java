package edu.jhuapl.sbmt.image2.controllers;

import java.util.List;

import javax.swing.JPanel;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image2.model.ColorImageBuilderModel;
import edu.jhuapl.sbmt.image2.ui.color.ColorImageBuilderPanel;

public class ColorImageBuilderController
{

	ColorImageBuilderPanel panel;
	ColorImageBuilderModel model;

	public ColorImageBuilderController(List<SmallBodyModel> smallBodyModels)
	{
		this.model = new ColorImageBuilderModel();

		this.panel = new ColorImageBuilderPanel(smallBodyModels);

		initGUI();
	}

	private void initGUI()
	{

	}



	public JPanel getView()
	{
		return panel;
	}
}
