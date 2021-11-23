package edu.jhuapl.sbmt.image2.controllers;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.ui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;

public class ImageSearchController implements Controller<ImageSearchParametersModel, ImagingSearchPanel>
{
	private ImageListTableController imageListTableController;
	private ImageSearchParametersController searchParametersController;
	private ImagingSearchPanel panel;
	private ImageSearchParametersModel imageSearchModel;

	public ImageSearchController(SmallBodyViewConfig config, PerspectiveImageCollection collection, IImagingInstrument instrument, ModelManager modelManager, Renderer renderer, PickManager pickManager)
	{
		this.imageSearchModel = new ImageSearchParametersModel(config, modelManager, renderer, instrument);
        this.searchParametersController = new ImageSearchParametersController(config, collection, imageSearchModel, modelManager, pickManager);
        this.searchParametersController.setupSearchParametersPanel();
        this.imageListTableController = new ImageListTableController(collection);
        initGUI();
	}

	public void initGUI()
	{
		panel = new ImagingSearchPanel();
		panel.addSubPanel(searchParametersController.getPanel());
		panel.addSubPanel(imageListTableController.getPanel());
	}

	@Override
	public ImageSearchParametersModel getModel()
	{
		return imageSearchModel;
	}

	@Override
	public ImagingSearchPanel getView()
	{
		return panel;
	}
}
